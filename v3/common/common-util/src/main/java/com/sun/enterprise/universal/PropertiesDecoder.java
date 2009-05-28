/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.universal;

import java.util.*;

/**
 *
 * @author Byron Nevins
 */

public class PropertiesDecoder {
     /**
      * There are several CLI commands that take properties arguments.  The properties
      * are "flattened". This class will unflatten them back into a Map for you.
      * <p>Example Input:  <b>foo=goo:xyz:hoo=ioo</b>
      *  <p>Output would be 3 pairs:
      * <ul>
      *  <li>foo, goo
      *  <li>xyz, null
      *  <li>hoo, ioo
      *  </ul>
      * @param props The flattened string properties
      * @return A Map of the String keys and values.  It will return an
      */
    
    public static Map<String,String> unflatten(final String s) {
        if(!ok(s))
            return Collections.emptyMap();

        Map<String,String> map = new HashMap<String,String>();
        String[] elements = s.split(":");

        for(String element : elements) {
            addPair(map, element);
        }

        return map;
    }

    private static void addPair(Map<String, String> map, String element) {
        // TODO this method is a perfect candidate for unit tests...
        // note: It is quite tricky and delicate finding every possible weirdness
        // that a user is capable of!

        // element is one of these:
        // 0.   ""
        // 1.   "foo"
        // 2.   "foo=goo"
        // 3.   "foo="
        // if we get garbage like a=b=c=d  we change to "a", "b=c=d"

        // 0.
        if(!ok(element))
            return; // no harm, no foul

        int index = element.indexOf("=");

        // 1.
        if(index < 0)
            map.put(element, null);

        // 3.
        else if(element.length() - 1 <= index ) {
            // lose the '='
            map.put(element.substring(0, index), null);
        }
        // 2
        else // guarantee:  at least one char after the '='
            map.put(element.substring(0, index), element.substring(index + 1));
    }


    private static boolean ok(String s) {
        return s != null && s.length() > 0;
    }
}
