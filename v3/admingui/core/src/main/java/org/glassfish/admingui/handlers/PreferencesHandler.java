/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 * @author jasonlee
 */
public class PreferencesHandler {

    @Handler(id = "saveTagInformation",
        input = {
            @HandlerInput(name = "tag", type = String.class, required = true),
            @HandlerInput(name = "name", type = String.class, required = true),
            @HandlerInput(name = "url", type = String.class, required = true)
        }
    )
    public static void saveTagInformation(HandlerContext handlerCtx) {
        try {
            String tag = (String) handlerCtx.getInputValue("tag");
            String name = (String) handlerCtx.getInputValue("name");
            String url = (String) handlerCtx.getInputValue("url");
            String user = handlerCtx.getFacesContext().getExternalContext().getUserPrincipal().getName();
            System.out.println("Adding the  '" + tag + "' ('" + name + "') for URL '" + url + "' on behalf of the user '" + user + "'.");

            // Once we're happy with the functionality, we can revisit how we handle this part
            Preferences tags = Preferences.userRoot().node(user).node("tags");
            Preferences page = tags.node(tag).node(name);
            page.put("url", url);
            for (String child1 : tags.childrenNames()) {
                Preferences node = tags.node(child1);
                for (String child2 : node.childrenNames()) {
                    System.out.println("Child the tag " + tag + ":  " + child2);
                }
            }
        } catch (BackingStoreException ex) {
            Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Handler(id="searchTags",
        input = { @HandlerInput(name="tag", type=String.class, required = true) },
        output = { @HandlerOutput(name="hits", type=List.class)})
    public static void searchTags(HandlerContext handlerCtx) throws BackingStoreException {
        String tag = (String) handlerCtx.getInputValue("tag");
        String user = handlerCtx.getFacesContext().getExternalContext().getUserPrincipal().getName();
        Preferences tags = Preferences.userRoot().node(user).node("tags").node(tag);
        List<Tag> hits = new ArrayList<Tag>();
        for (String entry : tags.childrenNames()) {
            Preferences child  = tags.node(entry);
            hits.add(new Tag(child.get("url", ""), entry));
        }
        handlerCtx.setOutputValue("hits", hits);
    }

    public static void main(String... args) {
        try {
            Preferences base = Preferences.userRoot().node("anonymous.tags.foo");
            Preferences page1 = base.node("This is the home page");
            page1.put("url", "http://localhost:8080/admingui/layouttest.jsf");
            Preferences page2 = base.node("This is the home page as well");
            page2.put("url", "http://localhost:8080/admingui/layouttest.jsf");
            
            System.out.println("Loop #1");
            for (String name : base.childrenNames()) {
                System.out.println("Child node for foo: " + name);
            }
            base.removeNode();
            base = Preferences.userRoot().node("anonymous.tags.foo");
            System.out.println("Loop #2");
            for (String name : base.childrenNames()) {
                System.out.println("Child node for foo: " + name);
            }
        } catch (BackingStoreException ex) {
            Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

// PoC class.  Will likely be refactored
class  Tag {
    String url;
    String name;
    Tag(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String toString() {
        return name + " - " + url;
    }
    
}