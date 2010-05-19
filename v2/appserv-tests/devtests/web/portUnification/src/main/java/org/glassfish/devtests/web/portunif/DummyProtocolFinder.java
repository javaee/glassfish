package org.glassfish.devtests.web.portunif;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.sun.grizzly.Context;
import com.sun.grizzly.portunif.PUProtocolRequest;
import com.sun.grizzly.portunif.ProtocolFinder;

public class DummyProtocolFinder implements ProtocolFinder {
    private final static String name = "dummy-protocol";
    private byte[] signature = name.getBytes();

    public String find(Context context, PUProtocolRequest protocolRequest)
            throws IOException {
        ByteBuffer buffer = protocolRequest.getByteBuffer();
        int position = buffer.position();
        int limit = buffer.limit();
        try {
            buffer.flip();
            if (buffer.remaining() >= signature.length) {
                for(int i=0; i<signature.length; i++) {
                    if (buffer.get(i) != signature[i]) {
                        return null;
                    }
                }

                return name;
            }
        } finally {
            buffer.limit(limit);
            buffer.position(position);
        }

        return null;
    }

}