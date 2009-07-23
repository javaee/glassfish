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
import com.sun.webui.jsf.component.Hyperlink;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

import org.glassfish.admingui.common.util.GuiUtil;
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
            @HandlerInput(name="tableField", type=com.sun.webui.jsf.component.Field.class),
            @HandlerInput(name="methodValue", type=String.class)}
        )
    public static void setDisableConnectionPoolTableField(HandlerContext handlerCtx) {
        String methodValue = (String)handlerCtx.getInputValue("methodValue");
        Field tableField = (Field)handlerCtx.getInputValue("tableField");
        if("table".equals(methodValue)){
            tableField.setDisabled(false);
        }else
            tableField.setDisabled(true);
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
}
