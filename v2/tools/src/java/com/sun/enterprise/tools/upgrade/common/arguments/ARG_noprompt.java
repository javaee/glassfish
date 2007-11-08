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

import com.sun.enterprise.tools.upgrade.common.ArgsParser;
import com.sun.enterprise.tools.upgrade.common.PasswordVerifier;
import com.sun.enterprise.tools.upgrade.common.UpgradeUtils;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Hans Hrasna
 */
public class ARG_noprompt extends ArgumentHandler {
    private String defaultAdminUser = "admin";
    private String defaultAdminPassword = "adminadmin";
    private String defaultMasterPassword = "changeit";
    
    /** Creates a new instance of ARG_noprompt */
    public ARG_noprompt(ParsedArgument pa) {
        super(pa);
		commonInfo.setNoprompt(true);
		
        //Silent upgrade support with default values
        if (commonInfo.getAdminUserName() == null){
            commonInfo.setAdminUserName(defaultAdminUser);
            interactiveMap.put(ArgsParser.ADMINUSER,defaultAdminUser);
        }
        if (commonInfo.getAdminPassword() == null){
            commonInfo.setAdminPassword(defaultAdminPassword);
            interactiveMap.put(ArgsParser.ADMINPASSWORD,defaultAdminPassword);
        }
        if (commonInfo.getMasterPassword() == null) {
            commonInfo.setMasterPassword(defaultMasterPassword);
        }
        
        // set up silent certificate upgrade
        commonInfo.setCertificateConversionFlag(true);
        List domainList = commonInfo.getDomainList();
        Iterator it = domainList.iterator();
        while (it.hasNext()){
            //attempt certificate migration for each domain
            String domainName = (String)it.next();
            commonInfo.setCurrentDomain(domainName);
            //commonInfo.setJksCAKeystorePassword(password);
            String password = UpgradeUtils.getUpgradeUtils(commonInfo).getJvmOptionValueFromSourceConfig("javax.net.ssl.keyStorePassword");
            if(password == null) {
                password = commonInfo.getMasterPassword();
            }
            commonInfo.setJksKeystorePassword(password);
            String trustPassword = UpgradeUtils.getUpgradeUtils(commonInfo).getJvmOptionValueFromSourceConfig("javax.net.ssl.trustStorePassword");
            if(trustPassword == null) {
                trustPassword = commonInfo.getMasterPassword();
            }
            commonInfo.setJksCAKeystorePassword(trustPassword);
            commonInfo.addDomainOptionName(domainName);
            interactiveMap.put(ArgsParser.DOMAIN + "-" + commonInfo.getCurrentDomain(), domainName);
            interactiveMap.put(ArgsParser.JKSPWD + "-" + domainName, password);
            interactiveMap.put(ArgsParser.CAPWD + "-" + domainName, password);
            interactiveMap.put(ArgsParser.NSSPWD + "-" + domainName, password);
            interactiveMap.put(ArgsParser.TARGETNSSPWD + "-" + domainName, password);
            commonInfo.setCertDbPassword(commonInfo.getCurrentDomain(), password);
            String jksPath=commonInfo.getSourceJKSKeyStorePath();
            if(!PasswordVerifier.verifyKeystorePassword(jksPath,password )) {
                commonInfo.recover();
                _logger.severe(sm.getString("enterprise.tools.upgrade.cli.Invalid_jks_keypair_password"));
                System.exit(1);
            }
            String trustJksPath = commonInfo.getSourceTrustedJKSKeyStorePath();
            if(!PasswordVerifier.verifyKeystorePassword(trustJksPath,trustPassword)) {
                commonInfo.recover();
                _logger.severe(sm.getString("enterprise.tools.upgrade.cli.Invalid_jks_CA_password"));
                System.exit(1);
            }
        }
    }
    
}
