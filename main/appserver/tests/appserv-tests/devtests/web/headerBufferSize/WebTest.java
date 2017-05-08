/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
 */

/*
 * $Header: /usr/local/cvsroot/DI3-Port187/scriptroot/di/187/WEB-INF/src/it/telecomitalia/di/utility/URLCaller.java,v 1.1.1.1 2005/03/03 09:25:51 cvs Exp $
 * $Revision: 1.1.1.1 $
 * $Date: 2005/03/03 09:25:51 $
 *
 * ====================================================================
 *
 * Copyright (c) 2002 <a href="http://www.telecomitalia.it/">Telecom Italia</a>.
 * All rights reserved.
 *
 * URLCaller.java
 *
**/




import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import com.sun.ejte.ccl.reporter.*;

/*
 * Test for bug 2136042: bufferSize property doesn't work
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "headerBufferSize";

	// Request keys
    /** Hashtable key for headers property (type of corresponding value = Hashtable). */
    public static final String HEADERS = "headers";

    /** Hashtable key for method property (type of corresponding value = String). */
    public static final String METHOD = "method";

    /** Hashtable key for request parameters property (type of corresponding value = Hashtable). */
    public static final String REQUEST_PARAMS = "request_params";

	// Response keys
    /** Hashtable key for response code property (type of corresponding value = Integer). */
    public static final String RESPONSE_CODE = "response_code";

    /** Hashtable key for response message property (type of corresponding value = String). */
    public static final String RESPONSE_MESSAGE = "response_message";

    /** Hashtable key for response content property (type of corresponding value = String). */
    public static final String RESPONSE_CONTENT = "response_content";

	// Request and response keys
    /** Hashtable key for cookies property (type of corresponding value = String[]). */
    public static final String COOKIES = "cookies";

	// Values
    /** GET value for property method. */
    public static final String GET_METHOD = "GET";

    /** POST value for property method. */
    public static final String POST_METHOD = "POST";

	// Default values
    /** Default value for property method. */
    private static final String DEFAULT_METHOD = GET_METHOD;

    /** Default value for user-agent header. */
    private static final String DEFAULT_USER_AGENT_HEADER = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)";

    /** Default value for user-agent header. */
    private static final String DEFAULT_CONNECTION_HEADER = "close";

	// Static methods

    /**
     ** Main method for testing purposes.
     **
     ** @param args The command line arguments.
     **/
    public static void main(String[] args) {

        String host = args[0];
        String port = args[1];
        String contextRoot = args[2];
        
    	try {
	    	String[] testUrls = new String[1];
	    	testUrls[0] = "http://" + host  + ":" + port + contextRoot
                + "/jsp/test.jsp";
	    	String[] methods = new String[2];
	    	methods[0] = "GET";
	    	methods[1] = "GET";
	    	Hashtable[] paramsTables = new Hashtable[4];
	    	Hashtable params1 = new Hashtable();
	    	params1.put("test", "on");
	    	params1.put("num_tel", "065216464");
	    	Hashtable params2 = new Hashtable();
	    	params2.put("test", "on");
	    	params2.put("num_tel", "0577749059");
	    	Hashtable params3 = new Hashtable();
	    	params3.put("num_tel", "0652361456");
	    	Hashtable params4 = new Hashtable();
	    	String[] multipleParam = new String[3];
	    	multipleParam[0] = "1";
	    	multipleParam[1] = "2";
	    	multipleParam[2] = "3";
	    	params4.put("a", multipleParam);
	    	params4.put("b", "2");
	    	params4.put("c", "3");
	    	paramsTables[0] = params1;
	    	paramsTables[1] = params2;
	    	paramsTables[2] = params3;
	    	paramsTables[3] = params4;
	    	for(int i = 0; i < 1; i++) {
	    		Hashtable reqProperties = new Hashtable();
	    		reqProperties.put(METHOD, methods[i]);
	    		reqProperties.put(REQUEST_PARAMS, paramsTables[i]);
	    		System.out.println("TEST " + (i + 1) + ": " + testUrls[i] + " (" + methods[i] + ")");
	    		System.out.println("Parameters:");
		        for(Enumeration keys = (paramsTables[i]).keys(); keys.hasMoreElements(); ) {
		            String paramName = (String) keys.nextElement();
		            Object paramValues = (paramsTables[i]).get(paramName);
		            if(paramValues instanceof String[]) {
		            	// Multiple parameter values
		            	String[] paramValueList = (String[]) paramValues;
		            	for(int j = 0; j < paramValueList.length; j++) {
		            		String paramValue = paramValueList[j];
			            	if(j == 0)
			            		System.out.print(paramName + " = " + paramValue);
			            	else
			            		System.out.print(", " + paramValue);
			            }
			            System.out.println();
		            }
		            else {
		            	// Single parameter value
		            	String paramValue = (String) paramValues;
			            System.out.println(paramName + " = " + paramValue);
		            }
		        }


                String[] cookieList = new String[9];

                String cookie = "JSESSIONID=05036F40443C1A71F49BD583C29C7F01;";
                cookieList[0] = cookie;

                cookie = "kp=81.123.187.218.1132235747731803;";
                cookieList[1] = cookie;

                cookie = "advconnspeedhp=hpbroadband;";
                cookieList[2] = cookie;

                cookie = "ADVNdGFlashDUU=1;";
                cookieList[3] = cookie;

                cookie = "ADVOverlayerHPSessione=1;";
                cookieList[4] = cookie;

                cookie = "ADVNdgFlashHome=1;";
                cookieList[5] = cookie;


                cookie = "SUNRISE=00D4Kzo5hXUz0BPXqCF75Yv1llw09MvdUoarhg8KyESZbule2edair+WEl19pqibgsMqU2ibW28gtT63D7vsdMQuOCWOGRTJwkpQZRB7Dnn/wNguL0dMJkr/SNAqI+NRBR33un+Fxv2s4eqKXNw2n9pq+u5yYLQ8vSCCuznr/QO9rkwwuPzPOfgLR5NVLxe3h98QG36Xh/U4mX4joCphyAPhohoMUirDnb8qETxTZxOR4NTNTJlC4V/ntsV0h4Wg1IJRVgYk9+/TJ34ngT4YW6jZKfKcVJQMrE61V6VVbR3cFJ9NYB3eUoCV5wIKFVSwci+m9lhExj2WnSObcoOuOzl7Gk4PJWJtkJWLkI+XqzbH7vY4aUf5HW02EF+Kt/8wJVJVktcxzBHkZZR6cKJgLi97H0ASYL5sNJpF+1RN2OBQsSBZBjsdBbENA953YqZyjabD4C4O6kSIIFJkyvEbeDkA==;";
                cookieList[6] = cookie;

                cookie = "SUNRISE2=MFEvUlVTbzNNLXllbmItP3Z2KVA2diIkcVVRPD8pJEl8OjVHSF9mdUNZIWJjZSl7bXIhcFUwICI1L3pZPTNGaXplYWQqRXRISUt6PVNYLVVBZ0owaWY8P3ZsICozOERJcVVRanllS0A4NFVBKHkrNlQqb1VBKHllS0BmclVBazVlN0RfISo1QzpdZmQ5b2tIS0wrOUs+MXBCKCMxamFmRWswP1A1NkRaJTFCPDFlYXQtJ2NqW1ArZENRMDNNZFF3V3Vhb1UwKSl3TUA4JiVrIjp8K1kuR2dqX00rdFBhdy5OYzIhWXIlPnM0PiZzK3FVOGt4KXldYks+fVtXR0xyaFBWPFVBZzdbWHUnQmc0PkxxN3tGcVVRaih5VEA4Q2xBKHllS3s4b1VBPj5sTkA4b1VBKHl1LUA4b11CfT5bKXFAN3BXR0RCPVJGPFVBZ18mV29bRXcxQnllK0xdJDE1Lj9bVE59dWRYRkx6Zl84NGJaWVZ7Yns4b3hsZCo0d3pJJWtKRDpbZnIrb2tIS1FuIVJXMXFaWUp7bF9UK3VoaChoS0dfdVU0PHk/ZmMiW2wzRXozS0BXJlZBcXp1bW04QmdEKHk3ZkBDKVVFanllS0A4NFVBKHkrNlQqb1VBKHllS0BmclVBazVlN0QkKDFOQztAVE0iNnhBKE0kTEBncGtjUjoxa3EtK2tHKHk3aEBDKVVFPCs+V3A4b2tYOnl9UkBUMVVBKHllT0A4b1VJSTVoS0A4b1VBKCNES0A4TVZRSUwvaE9IeHJRWTMlbGwtP3dsKTNoS0dfdVU0PHk/ZmMiW2wzRXozS0BXJlZBcXp1bW04QmdEKHk3ZkBDKVVFanllS0A4NFVBKHkrNlQqb1VBKHllS0BmclVBazVlN0QkKD1sQy46UHN8W3hFKnl1aH04R2JBZCklbmY8RnNCRnllK1Q5b11CfT4+S3AqclVBSillcVY4czpBKHllS0c4b1VBTUk3V0A4b1VBKHllKV84bztIKEpyUlU/RG9UWit6ZEoib1UwUnplKls7JTVaRDtdWE07dVVBSi1lcVY4c2JNYytbS0A7L1pBazVlN0Q4b1VBKDJlS0A4d3dILXllS0A4b1VRcnllSzA5NHd6TjU1enxdJDJWaSlsS0AiPC1XRUd2S0BXOTZkViR8aVt8b1UwXy03K3wjJilaKHldTF8qQmdBKE0/Z1BZOWFUZi8xalotK3NMKHk3TkA4b1VBKXllS0ArRiZBKHllS0A4bypQKHkrcEA9RndLKHllS0dTJmFBKHllSyBBPDlYR0x2S0AmdFVBLU0zaSBKIWJTPDY4K0RfI2dsRC5AVE47fSVXR016eEJEIXJZVy9mS28tdnh6UjR4dkRdIy5KRDp4M0A4Q2RqSk9mUls4NHJaWjMlZHEtL3NNPy0zOUwhclVRVD4kYnQnLS1rKXl1aFBTPGJWZSl7bUxEb1UwQnllS0A4c1VBKHltbSAvb1VBKHllS0dob1VBcXp1bXBmdXZwRk81OFtacFVRPEBsS1dCb1lISEx2Z089b1VkKHllbmI5L3kwIHllK3pHN1dBKD8jUHMgezwySkh2UV04NDpBKHllS0c4b1VBTUk3V0A4b1VBKHllKV84bztIKEpyaFBXOTZjLnllbns4b1VBKHplS0A4cTp4KHllS0A4b1UwfHllcVY4czpjUjcxblpbRXdHKHk3TEA4b1VBKXllS0ArRiZBKHllS0A4bypQY19samlmdXV6Qic2eUhfcVVrUT48YnR7LXRFRl9CUVIobzBSKnk+S0gvPlpEPEV7Uks4JTExLjtbUH0+fSVXR016eEJEIXJZVzNmS28tL1ZBZz0tKW09ODZNZi99bXAqR2N6XUppZEY/NTVKbDIlWFk8N2loRkxxK0BGMDVSOyskckoqOmxERl8vZEVHPG5ZUDclam49N2pEP051Zw==;";
                cookieList[7] = cookie;

                cookie = "CALIENUP2=WOAbedUzeH8HyTjUGXBwowuDOEKVWI9WKpakM9Z8I35M5hBaz6+/iXs6gBJ5gjF3UJMaa7mKGY389Z6HX9tyIvCijfZ51xmAxl6XFvIu83SUZP8qKeNlk0eze6ykBdgggtro7jMvMF91I5HaImRXn04atBjG86omlBnkvTHbAr3mfj5t6D9pt5J7i3vNcdZxzrg07sC/PEG9MO+xMXJ/vbfI4sC0ZUOmDfz8nlH54Ft3sj61V8sP9k20Lq5jxjnT0og/PG8NfOZvyx+Q76HrI1ipYVF/+K0s4O+dHC/SMW9IPxUMtPGAVAUjDWmNz5hWlHqJm694aDlo9N3sP0MrzyOrhqnGDWEs+wJcNVmx/pdxVZquzS9HdGQpGyr1FTPVrkFtMqBIyko8AnOe/sDjLmYzeKW9vF2iQSpibbfa2bMiqsNuBtXS8VLKIzxeJD/6lIe0CP0j5IfjCB/ZrjlJresS5hzTnGDIzVUZsD4g9+DUaKzcLHjqJ+ETvQKDgiqjMnPZnvj9b29ulpi/eDX/JBPULQjaKKTtmLunIFrLbHfImRv9GMK4ZvC4EQQBS3O+gnw8Q0ege4ktdBlJR2QFZDzeHEw6rSAfNrQSE03GL1hW3AdUzA5Yolc6NpUZPS0afDlgwhQFCWCknyDGR1wq3rggDczHTQ8A55Bf/WINEeRnX/DD5GVhyu+XKwM7CfK2YgPl0TssD//XRw8RVMSI97mGnlVuaRE03+oBgFLaG35l4qLoU2YdRICZK7ZBh/a6o46312hCe2CauLC9phBGmx79kosJ2opRvpqaOjIwVbneauQUqKZbUIRwnLZ2lGubtqt3L9xCrL17ClHtXdW/jn0l7J4Gsd1NmVXcTDjdp1A12b/HpuAsq8dneQmXG4FF8IF3Xw8DdxMBC+c6vdLWeHNU/iI7Cd8zCp03LPrq0qYlWMYSBXX7XJvrN1NrxUwpIn+7s8QRBaIrKTNu4CA+r8d13AQKUwHWBlQDfde9dAfuhxeAF78Ar8SUM3SxJ7j+o+/OZFsf6tRK7yy7CWv5vhrNIvvw3CPuYL4ujmSvMQbKInJOI09wnMPAUnXUbDicrW03IkJ1Tv4jfkpRXiG9Itqk3w278qfxRDOjB19K36zxa53k5GnQ4iYOtq/8jMYhguwnUVgExDZD2DACyPLaM1bfoDvkhDuggafqTduoRZ0FKggVzDPjMsRm2V6A5VhEk1REU+1AlgFcGzNZRqx0sNwXoJvpWqGRpaNXIBzMqjDJi55NQsp6APV+W5U+05PyPrphzUSZjIzXeZAUgLbfx6QQWtYNivLjh5S0uYlo0uWL4EzsqFiLltvJNMSiIlnCVZ8hv19+6P7g9wsds9NmNcyvzylInbDtk3b+Bvbp38B1TMy8hr6E6AW9Iid5bKzsXA/aHL9SDDW4xD39+BjnGTip7FecmVgMg0qtuZ+57SUUcVgBcCVPW2ujPjulFh0R7ZRL+kxztJtPT3NKHydcH+1OXH/wIFcwGvzImDR318Olsw=";
                cookieList[8] = cookie;


                reqProperties.put(COOKIES, cookieList);

	    		Hashtable result = call(testUrls[i], reqProperties);
	    		System.out.println("Result:");
	    		System.out.println( "Response Code = " + ( (Integer) result.get(RESPONSE_CODE) ).toString() );
	    		System.out.println( "Response Message = " + (String) result.get(RESPONSE_MESSAGE) );
	    		System.out.println("Response Content:");
	    		System.out.println( (String) result.get(RESPONSE_CONTENT) );
	    		String[] setCookies = (String[]) result.get(COOKIES);
	    		if( (setCookies != null) && (setCookies.length > 0) ) {
	    			System.out.println("Cookies set by Server:");
	    			for(int j = 0; j < setCookies.length; j++) {
	    				System.out.println("Cookie " + (j + 1) + " = " + setCookies[j]);
	    			}
	    		}
	    		System.out.println();
	    		System.out.println("-------------------------------------------");
	    		System.out.println();
	    		System.out.println();

                if ( ((Integer)result.get(RESPONSE_CODE)) == 200){
                    stat.addStatus(TEST_NAME, stat.PASS);
                } else {
                    stat.addStatus(TEST_NAME, stat.FAIL);
                }
	    	}
	    } catch(Exception exc) {
	    	System.err.println( "Exception occurred: " + exc.getMessage() );
	    	exc.printStackTrace(System.err);
            stat.addStatus(TEST_NAME, stat.FAIL);
	    }
        stat.printSummary(TEST_NAME);
    }

    /**
     ** Static method for performing an HTTP request to the specified URL.<br>
     ** This method requires as second parameter a non-empty Hashtable, that should specify the URL request properties.<br>
     ** If the Hashtable is null, a MalformedUrlException is generated.<br>
     ** The Hashtable can contain the following key/values properties:<br>
     ** <ul>
     ** <li><b>HEADERS</b> (key = &quot;headers&quot;): you can specify optional additional headers in this field.<br>
     ** The corresponding value should be an Hashtable with name/value pairs for each header to add.<br>
     ** If not present the &quot;User-Agent&quot; header assumes the default value of
     ** &quot;<code>Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0; T312461)</code>&quot;.<br>
     ** If not present the &quot;Connection&quot; header assumes the default value of &quot;<code>close</code>&quot;.</li>
     ** <li><b>METHOD</b> (key = &quot;method&quot;): you can specify which method to use for URL requests between
     ** &quot;GET&quot; and &quot;POST&quot; are the only ones supported for now.</li>
     ** <li><b>REQUEST PARAMETERS</b> (key = &quot;request_params&quot;): you can specify the parameters
     ** to add to the HTTP request in this field.<br> The corresponding value should be an Hashtable
     ** with name/value pairs for each parameter to add.<br> For multiple parameters use a String[] (string array)
     ** as value.</li>
     ** <li><b>COOKIES</b> (key = &quot;method&quot;): you can specify optional cookies to set in the
     ** HTTP request in this field.<br> The corresponding value should be a String[] (string array).</li>
     ** </ul>
     ** This method returns an Hashtable containing the response properties.<br>
     ** The Hashtable can contain the following key/values properties:<br>
     ** <ul>
     ** <li><b>RESPONSE CODE</b> (key = &quot;response_code&quot;): an Integer containing the HTTP response code,
     ** e.g. 404.</li>
     ** <li><b>RESPONSE MESSAGE</b> (key = &quot;response_message&quot;): a String containing the complete
     ** response message, e.g. <code>HTTP/1.1 404 Not Found<(code>.</li>
     ** <li><b>RESPONSE CONTENT</b> (key = &quot;response_content&quot;): a String containing the full response
     ** content of the requested URL, e.g. the entire HTML page.</li>
     ** <li><b>COOKIES</b> (key = &quot;method&quot;): a String[] (string array) with all the cookies, that the
     ** Server asks to set on the client.</li>
     ** </ul>
     **
     ** @param url The URL to call without parameters.
     ** @param requestProperties The URL to call without parameters.
     ** @return Hashtable The table of the response properties.
     ** @throws MalformedURLException if the URL is not well formed or the properties Hashtable is null
     ** @throws IOException if an I/O exception occurs during the HTTP request
     **/
    public static Hashtable call(String url, Hashtable requestProperties)
    		throws MalformedURLException, IOException {
		if(url == null)
			throw new MalformedURLException("URL address cannot be null!");
		if(requestProperties == null)
			throw new MalformedURLException("URL request properties cannot be null!");
		// else
		Hashtable headers = (Hashtable) requestProperties.get(HEADERS);
		if(headers == null)
			headers = new Hashtable();
		if( ( ( (String) headers.get("User-Agent") ) == null ) || ( ( (String) headers.get("User-Agent") ).equals("") ) )
			headers.put("User-Agent", DEFAULT_USER_AGENT_HEADER);
		if( ( ( (String) headers.get("Connection") ) == null ) || ( ( (String) headers.get("Connection") ).equals("") ) )
			headers.put("Connection", DEFAULT_CONNECTION_HEADER);
		String method = (String) requestProperties.get(METHOD);
		if( (method == null) || !( method.equalsIgnoreCase(GET_METHOD) || method.equalsIgnoreCase(POST_METHOD) ) )
			method = DEFAULT_METHOD;
		Hashtable requestParams = (Hashtable) requestProperties.get(REQUEST_PARAMS);
		if(requestParams == null)
			requestParams = new Hashtable();
		String[] cookies = (String[]) requestProperties.get(COOKIES);
		if(cookies == null)
			cookies = new String[0];
		if( method.equalsIgnoreCase(GET_METHOD) )
			return( doGetCall(url, headers, cookies, requestParams) );
		else
			return( doPostCall(url, headers, cookies, requestParams) );
	}

    /**
     ** Static protected method for performing GET HTTP request to the specified URL.<br>
     ** See the calling method for details on the parameters.<br>
     **
     ** @param url The URL to call without parameters.
     ** @param headers The request headers to set, organized in a table.
     ** @param headers The list of the cookies to set in the request.
     ** @param headers The request parameters to send in the query string, organized in a table.
     ** @return Hashtable The table of the response properties.
     ** @throws MalformedURLException if the URL is not well formed
     ** @throws IOException if an I/O exception occurs during the HTTP request
     **/
    protected static Hashtable doGetCall(String url, Hashtable headers, String[] cookies, Hashtable requestParams)
			throws MalformedURLException, IOException {
        // Set parameters

        StringBuffer queryString = new StringBuffer();
        for(Enumeration keys = requestParams.keys(); keys.hasMoreElements(); ) {
            String paramName = (String) keys.nextElement();
            Object paramValues = requestParams.get(paramName);
            if(paramValues instanceof String[]) {
            	// Multiple parameter values
            	String[] paramValueList = (String[]) paramValues;
            	for(int i = 0; i < paramValueList.length; i++) {
            		String paramValue = paramValueList[i];
	            	if(queryString.length() == 0)
	            		queryString.append("?");
	            	else
	            		queryString.append("&");
	            	queryString.append( URLEncoder.encode(paramName,"ISO8859-1") + "=" + URLEncoder.encode(paramValue,"ISO8859-1") );
	            }
            }
            else {
            	// Single parameter value
            	String paramValue = (String) paramValues;
            	if(queryString.length() == 0)
            		queryString.append("?");
            	else
            		queryString.append("&");
            	queryString.append( URLEncoder.encode(paramName,"ISO8859-1") + "=" + URLEncoder.encode(paramValue,"ISO8859-1") );
            }
        }
        URL urlToCall = new URL( url + queryString.toString() );
        HttpURLConnection urlConnection = (HttpURLConnection) urlToCall.openConnection();
        urlConnection.setInstanceFollowRedirects(true);
        urlConnection.setUseCaches(false);
        StringBuffer cookiesList = new StringBuffer();
        // Set headers
        for(Enumeration keys = headers.keys(); keys.hasMoreElements(); ) {
            String headerName = (String) keys.nextElement();
            String headerValue = (String) headers.get(headerName);
            if( headerName.equalsIgnoreCase("Cookie") )
            	cookiesList.append(headerValue);
            else
        		urlConnection.setRequestProperty(headerName, headerValue);
        }
        // Set cookies
        for(int i = 0; i < cookies.length; i++) {
        	if(cookiesList.length() > 0)
        		cookiesList.append("," + cookies[i]);
        	else
        		cookiesList.append(cookies[i]);
        }
        urlConnection.setRequestProperty( "Cookie", cookiesList.toString() );
        // Setting GET method
        urlConnection.setDoOutput(false);
        urlConnection.setRequestMethod(GET_METHOD);
        // Connect
        urlConnection.connect();
        // Read the response
        Hashtable result = new Hashtable();
        BufferedReader responseReader = new BufferedReader( new InputStreamReader( urlConnection.getInputStream() ) );
        // Read the response content
        StringBuffer responseContent = new StringBuffer();
        String lineSeparator = System.getProperty("line.separator");
        String inputLine = null;
        while ( ( inputLine = responseReader.readLine() ) != null) {
            responseContent.append(inputLine + lineSeparator);
        }
        result.put( RESPONSE_CONTENT, responseContent.toString() );
        // Get response code
        result.put( RESPONSE_CODE, new Integer( urlConnection.getResponseCode() ) );
        // Get response message
        String responseMessage = urlConnection.getResponseMessage();
        if(responseMessage != null)
        	result.put(RESPONSE_MESSAGE, responseMessage);
        // Receive cookies
        String returnedCookiesStr = urlConnection.getHeaderField("Set-Cookie");
        if(returnedCookiesStr != null) {
        	StringTokenizer commaTokenizer = new StringTokenizer(returnedCookiesStr, ",", false);
        	int tokens = commaTokenizer.countTokens();
        	if(tokens > 0) {
	        	String[] returnedCookies = new String[tokens];
	        	int z = 0;
	        	while( commaTokenizer.hasMoreTokens() ) {
	        		returnedCookies[z] = commaTokenizer.nextToken();
	        		z++;
				}
	        	result.put(COOKIES, returnedCookies);
	        }
        }
        responseReader.close();
        return(result);
    }

    /**
     ** Static protected method for performing POST HTTP request to the specified URL.<br>
     ** See the calling method for details on the parameters.<br>
     **
     ** @param url The URL to call without parameters.
     ** @param headers The request headers to set, organized in a table.
     ** @param cookies The list of the cookies to set in the request.
     ** @param requestParams The request parameters to send in the body of the request, organized in a table.
     ** @return Hashtable The table of the response properties.
     ** @throws MalformedURLException if the URL is not well formed
     ** @throws IOException if an I/O exception occurs during the HTTP request
     **/
    protected static Hashtable doPostCall(String url, Hashtable headers, String[] cookies, Hashtable requestParams)
			throws MalformedURLException, IOException {
        URL urlToCall = new URL(url);
        HttpURLConnection urlConnection = (HttpURLConnection) urlToCall.openConnection();
        urlConnection.setInstanceFollowRedirects(true);
        urlConnection.setUseCaches(false);
        StringBuffer cookiesList = new StringBuffer();
        // Set headers
        for(Enumeration keys = headers.keys(); keys.hasMoreElements(); ) {
            String headerName = (String) keys.nextElement();
            String headerValue = (String) headers.get(headerName);
            if( headerName.equalsIgnoreCase("Cookie") )
            	cookiesList.append(headerValue);
            else
        		urlConnection.setRequestProperty(headerName, headerValue);
        }
        // Set cookies
        for(int i = 0; i < cookies.length; i++) {
        	if(cookiesList.length() > 0)
        		cookiesList.append("," + cookies[i]);
        	else
        		cookiesList.append(cookies[i]);
        }
        urlConnection.setRequestProperty( "Cookie", cookiesList.toString() );
        // Setting POST method
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod(POST_METHOD);
        // Connect
        urlConnection.connect();
        // Write parameters in the request body
        PrintWriter out = new PrintWriter( urlConnection.getOutputStream() );
        int writtenParams = 0;
        for(Enumeration keys = requestParams.keys(); keys.hasMoreElements(); ) {
            String paramName = (String) keys.nextElement();
            Object paramValues = requestParams.get(paramName);
            if(paramValues instanceof String[]) {
            	// Multiple parameter values
            	String[] paramValueList = (String[]) paramValues;
            	for(int i = 0; i < paramValueList.length; i++) {
            		String paramValue = paramValueList[i];
		            if(writtenParams > 0) {
		            	out.print( "&" + URLEncoder.encode(paramName,"ISO8859-1") + "=" + URLEncoder.encode(paramValue,"ISO8859-1") );
		            }
		            else {
		            	out.print( URLEncoder.encode(paramName,"ISO8859-1") + "=" + URLEncoder.encode(paramValue,"ISO8859-1") );
		            }
            		writtenParams++;
	            }
            }
            else {
            	// Single parameter value
            	String paramValue = (String) paramValues;
	            if(writtenParams > 0) {
	            	out.print( "&" + URLEncoder.encode(paramName,"ISO8859-1") + "=" + URLEncoder.encode(paramValue,"ISO8859-1") );
	            }
	            else {
	            	out.print( URLEncoder.encode(paramName,"ISO8859-1") + "=" + URLEncoder.encode(paramValue,"ISO8859-1") );
	            }
        		writtenParams++;
            }
        }
        out.close();
        // Read the response
        Hashtable result = new Hashtable();
        BufferedReader responseReader = new BufferedReader( new InputStreamReader( urlConnection.getInputStream() ) );
        // Read the response content
        StringBuffer responseContent = new StringBuffer();
        String lineSeparator = System.getProperty("line.separator");
        String inputLine = null;
        while ( ( inputLine = responseReader.readLine() ) != null) {
            responseContent.append(inputLine + lineSeparator);
        }
        result.put( RESPONSE_CONTENT, responseContent.toString() );
        // Get response code
        result.put( RESPONSE_CODE, new Integer( urlConnection.getResponseCode() ) );
        // Get response message
        String responseMessage = urlConnection.getResponseMessage();
        if(responseMessage != null)
        	result.put(RESPONSE_MESSAGE, responseMessage);
        // Receive cookies
        String returnedCookiesStr = urlConnection.getHeaderField("Set-Cookie");
        if(returnedCookiesStr != null) {
        	StringTokenizer commaTokenizer = new StringTokenizer(returnedCookiesStr, ",", false);
        	int tokens = commaTokenizer.countTokens();
        	if(tokens > 0) {
	        	String[] returnedCookies = new String[tokens];
	        	int z = 0;
	        	while( commaTokenizer.hasMoreTokens() ) {
	        		returnedCookies[z] = commaTokenizer.nextToken();
	        		z++;
				}
	        	result.put(COOKIES, returnedCookies);
	        }
        }
        responseReader.close();
        return(result);
    }

}
