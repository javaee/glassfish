/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.admin.launcher;

import java.util.*;

/**
 *
 * @author bnevins
 */
class JvmOptions {
    JvmOptions(List<String> options) {
        // We get them from domain.xml as a list of Strings
        // -Dx=y   -Dxx  -XXfoo -XXgoo=zzz -client  -server
        
        for(String s : options) {
            if(s.startsWith("-D"))
                addSysProp(s);
            else if(s.startsWith("-XX"))
                addXxProp(s);
            else if(s.startsWith("-X"))
                addXProp(s);
            else if(s.startsWith("-"))
                addPlainProp(s);
            else
                // TODO i18n
                throw new RuntimeException("Corrupt domain.xml");
        }
    }

    @Override
    public String toString()
    {
        List<String> ss = toStringArray();
        StringBuilder sb = new StringBuilder();
        for(String s : ss)
            sb.append(s).append('\n');
        return sb.toString();
    }
    
    List<String> toStringArray()
    {
        List<String> ss = new ArrayList<String>();
        
        Set<String> keys = xxProps.keySet();
        for(String name : keys)
        {
            String value = xxProps.get(name);
            if(value != null)
                ss.add("-XX" + name + "=" + value);
            else
                ss.add("-XX" + name);
        }
        keys = xProps.keySet();
        for(String name : keys)
        {
            String value = xProps.get(name);
            if(value != null)
                ss.add("-X" + name + "=" + value);
            else
                ss.add("-X" + name);
        }
        keys = plainProps.keySet();
        for(String name : keys)
        {
            String value = plainProps.get(name);
            if(value != null)
                ss.add("-" + name + "=" + value);
            else
                ss.add("-" + name);
        }
        keys = sysProps.keySet();
        for(String name : keys)
        {
            String value = sysProps.get(name);
            if(value != null)
                ss.add("-D" + name + "=" + value);
            else
                ss.add("-D" + name);
        }
        return ss;
    }
    Map<String,String> getCombinedMap() {
        // used for resolving tokens
        Map<String,String> all = new HashMap<String,String>(plainProps);
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
    
    Map<String,String> sysProps = new HashMap<String,String>();
    Map<String,String> xxProps = new HashMap<String,String>();
    Map<String,String> xProps = new HashMap<String,String>();
    Map<String,String> plainProps = new HashMap<String,String>();

    private static class NameValue {
        NameValue(String s) {
            int index = s.indexOf("=");
            
            if(index < 0) {
                name = s;
            }
            else {
                name = s.substring(0, index);
                if(index + 1 < s.length())
                    value = s.substring(index + 1);
            }
        }
        private String name;
        private String value;
    }
}
