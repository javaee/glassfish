/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.resourcebase.resources;

import java.util.logging.Logger;
import java.util.logging.Level;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LoggerInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;


/**
 * Created with IntelliJ IDEA.
 * User: naman
 * Date: 18/3/13
 * Time: 12:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResourceLoggingConstansts {
    @LogMessageInfo(
            message = "Unexpected exception in loading class [{0}] by classloader.",
            comment = "This is a comment about this log message.",
            cause = "Classpath is not properly set in the domain.xml or application server process does not have " +
                    "read permissions on the directory that holds the classes/jar.",
            action = "Check that the classpath attribute in the java-config includes a reference to the jar/package " +
                    "directory for the class or you do not have read permissions on the directory that holds the classes/jar.",
            level = "SEVERE")
    public static final String LOAD_CLASS_FAIL = "NCLS-RESOURCE-00001";

    @LogMessageInfo(
            message = "Unexpected exception in loading class by classloader [{0}].",
            comment = "This is a comment about this log message.",
            cause = "Classpath is not properly set in the domain.xml or you do not have read permissions on the directory " +
                    "that holds the classes/jar.",
            action = "Check that the classpath attribute in the java-config includes a reference to the jar/package " +
                    "directory for the class or check that the directory where the classes/jars reside have read " +
                    "permission for the application server process",
            level = "SEVERE")
    public static final String LOAD_CLASS_FAIL_EXCEP = "NCLS-RESOURCE-00002";

    @LogMessageInfo(
            message = "Cannot bind resource [{0}] to naming manager. Following exception occurred [{1}].",
            comment = "This is a comment about this log message.",
            cause = "Please check the exception to get more details.",
            action = "Please check the exception to resolve the same.",
            level = "SEVERE")
    public static final String BIND_RESOURCE_FAILED = "NCLS-RESOURCE-00003";

    @LogMessageInfo(
            message = "Unable to deploy resource [{0}] due to following exception: [{1}].",
            comment = "This is a comment about this log message.",
            level = "WARNING")
    public static final String UNABLE_TO_DEPLOY = "NCLS-RESOURCE-00004";

    @LogMessageInfo(
            message = "Unable to undeploy resource, no Resource Deployer for [{0}].",
            comment = "This is a comment about this log message.",
            level = "WARNING")
    public static final String UNABLE_TO_UNDEPLOY = "NCLS-RESOURCE-00005";

    @LogMessageInfo(
            message = "Unable to undeploy resource [{0}] due to following exception: [{1}].",
            comment = "This is a comment about this log message.",
            level = "WARNING")
    public static final String UNABLE_TO_UNDEPLOY_EXCEPTION = "NCLS-RESOURCE-00006";

    @LogMessageInfo(
            message = "Error while handling Change event due to following exception: [{0}].",
            comment = "This is a comment about this log message.",
            level = "WARNING")
    public static final String ERROR_HANDLE_CHANGE_EVENT = "NCLS-RESOURCE-00007";

    @LogMessageInfo(
            message = "Error while handling Remove event due to following exception: [{0}].",
            comment = "This is a comment about this log message.",
            level = "WARNING")
    public static final String ERROR_HANDLE_REMOVE_EVENT = "NCLS-RESOURCE-00008";

    @LogMessageInfo(
            message = "Unable to find ResourceDeployer for [{0}].",
            comment = "This is a comment about this log message.",
            level = "WARNING")
    public static final String UNABLE_TO_FIND_RESOURCEDEPLOYER = "NCLS-RESOURCE-00009";

}
