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
 * $Id: ChartBean.java,v 1.3 2004/11/14 07:33:17 tcfujii Exp $
 */

package demo.model;

import java.util.ArrayList;
import java.util.Collection;

import components.model.ChartItem;
public class ChartBean {

    // Bar Chart Properties -------------------------
    
    public static final int	VERTICAL = 0;
    public static final int 	HORIZONTAL = 1;

    private int orientation = VERTICAL;
    public int getOrientation() {
        return orientation;
    }
    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }
    
    // ----------------------------------------------
    
    private int columns = 0;
    public int getColumns() {
        return columns;
    }
    public void setColumns(int columns) {
        this.columns = columns;
    }

    private ArrayList chartItems = null;
    public Collection getChartItems() {
        return chartItems;
    }

    private String title = null;
    public String getTitle() {
        return title;
    }
    public void setTitle() {
        this.title = title;
    }

    private int scale = 10;
    public int getScale() {
        return scale;
    }
    public void setScale(int scale) {
        this.scale = scale;
    }

    private int width = 400;
    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    private int height = 300;
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height= height;
    }

    public ChartBean() {

	setWidth(400);
	setHeight(300);
	setColumns(2);
	setOrientation(ChartBean.HORIZONTAL);

        chartItems = new ArrayList(columns);
	chartItems.add(new ChartItem("one", 10, "red"));
	chartItems.add(new ChartItem("two", 20, "blue"));

    }
}
