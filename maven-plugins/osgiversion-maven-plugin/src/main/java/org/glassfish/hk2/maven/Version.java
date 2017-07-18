/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.maven;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.shared.osgi.DefaultMaven2OsgiConverter;
import org.apache.maven.shared.osgi.Maven2OsgiConverter;
import org.codehaus.plexus.util.StringUtils;

/**
 *
 * @author Romain Grecourt
 */
public class Version {

    public enum COMPONENT {
        major,
        minor,
        micro,
        qualifier
    };
    
    private static final int DIGITS_INDEX = 1;
    public static final Pattern STANDARD_PATTERN = Pattern.compile(
            "^((?:\\d+\\.)*\\d+)" // digit(s) and '.' repeated - followed by digit (version digits 1.22.0, etc)
            + "([-_])?" // optional - or _  (annotation separator)
            + "([a-zA-Z]*)" // alpha characters (looking for annotation - alpha, beta, RC, etc.)
            + "([-_])?" // optional - or _  (annotation revision separator)
            + "(\\d*)" // digits  (any digits after rc or beta is an annotation revision)
            + "(?:([-_])?(.*?))?$");  // - or _ followed everything else (build specifier)
    String orig;
    int major = 0;
    int minor = 0;
    int incremental = 0;
    String qualifier = "";

    public Version(String v) {
        orig = v;
        List<String> digits = parseDigits(v);
        major = getDigit(digits, 0);
        minor = getDigit(digits, 1);
        incremental = getDigit(digits, 2);
        if(orig.contains("-")){
            qualifier = orig.substring(orig.indexOf('-')+1);
        }
    }

    private static int getDigit(List<String> digits, int idx) {
        if (digits.size() >= idx + 1
                && digits.get(idx) != null
                && !digits.get(idx).isEmpty()) {
            return Integer.parseInt(digits.get(idx));
        }
        return 0;
    }

    private List<String> parseDigits(String vStr) {
        Matcher m = STANDARD_PATTERN.matcher(vStr);
        if (m.matches()) {
            return Arrays.asList(StringUtils.split(
                    m.group(DIGITS_INDEX),
                    "."));
        }
        return Collections.EMPTY_LIST;
    }

    public int getMajorVersion() {
        return major;
    }

    public int getMinorVersion() {
        return minor;
    }

    public int getIncrementalVersion() {
        return incremental;
    }
    
    public String getQualifier(){
        return qualifier;
    }

    private static String formatString4Osgi(String s){
        return s.replaceAll("-", "_").replaceAll("\\.", "_");
    }
    
    public String convertToOsgi(COMPONENT comToDrop) {
        Maven2OsgiConverter converter = new DefaultMaven2OsgiConverter();

        if (comToDrop != null) {
            switch (comToDrop) {
                case major: {
                    return converter.getVersion("0.0.0");
                }
                case minor: {
                    return converter.getVersion(String.valueOf(getMajorVersion()));
                }
                case micro: {
                    return converter.getVersion(String.format("%s.%s",
                            getMajorVersion(),
                            getMinorVersion()));
                }
                case qualifier: {
                    return converter.getVersion(String.format("%s.%s.%s",
                            getMajorVersion(),
                            getMinorVersion(),
                            getIncrementalVersion()));
                }
            }
        }
        
        // init version major.minor.micro
        String version = String.format("%s.%s.%s",
                getMajorVersion(),
                getMinorVersion(),
                getIncrementalVersion());
        
        // if there is a qualifier, add it
        if(!getQualifier().isEmpty()){
            version = String.format("%s.%s",
                    version,
                    formatString4Osgi(getQualifier()));
        }
        return converter.getVersion(version);
    }
}
