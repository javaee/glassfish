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

package com.sun.enterprise.v3.admin.adapter;

/** A package-private class to store the progress of the installation operation.
 *  The class needs external synchronization.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish V3
 */
final class ProgressObject {

    private String message            = "Started Installing ...";
    private boolean done              = false;
    private AdapterState state        = AdapterState.INSTALLING;
    
    private boolean APPEND = true; //for debugging
    
    String getMessage() {
        return ( message );
    }
    
    void setMessage (String message) {
        if (APPEND)
            this.message = this.message + message;
        else
            this.message = message;
    }
    
    boolean isDone() {
        return ( done == true );
    }
    
    void finish() {
        this.message = "Done!";
        this.done    = true;
    }
    
    void setAdapterState(AdapterState state) {
        this.state = state;
    }
    
    AdapterState getAdapterState() {
        return ( state );
    }
}