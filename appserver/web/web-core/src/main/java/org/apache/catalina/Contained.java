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


/**
 * <p>Decoupling interface which specifies that an implementing class is
 * associated with at most one <strong>Container</strong> instance.</p>
 *
 * @author Craig R. McClanahan
 * @author Peter Donald
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:14 $
 */

public interface Contained {


    //-------------------------------------------------------------- Properties


    /**
     * Return the <code>Container</code> with which this instance is associated
     * (if any); otherwise return <code>null</code>.
     */
    public Container getContainer();


    /**
     * Set the <code>Container</code> with which this instance is associated.
     *
     * @param container The Container instance with which this instance is to
     *  be associated, or <code>null</code> to disassociate this instance
     *  from any Container
     */
    public void setContainer(Container container);


}
