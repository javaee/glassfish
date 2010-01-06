/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2010 Sun Microsystems, Inc. All rights reserved.
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

/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdFactoryIniter.java,v 1.2 2003/11/12 00:59:45 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/12 00:59:45 $
 */
package com.sun.cli.jcmd.framework;

import java.util.ArrayList;
import java.util.List;

/**
Helper object for initializing a CmdFactory using a CmdSource, other CmdFactory, or a
Cmd class.
 */
public final class CmdFactoryIniter
{

    final CmdFactory mFactory;


    /**
    Create a new initer referencing the factory

    @param factory	the CmdFactory to use
     */
    public CmdFactoryIniter(CmdFactory factory)
            throws Exception
    {
        mFactory = factory;
    }


    /**
    Create a new initer referencing the factory.  Add the commands in 'cmds'
    to the factory.

    @param factory	the CmdFactory to use
    @param cmds		the CmdSource to initialize the factory with
     */
    public CmdFactoryIniter(CmdFactory factory, CmdSource cmds)
            throws Exception
    {
        mFactory = factory;
        addMappings(cmds);
    }


    /**
    Add mappings for all Cmd objects found with the CmdSource.

    @param cmds		the CmdSource supplying the Cmd classes
     */
    public List<String[]> addMappings(CmdSource cmds)
            throws Exception
    {
        final List<Class<? extends Cmd>> commandClasses = cmds.getClasses();
        final List<String[]> names = new ArrayList<String[]>();

        for (int i = 0; i < commandClasses.size(); ++i)
        {
            names.add( addMappingsForClass(commandClasses.get(i)) );
        }

        return (names);
    }


    /**
    Add mappings for a specific Cmd class.

    @param theClass		Class object for a Cmd class
     */
    public String[] addMappingsForClass(Class<? extends Cmd> theClass)
            throws Exception
    {
        final String[] names = CmdBase.getCmdNames(theClass);

        for (int i = 0; i < names.length; ++i)
        {
            mFactory.addCmdMapping(names[i], theClass);
        }

        return (names);
    }


    /**
    Remove mappings for a specific Cmd class.

    @param theClass		Class object for a Cmd class
     */
    public void removeMappingsForClass(Class<? extends Cmd> theClass)
            throws Exception
    {
        final String[] names = CmdBase.getCmdNames(theClass);

        for (int i = 0; i < names.length; ++i)
        {
            mFactory.removeCmdMapping(names[i]);
        }
    }


    /**
    Remove mappings for a Cmd classes provided by the CmdSource

    @param cmds		the CmdSource supplying the Cmd classes
     */
    public void removeMappings(CmdSource cmds)
            throws Exception
    {
        for (final Class<? extends Cmd> c : cmds.getClasses())
        {
            removeMappingsForClass(c);
        }
    }
}

