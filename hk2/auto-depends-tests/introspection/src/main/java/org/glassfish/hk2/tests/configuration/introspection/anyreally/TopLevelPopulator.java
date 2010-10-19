package org.glassfish.hk2.tests.configuration.introspection.anyreally;

import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.module.bootstrap.Populator;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.DomDocument;

import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Oct 18, 2010
 * Time: 9:51:15 PM
 * To change this template use File | Settings | File Templates.
 */
@Service
public class TopLevelPopulator implements Populator {

    @Override
    public void run(ConfigParser parser) throws BootException {
        URL url = getClass().getResource("test1.xml");
        System.out.println("URL = " + url);
        if (url==null) return;              
        DomDocument document = parser.parse(url);
    }
}
