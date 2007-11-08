/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
/*
 * ReplicationSSOStorePoolElement.java
 *
 * Created on June 21, 2006, 12:49 PM
 *
 */

package com.sun.enterprise.ee.web.authenticator;

import java.io.IOException;

import org.apache.catalina.Container;
import org.apache.catalina.Session;
import org.apache.catalina.session.StandardSession;
import com.sun.enterprise.security.web.SingleSignOnEntry;
import com.sun.enterprise.ee.web.sessmgmt.StorePoolElement;

/**
 *
 * @author Larry White
 */
public interface ReplicationSSOStorePoolElement extends StorePoolElement {
    
    public SingleSignOnEntry loadSSO(String ssoId, ReplicationSingleSignOn repSingleSignOn) throws IOException;
    
    public void save(String ssoId,  SingleSignOnEntry ssoEntry) throws IOException;

    public void associate(StandardSession session, String ssoId) throws IOException;

    public void remove(String ssoId) throws IOException;

    public void removeInActiveSessions(String ssoId)throws IOException;

    public void setContainer(Container container);
    
    public void setParent(ReplicationSingleSignOn parent);
    
    public void setApplicationId(String applicationId);

    public void updateLastAccessTime(String ssoID, long lat) throws IOException;    
    
}
