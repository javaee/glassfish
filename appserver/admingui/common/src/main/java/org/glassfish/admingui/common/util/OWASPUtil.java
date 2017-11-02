/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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


package org.glassfish.admingui.common.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/*
Util class for static methods for handling encoding of invalid string characters. Use recommendations from Open Web Application Security Project (see here
http://www.owasp.org/index.php/Log_Forging)
 */
public final class OWASPUtil {
    public static final String HEADER_VALUE_VALIDATION_PATERN = "^[a-zA-Z0-9()\\-=\\*\\.\\?;,+\\/:&_ ]*$";
    private final static String  IMMUNE_HTML = ",.-_ ";
    private static final char REPLACEMENT_CHAR = '\ufffd';
    private static final String REPLACEMENT_HEX = "fffd";
    private static final String REPLACEMENT_STR = "" + REPLACEMENT_CHAR;
    private static final Map<Integer,String> characterToEntityMap = getCharacterToEntityMap();
    private static final String[] hex = new String[256];
    static {
        for ( char c = 0; c < 0xFF; c++ ) {
            if ( c >= 0x30 && c <= 0x39 || c >= 0x41 && c <= 0x5A || c >= 0x61 && c <= 0x7A ) {
                hex[c] = null;
            } else {
                hex[c] = toHex(c).intern();
            }
        }
    }

    private OWASPUtil() {
    }

    public static String neutralizeForLog(String message){
        StringBuilder sb = new StringBuilder();
        for(int offset  = 0; offset < message.length(); ){
            final int point = message.codePointAt(offset);
            if(Character.isValidCodePoint(point)){
                sb.append(encodeCharacter(point));
            }
            offset += Character.charCount(point);
        }
        return sb.toString();
    }

    public static String encodeCharacter(int codePoint){
        if(IMMUNE_HTML.indexOf(codePoint)!=-1) {
            return new StringBuilder().appendCodePoint(codePoint).toString();
        }
        String hex = getHexForNonAlphanumeric(codePoint);
        if ( hex == null && Character.isValidCodePoint(codePoint)) {
            return new StringBuilder().appendCodePoint(codePoint).toString();
        }
        // check for illegal characters
        if ( ( codePoint <= 0x1f
                && codePoint != '\t'
                && codePoint != '\n'
                && codePoint != '\r' )
                || ( codePoint >= 0x7f && codePoint <= 0x9f ) )
        {
            hex = REPLACEMENT_HEX;	// Let's entity encode this instead of returning it
            codePoint = REPLACEMENT_CHAR;
        }

        // check if there's a defined entity
        String entityName = (String) characterToEntityMap.get(codePoint);
        if (entityName != null) {
            return "&" + entityName + ";";
        }

        // return the hex entity as suggested in the spec
        return "&#x" + hex + ";";
    }
    private static String getHexForNonAlphanumeric(int c){
        if(c<0xFF){
            return hex[c];
        }else{
            return toHex(c);
        }
    }

    private static String toHex(int c){
        return Integer.toHexString(c);
    }

    public static String removeLinearWhiteSpaces(String input) {
        if (input != null) {
                input = Pattern.compile("//s").matcher(input).replaceAll(" ");
            }
        return input;
    }
    public static String getSafeHeaderValue(String headerValue) throws Exception {
        headerValue = removeLinearWhiteSpaces(headerValue);
        if (headerValue != null) {
                if (!Pattern.compile(HEADER_VALUE_VALIDATION_PATERN).matcher(headerValue).matches()) {
                        throw new Exception("Header Value invalid characters");
                    }
            }
        return headerValue;
    }

    private static Map<Integer,String> getCharacterToEntityMap(){
        Map<Integer, String> map = new HashMap<>(252);

        map.put(34,	"quot");	/* quotation mark */
        map.put(38,	"amp");		/* ampersand */
        map.put(60,	"lt");		/* less-than sign */
        map.put(62,	"gt");		/* greater-than sign */
        map.put(160,	"nbsp");	/* no-break space */
        map.put(161,	"iexcl");	/* inverted exclamation mark */
        map.put(162,	"cent");	/* cent sign */
        map.put(163,	"pound");	/* pound sign */
        map.put(164,	"curren");	/* currency sign */
        map.put(165,	"yen");		/* yen sign */
        map.put(166,	"brvbar");	/* broken bar */
        map.put(167,	"sect");	/* section sign */
        map.put(168,	"uml");		/* diaeresis */
        map.put(169,	"copy");	/* copyright sign */
        map.put(170,	"ordf");	/* feminine ordinal indicator */
        map.put(171,	"laquo");	/* left-pointing double angle quotation mark */
        map.put(172,	"not");		/* not sign */
        map.put(173,	"shy");		/* soft hyphen */
        map.put(174,	"reg");		/* registered sign */
        map.put(175,	"macr");	/* macron */
        map.put(176,	"deg");		/* degree sign */
        map.put(177,	"plusmn");	/* plus-minus sign */
        map.put(178,	"sup2");	/* superscript two */
        map.put(179,	"sup3");	/* superscript three */
        map.put(180,	"acute");	/* acute accent */
        map.put(181,	"micro");	/* micro sign */
        map.put(182,	"para");	/* pilcrow sign */
        map.put(183,	"middot");	/* middle dot */
        map.put(184,	"cedil");	/* cedilla */
        map.put(185,	"sup1");	/* superscript one */
        map.put(186,	"ordm");	/* masculine ordinal indicator */
        map.put(187,	"raquo");	/* right-pointing double angle quotation mark */
        map.put(188,	"frac14");	/* vulgar fraction one quarter */
        map.put(189,	"frac12");	/* vulgar fraction one half */
        map.put(190,	"frac34");	/* vulgar fraction three quarters */
        map.put(191,	"iquest");	/* inverted question mark */
        map.put(192,	"Agrave");	/* Latin capital letter a with grave */
        map.put(193,	"Aacute");	/* Latin capital letter a with acute */
        map.put(194,	"Acirc");	/* Latin capital letter a with circumflex */
        map.put(195,	"Atilde");	/* Latin capital letter a with tilde */
        map.put(196,	"Auml");	/* Latin capital letter a with diaeresis */
        map.put(197,	"Aring");	/* Latin capital letter a with ring above */
        map.put(198,	"AElig");	/* Latin capital letter ae */
        map.put(199,	"Ccedil");	/* Latin capital letter c with cedilla */
        map.put(200,	"Egrave");	/* Latin capital letter e with grave */
        map.put(201,	"Eacute");	/* Latin capital letter e with acute */
        map.put(202,	"Ecirc");	/* Latin capital letter e with circumflex */
        map.put(203,	"Euml");	/* Latin capital letter e with diaeresis */
        map.put(204,	"Igrave");	/* Latin capital letter i with grave */
        map.put(205,	"Iacute");	/* Latin capital letter i with acute */
        map.put(206,	"Icirc");	/* Latin capital letter i with circumflex */
        map.put(207,	"Iuml");	/* Latin capital letter i with diaeresis */
        map.put(208,	"ETH");		/* Latin capital letter eth */
        map.put(209,	"Ntilde");	/* Latin capital letter n with tilde */
        map.put(210,	"Ograve");	/* Latin capital letter o with grave */
        map.put(211,	"Oacute");	/* Latin capital letter o with acute */
        map.put(212,	"Ocirc");	/* Latin capital letter o with circumflex */
        map.put(213,	"Otilde");	/* Latin capital letter o with tilde */
        map.put(214,	"Ouml");	/* Latin capital letter o with diaeresis */
        map.put(215,	"times");	/* multiplication sign */
        map.put(216,	"Oslash");	/* Latin capital letter o with stroke */
        map.put(217,	"Ugrave");	/* Latin capital letter u with grave */
        map.put(218,	"Uacute");	/* Latin capital letter u with acute */
        map.put(219,	"Ucirc");	/* Latin capital letter u with circumflex */
        map.put(220,	"Uuml");	/* Latin capital letter u with diaeresis */
        map.put(221,	"Yacute");	/* Latin capital letter y with acute */
        map.put(222,	"THORN");	/* Latin capital letter thorn */
        map.put(223,	"szlig");	/* Latin small letter sharp sXCOMMAX German Eszett */
        map.put(224,	"agrave");	/* Latin small letter a with grave */
        map.put(225,	"aacute");	/* Latin small letter a with acute */
        map.put(226,	"acirc");	/* Latin small letter a with circumflex */
        map.put(227,	"atilde");	/* Latin small letter a with tilde */
        map.put(228,	"auml");	/* Latin small letter a with diaeresis */
        map.put(229,	"aring");	/* Latin small letter a with ring above */
        map.put(230,	"aelig");	/* Latin lowercase ligature ae */
        map.put(231,	"ccedil");	/* Latin small letter c with cedilla */
        map.put(232,	"egrave");	/* Latin small letter e with grave */
        map.put(233,	"eacute");	/* Latin small letter e with acute */
        map.put(234,	"ecirc");	/* Latin small letter e with circumflex */
        map.put(235,	"euml");	/* Latin small letter e with diaeresis */
        map.put(236,	"igrave");	/* Latin small letter i with grave */
        map.put(237,	"iacute");	/* Latin small letter i with acute */
        map.put(238,	"icirc");	/* Latin small letter i with circumflex */
        map.put(239,	"iuml");	/* Latin small letter i with diaeresis */
        map.put(240,	"eth");		/* Latin small letter eth */
        map.put(241,	"ntilde");	/* Latin small letter n with tilde */
        map.put(242,	"ograve");	/* Latin small letter o with grave */
        map.put(243,	"oacute");	/* Latin small letter o with acute */
        map.put(244,	"ocirc");	/* Latin small letter o with circumflex */
        map.put(245,	"otilde");	/* Latin small letter o with tilde */
        map.put(246,	"ouml");	/* Latin small letter o with diaeresis */
        map.put(247,	"divide");	/* division sign */
        map.put(248,	"oslash");	/* Latin small letter o with stroke */
        map.put(249,	"ugrave");	/* Latin small letter u with grave */
        map.put(250,	"uacute");	/* Latin small letter u with acute */
        map.put(251,	"ucirc");	/* Latin small letter u with circumflex */
        map.put(252,	"uuml");	/* Latin small letter u with diaeresis */
        map.put(253,	"yacute");	/* Latin small letter y with acute */
        map.put(254,	"thorn");	/* Latin small letter thorn */
        map.put(255,	"yuml");	/* Latin small letter y with diaeresis */
        map.put(338,	"OElig");	/* Latin capital ligature oe */
        map.put(339,	"oelig");	/* Latin small ligature oe */
        map.put(352,	"Scaron");	/* Latin capital letter s with caron */
        map.put(353,	"scaron");	/* Latin small letter s with caron */
        map.put(376,	"Yuml");	/* Latin capital letter y with diaeresis */
        map.put(402,	"fnof");	/* Latin small letter f with hook */
        map.put(710,	"circ");	/* modifier letter circumflex accent */
        map.put(732,	"tilde");	/* small tilde */
        map.put(913,	"Alpha");	/* Greek capital letter alpha */
        map.put(914,	"Beta");	/* Greek capital letter beta */
        map.put(915,	"Gamma");	/* Greek capital letter gamma */
        map.put(916,	"Delta");	/* Greek capital letter delta */
        map.put(917,	"Epsilon");	/* Greek capital letter epsilon */
        map.put(918,	"Zeta");	/* Greek capital letter zeta */
        map.put(919,	"Eta");		/* Greek capital letter eta */
        map.put(920,	"Theta");	/* Greek capital letter theta */
        map.put(921,	"Iota");	/* Greek capital letter iota */
        map.put(922,	"Kappa");	/* Greek capital letter kappa */
        map.put(923,	"Lambda");	/* Greek capital letter lambda */
        map.put(924,	"Mu");		/* Greek capital letter mu */
        map.put(925,	"Nu");		/* Greek capital letter nu */
        map.put(926,	"Xi");		/* Greek capital letter xi */
        map.put(927,	"Omicron");	/* Greek capital letter omicron */
        map.put(928,	"Pi");		/* Greek capital letter pi */
        map.put(929,	"Rho");		/* Greek capital letter rho */
        map.put(931,	"Sigma");	/* Greek capital letter sigma */
        map.put(932,	"Tau");		/* Greek capital letter tau */
        map.put(933,	"Upsilon");	/* Greek capital letter upsilon */
        map.put(934,	"Phi");		/* Greek capital letter phi */
        map.put(935,	"Chi");		/* Greek capital letter chi */
        map.put(936,	"Psi");		/* Greek capital letter psi */
        map.put(937,	"Omega");	/* Greek capital letter omega */
        map.put(945,	"alpha");	/* Greek small letter alpha */
        map.put(946,	"beta");	/* Greek small letter beta */
        map.put(947,	"gamma");	/* Greek small letter gamma */
        map.put(948,	"delta");	/* Greek small letter delta */
        map.put(949,	"epsilon");	/* Greek small letter epsilon */
        map.put(950,	"zeta");	/* Greek small letter zeta */
        map.put(951,	"eta");		/* Greek small letter eta */
        map.put(952,	"theta");	/* Greek small letter theta */
        map.put(953,	"iota");	/* Greek small letter iota */
        map.put(954,	"kappa");	/* Greek small letter kappa */
        map.put(955,	"lambda");	/* Greek small letter lambda */
        map.put(956,	"mu");		/* Greek small letter mu */
        map.put(957,	"nu");		/* Greek small letter nu */
        map.put(958,	"xi");		/* Greek small letter xi */
        map.put(959,	"omicron");	/* Greek small letter omicron */
        map.put(960,	"pi");		/* Greek small letter pi */
        map.put(961,	"rho");		/* Greek small letter rho */
        map.put(962,	"sigmaf");	/* Greek small letter final sigma */
        map.put(963,	"sigma");	/* Greek small letter sigma */
        map.put(964,	"tau");		/* Greek small letter tau */
        map.put(965,	"upsilon");	/* Greek small letter upsilon */
        map.put(966,	"phi");		/* Greek small letter phi */
        map.put(967,	"chi");		/* Greek small letter chi */
        map.put(968,	"psi");		/* Greek small letter psi */
        map.put(969,	"omega");	/* Greek small letter omega */
        map.put(977,	"thetasym");	/* Greek theta symbol */
        map.put(978,	"upsih");	/* Greek upsilon with hook symbol */
        map.put(982,	"piv");		/* Greek pi symbol */
        map.put(8194,	"ensp");	/* en space */
        map.put(8195,	"emsp");	/* em space */
        map.put(8201,	"thinsp");	/* thin space */
        map.put(8204,	"zwnj");	/* zero width non-joiner */
        map.put(8205,	"zwj");		/* zero width joiner */
        map.put(8206,	"lrm");		/* left-to-right mark */
        map.put(8207,	"rlm");		/* right-to-left mark */
        map.put(8211,	"ndash");	/* en dash */
        map.put(8212,	"mdash");	/* em dash */
        map.put(8216,	"lsquo");	/* left single quotation mark */
        map.put(8217,	"rsquo");	/* right single quotation mark */
        map.put(8218,	"sbquo");	/* single low-9 quotation mark */
        map.put(8220,	"ldquo");	/* left double quotation mark */
        map.put(8221,	"rdquo");	/* right double quotation mark */
        map.put(8222,	"bdquo");	/* double low-9 quotation mark */
        map.put(8224,	"dagger");	/* dagger */
        map.put(8225,	"Dagger");	/* double dagger */
        map.put(8226,	"bull");	/* bullet */
        map.put(8230,	"hellip");	/* horizontal ellipsis */
        map.put(8240,	"permil");	/* per mille sign */
        map.put(8242,	"prime");	/* prime */
        map.put(8243,	"Prime");	/* double prime */
        map.put(8249,	"lsaquo");	/* single left-pointing angle quotation mark */
        map.put(8250,	"rsaquo");	/* single right-pointing angle quotation mark */
        map.put(8254,	"oline");	/* overline */
        map.put(8260,	"frasl");	/* fraction slash */
        map.put(8364,	"euro");	/* euro sign */
        map.put(8465,	"image");	/* black-letter capital i */
        map.put(8472,	"weierp");	/* script capital pXCOMMAX Weierstrass p */
        map.put(8476,	"real");	/* black-letter capital r */
        map.put(8482,	"trade");	/* trademark sign */
        map.put(8501,	"alefsym");	/* alef symbol */
        map.put(8592,	"larr");	/* leftwards arrow */
        map.put(8593,	"uarr");	/* upwards arrow */
        map.put(8594,	"rarr");	/* rightwards arrow */
        map.put(8595,	"darr");	/* downwards arrow */
        map.put(8596,	"harr");	/* left right arrow */
        map.put(8629,	"crarr");	/* downwards arrow with corner leftwards */
        map.put(8656,	"lArr");	/* leftwards double arrow */
        map.put(8657,	"uArr");	/* upwards double arrow */
        map.put(8658,	"rArr");	/* rightwards double arrow */
        map.put(8659,	"dArr");	/* downwards double arrow */
        map.put(8660,	"hArr");	/* left right double arrow */
        map.put(8704,	"forall");	/* for all */
        map.put(8706,	"part");	/* partial differential */
        map.put(8707,	"exist");	/* there exists */
        map.put(8709,	"empty");	/* empty set */
        map.put(8711,	"nabla");	/* nabla */
        map.put(8712,	"isin");	/* element of */
        map.put(8713,	"notin");	/* not an element of */
        map.put(8715,	"ni");		/* contains as member */
        map.put(8719,	"prod");	/* n-ary product */
        map.put(8721,	"sum");		/* n-ary summation */
        map.put(8722,	"minus");	/* minus sign */
        map.put(8727,	"lowast");	/* asterisk operator */
        map.put(8730,	"radic");	/* square root */
        map.put(8733,	"prop");	/* proportional to */
        map.put(8734,	"infin");	/* infinity */
        map.put(8736,	"ang");		/* angle */
        map.put(8743,	"and");		/* logical and */
        map.put(8744,	"or");		/* logical or */
        map.put(8745,	"cap");		/* intersection */
        map.put(8746,	"cup");		/* union */
        map.put(8747,	"int");		/* integral */
        map.put(8756,	"there4");	/* therefore */
        map.put(8764,	"sim");		/* tilde operator */
        map.put(8773,	"cong");	/* congruent to */
        map.put(8776,	"asymp");	/* almost equal to */
        map.put(8800,	"ne");		/* not equal to */
        map.put(8801,	"equiv");	/* identical toXCOMMAX equivalent to */
        map.put(8804,	"le");		/* less-than or equal to */
        map.put(8805,	"ge");		/* greater-than or equal to */
        map.put(8834,	"sub");		/* subset of */
        map.put(8835,	"sup");		/* superset of */
        map.put(8836,	"nsub");	/* not a subset of */
        map.put(8838,	"sube");	/* subset of or equal to */
        map.put(8839,	"supe");	/* superset of or equal to */
        map.put(8853,	"oplus");	/* circled plus */
        map.put(8855,	"otimes");	/* circled times */
        map.put(8869,	"perp");	/* up tack */
        map.put(8901,	"sdot");	/* dot operator */
        map.put(8968,	"lceil");	/* left ceiling */
        map.put(8969,	"rceil");	/* right ceiling */
        map.put(8970,	"lfloor");	/* left floor */
        map.put(8971,	"rfloor");	/* right floor */
        map.put(9001,	"lang");	/* left-pointing angle bracket */
        map.put(9002,	"rang");	/* right-pointing angle bracket */
        map.put(9674,	"loz");		/* lozenge */
        map.put(9824,	"spades");	/* black spade suit */
        map.put(9827,	"clubs");	/* black club suit */
        map.put(9829,	"hearts");	/* black heart suit */
        map.put(9830,	"diams");	/* black diamond suit */

        return Collections.unmodifiableMap(map);
    }

}
