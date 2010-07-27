package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.RunLevel;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Example of a non-default, run level meta annotation
 *
 * @author Jeff Trent
 */
@Retention(RUNTIME)
@Contract
@RunLevel(value=7, environment=Object.class)
public @interface ANonDefaultEnvRunLevel {
}
