/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
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