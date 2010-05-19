package org.glassfish.devtests.web.portunif;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;

import com.sun.grizzly.Context;
import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.util.OutputWriter;

public class DummyProtocolFilter implements ProtocolFilter {
    public boolean execute(Context ctx) throws IOException {
        SelectableChannel channel = ctx.getSelectionKey().channel();
        OutputWriter.flushChannel(channel, ByteBuffer.wrap("Dummy-Protocol-Response".getBytes()));
        ctx.getSelectorHandler().closeChannel(channel);
        return false;
    }

    public boolean postExecute(Context ctx) throws IOException {
        return true;
    }

}
