/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.api.amx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

/***
    Annotation for creating AMXConfig MBeans, to be used on ConfigBeanProxy
    interfaces which ultimately result in AMXConfig MBeans.
    <p>
    The complete ObjectName with which an MBean is registered will be generated
    based in part upon this information; additional ObjectName properties can
    (and usual are) inserted in addition to the required ones, including 
    ones that define the containment hierarchy (following the same convention
    as does JSR 77).
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
@AMXMBeanMetadata
@Documented
//@CagedBy(AMXConfigRegistrar.class)
public @interface AMXConfigInfo {
    /**
        The default behavior is/will be to derive all attributes automatically from the ConfigBean.
        Specify a sub-interface of AMXConfig if desired.  By specifying AMXConfig.class
        as the amxInterface(), the MBeanInfo will automagically include all attributes
        from the config interface.
        <p>
        Should really be:
        <pre>Class&lt;? extends AMXConfig>  amxInterface() default AMXConfig.class;</pre>
        But this creates compile and runtime dependencies, so a String is used.  Leave the value
        empty to use the generic default (com.sun.appserv.management.config.AMXConfig).
      */
    public String amxInterfaceName() default "";
    
     
    /*
KEEP until it's clear we won't need this
not clear if this is needed; the interface should specify it
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
     
    String j2eeType() default "";
    */
    
    /*
KEEP until it's clear we will never need this
        Optional hint for generating the ObjectName; could be used to indicate
        an appropriate name and/or a key or lookup value for finding the correct name.
        The interpretation of this hint is up to the code
        that is constructing the ObjectName.
        <p>
        NOTE: use of this field should be very rare since the @Attribute annotation should
        provide the appropriate indicator.
    String nameHint() default "name";
     */

    /**
       Declares that there may be at most one MBean registered with this j2eeType
       <em>within its parent's scope</em>.
       By convention, singleton MBeans should all use the same name ("na"),
       because its j2eeType alone effectively denotes the name.  The AMX subsystem
       specifies this as AMX.NO_NAME.
     */
    boolean singleton() default false;
    
    /**
       Declares that the j2eeType of this MBean should not be included as an ancestor
       property in its children.  DomainConfig is the key example; including it would serve
       no useful purpose; the ObjectName would just become larger and more awkward.
     */
    boolean omitAsAncestorInChildObjectName() default false;
}










