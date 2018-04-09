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


import org.apache.catalina.HttpRequest;

import javax.servlet.ServletException;

/**
 * An <b>Authenticator</b> is a component (usually a Valve or Container) that
 * provides some sort of authentication service.  The interface itself has no
 * functional significance,  but is used as a tagging mechanism so that other
 * components can detect the presence (via an "instanceof Authenticator" test)
 * of an already configured authentication service.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:13 $
 */

public interface Authenticator {
    public void login(String userName, char[] password, HttpRequest request)
            throws ServletException;

    public void logout(HttpRequest request) throws ServletException;
}
