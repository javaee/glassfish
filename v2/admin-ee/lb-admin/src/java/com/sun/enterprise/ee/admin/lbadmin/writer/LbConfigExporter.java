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
package com.sun.enterprise.ee.admin.lbadmin.writer;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.io.IOException;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.LbConfig;
import com.sun.enterprise.config.serverbeans.Domain;

import com.sun.enterprise.ee.admin.lbadmin.reader.api.LoadbalancerReader;
import com.sun.enterprise.ee.admin.lbadmin.reader.impl.LoadbalancerReaderImpl;
import com.sun.enterprise.ee.admin.lbadmin.transform.LoadbalancerVisitor;
import com.sun.enterprise.ee.admin.lbadmin.beans.Loadbalancer;

import org.netbeans.modules.schema2beans.Schema2BeansException;

import com.sun.enterprise.util.i18n.StringManager;

/**
 * Export support class
 * 
 * @author Harsha R A
 */
public class LbConfigExporter {
  
    /**
     * exports the loadbalancer.xml from the config to the outputstream provided
     * @param ctx ConfigContext
     * @param lbConfigName name of lb-config 
     * @param out OutputStream into which the loadbalancer.xml is written
     */
    public static LoadbalancerReader getLbReader(ConfigContext ctx,
        String lbConfigName) throws ConfigException, Schema2BeansException{

        // reads the load balancer related data
        LbConfig lbConfig = ((Domain)ctx.getRootConfigBean()).getLbConfigs().
                                getLbConfigByName(lbConfigName);
        return new LoadbalancerReaderImpl(ctx,lbConfig);

    }

    /**
     * exports the loadbalancer.xml from the config to the outputstream provided
     * @param ctx ConfigContext
     * @param lbConfigName name of lb-config 
     * @param out OutputStream into which the loadbalancer.xml is written
     */
    public static void exportXml(LoadbalancerReader lbRdr,OutputStream out) 
                    throws IOException{

        // tranform the data using visitor pattern
        Loadbalancer _lb = new Loadbalancer();

        LoadbalancerVisitor lbVstr = new LoadbalancerVisitor(_lb);
        lbRdr.accept(lbVstr);

        try {
            String footer = _strMgr.getString("GeneratedFileFooter", 
                        new Date().toString());
            // write the header
            _lb.graphManager().setDoctype(PUBLICID, SYSTEMID);
            _lb.write(out);
            out.write(footer.getBytes());
        } finally {
            if (out != null) {
                out.close();
                out = null;
            }
        }
    }
    
    /**
     * returns the loadbalancer.xml as a string
     */
    public static String getXML(ConfigContext ctx,String lbConfigName) throws IOException, ConfigException, 
                Schema2BeansException{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // check if the lb exists
        LoadbalancerReader lbr = LbConfigExporter.getLbReader(ctx,lbConfigName);
        LbConfigExporter.exportXml(lbr, out);
        return out.toString();
    }
    
    

    private static final StringManager _strMgr = 
        StringManager.getManager(LbConfigWriter.class);

    private static final String PUBLICID = 
        "-//Sun Microsystems Inc.//DTD Sun Java System Application Server 9.1//EN";

    private static final String SYSTEMID = "sun-loadbalancer_1_2.dtd";


}
