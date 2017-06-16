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