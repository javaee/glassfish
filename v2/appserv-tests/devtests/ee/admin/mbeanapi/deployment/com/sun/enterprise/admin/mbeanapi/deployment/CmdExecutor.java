package com.sun.enterprise.admin.mbeanapi.deployment;

/**
 */
public class CmdExecutor implements Cmd
{
    private final Cmd[] _cmds;

    public CmdExecutor(Cmd[] cmds)
    {
        if (cmds == null)
        {
            throw new IllegalArgumentException();
        }
        _cmds = cmds;
    }

    public Object execute() throws Exception
    {
        for (int i = 0; i < _cmds.length; i++)
        {
            _cmds[i].execute();
        }
        return new Integer(0);
    }
}
