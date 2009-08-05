package org.glassfish.flashlight.impl.client;

/**
 * @author Sreenivas Munnangi
 *         Date: 04aug2009
 */

import java.io.OutputStream;

public final class NullStream extends OutputStream {
    public NullStream() {}

    public void write(byte b[]) {
    }

    public void write(byte b[], int off, int len) {
    }

    public void write(int b) {
    }
}
