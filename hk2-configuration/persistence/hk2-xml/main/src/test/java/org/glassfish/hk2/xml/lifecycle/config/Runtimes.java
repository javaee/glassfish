/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.xml.lifecycle.config;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public interface Runtimes {
    @XmlElement(name="runtime")
    List<Runtime> getRuntimes();
    void setRuntimes(List<Runtime> runtimes);
    Runtime lookupRuntime(String runtime);
    

    /*
    @DuckTyped
    <T extends Runtime> List<T> getRuntimeByType(Class<T> type);

    @DuckTyped
    <T extends Runtime> T getRuntimeByName(String name);

    @DuckTyped
    <T extends Runtime> T createRuntime(Map<String, PropertyValue> properties);
    
    @DuckTyped
    <T extends Runtime> T deleteRuntime(T runtime);
    */

    /*
    class Duck  {
        public static <T extends Runtime> List<T> getRuntimeByType(Runtimes runtimes, Class<T> type) {
            if (type == null || runtimes.getRuntimes() == null) {
                return null;
            }
            List<T> result = new ArrayList<T>();
            for (Runtime runtime: runtimes.getRuntimes()) {
                if (runtime.getClass().equals(type)) {
                    result.add(type.cast(runtime));
                }
            }
            return result;
        }
      
        @SuppressWarnings("unchecked")
        public static <T extends Runtime> T getRuntimeByName(Runtimes runtimes, String name) {
            if (name == null || runtimes.getRuntimes() == null) {
                return null;
            }
            for (Runtime runtime: runtimes.getRuntimes()) {
                if (runtime.getName().equals(name)) {   
                    return (T) runtime;
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        public static <T extends Runtime> T createRuntime(
                final Runtimes runtimes,
                final Map<String, PropertyValue> properties)
                throws TransactionFailure {
            T runtime = (T) ConfigSupport.apply(new SingleConfigCode<Runtimes>() {

                @Override
                public Object run(Runtimes writeableRuntime)
                        throws TransactionFailure,
                        PropertyVetoException {

                    Runtime runtime = writeableRuntime.createChild(Runtime.class);
                    for (String propertyName : properties.keySet()) {
                        String propertyValue;
                        Object value = properties.get(propertyName);
                        if (value instanceof StringPropertyValue) {
                          propertyValue = ((StringPropertyValue) value).getValue();
                        } else if (value instanceof ConfidentialPropertyValue) {
                          propertyValue = ((ConfidentialPropertyValue) value).getEncryptedValue();
                        } else { // TODO: PropertiesPropertyValue
                          propertyValue = value.toString();
                        }

                        if (propertyName.equalsIgnoreCase("type")) {
                            runtime.setType(propertyValue);
                        } else
                        if (propertyName.equalsIgnoreCase("name")) {
                            runtime.setName(propertyValue);
                        } else if (propertyName.equalsIgnoreCase("hostname")) {
                            runtime.setHostname(propertyValue);
                        } else if (propertyName.equalsIgnoreCase("port")) {
                            runtime.setPort(propertyValue);
                        } else {
                            Property property = runtime.createChild(Property.class);
                            try {
                                property.setName(propertyName);
                                property.setValue(propertyValue);
                            } catch (PropertyVetoException e) {
                                throw new RuntimeException(e);
                            }
                            runtime.getProperty().add(property);
                        }
                    }
                    writeableRuntime.getRuntimes().add(runtime);
                    return runtime;
                }
            }, runtimes);

            // read-only view
            return runtimes.getRuntimeByName(runtime.getName());
        }
        
        @SuppressWarnings("unchecked")
        public static <T extends Runtime> T deleteRuntime(final Runtimes runtimes,
                final T runtime) throws TransactionFailure {
            return (T) ConfigSupport.apply(new SingleConfigCode<Runtimes>() {

                @Override
                public Object run(Runtimes writeableRuntimes)
                        throws TransactionFailure {
                    writeableRuntimes.getRuntimes().remove(runtime);
                    return runtime; 

                }
            
            }, runtimes);
        
        }
    }
    */
}
