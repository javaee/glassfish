/*
 * $Id: ManagedPropertyBean.java,v 1.1 2005/09/20 21:11:27 edburns Exp $
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
 * <p>Configuration bean for <code>&lt;managed-property&gt; element.</p>
 */

public class ManagedPropertyBean extends FeatureBean
    implements ListEntriesHolder, MapEntriesHolder, NullValueHolder {


    // -------------------------------------------------------------- Properties


    private String propertyClass;
    public String getPropertyClass() { return propertyClass; }
    public void setPropertyClass(String propertyClass)
    { this.propertyClass = propertyClass; }


    private String propertyName;
    public String getPropertyName() { return propertyName; }
    public void setPropertyName(String propertyName)
    { this.propertyName = propertyName; }


    private String value;
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }


    // ----------------------------------------------- ListEntriesHolder Methods

    private ListEntriesBean listEntries;
    public ListEntriesBean getListEntries() { return listEntries; }
    public void setListEntries(ListEntriesBean listEntries)
    { this.listEntries = listEntries; }


    // ------------------------------------------------ MapEntriesHolder Methods

    private MapEntriesBean mapEntries;
    public MapEntriesBean getMapEntries() { return mapEntries; }
    public void setMapEntries(MapEntriesBean mapEntries)
    { this.mapEntries = mapEntries; }


    // ---------------------------------------------- NullValueHolder Properties


    private boolean nullValue = false;
    public boolean isNullValue() { return nullValue; }
    public void setNullValue(boolean nullValue) { this.nullValue = nullValue; }


    // -------------------------------------------------------------- Extensions


    // ----------------------------------------------------------------- Methods


}
