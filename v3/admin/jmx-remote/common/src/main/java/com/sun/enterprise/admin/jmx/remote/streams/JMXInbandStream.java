/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.admin.jmx.remote.streams;

import java.io.InputStream;

public class JMXInbandStream {
    static class JMXInbandStreamContext {
        InputStream outgoing = null;
        InputStream incoming = null;
        long length = -1;
    }

    private static ThreadLocal thrLocal = new ThreadLocal();

    public static void setOutputStream(InputStream in, long len) {
        JMXInbandStreamContext ctx = getContext();
        ctx.outgoing = in;
        if (len > 0)
            ctx.length = len;
        thrLocal.set(ctx);
    }

    public static InputStream getOutgoingStream() {
        JMXInbandStreamContext ctx = getContext();
        return ctx.outgoing;
    }

    public static long getOutgoingStreamLength() {
        JMXInbandStreamContext ctx = getContext();
        return ctx.length;
    }

    public static InputStream getInputStream() {
        JMXInbandStreamContext ctx = getContext();
        return ctx.incoming;
    }

    public static void setIncomingStream(InputStream in) {
        JMXInbandStreamContext ctx = getContext();
        ctx.incoming = in;
        thrLocal.set(ctx);
    }

    private static JMXInbandStreamContext getContext() {
        JMXInbandStreamContext ctx = 
            (JMXInbandStreamContext) thrLocal.get();
        if (ctx == null) {
            ctx = new JMXInbandStreamContext();
        }
        return ctx;
    }
}

