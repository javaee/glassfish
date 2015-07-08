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

// import javax.validation.Payload;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface Tenant extends Named, Payload, Auditable {

  @XmlAttribute
  String getId();
  void setId(String id);
  
  @XmlAttribute
  String getTopLevelDir();
  void setTopLevelDir(String topLevelDir);
  
  @XmlElement(name="service")
  List<Service> getServices();
  void setServices(List<Service> services);
  Service lookupService(String name);

  /*
  @DuckTyped
  void addTopLevelDir(String topLevelDir);
  
  @DuckTyped
  @Deprecated
  // FIXME: remove, see OWLS-9470
  Service createService(String name, String type, String envName);

  @DuckTyped
  Service createService(String id, String name, String type, String envName);

  @DuckTyped
  Service createService(String id, String name, String type, String envName, String identityDomain);

  @DuckTyped
  Service getServiceByName(String serviceName);

  @DuckTyped
  Service getServiceById(String id);

  @DuckTyped
  Service getServiceByPDBId(String pdbId);

  @DuckTyped
  Service deleteService(Service service);
  
  @DuckTyped
  Service deleteService(String serviceName);
  
  class Duck  {
    public static void addTopLevelDir(final Tenant tenant, final String topLevelDir) 
            throws TransactionFailure {
      ConfigSupport.apply(new SingleConfigCode<Tenant>() {
        @Override
        public Object run(Tenant writeableTenant) throws TransactionFailure, PropertyVetoException {
          writeableTenant.setTopLevelDir(topLevelDir);
          return null;
        }
      }, tenant);
    }  
    
    public static Service createService(final Tenant tenant, final String name, final String type, final String envName) 
        throws TransactionFailure {
      String id = tenant.getName() + tenant.getId();
      return createService(tenant, id, name, type, envName);
      
    }
    public static Service createService(final Tenant tenant, final String id, final String name, final String type, final String envName) 
            throws TransactionFailure {
      return (Service) ConfigSupport.apply(new SingleConfigCode<Tenant>() {
        @Override
        public Object run(Tenant writeableTenant) throws TransactionFailure, PropertyVetoException {
          Service service = writeableTenant.createChild(Service.class);
          service.setId(id);
          service.setName(name);
          service.setEnvironmentRef(envName);
          service.setServiceType(type);
          writeableTenant.getServices().add(service);
          return service;
        }
      }, tenant);
    }

  public static Service createService(final Tenant tenant, final String id, final String name,
                                      final String type, final String identityDomain, final String envName)
          throws TransactionFailure {
      return (Service) ConfigSupport.apply(new SingleConfigCode<Tenant>() {
          @Override
          public Object run(Tenant writeableTenant) throws TransactionFailure, PropertyVetoException {
              Service service = writeableTenant.createChild(Service.class);
              service.setId(id);
              service.setName(name);
              service.setEnvironmentRef(envName);
              service.setServiceType(type);
              service.setIdentityDomain(identityDomain);
              writeableTenant.getServices().add(service);
              return service;
          }
      }, tenant);
  }


      public static Service getServiceByName(final Tenant tenant, final String name) {
      List<Service> services = tenant.getServices();
      for (Service service : services) {
        if (name.equals(service.getName())) {
          return service;
        }
      }
      return null;
    }

    public static Service getServiceById(final Tenant tenant, final String id) {
      List<Service> services = tenant.getServices();
      for (Service service : services) {
        if (id.equals(service.getId())) {
          return service;
        }
      }
      return null;
    }

    public static Service getServiceByPDBId(final Tenant tenant, final String pdbId) {
      List<Service> services = tenant.getServices();
      for (Service service : services) {
        if (pdbId.equals(service.getPdb().getId())) {
          return service;
        }
      }
      return null;
    }

    public static Service deleteService(final Tenant tenant, final Service service) throws TransactionFailure {
      return (Service) ConfigSupport.apply(new SingleConfigCode<Tenant>() {
        @Override
        public Object run(Tenant writeableTenant) throws TransactionFailure {
          writeableTenant.getServices().remove(service);
          return service; 
        }
      }, tenant);    
    }
    
    public static Service deleteService(final Tenant tenant, final String serviceName) throws TransactionFailure {
      return (Service) ConfigSupport.apply(new SingleConfigCode<Tenant>() {
        @Override
        public Object run(Tenant writeableTenant) throws TransactionFailure {
          Service serviceRemove = null;
          if (serviceName != null) {
            for (Service service : writeableTenant.getServices()) {
              if (service.getName().equals(serviceName)) {
                serviceRemove = service;
                break;
              }
            }
            if (serviceRemove != null) {
              writeableTenant.getServices().remove(serviceRemove);
            }
          }
          return serviceRemove; 
        }
      }, tenant);    
    }
  }
  */
}
