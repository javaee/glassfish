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
 * $Id: ChartServlet.java,v 1.4 2004/11/14 07:33:14 tcfujii Exp $
 */

package components.renderkit;

import components.model.ChartItem;

import com.sun.image.codec.jpeg.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.geom.Ellipse2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <p><strong>ChartServlet</strong> is used to render the chart image.
 */
public final class ChartServlet extends HttpServlet {

    /**
     * <p>The <code>ServletConfig</code> instance for this servlet.</p>
     */
    private ServletConfig servletConfig = null;


    
    /**
     * <p>Release all resources acquired at startup time.</p>
     */
    public void destroy() {
        servletConfig = null;
    }

    /**
     * <p>Return the <code>ServletConfig</code> instance for this servlet.</p>
     */
    public ServletConfig getServletConfig() {

        return (this.servletConfig);

    }

    /**
     * <p>Return information about this Servlet.</p>
     */
    public String getServletInfo() {

        return (this.getClass().getName());

    }

    /**
     * <p>Perform initialization.</p>
     *
     * @exception ServletException if, for any reason, 
     * bn error occurred during the processing of
     * this <code>init()</code> method.
     */
    public void init(ServletConfig servletConfig) throws ServletException {
	
        // Save our ServletConfig instance
        this.servletConfig = servletConfig;
    }

    /**
     * <p>Process an incoming request, and create the corresponding
     * response.</p>
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs during processing
     * @exception ServletException if a servlet error occurs during processing
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {


	// Here's where we'd get the ChartBean from the session and determine
	// whether we're generating a pie chart or bar chart...
	//
	String type = request.getParameter("type");
	if ((type == null) || 
	    (!type.equals("bar")) && (!type.equals("pie"))) {
	    type = "bar";
	}
        
	if (type.equals("bar")) {
	    generateBarChart(request, response);
	} else {
            generatePieChart(request, response);
	}
    }

    /**
     * <p>Process an incoming request, and create the corresponding
     * response.</p>
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs during processing
     * @exception ServletException if a servlet error occurs during processing
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
	doGet(request, response);
    }

    /**
     * <p> Generate a bar chart from data.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs during processing
     * @exception ServletException if a servlet error occurs during processing
     */
    private void generateBarChart(HttpServletRequest request,
		                  HttpServletResponse response)
        throws IOException, ServletException {

        final int VERTICAL = 0;
	final int HORIZONTAL = 1;

	response.setContentType("image/jpeg");
	
        String id = request.getParameter("chartId");
        
	// get chart parameters
	String title = request.getParameter("title");
	if (title == null) {
	    title = "Chart";
	}
	
	int orientation = VERTICAL;
	String orientationStr = request.getParameter("orientation");
	if ((orientationStr == null) || 
	    (!orientationStr.equals("horizontal")) && (!orientationStr.equals("vertical"))) {
	    orientation = VERTICAL;
	} else if (orientationStr.equals("vertical")) {
	    orientation = VERTICAL;
	} else {
	    orientation = HORIZONTAL;
	}

	// label for x/y axis
	String xLabel = request.getParameter("xlabel");
	String yLabel = request.getParameter("ylabel");

	// default image size
	int width = 400;
	int height = 300;
	String widthStr = request.getParameter("width");
	String heightStr = request.getParameter("height");
	if (widthStr != null) {
	    width = Integer.parseInt(widthStr);
	}
	if (heightStr != null) {
	    height = Integer.parseInt(heightStr);
	}
	
	// get an array of chart items containing our data..
        HttpSession session = request.getSession(true);
	ChartItem[] chartItems = (ChartItem[])session.getAttribute(id);
	if (chartItems == null) {
            System.out.println("No data items specified...");
	    throw new ServletException("No data items specified...");
	}

        // remove the chart data from session now that chart has been rendered.
        session.removeAttribute(id);
        
	// maximum data value
	int maxDataValue = 0;
	// maximum label width
	int maxLabelWidth = 0;
	// space between bars
	int barSpacing = 10;
	// width of each bar
	int barWidth = 0;
	// x,y coordinates
	int cx, cy;
	// number of chart items
	int columns = chartItems.length;
	int scale = 10;
        // an individual chart data item
	ChartItem chartItem = null;
	String label = null;
	int value = 0;

        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bi.createGraphics();
        Font titleFont = new java.awt.Font("Courier", Font.BOLD, 12);
        FontMetrics titleFontMetrics = g2d.getFontMetrics(titleFont);
	
	// loop through and figure out the the widest item label, as well as
	// the maximum value.
        for (int i=0; i < columns; i++) {
	    chartItem = chartItems[i];
	    label = chartItem.getLabel();
	    value = chartItem.getValue();
	    if (value > maxDataValue) {
	        maxDataValue = value;
	    }
	    maxLabelWidth = Math.max(titleFontMetrics.stringWidth(label), maxLabelWidth);
	}

	// calculate chart dimensions
	int[] xcoords = new int[columns];
	int[] ycoords = new int[columns];
	int totalWidth = 0;
	int totalHeight = 0;
	for (int i=0; i < columns; i++) {
	    switch (orientation) {
	      case VERTICAL:
	      default: 
                barWidth = maxLabelWidth;
		cx = (Math.max((barWidth + barSpacing),maxLabelWidth) * i) +
		    barSpacing;
		totalWidth = cx + (4 * titleFont.getSize());
		break;
	      case HORIZONTAL:
		barWidth = titleFont.getSize();
		cy = ((barWidth + barSpacing) * i) + barSpacing;
		totalHeight = cy + (4 * titleFont.getSize()); 
		break;
	    }
	}
	if (orientation == VERTICAL) {
            totalHeight = maxDataValue + (8 * titleFont.getSize());
	    totalWidth = totalWidth + 50;
	} else {
	    totalWidth = maxDataValue + (4 * titleFont.getSize() +
		(Integer.toString(maxDataValue).length() * titleFont.getSize())+50);
	}

	// Make sure the the total height of the chart provides enough room
	// for the vertical label..
	//
	int yLabelHeight = 0;
	for (int i=0; i<yLabel.length(); i++) {
	    yLabelHeight += titleFontMetrics.getAscent();
	}
	if ((yLabelHeight+(12 * titleFontMetrics.getDescent())) > totalHeight) {
	    totalHeight = yLabelHeight+(8*titleFont.getSize());
	}
	    

        bi = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
        g2d = bi.createGraphics();
        titleFontMetrics = g2d.getFontMetrics(titleFont);

	// graph dimensions
	Dimension graphDim = new Dimension(totalWidth,totalHeight);
        Rectangle graphRect = new Rectangle(graphDim);

	// border dimensions
	Dimension borderDim = new Dimension(totalWidth-2,totalHeight-2);
        Rectangle borderRect = new Rectangle(borderDim);
	
	// background color
	g2d.setColor(Color.white);
	g2d.fill(graphRect);

	// draw border
	g2d.setColor(Color.black);
	borderRect.setLocation(1,1);
        g2d.draw(borderRect);

	// draw the title centered at the bottom of the bar graph
	int i = titleFontMetrics.stringWidth(title);
	g2d.setFont(titleFont);
	g2d.setColor(Color.black);
	g2d.drawString(title, Math.max((totalWidth - i)/2, 0),
	    totalHeight - titleFontMetrics.getDescent());

	// draw the x axis label
	i = titleFontMetrics.stringWidth(xLabel);
	g2d.drawString(xLabel, Math.max((totalWidth - i)/2, 0),
	    totalHeight - (6 * titleFontMetrics.getDescent()));

	// draw the y axis label
	i = titleFontMetrics.stringWidth(yLabel);
	cx = totalWidth-(totalWidth-6);
	cy = totalHeight - (12 * titleFontMetrics.getDescent());
	for (int j=yLabel.length(); j>0; j--) {
	    g2d.drawString(yLabel.substring(j-1,j), cx, cy);
	    cy -= titleFontMetrics.getAscent();
	}
	    
	// loop through to draw the chart items.
        for (i=0; i < columns; i++) {
	    chartItem = chartItems[i];
	    label = chartItem.getLabel();
	    value = chartItem.getValue();
	    String colorStr = chartItem.getColor();

	    Object color = getColor(colorStr);
	    switch (orientation) {
	      case VERTICAL:
	      default: 
                barWidth = maxLabelWidth;
		// set the next X coordinate to account for the label
		// being wider than the bar width.
		cx = (Math.max((barWidth + barSpacing),maxLabelWidth) * i) +
		    barSpacing + 12;

		// center the bar chart
		cx += Math.max((totalWidth - (columns * (barWidth + 
		    (2 * barSpacing))))/2,0);
		   
		// set the next Y coordinate to account for the height
		// of the bar as well as the title and labels painted
		// at the bottom of the chart.
		cy = totalHeight - (value) - 1 - (2 * titleFont.getSize());
	
		// draw the label
		g2d.setColor(Color.black);
		g2d.drawString((String)label, cx,
		    totalHeight - titleFont.getSize() - (8 * titleFontMetrics.getDescent()));	

		// draw the shadow bar
		if (color == Color.black) {
		    g2d.setColor(Color.gray);
		}
		g2d.fillRect(cx + 5, cy - 28, barWidth,  (value));
		// draw the bar with the specified color
		g2d.setColor((Color)(color));
                g2d.fillRect(cx, cy - 30, barWidth, (value));
                g2d.drawString("" + value, cx, cy - 30 - titleFontMetrics.getDescent());
		break;
	      case HORIZONTAL:
		barWidth = titleFont.getSize();
		// set the Y coordinate
	        cy = totalHeight - (((barWidth + barSpacing) * i) + barSpacing + 
		    (12 * titleFontMetrics.getDescent()));
		

		// set the X coordinate to be the width of the widest label
		cx = maxLabelWidth + 1;

		cx += Math.max((totalWidth - (maxLabelWidth + 1 +
	            titleFontMetrics.stringWidth("" + maxDataValue) +
                    (maxDataValue))) / 2, 0);
		// draw the labels and the shadow
		g2d.setColor(Color.black);
		g2d.drawString((String)label, cx - maxLabelWidth - 1,
			     cy + titleFontMetrics.getAscent());
		if (color == Color.black) {
		    g2d.setColor(Color.gray);
		}
		g2d.fillRect(cx + 3, cy + 5, (value), barWidth);

		// draw the bar in the current color
		g2d.setColor((Color)(color));
                g2d.fillRect(cx, cy, (value), barWidth);
                g2d.drawString("" + value, cx + (value ) + 3,
                    cy + titleFontMetrics.getAscent());
		break;
	    }
	}
        OutputStream output = response.getOutputStream();
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(output);
        encoder.encode(bi);
        output.close();
    }

    /**
     * <p> Generate a pie chart from data.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs during processing
     * @exception ServletException if a servlet error occurs during processing
     */
    private void generatePieChart(HttpServletRequest request,
		                  HttpServletResponse response)
        throws IOException, ServletException {
        response.setContentType("image/jpeg");
	
	// get chart parameters
        String id = request.getParameter("chartId");
	String title = request.getParameter("title");
	if (title == null) {
	    title = "Chart";
	}
	
	// label for x/y axis
	String xLabel = request.getParameter("xlabel");
	String yLabel = request.getParameter("ylabel");

	// default image size
	int width = 400;
	int height = 200;
	String widthStr = request.getParameter("width");
	String heightStr = request.getParameter("height");
	if (widthStr != null) {
	    width = Integer.parseInt(widthStr);
	}
	if (heightStr != null) {
	    height = Integer.parseInt(heightStr);
	}
	
	// get an array of chart items containing our data..
        HttpSession session = request.getSession();
	ChartItem[] chartItems = (ChartItem[])session.getAttribute(id);
	if (chartItems == null) {
            System.out.println("No data items specified...");
	    throw new ServletException("No data items specified...");
	}
        
        // remove the chart data from session now that chart has been rendered.
        session.removeAttribute(id);
        
        // begin pie chart
        Color dropShadow = new Color(240,240,240);
        //inner padding to make sure bars never touch the outer border
        int innerOffset = 20;
 
        int pieHeight = height - (innerOffset * 2);
        int pieWidth =  pieHeight;              
        int halfWidth = width/2;

        //Width of the inner graphable area
        int innerWidth = width - (innerOffset * 2);
        
        //graph dimension
        Dimension graphDim = new Dimension(width, height);
        Rectangle graphRect = new Rectangle(graphDim);

        //border dimensions
        Dimension borderDim = new Dimension(halfWidth-2,height-2);
        Rectangle borderRect = new Rectangle(borderDim);
        
        //Set content type
        response.setContentType("image/jpeg");

        //Create BufferedImage & Graphics2D
        BufferedImage bi = new BufferedImage(width, height, 
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bi.createGraphics();

        // Set Antialiasing
        RenderingHints renderHints = 
            new RenderingHints(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHints(renderHints);

        //Set graph background color to white:
        g2d.setColor(Color.white);
        g2d.fill(graphRect);

        //Draw black border
        g2d.setColor(Color.black);
        borderRect.setLocation(1,1);
        g2d.draw(borderRect);

        //Now draw border for legend
        borderRect.setLocation((width/2) + 1,1);
        g2d.draw(borderRect);

        //Draw data onto the graph:
        int x_pie = innerOffset;
        int y_pie = innerOffset;
        int border = 20;

        //Main chart Ellipse
        Ellipse2D.Double elb = new Ellipse2D.Double(x_pie - border/2, 
            y_pie - border/2, pieWidth + border, pieHeight + border);
        //Shadow
        g2d.setColor(dropShadow);
        g2d.fill(elb);

        //Border
        g2d.setColor(Color.black);
        g2d.draw(elb);

        // Calculate the total value so that the pies can be calculated.
        float yTotal = 0.0f;
        int lastElement = 0;
        for(int i=0; i<chartItems.length; i++) {
           int ycoord = chartItems[i].getValue();
           if(ycoord > 0.0f) {
               yTotal += ycoord;
               lastElement = i;
          }
        }

        // Draw the pie chart
        int startAngle = 0;

        //Legend variables
        int legendWidth = 20;
        int x_legendText = halfWidth + innerOffset/2 + legendWidth + 5;
        int x_legendBar = halfWidth + innerOffset/2;
        int textHeight = 20;
        int curElement = 0;
        int y_legend = 0;

        //Dimensions of the legend bar
        Dimension legendDim = new Dimension(legendWidth , textHeight/2);
        Rectangle legendRect = new Rectangle(legendDim);
        for(int i=0; i< chartItems.length; i++) {
            int ycoord = chartItems[i].getValue();
            if(ycoord > 0.0f) {
                //Calculate percentage sales
                float perc = (ycoord/yTotal);
                //Calculate new angle
                int sweepAngle = (int)(perc * 360);
                //Check that the last element goes back to 0 position
                if (i == lastElement) {sweepAngle = 360-startAngle;}
                // Draw Arc
                g2d.setColor(getColor(chartItems[i].getColor()));
                g2d.fillArc(x_pie, y_pie, pieWidth, pieHeight, startAngle, 
                        sweepAngle);
                //Increment startAngle with the sweepAngle
                startAngle += sweepAngle;

                //Draw Legend
                //Set y position for bar
                y_legend = curElement * textHeight + innerOffset;
                //Display the current column
                String display = chartItems[i].getLabel();
                g2d.setColor(Color.black);
                g2d.drawString(display, x_legendText, y_legend);
                //Display the total sales
                display = "" + ycoord;
                g2d.setColor(Color.black);
                g2d.drawString(display, x_legendText + 80, y_legend);
                //Display the sales percentage
                display = "  (" + (int)(perc*100) + "%)";
                g2d.setColor(Color.red);
                g2d.drawString(display, x_legendText + 110, y_legend);
                //Draw the bar
                g2d.setColor(getColor(chartItems[i].getColor()));
                legendRect.setLocation(x_legendBar,y_legend - textHeight/2);
                g2d.fill(legendRect);
                //Increment
                curElement++;
            }
        }
        // Encode the graph
        OutputStream output = response.getOutputStream();
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(output);
        encoder.encode(bi);
        output.close();
    }
    
    /**
     * Returns the Color instance corresponding the color passed in.
     *
     * @param colorStr a string representing a color instance.
     * @return Color instance corresponding to the input color.
     */
    protected Color getColor(String colorStr) {
        Color color = null;
	if (colorStr == null) {
            color = Color.gray;
        }
        if (colorStr.equals("red")) {
            color = Color.red;
        } else if (colorStr.equals("green")) {
            color = Color.green;
        } else if (colorStr.equals("blue")) {
            color = Color.blue;
        } else if (colorStr.equals("pink")) {
            color = Color.pink;
        } else if (colorStr.equals("orange")) {
            color = Color.orange;
        } else if (colorStr.equals("magenta")) {
            color = Color.magenta;
        } else if (colorStr.equals("cyan")) {
            color = Color.cyan;
        } else if (colorStr.equals("white")) {
            color = Color.white;
        } else if (colorStr.equals("yellow")) {
            color = Color.yellow;
        } else if (colorStr.equals("gray")) {
            color = Color.gray;
        } else if (colorStr.equals("darkGray")) {
            color = Color.darkGray;
        } else {
            color = Color.gray;
        }
        return color;
    }
}
