package taglib;

import java.io.InputStream;
import java.io.IOException;

import javax.servlet.jsp.tagext.PageData;
import javax.servlet.jsp.tagext.TagLibraryValidator;
import javax.servlet.jsp.tagext.ValidationMessage;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MyValidator extends TagLibraryValidator {

    public ValidationMessage[] validate(String prefix, String uri,
                                        PageData page) {

	/*
        StringBuffer sb = new StringBuffer();

        sb.append("---------- Prefix=" + prefix + " URI=" + uri +
                  "----------\n");

        InputStream is = page.getInputStream();
        while (true) {
            try {
                int ch = is.read();
                if (ch < 0) {
                    break;
                }
                sb.append((char) ch);
            } catch (IOException e) {
                break;
            }
        }
        sb.append("-----------------------------------------------");
        System.out.println(sb.toString());
	*/

        try {
	    DefaultHandler h = new DefaultHandler();

	    // parse the page
	    SAXParserFactory f = SAXParserFactory.newInstance();
	    f.setValidating(false);
	    f.setNamespaceAware(true);
	    SAXParser p = f.newSAXParser();
	    p.parse(page.getInputStream(), h);

	} catch (SAXException ex) {
	    return vmFromString(ex.toString());
	} catch (ParserConfigurationException ex) {
	    return vmFromString(ex.toString());
	} catch (IOException ex) {
	    return vmFromString(ex.toString());
	}

        return null;
    }

    // constructs a ValidationMessage[] from a single String and no ID
    private static ValidationMessage[] vmFromString(String message) {
	return new ValidationMessage[] {
	    new ValidationMessage(null, message)
	};
    }

}
