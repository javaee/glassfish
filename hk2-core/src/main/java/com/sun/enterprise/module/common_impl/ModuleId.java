/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2015 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.module.common_impl;

import com.sun.enterprise.module.ModuleDefinition;

/**
 * Class representing the primary Key for a {@link com.sun.enterprise.module.Module}.
 * A module is identified by its name and version. This class
 * encapsulates both and implements hashCode and equals method
 * so that it can be used in Sets and Maps.
 *
 * @author Sahoo@Sun.COM
 */
public class ModuleId
{
    protected String name;
    protected String version;

    protected ModuleId() {}

    public ModuleId(String name)
    {
        this.name = name;
    }

    public ModuleId(String name, String version)
    {
        init(name, version);
    }

    public ModuleId(ModuleDefinition md)
    {
        init(md.getName(), md.getVersion());
    }

    protected void init(String name, String version)
    {
        this.name = name;
        this.version = version;
    }

    @Override
    public int hashCode()
    {
        return (name + version).hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        boolean result = false;
        if (obj instanceof ModuleId) {
            ModuleId other = ModuleId.class.cast(obj);
            result = (name == other.name);
            if (!result && (name!=null)) {
                result = name.equals(other.name);
            }
            if (result) {
                result = (version == other.version);
                if (!result && (version!=null)) {
                    result = version.equals(other.version);
                }
            }
        }
        return result;
    }

    @Override
    public String toString()
    {
        return name + ":" + version;
    }
}
