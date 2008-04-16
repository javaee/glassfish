/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * $Id: ICommandEnvironment.java,v 1.3 2005/12/25 03:47:00 tcfujii Exp $
 */

package com.sun.enterprise.cli.framework;

import java.util.HashMap;

/**
	All the <strong> Subcommands </strong> operate in an Environment which
	itself is set by some other Subcommands. This way, some Subcommands
	<strong> create </strong> an Environment and others <strong> use this
	environment. This interface represents such an Environment that provides
	data required by SubCommands to execute successfully. Any Subcommand can
	override the data set in an Environment (in which it is executed)
	by parsing the arguments supplied on its own command line. Potentially
	there can be multiple Environments. An Environment consists of a
	variable set of Named Options. The Subcommands that create Environment,
	modify this set whenever they are executed. Thus any Option in an
	Environment represents an Environment Setting.
 */
public interface ICommandEnvironment
{
    /**
       Adds Environment.
       Environment have unique names. Successive calls to this method
       with same name would result in replacement of the environment.
    */
    public void setEnvironment(String name, String value);


    /**
       Removes an environment. Does nothing if environment does not exist.
       returns null if environment could not be removed.
    */
    public Object removeEnvironment( String name );

    /**
	Returns an iterator over collection of Option objects.
    */
    public HashMap getEnvironments();

    /**
     *  returns the envrionment value by the given key
     */
    public Object getEnvironmentValue(String key);


    /**
	Returns a string format of the list of options in the environment.
    */
    public String toString();

    /**
	Returns the number of environments
    */
    public int getNumEnvironments();

}
