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
package org.glassfish.hk2.configuration.properties.test;

import java.io.File;

/**
 * A bean with a lot of useless stuff in it
 * 
 * @author jwells
 *
 */
public class FooBean {
    /** The default type name for this bean */
    public final static String TYPE_NAME = "FooBeanType";
    
    private boolean fooBool;
    private short fooShort;
    private int fooInt;
    private long fooLong;
    private float fooFloat;
    private double fooDouble;
    private char fooChar;
    private String fooString;
    private byte fooByte;
    private File fooFile;
    
    // The getter/setter will use SNMP
    private String snmpValue;
    
    /**
     * @return the fooBool
     */
    public boolean isFooBool() {
        return fooBool;
    }
    /**
     * @param fooBool the fooBool to set
     */
    public void setFooBool(boolean fooBool) {
        this.fooBool = fooBool;
    }
    /**
     * @return the fooShort
     */
    public short getFooShort() {
        return fooShort;
    }
    /**
     * @param fooShort the fooShort to set
     */
    public void setFooShort(short fooShort) {
        this.fooShort = fooShort;
    }
    /**
     * @return the fooInt
     */
    public int getFooInt() {
        return fooInt;
    }
    /**
     * @param fooInt the fooInt to set
     */
    public void setFooInt(int fooInt) {
        this.fooInt = fooInt;
    }
    /**
     * @return the fooLong
     */
    public long getFooLong() {
        return fooLong;
    }
    /**
     * @param fooLong the fooLong to set
     */
    public void setFooLong(long fooLong) {
        this.fooLong = fooLong;
    }
    /**
     * @return the fooFloat
     */
    public float getFooFloat() {
        return fooFloat;
    }
    /**
     * @param fooFloat the fooFloat to set
     */
    public void setFooFloat(float fooFloat) {
        this.fooFloat = fooFloat;
    }
    /**
     * @return the fooDouble
     */
    public double getFooDouble() {
        return fooDouble;
    }
    /**
     * @param fooDouble the fooDouble to set
     */
    public void setFooDouble(double fooDouble) {
        this.fooDouble = fooDouble;
    }
    /**
     * @return the fooChar
     */
    public char getFooChar() {
        return fooChar;
    }
    /**
     * @param fooChar the fooChar to set
     */
    public void setFooChar(char fooChar) {
        this.fooChar = fooChar;
    }
    /**
     * @return the fooString
     */
    public String getFooString() {
        return fooString;
    }
    /**
     * @param fooString the fooString to set
     */
    public void setFooString(String fooString) {
        this.fooString = fooString;
    }
    /**
     * @return the fooByte
     */
    public byte getFooByte() {
        return fooByte;
    }
    /**
     * @param fooByte the fooByte to set
     */
    public void setFooByte(byte fooByte) {
        this.fooByte = fooByte;
    }
    /**
     * @return the fooFile
     */
    public File getFooFile() {
        return fooFile;
    }
    /**
     * @param fooFile the fooFile to set
     */
    public void setFooFile(File fooFile) {
        this.fooFile = fooFile;
    }
    /**
     * @return the snmpValue
     */
    public String getSNMPValue() {
        return snmpValue;
    }
    /**
     * @param snmpValue the snmpValue to set
     */
    public void setSNMPValue(String snmpValue) {
        this.snmpValue = snmpValue;
    }
    
    

}
