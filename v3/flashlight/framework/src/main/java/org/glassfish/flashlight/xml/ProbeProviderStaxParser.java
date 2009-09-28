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
/**
 *
 * @author bnevins
 */
public class ProbeProviderStaxParser extends StaxParser{
    ProbeProviderStaxParser(File f) throws XMLStreamException {
        super(f);
    }

    ProbeProviderStaxParser(InputStream in) throws XMLStreamException {
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
        if(!parser.getLocalName().equals(PROBE_PROVIDER))
            throw new XMLStreamException("START_ELEMENT is supposed to be " + PROBE_PROVIDER +
                    ", found: " + parser.getLocalName());
        
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
        if(!parser.getLocalName().equals(PROBE))
            throw new XMLStreamException("START_ELEMENT is supposed to be " + PROBE +
                    ", found: " + parser.getLocalName());

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
        if(!parser.getLocalName().equals(PROBE_PARAM))
            throw new XMLStreamException("START_ELEMENT is supposed to be " + PROBE_PARAM +
                    ", found: " + parser.getLocalName());

        Map<String,String> atts = parseAttributes();

        return new ProbeParam(atts.get(PROBE_PARAM_NAME), atts.get(PROBE_PARAM_TYPE));
    }

    private List<Provider> providers = null;
}

