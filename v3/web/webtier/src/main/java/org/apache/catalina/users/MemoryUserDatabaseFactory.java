

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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


package org.apache.catalina.users;


import java.util.Hashtable;
import javax.naming.Name;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.RefAddr;
import javax.naming.spi.ObjectFactory;


/**
 * <p>JNDI object creation factory for <code>MemoryUserDatabase</code>
 * instances.  This makes it convenient to configure a user database
 * in the global JNDI resources associated with this Catalina instance,
 * and then link to that resource for web applications that administer
 * the contents of the user database.</p>
 *
 * <p>The <code>MemoryUserDatabase</code> instance is configured based
 * on the following parameter values:</p>
 * <ul>
 * <li><strong>pathname</strong> - Absolute or relative (to the directory
 *     path specified by the <code>catalina.base</code> system property)
 *     pathname to the XML file from which our user information is loaded,
 *     and to which it is stored.  [conf/tomcat-users.xml]</li>
 * </ul>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:28:13 $
 * @since 4.1
 */

public class MemoryUserDatabaseFactory implements ObjectFactory {


    // --------------------------------------------------------- Public Methods


    /**
     * <p>Create and return a new <code>MemoryUserDatabase</code> instance
     * that has been configured according to the properties of the
     * specified <code>Reference</code>.  If you instance can be created,
     * return <code>null</code> instead.</p>
     *
     * @param obj The possibly null object containing location or
     *  reference information that can be used in creating an object
     * @param name The name of this object relative to <code>nameCtx</code>
     * @param nameCtx The context relative to which the <code>name</code>
     *  parameter is specified, or <code>null</code> if <code>name</code>
     *  is relative to the default initial context
     * @param environment The possibly null environment that is used in
     *  creating this object
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                    Hashtable environment)
        throws Exception {

        // We only know how to deal with <code>javax.naming.Reference</code>s
        // that specify a class name of "org.apache.catalina.UserDatabase"
        if ((obj == null) || !(obj instanceof Reference)) {
            return (null);
        }
        Reference ref = (Reference) obj;
        if (!"org.apache.catalina.UserDatabase".equals(ref.getClassName())) {
            return (null);
        }

        // Create and configure a MemoryUserDatabase instance based on the
        // RefAddr values associated with this Reference
        MemoryUserDatabase database = new MemoryUserDatabase(name.toString());
        RefAddr ra = null;

        ra = ref.get("pathname");
        if (ra != null) {
            database.setPathname(ra.getContent().toString());
        }

        // Return the configured database instance
        database.open();
        database.save();
        return (database);

    }


}
