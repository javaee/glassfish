/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
// Copyright (c) 1998, 2007, Oracle. All rights reserved.  


package oracle.toplink.essentials.testing.tests.cmp3.advanced;

import java.sql.Date;

/**
 * Used by ReportQueryConstructorExpressionTest to test different constructors
 */
public class DataHolder  {

    protected String string;
    protected Date date;
    protected Integer integer;
    protected int primitiveInt;

    public DataHolder() {
    }
    
    public DataHolder(String string, Date date, Integer integer){
        this.string = string;
        this.date = date;
        this.integer = integer;
    }
    
    public DataHolder(int primitiveInt){
        this.primitiveInt = primitiveInt;
    }
    
    public String getString(){
        return string;
    }
    
    public Date getDate(){
        return date;
    }
    
    public Integer getInteger(){
        return integer;
    }
    
    public int getPrimitiveInt(){
        return primitiveInt;
    }
}