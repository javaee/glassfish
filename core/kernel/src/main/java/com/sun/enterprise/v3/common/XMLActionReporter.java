/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.v3.common;

import java.io.OutputStream;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;

/**
 * Represents the action report as XML like this:
 * <br>
 * <!-- 
 *     Apologies for the formatting - it's necessary for the JavaDoc to be readable 
 *     If you are using NetBeans, for example, click anywhere in this comment area to see
 *     the document example clearly in the JavaDoc preview
 * -->
 * <code> 
 * <br>&lt;action-report description="xxx" exit-code="xxx" [failure-cause="xxx"]>
 * <br>&nbsp;&nbsp;&lt;message-part message="xxx">
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&lt;property name="xxx" value="xxx"/>
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;...
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&lt;message-part message="xxx" type="xxx">
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;...
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&lt;/message-part>
 * <br>&nbsp;&nbsp;&lt/message-part>
 * <br>&nbsp;&nbsp;&lt;action-report ...> [for subactions]
 * <br>&nbsp;&nbsp;...
 * <br>&nbsp;&nbsp;&lt;/action-report>
 * <br>&lt;/action-report>
 * </code>
 * 
 * @author tjquinn
 */
@Service(name="xml")
@Scoped(PerLookup.class)
public class XMLActionReporter extends ActionReporter {

    public void writeReport(OutputStream os)  {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document d = db.newDocument();

            d.appendChild(writeActionReport(d, this));
            writeXML(d, os);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
    
    /**
     * Creates a new Element representing the XML content describing an
     * action report.  Invokes itself recursively to capture information
     * about any subactions.
     * @param owningDocument Document which will own all generated XML content
     * @param report the ActionReporter to convert to XML content
     * @return Element for the specified ActionReporter (and any sub-reports)
     */
    private Element writeActionReport(Document owningDocument, ActionReporter report) {
        Element result = owningDocument.createElement("action-report");
        result.setAttribute("description", report.actionDescription);
        result.setAttribute("exit-code", report.getActionExitCode().name());
        if (exception != null) {
            result.setAttribute("failure-cause", exception.getLocalizedMessage());
        }

        writePart(result, report.getTopMessagePart(), null);
        for (ActionReporter subReport : report.subActions) {
            result.appendChild(writeActionReport(owningDocument, subReport));
        }
        return result;
    }

    @Override
    public String getContentType() {
        return "text/xml"; 
    }
    
    
    
    private void writePart(Element actionReport, MessagePart part, String childType) {
        Document d = actionReport.getOwnerDocument();
        Element messagePart = d.createElement("message-part");
        actionReport.appendChild(messagePart);
        if (childType != null) {
            messagePart.setAttribute("type", childType);
        }
        
        for (Map.Entry prop : part.getProps().entrySet()) {
            Element p = d.createElement("property");
            messagePart.appendChild(p);
            p.setAttribute("name", prop.getKey().toString());
            p.setAttribute("value", prop.getValue().toString());
        }
        messagePart.setAttribute("message", part.getMessage());
        for (MessagePart subPart : part.getChildren()) {
            writePart(messagePart, subPart, subPart.getChildrenType());
        }
    }
    
    private void writeXML(Document doc, OutputStream os) throws TransformerConfigurationException, TransformerException {
        Source source = new DOMSource(doc);

        Result result = new StreamResult(os);

        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(source, result);
    }
}
