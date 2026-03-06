# Keycloak RabbitMQ Email Template Renderer

This Keycloak plugin allows you to delegate email template rendering to an external service via RabbitMQ RPC. It extends the default `FreeMarkerEmailTemplateProvider`, providing a fallback to standard Freemarker rendering if the external service is unavailable.

## Features
- **External Rendering**: Sends attributes and template names to RabbitMQ for rendering.
- **Fallback**: Automatically falls back to default Freemarker templates on RPC timeout or failure.
- **EasyNetQ Compatible**: Designed to work with EasyNetQ's RPC pattern in C#.
- **Branding**: Customized for LevshitsVV.

## Configuration

The plugin can be configured using environment variables with the `KK_RMQ_EMAIL_` prefix:

| Variable | Description | Default |
|----------|-------------|---------|
| `KK_RMQ_EMAIL_URL` | RabbitMQ Host | `localhost` |
| `KK_RMQ_EMAIL_PORT` | RabbitMQ Port | `5672` |
| `KK_RMQ_EMAIL_USERNAME` | RabbitMQ Username | `admin` |
| `KK_RMQ_EMAIL_PASSWORD` | RabbitMQ Password | `admin` |
| `KK_RMQ_EMAIL_VHOST` | RabbitMQ Virtual Host | `/` |
| `KK_RMQ_EMAIL_EXCHANGE` | Exchange for RPC | `keycloak.email.exchange` |
| `KK_RMQ_EMAIL_ROUTING_KEY` | Routing key for requests | `keycloak.email.render` |
| `KK_RMQ_EMAIL_REPLY_TIMEOUT_MS` | RPC Timeout in ms | `10000` |
| `KK_RMQ_EMAIL_MSG_TYPE` | Message type for `type` property | `RabbitMqEmailRenderRequest` |

## Subject Keys

When a rendering request is sent to RabbitMQ, the `subjectKey` field contains one of the following standard Keycloak message keys. Your renderer should use these to determine the intent of the email or to look up the localized subject string.

| `subjectKey` | Default Template | Description |
|--------------|------------------|-------------|
| `passwordResetSubject` | `password-reset.ftl` | Sent when a user requests a password reset. |
| `emailVerificationSubject` | `email-verification.ftl` | Sent when a user needs to verify their email address. |
| `executeActionsSubject` | `execute-actions.ftl` | Sent for administrative actions (e.g., "Update Password"). |
| `identityProviderLinkSubject` | `identity-provider-link.ftl` | Sent when linking an account to an external IDP. |
| `emailUpdateConfirmationSubject` | `email-update-confirmation.ftl` | Sent to confirm an email address change. |
| `eventLoginErrorSubject` | `event-login-error.ftl` | Sent when a login error occurs (if configured). |
| `eventRemoveTotpSubject` | `event-remove-totp.ftl` | Sent when TOTP (MFA) is removed. |
| `eventUpdatePasswordSubject` | `event-update-password.ftl` | Sent after a successful password update. |
| `eventUpdateTotpSubject` | `event-update-totp.ftl` | Sent after a successful TOTP (MFA) update. |
| `orgInviteSubject` | `org-invite.ftl` | Sent for Organization invitations. |
| `testEmailSubject` | `test-email.ftl` | Sent when clicking "Test connection" in SMTP settings. |

## Default Template Wording

Below are the default English strings used by Keycloak. Placeholders like `{0}`, `{1}`, etc., are automatically injected based on the event context.

### 1. Password Reset
- **Subject**: `Reset your password`
- **Body**: `Someone has requested a password reset for your {2} account. If this was you, click the link below to reset your password.\n\n{0}\n\nThis link will expire within {3}.\n\nIf you don't want to reset your password, just ignore this message and nothing will be changed.`

### 2. Email Verification
- **Subject**: `Verify your email`
- **Body**: `Someone has created a {2} account with this email address. If this was you, click the link below to verify your email address.\n\n{0}\n\nThis link will expire within {3}.\n\nIf you didn't create this account, just ignore this message.`

### 3. Execute Actions (e.g. Update Password)
- **Subject**: `Update Your Account`
- **Body**: `Your administrator has just requested that you update your {2} account by performing the following action(s): {3}. Click on the link below to start this process.\n\n{0}\n\nThis link will expire within {4}.\n\nIf you are unaware that your administrator has requested this, just ignore this message and nothing will be changed.`

### 4. Identity Provider Link
- **Subject**: `Link {0}`
- **Body**: `Someone wants to link your {2} account with {1} account of user {0}. If this was you, click the link below to link accounts.\n\n{3}\n\nThis link will expire within {5}.\n\nIf you didn't initiate this process, just ignore this message.`

### 5. Email Update Confirmation
- **Subject**: `Confirm Email Update`
- **Body**: `To update your {2} account with email address {1}, click the link below.\n\n{0}\n\nThis link will expire within {3}.`

### 6. Test Email
- **Subject**: `Test message`
- **Body**: `This is a test message`

## Build

```bash
./mvnw clean package
```

The resulting JAR will be in `target/keycloak-rabbitmq-email-renderer-1.0.0.jar`.

## C# Implementation (EasyNetQ)

To handle rendering requests in C#, you can use the following snippet as a starting point.

### Request/Response Models

```csharp
public class RabbitMqEmailRenderRequest {
    public string realm { get; set; }
    public string user { get; set; }
    public string subjectKey { get; set; }
    public List<object> subjectAttributes { get; set; }
    public string templateName { get; set; }
    public Dictionary<string, object> attributes { get; set; }
}

public class RabbitMqEmailRenderResponse {
    public string subject { get; set; }
    public string textBody { get; set; }
    public string htmlBody { get; set; }
}
```

### Listener Example

```csharp
using EasyNetQ;

var bus = RabbitHutch.CreateBus("host=localhost;username=admin;password=admin");

await bus.Rpc.RespondAsync<RabbitMqEmailRenderRequest, RabbitMqEmailRenderResponse>(request => {
    Console.WriteLine($"Rendering template '{request.templateName}' for user {request.user}");
    
    // Your rendering logic here
    return new RabbitMqEmailRenderResponse {
        subject = "Rendered Subject",
        textBody = "Rendered Text Body",
        htmlBody = "<h1>Rendered HTML Body</h1>"
    };
});
```

> [!NOTE]
> **EasyNetQ Type Compatibility**: By default, the plugin sets the `type` property to `RabbitMqEmailRenderRequest`. If your C# side uses a different namespace or class name, you MUST configure `KK_RMQ_EMAIL_MSG_TYPE` to match the EasyNetQ expected format (e.g., `MyNamespace.MyRequest, MyAssembly`).
