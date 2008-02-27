/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.module.maven.sc;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sun.enterprise.module.maven.sc.ScriptConstants.*;

/** Identifies a <code> [section] </code> in the config file.
 *  It's a package private class.
 *  A section is comprised of a named header and a set of name-value pairs,
 *  one per line terminated by newline character.
 * @author Kedar Mhaswade (km@dev.java.net)
 */
final class Section {
    private final String name;
    private final Map<String, String> props;
    private final Properties env;
    
    Section(String name, Properties env) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("null arg");
        this.name  = name;
        this.props = new LinkedHashMap<String, String>();
        this.env   = env;
    }
    
    void put(String name, String value) {
        value = process(value);
        props.put(name, value);
    }
    
    void put(String line) {
        StringTokenizer st = new StringTokenizer(line, "=");
        if (st.countTokens() != 2)
            throw new IllegalArgumentException("invalid line format: " + line 
                    + ", should have been of the form: a=b");
        String key = st.nextToken();
        String val = process(st.nextToken());
        //System.out.println("key: " + key + ", val: " + val);
        props.put(key, val);
    }
    
    Map<String, String> getProps() {
        return ( Collections.unmodifiableMap(props) );
    }
    
    String getProperty(String name) {
        return ( props.get(name) );
    }
    
    String getName() {
        return ( name );
    }
    private String process(String value) {
        //Apply the rules now.
        String processed = processLocalRefs(value);   //first, replace '${token}' correctly
        processed        = processExtRefs(processed); //then, replace ${x} with properties in env
        return ( processed );
    }
    
    private String processLocalRefs(String s) {
        Pattern pattern = Pattern.compile(LOCAL_VAR_REFER_PATTERN);
        Matcher matcher = pattern.matcher(s);
        String replaced = s;
        while(matcher.find()) {
            String token = matcher.group();
            //token        = token.substring(1, token.length() - 1);
            //we replace '${token}' by $token and %token%
            token        = makeLocalRef(token);
            int start    = matcher.start();
            int end      = matcher.end();
            replaced     = s.substring(0, start) + token + s.substring(end);
        }
        return ( replaced );
    }
    
    private String makeLocalRef(String s) { //gets rid of '${ and }' from given string
        if (WINDOWS.equals(env.get(OPERATING_SYSTEM))) {
            return ("%" + s.substring(2, s.length() - 1) + "%"); 
        } else { //only "Windows" behaves differently
            return ( "$" + s.substring(2, s.length() - 1) );
        }
    }
    private String processExtRefs(String s) {
        Pattern pattern = Pattern.compile(EXT_PROP_REFER_PATTERN);
        Matcher matcher = pattern.matcher(s);
        String replaced = s;
        while(matcher.find()) {
            String token = matcher.group();
            token        = token.substring(2, token.length() - 1);
            //we replace ${token} by contancting the environment
            token        = env.getProperty(token);
            int start    = matcher.start();
            int end      = matcher.end();
            replaced     = s.substring(0, start) + token + s.substring(end);
        }
        return ( replaced );
    }

}