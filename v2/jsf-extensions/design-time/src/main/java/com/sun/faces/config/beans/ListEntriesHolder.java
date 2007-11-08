/*
 * $Id: ListEntriesHolder.java,v 1.1 2005/09/20 21:11:26 edburns Exp $
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
 * <code>listEntries</code> property.
 */

public interface ListEntriesHolder {


    // -------------------------------------------------------------- Properties


    /**
     * <p>Return the current <code>listEntries</code> object
     * for this configuration bean.</p>
     */
    public ListEntriesBean getListEntries();


    /**
     * <p>Set the current <code>listEntries</code> object
     * for this configuration bean.</p>
     *
     * @param listEntries The new <code>listEntries</code> object
     */
    public void setListEntries(ListEntriesBean listEntries);


}
