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

package org.apache.catalina.core;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

/**
 * Facade for the <b>StandardWrapper</b> object.
 *
 * @author Remy Maucharat
 * @version $Revision: 1.3 $ $Date: 2006/11/13 19:26:30 $
 */
public final class StandardWrapperFacade
    implements ServletConfig {


    // ----------------------------------------------------------- Constructors

    /**
     * Create a new facade around a StandardWrapper.
     */
    public StandardWrapperFacade(StandardWrapper config) {

        super();
        this.config = config;
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Wrapped config.
     */
    private ServletConfig config = null;


    /**
     * The context facade object for this wrapper.
     */
    private ServletContext context = null;


    // -------------------------------------------------- ServletConfig Methods


    public String getServletName() {
        return config.getServletName();
    }


    public ServletContext getServletContext() {

        if (context == null) {
            context = config.getServletContext();
            if ((context != null) && (context instanceof ApplicationContext)) {
                context = ((ApplicationContext) context).getFacade();
            }
        }

        return context;
    }


    public String getInitParameter(String name) {
        return config.getInitParameter(name);
    }


    public Enumeration<String> getInitParameterNames() {
        return config.getInitParameterNames();
    }

}
