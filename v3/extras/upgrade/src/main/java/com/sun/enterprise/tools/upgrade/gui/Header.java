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

public class Header extends Canvas
{

    private String headerText;

    public Header()
    {
        this("");
    }

    public Header(String s)
    {
        headerText = null;
        headerText = s;
        setForeground(Color.black);
        try
        {
            setFont(new Font("SansSerif", 0, 12));
        }
        catch(Error _ex) { }
    }

    public Dimension getMinimumSize()
    {
        return getPreferredSize();
    }

    public Dimension getPreferredSize()
    {
        return new Dimension(getSize().width, 22);
    }

    public String getText()
    {
        return headerText;
    }

    public void paint(Graphics g)
    {
        Dimension dimension = getSize();
        g.setClip(0, 0, dimension.width, dimension.height);
        FontMetrics fontmetrics = null;
        g.setColor(getBackground());
        g.fillRect(0, 0, dimension.width + 1, dimension.height + 1);
        g.setColor(getForeground());
        g.setFont(getFont());
        fontmetrics = Toolkit.getDefaultToolkit().getFontMetrics(getFont());
        int i = (dimension.height - (fontmetrics.getMaxAscent() + fontmetrics.getMaxDescent())) / 2 + fontmetrics.getMaxAscent();
        g.drawString(headerText, fontmetrics.stringWidth(""), i);
        int j = dimension.height - fontmetrics.getMaxDescent() / 2;
        g.drawLine(0, j, dimension.width, j);
    }

    public void setText(String s)
    {
        headerText = s;
        repaint();
    }
}
