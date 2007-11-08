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

package com.sun.enterprise.admin.server.core.mbean.config;

//JMX imports
import javax.management.*;

//Config imports
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;


//Admin imports
import com.sun.enterprise.admin.common.exception.MBeanConfigException;
import com.sun.enterprise.admin.common.constant.ConfigAttributeName;

/**
    This Config MBean represents a Ssl element.
    This class is generic base for different Ssl elemenets (from OrbListener or ConnectionGroup)
    It extends ConfigMBeanBase class which provides get/set attribute(s) and getMBeanInfo services according to text descriptions.
*/
public class SslBase extends ConfigMBeanBase implements ConfigAttributeName.Ssl
{
    /**
     * MAPLIST array defines mapping between "external" name and its location in XML relatively base node
     */
    static final String[][] MAPLIST  =
    {
        //----- SSL --------
        {kCertNickname       ,  ATTRIBUTE + ServerTags.CERT_NICKNAME},
        {kSsl2Enabled        ,  ATTRIBUTE + ServerTags.SSL2_ENABLED},
        {kSsl2Ciphers        ,  ATTRIBUTE + ServerTags.SSL2_CIPHERS},
        {kSsl3Enabled        ,  ATTRIBUTE + ServerTags.SSL3_ENABLED},
        {kSsl3TlsCiphers     ,  ATTRIBUTE + ServerTags.SSL3_TLS_CIPHERS},
        {kTlsEnabled         ,  ATTRIBUTE + ServerTags.TLS_ENABLED},
        {kTlsRollbackEnabled ,  ATTRIBUTE + ServerTags.TLS_ROLLBACK_ENABLED},
        {kClientAuthEnabled  ,  ATTRIBUTE + ServerTags.CLIENT_AUTH_ENABLED},
    };
    /** 
     * ATTRIBUTES array specifies attributes descriptions in format defined for MBeanEasyConfig
     */
    static final String[]   ATTRIBUTES  =
    {
        kCertNickname       +", String,  RW" ,
        kSsl2Enabled        +", Boolean, RW" ,
        kSsl2Ciphers        +", String,  RW" ,
        kSsl3Enabled        +", Boolean, RW" ,
        kSsl3TlsCiphers     +", String,  RW" ,
        kTlsEnabled         +", Boolean, RW" ,
        kTlsRollbackEnabled +", Boolean, RW" ,
        kClientAuthEnabled  +", Boolean, RW" ,
    };
    /** 
     * OPERATIONS array specifies operations descriptions in format defined for MBeanEasyConfig
     */
    private static final String[]   OPERATIONS  = null;
    
    /**
        Default constructor sets MBean description tables
    */
    public SslBase() throws MBeanConfigException
    {
        this.setDescriptions(MAPLIST, ATTRIBUTES, OPERATIONS);
    }

}