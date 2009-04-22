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

package com.sun.enterprise.tools.upgrade.common.arguments;

import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.*;

import com.sun.enterprise.tools.upgrade.common.CommonInfoModel;
import com.sun.enterprise.tools.upgrade.logging.*;
import com.sun.enterprise.tools.upgrade.common.UpgradeUtils;
import com.sun.enterprise.util.i18n.StringManager;


/**
 *
 * @author Hans Hrasna
 */
public abstract class ArgumentHandler {
    protected Logger _logger = LogService.getLogger(LogService.UPGRADE_LOGGER);
    protected CommonInfoModel commonInfo;
    protected Vector parameters;
    protected StringManager sm;
    protected UpgradeUtils utils;
	boolean _isRequiresParameter = true;
    
    /** Creates a new instance of ArgumentHandler */
	String cmd = null;
	String rawParameters = null;
	ArrayList<String> paramList = new ArrayList<String>();
	boolean _isValidParameter = false;
	
	public ArgumentHandler() {
		sm = StringManager.getManager(ArgumentHandler.class);
		this.commonInfo = CommonInfoModel.getInstance();
        utils = UpgradeUtils.getUpgradeUtils(commonInfo);
	}
	
	public void setCmd(String c){
		cmd = c;
	}
	public String getCmd(){
		return cmd;
	}
	
	public void setRawParameters(String p){
		rawParameters = p;
		//- each cmd may need its own param parsing rules.
		paramList.clear();
		paramList.add(rawParameters);
	}
	
	public String getRawParameter(){
		return rawParameters;
	}
	public ArrayList<String> getParameters(){
		return paramList;
	}
	
	//- process input parameters.
	public void exec(){}
	
	//- One option generates child options to process
	public List<ArgumentHandler> getChildren(){
		return new Vector<ArgumentHandler>();
	}
	
	/**
	 *  Indicate if this cmd option requires a parameter.
	 */
	public boolean isRequiresParameter(){
		//- some cmds may need to override this.
		return _isRequiresParameter;
	}
	
	/**
	 * Verify that input param value is valid for further processing
	 */
	public boolean isValidParameter(){
		//- some cmds may need to override this.
		return _isValidParameter;
	}
}
