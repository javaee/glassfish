package com.sun.s1asdev.ejb32.ejblite.timer;

import java.io.Serializable;

public class AppInfo implements Serializable
{
    private String foo;

    public AppInfo(String f){
        foo = f;
    }

    public String toString()
    {
	return foo;
    }
}
