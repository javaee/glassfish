/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.rails;

import java.io.IOException;
import java.io.InputStream;
import org.jruby.Ruby;
import org.jruby.util.IOHandlerUnseekable;

/**
 * Subclass of the jruby RubyIO implementation to add a inputstream ctor
 *
 * @author Jerome Dochez
 */
public class RubyIO extends org.jruby.RubyIO {
    
  public RubyIO(Ruby runtime, InputStream inputStream) {
       super(runtime, runtime.getClass("IO"));
       if (inputStream == null) {
           throw runtime.newIOError("Opening invalid stream");
       }
              try {
           handler = new IOHandlerUnseekable(runtime, inputStream, null);
       } catch (IOException e) {
           throw runtime.newIOError(e.getMessage());
       }
       modes = handler.getModes();
       registerIOHandler(handler);
   }    
}
