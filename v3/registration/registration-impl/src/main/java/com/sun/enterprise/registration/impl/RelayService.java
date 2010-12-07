/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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


package com.sun.enterprise.registration.impl;
import java.util.Date;
import java.util.List;
import java.io.*;
import java.text.*;

import com.sun.enterprise.registration.RegistrationException;
import com.sun.enterprise.registration.impl.environment.EnvironmentInformation;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.InetAddress;
import java.util.Formatter;
//import com.sun.scn.servicetags.contrib.STClientRegistryHelper;

public class RelayService {

    private static final Logger logger = RegistrationLogger.getLogger();
    private static final String ENV_TOKEN   =   "@@@ENVIRONMENT@@@";
    private static final String TAG_TOKEN   =   "@@@SERVICE_TAGS@@@";
    private static final String PRODUCTNAME_TOKEN =   "@@@PRODUCTNAME@@@";
    private static final String TEMPLATE_FILE = "com/sun/enterprise/registration/impl/relay-template.html";

    private RepositoryManager rm;

    public RelayService(String repositoryFile) throws RegistrationException {
        rm = new RepositoryManager(new File(repositoryFile));
        // make sure runtime values are generated in RepositoryManager
        rm.updateRuntimeValues();
    }

    public void generateRegistrationPage(String outputFile) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(TEMPLATE_FILE);
        if (is == null)
            throw new RegistrationException("Template file [" + TEMPLATE_FILE + "] not found");

        List<ServiceTag> serviceTags = rm.getServiceTags();
        String productName = "";
        for (ServiceTag tag : serviceTags) {
            if (productName.length() > 0)
                productName = productName + " + ";
            productName = productName + tag.getProductName() + " " + tag.getProductVersion();
        }
        
        String tags = getHtml(serviceTags);
        String env = getEnvironmentInformation();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));

        String line;
        while ((line = br.readLine())!= null) {
            if (line.indexOf(ENV_TOKEN) >= 0)
                line = line.replaceAll(ENV_TOKEN, env);
            if (line.indexOf(TAG_TOKEN) >= 0)
                line = line.replaceAll(TAG_TOKEN, tags);
            if (line.indexOf(PRODUCTNAME_TOKEN) >= 0)
                line = line.replaceAll(PRODUCTNAME_TOKEN, productName);
            bw.write(line);
            bw.newLine();
        }
        bw.flush();
    }



    private String getHtml(List<ServiceTag> serviceTags) {
        if (serviceTags.isEmpty()) {
            logger.log(Level.WARNING, "No tags found");
            return "";
        }
        StringBuilder tags = new StringBuilder();
        for (ServiceTag serviceTag : serviceTags) {
            tags.append(getHtml(serviceTag));
        }
        return tags.toString();
    }


    private String  getEnvironmentInformation() throws RegistrationException {
        StringBuilder html = new StringBuilder();
        EnvironmentInformation se = new EnvironmentInformation();
/*
        hostName, "", // hostID
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch"),
                "", //systemModel
                "", //systemManuf.
                "", //cpuManuf
                "");
 *
 */

/*
        SystemEnvironment se = SystemEnvironment.getSystemEnvironment();
*/
        Formatter fmt = new Formatter(html);

        html.append("<environment>");
        fmt.format("<hostname>%s</hostname>\r\n", se.getHostname());
        fmt.format("<hostId>%s</hostId>\r\n",se.getHostId());
        fmt.format("<osName>%s</osName>\r\n",se.getOsName());
        fmt.format("<osVersion>%s</osVersion>\r\n",se.getOsVersion());
        fmt.format("<osArchitecture>%s</osArchitecture>\r\n",se.getOsArchitecture());
        fmt.format("<systemModel>%s</systemModel>\r\n",se.getSystemModel());
        fmt.format("<systemManufacturer>%s</systemManufacturer>\r\n",se.getSystemManufacturer());
        fmt.format("<cpuManufacturer>%s</cpuManufacturer>\r\n",se.getCpuManufacturer());
        fmt.format("<serialNumber>%s</serialNumber>\r\n",se.getSerialNumber());


        fmt.format("<physmem>%s</physmem>\r\n", se.getPhysMem());
        fmt.format("<cpuinfo>\r\n");
        fmt.format("<sockets>%s</sockets>\r\n", se.getSockets());
        fmt.format("<cores>%s</cores>\r\n", se.getCores());
        fmt.format("<virtcpus>%s</virtcpus>\r\n", se.getVirtCpus());
        fmt.format("<name>%s</name>\r\n", se.getCpuName());
        fmt.format("<clockrate>%s</clockrate>\r\n", se.getClockRate());
        fmt.format("</cpuinfo>\r\n");

        html.append("</environment>\r\n");
        return html.toString();
    }

    
    private String getHtml(ServiceTag tag) {
        StringBuilder html = new StringBuilder();
        Formatter fmt = new Formatter(html);
        fmt.format("<service_tag>\r\n");
        fmt.format("<instance_urn>%s</instance_urn>\r\n",tag.getInstanceURN());
        fmt.format("<product_name>%s</product_name>\r\n",tag.getProductName());
        fmt.format("<product_version>%s</product_version>\r\n",tag.getProductVersion());
        fmt.format("<product_urn>%s</product_urn>\r\n",tag.getProductURN());
        fmt.format("<product_parent_urn/>\r\n");
        fmt.format("<product_parent>%s</product_parent>\r\n",tag.getProductParent());
        fmt.format("<product_defined_inst_id>%s</product_defined_inst_id>\r\n",tag.getProductDefinedInstID());
        fmt.format("<product_vendor>%s</product_vendor>\r\n",tag.getProductVendor());
        fmt.format("<platform_arch>%s</platform_arch>\r\n",tag.getPlatformArch());
        fmt.format("<timestamp>%s</timestamp>", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z")).format(new Date()));
        fmt.format("<container>%s</container>\r\n",tag.getContainer());
        fmt.format("<source>%s</source>\r\n",tag.getSource());
        fmt.format("<installer_uid>-1</installer_uid>");

//        fmt.format("<installer_uid>%s</installer_uid>",tag.getInstallerUID());
        fmt.format("</service_tag>\r\n");

        return html.toString();
    }
}
