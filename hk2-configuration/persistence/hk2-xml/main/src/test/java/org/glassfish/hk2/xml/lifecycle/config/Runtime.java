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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public interface Runtime extends Named, PropertyBag, Auditable {
    @XmlAttribute
    String getHostname();
    void setHostname(String hostname);

    @XmlAttribute
    String getPort();
    void setPort(String port);

    @XmlAttribute
    String getType();
    void setType(String type);

    @XmlElement(name="partition")
    List<Partition> getPartitions();
    void setPartitions(List<Partition> Partitions);

    /*
    @DuckTyped
    void update(Map<String, PropertyValue> properties);
    
    @DuckTyped
    Partition getPartitionById(String id);

    @DuckTyped
    Partition getPartitionByName(String name);

    @DuckTyped
    Partition createPartition(Map<String, PropertyValue> properties);

    @DuckTyped
    Partition deletePartition(Partition partition);
    */

    /*
    class Duck {
      public static void update(final Runtime runtime,
          final Map<String, PropertyValue> properties) throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<Runtime>() {
  
          @Override
          public Object run(Runtime runtime) throws TransactionFailure,
              PropertyVetoException {
  
            for (String propertyName : properties.keySet()) {
              String propertyValue = getString(properties.get(propertyName));
              if (propertyName.equalsIgnoreCase("type")) {
                runtime.setType(propertyValue);
              } else if (propertyName.equalsIgnoreCase("name")) {
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
                Property existingProperty = runtime.getProperty(propertyName);
                if (existingProperty != null) {
                  runtime.getProperty().remove(existingProperty);
                }
                runtime.getProperty().add(property);
              }
            }
            return runtime;
          }
        }, runtime);
      }

      public static Partition getPartitionById(final Runtime runtime, final String id) throws TransactionFailure {
        List<Partition> partitions = runtime.getPartitions();
        for (Partition partition : partitions) {
          if (partition.getId().equals(id)) {
            return partition;
          }
        }
        return null;
      }

      public static Partition getPartitionByName(final Runtime runtime, final String name) throws TransactionFailure {
        List<Partition> partitions = runtime.getPartitions();
        for (Partition partition : partitions) {
          if (partition.getName().equals(name)) {
            return partition;
          }
        }
        return null;
      }

      //private static String allocateId(Runtime writeableRuntime) {
      //  WriteableView writeableView = (WriteableView) Proxy.getInvocationHandler(writeableRuntime);
      //  Runtimes runtimeConfigs = writeableRuntime.getParent(Runtimes.class);
      //  LifecycleConfig lifecycleConfig = runtimeConfigs.getParent(LifecycleConfig.class);
      //  ObjectId objectId = lifecycleConfig.getObjectId();
      //  return objectId.allocateId(writeableView.getTransaction());
      //}

      private static PropertyValue removeProperty(Map<String, PropertyValue> properties, String propertyName) {
        if (properties != null && properties.containsKey(propertyName)) {
          PropertyValue propertyValue = properties.get(propertyName);
          properties.remove(propertyName);
          return propertyValue;
        } else {
          return null;
        }
      }

      private static String getString(PropertyValue value) {
        String propertyValue;
        if (value instanceof StringPropertyValue) {
          propertyValue = ((StringPropertyValue) value).getValue();
        } else if (value instanceof ConfidentialPropertyValue) {
          propertyValue = ((ConfidentialPropertyValue) value).getEncryptedValue();
        } else { // TODO: PropertiesPropertyValue
          propertyValue = value.toString();
        }
        return propertyValue;
      }

      public static Partition createPartition(final Runtime runtime, final Map<String, PropertyValue> properties) throws TransactionFailure {
        Partition partition = (Partition) ConfigSupport.apply(new SingleConfigCode<Runtime>() {
          @Override
          public Object run(Runtime writeableRuntime) throws TransactionFailure, PropertyVetoException {
            Partition partition = writeableRuntime.createChild(Partition.class);
            String partitionId = getString(removeProperty(properties, "id"));
            if (partitionId != null) {
              partition.setId(partitionId);
            } else {
              partitionId = getString(removeProperty(properties, "uuid"));
              if (partitionId != null) {
                partition.setId(partitionId);
              } else {
                //partitionId = allocateId(writeableRuntime);
                //partition.setId(partitionId);
                throw new RuntimeException("Create Partition config failed: No 'id' in properties map");
              }
            }
            String name = getString(removeProperty(properties, "name"));
            if (name != null) {
              partition.setName(name);
            } else {
              partition.setName("runtimePartition" + partitionId);
            }
            for (String propertyName : properties.keySet()) {
              Property property = partition.createChild(Property.class);
              try {
                property.setName(propertyName);
                property.setValue(getString(properties.get(propertyName)));
              } catch (PropertyVetoException e) {
                throw new RuntimeException(e);
              }
              partition.getProperty().add(property);
            }

            writeableRuntime.getPartitions().add(partition);
            return partition;
          }
        }, runtime);

        // read-only view
        return runtime.getPartitionByName(partition.getName());
      }

      public static Partition deletePartition(final Runtime runtime,
              final Partition partition) throws TransactionFailure {
        return (Partition) ConfigSupport.apply(new SingleConfigCode<Runtime>() {

          @Override
          public Object run(Runtime writeableRuntime)
              throws TransactionFailure {
              writeableRuntime.getPartitions().remove(partition);
            return partition;
          }

        }, runtime);

      }
    }
    */
}
