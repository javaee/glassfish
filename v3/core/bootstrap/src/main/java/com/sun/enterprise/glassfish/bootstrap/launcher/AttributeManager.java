/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.glassfish.bootstrap.launcher;

import java.util.*;

/**
 *
 * @author bnevins
 */
class AttributeManager 
{
    void add(String name, String value)
    {
        atts.add(new Attribute(name, value));
    }

    void dump()
    {
        for(Attribute att : atts)
        {
            System.out.println(att.name + " = " + att.value);
        }
    }
    
    String getValue(String name)
    {
        for(Attribute att : atts)
        {
            if(att.name.equals(name))
                return att.value;
        }
        return null;
    }
    
    private static class Attribute
    {
        Attribute(String n, String v)
        {
            name = n;
            value = v;
        }
        
        String name;
        String value;
    }
    List<Attribute> atts = new ArrayList<Attribute>();
}

