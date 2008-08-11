package org.glassfish.kernel.config;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.DomDocument;
import org.glassfish.api.admin.config.ConfigParser;
import org.glassfish.api.admin.config.Container;
import org.glassfish.config.support.GlassFishDocument;

import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.hk2.component.InhabitantsScanner;
import com.sun.hk2.component.InhabitantsParser;
import com.sun.hk2.component.Holder;
import com.sun.logging.LogDomains;
import com.sun.enterprise.module.bootstrap.Populator;

/**
 * @author Jerome Dochez
 */
@Service
public class DefaultConfigParser implements ConfigParser {

    Logger logger = Logger.getLogger(LogDomains.CORE_LOGGER);

    public Container parseContainerConfig(Habitat habitat, final URL configuration) throws IOException {


        org.jvnet.hk2.config.ConfigParser configParser = new org.jvnet.hk2.config.ConfigParser(habitat);
        final DomDocument doc = habitat.getByType(GlassFishDocument.class);
        
        (new Populator() {

            public void run(org.jvnet.hk2.config.ConfigParser parser) {
                long now = System.currentTimeMillis();
                if (configuration != null) {
                    try {                        
                        parser.parse(configuration,  doc);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    Logger.getAnonymousLogger().fine("time to parse domain.xml : " + String.valueOf(System.currentTimeMillis() - now));
                }
            }
        }).run(configParser);

        return  doc.getRoot().createProxy(Container.class);
    }

}
