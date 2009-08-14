package com.sun.enterprise.deployment;

/**
 * This class describes addressing element from webservices.xml .
 *
 * @author Bhakti Mehta
 *
 */
public class Addressing extends Descriptor {

    private boolean enabled;

    private boolean required;

    private String responses ;

    /**
     * copy constructor.
     */
    public Addressing(Addressing other) {
        super(other);
        enabled = other.enabled;
        required = other.required;
        responses = other.responses;
    }

    public Addressing() {
    }

    public Addressing(boolean enabled, boolean req, String resp) {
        this.enabled = enabled;
        this.required = req;
        this.responses = resp;
    }

    public String getResponses() {
        return responses;
    }

    public void setResponses(String responses) {
        this.responses = responses;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


     /**
     * @return a string describing the values I hold
     */
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("\nAddressing enabled = ").append(enabled).append(
            " required = ").append(required).append(" responses = ").append(responses);

    }
}
