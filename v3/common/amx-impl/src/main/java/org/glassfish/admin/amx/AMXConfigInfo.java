package org.glassfish.admin.amx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jvnet.hk2.annotations.InhabitantMetadata;

import org.jvnet.hk2.annotations.CagedBy;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.config.AMXConfig;


/**
  Marker annotation for {@link AMXConfig} MBeans.  Might contain other fields
  in the future.
 * @author llc
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@AMXMBeanMetadata(amxGroup=AMX.GROUP_CONFIGURATION)
@CagedBy(AMXConfigRegistrar.class)
public @interface AMXConfigInfo {
    Class<? extends AMXConfig>  amxInterface();
}

