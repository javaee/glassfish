package com.sun.s1asdev.ejb.timer.timertests;

import java.io.*;
import javax.ejb.*;

public class BarPrimaryKey implements Serializable
{
    public Long id;
    public String value2;

    public BarPrimaryKey(){}

    public BarPrimaryKey(Long id, String value2)
    {
        this.id = id;
        this.value2 = value2;
    }

    public boolean equals(Object other)
    {
	if ( other instanceof BarPrimaryKey ) {
            BarPrimaryKey bpk = (BarPrimaryKey) other;
	    return ((id.equals(bpk.id)) 
                    && (value2.equals(bpk.value2)));
        }
	return false;
    }

    public int hashCode()
    {
	return id.hashCode();
    }

    public String toString()
    {
	return id + "_" + value2;
    }
}
