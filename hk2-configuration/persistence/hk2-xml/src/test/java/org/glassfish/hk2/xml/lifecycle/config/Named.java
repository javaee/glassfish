/*
 *  Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 */
package org.glassfish.hk2.xml.lifecycle.config;

import javax.xml.bind.annotation.XmlAttribute;

import org.glassfish.hk2.xml.api.annotations.XmlIdentifier;

/**
 * An configured element which is named.
 * 
 * @author Jerome Dochez
 */
public interface Named {

    /**
     *  Name of the configured object
     *
     * @return name of the configured object
     */
    @XmlAttribute(required=true)
    @XmlIdentifier
    /*@NotNull*/
    public void setName(String value);
    public String getName();

    
    
}
