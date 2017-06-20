package test;

import javax.ejb.*;
import javax.annotation.*;
import java.net.*;
import java.util.*;

@Stateless
public class HelloBean implements Hello {
    private static URL expectedURL;
    private static URL[] expectedURLs = new URL[4];

    static {
        try {
            expectedURL = new URL("http://java.net");
            for(int i = 0; i < expectedURLs.length; i++) {
                expectedURLs[i] = expectedURL;
            }
        } catch (MalformedURLException e) {
            //igore
        }
    }

    @Resource(name="java:module/env/url/url2", lookup="url/testUrl")
    private URL url2;

    @Resource(name="java:module/env/url/url1", lookup="java:module/env/url/url2")
    private URL url1;

    @Resource(lookup="java:module/env/url/url1")
    private URL url3;

    @Resource(mappedName="url/testUrl")
    private URL url4;

    public String injectedURL() {
        URL[] actualURLs = {url1, url2, url3, url4};
        if(Arrays.equals(expectedURLs, actualURLs)) {
            return ("Got expected " + Arrays.toString(actualURLs));
        } else {
            throw new EJBException("Expecting " + Arrays.toString(expectedURLs) + 
                ", actual " + Arrays.toString(actualURLs));
        }
    }

}
