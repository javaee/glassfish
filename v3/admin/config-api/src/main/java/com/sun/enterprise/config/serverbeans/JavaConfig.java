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



package com.sun.enterprise.config.serverbeans;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.component.Injectable;

import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.glassfish.api.admin.config.PropertyDesc;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.quality.ToDo;

/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "profiler",
    "jvmOptionsOrProperty"
}) */
@org.glassfish.api.amx.AMXConfigInfo( amxInterfaceName="com.sun.appserv.management.config.JavaConfig", singleton=true)
@Configured
public interface JavaConfig extends ConfigBeanProxy, Injectable, PropertyBag {

    /**
     * Gets the value of the javaHome property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="${com.sun.aas.javaRoot}")
    public String getJavaHome();

    /**
     * Sets the value of the javaHome property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJavaHome(String value) throws PropertyVetoException;

    /**
     * Gets the value of the debugEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false")
    public String getDebugEnabled();

    /**
     * Sets the value of the debugEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDebugEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the debugOptions property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n")
    public String getDebugOptions();

    /**
     * Sets the value of the debugOptions property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDebugOptions(String value) throws PropertyVetoException;

    /**
     * Gets the value of the rmicOptions property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="-iiop -poa -alwaysgenerate -keepgenerated -g")
    public String getRmicOptions();

    /**
     * Sets the value of the rmicOptions property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRmicOptions(String value) throws PropertyVetoException;

    /**
     * Gets the value of the javacOptions property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="-g")
    public String getJavacOptions();

    /**
     * Sets the value of the javacOptions property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJavacOptions(String value) throws PropertyVetoException;

    /**
     * Gets the value of the classpathPrefix property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getClasspathPrefix();

    /**
     * Sets the value of the classpathPrefix property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setClasspathPrefix(String value) throws PropertyVetoException;

    /**
     * Gets the value of the classpathSuffix property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getClasspathSuffix();

    /**
     * Sets the value of the classpathSuffix property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setClasspathSuffix(String value) throws PropertyVetoException;

    /**
     * Gets the value of the serverClasspath property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getServerClasspath();

    /**
     * Sets the value of the serverClasspath property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setServerClasspath(String value) throws PropertyVetoException;

    /**
     * Gets the value of the systemClasspath property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getSystemClasspath();

    /**
     * Sets the value of the systemClasspath property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSystemClasspath(String value) throws PropertyVetoException;

    /**
     * Gets the value of the nativeLibraryPathPrefix property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getNativeLibraryPathPrefix();

    /**
     * Sets the value of the nativeLibraryPathPrefix property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setNativeLibraryPathPrefix(String value) throws PropertyVetoException;

    /**
     * Gets the value of the nativeLibraryPathSuffix property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getNativeLibraryPathSuffix();

    /**
     * Sets the value of the nativeLibraryPathSuffix property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setNativeLibraryPathSuffix(String value) throws PropertyVetoException;

    /**
     * Gets the value of the bytecodePreprocessors property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getBytecodePreprocessors();

    /**
     * Sets the value of the bytecodePreprocessors property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setBytecodePreprocessors(String value) throws PropertyVetoException;

    /**
     * Gets the value of the envClasspathIgnored property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="true")
    public String getEnvClasspathIgnored();

    /**
     * Sets the value of the envClasspathIgnored property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEnvClasspathIgnored(String value) throws PropertyVetoException;

    /**
     * Gets the value of the profiler property.
     *
     * @return possible object is
     *         {@link Profiler }
     */
    @Element
    public Profiler getProfiler();

    /**
     * Sets the value of the profiler property.
     *
     * @param value allowed object is
     *              {@link Profiler }
     */
    public void setProfiler(Profiler value) throws PropertyVetoException;

    @Element
    public List<String> getJvmOptions();
    
    public void setJvmOptions(List<String> options) throws PropertyVetoException;
    
    /**
    	Properties as per {@link PropertyBag}
     */
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Override
    @Element
    List<Property> getProperty();
}
