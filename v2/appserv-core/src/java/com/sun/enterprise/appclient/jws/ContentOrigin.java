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

import com.sun.enterprise.deployment.Application;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Represents the origin of some content to be served to Java Web Start.
 * @author tjquinn
 */
public abstract class ContentOrigin {

    protected static final String lineSep = System.getProperty("line.separator");
    
    /**
     *Map from URL path to the content for each file this app or
     *app client supports.
     */
    protected Map<String,Content> pathToContent;

    /** boolean indicating whether adhoc path is registered with container **/
    private boolean adhocPathRegistered = false;

    /**
     *Creates a new instance of the Entry class to record an app client
     *or J2EE application.
     *@param the application (descriptor) for the app client or J2EE app
     */
    public ContentOrigin() {
        pathToContent = new HashMap<String,Content>();
    }

    /**
     *Returns whether the administrator has enabled this content origin (either
     *a Java EE application or a stand-alone app client) for Java Web Start
     *access.
     *@return boolean indicating whether the module's app clients are enabled for JWS access
     */
    public abstract boolean isEnabled();
    
    protected Content getContent(String contentKey) {
        return pathToContent.get(contentKey);
    }

    protected DynamicContent addDynamicContent(String path, String template, Properties tokenValues, String mimeType) throws IOException {
        return addDynamicContent(path, template, tokenValues, mimeType, false /* requiresElevatedPrivs */);
    }
    
    protected DynamicContent addDynamicContent(
            String path,
            String template,
            Properties tokenValues,
            String mimeType,
            boolean requiresElevatedPrivs) {
        // merge - future
        String docText = Util.replaceTokens(template, tokenValues);
        String contentKey = getContentKeyPrefix() + path;
        DynamicContent result = new DynamicContent(this, contentKey, path, docText, mimeType, requiresElevatedPrivs);
        pathToContent.put(result.getContentKey(), result);
        return result;
    }

    protected DynamicContent addDynamicContent(DynamicContent content) {
        pathToContent.put(content.getContentKey(), content);
        return content;
    }
    
    protected StaticContent addStaticContent(String path, URI installRootURI, File file) throws URISyntaxException {
        String contentKey = getContentKeyPrefix() + path;
        StaticContent result = new StaticContent(this, contentKey, path, file, installRootURI, false /* isMainJar */);
        return addStaticContent(result);
    }
    
    protected StaticContent addStaticContent(StaticContent content) {
        pathToContent.put(content.getContentKey(), content);
        return content;
    }

    /**
     *Returns the prefix for content keys for content from this origin.
     *@return the content key prefix for this origin
     */
    protected abstract String getContentKeyPrefix();
    
    /**
     *Returns a collection of this origin's contents.
     *@param map from content keys to contents
     */
    public Collection<Content> getContents() {
        return pathToContent.values();
    }

    /**
     *Mark the origin as registered with webcontainer.
     */
    void adhocPathRegistered() {
        adhocPathRegistered=true;
    }

    /**
     *Return a boolean indicating whether this
     *is registerd with the webcontainer.
     */
    boolean isAdhocPathRegistered() {
        return adhocPathRegistered;
    }
    
    /**
     *Returns a longer display of information about this origin
     *@return detailed summary of this instance
     */
    public String toLongString() {
        return toString() + pathToContent.toString();
    }
}
