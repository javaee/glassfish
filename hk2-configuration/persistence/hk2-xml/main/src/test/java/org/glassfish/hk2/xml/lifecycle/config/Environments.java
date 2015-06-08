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

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface Environments {
  @XmlElement(name="environment")
  void setEnvironments(List<Environment> environments);
  List<Environment> getEnvironments();

  /*
  @DuckTyped
  Environment getOrCreateEnvironment(String name);

  @DuckTyped
  Environment createEnvironment(String name) throws TransactionFailure;

  @DuckTyped
  Environment getEnvironmentByName(String name);

  @DuckTyped
  Environment deleteEnvironment(Environment environment);

  @DuckTyped
  PartitionRef getPartitionRef(Partition partition);

  @DuckTyped
  Environment getReferencedEnvironment(Partition partition);

  class Duck  {
    public static Environment createEnvironment(final Environments environments, final String name) throws TransactionFailure {
      ConfigSupport.apply(new SingleConfigCode<Environments>() {
        @Override
        public Object run(Environments writeableEnvironments) throws TransactionFailure, PropertyVetoException {
          Environment environment = writeableEnvironments.createChild(Environment.class);
          environment.setName(name);
          writeableEnvironments.getEnvironments().add(environment);
          Associations associations = environment.createChild(Associations.class);
          environment.setAssociations(associations);
          return environment;
        }
      }, environments);

      // read-only view
      return getEnvironmentByName(environments, name);
    }

    public static Environment getEnvironmentByName(final Environments environments, final String name) {
      List<Environment> environmentConfigsList = environments.getEnvironments();
      for (Environment environment : environmentConfigsList) {
        if (name.equals(environment.getName())) {
          return environment;
        }
      }
      return null;
    }

    public static Environment getOrCreateEnvironment(final Environments environments, String name) throws TransactionFailure {
      Environment environment = getEnvironmentByName(environments, name);
      if (environment == null) {
        @SuppressWarnings("unused")
        Environment writeableEnvironment = createEnvironment(environments, name);
          return getEnvironmentByName(environments, name);
      } else {
          return environment;
      }
    }

    public static Environment deleteEnvironment(final Environments environments,
        final Environment environment) throws TransactionFailure {
      return (Environment) ConfigSupport.apply(new SingleConfigCode<Environments>() {

        @Override
        public Object run(Environments writeableEnvironments)
            throws TransactionFailure {
          writeableEnvironments.getEnvironments().remove(environment);
          return environment; 
        }

      }, environments);
    
    }

    public static PartitionRef getPartitionRef(final Environments environments,
        final Partition partition) throws TransactionFailure {
      List<Environment> environmentConfigs = environments.getEnvironments();
      for (Environment environment : environmentConfigs) {
    	PartitionRef partitionRef = environment.getPartitionRefById(partition.getId());
    	if (partitionRef != null) {
    		return partitionRef;
    	}
      }
      return null;
    }

    public static Environment getReferencedEnvironment(final Environments environments,
        final Partition partition) throws TransactionFailure {
      PartitionRef partitionRef = environments.getPartitionRef(partition);
      if (partitionRef != null) {
        return partitionRef.getParent(Environment.class);
      } else {
        return null;
      }
    }
  }
  */
}
