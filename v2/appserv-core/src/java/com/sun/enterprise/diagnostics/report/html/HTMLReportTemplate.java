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
package com.sun.enterprise.diagnostics.report.html;

import com.sun.enterprise.diagnostics.*;
import com.sun.enterprise.diagnostics.collect.DataType;
import com.sun.enterprise.diagnostics.collect.FileData;


import java.util.*;
import java.io.*;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

/**
 *
 * @author Manisha Umbarje
 */
public class HTMLReportTemplate {
    private static final String REPORT_NAME = File.separator + "ReportSummary.html";
    private Table toc;
    private Document doc;
    protected Element bodyElement;
    private Element descElement;
    private int tocNo;
    protected ReportConfig config;
    protected ReportTarget target;
    private String targetName;
    private String reportDir;
    protected DataTraverser dataObjTraverser ;

    private static  String report_description =
            "This Snapshot was generated for {0} at " +
            "{1,time} on {1,date}";

    private  static final String description = "Description";
    private static final String customer_information = "Customer Information";
    private static final String bug_ids = "Bug IDs";
    private static String component_details = "{0} details ";
    private static final String checksum_details = "Checksum Information";
    protected static final String monitoring_information = "Monitoring Information";
    private static final String system_information = "System Information";
    private static final String installation_log = "Installation Log";
    private static final String domain_xml_validation_details = "Domain Validation Details";
    private static Logger logger =
            LogDomains.getLogger(LogDomains.ADMIN_LOGGER);

     /**
      * Creates a new instance of HTMLReportTemplate
      * @targetName name of the target for which HTML report is being generated
      * @reportDir directory which contains data to generate HTML report summary
      */
    public HTMLReportTemplate(ReportConfig config, Data dataObj) {
            this.config = config;
            this.target = config.getTarget();
            this.targetName = target.getName();
            this.reportDir = target.getIntermediateReportDir();
            this.dataObjTraverser = new DataTraverser(dataObj);
            initialize();
    }

    public void write() {
        try {
            String reportFile = reportDir + REPORT_NAME;
            //System.out.println("Report Summary File Name : " + reportFile);
            File file = new File(reportFile);
            //System.out.println("DOC : " + doc.toString());
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(file));

            writer.write(doc.toString());
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
            //@todo handle exception intelligently
        }
    }

    protected void addMiscellaneousInfo(){
        // Do nothing for PE
    }

    private void initialize() {
        doc = new HTMLDocument();
        bodyElement = doc.getBody();

        addDocTitle();
        addDocHeading();
        addTOCSection();
        addDescSection();
        addCustomerInformation();
        addBugIds();
        addChecksumSection();
        addComponentDetails();
        addMiscellaneousInfo();
    }

    private void addDocHeading() {
        Element h1Element = new HTMLElement(HTMLReportConstants.H1);
        h1Element.addAttribute(HTMLReportConstants.ALIGN,
                HTMLReportConstants.CENTER);
        h1Element.addText(HTMLReportConstants.report_heading);
        bodyElement.add(h1Element);
    }

    private void addDocTitle() {
        Element titleElement = new HTMLElement(HTMLReportConstants.TITLE);
        titleElement.addText(HTMLReportConstants.report_heading);
        doc.getHead().add(titleElement);
    }

    private void addTOCSection() {
         toc = new Table();
         bodyElement.add(toc);
    }

    private void addDescSection() {
        addTitle(description, true, true, 0,0);
        descElement = new HTMLElement("desc");
        String description = getText(report_description,
                new Object[] {targetName, new Date()});
        descElement.addText(description);
        addElement(descElement);
   }

    private void addCustomerInformation() {
        Iterator<Data> iterator = dataObjTraverser.getData(DataType.CUSTOMER_INFO);

        if (iterator.hasNext()) {
            Data dataObj = iterator.next();
            addTitle(customer_information, true, true, 0,0);
            HTMLElement element = new HTMLElement("customer_information");
            if(dataObj instanceof FileData) {
                copyFromFile(element, dataObj.getSource());
            }
            element.addText(dataObj.getValues());
            addElement(element);
        }
    }

    private void addChecksumSection() {
        Iterator<Data> iterator = dataObjTraverser.getData(DataType.CHECKSUM);
        if(iterator.hasNext()) {
            ArrayList elements= new ArrayList();
            Element link = new Link(checksum_details,
                    "#"+getNextTocNo());
            elements.add(link);
            toc.addRow(elements,null);
            Element bodyLink = new Link(null,null,getTocNo());
            bodyElement.add(bodyLink);
        }
        while(iterator.hasNext()) {
            Data checksumData = iterator.next();
            Iterator<Iterator<String>> details = checksumData.getTable();

            Table table = new Table(1,0);
            Element h2 = new HTMLElement("h2");
            h2.addText(checksumData.getSource() + " " + checksum_details);
            bodyElement.add(h2);
            table.addRow(details.next(), true, null);

            while(details.hasNext()) {
                table.addRow(details.next(), false, null);
            }
            bodyElement.add(table);
        }

    }

    private void addBugIds() {
        if(config.getCLIOptions().getBugIds() != null &&
                config.getCLIOptions().getBugIds().trim().length() > 0) {
            addTitle(bug_ids, true, true, 0,0);
            HTMLElement bugIdElement = new HTMLElement(bug_ids);
            bugIdElement.addText(config.getCLIOptions().getBugIds());
            addElement(bugIdElement);
        }
    }


    private void addComponentDetails() {
        String componentDetailsTitle = getText(component_details,
                new Object[] {target.getName()});
        addTitle(componentDetailsTitle, true, true, 0,0);
        Element element = new HTMLElement("component_details");
        addInstanceSpecificSection(element);
        addElement(element);
     }
     protected void addInstanceSpecificSection(Element element) {
         if(element != null) {
            Iterator<ServiceConfig > configurations = config.getInstanceConfigurations();

            while(configurations.hasNext()) {
                ServiceConfig config = configurations.next();
                String instanceName = config.getInstanceName();
                int indentation = 1;
                String linkPrefix = "." + File.separator ;
                String instanceFolder = reportDir;
                if(!(instanceName.equals(TargetType.DAS.getType()))) {
                    linkPrefix = instanceName + File.separator ;
                    instanceFolder = instanceFolder + File.separator + instanceName;
                    addLink(element, instanceName, instanceName, indentation++);
                }

                addLink(element, "config", linkPrefix + "config",
                        indentation);

                //add generated and applications only if directories exist
                if(exists(instanceFolder, Constants.GENERATED))
                    addLink(element, Constants.GENERATED,
                            linkPrefix + Constants.GENERATED, indentation);
                if(exists(instanceFolder, Constants.APPLICATIONS))
                    addLink(element, Constants.APPLICATIONS,
                            linkPrefix + Constants.APPLICATIONS, indentation);
                if(exists(instanceFolder, Defaults.LOGS))
                    addLink(element,  Defaults.LOGS, linkPrefix + Defaults.LOGS,
                            indentation);
                if(exists(instanceFolder,Defaults.DOMAIN_XML_VERIFICATION_OUTPUT))
                    addLink(element, domain_xml_validation_details,
                            linkPrefix + Defaults.DOMAIN_XML_VERIFICATION_OUTPUT,
                            indentation);
                addInstallationLogSection(config.isCaptureInstallLogEnabled(),
                        element, indentation);
                addLink(config.isCaptureSystemInfoEnabled(),
                        element, DataType.SYSTEM_INFO, system_information,
                        indentation);

                addMonitoringInfo(element, instanceName, indentation);

            }//while
        }
    }

    protected void addMonitoringInfo(Element element, String instanceName,
                                     int indentation) {
        if (!config.getCLIOptions().isLocal()) {
             addLink(element, monitoring_information,
                     instanceName +
                     File.separator + Defaults.MONITORING_INFO_FILE,
                     indentation);
        }
    }
    private void addInstallationLogSection(boolean enabled,
            Element element, int indentation) {
        List<String> files = new ArrayList(2);
        if(enabled) {
            String[] logFiles =
                    new File(
                    target.getIntermediateReportDir()).list(new FilenameFilter() {
               public boolean accept(File folder, String name)  {
                   if( (name.contains(Constants.INSTALLATION_LOG_PREFIX)) ||
                           (name.contains(Constants.SJSAS_INSTALLATION_LOG_PREFIX)))
                       return true;
                   return false;
               }
            });
            files.addAll(Arrays.asList(logFiles));
        }

        for(String fileName : files) {
            addLink(element,  fileName, fileName,  indentation);
        }
    }


    private void addLink(boolean enabled, Element element, String dataType,
            String text, int indentation) {
        if(enabled) {
            Iterator<Data> iterator = dataObjTraverser.getData(dataType);
            while(iterator.hasNext()){
                Data data = iterator.next();
                addLink(element, text, data.getSource(), indentation);
            }
        }
    }
    protected boolean exists(String parent, String folder) {
        File file = new File(parent + File.separator +
                folder);
        return file.exists();
    }

     protected void addTitle(String title, boolean add2TOC,
            boolean showTitleInBody,
            int titleIndent, int textIndent){

        String tocTitle = indentText("", titleIndent);

         if(add2TOC){
            ArrayList elements= new ArrayList();
            Element p = new HTMLElement(HTMLReportConstants.PARA);
            p.addText(tocTitle);

            Element link = new Link(title,"#"+getNextTocNo());
            p.add(link);
            elements.add(p);
            toc.addRow(elements,null);
        }


        Element bodyLink = new Link(null,null,getTocNo());
        bodyElement.add(bodyLink);

        if(showTitleInBody){
            Element h2 = new HTMLElement(HTMLReportConstants.H2);
            h2.addText(title);
            bodyElement.add(h2);
        }
    }

    protected void addElement(Element element) {
        bodyElement.add(element);
        bodyElement.add(new HTMLElement(HTMLReportConstants.BR));
    }


    protected void addLink(Element element, String text, String link,
            int textIndent){
        String indentation = indentText("", textIndent);

        if(text!=null){
            if(link==null)
                link="#";
            Link detail = new Link(text,link);
            Element span = new HTMLElement(HTMLReportConstants.SPAN);
            span.addText(indentation);
            span.add(detail);
            element.add(span);
            element.add(new HTMLElement(HTMLReportConstants.BR));
        }
    }

    protected String getNextTocNo(){
      return String.valueOf(++tocNo);
    }

    protected String getTocNo(){
      return String.valueOf(tocNo);
    }

    /**
     * Indent a text
     */
    private String indentText(String text, int indentIndex) {
        String indentedText = text;
        for(int i=1; i <=indentIndex; i++){
            indentedText =  (Escape.getInstance().
             decodeEntities("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
             "&nbsp;&nbsp;&nbsp;")) + indentedText;
        }
        return indentedText;

    }

    private String getText(String str, Object[] args) {
        MessageFormat form = new MessageFormat(str);
        return form.format(str,args);
    }

    private void copyFromFile(Element element, String fileName) {
        if ((element != null) && (fileName != null)) {
            logger.log(Level.FINE,"fileName : " + fileName);
            BufferedReader reader = null;
            try {
                reader =
                    new BufferedReader(new FileReader(fileName));

                String entry ;
                while((entry = reader.readLine()) != null) {
                    logger.log(Level.FINE, "entry : " + entry);
                    element.addText(entry);
                    element.add(new HTMLElement("br"));
                }
                reader.close();

            } catch(IOException e) {
                logger.log(Level.WARNING,"diagnostic-service.copy_failed",
                        new Object[]{fileName,e.getMessage()});
            } finally {

            }
        }
    }
}
