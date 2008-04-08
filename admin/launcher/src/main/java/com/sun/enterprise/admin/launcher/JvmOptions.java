/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.enterprise.admin.launcher;

import com.sun.enterprise.universal.StringUtils;
import java.util.*;

/**
 *
 * @author bnevins
 */
class JvmOptions {

    JvmOptions(List<String> options) throws GFLauncherException {
        // We get them from domain.xml as a list of Strings
        // -Dx=y   -Dxx  -XXfoo -XXgoo=zzz -client  -server
        // Issue 4434 -- we might get a jvm-option like this:
        // <jvm-options>"-xxxxxx"</jvm-options> notice the literal double-quotes
        
        for (String s : options) {
            s = StringUtils.removeEnclosingQuotes(s);
            
            if (s.startsWith("-D")) {
                addSysProp(s);
            } else if (s.startsWith("-XX")) {
                addXxProp(s);
            } else if (s.startsWith("-X")) {
                addXProp(s);
            } else if (s.startsWith("-")) {
                addPlainProp(s);
            } else // TODO i18n
            {
                throw new GFLauncherException("UnknownJvmOptionFormat", s);
            }
        }
    }

    @Override
    public String toString() {
        List<String> ss = toStringArray();
        StringBuilder sb = new StringBuilder();
        for (String s : ss) {
            sb.append(s).append('\n');
        }
        return sb.toString();
    }

    List<String> toStringArray() {
        List<String> ss = new ArrayList<String>();

        Set<String> keys = xxProps.keySet();
        for (String name : keys) {
            String value = xxProps.get(name);
            if (value != null) {
                ss.add("-XX" + name + "=" + value);
            } else {
                ss.add("-XX" + name);
            }
        }

        keys = xProps.keySet();
        for (String name : keys) {
            String value = xProps.get(name);
            if (value != null) {
                ss.add("-X" + name + "=" + value);
            } else {
                ss.add("-X" + name);
            }
        }
        keys = plainProps.keySet();
        for (String name : keys) {
            String value = plainProps.get(name);
            if (value != null) {
                ss.add("-" + name + "=" + value);
            } else {
                ss.add("-" + name);
            }
        }
        keys = sysProps.keySet();
        for (String name : keys) {
            String value = sysProps.get(name);
            if (value != null) {
                ss.add("-D" + name + "=" + value);
            } else {
                ss.add("-D" + name);
            }
        }
        postProcessOrdering(ss);
        return ss;
    }

    Map<String, String> getCombinedMap() {
        // used for resolving tokens
        Map<String, String> all = new HashMap<String, String>(plainProps);
        all.putAll(xProps);
        all.putAll(xxProps);
        all.putAll(sysProps);
        return all;
    }

    private void addPlainProp(String s) {
        s = s.substring(1);
        NameValue nv = new NameValue(s);
        plainProps.put(nv.name, nv.value);
    }

    private void addSysProp(String s) {
        s = s.substring(2);
        NameValue nv = new NameValue(s);
        sysProps.put(nv.name, nv.value);
    }

    private void addXProp(String s) {
        s = s.substring(2);
        NameValue nv = new NameValue(s);
        xProps.put(nv.name, nv.value);
    }

    private void addXxProp(String s) {
        s = s.substring(3);
        NameValue nv = new NameValue(s);
        xxProps.put(nv.name, nv.value);
    }

    void addJvmLogging() {
        xxProps.put(":+UnlockDiagnosticVMOptions", null);
        xxProps.put(":+LogVMOutput", null);
        xxProps.put(":LogFile", "${com.sun.aas.instanceRoot}/logs/jvm.log");
    }
    void removeJvmLogging() {
        xxProps.remove(":+UnlockDiagnosticVMOptions");
        xxProps.remove(":+LogVMOutput");
        xxProps.remove(":LogFile");
    }
    private void postProcessOrdering(List<String> ss) {
        /*
         * (1)
         * JVM has one known order dependency.
         * If these 3 are here, then unlock MUST appear first in the list
         * -XX:+UnlockDiagnosticVMOptions
         * -XX:+LogVMOutput
         * -XX:LogFile=D:/as/domains/domain1/logs/jvm.log 
         * 
         * (2)
         *  TODO Get the name of the instance early.  We no longer send in the
         *  instanceRoot as an arg so -- ????
         */

        String arg = "-XX:+UnlockDiagnosticVMOptions";
        int index = ss.indexOf(arg);

        // if < 0, it isn't here.  if == 0 then it's already in the right position
        if (index > 0) {
            ss.remove(index);
            ss.add(0, arg);
        }
    }
    Map<String, String> sysProps = new HashMap<String, String>();
    Map<String, String> xxProps = new HashMap<String, String>();
    Map<String, String> xProps = new HashMap<String, String>();
    Map<String, String> plainProps = new HashMap<String, String>();

    private static class NameValue {

        NameValue(String s) {
            int index = s.indexOf("=");

            if (index < 0) {
                name = s;
            } else {
                name = s.substring(0, index);
                if (index + 1 < s.length()) {
                    value = s.substring(index + 1);
                }
            }
        }
        private String name;
        private String value;
    }
}

/**
 * Reference Section
<jvm-options>-XX:+UnlockDiagnosticVMOptions</jvm-options>
<jvm-options>-XX:+LogVMOutput</jvm-options>
<jvm-options>-XX:LogFile=${com.sun.aas.instanceRoot}/logs/jvm.log</jvm-options> 
 */
