/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.api.admin;

import com.sun.enterprise.module.bootstrap.StartupContext;

import org.jvnet.hk2.annotations.Contract;

import java.io.File;

/**
 * Allow access to the environment under which GlassFish operates.
 *
 * TODO : dochez : this needs to be reconciled with ServerContext and simplified...
 *
 * @author Jerome Dochez
 */
@Contract
public interface ServerEnvironment {


    public enum Status { starting, started, stopping, stopped };

    /** folder where the compiled JSP pages reside */
    public static final String kCompileJspDirName = "jsp";
    String DEFAULT_INSTANCE_NAME = "default-instance-name";

    public File getDomainRoot();    

    /**
     * return the startup context used to initialize this runtime
     */
    public StartupContext getStartupContext();

    /**
     *
     */
    public File getConfigDirPath();

    /**
     * Gets the directory for hosting user-provided jar files.
     * Normally {@code ROOT/lib}
     */
    public File getLibPath();

    /**
     * Gets the directory to store deployed applications
     * Normally {@code ROOT/applications}
     */
    public File getApplicationRepositoryPath();

    /**
     * Gets the directory to store generated stuff.
     * Normally {@code ROOT/generated}
     */
    public File getApplicationStubPath();

    /**
     * Returns the path for compiled JSP Pages from an J2EE application
     * that is deployed on this instance. By default all such compiled JSPs
     * should lie in the same folder.
     */
    public File getApplicationCompileJspPath();

    /**
     * Returns the path for compiled JSP Pages from an Web application
     * that is deployed standalone on this instance. By default all such compiled JSPs
     * should lie in the same folder.
     */
    public File getWebModuleCompileJspPath();

    public String getModuleStubPath();

    public File getApplicationGeneratedXMLPath();

    public File getApplicationEJBStubPath();

    /** A JCEKS keystore which is locked with a fixed-key. This is the "security-by-obfuscation"
     *  carried over from V2.
     *
     * @return File representing the JCEKS store containing the real master password
     */
    File getMasterPasswordFile();

    /** A Java KeyStore which is locked by administrator's master password.
     * @return File representing the JKS which is server's keystore in developer-product case
     */
    File getJKS();

    /**
     * Gets the server status
     */
    Status getStatus();
}
