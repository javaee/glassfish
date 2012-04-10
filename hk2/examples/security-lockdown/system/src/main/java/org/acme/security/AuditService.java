/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.acme.security;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Singleton;

import org.jvnet.hk2.annotations.Service;


/**
 * This is a dummy security service that should be available to some code (Alice)
 * but not directly available to other code (Mallory).  That's because Mallory would
 * spam the service and do something evil to it, while Alice would use the service
 * as intended.
 * 
 * @author jwells
 */
@Service @Singleton
public class AuditService {
    private final LinkedList<String> audits = new LinkedList<String>();
    
    /**
     * Securely (not really) audits a message
     * 
     * @param logMe The message to log
     */
    public void auditLog(String logMe) {
        if (logMe == null) return;
        audits.add(logMe);
    }
    
    /**
     * Gets everything that has been logged
     * 
     * @return A non-null (but possibly empty) set of logged messages
     */
    public List<String> getAuditLogs() {
        return Collections.unmodifiableList(audits);
    }

}
