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

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.org.apache.jdo.impl.state;

import com.sun.org.apache.jdo.state.FieldManager;

/**
 * This is the means by which a StateManager implementation's setXXXField()
 * method (where XXX is e.g. Int) can give the value back to the object 
 *
 * @author Marina Vatkina
 */
class StateFieldManager implements FieldManager {

    private boolean booleanValue = false;

    private char charValue = 0;

    private byte byteValue = 0;

    private short shortValue = 0;
    
    private int intValue = 0;
    
    private long longValue = 0;
    
    private float floatValue = 0;
    
    private double doubleValue = 0;
    
    private String stringValue = null;
    
    private Object objectValue = null;
    
    /**
     * Provides the means by which the value of a boolean field can be given
     * by a StateManager to an object that needs the value.
     * @param fieldNum Field number of the field in the object whose value is
     * given.
     * @param value Boolean that is the value of a particular field.
     */
    public void storeBooleanField(int fieldNum, boolean value) {
        booleanValue = value;
    }

    public boolean fetchBooleanField(int fieldNum) {
        return booleanValue;
    }
    
    /**
     * Provides the means by which the value of a char field can be given
     * by a StateManager to an object that needs the value.
     * @param fieldNum Field number of the field in the object whose value is
     * given.
     * @param value Char that is the value of a particular field.
     */
    public void storeCharField(int fieldNum, char value){
        charValue = value; 
    } 

    public char fetchCharField(int fieldNum) {
        return charValue;
    }
    
    /**
     * Provides the means by which the value of a byte field can be given
     * by a StateManager to an object that needs the value.
     * @param fieldNum Field number of the field in the object whose value is
     * given.
     * @param value Byte that is the value of a particular field.
     */
    public void storeByteField(int fieldNum, byte value){
        byteValue = value;
    } 


    public byte fetchByteField(int fieldNum) {
        return byteValue;
    }

    /**
     * Provides the means by which the value of a short field can be given
     * by a StateManager to an object that needs the value.
     * @param fieldNum Field number of the field in the object whose value is
     * given.
     * @param value Short that is the value of a particular field.
     */
    public void storeShortField(int fieldNum, short value){
        shortValue = value;
    } 


    public short fetchShortField(int fieldNum) {
        return shortValue;
    }

    /**
     * Provides the means by which the value of a int field can be given
     * by a StateManager to an object that needs the value.
     * @param fieldNum Field number of the field in the object whose value is
     * given.
     * @param value Int that is the value of a particular field.
     */
    public void storeIntField(int fieldNum, int value){
        intValue = value;
    } 


    public int fetchIntField(int fieldNum) {
        return intValue;
    }

    /**
     * Provides the means by which the value of a long field can be given
     * by a StateManager to an object that needs the value.
     * @param fieldNum Field number of the field in the object whose value is
     * given.
     * @param value Long that is the value of a particular field.
     */
    public void storeLongField(int fieldNum, long value){
        longValue = value;
    } 


    public long fetchLongField(int fieldNum) {
        return longValue;
    }

    /**
     * Provides the means by which the value of a  field can be given
     * by a StateManager to an object that needs the value.
     * @param fieldNum Field number of the field in the object whose value is
     * given.
     * @param value  that is the value of a particular field.
     */
    public void storeFloatField(int fieldNum, float value){
        floatValue = value;
    } 


    public float fetchFloatField(int fieldNum) {
        return floatValue;
    }

    /**
     * Provides the means by which the value of a double field can be given
     * by a StateManager to an object that needs the value.
     * @param fieldNum Field number of the field in the object whose value is
     * given.
     * @param value Double that is the value of a particular field.
     */
    public void storeDoubleField(int fieldNum, double value){
        doubleValue = value;
    } 


    public double fetchDoubleField(int fieldNum) {
        return doubleValue;
    }

    /**
     * Provides the means by which the value of a String field can be given
     * by a StateManager to an object that needs the value.
     * @param fieldNum Field number of the field in the object whose value is
     * given.
     * @param value String that is the value of a particular field.
     */
    public void storeStringField(int fieldNum, String value){
        stringValue = value;
    } 


    public String fetchStringField(int fieldNum) {
        return stringValue;
    }

    /**
     * Provides the means by which the value of an Object field can be given
     * by a StateManager to an object that needs the value.
     * @param fieldNum Field number of the field in the object whose value is
     * given.
     * @param value Object that is the value of a particular field.
     */
    public void storeObjectField(int fieldNum, Object value){
        objectValue = value;
    } 


    public Object fetchObjectField(int fieldNum) {
       return objectValue;
    }

}
