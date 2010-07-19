package org.jvnet.hk2.test.runlevel;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.RunLevel;

/**
 * RunLevelTen alias.
 * 
 * @author Jeff Trent
 */
@Retention(RUNTIME)
@Target({TYPE})
@RunLevel(20)
@Contract
public @interface RunLevelTwenty {
}
