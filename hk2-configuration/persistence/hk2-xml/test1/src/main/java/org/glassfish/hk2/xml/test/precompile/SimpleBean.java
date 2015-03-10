/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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
package org.glassfish.hk2.xml.test.precompile;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.hk2.xml.api.annotations.Customize;
import org.glassfish.hk2.xml.api.annotations.Customizer;
import org.glassfish.hk2.xml.api.annotations.Hk2XmlPreGenerate;
import org.glassfish.hk2.xml.test.precompile.anno.EverythingBagel;
import org.glassfish.hk2.xml.test.precompile.anno.GreekEnum;

/**
 * @author jwells
 *
 */
@Hk2XmlPreGenerate
@XmlRootElement(name="simple-bean")
@Customizer(SimpleBeanCustomizer.class)
public interface SimpleBean {
    @XmlElement
    public String getName();
    public void setName(String name);
    
    @XmlElement(name="bagel-type")
    @EverythingBagel(byteValue = 13,
        booleanValue=true,
        charValue = 'e',
        shortValue = 13,
        intValue = 13,
        longValue = 13L,
        floatValue = (float) 13.00,
        doubleValue = 13.00,
        enumValue = GreekEnum.BETA,
        stringValue = "13",
        classValue = PreCompiledRoot.class,
    
        byteArrayValue = { 13, 14 },
        booleanArrayValue = { true, false },
        charArrayValue = { 'e', 'E' },
        shortArrayValue = { 13, 14 },
        intArrayValue = { 13, 14 },
        longArrayValue = { 13L, 14L },
        floatArrayValue = { (float) 13.00, (float) 14,00 },
        doubleArrayValue = { 13.00, 14.00 },
        enumArrayValue = { GreekEnum.GAMMA, GreekEnum.ALPHA },
        stringArrayValue = { "13", "14" },
        classArrayValue = { String.class, double.class })
    public int getBagelPreference();
    public void setBagelPreference(int bagelType);
    
    public int customizer12(boolean z, int i, long j, float f, double d, byte b, short s, char c, int... var);
    
    @Customize
    public void addListener(boolean[] z, byte[] b, char[] c, short[] s, int[] i, long[]j, String[] l);
}
