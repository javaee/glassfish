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
 
 package org.glassfish.api.admin;

import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

 /**
  * <p>
  * Helper class to execute some code on configuration objects while taking
  * care of the transaction boiler plate code.
  * </p>
  * <p>
  * Programmers that wish to apply some changes to configuration objects
  * can use these convenience methods to reduce the complexity of handling
  * transactions.
  * </p>
  * <p>
  * For instance, say a programmer need to change the HttpListener port from
  * 8080 to 8989, it just needs to do :
  * </p>
  * <pre>
  *     ... in his code somewhere ...
  *     HttpListener httpListener = domain.get...
  *
  *     // If the programmer tries to modify the httpListener directly
  *     // it will get an exception
  *     httpListener.setPort("8989"); // will generate a PropertyVetoException
  *
  *     // instead he needs to use a transaction and can use the helper services
  *     TransactionHelper.apply(new SingleConfigCode<HttpListener>() {
  *         public boolean run(HttpListener okToChange) throws PropertyException {
  *             okToChange.setPort("8989"); // good...
  *             httpListener.setPort("7878"); // not good, exceptions still raised...
  *             return true;
  *         });
  *
  *     // Note that after this code
  *     System.out.println("Port is " + httpListener.getPort());
  *     // will display 8989
  * }
  * </pre>
  * @author Jerome Dochez
  */
 public class TransactionHelper {
 
    /**
     * Execute§ some logic on one config bean of type T protected by a transaction
     *
     * @param code code to execute
     * @param param config object participating in the transaction
     * @return list of events that represents the modified config elements.
     */
    public static <T extends ConfigBean> List<PropertyChangeEvent> apply(final SingleConfigCode<T> code, T param)
        throws PropertyVetoException {
        
        ConfigBean[] objects = new ConfigBean[1];
        objects[0]=param;
        return apply((new ConfigCode() {
            public boolean run(ConfigBean... objects) throws PropertyVetoException {
                return code.run((T) objects[0]);
            }
        }), objects);
    }
    
    /**
     * Executes some logic on some config beans protected by a transaction.
     *
     * @param code code to execute
     * @param objects config beans participating to the transaction
     */
    public static List<PropertyChangeEvent> apply(ConfigCode code, ConfigBean... objects)
            throws PropertyVetoException {
        
        // the fools think they operate on the "real" object while I am
        // feeding them with deep copies. Only if the transaction succeed
        // will I apply the "changes" to the real ones.
        ConfigBean[] cheapCopies = new ConfigBean[objects.length];
        for (int i=0;i<objects.length;i++) {
            //cheapCopies[i] = SerialClone.clone(objects[i]);
            try {
                cheapCopies[i] = (ConfigBean) objects[i].clone();
            } catch(CloneNotSupportedException e) {
                // does not matter as this code will be replaced with
                // DOLElement clone().
            }
        }

        // Of course I am not locking the live objects but the copies.
        // if the user try to massage the real objects, he will get
        // a well deserved nasty exception
        Transaction t = new Transaction();
        for (ConfigBean configBean : cheapCopies) {
            if (!configBean.join(t)) {
                t.rollback();
                return null;
            }
        }
        
        List<PropertyChangeEvent> transactionEvents = null;
        try {
            if (code.run(cheapCopies)) {
                try {
                    transactionEvents = t.commit();
                } catch (RetryableException e) {
                    System.out.println("Retryable...");
                    // TODO : do something meaninful here
                    t.rollback();
                    return null;
                } catch (TransactionFailure e) {
                    System.out.println("failure, not retryable...");
                    t.rollback();
                    return null;
                }

            } else {
                t.rollback();
                return null;
            }
        } catch (PropertyVetoException e) {
            t.rollback();
            throw e;
        }

        // now apply the changes to the "real objects if this was a success
        if (transactionEvents!=null && transactionEvents.size()>0) {
            // now I need to lock the live copies
            Transaction real = new Transaction();
            for (ConfigBean configBean : objects) {
                if (!configBean.join(real)) {
                    // should I wait and retry ?
                    real.rollback();
                    return null;
                }
            }

            // process to events on the "real" objects
            for (PropertyChangeEvent event : transactionEvents) {
                // the trick is to find the right object on which the event applies.
                int i=0;
                while (i<cheapCopies.length && cheapCopies[i]!=event.getSource())
                    i++;

                if (i<cheapCopies.length) {
                    ConfigBean source = objects[i];
                    try {
                        // TODO : cache those bean info, faster algorithm needed here.
                        BeanInfo info = Introspector.getBeanInfo(source.getClass());
                        for (PropertyDescriptor prop : info.getPropertyDescriptors()) {
                            if (prop.getName().equals(event.getPropertyName())) {
                                try {
                                    try {
                                        // remember that new objects also need to be part of the transaction
                                        ConfigBean configBean = ConfigBean.class.cast(event.getNewValue());
                                        configBean.join(real);
                                    } catch(ClassCastException e) {
                                        // ignore
                                    }
                                    prop.getWriteMethod().invoke(source, event.getNewValue());
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }
                            }
                        }

                    } catch (IntrospectionException e) {
                        System.out.println("Cannot introspect " + source.getClass() + " : " + e);
                    }


                }                

            }

            try {
                return real.commit();
            } catch (RetryableException e) {
                // should I wait and retry
            } catch (TransactionFailure e) {
                    System.out.println("failure, not retryable...");
                    real.rollback();
                    return null;
            }

        }
        return null;
    } 	
 
 }