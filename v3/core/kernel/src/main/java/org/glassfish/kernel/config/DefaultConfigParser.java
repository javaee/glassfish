package org.glassfish.kernel.config;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.*;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.glassfish.api.admin.config.ConfigParser;
import org.glassfish.api.admin.config.Container;
import org.glassfish.config.support.GlassFishDocument;
import org.glassfish.config.support.GlassFishConfigBean;

import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.beans.PropertyVetoException;

import com.sun.hk2.component.InhabitantsScanner;
import com.sun.hk2.component.InhabitantsParser;
import com.sun.hk2.component.Holder;
import com.sun.logging.LogDomains;
import com.sun.enterprise.module.bootstrap.Populator;
import com.sun.enterprise.config.serverbeans.Config;

import javax.xml.stream.XMLStreamReader;

/**
 * @author Jerome Dochez
 */
@Service
public class DefaultConfigParser implements ConfigParser {

    @Inject(name="server-config") // for now
    Config config;

    Logger logger = Logger.getLogger(LogDomains.CORE_LOGGER);

    public Container parseContainerConfig(Habitat habitat, final URL configuration) throws IOException {


        org.jvnet.hk2.config.ConfigParser configParser = new org.jvnet.hk2.config.ConfigParser(habitat);
        // I don't use the GlassFish document here as I don't need persistence
        final DomDocument doc = new DomDocument(habitat) {
            public Dom make(final Habitat habitat, XMLStreamReader xmlStreamReader, Dom dom, ConfigModel configModel) {
                // by default, people get the translated view.
                return new GlassFishConfigBean(habitat,this, dom, configModel, xmlStreamReader);
            }
        };

        (new Populator() {

            public void run(org.jvnet.hk2.config.ConfigParser parser) {
                long now = System.currentTimeMillis();
                if (configuration != null) {
                    try {                        
                        DomDocument newElement = parser.parse(configuration,  doc);
                        logger.info(newElement.getRoot().getProxyType().toString());
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    Logger.getAnonymousLogger().fine("time to parse domain.xml : " + String.valueOf(System.currentTimeMillis() - now));
                }
            }
        }).run(configParser);

        // add the new container configuration to the server config
        final Container container = doc.getRoot().createProxy(Container.class);

        try {
            ConfigSupport.apply(new SingleConfigCode<Config>() {
                public Object run(Config config) throws PropertyVetoException, TransactionFailure {
                    config.getContainers().add(container);
                    return null;
                }
            }, config);
        } catch(TransactionFailure e) {
            logger.log(Level.SEVERE, "Cannot add new configuration to the Config element", e);
        }

        return  container;
    }

}
