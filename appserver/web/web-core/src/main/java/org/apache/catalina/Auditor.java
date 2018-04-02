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
 * Defines interface of classes which implement audit functionality.
 *
 * <P>An <b>Auditor</b> class can be registered with a Context and
 * will receive notification of all auditable events processed by the
 * Authenticator of that context.
 *
 * <P> IASRI 4823322
 *
 * @author Jyri J. Virkki
 * @version $Revision: 1.2 $
 *
 */

public interface Auditor
{
    
    /**
     * Notify auditor of an authentication event.
     *
     * <P>This method will get invoked on every login attempt whether
     * it was approved or denied by the authentication infrastructure.
     *
     * @param user the user for whom authentication was processed
     * @param realm the realm which handled the authentication
     * @param success true if the authentication succeeded, false if denied
     */
    public void authentication(String user, String realm, boolean success);

    
    /**
     * Notify auditor of a servlet container invocation.
     *
     * <P>This method will get invoked on every request whether it
     * was permitted or not by the authorization infrastructure.
     *     
     * @param req the HttpRequest
     * @param success true if the invocation was allowed, false if denied.
     */
    public void webInvocation(HttpRequest req, boolean success);

    
}
