package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 */
public class CmdChainCmd implements Cmd
{
    private final List _cmds = new ArrayList();

    public CmdChainCmd()
    {
    }

    public CmdChainCmd(List cmds)
    {
        _cmds.addAll(cmds);
    }

    public void addCmd(Cmd cmd)
    {
        _cmds.add(cmd);
    }

    public Object execute() throws Exception
    {
        final Iterator it = _cmds.iterator();
        while (it.hasNext())
        {
            ((Cmd)it.next()).execute();
        }
        return new Integer(0);
    }
}
