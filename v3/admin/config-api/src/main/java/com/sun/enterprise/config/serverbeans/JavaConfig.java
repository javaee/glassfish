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
import org.glassfish.api.admin.ConfigBean;

import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "profiler",
    "jvmOptionsOrProperty"
}) */
@Configured
public class JavaConfig
 extends ConfigBean implements Serializable {

    private final static long serialVersionUID = 1L;
    @Attribute

    protected String javaHome;
    @Attribute

    protected String debugEnabled;
    @Attribute

    protected String debugOptions;
    @Attribute

    protected String rmicOptions;
    @Attribute

    protected String javacOptions;
    @Attribute

    protected String classpathPrefix;
    @Attribute

    protected String classpathSuffix;
    @Attribute

    protected String serverClasspath;
    @Attribute

    protected String systemClasspath;
    @Attribute

    protected String nativeLibraryPathPrefix;
    @Attribute

    protected String nativeLibraryPathSuffix;
    @Attribute

    protected String bytecodePreprocessors;
    @Attribute

    protected String envClasspathIgnored;
    protected Profiler profiler;

    @Element
    List<String> jvmOptions = new ConstrainedList<String>(this, "jvmOptions", support);

    @Element("property")
    protected List<Property> properties = new ConstrainedList<Property>(this, "properties", support);
    



    /**
     * Gets the value of the javaHome property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getJavaHome() {
        if (javaHome == null) {
            return "${com.sun.aas.javaRoot}";
        } else {
            return javaHome;
        }
    }

    /**
     * Sets the value of the javaHome property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJavaHome(String value) throws PropertyVetoException {
        support.fireVetoableChange("javaHome", this.javaHome, value);

        this.javaHome = value;
    }

    /**
     * Gets the value of the debugEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getDebugEnabled() {
        if (debugEnabled == null) {
            return "false";
        } else {
            return debugEnabled;
        }
    }

    /**
     * Sets the value of the debugEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDebugEnabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("debugEnabled", this.debugEnabled, value);

        this.debugEnabled = value;
    }

    /**
     * Gets the value of the debugOptions property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getDebugOptions() {
        if (debugOptions == null) {
            return "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n";
        } else {
            return debugOptions;
        }
    }

    /**
     * Sets the value of the debugOptions property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDebugOptions(String value) throws PropertyVetoException {
        support.fireVetoableChange("debugOptions", this.debugOptions, value);

        this.debugOptions = value;
    }

    /**
     * Gets the value of the rmicOptions property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getRmicOptions() {
        if (rmicOptions == null) {
            return "-iiop -poa -alwaysgenerate -keepgenerated -g";
        } else {
            return rmicOptions;
        }
    }

    /**
     * Sets the value of the rmicOptions property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRmicOptions(String value) throws PropertyVetoException {
        support.fireVetoableChange("rmicOptions", this.rmicOptions, value);

        this.rmicOptions = value;
    }

    /**
     * Gets the value of the javacOptions property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getJavacOptions() {
        if (javacOptions == null) {
            return "-g";
        } else {
            return javacOptions;
        }
    }

    /**
     * Sets the value of the javacOptions property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJavacOptions(String value) throws PropertyVetoException {
        support.fireVetoableChange("javacOptions", this.javacOptions, value);

        this.javacOptions = value;
    }

    /**
     * Gets the value of the classpathPrefix property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getClasspathPrefix() {
        return classpathPrefix;
    }

    /**
     * Sets the value of the classpathPrefix property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setClasspathPrefix(String value) throws PropertyVetoException {
        support.fireVetoableChange("classpathPrefix", this.classpathPrefix, value);

        this.classpathPrefix = value;
    }

    /**
     * Gets the value of the classpathSuffix property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getClasspathSuffix() {
        return classpathSuffix;
    }

    /**
     * Sets the value of the classpathSuffix property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setClasspathSuffix(String value) throws PropertyVetoException {
        support.fireVetoableChange("classpathSuffix", this.classpathSuffix, value);

        this.classpathSuffix = value;
    }

    /**
     * Gets the value of the serverClasspath property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getServerClasspath() {
        return serverClasspath;
    }

    /**
     * Sets the value of the serverClasspath property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setServerClasspath(String value) throws PropertyVetoException {
        support.fireVetoableChange("serverClasspath", this.serverClasspath, value);

        this.serverClasspath = value;
    }

    /**
     * Gets the value of the systemClasspath property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getSystemClasspath() {
        return systemClasspath;
    }

    /**
     * Sets the value of the systemClasspath property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSystemClasspath(String value) throws PropertyVetoException {
        support.fireVetoableChange("systemClasspath", this.systemClasspath, value);

        this.systemClasspath = value;
    }

    /**
     * Gets the value of the nativeLibraryPathPrefix property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getNativeLibraryPathPrefix() {
        return nativeLibraryPathPrefix;
    }

    /**
     * Sets the value of the nativeLibraryPathPrefix property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setNativeLibraryPathPrefix(String value) throws PropertyVetoException {
        support.fireVetoableChange("nativeLibraryPathPrefix", this.nativeLibraryPathPrefix, value);

        this.nativeLibraryPathPrefix = value;
    }

    /**
     * Gets the value of the nativeLibraryPathSuffix property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getNativeLibraryPathSuffix() {
        return nativeLibraryPathSuffix;
    }

    /**
     * Sets the value of the nativeLibraryPathSuffix property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setNativeLibraryPathSuffix(String value) throws PropertyVetoException {
        support.fireVetoableChange("nativeLibraryPathSuffix", this.nativeLibraryPathSuffix, value);

        this.nativeLibraryPathSuffix = value;
    }

    /**
     * Gets the value of the bytecodePreprocessors property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getBytecodePreprocessors() {
        return bytecodePreprocessors;
    }

    /**
     * Sets the value of the bytecodePreprocessors property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setBytecodePreprocessors(String value) throws PropertyVetoException {
        support.fireVetoableChange("bytecodePreprocessors", this.bytecodePreprocessors, value);

        this.bytecodePreprocessors = value;
    }

    /**
     * Gets the value of the envClasspathIgnored property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getEnvClasspathIgnored() {
        if (envClasspathIgnored == null) {
            return "true";
        } else {
            return envClasspathIgnored;
        }
    }

    /**
     * Sets the value of the envClasspathIgnored property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEnvClasspathIgnored(String value) throws PropertyVetoException {
        support.fireVetoableChange("envClasspathIgnored", this.envClasspathIgnored, value);

        this.envClasspathIgnored = value;
    }

    /**
     * Gets the value of the profiler property.
     *
     * @return possible object is
     *         {@link Profiler }
     */
    public Profiler getProfiler() {
        return profiler;
    }

    /**
     * Sets the value of the profiler property.
     *
     * @param value allowed object is
     *              {@link Profiler }
     */
    public void setProfiler(Profiler value) throws PropertyVetoException {
        support.fireVetoableChange("profiler", this.profiler, value);

        this.profiler = value;
    }


    public void addProperty(Property property) {
        properties.add(property);
    }

    public List<Property>  getProperty() {
        return properties;
    }

    public void addJvmOption(String option) {
        jvmOptions.add(option);
    }

    public List<String> getJvmOptions() {
        return jvmOptions;
    }

    /**
     * Backward compatibility API
     */
    public List<Object> getJvmOptionsOrProperty() {
        ArrayList bag = new ArrayList<Object>();
        bag.addAll(jvmOptions);
        bag.addAll(properties);
        return bag;

    }



}
