package org.glassfish.admin.rest;

import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class DomainTest extends RestTestBase {
    /*
    @Test
    public void testDomainGet1() throws InputException, IOException {
        final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3);
        webClient.addRequestHeader("Accept", "text/html");
        webClient.addRequestHeader("media-type", "text/html");
        final HtmlPage page = (HtmlPage) webClient.getPage(BASE_URL + "/domain");

        setFieldValue(page, "locale", "en_US");
        submitForm(page, "Update");


        HtmlPage submittedForm = (HtmlPage) webClient.getPage(BASE_URL + "/domain");
        assertEquals(getFieldValue(submittedForm, "locale"), "en_US");
    }

    protected HtmlSubmitInput submitForm(HtmlPage page, String buttonValue) throws IOException {
        HtmlForm form = page.getForms().get(0);
        final HtmlSubmitInput button = (HtmlSubmitInput) form.getInputsByValue(buttonValue).get(0);
        button.click();
        return button;
    }
    */

    @Test
    public void testDomainGet() throws IOException {
        Map payload = new HashMap();
        Map<String, String> current = getEntityValues(this.get(BASE_URL+"/domain", "application/xml"));

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

        String response = this.post(BASE_URL+"/domain", payload, "application/xml");

        // Reload the domain and make sure our new locale was saved
        Map<String, String> map = getEntityValues(this.get(BASE_URL+"/domain", "application/xml"));
        assertEquals(newLocale, map.get("locale"));
    }
}