package org.glassfish.admin.amx;

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

import org.jvnet.hk2.annotations.InhabitantMetadata;

import org.jvnet.hk2.annotations.CagedBy;

import com.sun.appserv.management.base.AMX;

/**
  Marker annotation for {@link AMXConfig} MBeans.  Might contain other fields
  in the future.
 * @author llc
 */
@Retention(RUNTIME)
@Target(TYPE)
@CagedBy(AMXConfigRegistrar.class)
@AMXMBeanMetadata(amxGroup=AMX.GROUP_CONFIGURATION)
public @interface AMXConfigInfo {
    String value() default "not used";
    
    // right, wrong, ???
    //@InhabitantMetadata
    //CagedBy cagedBy() default @CagedBy( AMXConfigRegistrar.class );
}

