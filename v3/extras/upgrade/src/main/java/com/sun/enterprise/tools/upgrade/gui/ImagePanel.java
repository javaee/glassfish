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

// Referenced classes of package com.sun.wizards.awt:
//            InsetsPanel

public class ImagePanel extends InsetsPanel
{

    protected Image image;
    protected int hAlign;
    protected int vAlign;
    protected boolean loaded;

    public ImagePanel()
    {
        image = null;
        hAlign = 2;
        vAlign = 2;
        loaded = false;
    }

    public ImagePanel(int i, int j)
    {
        image = null;
        hAlign = 2;
        vAlign = 2;
        loaded = false;
        hAlign = i;
        vAlign = j;
    }

    public ImagePanel(Insets insets)
    {
        //super(insets);
        image = null;
        hAlign = 2;
        vAlign = 2;
        loaded = false;
    }

    public ImagePanel(LayoutManager layoutmanager)
    {
        super(layoutmanager);
        image = null;
        hAlign = 2;
        vAlign = 2;
        loaded = false;
    }

    @Override
    public Dimension getMinimumSize()
    {
        return getPreferredSize();
    }

    @Override
    public Dimension getPreferredSize()
    {
        Dimension dimension = new Dimension(0, 0);
        if(image != null)
        {
            dimension.width = image.getWidth(this);
            dimension.height = image.getHeight(this);
        }
        return dimension;
    }

    @Override
    public synchronized boolean imageUpdate(Image image1, int i, int j, int k, int l, int i1)
    {
        if((i & 0x20) != 0)
        {
            loaded = true;
            repaint();
            return false;
        } else
        {
            return true;
        }
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        if(loaded)
        {
            int i = 0;
            int j = 0;
            int k = image.getWidth(this);
            int l = image.getHeight(this);
            Dimension dimension = getSize();
            g.setClip(0, 0, dimension.width, dimension.height);
            switch(hAlign)
            {
            case 1: // '\001'
                i = super.insets.left;
                break;

            case 2: // '\002'
                i = (dimension.width - (super.insets.left + super.insets.right) - k) / 2 + super.insets.left;
                break;

            case 3: // '\003'
                i = dimension.width - k - super.insets.right;
                break;
            }
            switch(vAlign)
            {
            case 1: // '\001'
                j = super.insets.top;
                break;

            case 2: // '\002'
                j = (dimension.height - (super.insets.top + super.insets.bottom) - l) / 2 + super.insets.top;
                break;

            case 3: // '\003'
                j = dimension.height - l - super.insets.bottom;
                break;
            }
            g.drawImage(image, i, j, this);
        }
    }

    public void setAlignment(int i, int j)
    {
        hAlign = i;
        vAlign = j;
    }

    public void setImage(Image image1)
    {
        loaded = false;
        image = image1;
        prepareImage(image1, this);
    }
}
