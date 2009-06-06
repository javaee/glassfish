/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package quickie;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

/**
 *
 * @author bnevins
 */
public class RestTest {
    public static void main(String[] args) {
        URL rootUrl = null;
        String rootUrlString = "";

        try {
            if(args.length > 0)
                rootUrlString = args[0];
            else
                rootUrlString = "http://localhost:4848/rest-resources/domain";

            rootUrl = new URL(rootUrlString);
        }
        catch(Exception e) {
            System.out.println("Bad URL: " + rootUrlString);
            System.exit(1);
        }

        System.out.println("****   TESTING " + rootUrlString + "\n");
        RestTest tester = new RestTest();
        tester.checkLinks(rootUrl);
        System.out.println("GOOD: " + tester.good.size());
        System.out.println("BAD: " + tester.bad.size());
    }

    // given a URL -- check all the links on the page...
    private void checkLinks(URL parent) {
        String page = readLink(parent);

        if(page != null) {
            good.add(parent);
        }
        else {
            bad.add(parent);
            System.out.println("BAD URL: " + parent);
            return;
        }

        List<URL> links = getChildLinks(page);

        for(URL url : links) {
            checkLinks(url);
        }
    }

    private String readLink(URL url) {
        // read in the page to a String and return it
        Reader in = null;
        final int len = 16384;
        final char[] buf = new char[len];
        final StringBuilder sb = new StringBuilder();

        try {
            in = new BufferedReader(new InputStreamReader(url.openStream()));
            int numRead;

            while((numRead = in.read(buf, 0, len)) > 0) {
                sb.append(buf, 0, numRead);
            }
            return sb.toString();
        }
        catch (IOException ex) {
            return null;
        }
        finally {
            // nonsense JSE stuff.  Put it on one line...
            if(in != null) try { in.close(); } catch(Exception e) { /*ignore*/ }
        }
    }

    private List<URL> getChildLinks(String page) {
        // parse out the links found in this URL and return them.
        // return an empty List if there are none...

        final String startToken = "<a href=";
        String[] linkStrings = page.split(startToken);
        List<URL> links = new LinkedList<URL>();

        // the first String is NEVER a link
        for(int i = 1; i < linkStrings.length; i++) {
            try {
                links.add(cleanLink(linkStrings[i]));
            }
            catch (MalformedURLException ex) {
                System.out.println("Bad Link in Page: " + linkStrings[i]);
            }
        }
        
        return links;
    }

    private URL cleanLink(String s) throws MalformedURLException {
        int index = s.indexOf(">");

        if(index > 0)
            s = s.substring(0, index);

        return new URL(s);
    }

    private ArrayList<URL> good = new ArrayList<URL>();
    private ArrayList<URL> bad  = new ArrayList<URL>();
}
