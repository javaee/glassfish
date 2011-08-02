/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.pluginprototype.plugins.hk2;

import org.glassfish.admingui.plugins.annotations.ConsolePlugin;
import org.glassfish.admingui.plugins.annotations.ViewFragment;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

/**
 *
 * @author jasonlee
 */
@Service
@Scoped(Singleton.class)
public class SamplePlugin implements ConsolePlugin {
    public int priority = 275;

    @ViewFragment(type="tab")
    public static final String TAB = "/hk2/tab.xhtml";
    
    @ViewFragment(type="navNode", parent="")
    public static final String NAV_NODES = "/sample/navNodes.xhtml";

    @ViewFragment(type="formPlugin")
    public static final String FORM = "/hk2/hello.xhtml";

    @ViewFragment(type="applicationPlugin")
    public static final String APPLICATION = "/hk2/application.xhtml";

    @ViewFragment(type="jdbc-resource")
    public static final String JDBC_RESOURCE = "/hk2/jdbc_resource.xhtml";
}