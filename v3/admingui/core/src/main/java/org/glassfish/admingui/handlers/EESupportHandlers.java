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
 * EESupportHandlers.java
 *
 * Created on August 30, 2006, 4:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
/**
 *
 * @author anilam
 */

package org.glassfish.admingui.handlers;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.TargetUtil;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;


/**
 *
 * @author Anissa Lam
 */
public class EESupportHandlers {
    
    /** Creates a new instance of EESupportHandlers */
    public EESupportHandlers() {
    }
 
    /**
     *	<p> This handler returns a list of virtual server for a give target.
     *  <p> Output value: "serverList" -- Type: <code>java.util.List</code>/</p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getAllVSList",
        input={
        @HandlerInput(name="targetName", type=String.class, required=true ),
        @HandlerInput(name="PE", type=Boolean.class)},
        output={
        @HandlerOutput(name="serverList", type=java.util.List.class)})
    public static void getAllVSList(HandlerContext handlerCtx) {
	String targetName = (String)handlerCtx.getInputValue("targetName");
	Boolean PE = (Boolean)handlerCtx.getInputValue("PE");
        Set<String> vsList = TargetUtil.getVirtualServers(targetName);
        
        ArrayList<String> serverList = new ArrayList<String>(vsList);
	if(PE != null && PE) {
		//In the case of PE add one empty string, to reference to all VS
		String noneSelected = GuiUtil.getMessage("deploy.option.NoneSelected");	
		serverList.add(0, noneSelected);
	}
	//Removing system VS
	boolean remove = serverList.remove("__asadmin");
        handlerCtx.setOutputValue("serverList", serverList);
    }
    

    /**
     *	<p> This handler creates VS references for the given application/module name 
     *
     *  <p> Input value: "name" -- Type: <code>String</code>/</p>
     *  <p> Input value: "vsTargets" -- Type: <code>String[]</code>/</p>
     *  <p> Output value: "targetName" -- Type: <code>String</code>/</p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="createVSReferences",
        input={
        @HandlerInput(name="name", type=String.class, required=true),
        @HandlerInput(name="targetName", type=String.class, required=true),
        @HandlerInput(name="vsTargets", type=String[].class, required=true )})
    public static void createVSReferences(HandlerContext handlerCtx) {
        String name = (String)handlerCtx.getInputValue("name");
        String targetName = (String)handlerCtx.getInputValue("targetName");
		String[] selTargets = (String[])handlerCtx.getInputValue("vsTargets");
		String noneSelected = GuiUtil.getMessage("deploy.option.NoneSelected");
		String targets = null;
		if(selTargets.length > 0  && !noneSelected.equals(selTargets[0])) {
			targets = GuiUtil.arrayToString(selTargets, ",");
		}
		TargetUtil.setVirtualServers(name, targetName, targets);
	}



    /**
     *	<p> Returns the options list to add in addRemove component
     *
     *  <p> Input value: "allTargets" -- Type: <code>List</code>
     *          - the list to be added to the field.</p>
     *          - <code>java.util.List</code> 
     *  <p> Output value: "optionsList" -- Type: <code>Option</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "getVSOptions", 
    input = {
        @HandlerInput(name = "vsList", type = List.class, required = true),
        @HandlerInput(name = "PE", type = Boolean.class),
        @HandlerInput(name = "targetName", type = String.class, required = true),
        @HandlerInput(name = "name", type = String.class, required = true)}, 
    output = {
        @HandlerOutput(name = "availableVS", type = List.class),
        @HandlerOutput(name = "selectedVS", type = String[].class)
    })
    public static void getVSOptions(HandlerContext handlerCtx) {
        String targetName = (String) handlerCtx.getInputValue("targetName");
        Boolean pe = (Boolean) handlerCtx.getInputValue("PE");
        String name = (String) handlerCtx.getInputValue("name");
        List<String> vsList = (List) handlerCtx.getInputValue("vsList");
        String associatedVS = TargetUtil.getAssociatedVS(name, targetName);
        String[] selectedOptions = null;
        if (vsList != null) {
            selectedOptions = GuiUtil.stringToArray(associatedVS, ",");
            if (pe != null && pe) {
                if (selectedOptions != null && !(selectedOptions.length > 0)) {
                    //None is selected by default
                    selectedOptions = new String[]{vsList.get(0)};
                }
            }
        }
        handlerCtx.setOutputValue("availableVS", vsList);
        handlerCtx.setOutputValue("selectedVS", selectedOptions);
    }
}
