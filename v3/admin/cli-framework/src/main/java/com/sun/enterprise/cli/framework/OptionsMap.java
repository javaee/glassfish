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

package com.sun.enterprise.cli.framework;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;


/**
 *    OptionMap Class
 *    @version  $Revision: 1.1 $
 */
public final class OptionsMap
{

    private Map<String, Value> optionsMap = null;

    public OptionsMap()
    {
        optionsMap = new HashMap<String, Value>();
    }
    

    
        /**
         * return option value
         */
    public String getOption(final String name)
    {
        if (!optionsMap.containsKey(name)) return null;
        return optionsMap.get(name).getValue();
    }


        /**
         *  return all options
         */
    public Map<String, String> getOptions()
    {
        Map<String, String> tempOptions = new HashMap<String, String>();
        final Iterator<String> optionNames = optionsMap.keySet().iterator();
        while (optionNames.hasNext())
        {
            final String optionKey = (String) optionNames.next();
            tempOptions.put(optionKey, optionsMap.get(optionKey).getValue());
        }
        return tempOptions;
    }
    

        /**
         * return all the command line options
         **/
    public Map<String,String> getCLOptions()
    {
        return getOptions(OptionValueFrom.CommandLine);
    }

    
        /**
         * return all the environment options
         **/
    public Map<String, String> getEnvOptions()
    {
        return getOptions(OptionValueFrom.Environment);
    }

    
        /**
         * return all the options with the default values
         **/
    public Map<String, String> getDefaultOptions()
    {
        return getOptions(OptionValueFrom.Default);
    }

    
        /**
         * return all the options that are set by thecommand module
         **/
    public Map<String, String> getOtherOptions()
    {
        return getOptions(OptionValueFrom.Other);
    }

    
    private Map<String, String> getOptions(final OptionValueFrom ovf)
    {
        Map<String, String> tempOptions = new HashMap<String, String>();
        final Iterator<String> optionNames = optionsMap.keySet().iterator();
        while (optionNames.hasNext())
        {
            final String optionKey = (String) optionNames.next();
            if (optionsMap.get(optionKey).getOptionValueFrom() == ovf)
            {
                tempOptions.put(optionKey, optionsMap.get(optionKey).getValue());
            }
        }
        return tempOptions;
    }
    

    /**
       add option to optionsMap.  If option name already exists, it
       will be replaced.
       @param name  option name
       @param value   option value
       @param ovf  option value from 
     */
    public void addOptionValue(final String name, final String value,
                          final OptionValueFrom ovf) 
    {
        optionsMap.put(name, new Value(value, ovf));
    }

    
    /**
       add option to optionsMap.  If option name already exists, it
       will be replaced.
       @param name  option name
       @param value   option value
     */
    public void addOptionValue(final String name, final String value)
    {
        optionsMap.put(name, new Value(value, OptionValueFrom.Other));
    }


    
    /**
       add CL option to optionsMap.  If option name already exists, it
       will be replaced.
       @param name  option name
       @param value   option value
       *param ovf  option value from 
     */
    public void addCLValue(final String name, final String value)
    {
        optionsMap.put(name, new Value(value, OptionValueFrom.CommandLine));
    }


    /**
       add Environment option to optionsMap.  If option name already exists, it
       will be replaced.
       @param name  option name
       @param value   option value
       *param ovf  option value from 
     */
    public void addEnvValue(final String name, final String value)
    {
        optionsMap.put(name, new Value(value, OptionValueFrom.Environment));
    }

    
    /**
       add Default option to optionsMap.  If option name already exists, it
       will be replaced.
       @param name  option name
       @param value   option value
       *param ovf  option value from 
     */
    public void addDefaultValue(final String name, final String value)
    {
        optionsMap.put(name, new Value(value, OptionValueFrom.Default));
    }


    public void addPrefValue(final String name, final String value)
    {
        optionsMap.put(name, new Value(value, OptionValueFrom.PrefFile));
    }


    public boolean containsName(final String name)
    {
        return optionsMap.containsKey(name);
    }


    public Set<String> nameSet()
    {
        return optionsMap.keySet();
    }
    

        /**
         * remove the entry from optionsMay
         */
    public void remove(final String name)
    {
        optionsMap.remove(name);
    }

    
    public String toString()
    {
        final Iterator optionNames = optionsMap.keySet().iterator();
        StringBuffer strbuf = new StringBuffer();
        
        while (optionNames.hasNext())
        {
            final String optionKey = (String) optionNames.next();
            strbuf.append("<"+optionKey+","+optionsMap.get(optionKey).getValue()+">");
            strbuf.append("\n");
        }
        return strbuf.toString();
    }
    
    
    private static class Value 
    {
        private String value;
        private OptionValueFrom ovf;

        Value(final String value, final OptionValueFrom ovf)
        {
            this.value = value;
            this.ovf = ovf;
        }
        

        /**
           Gets the value of the option
           @return the value of the option
        */
        String getValue()
        {
            return value;
        }
    
        /**
           Gets the OptionValueFrom
           @return OptionValueFrom
        */
        public OptionValueFrom getOptionValueFrom()
        {
            return ovf;
        }
    }

        //values from "Other" is for option valuel set during runtime from the Command module
    public enum OptionValueFrom { CommandLine, Environment, Default, PrefFile, Other}
}
