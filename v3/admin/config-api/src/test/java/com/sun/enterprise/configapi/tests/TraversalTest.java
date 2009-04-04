package com.sun.enterprise.configapi.tests;

import org.junit.Test;
import org.junit.Ignore;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.component.Habitat;

import java.util.Set;

import com.sun.enterprise.config.serverbeans.Domain;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Apr 3, 2009
 * Time: 9:40:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class TraversalTest extends ConfigApiTest {

    public String getFileName() {
        return "DomainTest";
    }

    @Ignore
    public void traverse() {
        Habitat habitat = super.getHabitat();
        Domain domain = habitat.getComponent(Domain.class);
        introspect(0, Dom.unwrap(domain));
    }
    
    public void introspect(int indent, Dom proxy) {
        // System.out.println("key for  " + proxy.getClass().getName()+" is "+proxy.getKey());
        indent = indent + 1;
        Set<String> ss = proxy.getAttributeNames();
        String id = "";
        for (int i = 0; i < indent; i++) {
            id = id + "    ";
        }
                    System.out.println(id + "--------"+ proxy.model.key);
        for (String a : ss) {

            System.out.println(id + a + "=" + proxy.attribute(a));
        }





        Set<String> elem = proxy.getElementNames();
        //System.out.println(id+"set size is" + elem.size());

        for (String bb : elem) {

            try {
             ///   List<Dom> dodo = proxy.nodeElements(bb);
             ///   for (Dom bbd : dodo) {

                Dom dodo = proxy.element(bb);
                    System.out.println(id + "<" + bb + ">");
                  //  System.out.println(id + "---collection-----"+ proxy.isCollection(bb));
                  //  System.out.println(id + "---leaf      -----"+ proxy.isLeaf(bb));
                    introspect(indent, dodo);
                    System.out.println(id + "</" + bb + ">");
                    System.out.println("    ");
              ///  }

            } catch (Exception e) {
                System.out.println(id + "</" + bb + "> ERRORROOR");
                e.printStackTrace();

            }
        }
    }
}
