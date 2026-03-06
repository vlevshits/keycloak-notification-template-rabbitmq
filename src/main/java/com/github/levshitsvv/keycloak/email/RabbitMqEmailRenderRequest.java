package com.github.levshitsvv.keycloak.email;

import java.util.List;
import java.util.Map;

public class RabbitMqEmailRenderRequest {
    private String realm;
    private String user;
    private String subjectKey;
    private List<Object> subjectAttributes;
    private String templateName;
    private Map<String, Object> attributes;

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSubjectKey() {
        return subjectKey;
    }

    public void setSubjectKey(String subjectKey) {
        this.subjectKey = subjectKey;
    }

    public List<Object> getSubjectAttributes() {
        return subjectAttributes;
    }

    public void setSubjectAttributes(List<Object> subjectAttributes) {
        this.subjectAttributes = subjectAttributes;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
