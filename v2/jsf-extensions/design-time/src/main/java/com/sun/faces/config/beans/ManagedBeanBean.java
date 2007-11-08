/*
 * $Id: ManagedBeanBean.java,v 1.1 2005/09/20 21:11:27 edburns Exp $
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


import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * <p>Configuration bean for <code>&lt;managed-bean&gt; element.</p>
 */

public class ManagedBeanBean extends FeatureBean
    implements ListEntriesHolder, MapEntriesHolder {


    // -------------------------------------------------------------- Properties


    private String managedBeanClass;
    public String getManagedBeanClass() { return managedBeanClass; }
    public void setManagedBeanClass(String managedBeanClass)
    { this.managedBeanClass = managedBeanClass; }


    private String managedBeanName;
    public String getManagedBeanName() { return managedBeanName; }
    public void setManagedBeanName(String managedBeanName)
    { this.managedBeanName = managedBeanName; }


    private String managedBeanScope;
    public String getManagedBeanScope() { return managedBeanScope; }
    public void setManagedBeanScope(String managedBeanScope)
    { this.managedBeanScope = managedBeanScope; }


    // -------------------------------------------------------------- Extensions


    // ----------------------------------------------- ListEntriesHolder Methods

    private ListEntriesBean listEntries;
    public ListEntriesBean getListEntries() { return listEntries; }
    public void setListEntries(ListEntriesBean listEntries)
    { this.listEntries = listEntries; }


    // ------------------------------------------- ManagedPropertyHolder Methods


    private List<ManagedPropertyBean> managedProperties = new ArrayList<ManagedPropertyBean>();


    public void addManagedProperty(ManagedPropertyBean descriptor) {
        managedProperties.add(descriptor);
    }


    public ManagedPropertyBean getManagedProperty(String name) {
	Iterator<ManagedPropertyBean> iter = managedProperties.iterator();
	ManagedPropertyBean cur = null;
	String  curName = null;
	while (iter.hasNext()) {
	    cur = iter.next();
	    if (null == cur) {
		continue;
	    }
	    curName = cur.getPropertyName();
	    // if the name is null, and we're looking for null
	    if (null == curName && null == name) {
		return cur;
	    }
	    // not a match
	    if (null == curName || null == name) {
		continue;
	    }
	    // guaranteed that both are non-null
	    if (curName.equals(name)) {
		return cur;
	    }
	}
	    
        return null;
    }


    public ManagedPropertyBean[] getManagedProperties() {
        ManagedPropertyBean results[] =
            new ManagedPropertyBean[managedProperties.size()];
        return (managedProperties.toArray(results));
    }


    public void removeManagedProperty(ManagedPropertyBean descriptor) {
	if (null == descriptor) {
	    return;
	}
	ManagedPropertyBean toRemove = 
	    getManagedProperty(descriptor.getPropertyName());
	if (null != toRemove) {
	    managedProperties.remove(toRemove);
	}
    }

    // ------------------------------------------------ MapEntriesHolder Methods

    private MapEntriesBean mapEntries;
    public MapEntriesBean getMapEntries() { return mapEntries; }
    public void setMapEntries(MapEntriesBean mapEntries)
    { this.mapEntries = mapEntries; }



    // ----------------------------------------------------------------- Methods


}
