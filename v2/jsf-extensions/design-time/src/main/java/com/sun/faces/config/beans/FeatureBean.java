/*
 * $Id: FeatureBean.java,v 1.1 2005/09/20 21:11:25 edburns Exp $
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
 * <p>Base class for configuration beans for "features" such as
 * attributes and components.</p>
 */

public abstract class FeatureBean {


    // -------------------------------------------------------------- Properties



    // ----------------------------------------------- DescriptionHolder Methods


    private Map<String,DescriptionBean> descriptions = new TreeMap<String, DescriptionBean>();


    public void addDescription(DescriptionBean descriptor) {
        descriptions.put(descriptor.getLang(), descriptor);
    }


    public DescriptionBean getDescription(String lang) {
        return (descriptions.get(lang));
    }


    public DescriptionBean[] getDescriptions() {
        DescriptionBean results[] = new DescriptionBean[descriptions.size()];
        return (descriptions.values().toArray(results));
    }


    public void removeDescription(DescriptionBean descriptor) {
        descriptions.remove(descriptor.getLang());
    }


    // ----------------------------------------------- DisplayNameHolder Methods


    private Map<String,DisplayNameBean> displayNames = new TreeMap<String, DisplayNameBean>();


    public void addDisplayName(DisplayNameBean descriptor) {
        displayNames.put(descriptor.getLang(), descriptor);
    }


    public DisplayNameBean getDisplayName(String lang) {
        return (displayNames.get(lang));
    }


    public DisplayNameBean[] getDisplayNames() {
        DisplayNameBean results[] = new DisplayNameBean[displayNames.size()];
        return (displayNames.values().toArray(results));
    }


    public void removeDisplayName(DisplayNameBean descriptor) {
        displayNames.remove(descriptor.getLang());
    }


    // ------------------------------------------------------ IconHolder Methods


    private Map<String,IconBean> icons = new TreeMap<String, IconBean>();


    public void addIcon(IconBean descriptor) {
        icons.put(descriptor.getLang(), descriptor);
    }


    public IconBean getIcon(String lang) {
        return (icons.get(lang));
    }


    public IconBean[] getIcons() {
        IconBean results[] = new IconBean[icons.size()];
        return (icons.values().toArray(results));
    }


    public void removeIcon(IconBean descriptor) {
        icons.remove(descriptor.getLang());
    }


    // -------------------------------------------------------------- Extensions


    // ----------------------------------------------------------------- Methods


}
