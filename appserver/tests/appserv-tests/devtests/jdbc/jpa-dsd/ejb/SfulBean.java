/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.s1asdev.ejb.ejb30.persistence.tx_propagation;

import java.util.Map;
import java.util.HashMap;

import javax.naming.InitialContext;

import javax.ejb.Stateful;
import javax.ejb.EJB;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityNotFoundException;
import javax.annotation.sql.*;

import javax.persistence.EntityManager;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.EJBException;

@DataSourceDefinition(name = "java:app/jdbc/xa",
                        className = "org.apache.derby.jdbc.ClientXADataSource",
                        minPoolSize = 0,
                        initialPoolSize = 0,
                        user = "dbuser",
                        password = "dbpassword",
                        databaseName = "testdb",
                        properties = {"connectionAttributes=;create=true"}

                )
@Stateful
@EJB(name="ejb/SfulBean", 
        beanInterface=com.sun.s1asdev.ejb.ejb30.persistence.tx_propagation.SfulDelegate.class)

public class SfulBean
    implements Sful {

    private String name;
    
    private @EJB SfulDelegate delegate;
    
    private @PersistenceContext(unitName="lib/ejb-ejb30-persistence-tx_propagation-par1.jar#em",
                type=PersistenceContextType.EXTENDED) 
            EntityManager extendedEM;
 
    public void setName(String name) {
        this.name = name;
        try {
            String lookupName = "java:comp/env/ejb/SfulBean";
            
            InitialContext initCtx = new InitialContext();
            delegate = (SfulDelegate) initCtx.lookup(lookupName);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    public Map<String, Boolean> doTests() {
        Person person = new Person(name);
        
        String delegateName = "delgname_" + name;
        String delegateData= "delgdata: " + name;
        delegate.create(delegateName, delegateData);

        Person dPerson = extendedEM.find(Person.class, delegateName);

        extendedEM.persist(person);
        Person foundPerson = delegate.find(name);
        
        
        Map<String, Boolean> map = new HashMap<String, Boolean>();
        map.put("findDelegateCreatedPerson", (dPerson != null));
        map.put("delegateFoundMe", (foundPerson != null));

        // See https://glassfish.dev.java.net/issues/show_bug.cgi?id=3867
        extendedEM.flush();

        return map;
    }

    Person findPerson() {

        Person p = extendedEM.find(Person.class, name);
        System.out.println("Found " + p);
        return p;
    }

    boolean removePerson(String personName) {

        Person p = extendedEM.find(Person.class, personName);
        boolean removed = false;
        if (p != null) {
            extendedEM.remove(p);
            removed = true;
        }
        return removed;
    }
    
}
