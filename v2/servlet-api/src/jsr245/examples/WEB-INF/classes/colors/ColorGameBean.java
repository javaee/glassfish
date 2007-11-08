/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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
package colors;

import javax.servlet.http.*;

public class ColorGameBean {

    private String background = "yellow";
    private String foreground = "red";
    private String color1 = foreground;
    private String color2 = background;
    private String hint = "no";
    private int attempts = 0;
	private int intval = 0;
    private boolean tookHints = false;

    public void processRequest(HttpServletRequest request) {

	// background = "yellow";
	// foreground = "red";

	if (! color1.equals(foreground)) {
	    if (color1.equalsIgnoreCase("black") ||
			color1.equalsIgnoreCase("cyan")) {
			background = color1;
		}
	}

	if (! color2.equals(background)) {
	    if (color2.equalsIgnoreCase("black") ||
			color2.equalsIgnoreCase("cyan")) {
			foreground = color2;
	    }
	}

	attempts++;
    }

    public void setColor2(String x) {
	color2 = x;
    }

    public void setColor1(String x) {
	color1 = x;
    }

    public void setAction(String x) {
	if (!tookHints)
	    tookHints = x.equalsIgnoreCase("Hint");
	hint = x;
    }

    public String getColor2() {
	 return background;
    }

    public String getColor1() {
	 return foreground;
    }

    public int getAttempts() {
	return attempts;
    }

    public boolean getHint() {
	return hint.equalsIgnoreCase("Hint");
    }

    public boolean getSuccess() {
	if (background.equalsIgnoreCase("black") ||
	    background.equalsIgnoreCase("cyan")) {
	
	    if (foreground.equalsIgnoreCase("black") ||
		foreground.equalsIgnoreCase("cyan"))
		return true;
	    else
		return false;
	}

	return false;
    }

    public boolean getHintTaken() {
	return tookHints;
    }

    public void reset() {
	foreground = "red";
	background = "yellow";
    }

    public void setIntval(int value) {
	intval = value;
	}

    public int getIntval() {
	return intval;
	}
}

