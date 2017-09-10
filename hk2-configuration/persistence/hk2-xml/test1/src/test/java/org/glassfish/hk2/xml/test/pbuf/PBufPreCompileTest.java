/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.test.pbuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Arrays;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.pbuf.api.PBufUtilities;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.pbuf.pc1.PBufPrecompileRootBean;
import org.glassfish.hk2.xml.test.pbuf.pc1.PBufPrecompileRootBeanOuterClass;
import org.glassfish.hk2.xml.test1.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

import com.google.protobuf.CodedOutputStream;

/**
 * @author jwells
 *
 */
public class PBufPreCompileTest {
    private final static String ALICE = "Alice";
    
    private final static String[] PROTO_RESOURCES = {
            "org/glassfish/hk2/xml/test/pbuf/pc1/PBufPrecompileChild.proto"
            , "org/glassfish/hk2/xml/test/pbuf/pc1/PBufPrecompileRootBean.proto"
            , "org/glassfish/hk2/xml/test/pbuf/pc2/PBufPrecompileChild2.proto"
            , "org/glassfish/hk2/xml/test/pbuf/pc2/TypeBean.proto"
            , "org/glassfish/hk2/xml/test/pbuf/pc1/ThingBean.proto"
            , "org/glassfish/hk2/xml/test/pbuf/pc1/ThingOneBean.proto"
            , "org/glassfish/hk2/xml/test/pbuf/pc1/ThingTwoBean.proto"
    };
    
    private final static String PRECOMPILED_PREFIX = "protos/";
    
    /**
     * Tests that the expected files are generate and put into the resulting jar file
     */
    @Test
    // @org.junit.Ignore
    public void testPrecompileHappens() throws Exception {
        ClassLoader loader = getClass().getClassLoader();
        
        for (String protoResource : PROTO_RESOURCES) {
            URL compiledURL = loader.getResource(protoResource);
            Assert.assertNotNull("Could not find " + protoResource, compiledURL);
            
            URL precompiledURL = loader.getResource(PRECOMPILED_PREFIX + protoResource);
            Assert.assertNotNull("Could not find " + protoResource, precompiledURL);
            
            byte compiledProto[] = Utilities.readBytesFromURL(compiledURL);
            byte precompiledProto[] = Utilities.readBytesFromURL(precompiledURL);
            
            Assert.assertTrue(Arrays.equals(precompiledProto, compiledProto));
        }
    }
    
    /**
     * Tests that we can interoperate with true protocol buffers
     * generated from java files
     */
    @Test
    public void testCanInteropWithTrueProtos() throws Exception {
        ServiceLocator locator = Utilities.createInteropLocator();
        
        PBufPrecompileRootBeanOuterClass.PBufPrecompileRootBean.Builder rootBuilder = PBufPrecompileRootBeanOuterClass.PBufPrecompileRootBean.newBuilder();
        
        PBufPrecompileRootBeanOuterClass.PBufPrecompileRootBean root = rootBuilder.setName(ALICE).
                setItype(13).
                build();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            int serializedSize = root.getSerializedSize();
            
            CodedOutputStream cos = CodedOutputStream.newInstance(baos);
            
            try {
                cos.writeInt32NoTag(serializedSize);
            
                root.writeTo(cos);
            }
            finally {
                cos.flush();
            }
        }
        finally {
            baos.close();
        }
        
        byte fromProto[] = baos.toByteArray();
        
        XmlService xmlService = locator.getService(XmlService.class, PBufUtilities.PBUF_SERVICE_NAME);
        
        XmlRootHandle<PBufPrecompileRootBean> writeHandle = xmlService.createEmptyHandle(PBufPrecompileRootBean.class);
        writeHandle.addRoot();
        
        PBufPrecompileRootBean writeRoot = writeHandle.getRoot();
        writeRoot.setName(ALICE);
        writeRoot.setIType(13);
        
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        try {
            writeHandle.marshal(baos2);
        }
        finally {
            baos2.close();
        }
        
        byte fromHk2[] = baos2.toByteArray();
        
        Assert.assertTrue(Arrays.equals(fromProto, fromHk2));
        
        PBufPrecompileRootBean hk2Root = null;
        ByteArrayInputStream bais = new ByteArrayInputStream(fromHk2);
        try {
            XmlRootHandle<PBufPrecompileRootBean> handle = xmlService.unmarshal(bais, PBufPrecompileRootBean.class);
            
            hk2Root = handle.getRoot();
        }
        finally {
            bais.close();
        }
        
        Assert.assertEquals(ALICE, hk2Root.getName());
        Assert.assertEquals(13, hk2Root.getIType());
    }

}
