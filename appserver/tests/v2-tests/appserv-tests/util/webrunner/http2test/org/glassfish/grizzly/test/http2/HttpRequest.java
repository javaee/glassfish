/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.grizzly.test.http2;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.Protocol;
import org.glassfish.grizzly.memory.Buffers;
import org.glassfish.grizzly.memory.MemoryManager;

/**
 * A simple Http2 request based on Grizzly runtime.
 *
 * @author Shing Wai Chan
 */
public class HttpRequest {
    private HttpRequestPacket httpRequestPacket;
    private Connection connection;
    private Buffer contentBuffer = Buffers.EMPTY_BUFFER;

    private HttpRequest(HttpRequestPacket httpRequestPacket,
            String charEncoding, String content, Connection connection) {
        this.httpRequestPacket = httpRequestPacket;
        httpRequestPacket.setCharacterEncoding(charEncoding);
        this.connection = connection;

        if (content != null) {
            MemoryManager mm = MemoryManager.DEFAULT_MEMORY_MANAGER;
            if (charEncoding != null) {
                try {
                    byte[] bytes = content.getBytes(charEncoding);
                    contentBuffer = Buffers.wrap(mm, bytes);
                } catch(UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                contentBuffer = Buffers.wrap(mm, content);
            }
        }
        httpRequestPacket.setContentLength(contentBuffer.remaining());
    }

    public void send() {
        connection.write(HttpContent.builder(httpRequestPacket)
                .content(contentBuffer)
                .last(true)
                .build());
    }

    // ----- inner class -----
    public static class Builder {
        private HttpRequestPacket.Builder reqPacketBuilder = new  HttpRequestPacket.Builder();
        private String charEncoding = null;
        private String content = null;
        private Connection connection = null;

        Builder(String host, int port, Connection connection) {
            reqPacketBuilder.method("GET").protocol(Protocol.HTTP_1_1)
                .header("Host", host + ":" + port);
            this.connection = connection;
        }

        public Builder method(String method) {
            reqPacketBuilder.method(method);
            return this;
        }

        public Builder path(String path) {
            reqPacketBuilder.uri(path);
            return this;
        }

        public Builder contentType(String contentType) {
            reqPacketBuilder.contentType(contentType);
            return this;
        }

        public Builder characterEncoding(String charEncoding) {
            this.charEncoding = charEncoding;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder header(String name, String value) {
            reqPacketBuilder.header(name, value);
            return this;
        }

        public HttpRequest build() {
            return new HttpRequest(reqPacketBuilder.build(), charEncoding, content, connection);
        }
    }
}
