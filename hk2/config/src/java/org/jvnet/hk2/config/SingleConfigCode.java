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

package org.jvnet.hk2.config;

import org.jvnet.hk2.config.TransactionFailure;

import java.beans.PropertyVetoException;

/**
 * SimpleConfigCode is code snippet that can be used to apply some configuration 
 * changes to one configuration object.
 *
 * For example say, you need to modify the HttpListener config object with a new 
 * port number, you can do so by writing the following code snippet. 
 *
 *	{@code	new SingleConfigCode<HttpListener>() {
 *			public boolean run(HttpListener httpListener) throws PropertyVetoException {
 *				httpListener.setPort("8989");
 *				return true;
 *			}
 *		};
 *  }
 * This new SingleConfigCode can then be used with in the TransactionHelper utilities to 
 * run this code within a Transaction freeing the developer to know/care about Transaction
 * APIs and semantics.
 *
 * @author Jerome Dochez
 */
public interface SingleConfigCode<T extends ConfigBeanProxy> {

	/**
	 * Runs the following command passing the configration object. The code will be run
	 * within a transaction, returning true will commit the transaction, false will abort
	 * it.
	 * 
	 * @param param is the configuration object protected by the transaction
     * @return true if the changes on param should be commited or false for abort.
     * @throws PropertyVetoException if the changes cannot be applied
     * to the configuration
	 */
    public boolean run(T param) throws PropertyVetoException, TransactionFailure;
}
