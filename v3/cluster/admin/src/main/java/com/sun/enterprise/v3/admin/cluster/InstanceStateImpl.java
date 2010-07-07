package com.sun.enterprise.v3.admin.cluster;

import org.glassfish.api.admin.InstanceState;
import org.jvnet.hk2.annotations.Service;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation of InstanceState
 * @author Vijay Ramachandran
 */
@Service
public class InstanceStateImpl implements InstanceState {

    private ConcurrentHashMap<String, StateType> instanceStates = new ConcurrentHashMap<String, StateType>();
    
    public StateType getState(String instanceName) {
        if(instanceStates.get(instanceName) == null)
            return InstanceState.StateType.NOT_RUNNING;
        return instanceStates.get(instanceName);
    }

    public void setState(String name, StateType s) {
        instanceStates.put(name, s);
    }
}
