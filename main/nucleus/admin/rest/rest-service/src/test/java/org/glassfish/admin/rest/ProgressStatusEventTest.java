/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package org.glassfish.admin.rest;

import com.sun.enterprise.admin.remote.reader.ProgressStatusEventJsonProprietaryReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.ws.rs.core.MediaType;
import org.glassfish.admin.rest.provider.ProgressStatusEventJsonProvider;
import org.glassfish.api.admin.progress.ProgressStatusEvent;
import org.glassfish.api.admin.progress.ProgressStatusEventComplete;
import org.glassfish.api.admin.progress.ProgressStatusEventCreateChild;
import org.glassfish.api.admin.progress.ProgressStatusEventProgress;
import org.glassfish.api.admin.progress.ProgressStatusEventSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author martinmares
 */
public class ProgressStatusEventTest {
    
    private static ProgressStatusEventSet EVENT_SET = new ProgressStatusEventSet("a", 1, null);
    private static ProgressStatusEventProgress EVENT_PROGRESS = new ProgressStatusEventProgress("a", 3, "some message", true);
    private static ProgressStatusEventComplete EVENT_COMPLETE = new ProgressStatusEventComplete("a", "some message");
    private static ProgressStatusEventCreateChild EVENT_CREATE_CHILD = new ProgressStatusEventCreateChild("a", "child", "a.b", 10, 5);
    
    private static ProgressStatusEventJsonProvider writer = new ProgressStatusEventJsonProvider();
    private static ProgressStatusEventJsonProprietaryReader reader = new ProgressStatusEventJsonProprietaryReader();
    
    @Test
    public void testEventSet() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writer.writeTo(EVENT_SET, null, null, null, MediaType.APPLICATION_JSON_TYPE, null, baos);
        baos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ProgressStatusEvent event = reader.readFrom(bais, MediaType.APPLICATION_JSON);
        assertEquals(event, EVENT_SET);
    }
    
    @Test
    public void testEventProgress() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writer.writeTo(EVENT_PROGRESS, null, null, null, MediaType.APPLICATION_JSON_TYPE, null, baos);
        baos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ProgressStatusEvent event = reader.readFrom(bais, MediaType.APPLICATION_JSON);
        assertEquals(event, EVENT_PROGRESS);
    }
    
    @Test
    public void testEventComplete() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writer.writeTo(EVENT_COMPLETE, null, null, null, MediaType.APPLICATION_JSON_TYPE, null, baos);
        baos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ProgressStatusEvent event = reader.readFrom(bais, MediaType.APPLICATION_JSON);
        assertEquals(event, EVENT_COMPLETE);
    }
    
    @Test
    public void testEventCreateChild() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writer.writeTo(EVENT_CREATE_CHILD, null, null, null, MediaType.APPLICATION_JSON_TYPE, null, baos);
        baos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ProgressStatusEvent event = reader.readFrom(bais, MediaType.APPLICATION_JSON);
        assertEquals(event, EVENT_CREATE_CHILD);
    }
    
}
