/*
 *  Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 */
package org.glassfish.hk2.xml.lifecycle.config;

import java.util.List;


// import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface Environment extends Named, Auditable {
  @XmlElement
  // @NotNull
  void setAssociations(Associations associations);
  Associations getAssociations();
  

  @XmlElement(name="partition-ref")
  void setPartitionRefs(List<PartitionRef> partitionRefs);
  List<PartitionRef> getPartitionRefs();
  

  /*
  @DuckTyped
  List<Partition> getPartitions();

  @DuckTyped
  Partition getPartitionById(String id);

  @DuckTyped
  PartitionRef getPartitionRefById(String id);

  @DuckTyped
  PartitionRef getPartitionRefByName(String name);

  @DuckTyped
  PartitionRef createPartitionRef(Partition partition, Properties properties);

  @DuckTyped
  PartitionRef deletePartitionRef(PartitionRef partitionRef);

  @DuckTyped
  Association createAssociation(Partition partition1, Partition partition2);

  @DuckTyped
  Association createAssociation(PartitionRef partition1, PartitionRef partition2);

  @DuckTyped
  Association createAssociation(String partition1, String partition2);

  @DuckTyped
  Association removeAssociation(Partition partition1, Partition partition2);

  @DuckTyped
  Association removeAssociation(PartitionRef partition1, PartitionRef partition2);

  @DuckTyped
  Association removeAssociation(String partition1, String partition2);

  @DuckTyped
  List<Association> findAssociations(Partition partition);

  @DuckTyped
  List<Association> findAssociations(PartitionRef partition);

  @DuckTyped
  List<Association> removeAssociations(Partition partition);

  @DuckTyped
  List<Association> removeAssociations(PartitionRef partition);

  @DuckTyped
  List<Association> removeAssociations(String partition);

  class Duck {

      public static List<Partition> getPartitions(final Environment environment) throws TransactionFailure {
        List<PartitionRef> partitionRefs = environment.getPartitionRefs();
        List<Partition> partitions = new ArrayList<Partition>(partitionRefs.size());
        for (PartitionRef partitionRef : partitionRefs) {
        	partitions.add(getPartitionById(environment, partitionRef.getId()));
        }
        return partitions;
      }

      public static Partition getPartitionById(final Environment environment, final String id) {
    	ServiceLocator serviceLocator = Dom.unwrap(environment).getHabitat();
    	return serviceLocator.getService(Partition.class, id);
      }

      public static PartitionRef getPartitionRefById(final Environment environment, final String id) throws TransactionFailure {
        List<PartitionRef> partitionRefs = environment.getPartitionRefs();
        for (PartitionRef partitionRef : partitionRefs) {
          if (partitionRef.getId().equals(id)) {
            return partitionRef;
          }
        }
        return null;
      }

      // FIXME: REMOVE
      public static PartitionRef getPartitionRefByName(final Environment environment, final String name) throws TransactionFailure {
        List<PartitionRef> partitionRefs = environment.getPartitionRefs();
        for (PartitionRef partitionRef : partitionRefs) {
          Partition partition = partitionRef.getRuntime().getPartitionByName(name);
          if (partition != null && partitionRef.getId().equals(partition.getId())) {
            return partitionRef;
          }
        }
        return null;
      }

      public static PartitionRef createPartitionRef(final Environment environment, final Partition partition, final Properties properties) throws TransactionFailure {
      PartitionRef partitionRef = (PartitionRef) ConfigSupport.apply(new SingleConfigCode<Environment>() {
        @Override
        public Object run(Environment writeableEnvironment) throws TransactionFailure, PropertyVetoException {
          PartitionRef partitionRef = writeableEnvironment.createChild(PartitionRef.class);
          partitionRef.setId(partition.getId());
          partitionRef.setRuntimeRef(partition.getRuntime().getName());
          for (String propertyName : properties.stringPropertyNames()) {
            Property property = partitionRef.createChild(Property.class);
            try {
              property.setName(propertyName);
              property.setValue(properties.getProperty(propertyName));
            } catch (PropertyVetoException e) {
              throw new RuntimeException(e);
            }
            partitionRef.getProperty().add(property);
          }
          writeableEnvironment.getPartitionRefs().add(partitionRef);
          return partitionRef;
        }
      }, environment);

      // read-only view
      return environment.getPartitionRefById(partitionRef.getId());
    }

    public static PartitionRef deletePartitionRef(final Environment environment,
            final PartitionRef partitionRef) throws TransactionFailure {
      return (PartitionRef) ConfigSupport.apply(new SingleConfigCode<Environment>() {

        @Override
        public Object run(Environment writeableEnvironment)
            throws TransactionFailure {
          writeableEnvironment.getPartitionRefs().remove(partitionRef);
          return partitionRef; 
        }

      }, environment);
    
    }

    public static Association createAssociation(final Environment environment, final Partition partition1, final Partition partition2) throws TransactionFailure {
      validateEnvironmentPartition(environment, partition1.getId());
      validateEnvironmentPartition(environment, partition2.getId());
      // validate unique association
      List<Association> existingAssociations = findAssociations(environment, partition1);
      String[] partitionIds = new String[] {partition1.getId(), partition2.getId()};
      Arrays.sort(partitionIds);
      for (Association association : existingAssociations) {
        String[] existingPartitionIds = new String[] {association.getPartition1().getId(), association.getPartition2().getId()}; 
        Arrays.sort(existingPartitionIds);
        
        if (Arrays.equals(existingPartitionIds, partitionIds)) {
          throw new IllegalArgumentException("Partitions " + partition1.getId() + " and " + partition2.getId() + " are already associated."); 
        }
      }
      // do create association
      Associations associations = environment.getAssociations();
      Association association = (Association) ConfigSupport.apply(new SingleConfigCode<Associations>() {
        @Override
        public Object run(Associations writeableAssociations) throws TransactionFailure, PropertyVetoException {
          Association association = writeableAssociations.createChild(Association.class);
          association.setPartition1(partition1);
          association.setPartition2(partition2);
          writeableAssociations.getAssociations().add(association);
          return association;
        }
      }, associations);
      // FIXME: read-only view
      return association;
    }

    private static void validateEnvironmentPartition(final Environment environment, String partitionId) throws IllegalArgumentException {
      if (environment.getPartitionRefById(partitionId) == null) {
        throw new IllegalArgumentException("Partition " + partitionId + " is not added to the environment.");  
      }
    }

    public static Association createAssociation(final Environment environment, final PartitionRef partition1, final PartitionRef partition2) throws TransactionFailure {
      Partition p1 = getPartitionById(environment, partition1.getId());
      Partition p2 = getPartitionById(environment, partition2.getId());
      return createAssociation(environment, p1, p2);
    }

    public static Association createAssociation(final Environment environment, final String partition1, final String partition2) throws TransactionFailure {
      Partition p1 = getPartitionById(environment, partition1);
      Partition p2 = getPartitionById(environment, partition2);
      return createAssociation(environment, p1, p2);
    }

    public static Association removeAssociation(final Environment environment, final Partition partition1, final Partition partition2) throws TransactionFailure {
      List<Association> existingAssociations = findAssociations(environment, partition1);
      String[] partitionIds = new String[] {partition1.getId(), partition2.getId()};
      Arrays.sort(partitionIds);
      for (final Association association : existingAssociations) {
        String[] existingPartitionIds = new String[] {association.getPartition1().getId(), association.getPartition2().getId()}; 
        Arrays.sort(existingPartitionIds);
        
        if (Arrays.equals(existingPartitionIds, partitionIds)) {
          
          Associations associations = environment.getAssociations();
          ConfigSupport.apply(new SingleConfigCode<Associations>() {
            @Override
            public Object run(Associations writeableAssociations) throws TransactionFailure, PropertyVetoException {
              writeableAssociations.getAssociations().remove(association);
              return association;
            }
          }, associations);

          return association;

        }
      }
      throw new IllegalArgumentException("Partitions " + partition1.getId() + " and " + partition2.getId() + " are not associated."); 
    }

    public static Association removeAssociation(final Environment environment, final PartitionRef partition1, final PartitionRef partition2) throws TransactionFailure {
      Partition p1 = getPartitionById(environment, partition1.getId());
      Partition p2 = getPartitionById(environment, partition2.getId());
      return removeAssociation(environment, p1, p2);
    }

    public static Association removeAssociation(final Environment environment, final String partition1, final String partition2) throws TransactionFailure {
      Partition p1 = getPartitionById(environment, partition1);
      Partition p2 = getPartitionById(environment, partition2);
      return removeAssociation(environment, p1, p2);
    }

    public static List<Association> findAssociations(final Environment environment, Partition partition) {
      
      Associations associations = environment.getAssociations();
      List<Association> associationList = associations.getAssociations();
      List<Association> result = new ArrayList<Association>(associationList.size());
      String partitionId = partition.getId();
      for (Association association : associationList) {
        if (association.getPartition1().getId().equals(partitionId)
            || association.getPartition2().getId().equals(partitionId)) {
          result.add(association);
        }
      }
      return result;
    }

    public static List<Association> findAssociations(final Environment environment, PartitionRef partition) {
      Partition p = getPartitionById(environment, partition.getId());
      return findAssociations(environment, p);
    }

    public static List<Association> removeAssociations(final Environment environment, final Partition partition) throws TransactionFailure {
      final List<Association> existingAssociations = findAssociations(environment, partition);
      Associations associations = environment.getAssociations();
      ConfigSupport.apply(new SingleConfigCode<Associations>() {
        @Override
          public Object run(Associations writeableAssociations) throws TransactionFailure, PropertyVetoException {

            for (Association association : existingAssociations) {
              writeableAssociations.getAssociations().remove(association); 
            }

            return null;
          }
      }, associations);

      return existingAssociations;
    }

    public static List<Association> removeAssociations(final Environment environment, final PartitionRef partition) throws TransactionFailure {
      return removeAssociations(environment, partition.getId());
    }

    public static List<Association> removeAssociations(final Environment environment, final String partition) throws TransactionFailure {
      Partition p = getPartitionById(environment, partition);
      return removeAssociations(environment, p);
    }

    
  }
  */


}
