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

package com.sun.enterprise.tools.upgrade.gui;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.BreakIterator;
import java.util.Enumeration;
import java.util.Vector;

public class FlowLabel extends Canvas
{

    private String labelText;
    private Vector textVector;
    private transient Object textVectorLock;
    private static Font labelFont = null;
    private static String addAttr[] = {
        "<bold>", "<b>", "<italics>", "<i>", "<tt>", "<br>", "<p>"
    };
    private static int attrBold = 3;
    private static int attrItalics = 12;
    private static int attrTt = 16;
    private static int attrBreak;
    private static int attrParagraph;
    private static int attrFlowControlOnly;
    private static String removeAttr[] = {
        "</bold>", "</b>", "</italics>", "</i>", "</tt>", "</br>", "</p>"
    };
    private transient Dimension currentSize;
    private transient Object currentSizeLock;
    private transient Vector paintVector;
    private transient Object paintVectorLock;
    private int horizontalMargin;
    private int verticalMargin;
    private transient boolean resizePerformed;
    static final long serialVersionUID = 0xc821ccd1154af049L;

    public FlowLabel()
    {
        labelText = "";
        textVector = new Vector();
        textVectorLock = new Object();
        currentSize = new Dimension(0, 0);
        currentSizeLock = new Object();
        paintVector = new Vector();
        paintVectorLock = new Object();
        horizontalMargin = 10;
        verticalMargin = 5;
        resizePerformed = false;
    }

    public FlowLabel(String s)
    {
        labelText = "";
        textVector = new Vector();
        textVectorLock = new Object();
        currentSize = new Dimension(0, 0);
        currentSizeLock = new Object();
        paintVector = new Vector();
        paintVectorLock = new Object();
        horizontalMargin = 10;
        verticalMargin = 5;
        resizePerformed = false;
        labelText = s;
        textVector = createTextVector();
    }

    private Vector createPaintVector(Dimension dimension)
    {
        Vector vector = new Vector();
        try
        {
            Dimension dimension1 = new Dimension(dimension);
            dimension1.width -= horizontalMargin * 2;
            dimension1.height -= verticalMargin * 2;
            int i = 0;
            boolean flag = false;
            Vector vector1 = new Vector();
            Enumeration enumeration = null;
            boolean flag1 = false;
            int i1 = 0;
            boolean flag2 = false;
            int k1 = 0;
            Font font = getFont();
            font = new Font(font.getName(), 0, font.getSize());
            FontMetrics fontmetrics = getToolkit().getFontMetrics(font);
            synchronized(textVector)
            {
                enumeration = textVector.elements();
            }
            while(enumeration.hasMoreElements()) 
            {
                Object obj = enumeration.nextElement();
                if(obj instanceof Integer)
                {
                    int l1 = ((Integer)obj).intValue();
                    if((l1 & attrBreak) != 0)
                    {
                        vector.addElement(vector1);
                        vector1 = new Vector();
                        int j = i1;
                        k1 = 0;
                        i = 0;
                    } else
                    if((l1 & attrParagraph) != 0)
                    {
                        vector.addElement(vector1);
                        vector1 = new Vector();
                        vector.addElement(vector1);
                        vector1 = new Vector();
                        int k = i1;
                        k1 = 0;
                        i = 0;
                    }
                    font = getNewFont(font, ((Integer)obj).intValue());
                    fontmetrics = getToolkit().getFontMetrics(font);
                    vector1.addElement(obj);
                } else
                {
                    String s1 = (String)obj;
                    int l = 0;
                    for(i1 = 0; i1 < s1.length() - 1;)
                    {
                        if(k1 == 0)
                        {
                            l = skipWhitespace(s1, l);
                            i1 = getNextWordIndex(s1, i1);
                            k1++;
                        }
                        for(int j1 = getNextWordIndex(s1, i1); j1 <= s1.length() && fontmetrics.stringWidth(s1.substring(l, j1)) + i < dimension1.width;)
                        {
                            i1 = j1;
                            j1 = getNextWordIndex(s1, i1);
                            k1++;
                        }

                        if(i1 >= s1.length() - 1)
                        {
                            i = fontmetrics.stringWidth(s1.substring(l, i1)) + i;
                            vector1.addElement(s1.substring(l, i1));
                        } else
                        {
                            vector1.addElement(s1.substring(l, i1));
                            vector.addElement(vector1);
                            vector1 = new Vector();
                            l = i1;
                            k1 = 0;
                            i = 0;
                        }
                    }

                }
            }
            if(vector1.size() > 0)
            {
                vector.addElement(vector1);
            }
        }
        catch(Exception _ex)
        {
            return createPaintVector(dimension);
        }
        return vector;
    }

    private Vector createTextVector()
    {
        Vector vector = new Vector();
        int i = 0;
        int j = 0;
        StringBuffer stringbuffer = new StringBuffer();
        boolean flag = false;
        while(i < labelText.length()) 
        {
            boolean flag1 = false;
            while(i < labelText.length() && labelText.charAt(i) == '<') 
            {
                for(int k = 0; k < addAttr.length; k++)
                {
                    if(labelText.length() >= i + addAttr[k].length() && labelText.substring(i, i + addAttr[k].length()).equalsIgnoreCase(addAttr[k]))
                    {
                        j |= 1 << k;
                        i += addAttr[k].length();
                        flag1 = true;
                    }
                    if(labelText.length() >= i + removeAttr[k].length() && labelText.substring(i, i + removeAttr[k].length()).equalsIgnoreCase(removeAttr[k]))
                    {
                        j &= 255 - (1 << k);
                        i += removeAttr[k].length();
                        flag1 = (1 << k & attrFlowControlOnly) == 0;
                    }
                }

                if(!flag1)
                {
                    break;
                }
            }
            if(flag1)
            {
                int l = stringbuffer.length();
                if(l > 0)
                {
                    vector.addElement(stringbuffer.toString());
                    stringbuffer = new StringBuffer();
                }
                vector.addElement(new Integer(j));
                j &= ~attrFlowControlOnly;
            } else
            {
                if(labelText.charAt(i) == '\n')
                {
                    if(stringbuffer.length() > 0)
                    {
                        vector.addElement(stringbuffer.toString());
                        stringbuffer = new StringBuffer();
                    }
                    vector.addElement(new Integer(attrBreak));
                    i++;
                }
                if(i < labelText.length())
                {
                    stringbuffer.append(labelText.charAt(i));
                    i++;
                }
            }
        }
        if(stringbuffer.length() > 0)
        {
            vector.addElement(stringbuffer.toString());
        }
        return vector;
    }

    public void forceResize()
    {
        if(!resizePerformed)
        {
            resizePerformed = true;
            invalidate();
            Container container;
            for(container = getParent(); container != null && container.getParent() != null; container = container.getParent()) { }
            container.validate();
        }
    }

    public String getContentString()
    {
        String s = "";
        Vector vector = createTextVector();
        for(Enumeration enumeration = vector.elements(); enumeration.hasMoreElements();)
        {
            Object obj = enumeration.nextElement();
            if(obj instanceof String)
            {
                s = s + obj;
            }
        }

        return s;
    }

    public Dimension getMinimumSize()
    {
        return getPreferredSize();
    }

    private Font getNewFont(Font font, int i)
    {
        if(font != null)
        {
            if(labelFont == null && (i & attrTt) != 0)
            {
                labelFont = font;
                font = new Font("Monospaced", 0, font.getSize());
                if(font == null)
                {
                    font = labelFont;
                    labelFont = null;
                }
            } else
            if(labelFont != null && (i & attrTt) == 0)
            {
                font = labelFont;
                labelFont = null;
            }
            font = new Font(font.getName(), ((i & attrBold) == 0 ? 0 : 1) | ((i & attrItalics) == 0 ? 0 : 2), font.getSize());
        }
        return font;
    }

    private int getNextWordIndex(String s, int i)
    {
        int j;
        for(j = i + 1; j < s.length() && Character.isWhitespace(s.charAt(j)); j++) { }
        if(j < s.length())
        {
            BreakIterator breakiterator = BreakIterator.getLineInstance();
            breakiterator.setText(s.substring(j, s.length()));
            int k = breakiterator.first();
            k = breakiterator.next();
            if(k != -1)
            {
                if(s.charAt((k + j) - 1) == '.')
                {
                    BreakIterator breakiterator1 = BreakIterator.getSentenceInstance();
                    breakiterator1.setText(s);
                    int l;
                    for(l = breakiterator1.first(); l != -1 && l < k + j; l = breakiterator1.next()) { }
                    if(l != k + j)
                    {
                        return getNextWordIndex(s, k + j);
                    }
                }
                return k + j;
            }
        }
        return j;
    }

    public Dimension getPreferredSize()
    {
        Container container = getParent();
        Dimension dimension = container.getSize();
        Dimension dimension1 = new Dimension(1, 1);
        if(dimension.width == 0)
        {
            dimension = new Dimension(400, 200);
        }
        Insets insets = container.getInsets();
        try
        {
            Font font = getNewFont(getFont(), 0);
            FontMetrics fontmetrics = getToolkit().getFontMetrics(font);
            dimension1 = new Dimension(dimension.width - insets.right - insets.left, dimension.height - insets.top - insets.bottom);
            synchronized(currentSizeLock)
            {
                synchronized(paintVectorLock)
                {
                    Vector vector = createPaintVector(dimension1);
                    dimension1.height = verticalMargin * 2 + fontmetrics.getHeight() * vector.size();
                }
            }
        }
        catch(Error _ex)
        {
            return getPreferredSize();
        }
        return dimension1;
    }

    public void paint(Graphics g)
    {
        Dimension dimension = getSize();
        synchronized(currentSizeLock)
        {
            if(currentSize.width != dimension.width)
            {
                sizeChanged(dimension);
            }
        }
        Dimension dimension1 = getPreferredSize();
        if(dimension.height < dimension1.height && !resizePerformed)
        {
            forceResize();
            return;
        }
        g.setClip(0, 0, dimension.width, dimension.height);
        //g.setColor(getForeground());
        g.setColor(Color.blue);
        Font font = getFont();
        font = new Font(font.getName(), 0, font.getSize());
        FontMetrics fontmetrics = getToolkit().getFontMetrics(font);
        g.setFont(font);
        int i = horizontalMargin;
        int j = fontmetrics.getMaxAscent() + verticalMargin;
        synchronized(paintVectorLock)
        {
            for(int k = 0; k < paintVector.size(); k++)
            {
                Vector vector = (Vector)paintVector.elementAt(k);
                for(int l = 0; l < vector.size(); l++)
                {
                    Object obj2 = vector.elementAt(l);
                    if(obj2 instanceof Integer)
                    {
                        font = getNewFont(font, ((Integer)obj2).intValue());
                        fontmetrics = getToolkit().getFontMetrics(font);
                        g.setFont(font);
                    }
                    if(obj2 instanceof String)
                    {
                        try
                        {
                            g.drawString((String)obj2, i, j);
                            i += fontmetrics.stringWidth((String)obj2);
                        }
                        catch(Exception _ex)
                        {
                            repaint();
                        }
                    }
                }

                i = horizontalMargin;
                j += fontmetrics.getHeight();
            }

        }
    }

    private void readObject(ObjectInputStream objectinputstream)
        throws IOException
    {
        try
        {
            objectinputstream.defaultReadObject();
        }
        catch(ClassNotFoundException classnotfoundexception)
        {
            classnotfoundexception.printStackTrace();
        }
        finally
        {
            textVectorLock = new Object();
            paintVectorLock = new Object();
            paintVector = new Vector();
            currentSize = new Dimension(0, 0);
            currentSizeLock = new Object();
        }
    }

    public void setHgap(int i)
    {
        horizontalMargin = i;
    }

    public void setText(String s)
    {
        labelText = s;
        synchronized(textVectorLock)
        {
            textVector = createTextVector();
        }
        Dimension dimension = getSize();
        if(dimension.width > 0 || dimension.height > 0)
        {
            synchronized(paintVectorLock)
            {
                paintVector = createPaintVector(dimension);
            }
        }
        if(isShowing())
        {
            invalidate();
            repaint();
        }
    }

    public void setVgap(int i)
    {
        verticalMargin = i;
    }

    public void sizeChanged(Dimension dimension)
    {
        synchronized(currentSizeLock)
        {
            Dimension dimension1 = currentSize;
            if(!dimension.equals(dimension1))
            {
                try
                {
                    Graphics g = getGraphics();
                    g.setColor(getBackground());
                    g.fillRect(0, 0, dimension1.width + 1, dimension1.height + 1);
                }
                catch(Exception _ex) { }
            }
            if(dimension.width != dimension1.width)
            {
                synchronized(paintVectorLock)
                {
                    paintVector = createPaintVector(dimension);
                }
            }
            currentSize = dimension;
            resizePerformed = false;
        }
    }

    private int skipWhitespace(String s, int i)
    {
        int j;
        for(j = i; j < s.length() && Character.isWhitespace(s.charAt(j)); j++) { }
        return j;
    }

    static 
    {
        attrBreak = 32;
        attrParagraph = 64;
        attrFlowControlOnly = attrBreak | attrParagraph;
    }
}
