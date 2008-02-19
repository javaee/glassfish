/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.deployment.client;

import java.io.InputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 *
 * @author tjquinn
 */
public class CommandXMLResultParser {

    static DFDeploymentStatus parse(InputStream is) throws XMLStreamException {
        XMLEventReader xmlReader = XMLInputFactory.newInstance().createXMLEventReader(is);
        DFDeploymentStatus topStatus = null;
        DFDeploymentStatus currentLevel = null;

        
        while(xmlReader.hasNext()) {
            XMLEvent event = xmlReader.nextEvent();
            if (event.isStartElement()) {
                StartElement start = (StartElement) event;
                if (start.getName().getLocalPart().equals("action-report")) {
                    /*
                     * This is the top-level element, so create the top-level DFDeploymentStatus object.
                     */
                    currentLevel = topStatus = new DFDeploymentStatus();
                    topStatus.setStageStatus(exitCodeToStatus(attrToText(start, "exit-code")));
                    topStatus.setStageDescription(attrToText(start, "description"));
                    String failureCause = attrToText(start, "failure-cause");
                    if (failureCause != null) {
                        topStatus.setStageStatusMessage(failureCause);
                    }
                } else if (start.getName().getLocalPart().equals("message-part")) {
                    /*
                     * Create a new lower-level stage.
                     */
                    currentLevel = new DFDeploymentStatus(currentLevel);
                    currentLevel.setStageStatusMessage(attrToText(start, "message"));
                    
                } else if (start.getName().equals("property")) {
                    currentLevel.addProperty(attrToText(start, "name"), attrToText(start, "value"));
                }
            } else if (event.isEndElement()) {
                EndElement end = (EndElement) event;
                if (end.getName().getLocalPart().equals("message-part") || end.getName().getLocalPart().equals("action-report")) {
                    currentLevel = currentLevel.getParent();
                }
            }
        }
        return topStatus;
    }
    
    private static DFDeploymentStatus.Status exitCodeToStatus(String exitCodeText) {
        return DFDeploymentStatus.Status.valueOf(exitCodeText);
    }
    
    private static String attrToText(StartElement e, String attrName) {
        Attribute a = e.getAttributeByName(QName.valueOf(attrName));
        return (a == null ? null : a.getValue());
    }

}
