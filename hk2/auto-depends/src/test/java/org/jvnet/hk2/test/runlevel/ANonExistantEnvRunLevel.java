package org.jvnet.hk2.test.runlevel;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.RunLevel;

/**
 * Used for testing services marked with a RunLevel environment
 * that is not backed by any RunLevelService.
 * 
 * @author Jeff Trent
 */
@Retention(RUNTIME)
@Contract
@RunLevel(value=7, environment=Integer.class)
public @interface ANonExistantEnvRunLevel {

}
