/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.fileupload;

import java.io.IOException;
import java.io.InputStream;


/**
 * <p> This interface provides access to a file or form item that was
 * received within a <code>multipart/form-data</code> POST request.
 * The items contents are retrieved by calling {@link #openStream()}.</p>
 * <p>Instances of this class are created by accessing the
 * iterator, returned by * {@link RequestItemIterator()}.</p>
 *
 * Original authors from org.apache.common.fileupload.FileItem:
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:sean@informage.net">Sean Legassick</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 *
 * Adopted for Glassfish:
 * @author Kin-man Chung
 */

interface RequestItem {

    /** Creates an {@link InputStream}, which allows to read the
     * items contents.
     * @return The input stream, from which the items data may
     *   be read.
     * @throws IllegalStateException The method was already invoked on
     * this item. It is not possible to recreate the data stream.
     * @throws IOException An I/O error occurred.
     * @see ItemSkippedException
     */
    InputStream openStream() throws IOException;

    /**
     * Closes the file item.
     * @throws IOException An I/O error occurred.
     */
    void close() throws IOException;

    /**
     * Returns the content type passed by the browser or <code>null</code> if
     * not defined.
     *
     * @return The content type passed by the browser or <code>null</code> if
     *         not defined.
     */
    String getContentType();

    /**
     * Returns the original filename in the client's filesystem, as provided by
     * the browser (or other client software). In most cases, this will be the
     * base file name, without path information. However, some clients, such as
     * the Opera browser, do include path information.
     *
     * @return The original filename in the client's filesystem.
     */
    String getSubmittedFileName();

    /**
     * Returns the name of the field in the multipart form corresponding to
     * this file item.
     *
     * @return The name of the form field.
     */
    String getFieldName();

    /**
     * Determines whether or not a <code>FileItem</code> instance represents
     * a simple form field.
     *
     * @return <code>true</code> if the instance represents a simple form
     *         field; <code>false</code> if it represents an uploaded file.
     */
    boolean isFormField();
    
    /**
     * Returns the item headers.
     * @return The items header object
     */
    public PartHeaders getHeaders();

}
