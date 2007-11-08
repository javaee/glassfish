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
 package com.sun.jbi.jsf.handlers;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.sun.data.provider.RowKey;
import com.sun.data.provider.TableDataProvider;
import com.sun.jbi.jsf.bean.ArchiveBean;
import com.sun.jbi.jsf.bean.JBIComponentConfigBean;
import com.sun.jbi.jsf.bean.ComponentConfigurationEntry;
import com.sun.jbi.jsf.bean.ShowBean;
import com.sun.jbi.jsf.util.BeanUtilities;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import com.sun.jsftemplating.component.dataprovider.MultipleListDataProvider;
import com.sun.webui.jsf.component.TableRowGroup;
import com.sun.data.provider.impl.ObjectListDataProvider;

import com.sun.jbi.ui.common.JBIAdminCommands;

public class ComponentConfigPropsTableHandler {

    private static Logger logger = Logger.getLogger(ComponentConfigPropsTableHandler.class.getName());

    /** Creates a new instance of TableHandler */
    public ComponentConfigPropsTableHandler() {
    }




    /**
     *  <p> This handler returns the selected row keys.</p>
     *
     *  @param  context The HandlerContext.
     */
    @Handler(id="jbiGetSelectedTableRowKeys",
        input={
            @HandlerInput(name="tableRowGroup", type=TableRowGroup.class, required=true)},
        output={
            @HandlerOutput(name="rowKeys", type=RowKey[].class)})
    public static void jbiGetSelectedTableRowKeys(HandlerContext handlerCtx) {
        TableRowGroup trg =
            (TableRowGroup) handlerCtx.getInputValue("tableRowGroup");
        RowKey[] keys = trg.getSelectedRowKeys();
        logger.fine("ComponentConfigPropsTableHandler.jbiGetSelectedTableRowKeys() = " + keys);
        handlerCtx.setOutputValue("rowKeys", keys);
    }

    /**
     *  <p> This handler deletes the given <code>RowKey</code>s.</p>
     *
     *  @param  context The HandlerContext.
     */
    @Handler(id="jbiDeleteTableRows",
        input={
            @HandlerInput(name="tableRowGroup", type=TableRowGroup.class, required=true),
            @HandlerInput(name="rowKeys", type=RowKey[].class, required=true)})
    public static void jbiDeleteTableRows(HandlerContext handlerCtx) {
        TableRowGroup trg =
            (TableRowGroup) handlerCtx.getInputValue("tableRowGroup");
        RowKey[] keys = (RowKey []) handlerCtx.getInputValue("rowKeys");
        logger.fine("ComponentConfigPropsTableHandler.jbiDeleteTableRows() = " + keys);
        ObjectListDataProvider dp =
            (ObjectListDataProvider) trg.getSourceData();
        for (RowKey key : keys) {
            dp.removeRow(key);
            logger.fine("delete Row with id = " + key.getRowId());
        }
    }

    /**
     *  <p> This handler commits the changes to a <code>TableRowGroup</code>'s
     *      DataProvider.</p>
     *
     *  @param  context The HandlerContext.
     */
    @Handler(id="jbiCommitTableRowGroup",
        input={
            @HandlerInput(name="tableRowGroup", type=TableRowGroup.class, required=true)})
    public static void jbiCommitTableRowGroup(HandlerContext handlerCtx) {
        TableRowGroup trg =
            (TableRowGroup) handlerCtx.getInputValue("tableRowGroup");
        logger.fine("ComponentConfigPropsTableHandler.jbiCommitTableRowGroup() = " + trg);
        ObjectListDataProvider dp =
            (ObjectListDataProvider) trg.getSourceData();
        dp.commitChanges();
    }

    /**
     *  <p> This handler takes in a HashMap, the name-value pair being the Properties.
     *  It turns each name-value pair to one hashMap, representing one row of table data,
     *  and returns the list of Map.
     *
     *  <p> Output value: "TableList" -- Type: <code>java.util.List</code>/</p>
     *  @param  context The HandlerContext.
     */
    @Handler(id="getJBIComponentConfigTableList",
    output={
        @HandlerOutput(name="TableList", type=List.class)})
        public static void getJBIComponentConfigTableList(HandlerContext handlerCtx) {
        JBIComponentConfigBean  configBean = BeanUtilities.getJBIComponentConfigBean();
        ObjectListDataProvider provider =
            (ObjectListDataProvider)configBean.getComponentNewConfigurationData();
        List<ComponentConfigurationEntry> JbiXMLdata = provider.getList();
        List data = new ArrayList();
        logger.fine("ComponentConfigPropsTableHandler.getJBIComponentConfigTableList()");
        if(JbiXMLdata != null && JbiXMLdata.size() > 0){
            for(ComponentConfigurationEntry entry : JbiXMLdata){
                HashMap oneRow = new HashMap();
                oneRow.put("name", entry.getName());
                oneRow.put("value", entry.getDefaultValue());
                oneRow.put("selected", false);
                oneRow.put("rendered", false);
                oneRow.put("disabled", true);
                data.add(oneRow);
                logger.fine("property name =" + entry.getName()+ " def.  value =" + entry.getDefaultValue());
            }
        }
        handlerCtx.setOutputValue("TableList", data);
    }

/**
     *  <p> This handler takes TableRowGroup as input and returns a List of Map objects.
     *  <p> The List returned contains Map objects with each Map representing one single row.
     *  <p> This method only works for tables where each row consists of one single map
     *
     *  <p> Input  value: "TableRowGroup" -- Type: <code> com.sun.webui.jsf.component.TableRowGroup</code></p>
     *  <p> Output  value: "Rows" -- Type: <code> java.util.List</code></p>
     *  @param  context The HandlerContext.
     */
    @Handler(id="jbiGetAllSingleMapRows",
    input={
        @HandlerInput(name="TableRowGroup", type=TableRowGroup.class, required=true)},
    output={
        @HandlerOutput(name="Rows", type=List.class)})
        public static void jbiGetAllSingleMapRows(HandlerContext handlerCtx) {

        TableRowGroup trg = (TableRowGroup)handlerCtx.getInputValue("TableRowGroup");
        MultipleListDataProvider dp = (MultipleListDataProvider)trg.getSourceData();
        List<List<Object>> data = dp.getLists();
        if ((null != data)
            && (0 < data.size()))
        {
           logger.fine("ComponentConfigPropsTableHandler.jbiGetAllSingleMapRows() = " + data.get(0));

           handlerCtx.setOutputValue("Rows", data.get(0));
        }
    }

    /**
     *  <p> This handler adds one row to  table
     *  <p> Input  value: "TableRowGroup" -- Type: <code> com.sun.webui.jsf.component.TableRowGroup</code></p>
     *  <p> Input value: "NameList" -- Type:<code>java.util.List</code></p>
     *  <p> Input value: "DefaultValueList" -- Type:<code>java.util.List</code></p>
     *  <p> Input value: "HasSelected" -- Type:<code>java.lang.Boolean</code></p>
     *  @param  context The HandlerContext.
     */
    @Handler(id="addRowToJBIComponentPropertiesTable",
    input={
        @HandlerInput(name="TableRowGroup", type=TableRowGroup.class, required=true),
        @HandlerInput(name="NameList", type=List.class),
        @HandlerInput(name="HasSelected", type=Boolean.class),
        @HandlerInput(name="DefaultValueList", type=List.class)} )
        public static void addRowToJBIComponentPropertiesTable(HandlerContext handlerCtx) {
        logger.fine("ComponentConfigPropsTableHandler.addRowToJBIComponentPropertiesTable()");
        TableRowGroup trg = (TableRowGroup)handlerCtx.getInputValue("TableRowGroup");
        List names = (List)handlerCtx.getInputValue("NameList");
        List defaults = (List)handlerCtx.getInputValue("DefaultValueList");
        Boolean hasSelected = (Boolean)handlerCtx.getInputValue("HasSelected");
        MultipleListDataProvider dp = (MultipleListDataProvider)trg.getSourceData();
        List<List<Object>> data = dp.getLists();
        ListIterator li = data.listIterator();
        while(li.hasNext()) {
            String name = null;
            String value = null;
            List list = (List)li.next();
            Map map = new HashMap<String, Object>();
            if(defaults != null && names != null) {
               if(names.size() == defaults.size()) {
                ListIterator ni = names.listIterator();
                ListIterator dv = defaults.listIterator();
                while(ni.hasNext() && dv.hasNext()) {
                    name = (String)ni.next();
                    value = (String)dv.next();
                    map.put(name, value);
                }

               } else {
                    ListIterator ni = names.listIterator();
                    while(ni.hasNext()) {
                    name = (String)ni.next();
                    map.put(name, " ");
                }
            }
            }
            if( names != null && defaults == null) {
                ListIterator ni = names.listIterator();
                while(ni.hasNext()) {
                    name = (String)ni.next();
                    map.put(name, " ");
                }
            }
            if(names == null && defaults == null) {
                map.put("name", " ");
                map.put("value", " ");
            }
            if(hasSelected == null) {
                    map.put("selected", false);
            } else {
                if(hasSelected.booleanValue()) {
                    map.put("selected", false);
                }
            }
            // add row has it checkbox  alway rendered
            map.put("rendered", true);
            // add row has it name columns  alway enabled
            map.put("disabled", false);
            list.add(0, map);
            logger.fine("added property named =" + name + " with value = " + value);

        }

    }


 /**
     *  <p> This handler returns the properties to be removed and added.</p>
     *
     *  @param  context The HandlerContext.
     */
    @Handler(id="getAddRemoveJBIComponentProps",
        input={
            @HandlerInput(name="NewList", type=List.class, required=true),
            @HandlerInput(name="NameList", type=ArrayList.class, required=true)},
        output={
            @HandlerOutput(name="AddProps", type=Map.class),
            @HandlerOutput(name="RemoveProps", type=ArrayList.class)})
        public static void getAddRemoveJBIComponentProps(HandlerContext handlerCtx) {
        logger.fine("ComponentConfigPropsTableHandler.getAddRemoveJBIComponentProps()");
        List newList = (List)handlerCtx.getInputValue("NewList");
        ArrayList names = (ArrayList)handlerCtx.getInputValue("NameList");
         JBIComponentConfigBean  configBean = BeanUtilities.getJBIComponentConfigBean();
        ObjectListDataProvider provider = (ObjectListDataProvider)configBean.getComponentNewConfigurationData();
        List<ComponentConfigurationEntry> origList = provider.getList();

        ListIterator nli = newList.listIterator();
        ArrayList removeProps = new ArrayList();
        Map addProps = new HashMap<String, String>();
        while(nli.hasNext()) {
            Map props = (Map)nli.next();
            if(!isOriginalEntry(origList,(String)props.get("name"))){
                String name = (String)props.get("name");
                if (name != null && (! name.trim().equals(""))) {
                    addProps.put((String)props.get("name"), (String)props.get("value"));
                    String value = (String)props.get("value");
                    value = value == null ? "" : value;
                    ComponentConfigurationEntry entry =
                        new ComponentConfigurationEntry(name,value,value,true);
                    origList.add(entry);
                    logger.fine("add new  property named =" + props.get("name") + " with value=" + props.get("value"));
                }
            } else {
                UpdateOriginalEntry(origList,(String)props.get("name"),(String)props.get("value"));
                logger.fine("updating property named =" + props.get("name") + " with value=" + props.get("value"));

            }
        }
        provider.commitChanges();
        handlerCtx.setOutputValue("AddProps", addProps);
        handlerCtx.setOutputValue("RemoveProps", removeProps);
    }

  /**
     * <p> This handler converts the table List to a Property map.
     *
     *  @param  context The HandlerContext.
     */
    @Handler(id="convertRowsToJBIComponentProperties",
        input={
            @HandlerInput(name="NewList", type=List.class, required=true)},
        output={
            @HandlerOutput(name="AddProps", type=Map.class)})
        public static void convertRowsToProperties(HandlerContext handlerCtx) {
        List newList = (List)handlerCtx.getInputValue("NewList");
        ListIterator li = newList.listIterator();
        Map addProps = new HashMap<String, String>();
        while(li.hasNext()) {
            Map props = (Map)li.next();
               String name = (String)props.get("name");
                if (name != null && (! name.trim().equals(""))) {
                    addProps.put(name, (String)props.get("value"));
                }
        }
        handlerCtx.setOutputValue("AddProps", addProps);
    }

  /**
     * <p> This handler converts the table List to a Properties map.
     *
     *  @param  context The HandlerContext.
     */
    @Handler(id="getJBIComponentProperties",
        input={
            @HandlerInput(name="NewList", type=List.class, required=true)},
        output={
            @HandlerOutput(name="AddProps", type=Map.class)})
    public static void getJBIComponentProperties(HandlerContext handlerCtx) {
        logger.fine("ComponentConfigPropsTableHandler.getJBIComponentProperties()");
        List newList = (List)handlerCtx.getInputValue("NewList");
        ListIterator li = newList.listIterator();
        Map addProps = new Properties();
        while(li.hasNext()) {
            Map props = (Map)li.next();
               String name = (String)props.get("name");
                if (name != null && (! name.trim().equals(""))) {
                                        String value = (String)props.get("value");
                    if (value != null && (! value.trim().equals(""))) {
                        addProps.put(name, value);
                        logger.fine("add to map property named =" + name + " and value=" + value);
                    }
                }
        }
        handlerCtx.setOutputValue("AddProps", addProps);
    }



    private static boolean isOriginalEntry(List<ComponentConfigurationEntry> oldList,
            String newPropName) {

        for (Iterator iter = oldList.iterator(); iter.hasNext();) {
            ComponentConfigurationEntry entry = (ComponentConfigurationEntry) iter.next();
            if(entry.getName().equals(newPropName)) {
                return true;
            }
        }
        return false;
    }

    private static void UpdateOriginalEntry(List<ComponentConfigurationEntry> oldList,
            String propName,String newValue) {

        for (Iterator iter = oldList.iterator(); iter.hasNext();) {
            ComponentConfigurationEntry entry = (ComponentConfigurationEntry) iter.next();
            if(entry.getName().equals(propName)) {
                entry.setNewValue(newValue);
            }
        }
     }


}
