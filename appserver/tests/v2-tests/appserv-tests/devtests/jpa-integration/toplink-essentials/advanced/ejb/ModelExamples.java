/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

// Copyright (c) 1998, 2006, Oracle. All rights reserved.  


package oracle.toplink.essentials.testing.models.cmp3.advanced;

import java.sql.Date;

public class ModelExamples  {

     public static Employee employeeExample1(){
        Employee emp = new Employee();
        emp.setFirstName("Brady");
        emp.setLastName("Bowaster");
        emp.setPeriod(new EmploymentPeriod());
        emp.getPeriod().setStartDate(new Date(System.currentTimeMillis()-1000000));
        emp.getPeriod().setEndDate(new Date(System.currentTimeMillis()+1000000));
        emp.setSalary(15000);
        return emp;
    }

    public static Employee employeeExample2(){
        Employee emp = new Employee();
        emp.setFirstName("Sassly");
        emp.setLastName("Soosly");
        emp.setPeriod(new EmploymentPeriod());
        emp.getPeriod().setStartDate(new Date(System.currentTimeMillis()-3000000));
        emp.getPeriod().setEndDate(new Date(System.currentTimeMillis()-10000));
        emp.setSalary(1000);
        return emp;
    }
    
    public static Employee employeeExample3(){
        Employee emp = new Employee();
        emp.setFirstName("Lacy");
        emp.setLastName("Lowry");
        emp.setPeriod(new EmploymentPeriod());
        emp.getPeriod().setStartDate(new Date(System.currentTimeMillis()-48000000));
        emp.getPeriod().setEndDate(new Date(System.currentTimeMillis()+10000000));
        emp.setSalary(2);
        return emp;
    }
    
    public static Employee employeeExample4(){
        Employee emp = new Employee();
        emp.setFirstName("Ralf");
        emp.setLastName("Guedder");
        emp.setPeriod(new EmploymentPeriod());
        emp.getPeriod().setStartDate(new Date(System.currentTimeMillis()-15000000));
        emp.getPeriod().setEndDate(new Date(System.currentTimeMillis()+15000000));
        emp.setSalary(100);
        return emp;
    }
    
    public static Project projectExample1(){
        Project project = new Project();
        project.setDescription("To undertake and evaluate the effecency of the companies farmers.");
        project.setName("Farmer effecency evaluations");
        return project;
    }
    
    public static Project projectExample2(){
        LargeProject project = new LargeProject();
        project.setDescription("To assess the changing demographics of the feline world");
        project.setName("Feline Demographics Assesment");
        project.setBudget(3654563.0);
        return project;
    }
    public static Project projectExample3(){
        SmallProject project = new SmallProject();
        project.setDescription("To carefully watch the grass grow.");
        project.setName("Horticulture Quantification");
        return project;
    }
    public static Address addressExample1(){
        Address address = new Address();
        address.setCity("Washabuc");
        address.setCountry("Canada");
        address.setPostalCode("K2T3A4");
        address.setProvince("Ontario");
        address.setStreet("1734 Wallywoo Drive");
        return address;
    }

    public static Address addressExample2(){
        Address address = new Address();
        address.setCity("Ekumseekum");
        address.setCountry("Canada");
        address.setPostalCode("B2N 2C0");
        address.setProvince("Nova Scotia");
        address.setStreet("2 Main");
        return address;
    }

    public static Address addressExample3(){
        Address address = new Address();
        address.setCity("Shoolee");
        address.setCountry("Canada");
        address.setPostalCode("B1M 1C2");
        address.setProvince("Nova Scotia");
        address.setStreet("3 Main");
        return address;
    }

    public static Address addressExample4(){
        Address address = new Address();
        address.setCity("Gander");
        address.setCountry("Canada");
        address.setPostalCode("A2C1B1");
        address.setProvince("Newfoundland");
        address.setStreet("324 Bay Street");
        return address;
    }
    
    public static Address addressExample5(){
        Address address = new Address();
        address.setCity("Goose Bay");
        address.setCountry("Canada");
        address.setPostalCode("A0P1S0");
        address.setProvince("Newfoundland");
        address.setStreet("360 Hamilton Rd");
        return address;
    }
    
    public static PhoneNumber phoneExample1(){
        return new PhoneNumber("Work", "613", "6544545");
    }

    public static PhoneNumber phoneExample2(){
        return new PhoneNumber("Work", "613", "8885875");
    }

    public static PhoneNumber phoneExample3(){
        return new PhoneNumber("Home", "613", "8457451");
    }

    public static PhoneNumber phoneExample4(){
        return new PhoneNumber("Cell", "613", "3656856");
    }

    public static PhoneNumber phoneExample5(){
        return new PhoneNumber("Cell2", "613", "1254525");
    }

    public static PhoneNumber phoneExample6(){
        return new PhoneNumber("Office", "613", "7854652");
    }

    public static PhoneNumber phoneExample7(){
        return new PhoneNumber("Reception", "613", "6352145");
    }

    public static PhoneNumber phoneExample8(){
        return new PhoneNumber("NextOfKin", "613", "8974562");
    }

    public static PhoneNumber phoneExample9(){
        return new PhoneNumber("Old", "613", "3232323");
    }

}
