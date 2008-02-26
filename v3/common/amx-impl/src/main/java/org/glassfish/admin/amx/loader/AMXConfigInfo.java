
package org.glassfish.admin.amx.loader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jvnet.hk2.annotations.InhabitantMetadata;

import org.jvnet.hk2.annotations.CagedBy;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.config.AMXConfig;


/**
    Annotation for {@link AMXConfig} MBeans.
    <p>
    The complete ObjectName with which an MBean is registered will be generated
    based in part upon this information; additional ObjectName properties can
    (and usual are) inserted in addition to the required ones, including 
    ones that define the containment hierarchy (following the same convention
    as does JSR 77). See {@link #j2eeType} for more information.
    <p>
    To generically participate as an AMX configuration MBean, a config interface need
    only annotate itself like this:
    &lt;pre>@AMXConfigInfo(j2eeType=X-MyType)&lt;/pre>
    (where 'MyType' is the desired type).  Of course a 'nameHint' might be needed, and if
    the config is a singleton within its scope, then those values should be included.

 * @author llc
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@AMXMBeanMetadata(amxGroup=AMX.GROUP_CONFIGURATION)
//@CagedBy(AMXConfigRegistrar.class)
public @interface AMXConfigInfo {
    /**
        The default behavior is to derive all attributes automatically from the ConfigBean.
        Specify a sub-interface of {@link AMXConfig} if desired.  By specifying AMXConfig.class
        as the amxInterface(), the MBeanInfo will automagically include all attributes
        from the config interface.
      */
    Class<? extends AMXConfig>  amxInterface() default AMXConfig.class;
     
    /**
       <em>If a generic AMX interface is used in {@link #amxInterface},
       then the 'j2eeType' *must* be specified with j2eeType().<em>
       Defines the value of the "j2eeType" property used in an ObjectName.
       <p>
       For backward compatibility, MBeans representing V2 domain.xml elements
       should use the Glassfish V2 J2EE_TYPE defined by the V2 AMX interface.
       <p>
       The convention for the j2eeType property in the ObjectName is for camel-case
       types beginning with "X-" and ending with an appropriate suffix corresponding to
       the type of MBean.  Example:<br>
       <pre>
       amx:j2eeType=X-DomainConfig,...
       amx:j2eeType=X-ServerMonitor,...
       amx:j2eeType=X-QueryMgr,...
       </pre>
       <p>
       A further convention (consistent with the JSR 77 ObjectName approach), is that
       an MBean logically contained by another MBean must include properties in its
       ObjectName that specify its place in a tree of MBeans ("containment").
       For example:<br>
        <pre>
        amx:j2eeType=J2EEServer,name=server
        amx:j2eeType=JVM,name=jvm1234,J2EEServer=server
        amx:j2eeType=X-JVMInfo,name=na,JVM=jvm1234,J2EEServer=server
        </pre>
        In the example above, the property "J2EEServer=server" in effect defines a containment
        hierarchy ("tree") in which the j2eeType=JVM MBean is a "child" (Containee) of the
        parent ("Container") amx:j2eeType=J2EEServer,name=server .  The j2eeType=X-JVMINfo
        ObjectName continues that arrangement, specifing its parent and parent's parent.
        This convention allows traversal of registered MBeans, the ability to build a generic management
        interface which relates MBeans to each other structurally, etc.
        <p>
        The preceeding convention is enforced by the framework for config beans based on 
        information available from @Configured interfaces, relying upon the fact that registration
        of a parent ("Container") MBean must occur before registration of a child ("Containee").
     */
    String j2eeType() default "";
    
    /**
        Optional hint for generating the ObjectName; could be used to indicate
        an appropriate name and/or a key or lookup value for finding the correct name.
        The interpretation of this hint is up to the {@link ObjectNameBuilder} or any code
        that is constructing the ObjectName.
     */
    String nameHint() default "name";

    /**
       Declares that there may be at most one MBean registered with this {@link #j2eeType}
       <em>within its parent’s scope</em>.
       By convention, singleton MBeans should all use the same name ("na"),
       because its j2eeType alone effectively denotes the name.
     */
    boolean singleton() default false;
    
    /**
       Declares that the j2eeType of this MBean should not be included as an ancestor
       property in its children.  DomainConfig is the key example; including it would serve
       no useful purpose.
     */
    boolean omitAsAncestorInChildObjectName() default false;
}

