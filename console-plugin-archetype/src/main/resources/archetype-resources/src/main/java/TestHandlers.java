/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package ${package};

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

/**
 *  <p>	This class is an example or placeholder for creating handlers.  For
 *	more information on JSFTemplating <code>handlers</code>, see the
 *	JSFTemplating documentation at:
 *	<a href="https://jsftemplating.dev.java.net/doc/">https://jsftemplating.dev.java.net/doc/</a></p>
 *
 *  @author Jason Lee (jlee@sun.com)
 *  @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class TestHandlers {

    /**
     *	<p> This simple handler returns the single "<code>in</code>" input
     *	    value that is given as it's single "<code>out</code>" output
     *	    value.  For example:</p>
     *
     *	<p> <code>${artifactId}.echo(in="Hello" out="#{requestScope.result}");</code></p>
     */
    @Handler(id="${artifactId}.echo",
        input={
            @HandlerInput(name="in", type=String.class, required=true)},
        output={
            @HandlerOutput(name="out", type=String.class)})
    public static void echoTest(HandlerContext handlerCtx) {
        handlerCtx.setOutputValue("out", handlerCtx.getInputValue("in"));
    }
}
