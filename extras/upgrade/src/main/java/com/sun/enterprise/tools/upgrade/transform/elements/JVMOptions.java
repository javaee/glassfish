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
 * JVMOptions.java
 *
 * Created on August 4, 2003, 2:04 PM
 */

package com.sun.enterprise.tools.upgrade.transform.elements;
import org.w3c.dom.Document;
import com.sun.enterprise.tools.upgrade.common.UpgradeConstants;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import com.sun.enterprise.tools.upgrade.transform.ElementToObjectMapper;


public class JVMOptions extends BaseElement {
	
	private String JAVA_EXT_DIRS = "-Djava.ext.dirs";
	private String JDBC_DRIVERS = "-Djdbc.drivers";
	
	/** Creates a new instance of Element */
	public JVMOptions() {
	}
	
	/**
	 * element - jvm-options
	 * parentSource - parent of jvm-options
	 * parentResult - parent of jbm-options result
	 */
	public void transform(Element element, Element parentSource, Element parentResult){
		// There are no children for jvm-options, neither attributes.
		// jvm-options has only #CDATA.  Just need to transfer it, if not exists.
		NodeList jvmOptions = parentResult.getElementsByTagName("jvm-options");
		Element jvmOption = null;
		String modPropValue = null;
		String modSrcTxtData = null;
		Node oldTextNode = null;
		Node newTextNode = null;
		String srcTxtDt = this.getTextNodeData(element);
		String[] sourceTextData = this.parseTextData(srcTxtDt);
		// Text is not of form a=b.  It is of form a.
		if(sourceTextData == null){
			if(this.canTransfer(srcTxtDt, "")){
				for(int lh =0; lh < jvmOptions.getLength(); lh++){
					// Check only text which is of form a.
					String tgTxtDt = this.getTextNodeData((Element)jvmOptions.item(lh));
					String[] targetTextData = this.parseTextData(tgTxtDt);
					if (targetTextData == null){
						if(srcTxtDt.equals(tgTxtDt)){
							jvmOption = (Element)jvmOptions.item(lh);
							break;
						}
					}
				}
				if(jvmOption == null){
					jvmOption = parentResult.getOwnerDocument().createElement("jvm-options");
					Node textNode = jvmOption.getOwnerDocument().createTextNode(srcTxtDt);
					jvmOption.appendChild(textNode);
					parentResult.appendChild(jvmOption);
				}
			}else {
				logger.log(java.util.logging.Level.WARNING,
					stringManager.getString("upgrade.transform.jvmoptions.notTransferred", srcTxtDt));
			}
			return;
		}
		
		
		if(srcTxtDt.trim().equals("-Djdbc.drivers=com.pointbase.jdbc.jdbcUniversalDriver")) {
			//JDBC Drivers should not be transformed as the universal driver is derby for 9.0
			return;
		}
		
		// Text is of form a=b.
		if(this.canTransfer(sourceTextData[0], sourceTextData[1])){
			for(int lh =0; lh < jvmOptions.getLength(); lh++){
				String tgTxtDt = this.getTextNodeData((Element)jvmOptions.item(lh));
				if(srcTxtDt.equals(tgTxtDt)){
					jvmOption = (Element)jvmOptions.item(lh);
					break;
				}
				String[] targetTextData = this.parseTextData(tgTxtDt);
				if(sourceTextData != null && targetTextData != null){
					if(sourceTextData[0].equals(targetTextData[0])){
						jvmOption = (Element)jvmOptions.item(lh);
						break;
					}
				}
			}
			if(jvmOption == null){
				if(sourceTextData[1].indexOf(this.commonInfoModel.getSource().getInstallDir()) == -1){
					jvmOption = parentResult.getOwnerDocument().createElement("jvm-options");
					Node textNode = jvmOption.getOwnerDocument().createTextNode(srcTxtDt);
					jvmOption.appendChild(textNode);
					parentResult.appendChild(jvmOption);
				}
			}
		}else{
			logger.log(java.util.logging.Level.WARNING,
				stringManager.getString("upgrade.transform.jvmoptions.notTransferred", sourceTextData[0]));
		}
	}
	
	private String[] parseTextData(String fullStr){
		String[] parsedStrings = null;
		java.util.StringTokenizer stk = new java.util.StringTokenizer(fullStr,"=");
		if((stk.hasMoreTokens()) && (stk.countTokens() == 2)){
			// There should be only two tokens typically.
			parsedStrings = new String[2];
			parsedStrings[0] = stk.nextToken();
			parsedStrings[1] = stk.nextToken();
		}
		return parsedStrings;
	}
	
	private String getTextNodeData(Element element){
		NodeList children = element.getChildNodes();
		for(int index=0; index < children.getLength(); index++){
			if(children.item(index).getNodeType() == Node.TEXT_NODE){
				return children.item(index).getNodeValue();
			}
		}
		return "";
	}
	
	private boolean canTransfer(String optionName, String optionValue){
		// This method should have a list of items that is not good to transfer to 81... Need to determine the list.
		if((optionName.indexOf("Dorg.xml.sax.parser") != -1) ||
			(optionName.indexOf("Dorg.xml.sax.driver") != -1) ||
			(optionName.indexOf("Dcom.sun.jdo.api.persistence.model.multipleClassLoaders") != -1) ||
			(optionName.indexOf("Djava.util.logging.manager") != -1) ||
			(optionName.indexOf("Dcom.sun.aas.imqLib") != -1) ||
			(optionName.indexOf("Dcom.sun.aas.imqBin") != -1) ||
			(optionName.indexOf("Dcom.sun.aas.webServicesLib") != -1) ||
			(optionName.indexOf("Djavax.rmi.CORBA.UtilClass") != -1) ||
			(optionName.indexOf("Dcom.sun.aas.configRoot") != -1)){
			return false;
		}
		
		// non-standard jvm options that are not to be transfered
		if((optionName.indexOf("Xbootclasspath") != -1) ||
			(optionName.indexOf("Xloggc") != -1) ||
			(optionName.indexOf("Xms") != -1) ||
			(optionName.indexOf("Xmx") != -1) ||
			(optionName.indexOf("Xss") != -1)){
			return false;
		}
		
		if(commonInfoModel.isPlatformEdition(commonInfoModel.getSource().getEdition())) {
			if((optionName.indexOf("Djavax.net.ssl.trustStore") != -1) ||
				(optionName.indexOf("Djavax.net.ssl.keyStore") != -1)) {
				return false;
			}
		}

		String repOpValue = commonInfoModel.getSource().getInstallDir().replace('\\','/');
		if((optionValue.indexOf(repOpValue) != -1) ||
			(optionValue.indexOf(commonInfoModel.getSource().getInstallDir()) != -1)){
			return false;
		}
		// Don't transfer javax.net.ssl.keyStore or javax.net.ssl.trustStore from PE
		if(commonInfoModel.isEnterpriseEdition(commonInfoModel.getSource().getEdition())
		&&  optionName.indexOf("javax.net.ssl") != -1) {
			return false;
		}
		return true;
	}
	
	/** This method is specfic to AS 8.1 to AS 8.2 EE upgrade
	 * @param value of the system property that needs to be modified
	 * @return String
	 */
	public String insertDerbyJars(String propValue) {
		String prefixValue=propValue.substring(0,
			propValue.lastIndexOf("${path.separator}"));
		String suffixValue=propValue.substring(propValue.lastIndexOf("${path.separator}"),
			propValue.length());
		return prefixValue+"${path.separator}/${com.sun.aas.derbyRoot}/lib"+
			suffixValue;
	}
}
