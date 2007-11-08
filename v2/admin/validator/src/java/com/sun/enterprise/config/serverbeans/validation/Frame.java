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

package com.sun.enterprise.config.serverbeans.validation;

import java.util.TreeMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;



/**
 * Frame.java
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.3 $
 */

class Frame {
      // The top frames have a special parent - this one responds to
      // "lookup(s)" by merely looking in the environment and/or
      // returning the key as a variable.
    static Frame newFrame(){
        return new Frame(
                new Frame(){
                    String lookup(String s){
                    return (System.getProperty(s) != null
                            ? System.getProperty(s)
                            : "${"+s+"}");
                    }
                    public String toString(){
                        return "[]";
                    }
                }
                );
    }
    static Frame newFrame(Frame f){
        return new Frame(f);
    }
    String lookup(String s){
        return (map.get(s) != null ? (String) map.get(s) : parent.lookup(s));
    }
    
    Frame put(String key, String value){
        map.put(key, value);
        return this;
    }

    Frame inheritFrom(Frame f) throws IllegalArgumentException {
        if (contains(getAncestors(),f) || contains(f.getAncestors(),this)){
            throw new IllegalArgumentException("Inheriting from an ancestor is illegal - it causes loops!");
        }
        parent = f;
        return this;
    }

    private boolean contains(Set s, Frame f){
        for (Iterator it = s.iterator(); it.hasNext();){
            if (it.next() == f) {
                return true;
            }
        }
        return false;
    }

    public String toString(){
        StringBuffer sb = new StringBuffer("[");
        for (Iterator it = map.entrySet().iterator(); it.hasNext();){
            Map.Entry e = (Map.Entry) it.next();
            sb.append(e.getKey() + "->" + e.getValue());
            if (it.hasNext()){
                sb.append(", ");
            }
        }
        sb.append(" "+parent +"]");
        return sb.toString();
    }
    
        
    public boolean equals(Object o){
        return this == o || (o != null && o instanceof Frame && this.equals((Frame) o));
    }
      /*
       * The contract for equals and hashcode is:
       * FORALL a, b . a.equals(b) => a.hashCode() == b.hashCode()
       *
       * This will be true in this case because only when two frames
       * have equal maps (amongst other things) can they be
       * equal(), therefore the map hashCodes will be ==.
      */
    public int hashCode(){
        return map.hashCode();
    }
    
    private boolean equals(Frame o){
        return o != null
        && (this.map.equals(o.map)
            && ((this.parent == null && o.parent == null) ||
                (this.parent != null  && this.parent.equals(o.parent))));
    }


    
    private Frame(){}
    private Frame(Frame f){
        parent = f;
    }
    
    private Map map = new TreeMap();
    private Frame parent;
    
    private Set getAncestors(){
        if (parent == null){
            return new HashSet();
        } else {
            Set ancestors = parent.getAncestors();
            ancestors.add(this);
            return ancestors;
        }
            
    }
    
}

