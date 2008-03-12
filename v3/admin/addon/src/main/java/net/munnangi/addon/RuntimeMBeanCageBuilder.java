package net.munnangi.addon;

import org.jvnet.hk2.component.CageBuilder;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.annotations.Service;
import javax.management.*;
import javax.management.modelmbean.*;
import java.lang.management.*;
import java.io.*;

@Service
public class RuntimeMBeanCageBuilder implements CageBuilder {

    public static int k = 0;

    public void onEntered(Inhabitant<?> i) {
        System.out.println("RuntimeMBeanCageBuilder: onEntered("+i.typeName()+"): time to create and register the mBean ...");

        Object o = i.get();
        if (o instanceof net.munnangi.addon.AddonCompanion) {
            System.out.println("instance of AddonCompanion ...");
            //System.out.println("classname = " + i.getClass().getName());
            System.out.println("getname = " + ((net.munnangi.addon.AddonCompanion)o).getName());
        } else if (o instanceof net.munnangi.addon.ConfigCompanion) {
            System.out.println("instance of ConfigCompanion ...");
            //System.out.println("classname = " + i.getClass().getName());
            System.out.println("getname = " + ((net.munnangi.addon.ConfigCompanion)o).getName());
        }

        if (k == 0) createMBean(o);
        k++;

    }



    private void createMBean(Object o) {
        System.out.println("creating mbean for o ..." + o);
        try {
            ObjectName mbon = new ObjectName("oem:name=companion");
            //Test1 t1 = new Test1();
            //System.out.println("ser res = " + t1.getClass().getResource("Test1.ser"));
            String className = o.getClass().getName();
            System.out.println("class name = " + className);
            System.out.println("ser res = " + o.getClass().getResource("AddonCompanion"+".ser"));
            InputStream fis = o.getClass().getResourceAsStream("AddonCompanion"+".ser");
            ObjectInputStream inStream = new ObjectInputStream( fis );
            ModelMBeanInfo mmbinfo1 = ( ModelMBeanInfo )inStream.readObject();
            RequiredModelMBean modelmbean = new RequiredModelMBean(mmbinfo1);
            modelmbean.setManagedResource(o, "objectReference");
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.registerMBean(modelmbean,mbon);
            System.out.println("invoking value ..." + mbs.invoke(mbon, "getName", null, null));
            //System.out.println("getattr value ..." + mbs.getAttribute(mbon, "Name"));
            //System.out.println("Waiting forever..."); 
            //Thread.sleep(Long.MAX_VALUE); 
        } catch (Exception e) {
            System.out.println("MSR: exception while creating mBean ...");
            e.printStackTrace();
        }
    }

}
