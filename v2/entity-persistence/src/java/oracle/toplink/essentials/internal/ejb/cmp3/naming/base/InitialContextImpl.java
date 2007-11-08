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
package oracle.toplink.essentials.internal.ejb.cmp3.naming.base;

import java.util.Hashtable;
import javax.naming.*;

public abstract class InitialContextImpl implements Context {
    Hashtable env;

    // Single global namespace
    static Hashtable stringNamespace = new Hashtable();
    static Hashtable namespace = new Hashtable();

    /************************/
    /***** Internal API *****/
    /************************/
    protected abstract Object handleEntityManagerFactory(Object obj);

    protected void debug(String s) {
        System.out.println(s);
    }

    public InitialContextImpl() {
    }

    public InitialContextImpl(Hashtable env) {
        this.env = env;
    }

    public Object internalLookup(Object name) {
        // Check the String namespace first
        Object obj = stringNamespace.get(name);

        // If not found then check the real namespace 
        if (obj == null) {
            obj = namespace.get(name);
        }

        // If still not found then it just isn't there 
        if (obj == null) {
            return null;
        }
        debug("Ctx - JNDI lookup, name=" + name + " value=" + obj);
        // Temporary workaround to instantiate an EntityManager from the Factory
        
        return handleEntityManagerFactory(obj);
    }

    /*************************************/
    /***** Supported Context API *****/
    /*************************************/
    public Object lookup(String name) throws NamingException {
        Object obj = internalLookup(name);
        if (obj == null) {
            throw new NameNotFoundException(name);
        }
        return obj;
    }

    public Object lookup(Name name) throws NamingException {
        Object obj = internalLookup(name);
        if (obj == null) {
            throw new NameNotFoundException(name.toString());
        }
        return obj;
    }

    public void bind(String name, Object obj) throws NamingException {
        if (internalLookup(name) != null) {
            throw new NameAlreadyBoundException(name);
        }
        rebind(name, obj);
    }

    public void bind(Name name, Object obj) throws NamingException {
        if (internalLookup(name) != null) {
            throw new NameAlreadyBoundException(name.toString());
        }
        rebind(name, obj);
    }

    public void rebind(String name, Object obj) throws NamingException {
        stringNamespace.put(name, obj);
        //        debug("Ctx - Namespace = " + stringNamespace + " class=" + this.getClass().getClassLoader());
        // Bind as a Name as well
        rebind(new CompositeName(name), obj);
    }

    public void rebind(Name name, Object obj) throws NamingException {
        namespace.put(name, obj);
    }

    public Hashtable getEnvironment() throws NamingException {
        return env;
    }

    public void close() throws NamingException {
    }

    /*************************************/
    /***** Not supported Context API *****/
    /*************************************/
    public void unbind(Name name) throws NamingException {
    }

    public void unbind(String name) throws NamingException {
    }

    public void rename(Name oldName, Name newName) throws NamingException {
    }

    public void rename(String oldName, String newName) throws NamingException {
    }

    public NamingEnumeration list(Name name) throws NamingException {
        return null;
    }

    public NamingEnumeration list(String name) throws NamingException {
        return null;
    }

    public NamingEnumeration listBindings(Name name) throws NamingException {
        return null;
    }

    public NamingEnumeration listBindings(String name) throws NamingException {
        return null;
    }

    public void destroySubcontext(Name name) throws NamingException {
    }

    public void destroySubcontext(String name) throws NamingException {
    }

    public Context createSubcontext(Name name) throws NamingException {
        return null;
    }

    public Context createSubcontext(String name) throws NamingException {
        return null;
    }

    public Object lookupLink(Name name) throws NamingException {
        return null;
    }

    public Object lookupLink(String name) throws NamingException {
        return null;
    }

    public NameParser getNameParser(Name name) throws NamingException {
        return null;
    }

    public NameParser getNameParser(String name) throws NamingException {
        return null;
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        return null;
    }

    public String composeName(String name, String prefix) throws NamingException {
        return null;
    }

    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        return null;
    }

    public Object removeFromEnvironment(String propName) throws NamingException {
        return null;
    }

    public String getNameInNamespace() throws NamingException {
        return null;
    }
}
