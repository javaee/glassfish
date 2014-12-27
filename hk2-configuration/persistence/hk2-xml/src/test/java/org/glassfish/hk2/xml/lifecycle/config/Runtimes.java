/*
 *  Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 */

package org.glassfish.hk2.xml.lifecycle.config;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public interface Runtimes {
    @XmlElement(name="runtime")
    List<Runtime> getRuntimes();
    void setRuntimes(List<Runtime> runtimes);

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
