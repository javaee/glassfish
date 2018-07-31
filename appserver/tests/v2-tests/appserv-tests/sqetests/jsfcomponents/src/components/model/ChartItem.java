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
 * $Id: ChartItem.java,v 1.3 2004/11/14 07:33:13 tcfujii Exp $
 */

package components.model;

/**
 * This class represents an individual graphable item for the chart conmponent.
 */
public class ChartItem {

    public ChartItem() {
       super();
    }
    
    public ChartItem(String label, int value, String color) {
        setLabel(label);
	setValue(value);
	setColor(color);
    }

    /**
     * <p>The label for this item.</p>
     */
    private String label = null;
    /**
     *<p>Return the label for this item.</p>
     */
    public String getLabel() {
        return label;
    }
    /**
     * <p>Set the label for this item.</p>
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * <p>The value for this item.</p>
     */
    private int value = 0;
    /**
     *<p>Return the value for this item.</p>
     */
    public int getValue() {
        return value;
    }
    /**
     * <p>Set the value for this item.</p>
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * <p>The color for this item.</p>
     */
    private String color = null;
    /**
     *<p>Return the color for this item.</p>
     */
    public String getColor() {
        return color;
    }
    /**
     * <p>Set the color for this item.</p>
     */
    public void setColor(String color) {
        this.color = color;
    }
}
