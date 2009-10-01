/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * CommonHandlers.java
 *
 * Created on August 30, 2006, 4:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.glassfish.admingui.handlers;


import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;


import com.sun.webui.jsf.component.Calendar;
import com.sun.webui.jsf.model.UploadedFile;
import com.sun.webui.jsf.component.Field;
import com.sun.webui.jsf.component.DropDown;
import com.sun.webui.jsf.component.Hyperlink;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import com.sun.webui.jsf.model.Option;
import com.sun.webui.jsf.model.OptionGroup;
import com.sun.webui.jsf.model.OptionTitle;


import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.glassfish.admingui.common.util.V3AMX;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.handlers.MonitoringHandlers;
import org.glassfish.admingui.util.SunOptionUtil;



public class WoodstockHandler {
    
    /** Creates a new instance of CommonHandlers */
    public WoodstockHandler() {
    }

    /**
     *	<p> This method uploads a file temp directory</p>
     *	<p> Input value: "file" -- Type: <code>com.sun.webui.jsf.model.UploadedFile</code></p>
     *	<p> Output value: "uploadDir" -- Type: <code>java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "uploadFileToTempDir",
    input = {
        @HandlerInput(name = "file", type = UploadedFile.class)},
    output = {
        @HandlerOutput(name = "origPath", type = String.class),
        @HandlerOutput(name = "uploadedTempFile", type = String.class)
    })
    public static void uploadFileToTempDir(HandlerContext handlerCtx) {
        UploadedFile uploadedFile = (UploadedFile) handlerCtx.getInputValue("file");
        File tmpFile = null;
        String uploadTmpFile = "";
        if (uploadedFile != null) {
            String name = uploadedFile.getOriginalName();
            //see bug# 6498910, for IE, getOriginalName() returns the full path, including the drive.
            //for any other browser, it just returns the file name.
            int lastIndex = name.lastIndexOf("\\");
            if (lastIndex != -1) {
                name = name.substring(lastIndex + 1, name.length());
            }
            int index = name.indexOf(".");
            if (index <= 0) {
                String mesg = GuiUtil.getMessage("msg.deploy.nullArchiveError");
                GuiUtil.handleError(handlerCtx, mesg);
                return;
            }
            String suffix = name.substring(index);
            String prefix = name.substring(0, index);
            handlerCtx.setOutputValue("origPath", prefix);
            try {
                //createTempFile requires min. of 3 char for prefix.
                if (prefix.length() <= 2) {
                    prefix = prefix + new Random().nextInt(100000);
                }
                tmpFile = File.createTempFile(prefix, suffix);
                tmpFile.deleteOnExit();
                uploadedFile.write(tmpFile);
                uploadTmpFile = tmpFile.getCanonicalPath();
            } catch (IOException ioex) {
                try {
                    uploadTmpFile = tmpFile.getAbsolutePath();
                } catch (Exception ex) {
                //Handle AbsolutePathException here
                }
            } catch (Exception ex) {
                GuiUtil.handleException(handlerCtx, ex);
            }
        }
        handlerCtx.setOutputValue("uploadedTempFile", uploadTmpFile);
    }


    /**
     *	<p> This handler enable or disable the table text field according to the method value.
     */
    @Handler(id="setDisableConnectionPoolTableField",
        input={
            @HandlerInput(name="tableDD", type=com.sun.webui.jsf.component.DropDown.class),
            @HandlerInput(name="validationField", type=com.sun.webui.jsf.component.Field.class),
            @HandlerInput(name="methodValue", type=String.class)}
        )
    public static void setDisableConnectionPoolTableField(HandlerContext handlerCtx) {
        String methodValue = (String) handlerCtx.getInputValue("methodValue");
        DropDown tableDD = (DropDown) handlerCtx.getInputValue("tableDD");
        Field validationField = (Field) handlerCtx.getInputValue("validationField");
        if ("table".equals(methodValue)) {
            tableDD.setDisabled(false);
            validationField.setDisabled(true);
        } else if ("custom-validation".equals(methodValue)) {
            tableDD.setDisabled(true);
            validationField.setDisabled(false);

        } else {
            tableDD.setDisabled(true);
            validationField.setDisabled(true);
        }
    }
    

    @Handler(id="createHyperlinkArray",
    output={
		@HandlerOutput(name="links", type=Hyperlink[].class)
    })
    public static void createHyperlinkArray(HandlerContext handlerCtx) {
        FacesContext ctx = handlerCtx.getFacesContext();
        ExternalContext extCtx = ctx.getExternalContext();
        Map<String, String[]> reqParams = extCtx.getRequestParameterValuesMap();
        String linkText[] = reqParams.get("text");
        String linkUrl[] = reqParams.get("urls");
        if (linkText == null) {
            // No data!  Should we add something here anyway?
            return;
        }

        int len = linkText.length;
        Hyperlink arr[] = new Hyperlink[len];
        String url = null;
        String ctxPath = extCtx.getRequestContextPath();
        int ctxPathSize = ctxPath.length();
        for (int idx=0; idx < len; idx++) {
            // FIXME: Set parent
            arr[idx] = new Hyperlink();
            arr[idx].setId("bcLnk" + idx);
            // Set rendererType to avoid using widget renderer!!
            arr[idx].setRendererType("com.sun.webui.jsf.Hyperlink");
            arr[idx].setText(linkText[idx]);
            url = linkUrl[idx];
            if (url.startsWith(ctxPath)) {
            url = url.substring(ctxPathSize);
            }
            arr[idx].setUrl(url);
        }
        handlerCtx.setOutputValue("links", arr);
    }



    @Handler(id="dummyHyperlinkArray",
    output={
		@HandlerOutput(name="links", type=Hyperlink[].class)
    })
    public static void dummyHyperlinkArray(HandlerContext handlerCtx) {
        Hyperlink arr[] = new Hyperlink[1];
        arr[0]=new Hyperlink();
        arr[0].setText(">");
        handlerCtx.setOutputValue("links", arr);
    }




     @Handler(id = "StringArrayToSelectItemArray",
    input = {
        @HandlerInput(name = "stringArray", type = String[].class, required = true)},
    output = {
        @HandlerOutput(name = "item", type = SelectItem[].class)})
    public static void StringArrayToSelectItemArray(HandlerContext handlerCtx) {

        String[] stringArray = (String[]) handlerCtx.getInputValue("stringArray");
        handlerCtx.setOutputValue("item", SunOptionUtil.getOptions(stringArray));

     }

     @Handler(id = "selectItemArrayToStrArray",
    input = {
        @HandlerInput(name = "item", type = SelectItem[].class, required = true)},
    output = {
        @HandlerOutput(name = "strAry", type = String[].class)})
    public static void selectItemArrayToStrArray(HandlerContext handlerCtx) {

        SelectItem[] item = (SelectItem[]) handlerCtx.getInputValue("item");
        if (item == null || item.length == 0){
            handlerCtx.setOutputValue("strAry", new String[0]);
            return;
        }
        String[] strAry = new String[item.length];
        for(int i=0; i<item.length; i++){
            strAry[i] = (String)item[i].getValue();
        }
        handlerCtx.setOutputValue("strAry", strAry);
     }




    /**
     *  <p> Returns the date pattern for this calendar component.
     *
     */
    @Handler(id="getDatePattern",
    input={
           @HandlerInput(name="calendarComponent", type=com.sun.webui.jsf.component.Calendar.class, required=true)},
    output={
        @HandlerOutput(name="pattern", type=String.class)}
    )
    public static void getDatePattern(HandlerContext handlerCtx) {
        Calendar calendar = (Calendar) handlerCtx.getInputValue("calendarComponent");
		String pattern = calendar.getDateFormatPattern();

		if(pattern == null || pattern.length() == 0) {
			pattern = calendar.getDatePicker().getDateFormatPattern();

			if(pattern == null || pattern.length() == 0) {
				pattern="MM/dd/yyyy"; //default pattern
			}
		}
        handlerCtx.setOutputValue("pattern", pattern);
    }

  /**
     *  <p> Returns the list of monitorable server components</p>
     *
     */
  @Handler(id="populateServerMonitorDropDown",
        input={
            @HandlerInput(name="VSList", type=List.class, required=true),
            @HandlerInput(name="ThreadSystemList", type=List.class, required=true)},
        output={
            @HandlerOutput(name="MonitorList", type=Option[].class)})
    public void populateServerMonitorDropDown(HandlerContext handlerCtx) {
        List vsList = (List) handlerCtx.getInputValue("VSList");
        List threadList = (List) handlerCtx.getInputValue("ThreadSystemList");
        ArrayList menuList = new ArrayList();
        menuList.add(new Option("", ""));
        ListIterator vs = vsList.listIterator();
        // Menu for Instances
        while (vs.hasNext()) {            
          Option[] groupedOptions1 = new Option[0];
          OptionGroup jumpGroup1 =  new OptionGroup();
          ArrayList optionList = new ArrayList();
          String name = (String) vs.next();
          jumpGroup1.setLabel(name);
          String listeners = (String) V3AMX.getAttribute("amx:pp=/domain/configs/config[server-config]/http-service,type=virtual-server,name=" + name, "NetworkListeners");
          if (listeners != null) {
             StringTokenizer tokens = new StringTokenizer(listeners, ",");
              while (tokens.hasMoreTokens()) {
                  optionList.add(new Option(name, name));
                  String token = tokens.nextToken().trim();
                  optionList.add(new Option(token, token));
              }
              groupedOptions1 = (Option[]) optionList.toArray(new Option[optionList.size()]);
          }
          jumpGroup1.setOptions(groupedOptions1);
          menuList.add(jumpGroup1);
      }

        // Menu for Thread System
        ArrayList tList = new ArrayList();
        Option[] groupedOptions2 = new Option[0];
        ListIterator tl = threadList.listIterator();
        tList.add(new Option("thread-system", "thead-system"));
        while (tl.hasNext()) {
            String name = (String) tl.next();
            tList.add(new Option(name, name));
        }
        groupedOptions2 = (Option[])tList.toArray(new Option[tList.size()]);
        OptionGroup jumpGroup2 = new OptionGroup();
        jumpGroup2.setLabel("thread-system");
        jumpGroup2.setOptions(groupedOptions2);
        menuList.add(jumpGroup2);

        // Add Menu Options.
         jumpMenuOptions = (Option[])menuList.toArray(new Option[menuList.size()]);
         
        handlerCtx.setOutputValue("MonitorList", jumpMenuOptions);
    }

   /**
     *  <p> Returns the list of monitorable resource components</p>
     *
     */
  @Handler(id="populateResourceMonitorDropDown",
        input={
            @HandlerInput(name="ResourceList", type=List.class, required=true)},
        output={
            @HandlerOutput(name="MonitorList", type=Option[].class),
            @HandlerOutput(name="FirstItem", type=String.class)})
    public void populateResourceMonitorDropDown(HandlerContext handlerCtx) {
        List rList = (List) handlerCtx.getInputValue("ResourceList");
         ArrayList menuList = new ArrayList();
        // Menu for Resources
        ArrayList resList = new ArrayList();
        Option[] groupedOptions1 = new Option[0];
        String firstItem = "";
        if (rList != null) {
        ListIterator rl = rList.listIterator();
            while (rl.hasNext()) {
                String name = (String) rl.next();
                resList.add(new Option(name, name));
                if (GuiUtil.isEmpty(firstItem)) {
                    firstItem = name;
                }
            }
        }
        groupedOptions1 = (Option[]) resList.toArray(new Option[resList.size()]);
        OptionGroup jumpGroup1 = new OptionGroup();
        jumpGroup1.setLabel("resources");
        jumpGroup1.setOptions(groupedOptions1);
        menuList.add(jumpGroup1);


        // Add Menu Options.
        jumpMenuOptions = (Option[]) menuList.toArray(new Option[menuList.size()]);

        handlerCtx.setOutputValue("MonitorList", jumpMenuOptions);
        handlerCtx.setOutputValue("FirstItem", firstItem);
    }

   /**
     *  <p> Returns the list of monitorable resource components</p>
     *
     */
  @Handler(id="populateApplicationsMonitorDropDown",
        input={
            @HandlerInput(name="AppsList", type=List.class, required=true)},
        output={
            @HandlerOutput(name="MonitorList", type=Option[].class),
            @HandlerOutput(name="FirstItem", type=String.class)})
    public void populateApplicationsMonitorDropDown(HandlerContext handlerCtx) {
        List aList = (List) handlerCtx.getInputValue("AppsList");
        ArrayList menuList = new ArrayList();
        // Menu for Resources
        ArrayList appsList = new ArrayList();
        Option[] groupedOptions1 = new Option[0];
        String firstItem = "";
        if (aList != null) {
            ListIterator al = aList.listIterator();
            while (al.hasNext()) {
                String name = (String) al.next();
                appsList.add(new Option(name, name));
                if (GuiUtil.isEmpty(firstItem)) {
                    firstItem = name;
                }
            }
        }
        groupedOptions1 = (Option[]) appsList.toArray(new Option[appsList.size()]);
        OptionGroup jumpGroup1 = new OptionGroup();
        jumpGroup1.setLabel("applications");
        jumpGroup1.setOptions(groupedOptions1);
        menuList.add(jumpGroup1);

          // Menu for ejb app info
        OptionGroup ejbAppOptions = setEjbGroupOptions("ejb-application-mon", "ejb-application-info");
        if(ejbAppOptions !=null){
            menuList.add(ejbAppOptions);
        }

           // Menu for ejb app info
        OptionGroup ejbTimerOptions = setEjbGroupOptions("ejb-timed-object-mon", "ejb-timer");
        if(ejbTimerOptions !=null){
            menuList.add(ejbTimerOptions);
        }
        
         // Menu for bean-cache
        OptionGroup bcOptions = setEjbGroupOptions("bean-cache-mon", "bean-cache");
        if(bcOptions !=null){
            menuList.add(bcOptions);
        }
        
          // Menu for bean-pool
        OptionGroup bpOptions = setEjbGroupOptions("bean-pool-mon", "bean-pool");
        if(bpOptions !=null){
            menuList.add(bpOptions);
        }

        // Menu for bean-methods
        OptionGroup bmOptions = setEjbGroupOptions("bean-method-mon", "bean-methods");
        if(bmOptions !=null){
            menuList.add(bmOptions);
        }
        
        // Add Menu Options.
        jumpMenuOptions = (Option[]) menuList.toArray(new Option[menuList.size()]);

        handlerCtx.setOutputValue("MonitorList", jumpMenuOptions);
        handlerCtx.setOutputValue("FirstItem", firstItem);
    }

    public static OptionGroup setEjbGroupOptions(String type, String label) {
        List nameList = V3AMX.getProxyListByType(type);
        if (nameList != null && nameList.size() != 0) {
            ArrayList nList = new ArrayList();
            ListIterator nl = nameList.listIterator();
            while (nl.hasNext()) {
                String name = (String) nl.next();
                nList.add(new Option(name, name));
            }
            Option[] groupedOptions = new Option[0];
            groupedOptions = (Option[]) nList.toArray(new Option[nList.size()]);
            OptionGroup jumpGroup = new OptionGroup();
            jumpGroup.setLabel(label);
            jumpGroup.setOptions(groupedOptions);
            return jumpGroup;
        } else {
            return null;
        }
    }

    private Option[] jumpMenuOptions = null;

}
