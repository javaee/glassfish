/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admin.rest.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author rajeshwar patil
 */
public class JsonInputObject extends InputObject {

    /**
     * Construct a JsonInputObject from a input stream.
     * @param inputstream an input stream 
     * @exception InputException If there is a syntax error in the source
     *  input stream or a duplicate key.
     */
    public JsonInputObject(InputStream inputstream) throws InputException, IOException {
        this(readAsString(inputstream));
    }


    /**
     * Construct a JsonInputObject from a JSON text string.
     * @param source    A JSON text string
     * @exception InputException If there is a syntax error in the source
     *  string or a duplicated key.
     */
    public JsonInputObject(String source) throws InputException {
        this(new JsonInputReader(source));
    }


    public JsonInputObject(JsonInputReader jsonReader) throws InputException {
        this.jsonReader = jsonReader;
        this.map = new HashMap();
    }


    /**
     * Construct and returns a map of input key-value pairs
     * @throws InputException If there is a syntax error in the source string
     *  or a duplicated key.
     */
    @Override
    public Map initializeMap() throws InputException {
        char c;
        String key = null;


        if (jsonReader.nextNonSpace() != '{') {
            //Fix for Issue: 8967; allowing empty (no representation) as valid json.
            if (jsonReader.nextNonSpace() == 0) {
                //we are reading first character and nextNonSpace() is returning 0
                //nextNonSpace() return 0 if there are no more characters.
                return new HashMap<String, String>();
            } else {
                throw jsonReader.error("A JSON text must begin with '{'");
            }
        }
        for (;;) {
            c = jsonReader.nextNonSpace();
            switch (c) {
            case 0:
                throw jsonReader.error("A JSON text must end with '}'");
            case '}':
                return map;
            default:
                jsonReader.back();
                key = jsonReader.nextValue().toString();
                
            }

            /*
             * The key is followed by ':'.
             */

            c = jsonReader.nextNonSpace();
            if (c != ':') {
                throw jsonReader.error("Expected a ':' after a key");
            }
            
            Object value = jsonReader.nextValue();
            if (value instanceof JsonInputObject) {
                JsonInputObject jsonObject = (JsonInputObject) value;
                map.putAll(jsonObject.initializeMap());
            } else {
                put(key, (String) value);
            } 

            /*
             * Pairs are separated by ','.
             */

            switch (jsonReader.nextNonSpace()) {
            case ',':
                if (jsonReader.nextNonSpace() == '}') {
                    return map;
                }
                jsonReader.back();
                break;
            case '}':
                return map;
            default:
                throw jsonReader.error("Expected a ',' or '}'");
            }
        }
    }


    private JsonInputReader jsonReader;
}
