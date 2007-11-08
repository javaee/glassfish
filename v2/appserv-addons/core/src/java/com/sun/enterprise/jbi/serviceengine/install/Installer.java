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
package com.sun.enterprise.jbi.serviceengine.install;

import com.sun.enterprise.jbi.serviceengine.*;
import com.sun.enterprise.jbi.serviceengine.ServiceEngineException;



/**
 *
 * @author Manisha Umbarje
 */
public interface Installer {
    
    public boolean isJBIInstalled();

    public void setComponentName(String componentName);
    
    /**
     * Installs the service engine and starts it
     * @param zipFilePath packaged 208 compliant service engine bundle
     * 
     */
    public String install(String zipFilePath) throws ServiceEngineException;
    
    /**
     * Starts the component with provided name
     */
    public void start() throws ServiceEngineException;
 

    /**
     * Checks if the compoenent specified by componentName is installed or
     * not
     */
    public boolean isComponentInstalled();

    /**
     * Stops the component with provided name
     */
    public void stop() throws ServiceEngineException;

    /**
     * Uninstalls the component with provided name
     */
    public void uninstall() throws ServiceEngineException;
    
    
}
