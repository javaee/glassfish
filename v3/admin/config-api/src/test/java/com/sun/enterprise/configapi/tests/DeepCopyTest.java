/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.configapi.tests;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import org.glassfish.config.support.ConfigurationPersistence;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hk2.config.*;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.beans.PropertyVetoException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Test the deepCopy feature of ConfigBeans.
 *
 * @author Jerome Dochez
 */
public class DeepCopyTest extends ConfigApiTest {

    public String getFileName() {
        return "DomainTest";
    }

    @Test
    public void configCopy() throws Exception {
        Config config = getHabitat().getComponent(Config.class);
        Assert.assertNotNull(config);
        String configName = config.getName();
        final Config newConfig = (Config) config.deepCopy();
        Assert.assertNotNull(newConfig);
        try {
            newConfig.setName("some-config");
        } catch(Exception e) {
            // I was expecting this...
        }
        ConfigSupport.apply(new SingleConfigCode<Config>() {
            @Override
            public Object run(Config wConfig) throws PropertyVetoException, TransactionFailure {
                wConfig.setName("some-config");
                return null;
            }
        }, newConfig);
        Assert.assertEquals(newConfig.getName(), "some-config");
        Assert.assertEquals(config.getName(), configName);

        // add it the parent
        ConfigSupport.apply(new SingleConfigCode<Configs>() {
            @Override
            public Object run(Configs wConfigs) throws PropertyVetoException, TransactionFailure {
                wConfigs.getConfig().add(newConfig);
                return null;
            }
        }, getHabitat().getComponent(Configs.class));
        String resultingXML = save(document).toString();
        Assert.assertTrue(resultingXML.contains("some-config"));
    }

    final DomDocument document = getDocument(getHabitat());

    public OutputStream save(DomDocument doc) throws IOException, XMLStreamException {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        outStream.reset();

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = factory.createXMLStreamWriter(outStream);
        doc.writeTo(new IndentingXMLStreamWriter(writer));
        writer.close();
        return outStream;
    }
}
