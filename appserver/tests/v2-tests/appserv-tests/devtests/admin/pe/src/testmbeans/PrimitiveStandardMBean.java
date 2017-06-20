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
