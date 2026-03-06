package com.github.levshitsvv.keycloak.email;

import com.rabbitmq.client.Channel;
import org.jboss.logging.Logger;
import org.keycloak.email.EmailException;
import org.keycloak.email.freemarker.FreeMarkerEmailTemplateProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.util.JsonSerialization;

import java.util.Map;

public class RabbitMqEmailTemplateProvider extends FreeMarkerEmailTemplateProvider {

    private static final Logger log = Logger.getLogger(RabbitMqEmailTemplateProvider.class);

    private final RabbitMqEmailConfig cfg;
    private final Channel channel;

    public RabbitMqEmailTemplateProvider(KeycloakSession session, RabbitMqEmailConfig cfg, Channel channel) {
        super(session);
        this.cfg = cfg;
        this.channel = channel;
    }

    @Override
    public void send(String subjectFormatKey, String bodyTemplate, Map<String, Object> bodyAttributes)
            throws EmailException {
        try {
            renderAndSend(subjectFormatKey, null, bodyTemplate, bodyAttributes);
        } catch (Exception e) {
            log.warn("Failed to render email via RabbitMQ, falling back to FreeMarker", e);
            super.send(subjectFormatKey, bodyTemplate, bodyAttributes);
        }
    }

    @Override
    public void send(String subjectFormatKey, java.util.List<Object> subjectAttributes, String bodyTemplate,
            Map<String, Object> bodyAttributes) throws EmailException {
        try {
            renderAndSend(subjectFormatKey, subjectAttributes, bodyTemplate, bodyAttributes);
        } catch (Exception e) {
            log.warn("Failed to render email via RabbitMQ, falling back to FreeMarker", e);
            super.send(subjectFormatKey, subjectAttributes, bodyTemplate, bodyAttributes);
        }
    }

    private void renderAndSend(String subjectFormatKey, java.util.List<Object> subjectAttributes, String bodyTemplate,
            Map<String, Object> bodyAttributes) throws Exception {
        RabbitMqEmailRenderRequest request = new RabbitMqEmailRenderRequest();
        request.setRealm(realm.getName());
        request.setUser(user.getUsername());
        request.setSubjectKey(subjectFormatKey);
        request.setSubjectAttributes(subjectAttributes);
        request.setTemplateName(bodyTemplate);
        request.setAttributes(bodyAttributes);

        String jsonRequest = JsonSerialization.writeValueAsString(request);

        try (RabbitMqRpcClient rpcClient = new RabbitMqRpcClient(channel)) {
            String jsonResponse = rpcClient.call(cfg.getExchange(), cfg.getRoutingKey(), jsonRequest,
                    cfg.getMsgType(), cfg.getReplyTimeoutMs());
            RabbitMqEmailRenderResponse response = JsonSerialization.readValue(jsonResponse,
                    RabbitMqEmailRenderResponse.class);

            String subject = response.getSubject();
            String textBody = response.getTextBody();
            String htmlBody = response.getHtmlBody();

            if (subject != null && (textBody != null || htmlBody != null)) {
                org.keycloak.email.EmailSenderProvider emailSender = session
                        .getProvider(org.keycloak.email.EmailSenderProvider.class);
                emailSender.send(realm.getSmtpConfig(), user, subject, textBody, htmlBody);
            } else {
                throw new RuntimeException("Invalid response from RabbitMQ renderer: missing subject or body");
            }
        }
    }
}
