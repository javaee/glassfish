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

/**
 * PrimitiveStandardMBean.java
 *
 * Created on Fri Jul 08 20:26:16 PDT 2005
 */
package testmbeans;

import java.util.Date;
import javax.management.ObjectName;

/**
 * Interface PrimitiveStandardMBean
 * A Standard Test MBean with various attributes that are primitive data types
 */
public interface PrimitiveStandardMBean
{
   /**
    * Get A boolean State Attribute
    */
    public boolean getState();

   /**
    * Set A boolean State Attribute
    */
    public void setState(boolean value);

   /**
    * Get An integer Rank
    */
    public int getRank();

   /**
    * Set An integer Rank
    */
    public void setRank(int value);

   /**
    * Get Time in milliseconds
    */
    public long getTime();

   /**
    * Set Time in milliseconds
    */
    public void setTime(long value);

   /**
    * Get Length in bytes
    */
    public byte getLength();

   /**
    * Set Length in bytes
    */
    public void setLength(byte value);

   /**
    * Get A Color Code as a char
    */
    public char getColorCode();

   /**
    * Set A Color Code as a char
    */
    public void setColorCode(char value);

   /**
    * Get Number of characters
    */
    public short getCharacters();

   /**
    * Set Number of characters
    */
    public void setCharacters(short value);

   /**
    * Get The Annual Percent Rate as a float
    */
    public float getAnnualPercentRate();

   /**
    * Set The Annual Percent Rate as a float
    */
    public void setAnnualPercentRate(float value);

   /**
    * Get Temperature in degrees
    */
    public double getTemperature();

   /**
    * Set Temperature in degrees
    */
    public void setTemperature(double value);

    /** 
     * Get Name as the String
     */
    public String getName();
    /**
     * Set the Name
     */
    public void setName(String name);
    
    /**
     * Get the StartDate attribute
     */
    public Date getStartDate();
    
    /**
     * Set the StartDate attribute
     */
    public void setStartDate(Date date);
    
    /**
     * Get the ObjectName of Resource
     */
    public ObjectName getResourceObjectName();
    
    /**
     * Set the ObjectName of Resource
     */
    public void setResourceObjectName(ObjectName on);
}
