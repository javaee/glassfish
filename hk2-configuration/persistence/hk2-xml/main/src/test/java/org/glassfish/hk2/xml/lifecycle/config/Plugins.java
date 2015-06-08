/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.xml.lifecycle.config;

import java.beans.PropertyVetoException;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public interface Plugins {

  @XmlElement(name="*")
  List<Plugin> getPlugins();
  void setPlugins(List<Plugin> plugins);

  /*
  @DuckTyped
  Plugin createPlugin(String name, String type, String path);

  @DuckTyped
  Plugin getPluginByName(String name);

  @DuckTyped
  Plugin deletePlugin(Plugin plugin);

  class Duck  {
    public static Plugin createPlugin(final Plugins plugins, final String name, final String type, final String path) throws TransactionFailure {
      ConfigSupport.apply(new SingleConfigCode<Plugins>() {
        @Override
        public Object run(Plugins writeablePlugins) throws TransactionFailure, PropertyVetoException {
          Plugin plugin = writeablePlugins.createChild(Plugin.class);
          plugin.setName(name);
          plugin.setType(type);
          plugin.setPath(path);
          writeablePlugins.getPlugins().add(plugin);
          return plugin;
        }
      }, plugins);

      // read-only view
      return getPluginByName(plugins, name);
    }

    public static Plugin getPluginByName(final Plugins plugins, final String name) {
      List<Plugin> pluginConfigsList = plugins.getPlugins();
      for (Plugin plugin : pluginConfigsList) {
        if (name.equals(plugin.getName())) {
          return plugin;
        }
      }
      return null;
    }

    public static Plugin deletePlugin(final Plugins plugins,
        final Plugin plugin) throws TransactionFailure {
      return (Plugin) ConfigSupport.apply(new SingleConfigCode<Plugins>() {

        @Override
        public Object run(Plugins writeablePlugins)
            throws TransactionFailure {
          writeablePlugins.getPlugins().remove(plugin);
          return plugin; 
        }

      }, plugins);
    
    }

  }
  */
}
