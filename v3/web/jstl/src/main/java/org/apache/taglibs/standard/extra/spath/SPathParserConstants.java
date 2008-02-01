/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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
package org.apache.taglibs.standard.extra.spath;

public interface SPathParserConstants {

  int EOF = 0;
  int LITERAL = 1;
  int QNAME = 2;
  int NCNAME = 3;
  int NSWILDCARD = 4;
  int NCNAMECHAR = 5;
  int LETTER = 6;
  int DIGIT = 7;
  int COMBINING_CHAR = 8;
  int EXTENDER = 9;
  int UNDERSCORE = 10;
  int DOT = 11;
  int DASH = 12;
  int SLASH = 13;
  int STAR = 14;
  int COLON = 15;
  int START_BRACKET = 16;
  int END_BRACKET = 17;
  int AT = 18;
  int EQUALS = 19;

  int DEFAULT = 0;

  String[] tokenImage = {
    "<EOF>",
    "<LITERAL>",
    "<QNAME>",
    "<NCNAME>",
    "<NSWILDCARD>",
    "<NCNAMECHAR>",
    "<LETTER>",
    "<DIGIT>",
    "<COMBINING_CHAR>",
    "<EXTENDER>",
    "\"_\"",
    "\".\"",
    "\"-\"",
    "\"/\"",
    "\"*\"",
    "\":\"",
    "\"[\"",
    "\"]\"",
    "\"@\"",
    "\"=\"",
  };

}
