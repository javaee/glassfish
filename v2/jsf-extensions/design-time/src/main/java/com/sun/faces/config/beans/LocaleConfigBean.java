/*
 * $Id: LocaleConfigBean.java,v 1.1 2005/09/20 21:11:27 edburns Exp $
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


import java.util.Set;
import java.util.TreeSet;


/**
 * <p>Configuration bean for <code>&lt;locale-config&gt; element.</p>
 */

public class LocaleConfigBean {


    // -------------------------------------------------------------- Properties


    private String defaultLocale;
    public String getDefaultLocale() { return defaultLocale; }
    public void setDefaultLocale(String defaultLocale)
    { this.defaultLocale = defaultLocale; }


    // ------------------------------------------- SupportedLocaleHolder Methods


    private Set<String> supportedLocales = new TreeSet<String>();


    public void addSupportedLocale(String supportedLocale) {
        if (!supportedLocales.contains(supportedLocale)) {
            supportedLocales.add(supportedLocale);
        }
    }


    public String[] getSupportedLocales() {
        String results[] = new String[supportedLocales.size()];
        return (supportedLocales.toArray(results));
    }


    public void removeSupportedLocale(String supportedLocale) {
        supportedLocales.remove(supportedLocale);
    }


    // ----------------------------------------------------------------- Methods


}
