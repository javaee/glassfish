/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.internal.api;

import org.jvnet.hk2.annotations.Contract;

/**
 * Init services are run at server start. They are run after the component manager
 * is initialized but before the Startup services are run. Startup are meant to be
 * public API where users can add their own start up services. This is not the case
 * for Init services which results are expected to be fully constructed at the time
 * the first startup service is ran.
 *
 * One of the thing that an Init service can do is initialize the security manager
 * or logging or even add modules and repositories to GlassFish where Startup services
 * can be loaded from.
 */
@Contract
public interface Init {
}
