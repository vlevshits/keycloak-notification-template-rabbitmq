package com.github.levshitsvv.keycloak.email;

import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;

import java.util.Locale;

public class RabbitMqEmailConfig {

    private static final Logger log = Logger.getLogger(RabbitMqEmailConfig.class);

    private String hostUrl;
    private Integer port;
    private String username;
    private String password;
    private String vhost;
    private Boolean useTls;

    private String exchange;
    private String routingKey;
    private Integer replyTimeoutMs;
    private String msgType;
    private Boolean fallbackToDefault;

    public static RabbitMqEmailConfig createFromScope(Scope config) {
        RabbitMqEmailConfig cfg = new RabbitMqEmailConfig();

        cfg.hostUrl = resolveConfigVar(config, "url", "localhost");
        cfg.port = Integer.valueOf(resolveConfigVar(config, "port", "5672"));
        cfg.username = resolveConfigVar(config, "username", "admin");
        cfg.password = resolveConfigVar(config, "password", "admin");
        cfg.vhost = resolveConfigVar(config, "vhost", "");
        cfg.useTls = Boolean.valueOf(resolveConfigVar(config, "use_tls", "false"));

        cfg.exchange = resolveConfigVar(config, "exchange", "keycloak.email.exchange");
        cfg.routingKey = resolveConfigVar(config, "routing_key", "keycloak.email.render");
        cfg.replyTimeoutMs = Integer.valueOf(resolveConfigVar(config, "reply_timeout_ms", "10000"));
        cfg.msgType = resolveConfigVar(config, "msg_type", null);
        cfg.fallbackToDefault = Boolean.valueOf(resolveConfigVar(config, "fallback_to_default", "true"));

        return cfg;
    }

    private static String resolveConfigVar(Scope config, String variableName, String defaultValue) {
        String value = defaultValue;
        if (config != null && config.get(variableName) != null) {
            value = config.get(variableName);
        } else {
            // try from env variables eg: KK_RMQ_EMAIL_URL
            String envVariableName = "KK_RMQ_EMAIL_" + variableName.toUpperCase(Locale.ENGLISH);
            String env = System.getenv(envVariableName);
            if (env != null) {
                value = env;
            }
        }
        if (!"password".equals(variableName)) {
            log.infof("keycloak-rabbitmq-email-renderer configuration: %s=%s", variableName, value);
        }
        return value;
    }

    public String getHostUrl() {
        return hostUrl;
    }

    public Integer getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getVhost() {
        return vhost;
    }

    public Boolean getUseTls() {
        return useTls;
    }

    public String getExchange() {
        return exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public Integer getReplyTimeoutMs() {
        return replyTimeoutMs;
    }

    public String getMsgType() {
        return msgType;
    }

    public Boolean isFallbackToDefault() {
        return fallbackToDefault;
    }
}
