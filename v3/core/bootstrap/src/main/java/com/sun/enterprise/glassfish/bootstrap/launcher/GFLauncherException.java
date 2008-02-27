package com.sun.enterprise.glassfish.bootstrap.launcher;

/**
 *
 * @author bnevins
 */
public class GFLauncherException extends Exception 
{
    public GFLauncherException(String msg) 
    {
        super(strings.get(msg));
    }

    public GFLauncherException(String msg, Object... objs) 
    {
        super(strings.get(msg, objs));
    }

    public GFLauncherException(String msg, Throwable t) 
    {
        super(strings.get(msg), t);
    }

    public GFLauncherException(String msg, Throwable t, Object... objs) 
    {
        super(strings.get(msg, objs), t);
    }
    
    private final static LocalStringsImpl strings = new LocalStringsImpl(GFLauncherException.class);
}
