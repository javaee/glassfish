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

package com.sun.enterprise.admin.meta;

//jdk imports
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import com.sun.enterprise.util.LocalStringManagerImpl;

// config imports
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.serverbeans.*;

//JMX
import javax.management.Attribute;
import javax.management.AttributeList;

/**
 *  This class provides helper methods for xpath handling in ConfigBeans space
 *
 *  $Id: XPathHelper.java,v 1.1 2006/05/08 17:18:53 kravtch Exp $
 *  @author alexkrav
 *
 *
*/

public abstract class XPathHelper {
    
    /**
     * This method returns number of elements in given xpath (depth)
     *
     **/
    public static int getNumberOfElemTokens(String xpath)
    {
        int[] cuts =  getElemDelimitersPositions(xpath);
        int iTokens = cuts.length;
        if(iTokens>0 && cuts[0]==0)
            iTokens--;
        if(cuts.length>0 && cuts[cuts.length-1]<xpath.length()-1)
           iTokens++;
        return iTokens;
    }

    public static String getXPathPrefix(String xpath, int numberOfTokensInPrefix)
    {
        if(numberOfTokensInPrefix<=0)
            return "";
        int[] cuts =  getElemDelimitersPositions(xpath);
        int delimIdx = numberOfTokensInPrefix;
//System.out.println("$$getXPathPrefix() xpath="+xpath + "   numberOfTokensInPrefix=" + numberOfTokensInPrefix);
        if(cuts.length>0 && cuts[0]!=0)
            delimIdx--;
        if(delimIdx<cuts.length)
            return xpath.substring(0, cuts[delimIdx]);
        if(delimIdx==cuts.length && cuts[cuts.length-1]<xpath.length()-1)
            return xpath;
        return "";
    }

    public static String[] extractTokens(String xpath)
    {
        ArrayList arr = new ArrayList();
        int[] cuts =  getElemDelimitersPositions(xpath);
        int iTokenPos = 0;
        for(int i=0; i<cuts.length; i++)
        {
            if(iTokenPos==cuts[i]) //empty element
            {
                arr.add("");
            }
            else
            {
                arr.add(xpath.substring(iTokenPos, cuts[i]));
            }
            iTokenPos =  cuts[i] +1;
        }
        if(iTokenPos<xpath.length()-1)
        {
            arr.add(xpath.substring(iTokenPos));
        }
        return (String[])arr.toArray(new String[arr.size()]);
    }

    public static int[] getElemDelimitersPositions(String xpath)
    {
         return getElemDelimitersPositions(xpath, -1);
    }

    public static int[] getElemDelimitersPositions(String xpath, int maxDeep)
    {
        int[] arr = new int[100];
        int length = 0;
        char[] buf = xpath.toCharArray();
        int i=0;
        int iNext;
        while(maxDeep!=0 && (iNext=getNextElemDelimiterPos(buf, i))>=0)
        {
            //add token boundary to list
            arr[length++] = iNext; 
            maxDeep--;
            i = iNext+1;    
        }
        int[] ret_arr = new int[length];
        for(i=0; i<length; i++)
            ret_arr[i] = arr[i];
        return ret_arr;
    }

    public static boolean isAbsoluteXPath(String xpath)
    {
        return (xpath!=null && xpath.startsWith("/"));
    }
    
    private static int getNextElemDelimiterPos(char[] buf, int iFrom)
    {
        int i = iFrom;
        while(i>=0 && i<buf.length)
        {
            if(buf[i]=='/')
                return i;
            if(buf[i]=='\'' || buf[i]=='"')
            {
                i = indexOf(buf, i+1, buf[i]); //in suggestion that there is no similar symbols inside 
                if(i>0)
                   i++;
                continue;
            }
            i++;
        }
        return -1;
    }
    
    private static int indexOf(char[] buf, int iFrom, char chr)
    {
        for(int i=iFrom; i<buf.length; i++)
        {
            if(buf[i]==chr)
                return i;
        }
        return -1;
    }
    
    
    /**
     * Convert a DTD name into a bean name:
     *
     * Any - or _ character is removed. The letter following - and _
     * is changed to be upper-case.
     * If the user mixes upper-case and lower-case, the case is not
     * changed.
     * If the Word is entirely in upper-case, the word is changed to
     * lower-case (except the characters following - and _)
     * The first letter is always upper-case.
     */
    public static String convertName(String name) {
        CharacterIterator  ci;
        StringBuffer    n = new StringBuffer();
        boolean    up = true;
        boolean    keepCase = false;
        char    c;
        
        // hack need for schema2beans limitation.
        if(name.equals("property")) {
            name = "element-property";
        }
        
        ci = new StringCharacterIterator(name);
        c = ci.first();
        
        // If everything is uppercase, we'll lowercase the name.
        while (c != CharacterIterator.DONE) {
            if (Character.isLowerCase(c)) {
                keepCase = true;
                break;
            }
            c = ci.next();
        }
        
        c = ci.first();
        while (c != CharacterIterator.DONE) {
            if (c == '-' || c == '_')
                up = true;
            else {
                if (up)
                    c = Character.toUpperCase(c);
                else
                    if (!keepCase)
                        c = Character.toLowerCase(c);
                n.append(c);
                up = false;
            }
            c = ci.next();
        }
        return n.toString();
    }
    
    static AttributeList resolve(ConfigContext ctx, ArrayList xpathes, String onlyPrefix)
    {
        AttributeList merge_list = null;
        if(xpathes!=null)
        {
            merge_list = new AttributeList();
            for(int i=0; i<xpathes.size(); i++)
            {
                AttributeList list = resolve(ctx, (String)xpathes.get(i), onlyPrefix);
                if(list!=null)
                    merge_list.addAll(list);
            }
        }
        return merge_list;
    }

    static AttributeList resolve(ConfigContext ctx, String xpath, String prefixFilter)
    {
        if(xpath==null)
            return null;
        String[] elems  = extractTokens(xpath);
        if(elems.length==0 || elems[0].length()!=0)           
           return null;
        ConfigBean cb = null;
        try {
            cb = ctx.getRootConfigBean();
        } catch (ConfigException ce){
        }
        AttributeList list = new AttributeList();
        navigate(cb, elems, 1, prefixFilter, list);
        return list;
        
    }
    
    private static void addAttributeToList(String attrName, ConfigBean cb, AttributeList list, String xpath)
    {
        String attrValue = getBeanAttributeValue(cb, attrName);
        if(attrValue!=null)
        {
//System.out.println("   --------->>>addAttributeToList: "+xpath+"/@"+attrName + "=" + attrValue );        
            list.add(new Attribute(xpath+"/@"+attrName, attrValue)); 
        }
    }

    private static ConfigBean getNextBean(ConfigBean cb, String name)
    {
        try{
            if("..".equals(name))
               return (ConfigBean)cb.parent();
            return (ConfigBean)cb.getValue(convertName(name));
        } catch(Throwable t){
            return null;
        }
    }

    private static ConfigBean[] getNextBeans(ConfigBean cb, String name)
    {
        try{
            if("..".equals(name))
            {
               ConfigBean parent = (ConfigBean)cb.parent();
               if(parent==null)
                   return null;
               ConfigBean grand = (ConfigBean)parent.parent();
               if(grand==null)
                   return new ConfigBean[]{parent};
               return (ConfigBean[])grand.getValue(parent.name());
            }
            return (ConfigBean[])cb.getValues(convertName(name));
        } catch(Throwable t){
            return null;
        }
    }

    public static String getBeanAttributeValue(ConfigBean cb, String name)
    {
        try{
            return cb.getAttributeValue(convertName(name));
        } catch(Throwable t){
            return null;
        }
    }
    
    private static void navigate(ConfigBean cb, String[] tokens, int iCurrentToken, String prefixFilter, AttributeList list)
    {
        if(cb==null || tokens==null || tokens.length<=iCurrentToken)
            return;
        String xpath = cb.getXPath();
        if(xpath==null)
            return;
        
        //stop navigation according to prefix filter
        if(prefixFilter!=null && prefixFilter.length()>0)
        {  
            if(xpath.length()>=prefixFilter.length())
            {
                if(!xpath.startsWith(prefixFilter))
                    return;
            }
            else
            {
                if(!prefixFilter.startsWith(xpath))
                    return;
            }
        }
        
//System.out.println( "###>>navigate iCurrentToken=" + iCurrentToken + " cb=" + xpath );        
        //*********************
        //1. Maybe this is it
        if(iCurrentToken==tokens.length-1)
        {
            list.add(new Attribute(xpath, cb));
            return;
        }
        
        //goto the next token
        int iNextToken = iCurrentToken+1;
        String nextToken = tokens[iNextToken];
        //*********************
        //2. Maybe next token is attribute name
        if(iNextToken==(tokens.length-1) && nextToken.startsWith("@"))
        {
//            try {
                if("@*".equals(nextToken)) //all names
                {
                    String[] names = cb.getAttributeNames();
                    if(names.length>0)
                        for(int i=0; i<names.length; i++)
                            addAttributeToList(names[i], cb, list, xpath);
                }
                else //only one attrname
                    addAttributeToList(nextToken.substring(1), cb, list, xpath);
//            } catch (ConfigException ce) {
//            }
            return;     
        }
        //*********************************************
        //3. what if "//"-wildcard or "*" (all children)
        if(nextToken.length()==0 ||
           "*".equals(nextToken))
        {
            ConfigBean[] children = cb.getAllChildBeans();
            if(children!=null)
                for(int i=0; i<children.length; i++)
                {
                    if(nextToken.length()==0)
                        navigate(children[i], tokens, iCurrentToken , prefixFilter, list);
                    
                    navigate(children[i], tokens, iNextToken , prefixFilter, list);
                }
            return;
        }
        
        //************************************************
        //4. looks like new element. First, non-indexed
        if(!nextToken.endsWith("]"))
        {
            ConfigBean cbNext = getNextBean(cb, nextToken);
            if(cbNext!=null)
                navigate((ConfigBean)cbNext, tokens, iNextToken , prefixFilter, list);
            return;
        }
        
        //get index.
        int indexStartPos = nextToken.indexOf("[");
        String indexStr = nextToken.substring(indexStartPos+1, nextToken.length()-1);
        String baseName = nextToken.substring(0, indexStartPos);

        //************************************************
        //5. [*]?
        if("*".equals(indexStr)) //all of kind
        {
            ConfigBean[] beans = getNextBeans(cb, baseName);
            if(beans!=null)
            {
                for(int i=0; i<beans.length; i++)
                {
                    navigate(beans[i], tokens, iNextToken , prefixFilter, list);
                }
            }
            return;
        }
        
        //************************************************
        //5. [@xxx='yyy']?
        if(indexStr.startsWith("@") && indexStr.startsWith("'")) //all of kind
        {
            String attrName  = indexStr.substring(1, indexStr.indexOf('='));
            String attrValue = indexStr.substring(indexStr.indexOf("='")+2, indexStr.length()-2);
            ConfigBean[] beans = getNextBeans(cb, baseName);
            if(beans!=null)
            {
                for(int i=0; i<beans.length; i++)
                {
                    String value = getBeanAttributeValue(beans[i], convertName(attrName));
                    if(value==null)
                        continue;
                    if(attrValue.equals("*") || attrValue.equals(value))
                        navigate(beans[i], tokens, iNextToken , prefixFilter, list);
                }
            }
            return;
        }
        //6 [number] not yet
    }

     /**
      * get 
      */
/**
     public String[] getAttrValueAmongSiblings(ConfigBean cb) {
         ArrayList cbRet = new ArrayList();
         ConfigBean parent = (ConfigBean)this.parent();
         if(parent!=null)
         {
             String[] childNames = parent.getChildnt(BeanNames();
             if(childNames == null || childNames.length == 0) 
                 return null;
             ConfigBean[] cbs = parent.getChildBeansByName(this.name());
             if(cbs == null)
                 cbs=null;
         }
         return toConfigBeanArray(cbRet);
     }
 **/
}