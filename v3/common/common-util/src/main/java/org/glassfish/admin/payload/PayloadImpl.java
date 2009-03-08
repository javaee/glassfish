/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.admin.payload;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import org.glassfish.api.admin.Payload;

/**
 * Abstract implementation of the Payload API.
 *
 * @author tjquinn
 */
public class PayloadImpl implements Payload {

    public abstract static class Outbound implements Payload.Outbound {
        /**
         * Partial implementation of the Outbound Payload.
         */
        private final ArrayList<Payload.Part> parts = new ArrayList<Payload.Part>();

        public void addPart(
                final String contentType,
                final String name,
                final Properties props,
                final String content) throws IOException {
            parts.add(Part.newInstance(contentType, name, props, content));
        }

        public void addPart(
                final String contentType,
                final String name,
                final Properties props,
                final InputStream content) throws IOException {
            parts.add(Part.newInstance(contentType, name, props, content));
        }

        public void addPart(
                final int index,
                final String contentType,
                final String name,
                final Properties props,
                final InputStream content
                ) throws IOException {
            parts.add(index, Part.newInstance(contentType, name, props, content));
        }

        public void attachFile(
                final String contentType,
                final URI fileURI,
                final String dataRequestName,
                final File file) throws IOException {
            attachFile(contentType, fileURI, dataRequestName, null /* props */, file);
        }

        public void attachFile(
                final String contentType,
                final URI fileURI,
                final String dataRequestName,
                final Properties props,
                final File file) throws IOException {
            Properties enhancedProps = new Properties();
            if (props != null) {
                enhancedProps.putAll(props);
            }
            enhancedProps.setProperty("data-request-type", "file-xfer");
            enhancedProps.setProperty("data-request-name", dataRequestName);
            enhancedProps.setProperty("last-modified", Long.toString(file.lastModified())
                    );
            parts.add(Part.newInstance(
                    contentType,
                    fileURI.getPath(),
                    enhancedProps,
                    new BufferedInputStream(new FileInputStream(file))));
        }

        public String getHeaderName() {
            return Payload.PAYLOAD_HEADER_NAME;
        }

        public String getContentType() {
            return (isComplex()) ? getComplexContentType() : getSinglePartContentType();
        }

        ArrayList<Payload.Part> getParts() {
            return parts;
        }

        /**
         * Writes the Parts in this Outbound Payload to the specified output
         * stream; concrete implementations will implement this abstract method.
         * @param os the OutputStream to which the Parts should be written
         * @throws java.io.IOException
         */
        abstract void writePartsTo(final OutputStream os) throws IOException;

        /**
         * Writes the Payload to the specified output stream.
         *
         * @param os the OutputStream to which the Payload should be written
         * @throws java.io.IOException
         */
        public void writeTo(final OutputStream os) throws IOException {
            if (isComplex()) {
                writePartsTo(os);
            } else {
                parts.get(0).copy(os);
            }
        }

        /**
         * Returns the Content-Type which reflects that multiple Parts will be
         * in the Payload.
         * <p>
         * This content type might vary among different implementations of
         * Payload.
         *
         * @return the content type for complex payloads
         */
        public abstract String getComplexContentType();

        private boolean isComplex(final String partType) {
            return (parts.size() > 1) ||
                   ( ! partType.startsWith("text"));
        }

        private boolean isComplex() {
            return isComplex(parts.get(0).getContentType());
        }

        String getSinglePartContentType() {
            /*
             * If the one part is text/? then return it as the single-part
             * content type.  Otherwise the more complicated part is stored
             * in an implementation-dependent way so we need to return
             */
            String partType = parts.get(0).getContentType();
            if (isComplex(partType)) {
                return getComplexContentType();
            } else {
                return partType;
            }
        }

        public static Outbound newInstance() {
            return ZipPayloadImpl.Outbound.newInstance();
        }
    }

    /**
     * Partial implementation of the Inbound interface.
     */
    public static abstract class Inbound implements Payload.Inbound {

        /**
         * Creates a new Inbound Payload of the given content type, read from
         * the specified InputStream.  The payloadContentType should be the
         * content-type from the inbound http request or response.
         * @param payloadContentType content-type from the inbound http request or response
         * @param is the InputStream from which the Payload should be read
         * @return the prepared Payload
         * @throws java.io.IOException
         */
        public static Inbound newInstance(final String payloadContentType, final InputStream is) throws IOException {
            if (payloadContentType == null) {
                return EMPTY_PAYLOAD;
            }
            if (payloadContentType.startsWith("text")) {
                return TextPayloadImpl.Inbound.newInstance(payloadContentType, is);
            } else {
                return ZipPayloadImpl.Inbound.newInstance(payloadContentType, is);
            }
        }

        public String getHeaderName() {
            return Payload.PAYLOAD_HEADER_NAME;
        }

        /**
         * An empty inbound payload.
         */
        private static final Inbound EMPTY_PAYLOAD = new Inbound() {

            public Iterator<Payload.Part> parts() {
                return Collections.EMPTY_LIST.iterator();
            }

        };
    }

    /**
     * Partial implementation of Part.
     */
    public static abstract class Part implements Payload.Part {

        private String name;
        private String contentType;
        private Properties props;

        /**
         * Creates a new Part implementation.
         * @param contentType content type of the Part
         * @param name name for the Part
         * @param props Properties associated with the Part
         */
        Part(final String contentType, final String name, final Properties props) {
            this.contentType = contentType;
            this.name = name;
            /*
             * Copy the caller-supplied properties in case the caller
             * adjusts the properties later.
             */
            this.props = new Properties();
            if (props != null) {
                this.props.putAll(props);
            }
        }

        public String getName() {
            return name;
        }

        public String getContentType() {
            return contentType;
        }

        public Properties getProperties() {
            return props;
        }

        /**
         * Creates a new Part from an InputStream.
         * @param contentType content type for the Part
         * @param name name of the Part
         * @param props Properties to be associated with the Part
         * @param is InputStream to be used to populate the Part's data
         * @return the new Part
         */
        public static Part newInstance(
                final String contentType,
                final String name,
                final Properties props,
                final InputStream is) {
            return new Streamed(contentType, name, props, is);
        }

        /**
         * Creates a new Part from a String.
         * @param contentType content type for the Part
         * @param name name of the Part
         * @param props Properties to be associated with the Part
         * @param content String containing the content for the Part
         * @return
         */
        public static Part newInstance(
                final String contentType,
                final String name,
                final Properties props,
                final String content) {
            return new Buffered(contentType, name, props, content);
        }

        public void copy(final OutputStream os) throws IOException {
            int bytesRead;
            byte [] buffer = new byte[1024];
            final InputStream is = getInputStream();
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }

        /**
         * Implements Part using a stream.
         */
        static class Streamed extends PayloadImpl.Part {
            private final InputStream is;

            /**
             * Creates a new stream-baesd Part.
             * @param contentType content type for the Part
             * @param name name of the Part
             * @param props Properties to be associated with the Part
             * @param is InputStream containing the data for the Part
             */
            Streamed(
                    final String contentType,
                    final String name,
                    final Properties props,
                    final InputStream is) {
                super(contentType, name, props);
                this.is = is;
            }

            public InputStream getInputStream() {
                return is;
            }
        }

        /**
         * Implements Part using an internal buffer.
         */
        static class Buffered extends PayloadImpl.Part {
            private final String content;
            private InputStream is = null;

            /**
             * Creates a new buffer-based Part.
             * @param contentType content type for the Part
             * @param name name of the Part
             * @param props Properties to be associated with the Part
             * @param content String containing the data to be placed in the Part
             */
            Buffered(
                    final String contentType,
                    final String name,
                    final Properties props,
                    final String content) {
                super(contentType, name, props);
                this.content = content;

            }

            public InputStream getInputStream() {
                if (is == null) {
                    is = new ByteArrayInputStream(content.getBytes());
                }
                return is;
            }
        }
    }
}
