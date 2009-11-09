/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.xml;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import static org.glassfish.flashlight.xml.XmlConstants.*;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.LocalStringManagerImpl;

/**
 * Read the XML file, parse it and return a list of ProbeProvider objects
 * @author bnevins
 */
public class ProbeProviderStaxParser extends StaxParser{

    private static final Logger logger =
        LogDomains.getLogger(ProbeProviderStaxParser.class, LogDomains.MONITORING_LOGGER);
    public final static LocalStringManagerImpl localStrings =
                            new LocalStringManagerImpl(ProbeProviderStaxParser.class);

    public ProbeProviderStaxParser(File f) throws XMLStreamException {
        super(f);
    }

    public ProbeProviderStaxParser(InputStream in) throws XMLStreamException {
        super(in);
    }

    public List<Provider> getProviders() {
        if(providers == null) {
            try {
                read();
            } 
            catch(Exception ex) {
                // normal 
                close();
            }
        }
        if (providers.isEmpty()) {
            // this line snatched from the previous implementation (DOM)
            logger.log(Level.SEVERE, "noProviderIdentifiedFromXML");
        }

        return providers;
    }

    @Override
    protected void read() throws XMLStreamException, EndDocumentException{
        providers = new ArrayList<Provider>();
        // move past the root -- "probe-providers".
        skipPast("probe-providers");

        while(true) {
            providers.add(parseProbeProvider());
        }
    }

    private Provider parseProbeProvider() throws XMLStreamException {
        if(!parser.getLocalName().equals(PROBE_PROVIDER)) {
            String errStr = localStrings.getLocalString("invalidStartElement",
                                "START_ELEMENT is supposed to be {0}" +
                                ", found: {1}", PROBE_PROVIDER, parser.getLocalName());
            throw new XMLStreamException(errStr);
        }
        
        Map<String,String> atts = parseAttributes();
        List<Probe> probes = parseProbes();

        return new Provider(atts.get(MODULE_PROVIDER_NAME),
                            atts.get(MODULE_NAME),
                            atts.get(PROBE_PROVIDER_NAME),
                            atts.get(PROBE_PROVIDER_CLASS),
                            probes );
    }

    private List<Probe> parseProbes() throws XMLStreamException {
        List<Probe> probes = new ArrayList<Probe>();

        boolean done = false;
        while(!done) {
            try {
                nextStart();

                if(parser.getLocalName().equals(PROBE))
                    probes.add(parseProbe());
                else
                    done = true;
            }
            catch (EndDocumentException ex) {
                // ignore -- this must be the last START_ELEMENT in the doc
                // that's normal
                done = true;
            }
        }
        return probes;
    }

    private Probe parseProbe() throws XMLStreamException {
        if(!parser.getLocalName().equals(PROBE)) {
            String errStr = localStrings.getLocalString("invalidStartElement",
                                "START_ELEMENT is supposed to be {0}" +
                                ", found: {1}", PROBE, parser.getLocalName());
            throw new XMLStreamException(errStr);
        }

        // for some unknown reason method is an element not an attribute
        // Solution -- use the last item if there are more than one

        List<ProbeParam> params = new ArrayList<ProbeParam>();
        Map<String,String> atts = parseAttributes();
        String method = null;
        String name = atts.get(PROBE_NAME);
        boolean self = Boolean.parseBoolean(atts.get(PROBE_SELF));
        boolean hidden = Boolean.parseBoolean(atts.get(PROBE_HIDDEN));


        boolean done = false;
        while(!done) {
            try {
                nextStart();
                String localName = parser.getLocalName();

                if(localName.equals(METHOD))
                    method = parser.getElementText();
                else if(localName.equals(PROBE_PARAM))
                    params.add(parseParam());
                else
                    done = true;
            }
            catch (EndDocumentException ex) {
                // ignore -- possibly normal -- but stop!
                done = true;
            }
        }
        return new Probe(name, method, params, self, hidden);
    }
    private ProbeParam parseParam() throws XMLStreamException {
        if(!parser.getLocalName().equals(PROBE_PARAM)){
            String errStr = localStrings.getLocalString("invalidStartElement",
                                "START_ELEMENT is supposed to be {0}" +
                                ", found: {1}", PROBE_PARAM, parser.getLocalName());
            throw new XMLStreamException(errStr);
        }

        Map<String,String> atts = parseAttributes();

        return new ProbeParam(atts.get(PROBE_PARAM_NAME), atts.get(PROBE_PARAM_TYPE));
    }

    private List<Provider> providers = null;
}

