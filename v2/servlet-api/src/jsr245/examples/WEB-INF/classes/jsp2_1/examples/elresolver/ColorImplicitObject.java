/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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
package jsp2_1.examples.elresolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Implicit object that ${Color} resolves to.
 * 
 * @author Mark Roth
 */
public class ColorImplicitObject {
    
    /** Set of colors by name */
    private static HashMap colorNames = null;
    
    /**
     * Returns a color from an HTML-style hex String, e.g. #f0f0f0
     */
    public static ColorRGB fromHex(String hex) {
        return fromColor(java.awt.Color.decode(hex));
    }
    
    /**
     * Returns a color from a java.awt.Color object.
     */ 
    public static ColorRGB fromColor(java.awt.Color color) {
        return new ColorRGB(color.getRed(), color.getGreen(), color.getBlue());
    }
    
    /**
     * Returns a color from a name.  Uses the resource rgb.txt to load
     * color names.
     */
    public static ColorRGB fromName(String name) {
        if(colorNames == null) {
            loadColorNames();
        }
        return (ColorRGB)colorNames.get(name);
    }
    
    public String toString() {
        return "Color Implicit Object";
    }
    
    /**
     * Package-scope method to get list of all color names
     */
    static Iterator colorNameIterator() {
        if(colorNames == null) {
            loadColorNames();
        }
        return colorNames.keySet().iterator();
    }
    
    /**
     * Loads colors from resource rgb.txt and converts them to 
     * instances of ColorRGB.
     */
    private synchronized static void loadColorNames() {
        if(colorNames == null) {
            colorNames = new HashMap();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                    ColorImplicitObject.class.getResourceAsStream(
                    "/jsp2_1/examples/elresolver/rgb.txt")));
                String line;
                while((line = in.readLine()) != null) {
                    if(!line.startsWith("!")) {
                        String colorText = line.substring(0, 12);
                        String colorName = line.substring(12).trim();
                        StringTokenizer st = new StringTokenizer(
                            colorText, " ");
                        int red = Integer.parseInt(st.nextToken().trim());
                        int green = Integer.parseInt(st.nextToken().trim());
                        int blue = Integer.parseInt(st.nextToken().trim());
                        colorNames.put(colorName, new ColorRGB(red, green, 
                            blue));
                    }
                }
                in.close();
            }
            catch(IOException e) {
                throw new RuntimeException("Could not load rgb.txt", e);
            }
        }
    }
    
    /**
     * Color whose red property has been specified
     * (e.g. ${Color[100]})
     */
    public static class ColorR {
        private int red;
        public ColorR(int red) {
            // Handle low or high values robustly.
            if(red < 0) red = 0;
            if(red > 255) red = 255;
            this.red = red;
        }
        public int getRed() {
            return red;
        }
        public void setRed(int r) {
            red = r;
        }
        public String toString() {
            return "Color(" + red + ", ?, ?)";
        }
    }
    
    /**
     * Color whose red and green properties have been specified
     * (e.g. ${Color[100][150]})
     */
    public static class ColorRG 
        extends ColorR
    {
        private int green;
        public ColorRG(int red, int green) {
            super(red);
            // Handle low or high values robustly.
            if(green < 0) green = 0;
            if(green > 255) green = 255;
            this.green = green;
        }
        public int getGreen() {
            return green;
        }
        public void setGreen(int g) {
            green = g;
        }
        public String toString() {
            return "Color(" + getRed() + ", " + green + ", ?)";
        }
    }
    
    public static class ColorRGB 
        extends ColorRG
    {
        private int blue;
        public ColorRGB(int red, int green, int blue) {
            super(red, green);
            // Handle low or high values robustly.
            if(blue < 0) blue = 0;
            if(blue > 255) blue = 255;
            this.blue = blue;
        }
        public int getBlue() {
            return blue;
        }
        public void setBlue(int b) {
            blue = blue;
        }
        public java.awt.Color getColor() {
            return new java.awt.Color(getRed(), getGreen(), getBlue());
        }
        
        public ColorRGB getDarker() {
            java.awt.Color darkerColor = getColor().darker();
            return fromColor(darkerColor);
        }
        
        public ColorRGB getBrighter() {
            java.awt.Color brighterColor = getColor().brighter();
            return fromColor(brighterColor);
        }
        
        public String getHex() {
            return "#" + toHex(getRed()) + toHex(getGreen()) + 
                toHex(getBlue());
        }
        
        private String toHex(int i) {
            String result;
            if(i < 16) {
                result = "0" + Integer.toHexString(i);
            }
            else {
                result = Integer.toHexString(i);
            }
            return result;
        }
        
        public String toString() {
            return "Color(" + getRed() + ", " + getGreen() + ", " + blue + ")";
        }
    }
}
