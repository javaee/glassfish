
/*
 * SystemProps.java
 *
 * Created on October 2, 2001, 2:53 PM
 */

package com.sun.enterprise.universal;

import java.util.*;
//import com.sun.enterprise.util.StringUtils;

/**
 *
 * @author  bnevins
 * @version
 */
public class SystemProps
{
    public static List<Map.Entry> get()
    {
        Properties p = System.getProperties();
        // these 2 lines woul;d be nice -- but it's case-sensitive...
        //Map sortedMap = new TreeMap(p);
        //Set sortedSet = sortedMap.entrySet();
        Set<Map.Entry<Object, Object>>  set  = p.entrySet();
        List<Map.Entry>	list = new ArrayList<Map.Entry>(set);
        
        Collections.sort(list, new Comparator<Map.Entry>()
        {
            public int compare(Map.Entry me1, Map.Entry me2)
            {
                return ((String)me1.getKey()).compareToIgnoreCase((String)me2.getKey());
            }
        });
        
        return list;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public static String toStringStatic()
    {
        int             longestKey	= 0;
        List<Map.Entry>	list		= get();
        StringBuffer	sb		= new StringBuffer();
        
        /* Go through the list twice.  
         * The first time through gets the maximum length entry
         * The second time through uses that info for 'pretty printing'
         */
        
        for(Map.Entry entry : list)
        {
            int len = ((String)entry.getKey()).length();
            
            if(len > longestKey)
                longestKey = len;
        }
        
        longestKey += 1;
        
        for(Map.Entry entry : list)
        {
            sb.append(StringUtils.padRight((String)entry.getKey(), longestKey));
            sb.append("= ");
            sb.append((String)entry.getValue());
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private SystemProps()
    {
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public static void main(String[] args)
    {
        System.out.println(toStringStatic());
    }
}
