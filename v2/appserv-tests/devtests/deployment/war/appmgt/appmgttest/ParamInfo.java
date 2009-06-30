/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package appmgttest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author tjquinn
 */
public class ParamInfo {

    /** env pattern is name(class)=value//desc */
    private static final String BRIEF_PATTERN_STRING = "([^=]*)=(.*?)";
    private static final String FULL_PATTERN_STRING = BRIEF_PATTERN_STRING + "//(.*)";

    private static final Pattern BRIEF_PATTERN = Pattern.compile(BRIEF_PATTERN_STRING);
    private static final Pattern FULL_PATTERN = Pattern.compile(FULL_PATTERN_STRING);

    private final String name;
    private final String value;
    private final String desc;

    public ParamInfo(final String name, final String value, final String desc) {
        super();
        this.name = name;
        this.value = value;
        this.desc = desc;
    }

    public ParamInfo(final String name, final String value) {
        this(name, value, null);
    }

    public static ParamInfo parseFull(final String expr) throws ClassNotFoundException {
        return parse(expr, FULL_PATTERN);
    }

    public static ParamInfo parseBrief(final String expr) throws ClassNotFoundException {
        return parse(expr, BRIEF_PATTERN);
    }



    private static ParamInfo parse(final String expr, final Pattern p) throws ClassNotFoundException {
        Matcher m = p.matcher(expr);
        ParamInfo result = null;
        if (m.matches()) {
            result = new ParamInfo(m.group(1),
                    m.group(2), m.groupCount() > 2 ? m.group(3) : null);
        }
        return result;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("%1s$=%2$s//\"%3$s\"", name, value, desc);
    }

    public String toStringBrief() {
        return String.format("%1$s=%2$s", name, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ParamInfo other = (ParamInfo) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.value == null) ? (other.value != null) : !this.value.equals(other.value)) {
            return false;
        }
        if ((this.desc == null) ? (other.desc != null) : !this.desc.equals(other.desc)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 97 * hash + (this.value != null ? this.value.hashCode() : 0);
        hash = 97 * hash + (this.desc != null ? this.desc.hashCode() : 0);
        return hash;
    }

    
}
