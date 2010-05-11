package org.glassfish.admin.rest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class DomainTest extends RestTestBase {
    @Test
    public void testDomainGet() throws IOException {
        Map payload = new HashMap();
        Map<String, String> current = getEntityValues(get(BASE_URL+"/domain", "application/xml"));

        // Select a random locale so we're not setting the locale to its current value
        List<String> locales = new ArrayList<String>() {{
            add("en_US");
            add("en");
            add("de_DE");
            add("_GB");
            add("en_US_WIN");
            add("de__POSIX");
            add("fr__MAC");
        }};
        locales.remove(current.get("locale"));
        final int random = new Random().nextInt(locales.size());
        String newLocale = locales.get(random);

        payload.put("locale", newLocale);

        String response = post(BASE_URL+"/domain", payload, "application/xml");

        // Reload the domain and make sure our new locale was saved
        Map<String, String> map = getEntityValues(this.get(BASE_URL+"/domain", "application/xml"));
        assertEquals(newLocale, map.get("locale"));
    }
}
