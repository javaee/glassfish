package test3;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.VariableResolver;
import org.jvnet.hk2.config.TranslationException;
import org.jvnet.hk2.config.Dom;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Uses &lt;property name="..." value="..." /> in the config file
 * to perform variable substitutions.
 * 
 * @author Kohsuke Kawaguchi
 */
@Service
public class VariableResolverImpl extends VariableResolver {
    /**
     * Represents raw property key -> value map.
     *
     * <p>
     * The value may contain variable references, so these need
     * to be recursively resolved.
     */
    private Map<String,String> properties = new HashMap<String,String>();

    private boolean set = false;

    public void setProperties(List<Dom> propDoms) {
        for (Dom p : propDoms)
            properties.put(p.attribute("name"),p.attribute("value"));
        set = true;
    }

    protected String getVariableValue(String varName) throws TranslationException {
        String v = properties.get(varName);
        if(v==null)     return null;

        // recursively expand variables
        return translate(v);
    }

    public String translate(String str) throws TranslationException {
        if(!set)    return str; // no translation yet
        return super.translate(str);
    }
}
