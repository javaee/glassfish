/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.webservices.transport.tcp;

import com.sun.grizzly.Context;
import com.sun.grizzly.portunif.PUProtocolRequest;
import com.sun.grizzly.portunif.ProtocolFinder;
import com.sun.logging.LogDomains;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alexey Stashok
 */
public class WSTCPProtocolFinder implements ProtocolFinder {

    private static Logger logger = LogDomains.getLogger(WSTCPProtocolFinder.class, LogDomains.WEBSERVICES_LOGGER);
    private final static byte[] PROTOCOL_SCHEMA_BYTES;

    static {
        byte[] bytes;
        try {
            bytes = TCPConstants.PROTOCOL_SCHEMA.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.WARNING, "Can not convert SOAP/TCP protocol id to byte array", e);
            bytes = TCPConstants.PROTOCOL_SCHEMA.getBytes();
        }

        PROTOCOL_SCHEMA_BYTES = bytes;
    }

    public String find(Context context, PUProtocolRequest puRequest)
            throws IOException {

        ByteBuffer buffer = puRequest.getByteBuffer();
        int position = buffer.position();
        int limit = buffer.limit();
        try {
            buffer.flip();


            if (buffer.remaining() < PROTOCOL_SCHEMA_BYTES.length) {
                return null;
            }

            for (int i = 0; i < PROTOCOL_SCHEMA_BYTES.length; i++) {
                if (buffer.get(i) != PROTOCOL_SCHEMA_BYTES[i]) {
                    return null;
                }
            }

            puRequest.setMapSelectionKey(true);
            return TCPConstants.PROTOCOL_SCHEMA;
        } finally {
            buffer.limit(limit);
            buffer.position(position);
        }
    }
}
