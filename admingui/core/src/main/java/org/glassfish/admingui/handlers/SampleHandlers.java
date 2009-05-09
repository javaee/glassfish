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
 * UtilHandlers.java
 *
 * Created on Feb 20 2008
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.glassfish.admingui.handlers;


import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

//import com.sun.webui.jsf.component.Hyperlink;

import java.util.Map;
import java.util.Random;
import java.util.HashSet;

/**
 *
 * @author anilam
 */
public class SampleHandlers {
    
    /** Creates a new instance of UtilHandlers */
    public SampleHandlers() {
    }
    

    /**
     *	<p> Returns the value to which the input map maps the input key. </p>
     *
     *  <p> Input value: "Map" -- Type: <code>java.util.Map</code> 
     *  <p> Input value: "Key" -- Type: <code>Object</code>
     *  <p> Output value: "Value" -- Type: <code>Object</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="sampleMapGet",
    	input={
	    @HandlerInput(name="Map", type=Map.class, required=true),
            @HandlerInput(name="Key", type=Object.class, required=true)},
        output={
            @HandlerOutput(name="Value", type=Object.class)})
    public static void sampleMapGet(HandlerContext handlerCtx) {
        Map map = (Map) handlerCtx.getInputValue("Map");
        Object key = (Object) handlerCtx.getInputValue("Key");
        handlerCtx.setOutputValue("Value", (Object) map.get(key));        
    }

	@Handler(id="getjMakiValues",
    	output={
        	@HandlerOutput(name="values", type=String.class)
    	})
	 public static void getjMakiValues(HandlerContext handlerCtx) {
         Random generator = new Random();
	HashSet<Integer> hset = new HashSet<Integer>();
        String values = "[";
	int i = 0;

        while(i < 12) {
            int x = generator.nextInt(LIMIT);
            x = x - x%DIVISOR;
            if(x > MID_WAY) {
                x = x - DIFF;
            }
            else {
                x = x + DIFF;
            }
		if(!(hset.contains(x))) {
			hset.add(x);
			i++;
			values = values + x + ",";
		}
        }
	values = values.substring(0, values.lastIndexOf(','));
        values += "]";
        //String values = "[25, 45, 25, 45, 50, 25, 35, 25, 25, 20, 35, 45]";
            handlerCtx.setOutputValue("values", values);
    }

	private static int MAX = 11;
    private static int LIMIT = 100;
    private static int DIVISOR = 5;
    private static int DIFF = 10;
    private static int MID_WAY = 50;
    
}
