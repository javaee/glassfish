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
 * NopromptInput.java
 *
 * Created on November 19, 2007, 12:23 PM
 *
 */

package com.sun.enterprise.tools.upgrade.common;

import java.util.ArrayList;
import java.util.logging.*;

import com.sun.enterprise.tools.upgrade.logging.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.common.arguments.ARG_adminuser;
import com.sun.enterprise.tools.upgrade.common.arguments.ARG_adminpassword;
import com.sun.enterprise.tools.upgrade.common.arguments.ARG_masterpassword;
import com.sun.enterprise.tools.upgrade.common.arguments.ArgumentHandler;

/**
 * Utility to evaluate the CLI input arguments when the
 * user has indicated he is not to be prompted for input data.
 *
 * @author rebeccas
 */
public class NopromptInput implements InteractiveInput{
	private CommonInfoModel commonInfo = CommonInfoModel.getInstance();
	private StringManager sm;
	private Logger _log;
	
	/** Creates a new instance of NopromptInput */
	public NopromptInput() {
		sm = StringManager.getManager(NopromptInput.class);
		this._log = LogService.getLogger(LogService.UPGRADE_LOGGER);
	}

    public void processArguments(ArrayList<ArgumentHandler> aList){
		for(ArgumentHandler v: aList){
			if (v.isValidParameter()){
				v.exec();
			} else {
				if (v instanceof ARG_adminpassword || v instanceof ARG_masterpassword){
					//- don't print security info
					_log.severe(sm.getString("enterprise.tools.upgrade.cli._invalid_option_or_value",
						v.getCmd(), ""));
				}else {
					_log.severe(sm.getString("enterprise.tools.upgrade.cli._invalid_option_or_value",
						v.getCmd(), v.getRawParameter()));
				}
				commonInfo.recover();
				System.exit(1);
			}
		}

		if (!commonInfo.isUpgradeSupported()){
			commonInfo.recover();
			System.exit(1);
		}
	}
}
