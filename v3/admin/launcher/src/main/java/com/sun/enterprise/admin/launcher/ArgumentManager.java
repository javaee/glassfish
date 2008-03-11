/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.admin.launcher;

import java.util.*;

/**
 * API -- For now sticking with the draft1 API and behavior
 * This class will be handy for fixing error detection of bad input as below.
 * 
 * -name1 value1 -name2 value2 value3 value4 value5 -name3 -name4 -name5
 * --> "-name1":"value1",  "-name2":"value2", "default":"value5", "-name3":"-name4" 
 * 
 * @author bnevins
 */

public class ArgumentManager 
{
    public static Map<String,String> argsToMap(String[] sargs)
    {
        ArgumentManager mgr = new ArgumentManager(sargs);
        return mgr.getArgs();
    }
 
    public static Map<String,String> argsToMap(List<String>sargs)
    {
        ArgumentManager mgr = new ArgumentManager(sargs);
        return mgr.getArgs();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    //////   ALL PRIVATE BELOW      ///////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    
    private ArgumentManager(String[] sargs)
    {
        args = new ArrayList<String>();
        
        for(String s : sargs)
            args.add(s);
    }

    private ArgumentManager(List<String> sargs)
    {
        args = sargs;
    }

    private Map<String, String> getArgs()
    {
        int len = args.size();
        
        // short-circuit out of here!
        if(len <= 0)
            return map;
        
        for(int i = 0; i < len; i++)
        {
            String name = args.get(i);
            
            if(name.startsWith("-"))
            {
                // throw it away if there is no value left
                if(i + 1 < len)
                {
                    map.put(name, args.get(++i));
                }
            }
            else
            {
                // default --> last one wins!
                map.put("default", args.get(i));
            }
        }
        return map;
    }

    Map<String,String>  map     = new HashMap<String,String>();
    List<String>        args;
}
