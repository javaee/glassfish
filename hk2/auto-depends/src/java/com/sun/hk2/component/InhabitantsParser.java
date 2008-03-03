package com.sun.hk2.component;

import static com.sun.hk2.component.InhabitantsFile.CLASS_KEY;
import static com.sun.hk2.component.InhabitantsFile.INDEX_KEY;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.MultiMap;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Parses <tt>/META-INF/inhabitants</tt> and
 * populate {@link Habitat}.
 *
 * @author Kohsuke Kawaguchi
 */
public class InhabitantsParser {
    private final Habitat habitat;

    public InhabitantsParser(Habitat habitat) {
        this.habitat = habitat;
    }

    public void parse(InhabitantsScanner scanner, Holder<ClassLoader> classLoader) throws IOException {
        for( KeyValuePairParser kvpp : scanner) {
            MultiMap<String,String> metadata=buildMetadata(kvpp);
            Inhabitant i = new LazyInhabitant(habitat, classLoader, metadata.getOne(CLASS_KEY), metadata);
            habitat.add(i);

            for (String v : kvpp.findAll(INDEX_KEY)) {
                // register inhabitant to the index
                int idx = v.indexOf(':');
                if(idx==-1) {
                    // no name
                    habitat.addIndex(i,v,null);
                } else {
                    // v=contract:name
                    String contract = v.substring(0, idx);
                    String name = v.substring(idx + 1);
                    habitat.addIndex(i, contract, name);
                }
            }
        }
    }

    public static MultiMap<String,String> buildMetadata(KeyValuePairParser kvpp) {
        MultiMap<String,String> metadata=new MultiMap<String, String>();

        while(kvpp.hasNext()) {
            kvpp.parseNext();

            if(kvpp.getKey().equals(INDEX_KEY)) {
                String v = kvpp.getValue();
                int idx = v.indexOf(':');
                if(idx!=-1) {
                    // v=contract:name
                    String contract = v.substring(0, idx);
                    String name = v.substring(idx + 1);
                    metadata.add(contract,name);
                }
            }
            metadata.add(kvpp.getKey(),kvpp.getValue());
        }

        return metadata;
    }
}
