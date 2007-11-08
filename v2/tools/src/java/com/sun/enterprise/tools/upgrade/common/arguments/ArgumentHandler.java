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
import java.util.logging.*;
import com.sun.enterprise.tools.upgrade.common.CommonInfoModel;
import com.sun.enterprise.tools.upgrade.logging.*;
import com.sun.enterprise.tools.upgrade.common.UpgradeUtils;
import java.util.Map;
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
    protected Map interactiveMap;
    
    /** Creates a new instance of ArgumentHandler */
    public ArgumentHandler(ParsedArgument pa) {
        commonInfo = pa.getCommonInfo();
        parameters = pa.getParameters();
        sm = StringManager.getManager(LogService.UPGRADE_CLI_LOGGER);
        utils = UpgradeUtils.getUpgradeUtils(commonInfo);
        interactiveMap = pa.getInteractiveMap();
    }
    
    protected void helpUsage(){
        
        System.out.println(sm.getString("enterprise.tools.upgrade.cli.usage"));
        /**
         * CR 6568833 : For 9.1, the options used to pass passwords for certififcate
         * transfer have been removed since they are no longer required. So
         * use a single usage command common to all upgrade paths.
         */
        System.out.println(sm.getString("enterprise.tools.upgrade.cli.command_string"));
        System.out.println(sm.getString("enterprise.tools.upgrade.cli.usage_options_summary"));
        System.out.println();

        /* Comment out code since separate code usage command lines not needed for 9.1 
        if(commonInfo.isUpgradeJKStoNSS() || commonInfo.isUpgradeNSStoJKS()) {
            System.out.println(".... Upgrading JKS -> NSS or NSS -> JKS ");
            System.out.println(sm.getString("enterprise.tools.upgrade.cli.convert_certs"));
            System.out.println(sm.getString("enterprise.tools.upgrade.cli.usage_options_summary"));
            System.out.println();
            return;
        }
        
        if(commonInfo.isUpgradeNSStoNSS()) {
            System.out.println(".... Upgrading NSS -> NSS ");
            System.out.println(sm.getString("enterprise.tools.upgrade.cli.NSS_certs"));
            System.out.println(sm.getString("enterprise.tools.upgrade.cli.usage_options_summary"));
            System.out.println();
            return;
        }
        
        if(commonInfo.isUpgradeJKStoJKS()) {
            System.out.println(".... Upgrading JKS -> JKS ");
            System.out.println(sm.getString("enterprise.tools.upgrade.cli.JKS_certs"));
            System.out.println(sm.getString("enterprise.tools.upgrade.cli.usage_options_summary"));
            System.out.println();
            return;
        }

        System.out.println(".... Upgrading Convertng certs");
        System.out.println(sm.getString("enterprise.tools.upgrade.cli.convert_certs"));
        */
    }
    
    protected void helpUsage(String msg) {
        System.out.println(msg);
        System.out.println();
        helpUsage();
    }
}
