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

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

/* ObjectNameGenerator.java
 * Indentation Information:
 * 0. Please (try to) preserve these settings.
 * 1. Spaces are preferred over tabs.
 * 2. In vi/vim -
 *		:set tabstop=4 :set shiftwidth=4 :set softtabstop=4
 *		:set list and make sure you do not introduce any ^Is.
 * 3. In S1 Studio -
 *		1. Tools->Options->Editor Settings->Java Editor->Tab Size = 4
 *		2. Tools->Options->Indentation Engines->Java Indentation Engine->Expand Tabs to Spaces = True.
 *		3. Tools->Options->Indentation Engines->Java Indentation Engine->Number of Spaces per Tab = 4.
 */

package com.sun.enterprise.admin.util.jmx;
import java.util.List;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import com.sun.org.apache.commons.modeler.Registry;
import com.sun.org.apache.commons.modeler.ManagedBean;
import com.sun.org.apache.commons.modeler.FieldInfo;

import com.sun.enterprise.admin.util.jmx.AttributeListUtils;
/**
 * @author  Kedar Mhaswade
 * @since Appserver 8.1
 * @version $Revision: 1.3 $
 */
public class ObjectNamesSourceGenerator {
	
    private final File regFile;
    private final OutputStream out;
    private final Registry reg;
    private String pkgName;
    private String className;
    private final static String PARAM = "param";
    private final static char ONDELIMITER = ':';
    private final static String ANY_GETTER = "getConfigBeanObjectName";
    private final static String DEFAULT_PACKAGE_NAME = null;
    private final static String DEFAULT_CLASS_NAME = "ConfigMBeanNames";
	public ObjectNamesSourceGenerator(final File regFile, final OutputStream out) throws Exception {
        if (regFile == null || !regFile.canRead() || out == null) {
            throw new IllegalArgumentException("Either the registry file is unreadable or stream could not be written to");
        }
        this.regFile = regFile;
        this.out = out;
        this.pkgName = DEFAULT_PACKAGE_NAME;
        this.className = DEFAULT_CLASS_NAME;
        this.reg = new Registry();
        //reg.loadRegistry(new BufferedInputStream(new FileInputStream(regFile)));
        final InputStream stream = new BufferedInputStream(new FileInputStream(regFile));
        reg.loadDescriptors("MbeansDescriptorsDOMSource", stream, null);
	}
    private void setClassName(final String c) {
        this.className = c;
    }
    private void setPackageName(final String p) {
        this.pkgName = p;
    }
    
    public String asString() throws Exception {
        final String[] s = reg.findManagedBeans();
        final StringBuffer sb = new StringBuffer();
        sb.append(getClassPreamble());
        for (int i = 0 ; i < s.length ; i ++) {
            sb.append(mb2s(s[i]));
        }
        sb.append('\n' + "}");
        return ( sb.toString() );
    }
    
    public void write() throws Exception {
        out.write(this.asString().getBytes());
    }
    private String getClassPreamble() {
        final StringBuffer sb = new StringBuffer();
        sb.append(getPackageDeclaration());
        sb.append(getImports());
        sb.append(getClassJavadoc());
        return ( sb.toString() );
    }
    private String getPackageDeclaration() {
        final StringBuffer sb = new StringBuffer();
        sb.append("package TBD...").append('\n');
        sb.append('\n');
        return ( sb.toString() );
    }
    private String getClassJavadoc() {
        final StringBuffer sb = new StringBuffer("/** An Auto-generated MBean ObjectName Factory */");
        sb.append('\n');
        sb.append("public class ConfigMBeanNames  {");
        sb.append('\n');
        return ( sb.toString() );
    }
    private String getImports() {
        return ("import javax.management.ObjectName;" + '\n');
    }
    private String mb2s(final String name) {
        final ManagedBean mb = reg.findManagedBean(name);
        final StringBuffer sb = new StringBuffer();
        final String method = getMethodSignature(mb);
        sb.append(method).append('\n');
        sb.append(getMethodBody(mb)).append('\n');
        //System.out.println(sb.toString());
        return ( sb.toString() );
    }
    private String getMethodSignature(final ManagedBean mb) {
        final StringBuffer sb = new StringBuffer("public static final ObjectName ");
        sb.append("get");
        final String humped = AttributeListUtils.dash2CamelCase(mb.getName());
        sb.append(getValidJavaMethodName(humped));
        sb.append("ObjectName(");
        final String onv = getObjectNameValue(mb);
        sb.append(getParameterList(onv));
        sb.append(" )");
        return ( sb.toString() );
    }
    private String getMethodBody(final ManagedBean mb) {
        final String onv = getObjectNameValue(mb);
        final String params = getParameterList(onv);
        final String nodm = onv.substring(onv.indexOf(ONDELIMITER) + 1);
        //System.out.println(nodm);
        //System.out.println(params);
        final String ong = positionalReplace(nodm);
        final String ret = getReturnStatement(ong);
        final StringBuffer sb = new StringBuffer("{").append('\n').append(ret).append('\n').append("}");
        return ( sb.toString() );
    }
    private String positionalReplace(final String tk) {
        final String rs = Tok2Params.replacePositionalTokens(tk);
        return ( rs );
    }
    private String getReturnStatement(final String s) {
        final StringBuffer sb = new StringBuffer();
        final String qs = "\"" + s + "\"";
        sb.append("return ( ").append(ANY_GETTER).append("( ").append(qs).append(" )").append(" )");
        return ( sb.toString() );
    }
    private String getObjectNameValue(final ManagedBean mb) {
        final List fields = mb.getFields();
        final Iterator iter = fields.iterator();
        String val = null;
        while (iter.hasNext()) {
            final FieldInfo f = (FieldInfo)iter.next();
            if ("ObjectName".equals(f.getName())) {
                val = (String) f.getValue();
            }
        }
        return ( val );
    }
    private String getValidJavaMethodName(final String s) {
        /* Currently '#' is the only culprit */
        String n = s.replace('#', '_');
        return ( n );
    }
    private String getParameterList(final String on) {
        return ( Tok2Params.convert2ParamList(on) );
    }
    public static void main(final String[] args) {
        try {
            final File r = new File("/export/home/kedar/ee/LATEST/admin-core/admin/dtds/admin-mbeans-descriptors.xml");
            final OutputStream op = System.out;
            final ObjectNamesSourceGenerator o = new ObjectNamesSourceGenerator(r, op);
            final String s = o.asString();
            op.write(s.getBytes());
            System.out.println("Done");
            System.out.println("The length of Java source file would be, Characters: " + s.length());
        }
        catch (final Throwable e) {
            e.printStackTrace();
        }
    }
    
    private static class Tok2Params {
        private final static String REGEX1 = "a*\\{\\d\\}a*";
        private static String convert2ParamList(final String s) {
            final Pattern p = Pattern.compile(REGEX1);
            final Matcher m = p.matcher(s);
            int n = 0;
            while (m.find()) {
                n++;
            }
            return ( getStringParamList(n-1) ) ;
        }
        private static String getStringParamList(final int n) {
            final StringBuffer sb = new StringBuffer();
            for (int i = 0 ; i < n ; i++) {
                sb.append(" final String ").append(PARAM).append(i+1);
                if (i != n-1) {
                    sb.append(',');
                }
            }
            return ( sb.toString() );
        }
        private static String replacePositionalTokens(final String ts) {
            final Pattern p = Pattern.compile(REGEX1);
            final Matcher m = p.matcher(ts);
            final StringBuffer sb = new StringBuffer();
            while(m.find()) {
                final String gr = m.group(); // this has to be of the form {d}
                final char ch = gr.charAt(1);
                final String replacement = PARAM + ch;
                m.appendReplacement(sb, replacement);
            }
            m.appendTail(sb);
            return ( sb.toString() );
        }
    }
}
