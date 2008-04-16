/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.servlet.jsp.jstl.tlv;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.jsp.tagext.PageData;
import javax.servlet.jsp.tagext.TagLibraryValidator;
import javax.servlet.jsp.tagext.ValidationMessage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>A TagLibraryValidator for enforcing restrictions against
 * the use of JSP scripting elements.</p>
 * <p>This TLV supports four initialization parameters, for controlling
 * which of the four types of scripting elements are allowed or prohibited:</p>
 * <ul>
 * <li><b>allowDeclarations</b>: if true, indicates that declaration elements
 * are not prohibited.
 * <li><b>allowScriptlets</b>: if true, indicates that scriptlets are not
 * prohibited
 * <li><b>allowExpressions</b>: if true, indicates that top-level expression
 * elements (i.e., expressions not associated with request-time attribute
 * values) are not prohibited.
 * <li><b>allowRTExpressions</b>: if true, indicates that expression elements
 * associated with request-time attribute values are not prohibited.
 * </ul>
 * <p>The default value for all for initialization parameters is false,
 * indicating all forms of scripting elements are to be prohibited.</p>
 * 
 * @author <a href="mailto:mak@taglib.com">Mark A. Kolb</a>
 * @author Shawn Bayern (minor changes)
 */
public class ScriptFreeTLV extends TagLibraryValidator {
  private boolean allowDeclarations = false;
  private boolean allowScriptlets = false;
  private boolean allowExpressions = false;
  private boolean allowRTExpressions = false;
  private SAXParserFactory factory;

  /**
   * Constructs a new validator instance.
   * Initializes the parser factory to create non-validating, namespace-aware
   * SAX parsers.
   */
  public ScriptFreeTLV () {
    factory = SAXParserFactory.newInstance();
    factory.setValidating(false);
    factory.setNamespaceAware(true);
  }

  /**
   * Sets the values of the initialization parameters, as supplied in the TLD.
   * @param initParms a mapping from the names of the initialization parameters
   * to their values, as specified in the TLD.
   */
  public void setInitParameters (Map<String, Object> initParms) {
    super.setInitParameters(initParms);
    String declarationsParm = (String) initParms.get("allowDeclarations");
    String scriptletsParm = (String) initParms.get("allowScriptlets");
    String expressionsParm = (String) initParms.get("allowExpressions");
    String rtExpressionsParm = (String) initParms.get("allowRTExpressions");

    allowDeclarations = "true".equalsIgnoreCase(declarationsParm);
    allowScriptlets = "true".equalsIgnoreCase(scriptletsParm);
    allowExpressions = "true".equalsIgnoreCase(expressionsParm);
    allowRTExpressions = "true".equalsIgnoreCase(rtExpressionsParm);
  }

  /**
   * Validates a single JSP page.
   * @param prefix the namespace prefix specified by the page for the
   * custom tag library being validated.
   * @param uri the URI specified by the page for the TLD of the
   * custom tag library being validated.
   * @param page a wrapper around the XML representation of the page
   * being validated.
   * @return null, if the page is valid; otherwise, a ValidationMessage[]
   * containing one or more messages indicating why the page is not valid.
   */
  public ValidationMessage[] validate
      (String prefix, String uri, PageData page) {
    InputStream in = null;
    SAXParser parser;
    MyContentHandler handler = new MyContentHandler();
    try {
      synchronized (factory) {
	parser = factory.newSAXParser();
      }
      in = page.getInputStream();
      parser.parse(in, handler);
    }
    catch (ParserConfigurationException e) {
      return vmFromString(e.toString());
    }
    catch (SAXException e) {
      return vmFromString(e.toString());
    }
    catch (IOException e) {
      return vmFromString(e.toString());
    }
    finally {
      if (in != null) try { in.close(); } catch (IOException e) {}
    }
    return handler.reportResults();
  }

  /** 
   * Handler for SAX events. 
   * Four counters are provided as instance variables,
   * for counting occurrences of prohibited scripting elements.
   */
  private class MyContentHandler extends DefaultHandler {
    private int declarationCount = 0;
    private int scriptletCount = 0;
    private int expressionCount = 0;
    private int rtExpressionCount = 0;

    /** 
     * This event is received whenever a new element is encountered.
     * The qualified name of each such element is compared against
     * the names of any prohibited scripting elements. When found, the
     * corresponding counter is incremented.
     * If expressions representing request-time attribute values are
     * prohibited, it is also necessary to check the values of all
     * attributes specified by the element. (Trying to figure out
     * which attributes actually support request-time attribute values
     * and checking only those is far more trouble than it's worth.)
     */
    public void startElement (String namespaceUri, 
			      String localName, String qualifiedName,
			      Attributes atts) {
      if ((! allowDeclarations)
	  && qualifiedName.equals("jsp:declaration"))
	++declarationCount;
      else if ((! allowScriptlets)
	       && qualifiedName.equals("jsp:scriptlet"))
	++scriptletCount;
      else if ((! allowExpressions)
	       && qualifiedName.equals("jsp:expression"))
	++expressionCount;
      if (! allowRTExpressions) countRTExpressions(atts);
    }

    /**
     * Auxiliary method for checking attribute values to see if
     * are specified via request-time attribute values.
     * Expressions representing request-time attribute values are
     * recognized by their "%=" and "%" delimiters. When found, the
     * corresponding counter is incremented.
     */
    private void countRTExpressions (Attributes atts) {
      int stop = atts.getLength();
      for (int i = 0; i < stop; ++i) {
	String attval = atts.getValue(i);
	if (attval.startsWith("%=") && attval.endsWith("%"))
	  ++rtExpressionCount;
      }
    }

    /**
     * Constructs a String reporting the number(s) of prohibited
     * scripting elements that were detected, if any.
     * Returns null if no violations were found, making the result
     * of this method suitable for the return value of the
     * TagLibraryValidator.validate() method.
     * 
     * TODO:  The update from 7/13/2001 merely makes this validator
     * compliant with the new TLV API, but does not fully take advantage
     * of this API.  In the future, we should do so... but because
     * of the possibility that anti-script checking will be incorporated
     * into the base TLV, I've held off for now and just changed this
     * class to use the new API.  -- SB.
     */
    public ValidationMessage[] reportResults () {
      if (declarationCount + scriptletCount + expressionCount 
          + rtExpressionCount > 0) {
	StringBuffer results = new StringBuffer("JSP page contains ");
	boolean first = true;
	if (declarationCount > 0) {
	  results.append(Integer.toString(declarationCount));
	  results.append(" declaration");
	  if (declarationCount > 1) results.append('s');
	  first = false;
	}
	if (scriptletCount > 0) {
	  if (! first) results.append(", ");
	  results.append(Integer.toString(scriptletCount));
	  results.append(" scriptlet");
	  if (scriptletCount > 1) results.append('s');
	  first = false;
	}
	if (expressionCount > 0) {
	  if (! first) results.append(", ");
	  results.append(Integer.toString(expressionCount));
	  results.append(" expression");
	  if (expressionCount > 1) results.append('s');
	  first = false;
	}
	if (rtExpressionCount > 0) {
	  if (! first) results.append(", ");
	  results.append(Integer.toString(rtExpressionCount));
	  results.append(" request-time attribute value");
	  if (rtExpressionCount > 1) results.append('s');
	  first = false;
	}
	results.append(".");
	return vmFromString(results.toString());
      } else {
	return null;
      }
    }
  }


  // constructs a ValidationMessage[] from a single String and no ID
  private static ValidationMessage[] vmFromString(String message) {
    return new ValidationMessage[] {
      new ValidationMessage(null, message)
    };
  }

}
