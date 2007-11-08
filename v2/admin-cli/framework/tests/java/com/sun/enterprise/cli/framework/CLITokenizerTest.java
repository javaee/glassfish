/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 */

package com.sun.enterprise.cli.framework;

/**
   Note that this test requires resources for testing. These resources
   are construct4ed from the two files P1 & P2 located in the current
   directory. If these file names are changed then the corresponding
   names in this submodules build.xml file should be changed also
*/
import junit.framework.*;
import junit.textui.TestRunner;

/**
 *
 * @author jane.young@sun.com
 * @version $Revision: 1.4 $
 */

/**
   Execute these tests using gmake (and Ant) by:
   cd <framework>
   gmake ANT_TARGETS=CLITokenizerTest
*/

public class CLITokenizerTest extends TestCase {

  // test simple token with ":" as the delimiter
  public void testSimpleTokens() throws Exception{

      CLITokenizer cliTokenizer = new CLITokenizer("name1=value1:name2=value2:name3=value3", ":");

      assertEquals("number of tokens is 3", 3, cliTokenizer.countTokens());
      assertEquals("first token is name1=value1", "name1=value1", cliTokenizer.nextToken());
      assertEquals("second token is name2=value2", "name2=value2", cliTokenizer.nextToken());
      assertEquals("hasMoreToken should be true, since there is still one more token", true, cliTokenizer.hasMoreTokens());
      assertEquals("third token is name3=value3", "name3=value3", cliTokenizer.nextToken());
      assertEquals("hasMoreToken should be false, since there are no more tokens", false, cliTokenizer.hasMoreTokens());
  }

  // test tokens with quotes and  ":" as the delimiter
  public void testTokensWithQuotes() throws Exception{

      CLITokenizer cliTokenizer = new CLITokenizer("\"name1:123\"=value1:name2=\"value2:xyz\":\"name3=:value3\":name4=value4", ":");

      assertEquals("number of tokens is 4", 4, cliTokenizer.countTokens());
      assertEquals("first token is name1:123=value1", "name1:123=value1", cliTokenizer.nextTokenWithoutEscapeAndQuoteChars());
      assertEquals("second token is name2=\"value2:xyz\"", "name2=\"value2:xyz\"", cliTokenizer.nextToken());
      assertEquals("hasMoreToken should be true, since there is still two more token", true, cliTokenizer.hasMoreTokens());
      assertEquals("third token is name3=:value3", "name3=:value3", cliTokenizer.nextTokenWithoutEscapeAndQuoteChars());
      assertEquals("forth token is name4=value4", "name4=value4", cliTokenizer.nextToken());
      assertEquals("hasMoreToken should be false, since there are no more tokens", false, cliTokenizer.hasMoreTokens());
  }


  // test tokens with escape characters and  ":" as the delimiter
  public void testTokensWithEscapeChar() throws Exception{

      final CLITokenizer cliTokenizer = new CLITokenizer("name1\\:123=value1:name2=value2\\:xyz:name3=\\:value3:name4=value4", ":");

      assertEquals("number of tokens is 4", 4, cliTokenizer.countTokens());
      assertEquals("first token is name1:123=value1", "name1:123=value1", cliTokenizer.nextTokenWithoutEscapeAndQuoteChars());
      assertEquals("second token is name2=\"value2:xyz\"", "name2=value2\\:xyz", cliTokenizer.nextToken());
      assertEquals("hasMoreToken should be true, since there is still two more token", true, cliTokenizer.hasMoreTokens());
      assertEquals("third token is name3=:value3", "name3=:value3", cliTokenizer.nextTokenWithoutEscapeAndQuoteChars());
      assertEquals("forth token is name4=value4", "name4=value4", cliTokenizer.nextToken());
      assertEquals("hasMoreToken should be false, since there are no more tokens", false, cliTokenizer.hasMoreTokens());
  }


  // test tokens with escape characters and quotes and  ":" as the delimiter
  public void testTokensWithEscapeCharAndQuotes() throws Exception{

      final CLITokenizer cliTokenizer = new CLITokenizer("name1=value1:name2=abc\\:def:name3=\"abc:def\":name4\\=123=value4:\"name5=123\"=value5:name6=\"abc\\:def\":name7=value7", ":");

      assertEquals("number of tokens is 7", 7, cliTokenizer.countTokens());
      assertEquals("first token is name1=value1", "name1=value1", cliTokenizer.nextToken());
      assertEquals("second token is name2=abc:def", "name2=abc:def", cliTokenizer.nextTokenWithoutEscapeAndQuoteChars());
      assertEquals("hasMoreToken should be true, since there is still four more tokens", true, cliTokenizer.hasMoreTokens());
      assertEquals("third token is name3=abc:def", "name3=abc:def", cliTokenizer.nextTokenWithoutEscapeAndQuoteChars());
      assertEquals("forth token is name4\\=123=value4", "name4\\=123=value4", cliTokenizer.nextToken());
      assertEquals("fifth token is \"name5=123\"=value5", "\"name5=123\"=value5", cliTokenizer.nextToken());
      assertEquals("sixth token is name6=abc\\:def", "name6=abc\\:def", cliTokenizer.nextTokenWithoutEscapeAndQuoteChars());
      assertEquals("seventh token is name7=value7", "name7=value7", cliTokenizer.nextToken());
      assertEquals("hasMoreToken should be false, since there are no more tokens", false, cliTokenizer.hasMoreTokens());
  }

  // test tokens with escape characters and quotes and  ":" as the delimiter
  public void testRecursiveTokensWithEscapeCharAndQuotes() throws Exception{

      final CLITokenizer cliTokenizer = new CLITokenizer("name1=value1:name2=abc\\:def:name3=\"abc:def\":name4\\=123=value4:\"name5=123\"=value5:name6=\"abc\\:def\":name7=value7", ":");

      assertEquals("number of tokens is 7", 7, cliTokenizer.countTokens());

      final String firstToken = cliTokenizer.nextToken();
      final CLITokenizer cliTokenizer1 = new CLITokenizer(firstToken, "=");
      assertEquals("first token is name1=value1", "name1=value1", firstToken);
      assertEquals("name of name1=value1", "name1", cliTokenizer1.nextToken());
      assertEquals("value of name1=value1", "value1", cliTokenizer1.nextToken());

      final String secondToken = cliTokenizer.nextToken();
      final CLITokenizer cliTokenizer2 = new CLITokenizer(secondToken, "=");
      assertEquals("second token is name2=abc\\:def", "name2=abc\\:def", secondToken);
      assertEquals("name of name2=abc:def", "name2", cliTokenizer2.nextTokenWithoutEscapeAndQuoteChars());
      assertEquals("value of name2=abc:def", "abc:def", cliTokenizer2.nextTokenWithoutEscapeAndQuoteChars());
      assertEquals("hasMoreToken should be true, since there is still four more tokens", true, cliTokenizer.hasMoreTokens());

      final String thirdToken = cliTokenizer.nextToken();
      final CLITokenizer cliTokenizer3 = new CLITokenizer(thirdToken, "=");
      assertEquals("third token is name3=\"abc:def\"", "name3=\"abc:def\"", thirdToken);
      assertEquals("name of name3=abc:def", "name3", cliTokenizer3.nextTokenWithoutEscapeAndQuoteChars());
      assertEquals("value of name3=abc:def", "abc:def", cliTokenizer3.nextTokenWithoutEscapeAndQuoteChars());

      final String fourthToken = cliTokenizer.nextToken();
      final CLITokenizer cliTokenizer4 = new CLITokenizer(fourthToken, "=");
      assertEquals("forth token is name4\\=123=value4", "name4\\=123=value4", fourthToken);
      assertEquals("name of name4\\=123=value4", "name4=123", cliTokenizer4.nextTokenWithoutEscapeAndQuoteChars());
      assertEquals("value of name4\\=123=value4", "value4", cliTokenizer4.nextTokenWithoutEscapeAndQuoteChars());

      final String fifthToken = cliTokenizer.nextToken();
      final CLITokenizer cliTokenizer5 = new CLITokenizer(fifthToken, "=");
      assertEquals("fifth token is \"name5=123\"=value5", "\"name5=123\"=value5", fifthToken);
      assertEquals("name of \"name5=123\"=value5", "name5=123", cliTokenizer5.nextTokenWithoutEscapeAndQuoteChars());
      assertEquals("value of \"name5=123\"=value5", "value5", cliTokenizer5.nextTokenWithoutEscapeAndQuoteChars());

      final String sixthToken = cliTokenizer.nextToken();
      final CLITokenizer cliTokenizer6 = new CLITokenizer(sixthToken, "=");
      assertEquals("sixth token is name6=\"abc\\:def\"", "name6=\"abc\\:def\"", sixthToken);
      assertEquals("name of name6=\"abc\\:def\"", "name6", cliTokenizer6.nextTokenWithoutEscapeAndQuoteChars());
      assertEquals("value of name6=\"abc\\:def\"", "abc\\:def", cliTokenizer6.nextTokenWithoutEscapeAndQuoteChars());


      final String seventhToken = cliTokenizer.nextToken();
      final CLITokenizer cliTokenizer7 = new CLITokenizer(seventhToken, "=");
      assertEquals("seventh token is name7=value7", "name7=value7", seventhToken);
      assertEquals("name of name7=value7", "name7", cliTokenizer7.nextTokenWithoutEscapeAndQuoteChars());
      assertEquals("value of name7=value7", "value7", cliTokenizer7.nextTokenWithoutEscapeAndQuoteChars());

      assertEquals("hasMoreToken should be false, since there are no more tokens", false, cliTokenizer.hasMoreTokens());
  }

  // test token with " " (spaces as the delimiter
  public void testTokensWithSpace() throws Exception{

      CLITokenizer cliTokenizer = new CLITokenizer("   There is a \"right time\" and    a \"right way\" to do everything    ", " ");

      assertEquals("number of tokens is 10", 10, cliTokenizer.countTokens());
      assertEquals("first token is There", "There", cliTokenizer.nextToken());
      assertEquals("second token is is", "is", cliTokenizer.nextToken());
      assertEquals("hasMoreToken should be true, since there are still more tokens", true, cliTokenizer.hasMoreTokens());
      assertEquals("third token is a", "a", cliTokenizer.nextToken());
      assertEquals("fourth token is \"right time\"", "\"right time\"", cliTokenizer.nextToken());
      assertEquals("fifth token is and", "and", cliTokenizer.nextToken());
      assertEquals("sixth token is a", "a", cliTokenizer.nextToken());
      assertEquals("seventh token is \"right way\"", "\"right way\"", cliTokenizer.nextToken());
      assertEquals("eighth token is to", "to", cliTokenizer.nextToken());
      assertEquals("nineth token is do", "do", cliTokenizer.nextToken());
      assertEquals("tenth token is everything", "everything", cliTokenizer.nextToken());
      assertEquals("hasMoreToken should be false, since there are no more tokens", false, cliTokenizer.hasMoreTokens());
  }


  // test token with " " (spaces as the delimiter)
  public void testTokensWithSpace2() throws Exception{

      CLITokenizer cliTokenizer = new CLITokenizer("-Dx=\\\" abc\\\":-D\\\"xyz=a bc\\\":-Dy=\\\"1 2 3\\\":-Dz=1\\\"2 3\\\":-Da=bc", ":");

      assertEquals("number of tokens is 5", 5, cliTokenizer.countTokens());
      assertEquals("first token is -Dx=\" abc\"", "-Dx=\" abc\"", cliTokenizer.nextTokenWithoutEscapeAndQuoteChars());
      assertEquals("second token is -D\"xyz=a bc\"", "-D\"xyz=a bc\"", cliTokenizer.nextTokenWithoutEscapeAndQuoteChars());
      assertEquals("third token is -Dy=\"1 2 3\"", "-Dy=\"1 2 3\"", cliTokenizer.nextTokenWithoutEscapeAndQuoteChars());
      assertEquals("forth token is -Dz=1\"2 3\"", "-Dz=1\"2 3\"", cliTokenizer.nextTokenWithoutEscapeAndQuoteChars());
      assertEquals("fifth token is -Da=bc", "-Da=bc", cliTokenizer.nextToken());
  }

  public void testIncompletQuote() throws Exception {
      try {
          CLITokenizer cliTokenizer = new CLITokenizer("abc=def:\"123=4:56\":xyz\"", ":");
      }
      catch (Exception e) {
          assertEquals("CommandException caught", "com.sun.enterprise.cli.framework.CommandException", e.getClass().getName());
      }
  }
    

  public CLITokenizerTest(String name){
	super(name);
  }


  protected void setUp() {
  }
  
  

  protected void tearDown() {
  }

  private void nyi(){
	fail("Not Yet Implemented");
  }

  public static Test suite(){
	TestSuite suite = new TestSuite(CLITokenizerTest.class);
	return suite;
  }

  public static void main(String args[]) throws Exception {
    final TestRunner runner= new TestRunner();
    final TestResult result = runner.doRun(CLITokenizerTest.suite(), false);
    System.exit(result.errorCount() + result.failureCount());
  }
}

