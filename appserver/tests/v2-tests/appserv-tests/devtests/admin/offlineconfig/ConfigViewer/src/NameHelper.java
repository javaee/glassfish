/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
 * NameHelper.java
 *
 * Created on April 21, 2006, 12:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
import com.sun.enterprise.admin.config.OfflineConfigMgr;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import javax.management.AttributeList;
import javax.management.Attribute;

/**
 *
 * @author kravtch
 */
public class NameHelper
{
    OfflineConfigMgr _mgr;
    
    /** Creates a new instance of NameHelper */
    public NameHelper(String fileName) throws Exception
    {
       _mgr = new OfflineConfigMgr(fileName);
    }
    
    void fillDottedNamesTree(DefaultMutableTreeNode root) 
       throws Exception
    {
       fillAddNodeChildren(root, ""); 
    }
    
    void fillAddNodeChildren(DefaultMutableTreeNode parentNode, 
                    String parentDottedName)
          throws Exception
    {
        ArrayList childList = _mgr.getListDottedNames(parentDottedName);
        for (int i=0; i<childList.size(); i++)
        {
            String childName = (String)childList.get(i);
            DefaultMutableTreeNode childNode = 
                    new DefaultMutableTreeNode(new DottedNameInfo(childName,parentDottedName));
            fillAddNodeChildren(childNode, childName);
            parentNode.add(childNode);
        }
    }
    

    Object[][] getAttributesForNodeInPrintForm(DefaultMutableTreeNode node, boolean bProperties) throws Exception
    {
        if(_mgr==null)
            return null;
        if(!(node.getUserObject() instanceof DottedNameInfo))
            return null;
        DottedNameInfo info = (DottedNameInfo)node.getUserObject();
        String name = info._name; //_mgr.removeNamePrefixes(info._name);
        AttributeList list = _mgr.getAttributes(name+(bProperties?".property.*":".*"));
        if(list==null)
            return null;
        Object[][] arr = new Object[list.size()][2];
        int iOffset = name.length()+1;
        if(bProperties)
            iOffset += "property.".length();
            
        for (int i=0; i<list.size(); i++)
        {
            Attribute attr = (Attribute)list.get(i);
            String attrName = attr.getName();
            arr[i][0] = attrName.substring(iOffset);
            Object value = null;
            try {
                value = attr.getValue();
            } catch (Exception e) {
                value = e;
            }
            arr[i][1] = attr.getValue();
        }
        return arr;
    }
    void setValue(DefaultMutableTreeNode node, String name, Object value, boolean bProperties)
        throws Exception
    {
        DottedNameInfo info = (DottedNameInfo)node.getUserObject();
        setValue(info, name, value, bProperties);
    }
    
    void setValue(DottedNameInfo info, String name, Object value, boolean bProperties)
        throws Exception
    {
        String dottedName = info._name; //_mgr.removeNamePrefixes(info._name);
        name = name.replaceAll("\\.","\\\\.");
        _mgr.setAttribute(dottedName+  (bProperties?".property.":".") + name, value);
    }
}
