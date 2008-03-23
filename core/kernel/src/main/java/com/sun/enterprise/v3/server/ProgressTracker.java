package com.sun.enterprise.v3.server;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Feb 13, 2008
 * Time: 4:49:42 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ProgressTracker {

    Map<String, List<Object>> subjects = new HashMap<String, List<Object>>();

    public synchronized <T> void add(String name, Class<T> type, T subject) {
        if (!subjects.containsKey(name)) {
            subjects.put(name, new ArrayList());
        }
        subjects.get(name).add(subject);
        
    }


    public <T> void add(Class<T> type, T subject) {
        add(type.getName(), type, subject);
    }

    public <T> void addAll(Class<T> type, Iterable<T> subjects) {
        for (T subject : subjects) {
            add(type, subject);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> get(String name, Class<T> type) {
        if (subjects.containsKey(name)) {
            return (List<T>) subjects.get(name);
        } else {
            return Collections.emptyList();
        }
    }


    @SuppressWarnings("unchecked")
    public <T> List<T> get(Class<T> type) {
        return get(type.getName(), type);
    }

    public abstract void actOn(Logger logger);
}
