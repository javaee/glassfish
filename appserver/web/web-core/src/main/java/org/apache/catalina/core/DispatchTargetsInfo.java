/*
 * Copyright (c) 2011-2018 Oracle and/or its affiliates. All rights reserved.
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

package org.apache.catalina.core;

import java.io.Serializable;

/**
 * This class stores information about the last dispatch target
 * which is used for AsyncContext#dispatch().
 *
 * @author Shing Wai Chan
 */
public class DispatchTargetsInfo implements Serializable {
    private String[] targets = new String[2];
    private boolean[] named = new boolean[2];

    DispatchTargetsInfo() {
    }

    void addDispatchTarget(String target, boolean isNamed) {
        targets[0] = targets[1];
        targets[1] = target;

        named[0] = named[1];
        named[1] = isNamed;
    }

    public String getLastDispatchTarget() {
        return targets[0];
    }

    public boolean isLastNamedDispatchTarget() {
        return named[0];
    }
}
