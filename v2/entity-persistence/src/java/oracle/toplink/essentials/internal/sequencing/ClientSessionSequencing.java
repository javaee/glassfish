/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.internal.sequencing;

import oracle.toplink.essentials.internal.sequencing.Sequencing;
import oracle.toplink.essentials.threetier.ClientSession;

/**
 * ClientSessionSequencing is private to TopLink.
 * It provides sequencing for ClientSession.
 * It contains a reference to SequencingServer object owned by
 * ClientSession's parent ServerSession.
 *
 * @see SequencingServer
 * @see oracle.toplink.essentials.threetier.ClientSession
 *
 */
class ClientSessionSequencing implements Sequencing {
    // ownerClientSession
    protected ClientSession clientSession;

    // SequencingServer owned by clientSession's parent SrverSession
    protected SequencingServer sequencingServer;

    /**
    * INTERNAL:
    * Takes a potential owner - ClientSession as an argument.
    * This static method is called before an instance of this class is created.
    * The goal is to verify whether the instance of ClientSessionSequencing should be created.
    */
    public static boolean sequencingServerExists(ClientSession cs) {
        return cs.getParent().getSequencingServer() != null;
    }

    /**
    * INTERNAL:
    * Takes an owner - ClientSession as an argument.
    */
    public ClientSessionSequencing(ClientSession clientSession) {
        this.clientSession = clientSession;
        sequencingServer = clientSession.getParent().getSequencingServer();
    }

    /**
    * INTERNAL:
    * Simply calls the same method on SequencingServer
    */
    public boolean shouldAcquireValueAfterInsert(Class cls) {
        return sequencingServer.shouldAcquireValueAfterInsert(cls);
    }

    /**
    * INTERNAL:
    * Simply calls the same method on SequencingServer
    */
    public int whenShouldAcquireValueForAll() {
        return sequencingServer.whenShouldAcquireValueForAll();
    }

    /**
    * INTERNAL:
    * Simply calls the same method on SequencingServer
    */
    public boolean shouldOverrideExistingValue(Class cls, Object existingValue) {
        return sequencingServer.shouldOverrideExistingValue(cls, existingValue);
    }

    /**
    * INTERNAL:
    * This method is the reason for this class to exist:
    * SequencingServer.getNextValue takes two arguments
    * the first argument being a session which owns write connection
    * (either DatabaseSession or ClientSession).
    */
    public Object getNextValue(Class cls) {
        return sequencingServer.getNextValue(clientSession, cls);
    }
}
