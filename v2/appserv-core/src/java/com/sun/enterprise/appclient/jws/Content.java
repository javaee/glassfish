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

/**
 *Abstract superclass of all types of content.
 *
 * @author tjquinn
 */
    
public abstract class Content {

    protected final String lineSep = System.getProperty("line.separator");

    /**
     *The path to the content as specified in URLs.  This is the "virtual"
     *path.  
     */
    private String path;

    /**
     *The key by which this content can be looked up in the content map.
     */
    protected String contentKey;
    
    /** The origin that owns this content. */
    private ContentOrigin origin;

    /** Creates a new instance of Content */
    public Content(ContentOrigin origin, String contentKey, String path) {
        this.origin = origin;
        this.contentKey = contentKey;
        this.path = path;
    }

    /**
     *Returns the virtual path within the content's subcategory to the content.
     *
     */
    public String getPath() {
        return path;
    }

    /**
     *Returns the content key for this content.
     *<p>
     *Prefixing a slash makes it easier to compare this to the pathInfo from
     *an incoming HTTP request that is seeking content.
     */
    public String getContentKey() {
        return contentKey;
    }

    /**
     *Returns the content origin which owns this content.
     *@return ContentOrigin for the owning origin
     */
    public ContentOrigin getOrigin() {
        return origin;
    }
    
    /**
     *Returns a string representation of the content.
     * <p>
     * Even though Content is abstract, subclasses use super.toString().
     */
    public String toString() {
        return getClass().getName() + ": content key=" + getContentKey() + ", path=" + getPath();
    }
}
