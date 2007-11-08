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
import java.io.File;
import java.util.Date;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Proxy;
import java.net.InetSocketAddress;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.LbConfig;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ElementProperty;

import com.sun.enterprise.ee.admin.lbadmin.reader.api.LoadbalancerReader;
import com.sun.enterprise.ee.admin.lbadmin.transform.LoadbalancerVisitor;
import com.sun.enterprise.ee.admin.lbadmin.beans.Loadbalancer;

import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import org.netbeans.modules.schema2beans.Schema2BeansException;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.security.SSLUtils;

import com.sun.enterprise.util.i18n.StringManager;

/**
 * Export support class
 * 
 * @author Satish Viswanatham
 */
public class LbConfigWriter {

    public LbConfigWriter(ConfigContext ctx, String lbConfigName, String path){
    
        if ((path == null) || "".equals(path) || "null".equals(path)) {
            // creates a file (loadbalancer.xml.<lbconfigname> in the generated
            // directory.
            String iRoot = System.getProperty(
                        SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);

            _path = iRoot + File.separator + PEFileLayout.GENERATED_DIR 
                    + File.separator +  _f + "." + lbConfigName;
        } else {
            File f = new File(path);
            if (f.isDirectory()) {
                _path = path + File.separator +  _f + "." + lbConfigName;
            } else {
                _path = path;
            }
        }
        _name = lbConfigName;
        _ctx = ctx;

    }


    public String write() throws IOException, ConfigException, 
                Schema2BeansException{

        // check if the lb exists
        LoadbalancerReader lbr = LbConfigExporter.getLbReader(_ctx, _name);            
        File f = new File(_path);

        if (f.isDirectory() ) {
         f = new File(f, _f);
        }

        if (f.exists()) {
            String msg = _strMgr.getString("FileExists", f.getPath());
            throw new IOException(msg);
        }

        if ( !(f.getParentFile().exists()) ) {
            String msg = _strMgr.getString("ParentFileMissing", f.getParent());
            throw new IOException(msg);
        }

        FileOutputStream fo = null;

        try {
            fo = new FileOutputStream(f);
            String footer = _strMgr.getString("GeneratedFileFooter", 
                        new Date().toString());
            LbConfigExporter.exportXml(lbr, fo);            
            String fAbsPath = f.getAbsolutePath();
            return fAbsPath;
        } finally {
            if (fo != null) {
                fo.close();
                fo = null;
            }
        }
    }
    

    
    public static void main(String[] args) {

        if ( args.length < 2) {
            return; // print errror and usage XXX
        }

        try {
            String cPath = "domain.xml";
            ConfigContext ctx =  null;
            try {
                ctx = ConfigFactory.createConfigContext(cPath); 
            } catch (ConfigException ce) {
                ce.printStackTrace();
            }
            LbConfigWriter lbw = new LbConfigWriter(ctx, args[0], args[1]);
            lbw.write();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // ----- PRIVATE VARS -----
    Loadbalancer _lb = null;
    String _path = null;
    String _name = null;
    ConfigContext _ctx = null;
    String _f = "loadbalancer.xml";
    private static final StringManager _strMgr = 
        StringManager.getManager(LbConfigWriter.class);



}
