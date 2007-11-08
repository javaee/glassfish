/*
 * $Id: ResourceBundleBean.java,v 1.1 2005/09/20 21:11:29 edburns Exp $
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


import java.util.Map;
import java.util.TreeMap;


/**
 * <p>Configuration bean for <code>&lt;attribute&gt; element.</p>
 */

public class ResourceBundleBean extends FeatureBean {
    /**
     * Holds value of property basename.
     */
    private String basename;

    /**
     * Getter for property basename.
     * @return Value of property basename.
     */
    public String getBasename() {

        return this.basename;
    }

    /**
     * Setter for property basename.
     * @param basename New value of property basename.
     */
    public void setBasename(String basename) {

        this.basename = basename;
    }

    /**
     * Holds value of property var.
     */
    private String var;

    /**
     * Getter for property var.
     * @return Value of property var.
     */
    public String getVar() {

        return this.var;
    }

    /**
     * Setter for property var.
     * @param var New value of property var.
     */
    public void setVar(String var) {

        this.var = var;
    }



}
