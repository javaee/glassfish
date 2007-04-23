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

package com.sun.enterprise.module;

import java.net.URI;

/**
 * Factory to create repositories. 
 *
 * @author Jerome Dochez
 */
public abstract class RepositoryFactory {
        
    /**
     * Returns true if this factory can handle this type of repository
     * @param type repository type
     */
    public abstract boolean handleType(String type);
    
    /**
     * Creates a new <code>Repository</code> with a parent (for delegating 
     * module resolutions) and a name. The URI source identifies the repository 
     * location.
     * @param parent the parent <code>Repository</code> to delegate module 
     * resolution
     * @param name the repository name
     * @param source the location of the repository
     */
    public abstract Repository createRepository(Repository parent, String name, URI source);
    
    /**
     * Creates a new <code>Repository</code>. The URI source identifies the 
     * repository location.
     * @param name the repository name
     * @param source the location of the repository
     */    
    public abstract Repository createRepository(String name, URI source);
}
