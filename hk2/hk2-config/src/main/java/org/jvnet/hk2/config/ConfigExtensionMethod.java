package org.jvnet.hk2.config;

public @interface ConfigExtensionMethod {
    public String value() default "basic-config-extension-handler";
}
