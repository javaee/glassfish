package com.sun.enterprise.admin.mbeanapi.deployment;

/**
 */
public class PipeCmd implements Cmd, SourceCmd
{
    private final SourceCmd _srcCmd;
    private final SinkCmd   _sinkCmd;

    public PipeCmd(SourceCmd srcCmd, SinkCmd sinkCmd)
    {
        if ((srcCmd == null) || (srcCmd == sinkCmd))
        {
            throw new IllegalArgumentException();
        }
        _srcCmd     = srcCmd;
        _sinkCmd    = sinkCmd;
    }

    public Object execute() throws Exception
    {
        final Object o = _srcCmd.execute();
        _sinkCmd.setPipedData(o);
        return _sinkCmd.execute();
    }
}
