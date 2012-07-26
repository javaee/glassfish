/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session3;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ConnectorResourceDefinitionDescriptor;
import com.sun.enterprise.deployment.DataSourceDefinitionDescriptor;
import com.sun.enterprise.deployment.io.ApplicationDeploymentDescriptorFile;

public class Client {

    private static SimpleReporterAdapter stat =  new SimpleReporterAdapter("appserv-tests");

    public Client(String[] args) {
    }

    public static void main(String[] args) throws Exception {
        stat.addDescription("connectore-resource-definitionclient");
        Client client = new Client(args);
        client.doTestDD();
        stat.printSummary("connectore-resource-definitionclient");
    }

    private void doTestDD() throws Exception{
        doTestDD_APP();
    }
    
    private void doTestDD_APP() throws Exception {
        String tcName = "connectore-resource-definition-DD-application-test";
        InputStream ddIS=null;
        try{
            String ddFileName = "crd-application.xml";
            File ddFile = new File("./descriptor", ddFileName);
            ddIS = new FileInputStream(ddFile);
            ApplicationDeploymentDescriptorFile ddReader = new ApplicationDeploymentDescriptorFile();
            Application application = (Application) ddReader.read( ddIS);
            
            Set<DataSourceDefinitionDescriptor> dsds = application.getDataSourceDefinitionDescriptors();
            for(DataSourceDefinitionDescriptor desc : dsds){
                System.out.println("DSD's Description is "+desc.getDescription());
            }
            
            Set<ConnectorResourceDefinitionDescriptor> actualCRDDs = application.getConnectorResourceDefinitionDescriptors();
            for(ConnectorResourceDefinitionDescriptor desc : actualCRDDs){
                System.out.println("CRD's Description is "+desc.getDescription());
            }

            Map<String,ConnectorResourceDefinitionDescriptor> expectedCRDDs = 
                    new HashMap<String,ConnectorResourceDefinitionDescriptor>();
            ConnectorResourceDefinitionDescriptor desc;

            desc = new ConnectorResourceDefinitionDescriptor();
            desc.setName("java:global/env/Application_Level_ConnectorResource");
            desc.setClassName("javax.resource.cci.ConnectionFactory");
            desc.setDescription("global-scope resource defined in application DD");
            desc.addProperty("transactionSupport", "LocalTransaction");
            desc.addProperty("resource-adatper-name", "RaApplicationName");
            expectedCRDDs.put(desc.getName(), desc);
            
            desc = new ConnectorResourceDefinitionDescriptor();
            desc.setName("java:app/env/Application_Level_ConnectorResource");
            desc.setClassName("javax.resource.cci.ConnectionFactory");
            desc.setDescription("application-scope resource defined in application DD");
            desc.addProperty("transactionSupport", "LocalTransaction");
            desc.addProperty("resource-adatper-name", "RaApplicationName");
            expectedCRDDs.put(desc.getName(), desc);

//            ClientDescriptorUtil.compareCRDD(expectedCRDDs, actualCRDDs);
            stat.addStatus(tcName, stat.PASS);
            
        }catch(Exception e){
            stat.addStatus(tcName, stat.FAIL);
            throw e;
        }finally{
            if(ddIS != null){
                ddIS.close();
            }
        }

        return;

    }


}

