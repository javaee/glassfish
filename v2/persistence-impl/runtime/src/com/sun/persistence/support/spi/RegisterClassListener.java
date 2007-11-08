/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
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

/*
 *  RegisterClassListener.java
 *
 * Created on July 11, 2001
 */

package com.sun.persistence.support.spi;

import java.util.EventListener;

/**
 * A "RegisterClassEvent" event gets fired whenever a persistence-capable class 
 * is loaded and gets registered with the <code>JDOImplHelper</code>. You can register a 
 * <code>RegisterClassListener</code> so as to be notified of any registration of a 
 * persistence-capable class.
 *
 * @author Michael Bouschen
 * @version 1.0
 */
public interface RegisterClassListener 
    extends EventListener
{
    /**
     * This method gets called when a persistence-capable class is registered.
     * @param event a <code>RegisterClassEvent</code> instance describing the registered 
     * class plus metadata.
     */
    public void registerClass(RegisterClassEvent event);
}
