/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package sample.controllers;

import org.junit.ClassRule;
import org.junit.Test;
import sample.EmbeddedGrizzly;
import sample.MyApplication;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

/**
 * Created by yoan on 2016/06/02.
 */
public class SampleControllerTest {
    @ClassRule
    public static EmbeddedGrizzly embeddedGrizzly = new EmbeddedGrizzly(MyApplication.class);

    @Test // @org.junit.Ignore
    public void foo() throws Exception {
        String res = get("foo");
        assertThat(res, startsWith("foo"));
        assertThat(res, containsString("http://localhost:8090/"));
    }

    @Test // @org.junit.Ignore
    public void baa() throws Exception {
        MultivaluedMap<String, String> form = new MultivaluedHashMap<String, String>() {{
            add("btnName", "abc");
        }};
        String res = post("baa", form);
        assertThat(res, containsString("btnName = abc"));
        assertThat(res, containsString("http://localhost:8090/"));
    }

    @Test @org.junit.Ignore
    public void hoge() throws Exception {
        MultivaluedMap<String, String> form = new MultivaluedHashMap<String, String>() {{
            add("btnName", "fuga");
        }};
        String res = post("hoge", form);
        assertThat(res, containsString("btnName = fuga"));
        assertThat(res, containsString("http://localhost:8090/"));
    }

    private String get(String path) {
        return ClientBuilder.newClient()
                .target(embeddedGrizzly.getBaseUri())
                .path(path)
                .request()
                .get(String.class);
    }

    private String post(String path, MultivaluedMap<String, String> form) {
        return ClientBuilder.newClient()
                .target(embeddedGrizzly.getBaseUri())
                .path(path)
                .request()
                .post(Entity.form(form), String.class);
    }

}