/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.deployment.client;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author tjquinn
 */
public class CommandXMLResultParser {

    static DFDeploymentStatus parse(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory pf = SAXParserFactory.newInstance();
        SAXParser parser = pf.newSAXParser();
        
        
        
        DFDeploymentStatus topStatus = null;
        ResultHandler rh = new ResultHandler();
        parser.parse(is, rh);
        
        topStatus = rh.getTopStatus();
        
        return topStatus;
    }
    
    private static DFDeploymentStatus.Status exitCodeToStatus(String exitCodeText) {
        return DFDeploymentStatus.Status.valueOf(exitCodeText);
    }
    
    private static class ResultHandler extends DefaultHandler {

        private DFDeploymentStatus topStatus;
        private DFDeploymentStatus currentLevel;

        private String attrToText(Attributes attrs, String attrName) {
            return attrs.getValue(attrName);
        }
        
        private DFDeploymentStatus getTopStatus() {
            return topStatus;
        }
        
        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            if (localName.equals("action-report")) {
                currentLevel = topStatus = new DFDeploymentStatus();
                topStatus.setStageStatus(exitCodeToStatus(attrToText(attributes, "exit-code")));
                topStatus.setStageDescription(attrToText(attributes, "description"));
                String failureCause = attrToText(attributes, "failure-cause");
                if (failureCause != null) {
                    topStatus.setStageStatusMessage(failureCause);
                }
            } else if (localName.equals("message-part")) {
                currentLevel = new DFDeploymentStatus(currentLevel);
                currentLevel.setStageStatusMessage(attrToText(attributes, "message"));
            } else if (localName.equals("property")) {
                currentLevel.addProperty(attrToText(attributes, "name"), attrToText(attributes, "value"));
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (localName.equals("message-part") || localName.equals("action-report")) {
                currentLevel = currentLevel.getParent();
            }
        }
    }
}
