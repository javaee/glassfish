/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.jvnet.hk2.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.ComponentException;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a whole DOM tree.
 *
 * @author Kohsuke Kawaguchi
 */
public class DomDocument {
    /**
     * A hook to perform variable replacements on the attribute/element values
     * that are found in the configuration file.
     * The translation happens lazily when objects are actually created, not when
     * configuration is parsed, so this allows circular references &mdash;
     * {@link Translator} may refer to objects in the configuration file being read.
     */
    private volatile Translator translator = Translator.NOOP;

    protected final Map<Inhabitant<? extends ConfigInjector>,ConfigModel> models = new HashMap<Inhabitant<? extends ConfigInjector>, ConfigModel>();

    /*package*/ final Habitat habitat;

    /*package*/ Dom root;

    private final Map<String, DataType> validators = new HashMap<String, DataType>();
    
    /*package*/ static final List<String> PRIMS = Collections.unmodifiableList(Arrays.asList(
    "boolean", "char", "int", "java.lang.Boolean", "java.lang.Character", "java.lang.Integer"));
    
    public DomDocument(Habitat habitat) {
        this.habitat = habitat;
        for (String prim : PRIMS) {
            validators.put(prim, new PrimitiveDataType(prim) );
        }
    }

    public Dom getRoot() {
        return root;
    }

    public Translator getTranslator() {
        return translator;
    }

    public void setTranslator(Translator translator) {
        this.translator = translator;
    }

    /**
     * Creates {@link ConfigModel} for the given {@link ConfigInjector} if we haven't done so.
     */
    /*package*/ ConfigModel buildModel(Inhabitant<? extends ConfigInjector> i) {
        ConfigModel m = models.get(i);
        if(m==null)
            m = new ConfigModel(this,i,i.metadata());
        return m;
    }

    /**
     * Obtains a {@link ConfigModel} for the given class (Which should have {@link Configured} annotation on it.)
     */
    public ConfigModel buildModel(Class<?> clazz) {
        return buildModel(clazz.getName());
    }

    /**
     * Obtains a {@link ConfigModel} for the given class (Which should have {@link Configured} annotation on it.)
     */
    public ConfigModel buildModel(String fullyQualifiedClassName) {
        Inhabitant i = habitat.getInhabitantByAnnotation(InjectionTarget.class, fullyQualifiedClassName);
        if(i==null)
            throw new ComponentException("ConfigInjector for %s is not found",fullyQualifiedClassName);
        return buildModel(i);
    }

    /**
     * Obtains the {@link ConfigModel} from the "global" element name.
     *
     * <p>
     * This method uses {@link #buildModel} to lazily build models if necessary.
     * 
     * @return
     *      Null if no configurable component is registered under the given global element name.
     */
    public ConfigModel getModelByElementName(String elementName) {
        Inhabitant<? extends ConfigInjector> i = habitat.getInhabitant(ConfigInjector.class, elementName);
        if(i==null) return null;
        return buildModel(i);
    }

    // TODO: to be removed once we make sure that no one is using it anymore
    @Deprecated
    public ConfigModel getModel(Class c) {
        return buildModel(c);
    }

    public Dom make(Habitat habitat, XMLStreamReader in, Dom parent, ConfigModel model) {
        return new Dom(habitat,this,parent,model,in);
    }

    /**
     * Writes back the whole DOM tree as an XML document.
     *
     * <p>
     * To support writing a subtree, this method doesn't invoke the start/endDocument
     * events. Those are the responsibility of the caller.
     *
     * @param w
     *      Receives XML infoset stream.
     */
    public void writeTo(XMLStreamWriter w) throws XMLStreamException {
        root.writeTo(null,w);
    }
    
    /*package*/
    DataType getValidator(String dataType) {
        synchronized(validators) {
            DataType validator = validators.get(dataType);
            if (validator != null)
                return (validator);
        }
        Collection<DataType> dtfh = habitat.getAllByContract(DataType.class);
        synchronized(validators) {
            for (DataType dt : dtfh) {
                validators.put(dt.getClass().getCanonicalName(), dt);
            }
            return (validators.get(dataType));
        }
    }
    
}
