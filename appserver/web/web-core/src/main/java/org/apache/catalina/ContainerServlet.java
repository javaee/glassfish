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
 * A <b>ContainerServlet</b> is a servlet that has access to Catalina
 * internal functionality, and is loaded from the Catalina class loader
 * instead of the web application class loader.  The property setter
 * methods must be called by the container whenever a new instance of
 * this servlet is put into service.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:15 $
 */

public interface ContainerServlet {


    // ------------------------------------------------------------- Properties


    /**
     * Return the Wrapper with which this Servlet is associated.
     */
    public Wrapper getWrapper();


    /**
     * Set the Wrapper with which this Servlet is associated.
     *
     * @param wrapper The new associated Wrapper
     */
    public void setWrapper(Wrapper wrapper);


}
