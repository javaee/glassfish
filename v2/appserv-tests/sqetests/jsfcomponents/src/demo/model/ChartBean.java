/*
 * $Id: ChartBean.java,v 1.3 2004/11/14 07:33:17 tcfujii Exp $
 */

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
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
