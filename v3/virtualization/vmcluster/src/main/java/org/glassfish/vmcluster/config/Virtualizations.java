/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.vmcluster.config;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.DomainExtension;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.config.support.*;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.*;

import java.beans.PropertyVetoException;
import java.util.List;

/**
 * Top level configuration for all virtualization related config. For now, a bit of
 * a mess, will need to review this past prototype stage.
 *
 * @author Jerome Dochez
 */
@Configured
public interface Virtualizations extends DomainExtension {

    @Element("virtualization")
    List<Virtualization> getVirtualizations();

    @Element("emulator")
    @Create(value = "create-emulator", resolver = VirtResolver.class, i18n = @I18n("org.glassfish.vmcluster.create-emulator"))
    @Listing(value = "list-emulators", resolver = VirtResolver.class, i18n = @I18n("org.glassfish.vmcluster.list-emulators"))
    @Delete(value="delete-emulator", resolver= TypeAndNameResolver.class, i18n = @I18n("org.glassfish.vmcluster.delete-emulator"))
    List<Emulator> getEmulators();


    @Element("group-managers")
    @Create(value = "create-group-manager", resolver = VirtResolver.class, i18n = @I18n("org.glassfish.vmcluster.create-group-manager"))
    @Listing(value = "list-group-managers", resolver = VirtResolver.class, i18n = @I18n("org.glassfish.vmcluster.list-group-managers"))
    @Delete(value="delete-group-manager", resolver= TypeAndNameResolver.class, i18n = @I18n("org.glassfish.vmcluster.delete-group-manager"))
    List<GroupManager> getGroupManagers();

    /**
     * This really should not be here mixing clients and providers information , but for prototyping
     * it's fine.
     */
    @Element("group-providers")
    @Create(value = "create-virt-group", resolver = VirtResolver.class, decorator = GroupDecorator.class, i18n = @I18n("org.glassfish.vmcluster.create-virt-group"))
    @Listing(value = "list-virt-groups", resolver = VirtResolver.class, i18n = @I18n("org.glassfish.vmcluster.list-virt-groups"))
    @Delete(value="delete-virt-group", resolver= TypeAndNameResolver.class, i18n = @I18n("org.glassfish.vmcluster.list-virt-group"))
    List<GroupConfig> getGroupConfigs();

    @Attribute(defaultValue = "${com.sun.aas.instanceRoot}/virt/disks")
    String getDisksLocation();
    public void setDisksLocation();

    @Attribute(defaultValue = "${com.sun.aas.instanceRoot}/virt/templates")
    String getTemplatesLocation();
    public void setTemplatesLocation();

    /**
     * Returns a virtualization configuration using the virtualization name.
     * @param name the virtualization configuration name
     * @return  the virtualization configuration if found or null;
     */
    @DuckTyped
    Virtualization byName(String name);

    /**
     * Returns a group configuration using a group name
     * @param name the group name
     * @return group configuration or null if not found
     */
    @DuckTyped
    GroupConfig groupConfigByName(String name);

    /**
     * Returns the emulator configuration using an emulator name
     *
     * @param name  the emulator name
     * @return  the emulator configuration or null if not found
     */
    @DuckTyped
    Emulator emulatorByName(String name);

    public class Duck {

        public static Virtualization byName(Virtualizations self, String name) {
            for (Virtualization v : self.getVirtualizations()) {
                if (v.getName().equals(name)) {
                    return v;
                }
            }
            return null;
        }

        public static GroupConfig groupConfigByName(Virtualizations self, String name) {
            for (GroupConfig groupConfig : self.getGroupConfigs()) {
                if (groupConfig.getName().equals(name)) {
                    return groupConfig;
                }
            }
            return null;
        }

        public static Emulator emulatorByName(Virtualizations self, String name) {
            for (Emulator emulator : self.getEmulators()) {
                if (emulator.getName().equals(name)) {
                    return emulator;
                }
            }
            return null;
        }
    }

    @Service
    public class VirtResolver implements CrudResolver {
        @Inject
        Domain domain;

        @Inject(optional = true)
        Virtualizations virt = null;

        @Override
        public <T extends ConfigBeanProxy> T resolve(AdminCommandContext context, Class<T> type)  {
            if (virt!=null) return (T) virt;
            try {
                virt = (Virtualizations) ConfigSupport.apply(new SingleConfigCode<Domain>() {
                    @Override
                    public Object run(Domain wDomain) throws PropertyVetoException, TransactionFailure {
                        Virtualizations v = wDomain.createChild(Virtualizations.class);
                        wDomain.getExtensions().add(v);
                        return v;
                    }
                }, domain);
            } catch (TransactionFailure t)  {
                throw new RuntimeException(t);
            }
            return (T) virt;
        }
    }

    // ToDo : I should not have to do this, the CRUD framework should do it for me since VirtUser is annotated with @NotNull
    @Service
    public class GroupDecorator implements CreationDecorator<GroupConfig> {

        @Param(name="virtName")
        String virtName;

        @Inject
        Virtualizations virts;

        @Override
        public void decorate(AdminCommandContext context, GroupConfig instance) throws TransactionFailure, PropertyVetoException {
            VirtUser user = instance.createChild(VirtUser.class);
            instance.setUser(user);
            Virtualization virt = virts.byName(virtName);
            if (virt==null) {
                throw new TransactionFailure("Cannot find virtualization configuration for " + virtName);
            }
            instance.setVirtualization(virt);
        }
    }
}
