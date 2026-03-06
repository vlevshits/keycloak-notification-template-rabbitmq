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
| `KK_RM_EMAIL_ROUTING_KEY` | Routing key for requests | `keycloak.email.render` |
| `KK_RMQ_EMAIL_REPLY_TIMEOUT_MS` | RPC Timeout in ms | `10000` |
| `KK_RMQ_EMAIL_MSG_TYPE` | Message type for `type` property | `RabbitMqEmailRenderRequest` |

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
