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
package com.sun.enterprise.tools;

import static com.sun.hk2.component.InhabitantsFile.INDEX_KEY;

import com.sun.hk2.component.InhabitantParser;
import com.sun.hk2.component.InhabitantsFile;
import com.sun.hk2.component.InhabitantsScanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Inhabitants descriptor as a map from the class name to its line.
 * 
 * @author Kohsuke Kawaguchi
 * @author Jeff Trent
 */
public class InhabitantsDescriptor extends HashMap<String, String> {
  private boolean dirty = false;

  public InhabitantsDescriptor() {
  }

  public InhabitantsDescriptor(File f) throws IOException {
    load(f);
  }

  /**
   * Loads an existing file.
   */
  public void load(File f) throws IOException {
    InhabitantsScanner scanner = new InhabitantsScanner(new FileInputStream(f),
        f.getPath());
    for (InhabitantParser kvpp : scanner)
      put(kvpp.getImplName(), kvpp.getLine());
  }

  public String put(String key, String value) {
    dirty = true;
    return super.put(key, value);
  }

  public String putAll(String service, Collection<String> contracts,
      String name, Map<String, String> meta) {
    StringBuilder buf = new StringBuilder();
    buf.append(InhabitantsFile.CLASS_KEY).append('=').append(service);
    for (String contract : contracts) {
      buf.append(",").append(INDEX_KEY).append("=").append(contract);
      if (null != name) {
        buf.append(":").append(name);
      }
    }

    if (null != meta) {
      for (Map.Entry<String, String> entry : meta.entrySet()) {
        buf.append(",").append(entry.getKey()).append("=").append(
            entry.getValue());
      }
    }
    
    return put(service, buf.toString());
  }

  public String remove(Object key) {
    dirty = true;
    return super.remove(key);
  }

  /**
   * Writes the descriptor to a file.
   */
  public void write(File outputDir, Messager messager, String habitatName) {
    if (!dirty)
      return; // no need to write.

    PrintWriter w = null;
    try {
      File out = new File(new File(outputDir, InhabitantsFile.PATH), habitatName);
      out.getParentFile().mkdirs();
      w = new PrintWriter(out, "UTF-8");

      write(w);
    } catch (IOException e) {
      messager.printError("Failed to write inhabitants file " + habitatName, e);
    } finally {
      if (null != w) {
        w.close();
      }
    }
  }

  public void write(PrintWriter w) {
    w.println("# generated on " + new Date().toGMTString());
    for (String line : values()) {
      w.println(line);
    }
  }

}
