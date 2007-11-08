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

package com.sun.enterprise.appclient.jws;

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.instance.BaseManager;
import com.sun.enterprise.util.io.FileUtils;
import java.io.File;

/**
 *Represents a nested (embedded) app client as an origin of content.
 *
 * @author tjquinn
 */
public class NestedAppclientContentOrigin extends AppclientContentOrigin {
  
    /** the origin corresponding to the embedded app client's enclosing parent application */
    private ApplicationContentOrigin parent;
    
    /** unique identifier for this embedded app client within its parent */
    private String name;
    
    /**
     *Creates a new instance of NestedAppclientContentOrigin
     *@param the parent ApplicationContentOrigin object
     *@param the ModuleDescriptor for this embedded app client
     *@param the context root by which this app client's content is to be addressable
     */
    public NestedAppclientContentOrigin(ApplicationContentOrigin parent, ModuleDescriptor moduleDescr, String contextRoot) {
        super(parent.getApplication(), moduleDescr, contextRoot);
        this.parent = parent;
        this.name = NamingConventions.NestedAppclient.archiveURIToName(moduleDescr.getArchiveUri());
    }
    
    /**
     *Returns whether this nested app client's parent application is currently
     *enabled for Java Web Start access.
     */
    public boolean isEnabled() {
        return parent.isEnabled();
    }
    
    /**
     *Returns the registration name for the top-level module associated with this
     *nested app client.
     *<p>
     *This method is primarily used to get a name to use in checking whether 
     *the relevant module has been enabled or disabled for Java Web Start access.
     *This implementation returns the name from the parent, whereas top-level
     *app clients return their own reg. name.
     *@return the parent's registration name
     */
    public String getTopLevelRegistrationName() {
        return parent.getApplication().getRegistrationName();
    }
    
    /**
     *Returns the parent origin of this nested app client origin.
     *@return the parent ApplicationContentOrigin 
     */
    public ApplicationContentOrigin getParent() {
        return parent;
    }
    
    /**
     *This method should not be called.  To prevent the superclass's method
     *from gaining control in case of an inadvertent invocation, throw an
     *exception.
     *@return the path to the client jar file
     */
    public String getAppclientJarPath() {
        throw new RuntimeException("Unexpected invocation");
    }
    
    public String toString() {
        return super.toString() + lineSep + ", parent=" + getTopLevelRegistrationName();
    }
    
    /**
     *Returns the prefix for the content map key that is common to all content from this origin.
     *@return the common content key prefix
     */
    public String getContentKeyPrefix() {
        return NamingConventions.NestedAppclient.contentKeyPrefix(this);
    }
    
    public String getVirtualPath() {
        return NamingConventions.NestedAppclient.virtualContextRoot(parent.application, moduleDescriptor);
    }
    
    /**
     *Returns the unique name for this embedded app client within its containing application.
     *@return the app client's name
     */
    public String getName() {
        return name;
    }

    /**
     *Returns a File object for the actual File in the app's directory corresponding
     *to the specified URI.
     */
    public File locateFile(BaseManager manager, String URI) throws ConfigException {
        File dir = new File(manager.getLocation(getApplication().getRegistrationName()), 
                FileUtils.makeFriendlyFileName(moduleDescriptor.getArchiveUri()));
        return new File (dir, URI);
    }
    
    protected String getTargetPath() {
        return NamingConventions.NestedAppclient.actualContextRoot(this);
    }
}
