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
package org.glassfish.vmcluster.spi;

import java.util.*;

/**
 * Virtual machine procurement order.
 * @author Jerome Dochez
 */
public class VMOrder {

    final int number;
    final List<Group> groups = new ArrayList<Group>();
    final List<VirtualMachine> noColocationList = new ArrayList<VirtualMachine>();

    public VMOrder(int number) {
        this.number = number;
    }
    /**
     *
     * @return the desired number of machines
     */
    public int number() {
        return number;
    }

    /**
     * Specifies the group in which the number of virtual machines should be allocated.
     * If no group is speficied, it's left to the Infrastructure Management Service to
     * decide in which groups those Virtual Machines will be allocated.
     *
     * @param groups desired group instance
     * @return itself
     */
    public VMOrder in(Group... groups) {
        this.groups.addAll(Arrays.asList(groups));
        return this;
    }

    /**
     * Specifies the virtual machines that should not be co-located on the same hardware
     * with the new allocated virtual machines. This is particularly useful when willing
     * to allocate replication instances for existing virtual machines so they do not end
     * up being running on the same hardware resource.
     *
     * @param vms list of virtual machines to not co-locate with.
     * @return itself.
     */
    public VMOrder noColocationWith(VirtualMachine... vms) {
        this.noColocationList.addAll(Arrays.asList(vms));
        return this;
    }

    public Collection<Group> affinities() {
        return Collections.unmodifiableList(groups);
    }

    public List<VirtualMachine> separateFrom() {
        return Collections.unmodifiableList(noColocationList);
    }
}
