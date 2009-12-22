/*
 * $Id: ChartBean.java,v 1.4 2005/12/14 22:27:22 rlubke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at
 * https://javaserverfaces.dev.java.net/CDDL.html or
 * legal/CDDLv1.0.txt. 
 * See the License for the specific language governing
 * permission and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.    
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * [Name of File] [ver.__] [Date]
 * 
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */

package demo.model;

import java.util.ArrayList;
import java.util.Collection;

import com.sun.javaee.blueprints.components.ui.model.ChartItem;

public class ChartBean {

    // Bar Chart Properties -------------------------

    public static final int VERTICAL = 0;
    public static final int HORIZONTAL = 1;

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

    public void setTitle(String title) {
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
        this.height = height;
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
