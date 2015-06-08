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

//import javax.validation.Payload;
//import javax.validation.constraints.NotNull;

// import com.oracle.weblogic.lifecycle.config.validators.ReferenceConstraint;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.glassfish.hk2.xml.api.annotations.XmlIdentifier;

import java.beans.PropertyVetoException;

// @ReferenceConstraint(skipDuringCreation=false, payload=Service.class)
public interface Service extends Payload, Auditable {

  @XmlAttribute(required=true /*, key=true */)
  // @NotNull
  String getId();
  void setId(String id) throws PropertyVetoException;

  @XmlAttribute(required=true /*, key=false */)
  @XmlIdentifier
  // @NotNull
  String getName();
  void setName(String value) throws PropertyVetoException;

  @XmlAttribute
  // @ReferenceConstraint.RemoteKey(message="{resourceref.invalid.configref}", type=Environment.class)
  String getEnvironmentRef();
  void setEnvironmentRef(String envName);
  
  @XmlAttribute
  String getServiceType();
  void setServiceType(String serviceType);

  @XmlAttribute
  String getIdentityDomain();
  void setIdentityDomain(String identityDomain);

  @XmlElement
  PDB getPdb();
  void setPdb(PDB pdb);
  
  /*
  @DuckTyped
  PDB createPDB(String name, String id, String status);
   
  @DuckTyped
  PDB deletePDB(PDB pdb);
  
  @DuckTyped
  PDB deletePDB(String pdbName);

  @DuckTyped
  Resources createResourcesIfNotFound();

  @Element
  Resources getResources();
  void setResources(Resources resources);

  @DuckTyped
  Environment getEnvironment();

  @DuckTyped
  Tenant getTenant();

  class Duck {

    public static PDB createPDB(final Service service, final String name, final String id, final String status)
        throws TransactionFailure {
      ConfigSupport.apply(new SingleConfigCode<Service>() {
        @Override
        public Object run(Service writeableService) throws TransactionFailure, PropertyVetoException {
          PDB pdb = writeableService.createChild(PDB.class);
          pdb.setName(name);
          pdb.setId(id);
          pdb.setPdbStatus(status);
          writeableService.setPdb(pdb);
          return pdb;
        }
      }, service);

      // read-only view
      return service.getPdb();
    }

    public static PDB deletePDB(final Service service, final PDB pdb) throws TransactionFailure {
      return (PDB) ConfigSupport.apply(new SingleConfigCode<Service>() {
        @Override
        public Object run(Service writeableService) throws TransactionFailure {
          writeableService.setPdb(null);
          return pdb;
        }
      }, service);
    }

    public static Resources createResourcesIfNotFound(final Service service) throws TransactionFailure{


        return (Resources)ConfigSupport.apply(new SingleConfigCode<Service>() {
          @Override
          public Object run(Service writeableService) throws TransactionFailure, PropertyVetoException {
              if(writeableService.getResources() == null){
                  Resources resources = writeableService.createChild(Resources.class);
                  writeableService.setResources(resources);
              }
            return writeableService.getResources();
          }
        }, service);

    }

    public static Environment getEnvironment(final Service service) throws TransactionFailure {
      LifecycleConfigBean bean = (LifecycleConfigBean) Dom.unwrap(service);
      ServiceLocator serviceLocator = bean.getHabitat();
      return serviceLocator.getService(Environment.class, service.getEnvironmentRef());
    }

    public static Tenant getTenant(final Service service) throws TransactionFailure {
      return service.getParent(Tenant.class);
    }
  }
  */
}
