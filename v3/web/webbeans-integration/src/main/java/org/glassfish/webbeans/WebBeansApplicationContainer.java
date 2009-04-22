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
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.webbeans;

import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;

public class WebBeansApplicationContainer implements ApplicationContainer {

    public Object getDescriptor() {
        return null;
    }

    public boolean start(ApplicationContext startupContxt) {
        return true;
    }

    public boolean stop(ApplicationContext stopContext) {
        return true;
    }

    public boolean suspend() {
        return false;
    }

    public boolean resume() {
        return false;
    }

    public ClassLoader getClassLoader() {
        return null;
    }
}

