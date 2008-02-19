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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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
 * <br>&nbsp;&nbsp;&lt>/message-part>
 * <br>&lt;/action-report>
 * </code>
 * 
 * @author tjquinn
 */
public class XMLActionReporter extends ActionReporter {

    @Override
    public void writeReport(OutputStream os) throws IOException {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        try {
            XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(os);
            streamWriter.writeStartDocument();
            streamWriter.writeStartElement("action-report");
            streamWriter.writeAttribute("description", actionDescription);
            streamWriter.writeAttribute("exit-code", exitCode.name());
            if (exception != null) {
                streamWriter.writeAttribute("failure-cause", exception.getLocalizedMessage());
            }
            
            writePart(streamWriter, topMessage, null);
            
            streamWriter.writeEndElement();
            streamWriter.writeEndDocument();
            streamWriter.close();
        } catch (XMLStreamException se) {
            throw new IOException(se);
        }
        
    }

    @Override
    public String getContentType() {
        return "text/xml";
    }
    
    
    private void writePart(XMLStreamWriter writer, MessagePart part, String childType) throws XMLStreamException {
        writer.writeStartElement("message-part");
        writer.writeAttribute("message", part.getMessage());
        if (childType != null) {
            writer.writeAttribute("type", childType);
        }

        for (Map.Entry prop : part.getProps().entrySet()) {
            writer.writeStartElement("property");
            writer.writeAttribute("name", prop.getKey().toString());
            writer.writeAttribute("value", prop.getValue().toString());
            writer.writeEndElement();
        }
        
        for (MessagePart subPart : part.getChildren()) {
            writePart(writer, subPart, subPart.getChildrenType());
        }
        writer.writeEndElement();
    }

}
