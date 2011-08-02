/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.plugins;

import org.glassfish.admingui.plugins.annotations.ViewFragment;
import org.glassfish.admingui.plugins.annotations.ConsolePlugin;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Singleton;

/**
 *
 * @author jasonlee
 */
@Service
@Scoped(Singleton.class)
public class PluginService {

    private static List<ConsolePluginMetadata> plugins;
    private static final Set<String> classNames = new HashSet<String>();

    @Inject
    private static Habitat habitat;

    public List<ConsolePluginMetadata> getPlugins() {
        if (plugins == null) {
            plugins = new ArrayList<ConsolePluginMetadata>();
            for (ConsolePlugin cp : habitat.getAllByContract(ConsolePlugin.class)) {
                ConsolePluginMetadata cpm = new ConsolePluginMetadata(cp.priority);
                Class clazz = cp.getClass();
                try {
                    processAnnotations(clazz.getFields(), cpm, clazz);
                    cpm.setPluginPackage(clazz.getPackage().getName());
                } catch (Exception ex) {
                    Logger.getLogger(PluginService.class.getName()).log(Level.SEVERE, null, ex);
                }

                plugins.add(cpm);
            }

            Collections.sort(plugins, new ConsolePluginComparator());
        }
        return Collections.unmodifiableList(plugins);
    }

    protected void processAnnotations(Field[] fields, ConsolePluginMetadata cp, Class<?> clazz) throws IllegalAccessException, IllegalArgumentException {
        for (Field field : fields) {
            ViewFragment vf = field.getAnnotation(ViewFragment.class);
            if (vf != null) {
                cp.addViewFragment(vf.type(), (String) field.get(clazz));
            }
        }
    }

    public void addClass(String className) {
        classNames.add(className);
    }

    public static Set<String> getClassNames() {
        return classNames;
    }
}
