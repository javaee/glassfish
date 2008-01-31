/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.api.deployment.archive;

import org.glassfish.api.deployment.DeploymentContext;
import org.jvnet.hk2.annotations.Contract;

import java.io.IOException;

/**
 * ArchiveHandlers are handling certain archive type. An archive has a unique type which is usually defines how
 * classes and resources are loaded from the archive. 
 *
 * ArchiveHandler should be stateless objects although the implementations of this contract can
 * control that using the scope element of the @Service annotation.
 * 
 * @author Jerome Dochez
 */
@Contract
public interface ArchiveHandler {

    public String getArchiveType();

    /**
     * Returns the default name by which the specified archive can be 
     * identified.
     * <p>
     * The default name is used, for example, during deployment if no name
     * was specified explicitly as part of the deployment request.  
     * @param archive the archive for which to provide the default name
     * @return the default name for identifying the specified archive
     */
    public String getDefaultApplicationName(ReadableArchive archive);
    
    public boolean handles(ReadableArchive archive);

    public ClassLoader getClassLoader(ClassLoader parent, ReadableArchive archive);
    
    /**
     * Prepares the jar file to a format the ApplicationContainer is
     * expecting. This could be just a pure unzipping of the jar or
     * nothing at all.
     * @param source of the expanding
     * @param target of the expanding
     */
    public void expand(ReadableArchive source, WritableArchive target) throws IOException;
}
