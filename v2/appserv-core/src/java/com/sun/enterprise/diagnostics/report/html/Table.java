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
package com.sun.enterprise.diagnostics.report.html;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 *
 * @author Jagadish
 */
public class Table extends HTMLElement {
    
    private int border;
    private int cellSpacing;
    private String alignment;
    private float width;
    private boolean absoluteWidth;

    public static final String ALIGN_LEFT="LEFT";
    public static final String ALIGN_CENTER="CENTER";
    public static final String ALIGN_RIGHT="RIGHT";
    
    /** Creates a new instance of Table */
    public Table() {
        super("table");
    }
    
    /** Creates a new instance of Table */
    public Table( int border, int cellSpacing){
        this();
        addAttribute("border",String.valueOf(border));
        addAttribute("cellspacing",String.valueOf(cellSpacing));
        
    }
    
    /**
     *Sets the border attribute for the table
     *@param border in pixels
     */
    public void setBorder(int border){
        this.border = border;
        setAttribute("border",String.valueOf(border));
    }
    
    public void setCellSpacing(int cellSpacing){
        this.cellSpacing = cellSpacing;
        setAttribute("cellspacing", String.valueOf(cellSpacing));
    }
    
    public void setAlignment(String alignment){
        this.alignment = alignment;
        setAttribute("alignment",alignment );
    }
    
    public void setWidth(float width, boolean absolute){
        this.width = width;
        this.absoluteWidth = absolute;
        if(absolute){
            setAttribute("width",String.valueOf(width));
        }
        else{
            setAttribute("width",String.valueOf(width)+"%" );
        }
    }
    
    private void setAttribute(String property, String value){
        if(!(property==null || value==null)){
            List<Attribute> attributes= getAttributes(property);
            if(attributes.size()==0){
                addAttribute(property,value);
            }
            else{
                Attribute attribute = attributes.get(0);
                attribute.setValue(value);   
            }
        }
    }
    

    public int getBorder(){
        return border;
    }
    
    public int getCellSpacing(){
        return cellSpacing;
    }
    
    public String getAlignment(){
        return alignment;
    }
    
    public float getWidth(){
        return width;
    }
    
    public boolean isAbsoluteWidth(){
        return absoluteWidth;
    }
    
    public TR addRow(Iterator<String> values, boolean header, String cssClass) {
        TR row = new TR();
        while(values.hasNext()) {
            CSSElement tableData = null;
            if(header)
                tableData = new TH();
            else
                tableData = new TD();
            
            tableData.addText(values.next());
            setCSSClass(tableData, cssClass);
            row.add(tableData);
        }
        add(row);
        return row;
    }
    
    public TR addRow(ArrayList elements, String cssClass) {
        TR row = new TR();
        for(int i=0; i<elements.size();i++){
            TD tableData = new TD();
            tableData.add((Element)elements.get(i));
            setCSSClass(tableData, cssClass);
            row.add(tableData);
        }
        add(row);
        return row;
    }
    
    private void setCSSClass(CSSElement element, String cssClass) {
        if(cssClass != null){
            element.setCSSClass(cssClass);
        } 
    }
}
