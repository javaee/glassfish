/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.registration.impl;
import java.util.List;
import java.io.*;

import com.sun.enterprise.registration.RegistrationException;
import java.util.logging.Logger;
import java.util.logging.Level;
//import com.sun.scn.servicetags.SystemEnvironment;
import java.util.Formatter;
//import com.sun.scn.servicetags.contrib.STClientRegistryHelper;

public class Relay {

    private RepositoryManager rm;
    private static final Logger logger = RegistrationLogger.getLogger();
    private static final String ENV_TOKEN = "@@@ENV_TOKEN@@@";
    private static final String  TAG_TOKEN = "@@@SERVICE_TAG_TOKEN@@@";
    private static final String TEMPLATE_FILE = "com/sun/enterprise/registration/impl/relay-template.html";

    public Relay(String repositoryFile) throws RegistrationException {
        rm = new RepositoryManager(new File(repositoryFile));
        // make sure runtime values are generated in RepositoryManager
        rm.updateRuntimeValues();
    }

    public void generateRegistrationPage(String outputFile) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(TEMPLATE_FILE);
        if (is == null)
            throw new RegistrationException("Template file [" + TEMPLATE_FILE + "] not found");

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        FileWriter fw = new FileWriter(outputFile);
        if (fw == null)
            throw new RegistrationException("fw is NULL!!");

        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        String line;
        String env = getEnvironmentInformation();
        String tags = getServiceTags();

        while ((line = br.readLine())!= null) {
            if (line.indexOf(ENV_TOKEN) >= 0)
                line.replaceAll(line, env);
            if (line.indexOf(TAG_TOKEN) >= 0)
                line.replaceAll(line, tags);
            System.out.println(line);
            bw.write(line);
        }
        bw.flush();
    }



    private String getServiceTags() {
        List<ServiceTag> serviceTags = rm.getServiceTags();
        if (serviceTags.isEmpty()) {
            logger.log(Level.WARNING, "No unregistered tags found");
            return "";
        }
        StringBuilder tags = new StringBuilder();
        for (ServiceTag serviceTag : serviceTags) {
            tags.append(getHtml(serviceTag));
        }
        return tags.toString();
    }


    private String  getEnvironmentInformation() throws Exception {
        StringBuilder html = new StringBuilder();
        /*
        html.append("<input type=\"hidden\" name=\"servicetag_payload\" ");
        html.append("value=\"<?html version=%221.0%22 encoding=%22UTF-8%22 standalone=%22no%22?>");
        html.append("<registration_data version=%221.0%22><environment>");
        */
/*
        SystemEnvironment se = SystemEnvironment.getSystemEnvironment();

        Formatter fmt = new Formatter(html);

        fmt.format("<hostname>%s</hostname>", se.getHostname());
        fmt.format("<hostId>%s</hostId>",se.getHostId());
        fmt.format("<osName>%s</osName>",se.getOsName());
        fmt.format("<osVersion>%s</osVersion>",se.getOsVersion());
        fmt.format("<osArchitecture>%s</osArchitecture>",se.getOsArchitecture());
        fmt.format("<systemModel>%s</systemModel>",se.getSystemModel());
        fmt.format("<systemManufacturer>%s</systemManufacturer>",se.getSystemManufacturer());
        fmt.format("<cpuManufacturer>%s</cpuManufacturer>",se.getCpuManufacturer());
        fmt.format("<serialNumber>%s</serialNumber>",se.getSerialNumber());
        fmt.format("<physmem>%s</physmem>",se.getPhysMem());
        fmt.format("<cpuinfo>");
        fmt.format("<sockets>%s</sockets>",se.getSockets());
        fmt.format("<cores>%s</cores>",se.getCores());
        fmt.format("<virtcpus>%s</virtcpus>",se.getVirtCpus());
        fmt.format("<name>%s</name>",se.getCpuName());
        fmt.format("<clockrate>%s</clockrate>",se.getClockRate());
        fmt.format("</cpuinfo>");
        html.append("</environment>");
        html.append("<registry urn=%22");
        html.append(STClientRegistryHelper.getRegistryURN());
        html.append("%22 version=%221.0%22>");
*/
        return html.toString();
    }

    private String getHtml(ServiceTag tag) {
        StringBuilder html = new StringBuilder();
        Formatter fmt = new Formatter(html);
        fmt.format("<service_tag>");
        fmt.format("<instance_urn>%s</instance_urn>",tag.getInstanceURN());
        fmt.format("<product_name>%s</product_name>",tag.getProductName());
        fmt.format("<product_version>%s</product_version>",tag.getProductVersion());
        fmt.format("<product_urn>%s</product_urn>",tag.getProductURN());
        fmt.format("<product_parent_urn/>");
        fmt.format("<product_parent>%s</product_parent>",tag.getProductParent());
        fmt.format("<product_defined_inst_id>%s</product_defined_inst_id>",tag.getProductDefinedInstID());
        fmt.format("<product_vendor>%s</product_vendor>",tag.getProductVendor());
        fmt.format("<platform_arch>%s</platform_arch>",tag.getPlatformArch());
//        fmt.format("<timestamp>%s</timestamp>", df.format(tag.getTimestamp()));
        fmt.format("<container>%s</container>",tag.getContainer());
        fmt.format("<source>%s</source>",tag.getSource());
//        fmt.format("<installer_uid>%s</installer_uid>",tag.getInstallerUID());
        fmt.format("</service_tag>");

        return html.toString();
    }
}
