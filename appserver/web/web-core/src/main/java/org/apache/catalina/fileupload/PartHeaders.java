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

import java.io.Serializable;
import java.util.*;

/**
 * @author Michael C. Macaluso
 * @author Kin-man Chung (Adopted for Glassfish)
 */
class PartHeaders implements Serializable {
    private static final long serialVersionUID = -4455695752627032559L;

    /**
     * Map of <code>String</code> keys to a <code>List</code> of
     * <code>String</code> instances.
     */
    private final Map<String, List<String>> headerMap =
        new LinkedHashMap<String, List<String>>();

    private Set<String> headerNames = null;

    public String getHeader(String name) {
        String nameLower = name.toLowerCase(Locale.ENGLISH);
        List<String> headerValueList = headerMap.get(nameLower);
        if (null == headerValueList) {
            return null;
        }
        return headerValueList.get(0);
    }

    public synchronized Collection<String> getHeaderNames() {
        if (null == headerNames) {
            headerNames = Collections.unmodifiableSet(headerMap.keySet());
        }
        return headerNames;
    }

    public List<String> getHeaders(String name) {
        String nameLower = name.toLowerCase(Locale.ENGLISH);
        List<String> headerValueList = headerMap.get(nameLower);
        if (null == headerValueList) {
            return Collections.emptyList();
        }
        return headerValueList;
    }

    /**
     * Method to add header values to this instance.
     *
     * @param name name of this header
     * @param value value of this header
     */
    public synchronized void addHeader(String name, String value) {
        String nameLower = name.toLowerCase(Locale.ENGLISH);
        List<String> headerValueList = headerMap.get(nameLower);
        if (null == headerValueList) {
            headerValueList = new ArrayList<String>();
            headerMap.put(nameLower, headerValueList);
        }
        headerValueList.add(value);
    }
}
