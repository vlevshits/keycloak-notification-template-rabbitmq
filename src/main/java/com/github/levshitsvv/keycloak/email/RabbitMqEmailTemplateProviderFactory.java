package com.github.levshitsvv.keycloak.email;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.email.EmailTemplateProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMqEmailTemplateProviderFactory implements EmailTemplateProviderFactory {

    private static final Logger log = Logger.getLogger(RabbitMqEmailTemplateProviderFactory.class);

    private RabbitMqEmailConfig cfg;
    private Connection connection;
    private Channel channel;

    @Override
    public EmailTemplateProvider create(KeycloakSession session) {
        return new RabbitMqEmailTemplateProvider(session, cfg, channel);
    }

    @Override
    public void init(Config.Scope config) {
        cfg = RabbitMqEmailConfig.createFromScope(config);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(cfg.getUsername());
        factory.setPassword(cfg.getPassword());
        factory.setVirtualHost(cfg.getVhost());
        factory.setHost(cfg.getHostUrl());
        factory.setPort(cfg.getPort());

        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            // Declare exchange if it's not a default one
            if (!"".equals(cfg.getExchange()) && !"amq.topic".equals(cfg.getExchange())
                    && !"amq.direct".equals(cfg.getExchange())) {
                channel.exchangeDeclare(cfg.getExchange(), "direct", true);
            }
        } catch (IOException | TimeoutException e) {
            log.error("Failed to connect to RabbitMQ for Email Template Provider", e);
        }
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
        try {
            if (channel != null && channel.isOpen())
                channel.close();
            if (connection != null && connection.isOpen())
                connection.close();
        } catch (IOException | TimeoutException e) {
            log.warn("Error closing RabbitMQ connection", e);
        }
    }

    @Override
    public String getId() {
        return "rabbitmq-email-renderer";
    }
}
