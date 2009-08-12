package com.sun.enterprise.deployment;

/**
 * This class describes respect-binding element from webservices.xml .
 *
 * @author Bhakti Mehta
 *
 */
public class RespectBinding extends Descriptor{
    private boolean enabled;

    /**
     * copy constructor.
     */
    public RespectBinding(RespectBinding other) {
        super(other);
        enabled = other.enabled;

    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public RespectBinding() {
    }

    /**
     * @return a string describing the values I hold
     */
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("\nRespectBinding enabled = ").append(enabled);

    }


}
