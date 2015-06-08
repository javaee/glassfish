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

import java.beans.PropertyVetoException;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface Tenants {
  @XmlElement(name="tenant")
  List<Tenant> getTenants();
  void setTenants(List<Tenant> tenants);
  
  /*
  @DuckTyped
  Tenant createTenant(String name, String id, String topLevelDir);
  
  @DuckTyped
  Tenant createTenant(String name, String id);
   
  @DuckTyped
  Tenant deleteTenant(Tenant tenant);
  
  @DuckTyped
  Tenant getTenant(String name, String id);
  
  @DuckTyped
  Tenant getTenant(String id);
  
  @DuckTyped
  Tenant getTenantById(String id);

  @DuckTyped
  Tenant getTenantByName(String name);

  @DuckTyped
  Tenant getTenantForPartition(String partitionName, String partitionId);
  
  @DuckTyped
  Tenant getTenantForPDB(String pdbName, String pdbId);

  @DuckTyped
  Service getServiceById(String serviceId);

  @NotNull
  @Element
  Resources getResources();
  void setResources(Resources resources);

  class Duck  {
    public static Tenant createTenant(final Tenants tenantMapping, 
            final String name, final String id) 
            throws TransactionFailure {
        return createTenant(tenantMapping, name, id, null);
    }  
      
    public static Tenant createTenant(final Tenants tenantMapping, 
            final String name, final String id, final String topLevelDir) 
            throws TransactionFailure {
      return (Tenant) ConfigSupport.apply(new SingleConfigCode<Tenants>() {
        @Override
        public Object run(Tenants writeableTenantMapping) throws TransactionFailure, PropertyVetoException {
          Tenant tenant = writeableTenantMapping.createChild(Tenant.class);
          tenant.setName(name);
          tenant.setId(id);
          if (topLevelDir != null) tenant.setTopLevelDir(topLevelDir);
          writeableTenantMapping.getTenants().add(tenant);
          return tenant;
        }
      }, tenantMapping);
    }
    
    public static Tenant deleteTenant(final Tenants tenantMapping,
        final Tenant tenant) throws TransactionFailure {
      return (Tenant) ConfigSupport.apply(new SingleConfigCode<Tenants>() {

        @Override
        public Object run(Tenants writeableTenantMapping)
            throws TransactionFailure {
          writeableTenantMapping.getTenants().remove(tenant);
          return tenant; 
        }

      }, tenantMapping);
    }
    
    public static Tenant getTenant(final Tenants tenantMapping,
              final String name, final String id) {
      Tenant tenant = null;
      for (Tenant t : tenantMapping.getTenants()) {
        if (t.getName().equals(name) && t.getId().equals(id)) {
          tenant = t;
          break;
        }
      }
      return tenant;
    }
    
    public static Tenant getTenant(final Tenants tenantMapping,
              final String id) {
      Tenant tenant = null;
      for (Tenant t : tenantMapping.getTenants()) {
        if (t.getId().equals(id)) {
          tenant = t;
          break;
        }
      }
      return tenant;
    }
    
    public static Tenant getTenantByName(final Tenants tenantMapping,
              final String name) {
      Tenant tenant = null;
      for (Tenant t : tenantMapping.getTenants()) {
        if (t.getName().equals(name)) {
          tenant = t;
          break;
        }
      }
      return tenant;
    }
    
    public static Tenant getTenantForPartition(final Tenants tenantMapping,
        final String partitionId) {
      long inputPartitionId = Long.parseLong(partitionId);
      
      for (Tenant tenant : tenantMapping.getTenants()) {
        LifecycleConfig lifecycleConfig = tenantMapping.getParent(LifecycleConfig.class);
        List<Runtime> runtimeConfigs = lifecycleConfig.getRuntimes().getRuntimes();
        for (Runtime runtimeConfig : runtimeConfigs) {
          for (Partition partition : runtimeConfig.getPartitions()) {
            long candidateId = Long.parseLong(partition.getId());
            if (candidateId == inputPartitionId ) {
              return tenant;
            }
          }
        }
      }
      return null;
    }
    
    public static Tenant getTenantForPDB(final Tenants tenantMapping, 
        final String pdbId) {
      for (Tenant tenant : tenantMapping.getTenants()) {
        Service service = tenant.getServiceByPDBId(pdbId);
        if (service != null) {
          return tenant;
        }
      }
      return null;
    }

    public static Service getServiceById(final Tenants tenants, final String id) {
      for (Tenant tenant : tenants.getTenants()) {
        Service service = tenant.getServiceById(id);
        if (service != null) {
            return service;
        }
      }
      return null;
    }
  }
  */
}
