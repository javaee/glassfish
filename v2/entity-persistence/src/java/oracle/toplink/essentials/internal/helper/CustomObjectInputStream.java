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
package oracle.toplink.essentials.internal.helper;

import java.lang.Class;
import java.lang.ClassNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectInputStream;
import oracle.toplink.essentials.sessions.Session;
import oracle.toplink.essentials.internal.helper.ConversionManager;

/**
 * INTERNAL:
 * Defines a custom ObjectInputStream that is used with SerializedObjectMappings
 * to ensure the correct class loader is used.
 * BUG# 2813583
 *
 * @auther Guy Pelletier
 * @version 1.0 March 25/03
 */
public class CustomObjectInputStream extends ObjectInputStream {
    Session m_session;

    public CustomObjectInputStream(InputStream stream, Session session) throws IOException {
        super(stream);
        m_session = session;
    }

    public Class resolveClass(ObjectStreamClass classDesc) throws ClassNotFoundException, IOException {
        ConversionManager cm = m_session.getDatasourceLogin().getDatasourcePlatform().getConversionManager();
        return (Class)cm.convertObject(classDesc.getName(), Class.class);
    }
}
