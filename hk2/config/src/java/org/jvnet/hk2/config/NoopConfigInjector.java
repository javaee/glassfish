package org.jvnet.hk2.config;

/**
 * Used for bringing in {@link ConfigBeanProxy} to the habitat.
 * @author Kohsuke Kawaguchi
 */
public class NoopConfigInjector extends ConfigInjector {
    public void inject(Dom dom, Object target) {
    }

    public void injectElement(Dom dom, String elementName, Object target) {
    }

    public void injectAttribute(Dom dom, String attributeName, Object target) {
    }
}
