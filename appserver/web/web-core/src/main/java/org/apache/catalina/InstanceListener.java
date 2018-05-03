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

package org.apache.catalina;

import org.jvnet.hk2.annotations.Contract;


/**
 * Interface defining a listener for significant events related to a
 * specific servlet instance, rather than to the {@link Wrapper} component that
 * is managing that instance.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:17 $
 */
@Contract
public interface InstanceListener {
    /**
     * Acknowledge the occurrence of the specified event.
     *
     * @param event InstanceEvent that has occurred
     */
    public void instanceEvent(InstanceEvent event);
}
