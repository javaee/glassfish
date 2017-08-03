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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.javaee7.samples.batch.twosteps;

import javax.inject.Inject;


@javax.inject.Named//("com.oracle.javaee7.samples.batch.simple.SimpleItemProcessor")
public class SimpleItemProcessor
    implements javax.batch.api.ItemProcessor<String, String> {

//    @Inject
//    IdGenerator idGen;
    
    @Override
    public String processItem(String t) throws Exception {
        String[] record = t.split(", ");
        
    //EMP-ID, MONTH-YEAR, SALARY, TAX%, MEDICARE%, OTHER
        int salary = Integer.valueOf(record[2]);
        double tax = Double.valueOf(record[3]);
        double mediCare = Double.valueOf(record[4]);
        StringBuilder sb = new StringBuilder(t);
        sb.append(", ").append(salary * tax / 100);
        sb.append(", ").append(salary * mediCare / 100);
        sb.append(", ").append(salary - (salary * tax / 100) - (salary * mediCare / 100));
        return  sb.toString();
    }
    
}
