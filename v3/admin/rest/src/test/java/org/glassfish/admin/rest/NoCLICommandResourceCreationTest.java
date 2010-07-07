/*
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

package org.glassfish.admin.rest;

import com.sun.jersey.api.client.ClientResponse;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Mitesh Meswani
 */
public class NoCLICommandResourceCreationTest extends RestTestBase {
    private static final String BASE_DOMAIN_PROPERTY_URL = BASE_URL+ "/property";

    @Test
    public void testPropertyCreation() {
        final String propertyKey  = "propertyName" + generateRandomString();
        String propertyValue = generateRandomString();

        //Create a property
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", propertyKey);
        params.put("value",propertyValue);
        ClientResponse response = create(BASE_DOMAIN_PROPERTY_URL, params);
        assertTrue(isSuccess(response));

        //Verify the property got created
        String propertyURL = BASE_DOMAIN_PROPERTY_URL + "/" + propertyKey;
        response = get (propertyURL);
        assertTrue(isSuccess(response));
        Map<String, String> entity = getEntityValues(response);
        assertTrue(entity.get("name").equals(propertyKey));
        assertTrue(entity.get("value").equals(propertyValue));

        // Verify property update
        propertyValue = generateRandomString();
        params.put("value", propertyValue);
        response = update(propertyURL, params);
        assertTrue(isSuccess(response));
        response = get (propertyURL);
        assertTrue(isSuccess(response));
        entity = getEntityValues(response);
        assertTrue(entity.get("name").equals(propertyKey));
        assertTrue(entity.get("value").equals(propertyValue));

        //Clean up to leave domain.xml good for next run
        response = delete(propertyURL, new HashMap<String, String>());
        assertTrue(isSuccess(response));
    }

}