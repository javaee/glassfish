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
 * PrimitiveStandard.java
 *
 * Created on Fri Jul 08 20:26:16 PDT 2005
 */
package testmbeans;
import java.util.Date;
import javax.management.*;

/**
 * Class PrimitiveStandard
 * A Standard Test MBean with various attributes that are primitive data types.
 * It is now extended to add more such types.
 */
public class PrimitiveStandard implements PrimitiveStandardMBean
{
    /** Attribute : State */
    private boolean state = false;

    /** Attribute : Rank */
    private int rank = 0;

    /** Attribute : Time */
    private long time = 0;

    /** Attribute : Length */
    private byte length = (byte)0;

    /** Attribute : ColorCode */
    private char colorCode = (char)0;

    /** Attribute : Characters */
    private short characters = 0;

    /** Attribute : AnnualPercentRate */
    private float annualPercentRate = 0.0f;

    /** Attribute : Temperature */
    private double temperature = 0.0;
    
    /** Attribute: Name */
    private String name = null;
    
    /** Attribute: StartDate */
    private Date sd = null;
    
    /** Attribute ResourceObjectName */
   private ObjectName ron = null;
   
   /* Creates a new instance of PrimitiveStandard */
    public PrimitiveStandard()
    {
    }

   /**
    * Get A boolean State Attribute
    */
    public boolean getState()
    {
        return state;
    }

   /**
    * Set A boolean State Attribute
    */
    public void setState(boolean value)
    {
        state = value;
    }

   /**
    * Get An integer Rank
    */
    public int getRank()
    {
        return rank;
    }

   /**
    * Set An integer Rank
    */
    public void setRank(int value)
    {
        rank = value;
    }

   /**
    * Get Time in milliseconds
    */
    public long getTime()
    {
        return time;
    }

   /**
    * Set Time in milliseconds
    */
    public void setTime(long value)
    {
        time = value;
    }

   /**
    * Get Length in bytes
    */
    public byte getLength()
    {
        return length;
    }

   /**
    * Set Length in bytes
    */
    public void setLength(byte value)
    {
        length = value;
    }

   /**
    * Get A Color Code as a char
    */
    public char getColorCode()
    {
        return colorCode;
    }

   /**
    * Set A Color Code as a char
    */
    public void setColorCode(char value)
    {
        colorCode = value;
    }

   /**
    * Get Number of characters
    */
    public short getCharacters()
    {
        return characters;
    }

   /**
    * Set Number of characters
    */
    public void setCharacters(short value)
    {
        characters = value;
    }

   /**
    * Get The Annual Percent Rate as a float
    */
    public float getAnnualPercentRate()
    {
        return annualPercentRate;
    }

   /**
    * Set The Annual Percent Rate as a float
    */
    public void setAnnualPercentRate(float value)
    {
        annualPercentRate = value;
    }

   /**
    * Get Temperature in degrees
    */
    public double getTemperature()
    {
        return temperature;
    }

   /**
    * Set Temperature in degrees
    */
    public void setTemperature(double value)
    {
        temperature = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStartDate(Date date) {
        this.sd = date;
    }

    public void setResourceObjectName(ObjectName on) {
        this.ron = on;
    }

    public Date getStartDate() {
        return ( sd );
    }

    public ObjectName getResourceObjectName() {
        return ( ron );
    }
}
