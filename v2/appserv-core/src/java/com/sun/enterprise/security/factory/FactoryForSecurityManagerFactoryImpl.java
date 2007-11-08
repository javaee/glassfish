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
 * FactoryForSecurityManagerFactoryImpl.java
 *
 * Created on June 9, 2003, 1:51 PM
 */

package com.sun.enterprise.security.factory;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Creates a Singleton for FactoryForSecurityManagerImpl
 * @author  Harpreet Singh
 */
public class FactoryForSecurityManagerFactoryImpl 
    implements FactoryForSecurityManagerFactory {

    private static final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    
    private static FactoryForSecurityManagerFactory _theFactory; 
    private static String WEB = "web";
    private static String EJB = "ejb";
    
    /** Creates a new instance of FactoryForSecurityManagerFactoryImpl */
    private FactoryForSecurityManagerFactoryImpl() {
    }
    
    public static FactoryForSecurityManagerFactory getInstance() {
        try {
            rwLock.readLock().lock();
            if (_theFactory != null) {
                return _theFactory;
            }
        } finally {
            rwLock.readLock().unlock();
        }

        try {
            rwLock.writeLock().lock();
            if (_theFactory == null) {
                _theFactory = new FactoryForSecurityManagerFactoryImpl();
            }
            return _theFactory;
        } finally {
            rwLock.writeLock().unlock();
        }
    }
    /* @todo uncommnent the web route
     *
     */
    public SecurityManagerFactory getSecurityManagerFactory(String type) {
        if(type.equalsIgnoreCase(WEB)){
//            return WebSecurityManagerFactory.getInstance();            
        } else if (type.equalsIgnoreCase(EJB)){
            return EJBSecurityManagerFactory.getInstance();
        }
        return null;
    }
}
