/*
 * $Id: NullValueHolder.java,v 1.1 2005/09/20 21:11:28 edburns Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at
 * https://javaserverfaces.dev.java.net/CDDL.html or
 * legal/CDDLv1.0.txt. 
 * See the License for the specific language governing
 * permission and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.    
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * [Name of File] [ver.__] [Date]
 * 
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.faces.config.beans;


/**
 * <p>Interface denoting a configuration bean that stores a
 * <code>nullValue</code> property.
 */

public interface NullValueHolder {


    // -------------------------------------------------------------- Properties


    /**
     * <p>Return a flag indicating that the value of the parent
     * compoennt should be set to <code>null</code>.</p>
     */
    public boolean isNullValue();


    /**
     * <p>Set a flag indicating that the value of the parent
     * component should be set to <code>null</code>.</p>
     *
     * @param nullValue The new null value flag
     */
    public void setNullValue(boolean nullValue);


}
