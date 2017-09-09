/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.hk2.pbuf.test.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.glassfish.hk2.pbuf.api.annotations.OneOf;
import org.jvnet.hk2.annotations.Contract;

/**
 * @author jwells
 *
 */
@Contract
@XmlRootElement(name="onOf")
@XmlType(propOrder={
        "mr"
        , "mrs"
        , "miss"
        , "between"
        , "CEO"
        , "CFO"
        , "CTO"
        , "employee"
        , "country"
        })
public interface OneOfRootBean {
    @XmlElement @OneOf("title")
    public String getMr();
    public void setMr(String mr);
    
    @XmlElement @OneOf("title")
    public String getMiss();
    public void setMiss(String name);
    
    @XmlElement @OneOf("job")
    public int getCEO();
    public void setCEO(int ceo);
    
    @XmlElement @OneOf("job")
    public CustomerBean getCFO();
    public void setCFO(CustomerBean customers);
    
    @XmlElement @OneOf("title")
    public String getMrs();
    public void setMrs(String mrs);
    
    @XmlElement @OneOf("job")
    public double getCTO();
    public void setCTO(double cto);
    
    @XmlElement
    public String getCountry();
    public void setCountry(String country);
    
    @XmlElement
    public String getBetween();
    public void setBetween(String between);
    
    @XmlElement @OneOf("job")
    public FooBean getEmployee();
    public void setEmployee(FooBean foo);
}
