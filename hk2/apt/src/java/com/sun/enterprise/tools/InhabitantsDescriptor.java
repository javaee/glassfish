/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.enterprise.tools;

import static com.sun.hk2.component.InhabitantsFile.INDEX_KEY;

import com.sun.hk2.component.InhabitantFileBasedParser;
import com.sun.hk2.component.InhabitantParser;
import com.sun.hk2.component.InhabitantsFile;
import com.sun.hk2.component.InhabitantsScanner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.jvnet.hk2.component.MultiMap;

/**
 * Inhabitants descriptor as a map from the class name to its line.
 * 
 * @see InhabitantFileBasedParser
 * 
 * @author Kohsuke Kawaguchi
 * @author Jeff Trent
 */
@SuppressWarnings("serial")
public class InhabitantsDescriptor extends HashMap<String, String> {
  private boolean dirty = false;
  private boolean dateEnabled = true;
  private boolean commentEnabled = true;
  private String comment;
  
  public InhabitantsDescriptor() {
  }

  public InhabitantsDescriptor(File f) throws IOException {
    load(f);
  }
  
  public void enableDateOutput(boolean enabled) {
    this.dateEnabled = enabled;
  }
  
  public void enableCommentOutput(boolean enabled) {
    this.commentEnabled = enabled;
  }
  
  public void setComment(String comment) {
    if (null == comment) {
      this.comment = comment;
    } else {
      this.comment = "# " + comment;
    }
  }

  public void appendComment(String comment) {
    if (null == this.comment) {
      setComment(comment);
    } else {
      this.comment += "\n# " + comment;
    }
  }
  
  /**
   * Loads an existing file.
   */
  public void load(File f) throws IOException {
    FileInputStream in = new FileInputStream(f);
    try {
      InhabitantsScanner scanner = new InhabitantsScanner(in, f.getPath());
      for (InhabitantParser kvpp : scanner) {
        put(kvpp.getImplName(), kvpp.getLine());
      }
    } finally {
      if (null != in) {
        in.close();
      }
    }
  }

  @Override
  public String put(String key, String value) {
    dirty = true;
    return super.put(key, value);
  }

  @SuppressWarnings("unchecked")
  public String putAll(String service,
      Collection<String> contracts,
      Collection<String> annotations,
      String name,
      Object metaObj) {
    StringBuilder buf = new StringBuilder();
    buf.append(InhabitantsFile.CLASS_KEY).append('=').append(service);
    
    if (null != contracts) {
      for (String contract : contracts) {
        buf.append(",").append(INDEX_KEY).append("=").append(contract);
        if (null != name) {
          buf.append(":").append(name);
        }
      }
    }
    
    if (null != annotations) {
      for (String contract : annotations) {
        buf.append(",").append(INDEX_KEY).append("=").append(contract);
      }
    }
    
    if (metaObj instanceof Map) {
      Map<String, String> meta = (Map<String, String>)metaObj;
      for (Map.Entry<String, String> entry : meta.entrySet()) {
        buf.append(",").append(entry.getKey()).append("=").append(entry.getValue());
      }
    } else if (metaObj instanceof MultiMap) {
      MultiMap<String, String> meta = (MultiMap<String, String>)metaObj;
      for (Map.Entry<String, List<String>> entry : meta.entrySet()) {
        String key = entry.getKey();
        List<String> vals = entry.getValue();
        for (String val : vals) {
          buf.append(",").append(key).append("=").append(val);
        }
      }
    }
    
    return put(service, buf.toString());
  }

  @Override
  public String remove(Object key) {
    dirty = true;
    return super.remove(key);
  }

  /**
   * Writes the descriptor to a file.
   */
  public void write(File outputDir, Messager messager, String habitatName) {
    File out = new File(new File(outputDir, InhabitantsFile.PATH), habitatName);
    
    if (!dirty) {
      if (out.exists()) {
        out.delete();
      }
      
      return; // no need to write.
    }

    try {
      write(out);
    } catch (IOException e) {
      if (null == messager) {
        Logger.getAnonymousLogger().warning("Failed to write inhabitants file " + habitatName);
      } else {
        messager.printError("Failed to write inhabitants file " + habitatName, e);
      }
    }
  }

  public void write(File out) throws IOException {
    File parent = out.getParentFile();
    if (null != parent) {
      parent.mkdirs();
    }
    
    PrintWriter w = new PrintWriter(out, "UTF-8");
    try {
      write(w);
    } finally {
      w.close();
    }
  }

  @SuppressWarnings("deprecation")
  public void write(PrintWriter w) {
    if (dateEnabled) {
      w.println("# generated on " + new Date().toGMTString());
    }
    
    if (commentEnabled && null != comment) {
      w.println(comment);
    }
    
    for (String line : values()) {
      w.println(line);
    }
  }

  @Override
  public String toString() {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    PrintWriter w = new PrintWriter(os);
    write(w);
    w.close();
    return os.toString();
  }
  
}
