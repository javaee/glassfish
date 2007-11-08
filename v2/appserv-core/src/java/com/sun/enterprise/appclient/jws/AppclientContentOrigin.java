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
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.instance.BaseManager;
import java.io.File;

/**
 *Records mappings of paths to content that stem from a single app client.
 *
 * @author tjquinn
 */
    
public class AppclientContentOrigin extends UserContentOrigin {

    /** records the content origin's context root */
    private String contextRoot;

    /** module descriptor for the content origin */
    protected ModuleDescriptor moduleDescriptor;
    
    /** vendor info derived from the <vendor> element in the sun-application-client.xml
     * <java-web-start-access> element.
     */
    private VendorInfo vendorInfo = null;
    
    /**
     *Creates a new instance of the appclient content origin.
     *@param Application object for the appclient (either an artificial wrapper
     *around a stand-alone app client or the parent ear's Application if this is 
     *an embedded app client)
     *@param ModuleDescriptor for the specific app client
     *@param the context root to be used for this app client
     */
    public AppclientContentOrigin(Application application, ModuleDescriptor moduleDescriptor, String contextRoot) {
        super(application);
        this.moduleDescriptor = moduleDescriptor;

        /*
         *Make sure the context root is not empty and has a 
         *slash at the beginning.  The default value is legal but
         *a user-supplied one might not be.
         */
        if (contextRoot.length() < 2 || ( ! contextRoot.substring(0,1).equals("/") )) {
            String regName = application.getRegistrationName();
            throw new IllegalArgumentException("Java Web Start-related context root of '" + contextRoot + "' specified for app client " + regName + " must begin with a slash and contain at least one other character");
        }
        this.contextRoot = contextRoot;
    }
    
    /**
     *Returns the origin's context root.
     *@return the string value for the context root
     */
    public String getContextRoot() {
        return contextRoot;
    }

    /**
     *Returns the display name for the app client.
     *@return the display name as a String
     */
    public String getDisplayName() {
        return moduleDescriptor.getDescriptor().getDisplayName();        
    }
    
    /**
     *Returns the descriptor for the app client.
     *@return the descriptor
     */
    public String getDescription() {
        return getApplication().getDescription();
    }
    
    /**
     *Returns the path, within the virtual namespace provided by the JWS system
     *servlet, where the app client jar file for this app client resides.
     *@return the path to the client jar file
     */
    public String getAppclientJarPath() {
        return NamingConventions.TopLevelAppclient.appclientJarPath(this);
    }
    
    
    protected String getContentKeyPrefix() {
        return NamingConventions.TopLevelAppclient.contentKeyPrefix(this);
    }
    
    /**
     *Returns the path to which requests for the virtual path for this origin
     *should be dispatched so they can be served by the system servlet.
     *@return the path to which the virtual path should be mapped
     */
    protected String getTargetPath() {
        return NamingConventions.TopLevelAppclient.actualContextRoot(application);
    }
    
    /**
     *Returns the virtual path users can use to refer to the app client from
     *this origin.  If the developer did not specify one, the default path
     *is returned.
     *@return the path by which users can access the app client
     */
    public String getVirtualPath() {
        return NamingConventions.TopLevelAppclient.virtualContextRoot(application, moduleDescriptor);
    }
    
    public String toString() {
        return super.toString() + ", context root=" + getVirtualPath() + ", module name=" + moduleDescriptor.getName();
    }
    
    public String getVendor() {
        return getVendorInfo().getVendor();
    }
    
    public String getImageURI() {
        return getVendorInfo().getImageURI();
    }
    
    public String getSplashImageURI() {
        return getVendorInfo().getSplashImageURI();
    }
    
    /**
     *Returns a File object for the actual File in the app's directory corresponding
     *to the specified URI.
     *<p>
     *This implementation covers the top-level app client case and is overridden
     *by an implementation for a nested app client.
     */
    public File locateFile(BaseManager manager, String URI) throws ConfigException {
        File file = new File(manager.getLocation(getApplication().getRegistrationName()), URI);
        return file;
    }
    
    private VendorInfo getVendorInfo() {
        if (vendorInfo == null) {
            vendorInfo = new VendorInfo(((ApplicationClientDescriptor) 
                (moduleDescriptor.getDescriptor())).getJavaWebStartAccessDescriptor().getVendor());
        }
        return vendorInfo;
    }
    
    private class VendorInfo {
        private String vendorStringFromDescriptor;
        private String vendor = "";
        private String imageURIString = "";
        private String splashImageURIString = "";
        
        private VendorInfo(String vendorStringFromDescriptor) {
            this.vendorStringFromDescriptor = vendorStringFromDescriptor != null ?
                vendorStringFromDescriptor : "";
            String [] parts = this.vendorStringFromDescriptor.split("::");
            if (parts.length == 1) {
                vendor = parts[0];
            } else if (parts.length == 2) {
                imageURIString = parts[0];
                vendor = parts[0];
            } else if (parts.length == 3) {
                imageURIString = parts[0];
                splashImageURIString = parts[1];
                vendor = parts[2];
            }
        }
        
        private String getVendor() {
            return vendor;
        }
        
        private String getImageURI() {
            return imageURIString;
        }
        
        private String getSplashImageURI() {
            return splashImageURIString;
        }
    }
    
    public String getName() {
        return application.getRegistrationName();
    }
    
}
