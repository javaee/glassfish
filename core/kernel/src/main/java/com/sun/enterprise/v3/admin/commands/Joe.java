/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.v3.admin.commands;

import com.sun.enterprise.util.i18n.StringManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A package private immmutable class that denotes a Java VM Option.
 * An instance of Joe has a name and a value. The two are always
 * delimited by a ':' when it is represented as a single string.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish V3
 */
final class Joe {

    static final String NAME_VALUE_DELIMITER = "=";
    static final char VALID_JOE_START = '-';
    final static String OPTION_DELIMITER = ":";
    final static String DOMAINXML_DELIM = " ";
    
    public static final String CMDLINE_JVMOPTS_REGEX = "(-([^:]|[.*\\:.*])+:?)+";
    public static final String SPLIT_PATTERN         = "(?<!\\\\):";
    
    private final String name;
    private final String value;
    private String ts;
    
    private static final StringManager lsm = StringManager.getManager(Joe.class);
    
    Joe (String name) {
        this(name, ""); //several Joes have no value
    }
    
    Joe(String name, String value) {
        if (name == null || value == null)
            throw new IllegalArgumentException("null value for name or value of a JVM Option");
        if (name.length() <= 1)
            throw new IllegalArgumentException("Illegal JVM Option name (too short): " + name);
        if (name.charAt(0) != VALID_JOE_START) {
            String msg = lsm.getString("joe.invalid.start", name);
            throw new IllegalArgumentException(msg);
        }
        name = name.replace("\\", "");//removes backslashes!
        this.name  = name;
        this.value = value;
        if ("".equals(value))
            this.ts = name;
        else
            this.ts    = name + NAME_VALUE_DELIMITER + value;
        ts = quoteIfNeeded(ts);
    }
    
    String getName() {
        return ( name );
    }
    
    String getValue() {
        return ( value );
    }
    
    boolean existsIn(List<String> options) {
        for(String option : options) {
            //TODO need to take care of multiple ones ...
            if (this.toString().equals(option)) {
                return ( true );
            }
        }
        return ( false );
    }
    
    @Override
    public String toString() {
        return ( ts );
    }
    
    private static String quoteIfNeeded(String s) {
        if(s.indexOf(DOMAINXML_DELIM) != -1) {
            s = "\"" + s + "\"";
        }
        return ( s );
    }
    
    /** Method to help with parsing the commmand line. The command line string
     *  always delimits various options with ':'.
     * @param cmdlineString a String delimited with ':'s
     * @return a Set of Java Options of the form "-abc" or "-abc=xyz". Empty set if
     * there are none. Never returns null.
     */
    static List<Joe> toJoes(String cmd) {
        if (cmd == null)
            throw new IllegalArgumentException("null arg");
        List<Joe> joes  = new ArrayList<Joe>();
        Pattern syntax  = Pattern.compile(CMDLINE_JVMOPTS_REGEX);
        Matcher matcher = syntax.matcher(cmd);
        if (!matcher.matches()) {
            String msg = lsm.getString("joe.invalid.cmd.syntax", cmd);
            throw new IllegalArgumentException(msg);
        }
        Pattern splitter = Pattern.compile(SPLIT_PATTERN);
        String[] parts = splitter.split(cmd);
        for (String part : parts)
            joes.add(new Joe(part));
        return ( Collections.unmodifiableList(joes) );
    }
    
    static List<String> toStrings(List<Joe> joes) {
        if (joes == null)
            throw new IllegalArgumentException("null args");
        List<String> strings = new ArrayList<String>(joes.size());
        for (Joe joe : joes) {
            strings.add(joe.toString()); //Note!
        }
        return ( Collections.unmodifiableList(strings) );
    }
    
    static List<Joe> pruneJoes(List<String> fromDomainXml, List<Joe> nascent) {
        List<Joe> pruned = new ArrayList<Joe>(nascent);
        for(Joe joe: nascent) {
            if(joe.existsIn(fromDomainXml)) {
                pruned.remove(joe);
            }
        }
        return ( pruned );
    }
}