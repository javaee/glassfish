/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.test.basic;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jvnet.hk2.annotations.Contract;

/**
 * @author jwells
 *
 */
@XmlRootElement @Contract
public interface Employees {
    public String getCompanyName();
    
    @XmlElement(name="company-name")
    public void setCompanyName(String name);
    
    @XmlElement
    public void setFinancials(Financials finances);
    public Financials getFinancials();
    public void addFinancials();
    public Financials removeFinancials();
    
    @XmlElement(name="employee")
    public void setEmployees(List<Employee> employees);
    public List<Employee> getEmployees();
    public Employee lookupEmployees(String employeeName);
    public void addEmployees(String employeeName);
    public void addEmployees(String employeeName, int index);
    public void addEmployees(Employee employee);
    public void addEmployees(Employee employee, int index);
    public Employee removeEmployees(String employeeName);
    
    @XmlElement(name="other-data")
    public void setOtherData(List<OtherData> otherData);
    public List<OtherData> getOtherData();
    public void addOtherData(int position);
    public void addOtherData(OtherData otherData);
    public void addOtherData(OtherData otherData, int position);
    public boolean removeOtherData(int position);
    
    @XmlElement(name="bagel-type")
    @EverythingBagel(byteValue = 13,
        booleanValue=true,
        charValue = 'e',
        shortValue = 13,
        intValue = 13,
        longValue = 13L,
        floatValue = (float) 13.00,
        doubleValue = 13.00,
        enumValue = GreekEnum.BETA,
        stringValue = "13",
        classValue = Employees.class,
    
        byteArrayValue = { 13, 14 },
        booleanArrayValue = { true, false },
        charArrayValue = { 'e', 'E' },
        shortArrayValue = { 13, 14 },
        intArrayValue = { 13, 14 },
        longArrayValue = { 13L, 14L },
        floatArrayValue = { (float) 13.00, (float) 14,00 },
        doubleArrayValue = { 13.00, 14.00 },
        enumArrayValue = { GreekEnum.GAMMA, GreekEnum.ALPHA },
        stringArrayValue = { "13", "14" },
        classArrayValue = { String.class, double.class })
    public void setBagelPreference(int bagelType);
    public int getBagelPreference();
}
