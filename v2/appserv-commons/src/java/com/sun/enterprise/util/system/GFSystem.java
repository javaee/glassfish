/*
 * GFSystem.java
 *
 * Created on August 21, 2007, 11:37 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.util.system;

import com.sun.enterprise.util.*;
import com.sun.logging.*;
import java.util.*;
import java.util.logging.*;

/**
 * This is a simple class that wraps evil calls to System.setProperty.  System.setProperty
 * is evil because it's very hard to debug, it's thread-unsafe, it's super-global and it is
 * over-used in GF.
 * To use it -- simply call GFSystem.setProperty() instead of System.setProperty()
 * To debug -- you can turn up the logging level with setLevel() and restore with resetLevel()
 * @author bnevins
 */
public class GFSystem
{
    private GFSystem()
    {
    }
    
    public static void setProperty(String name, String value)
    {
        System.setProperty(name, value);
        log(stringy.get("set.property", name, value));
        
        //SystemPropertyConstants.INSTANCE_ROOT_PROPERTY;
    }

    public static void setProperties(Properties props)
    {
        System.setProperties(props);
        log(stringy.get("set.properties", props));
    }
    
    public static void setLevel(Level newLevel)
    {
        level = newLevel;
    }

    public static void resetLevel()
    {
        level = DEFAULT_LEVEL;
    }
    
    private static void log(String s)
    {
        if(logger.isLoggable(level))
            logger.log(level, s);
    }
    
    private static final Logger             logger          = LogDomains.getLogger(LogDomains.UTIL_LOGGER);
    private static final Level              DEFAULT_LEVEL   = Level.FINE;
    private static       Level              level           = DEFAULT_LEVEL;
    private static final LocalStringsImpl   stringy         = new LocalStringsImpl(GFSystem.class);
}
