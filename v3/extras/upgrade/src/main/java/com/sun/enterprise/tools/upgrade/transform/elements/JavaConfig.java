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

/*
 * JavaConfig.java
 *
 * Created on August 4, 2003, 2:04 PM
 */

package com.sun.enterprise.tools.upgrade.transform.elements;

/**
 *
 * @author  prakash
 */
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import com.sun.enterprise.tools.upgrade.transform.ElementToObjectMapper;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

public class JavaConfig extends BaseElement {
    private java.util.List exludedJarList = null;

    /** Creates a new instance of Element */
    public JavaConfig() {
    }
    /**
     * element - java-config
     * parentSource - parent server of element
     * parentResult - domain
     */
    public void transform(Element element, Element parentSource, Element parentResult){
        // There is always a security service in result as well as source
       logger.log(Level.FINE, stringManager.getString("upgrade.transform.transformingMSG", this.getClass().getName(), element.getTagName()));
       
	   java.util.Vector notToTransferAttrList = new java.util.Vector();
	   notToTransferAttrList.add("classpath-suffix");
	   notToTransferAttrList.add("classpath-prefix");
	   notToTransferAttrList.add("java-home");
	   notToTransferAttrList.add("server-classpath");
	   
	   NodeList javaConfs = parentResult.getElementsByTagName("java-config");
       Element javaConf = null;
	   
	   logger.log(Level.FINE, stringManager.getString(this.getClass().getName() + ":: javaConfs.getLength() " , javaConfs.getLength()));
       if(javaConfs.getLength() == 0){
            javaConf = parentResult.getOwnerDocument().createElement("java-config");
            // server-classpth should be parsed and should be appended to the target.  FIX IT
            this.transferAttributes(element, javaConf, notToTransferAttrList);
            this.updateClassPathAttributes(element,javaConf);
			this.appendElementToParent(parentResult,javaConf);
        }else {
            javaConf = (Element)javaConfs.item(0);
            this.transferAttributes(element, javaConf, notToTransferAttrList);
           this.updateClassPathAttributes(element,javaConf);
        }
        super.transform(element,  parentSource, javaConf);
    }

    private void updateClassPathAttributes(Element source, Element target){
        // update classpath-suffix
        String cpSuffix = source.getAttribute("classpath-suffix");
        if((cpSuffix != null) && (!cpSuffix.trim().equals(""))){
            String cpToAppend = getClassPathStringToAppend(cpSuffix);
            if(cpToAppend != null){
                String targetCP = target.getAttribute("classpath-suffix");
                if((targetCP == null) || (targetCP.trim().equals(""))){
                    target.setAttribute("classpath-suffix", cpToAppend);
                }else{
                    targetCP = targetCP+"${path.separator}"+cpToAppend;
                    target.setAttribute("classpath-suffix", targetCP);
                }
            }
        }
        //${com.sun.aas.installRoot}/lib/install/applications/jmsra/imqjmsra.jar${path.separator}${com.sun.aas.imqLib}/jaxm-api.jar${path.separator}${com.sun.aas.imqLib}/fscontext.jar${path.separator}${com.sun.aas.antLib}/ant.jar${path.separator}${com.sun.aas.hadbRoot}/lib/hadbjdbc4.jar${path.separator}${com.sun.aas.jdmkHome}/lib/jdmkrt.jar${path.separator}${com.sun.aas.mfwkHome}/lib/mfwk_instrum_tk.jar:C:/Softwares/SunStud/AppServer7/lib/appserv-ideplugin.jar;C:/Softwares/SunStud/MessageQueue3.5/imq/lib/imq.jar;C:/Softwares/SunStud/MessageQueue3.5/imq/lib/jaxm-api.jar;C:/Softwares/SunStud/MessageQueue3.5/imq/lib/imqadmin.jar;C:/Softwares/SunStud/MessageQueue3.5/imq/lib/imqutil.jar;C:/Softwares/SunStud/MessageQueue3.5/imq/lib/fscontext.jar;C:/Softwares/SunStud/MessageQueue3.5/imq/lib/providerutil.jar
        // update classpath-prefix
        String cpPrefix = source.getAttribute("classpath-prefix");
        if((cpPrefix != null) && (!cpPrefix.trim().equals(""))){
            String cpToAppend = getClassPathStringToAppend(cpPrefix);
            if(cpToAppend != null){
                String targetCP = target.getAttribute("classpath-prefix");
                if((targetCP == null) || (targetCP.trim().equals(""))){
                    target.setAttribute("classpath-prefix", cpToAppend);
                }else{
                    targetCP = targetCP+"${path.separator}"+cpToAppend;
                    target.setAttribute("classpath-prefix", targetCP);
                }
            }
        }
    }
    private String getClassPathStringToAppend(String sourceCPString){
        if(sourceCPString == null)
            return null;
        java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(sourceCPString,System.getProperty("path.separator"));
        String cpToAppend = null;
        if(this.exludedJarList == null)
            this.buildExcludedJarList();
        while(tokenizer.hasMoreTokens()){
            String token = tokenizer.nextToken();
            if(this.isValidClassPathElement(token)){
                if(cpToAppend == null){
                    cpToAppend = token;
                }else{
                    cpToAppend = cpToAppend+"${path.separator}"+token;
                }
            }
        }
        return cpToAppend;
    }
    private boolean isValidClassPathElement(String cp){
        // compare the cp with
        for(int i=0; i < this.exludedJarList.size(); i++){
            if(cp.indexOf((String)this.exludedJarList.get(i)) != -1)
                return false;
        }
        return true;
    }
    private void buildExcludedJarList(){
        if(this.exludedJarList !=null)
            return;
        this.exludedJarList = new java.util.ArrayList();
        this.exludedJarList.add("pbclient42RE.jar");
        this.exludedJarList.add("sax.jar");
        this.exludedJarList.add("xalan.jar");
        this.exludedJarList.add("dom4j.jar");
        this.exludedJarList.add("xercesImpl.jar");
        this.exludedJarList.add("jaxp-api.jar");
        this.exludedJarList.add("jaxrpc-api.jar");
        this.exludedJarList.add("xmlsec.jar");
        this.exludedJarList.add("saaj-api.jar");
        this.exludedJarList.add("jaxrpc-impl.jar");
        this.exludedJarList.add("jaxrpc-spi.jar");
        this.exludedJarList.add("common-logging.jar");
        this.exludedJarList.add("saaj-impl.jar");
        this.exludedJarList.add("mail.jar");
        this.exludedJarList.add("activation.jar");
        this.exludedJarList.add("jaas.jar");
        this.exludedJarList.add("jdk_logging.jar");
        this.exludedJarList.add("servlet.jar");
        this.exludedJarList.add("xsltc.jar");
        this.exludedJarList.add("relaxngDatatype.jar");
        this.exludedJarList.add("xsdlib.jar");
        this.exludedJarList.add("jakarta-log4j.jar");
        this.exludedJarList.add("namespace.jar");
        this.exludedJarList.add("appserv-rt.jar");
        this.exludedJarList.add("jmxremote_optional.jar");
        this.exludedJarList.add("rmissl.jar");
        this.exludedJarList.add("appserv-tags.jar");
        this.exludedJarList.add("jsf-api.jar");
        this.exludedJarList.add("activation.jar");
        this.exludedJarList.add("appserv-upgrade.jar");
        this.exludedJarList.add("jsf-impl.jar");
        this.exludedJarList.add("admin-cli.jar");
        this.exludedJarList.add("appservLauncher.jar");
        this.exludedJarList.add("j2ee-svc.jar");
        this.exludedJarList.add("j2ee.jar");
        this.exludedJarList.add("sun-appserv-ant.jar");
        this.exludedJarList.add("jaxr-api.jar");
        this.exludedJarList.add("appserv-admin.jar");
        this.exludedJarList.add("jaxr-impl.jar");
        this.exludedJarList.add("appserv-assemblytool.jar");
        this.exludedJarList.add("jaxrpc-api.jar");
        this.exludedJarList.add("appserv-cmp.jar");
        this.exludedJarList.add("commons-launcher.jar");
        this.exludedJarList.add("appserv-ext.jar");
        this.exludedJarList.add("commons-logging.jar");
        this.exludedJarList.add("jhall.jar");
        this.exludedJarList.add("appserv-jstl.jar");
        this.exludedJarList.add("deployhelp.jar");
        this.exludedJarList.add("jmxremote.jar");
        this.exludedJarList.add("relaxngDatatype.jar");
        this.exludedJarList.add("jaxb-api.jar");
        this.exludedJarList.add("jaxb-impl.jar");
        this.exludedJarList.add("jaxb-libs.jar");
        this.exludedJarList.add("jaxb-xjc.jar");
        this.exludedJarList.add("jax-qname.jar");
        this.exludedJarList.add("namespace.jar");
    }

    /**
     * This method appends the classpath for montoring framework for AS 8.2
     */
    public void appendSuffixToClasspath(Element source, Element javaConf) {
        String serverCP = javaConf.getAttribute("server-classpath");
        if(serverCP != null && !serverCP.trim().equals("")) {
            String modServerCP = serverCP+
                    "${path.separator}${com.sun.aas.mfwkHome}/lib/mfwk_instrum_tk.jar";
            javaConf.setAttribute("server-classpath", modServerCP);

        }
    }

    public void appendSystemClasspath(Element source, Element javaConf) {
        String systemCP = javaConf.getAttribute("system-classpath");
        if(systemCP != null && !systemCP.trim().equals("")) {
	        String modSystemCP = systemCP +
				"${path.separator}${com.sun.aas.installRoot}/lib/appserv-launch.jar";
			javaConf.setAttribute("system-classpath", modSystemCP);
	    } else {
            //Create and add
			javaConf.setAttribute("system-classpath",
                "${com.sun.aas.installRoot}/lib/appserv-launch.jar");
        }
    }
}
