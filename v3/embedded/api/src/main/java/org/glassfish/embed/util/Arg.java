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
package org.glassfish.embed.util;

import org.glassfish.embed.util.StringUtils;

/**
 *
 * @author Byron Nevins
 */
public class Arg {
    public Arg(String ln, String sn, String defaultValue, String desc) {
        this(ln, sn, defaultValue, false, desc);
    }
    
    public Arg(String ln, String sn, boolean required, String desc) {
        this(ln, sn, null, required, desc);
    }
    
    public Arg(String ln, String sn, String defaultValue, boolean required, String desc) {
        longName = ln;
        shortName = sn;
        this.defaultValue = defaultValue;
        this.required = required;
        description = desc;
    }
    
    public static String toHelp(Arg[] args) {
        String[] longs = new String[args.length];
        String[] shorts = new String[args.length];
        String[] descs = new String[args.length];
        String[] defs = new String[args.length];
        String[] reqs = new String[args.length];
        
        for(int i = 0; i < args.length; i++) {
            longs[i] = args[i].longName;
            shorts[i] = args[i].shortName;
            descs[i] = args[i].description;
            defs[i] = args[i].defaultValue;
            reqs[i] = Boolean.toString(args[i].required);
        }
        
        int longMax = Math.max(LONG.length(), StringUtils.maxWidth(longs)) + 4;
        int shortMax = Math.max(SHORT.length(), StringUtils.maxWidth(shorts)) + 3;
        int descMax = Math.max(DESC.length(), StringUtils.maxWidth(descs)) + 2;
        int defMax = Math.max(DEF.length(), StringUtils.maxWidth(defs)) + 2;
        int reqMax = Math.max(REQ.length(), StringUtils.maxWidth(reqs)) + 2;
        
        StringBuilder sb = new StringBuilder();
        
        sb.append(pr(DESC, descMax));
        sb.append(pr(LONG, longMax));
        sb.append(pr(SHORT, shortMax));
        sb.append(pr(DEF, defMax));
        sb.append(pr(REQ, reqMax));
        sb.append(StringUtils.EOL);
        
        for(Arg arg : args) {
            sb.append(StringUtils.EOL);
            sb.append(pr(arg.description, descMax));
            sb.append(pr("--" + arg.longName, longMax));
            sb.append(pr(arg.shortName == null ? "" : "-" + arg.shortName, shortMax));
            sb.append(pr(arg.defaultValue == null ? "" : arg.defaultValue, defMax));
            sb.append(pr(Boolean.toString(arg.required), reqMax));
        }
        
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Name: " + longName + ", value: " + 
                value + ", required: " + required + ", default: " + defaultValue;
    }
    
    boolean isRequired() {
        return required;
    }

    boolean isThisYou(String s) {
        if(ok(shortName) && s.equals("-" + shortName))
            return true;
    
        if(s.equals("--" + longName))
            return true;
        
        return false;
    }
    
    public String getValue() {
        return value;
    }
    
    boolean requiresParameter() {
        return true;
    }
    
    private boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    private static String pr(String s, int num) {
        return StringUtils.padRight(s, num);
    }
    
    public String longName;
    String shortName;
    String value;
    String defaultValue;
    boolean required;
    private String description;
    private static final String DESC = "Description";
    private static final String LONG = "Long Name";
    private static final String SHORT = "Short Name";
    private static final String REQ = "Required";
    private static final String DEF = "Default";
}
