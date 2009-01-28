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
 * SomeClass.java
 *
 * Created on July 28, 2004, 12:35 AM
 */

package com.sun.enterprise.tools.upgrade.transform;

/**
 *
 * @author  prakash
 */
import java.io.*;
import java.util.*;
public class AttributeExtracter {
    
    private String fileName;
    private static AttributeExtracter attrExtracter;
    private HashMap attributeMap;
    /** Creates a new instance of SomeClass */
    private AttributeExtracter(String fileName) {
        this.fileName = fileName;
        attributeMap = new HashMap();
    }
    public static AttributeExtracter getExtracter(String fileName){
        if(attrExtracter == null){
            attrExtracter = new AttributeExtracter(fileName);
        }
        return attrExtracter;
    }
    public java.util.List getAttributeList(String elementName){
        if(attributeMap.get(elementName) != null)
            return (List)attributeMap.get(elementName);
        ArrayList attrList = new ArrayList();
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            String readLine = null;
            while((readLine = reader.readLine())!= null){
                String attrLine = this.getAttributeLine(elementName, readLine);
                if(attrLine == null)
                    continue;
                if((attrLine.length() > 0) && (!attrLine.trim().equals("")))
                    attrList.add(this.getAttributeFromLine(attrLine));
                this.extractAttributes(reader, attrList);
            }
            this.attributeMap.put(elementName, attrList);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return attrList;
    }
    private String getAttributeLine(String elementName, String line){
        if(line.startsWith("<!ATTLIST")){
            String atLine = line.substring("<!ATTLIST".length()).trim();
            StringTokenizer stk = new StringTokenizer(atLine);
            if(stk.nextToken().equals(elementName))
                return atLine.substring(elementName.length());
        }
        return null;
    }
    private String getAttributeFromLine(String attrLine){
        StringTokenizer stk = new StringTokenizer(attrLine);
        return stk.nextToken();
    }
    private void extractAttributes(BufferedReader reader, List attrList) throws Exception{
        for(int i=0; i<50; i++){
            String attrLine = reader.readLine().trim();
            attrList.add(getAttributeFromLine(attrLine.trim()));
            if(attrLine.endsWith(">"))
                break;
        }
    }
    public static void main(String[] args){
    }
}
