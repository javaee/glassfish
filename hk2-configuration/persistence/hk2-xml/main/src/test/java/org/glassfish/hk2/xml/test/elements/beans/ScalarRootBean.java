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
package org.glassfish.hk2.xml.test.elements.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.jvnet.hk2.annotations.Contract;

/**
 * @author jwells
 *
 */
@Contract
@XmlRootElement(name = "scalars")
public interface ScalarRootBean {
    @XmlElement(name="number")
    List<Integer> getNumbers();
    
    @XmlElement(name="array-number")
    int[] getArrayNumbers();
    
    @XmlElement(name="time")
    List<Long> getTimes();
    
    @XmlElement(name="array-time")
    Long[] getArrayTimes();
    
    @XmlElement(name="one-number")
    public int getOneNumber();
    
    public List<String> getStrings();
    @XmlElement(name="string")
    public void setStrings(List<String> strings);
    
    @XmlElements ({
        @XmlElement(name="e-integer", type=Integer.class)
        , @XmlElement(name="e-long", type=Long.class)
        , @XmlElement(name="e-boolean", type=Boolean.class)
        , @XmlElement(name="e-short", type=Short.class)
        , @XmlElement(name="e-character", type=Character.class)
        , @XmlElement(name="e-float", type=Float.class)
        , @XmlElement(name="e-double", type=Double.class)
        , @XmlElement(name="e-string", type=String.class)
        , @XmlElement(name="e-earth", type=EarthBean.class)
    })
    List<Object> getTypesTypesAndMoreTypes();

}
