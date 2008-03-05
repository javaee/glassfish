package com.sun.enterprise.configapi.tests;

import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.HttpService;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * User: Jerome Dochez
 * Date: Mar 4, 2008
 * Time: 2:44:59 PM
 */
public class Ssl2EnabledTest extends ConfigApiTest {


    public String getFileName() {
        return "DomainTest";
    }

    HttpService service = null;

    @Before
    public void setup() {
        service = getHabitat().getComponent(HttpService.class);
        assertTrue(service!=null);

    }

    @Test
    public void sslEnbaledTest() {
        for (HttpListener listener : service.getHttpListener()) {
            if (listener.getSsl()!=null) {
                try {
                    logger.fine("SSL2 ENABLED = " + listener.getSsl().getSsl2Enabled());
                    assertTrue(Boolean.parseBoolean(listener.getSsl().getSsl2Enabled()));
                } catch(Exception e) {
                     e.printStackTrace();
                }
            }
        }
    }
}
