/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.naming.resources;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Encapsultes the contents of a resource.
 * 
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @version $Revision: 1.1.2.1 $
 */
public class Resource {
    
    
    // ----------------------------------------------------------- Constructors
    
    
    public Resource() {
    }
    
    
    public Resource(InputStream inputStream) {
        setContent(inputStream);
    }
    
    
    public Resource(byte[] binaryContent) {
        setContent(binaryContent);
    }
    
    
    // ----------------------------------------------------- Instance Variables
    
    
    /**
     * Binary content.
     */
    protected byte[] binaryContent = null;
    
    
    /**
     * Input stream.
     */
    protected InputStream inputStream = null;
    
    
    // ------------------------------------------------------------- Properties
    
    
    /**
     * Content accessor.
     * 
     * @return InputStream
     */
    public InputStream streamContent()
        throws IOException {
        if (binaryContent != null) {
            return new ByteArrayInputStream(binaryContent);
        }
        return inputStream;
    }
    
    
    /**
     * Content accessor.
     * 
     * @return binary content
     */
    public byte[] getContent() {
        return binaryContent;
    }
    
    
    /**
     * Content mutator.
     * 
     * @param inputStream New input stream
     */
    public void setContent(InputStream inputStream) {
        this.inputStream = inputStream;
    }
    
    
    /**
     * Content mutator.
     * 
     * @param binaryContent New bin content
     */
    public void setContent(byte[] binaryContent) {
        this.binaryContent = binaryContent;
    }
    
    
}
