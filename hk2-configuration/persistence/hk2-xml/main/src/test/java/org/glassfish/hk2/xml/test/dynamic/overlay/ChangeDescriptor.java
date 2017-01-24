package org.glassfish.hk2.xml.test.dynamic.overlay;

import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.glassfish.hk2.configuration.hub.api.Change;
import org.glassfish.hk2.configuration.hub.api.Change.ChangeCategory;
import org.glassfish.hk2.utilities.general.GeneralUtilities;

/**
 * This is diff'd against the change that was received to make
 * it easier to build up test cases
 * @author jwells
 *
 */
public final class ChangeDescriptor {
    private final ChangeCategory category;
    private final String typeName;
    private final List<String> instanceKey;
    private final String props[];
    private final String instance;
    private final String arName; // add-remove name also the old name
    
    public ChangeDescriptor(ChangeCategory category, String type, String instance, String arName, String... props) {
        this.category = category;
        this.typeName = type;
        this.props = props;
        this.instanceKey = tokenizeInstanceKey(instance);
        this.instance = instance;
        this.arName = arName;
    }
    
    private static List<String> tokenizeInstanceKey(String instance) {
        LinkedList<String> retVal = new LinkedList<String>();
        
        if (instance == null) return retVal;
        
        StringTokenizer st = new StringTokenizer(instance, ".");
        while (st.hasMoreTokens()) {
            String nextToken = st.nextToken();
            if (nextToken.startsWith("XMLServiceUID")) continue;
            
            retVal.add(nextToken);
        }
        
        return retVal;
    }
    
    private String checkInstanceKey(String recievedKey) {
        List<String> receivedToken = tokenizeInstanceKey(recievedKey);
        
        if (instanceKey.size() != receivedToken.size()) {
            return "Instance cardinality for " + recievedKey + " does not match " + instance;
        }
        
        for (int lcv = 0; lcv < receivedToken.size(); lcv++) {
            String expected = instanceKey.get(lcv);
            String received = receivedToken.get(lcv);
            
            if ("*".equals(expected)) continue;
            if (!GeneralUtilities.safeEquals(expected, received)) {
              return "Failed in " + this + " at index " + lcv;
            }
        }
        
        return null;
    }
    
    String check(Change change) {
        if (!GeneralUtilities.safeEquals(category, change.getChangeCategory())) {
            return "Category is not the same expected=" + this + " got=" + change;
        }
        
        if (!GeneralUtilities.safeEquals(typeName, change.getChangeType().getName())) {
            return "Type is not the same expected=" + this + " got=" + change;
        }
        
        String errorInstanceKey = checkInstanceKey(change.getInstanceKey());
        if (errorInstanceKey != null) return errorInstanceKey;
        
        List<PropertyChangeEvent> modifiedProperties = change.getModifiedProperties();
        if (modifiedProperties == null) {
            modifiedProperties = Collections.emptyList();
        }
        
        if (props.length != modifiedProperties.size()) {
            return "Expectect property length of " + props.length + " but got size " + modifiedProperties.size();
        }
        for (int lcv = 0; lcv < props.length; lcv++) {
            String prop = props[lcv];
            
            // Props is unordered, must go through list
            boolean found = false;
            for (int inner = 0; inner < modifiedProperties.size(); inner++) {
                if (GeneralUtilities.safeEquals(prop, modifiedProperties.get(inner).getPropertyName())) {
                    found = true;
                    break;
                }
            }
            
            if (!found) {
              return "Did not find prop " + prop + " in " + this;
            }
        }
        
        return null;
    }
    
    @Override
    public String toString() {
        return category + " type=" + typeName + " name=" + arName + " instanceKey=" + instanceKey;
    }
    
}