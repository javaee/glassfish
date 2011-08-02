/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.plugins.sample;

import org.glassfish.admingui.plugins.annotations.ConsolePlugin;
import org.glassfish.admingui.plugins.annotations.ViewFragment;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

/**
 * Only one of these is needed, strictly speaking, per plugin module. You can
 * use several if you want the separation of data, but it's not necessary.
 * @author jasonlee
 */
@Service
@Scoped(Singleton.class)
public class SamplePlugin implements ConsolePlugin {
    public int priority = 275;

    @ViewFragment(type="tab")
    public static final String TAB = "/sample/tab.xhtml";
    
    @ViewFragment(type="navNode", parent="")
    public static final String NAV_NODES = "/sample/navNodes.xhtml";
}