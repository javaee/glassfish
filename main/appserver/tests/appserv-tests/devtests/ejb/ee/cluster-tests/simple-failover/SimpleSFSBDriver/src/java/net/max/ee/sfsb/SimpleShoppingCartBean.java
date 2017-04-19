/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.max.ee.sfsb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.ejb.Remove;
import javax.ejb.Stateful;

/**
 *
 * @author mk
 */
@Stateful
@LocalBean
public class SimpleShoppingCartBean
    implements Serializable {

    private static AtomicInteger ai = new AtomicInteger();

    private ArrayList<String> items = new ArrayList<String>();

    private StringBuilder sb = new StringBuilder();

    private String id;

    private long counter = 0;

    public SimpleShoppingCartBean() {
        id = "id-" + ai.incrementAndGet();
        sb.append(id).append(" => ");
    }

    public String getId() {
        return id;
    }

    public void addItem() {
        String item = "Item-" + items.size();
        items.add(item);
        sb.append(item).append("; ");
    }

    public String asString() {
        return id + ": accessCount: " + sb.toString();
    }

    @Remove
    public void cleanup() {
    }
}
