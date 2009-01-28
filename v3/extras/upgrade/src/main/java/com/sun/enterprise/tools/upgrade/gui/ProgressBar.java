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
import java.util.Stack;

public class ProgressBar extends Component
{

    protected Dimension size;
    protected int progress;
    protected int depth;

    public ProgressBar()
    {
        size = new Dimension(250, 16);
        progress = 0;
        depth = 1;
    }

    protected void drawProgress(Graphics g, int i, int j)
    {
        if(g == null)
        {
            return;
        }
        int k = size.width - 2 * depth;
        int l = (int)(((double)k / 100D) * (double)i);
        int i1 = (int)(((double)k / 100D) * (double)j);
        if(g != null)
        {
            if(i1 < l)
            {
                g.setColor(Color.white);
                g.fillRect(depth + i1, depth, l - i1, size.height - 2 * depth);
            } else
            {
                g.setColor(Color.blue);
                g.fillRect(depth + l, depth, i1 - l, size.height - 2 * depth);
            }
        }
    }

    protected void drawProgressBorder(Graphics g)
    {
        size = getSize();
        if(size.width == 0 || size.height == 0)
        {
            Stack stack = new Stack();
            Container container = getParent();
            Dimension dimension = null;
            if(container != null)
            {
                dimension = container.getSize();
            }
            while(container != null && (dimension.width == 0 || dimension.height == 0)) 
            {
                stack.push(container);
                container = container.getParent();
                if(container != null)
                {
                    dimension = container.getSize();
                }
            }
            if(container == null)
            {
                container = (Container)stack.pop();
            }
            if(container != null)
            {
                container.validate();
            }
            return;
        }
        Color color = getBackground();
        Color color1 = Color.white;
        Color color2 = Color.black;
        int i = 150 / depth;
        for(int j = 0; j < depth; j++)
        {
            g.setColor(color1);
            g.drawLine(j, size.height - (j + 1), j, j);
            g.drawLine(j, j, size.width - (j + 1), j);
            g.setColor(color2);
            g.drawLine(j + 1, size.height - (j + 1), size.width - (j + 1), size.height - (j + 1));
            g.drawLine(size.width - (j + 1), size.height - (j + 1), size.width - (j + 1), j + 1);
            color1 = new Color(color1.getRed() - i, color1.getGreen() - i, color1.getBlue() - i);
            color2 = new Color(color2.getRed() + i, color2.getGreen() + i, color2.getBlue() + i);
        }

        g.setColor(Color.white);
        g.fillRect(depth, depth, size.width - 2 * depth, size.height - 2 * depth);
    }

    public int getDepth()
    {
        return depth;
    }

    public Dimension getMinimumSize()
    {
        return getPreferredSize();
    }

    public Dimension getPreferredSize()
    {
        Container container = getParent();
        if(container == null)
        {
            return size;
        }
        Dimension dimension = container.getSize();
        if(dimension.width == 0 || dimension.height == 0)
        {
            return size;
        } else
        {
            Insets insets = container.getInsets();
            return new Dimension(dimension.width - insets.left - insets.right, size.height);
        }
    }

    public int getProgress()
    {
        return progress;
    }

    public void paint(Graphics g)
    {
        Dimension dimension = getSize();
        g.setClip(0, 0, dimension.width, dimension.height);
        drawProgressBorder(g);
        drawProgress(g, 0, progress);
    }

    public void setDepth(int i)
    {
        depth = i;
    }

    public void setProgress(int i)
    {
        Graphics g = getGraphics();
        if(g != null)
        {
            Dimension dimension = getSize();
            g.setClip(0, 0, dimension.width, dimension.height);
        }
        drawProgress(g, progress, i);
        progress = i;
    }

    public void setSize(Dimension dimension)
    {
        super.setSize(dimension);
        size = dimension;
    }
}
