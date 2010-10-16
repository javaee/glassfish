package com.sun.hk2.component;

import org.jvnet.hk2.component.MultiMap;

import static com.sun.hk2.component.InhabitantsFile.CLASS_KEY;
import static com.sun.hk2.component.InhabitantsFile.INDEX_KEY;

/**
 * {@link com.sun.hk2.component.InhabitantParser} implementation based on the inhabitant
 * file format.
 *
 * Format of the inhabitant is subject to changes and compatibility across releases cannot
 * be guaranteed.
 *
 * file : line+
 * line : class=class-value, (key=value ,?)+
 * key : index | targetType | any
 * class-value : {@link Class#getName()} name of the service implementation
 * index : index-name[:name](,index)*
 * index-name : {@link Class#getName()} class name of the contract
 * name: string identifying the service name
 * targeType : class-type (, method-name)?
 * class-type : class name where {@link org.jvnet.hk2.annotations.InhabitantAnnotation} was declared
 * method-name : method name if the {@link org.jvnet.hk2.annotations.InhabitantAnnotation} was placed on a method
 * any : some-key=some-value
 * some-key : [a-z]+
 * some-value : [a-z]+
 *
 * "Any" above contributes to the metadata portion of the inhabitant.
 */
public class InhabitantFileBasedParser implements InhabitantParser {
    final MultiMap<String,String> metadata;
    final KeyValuePairParser parser;

    public InhabitantFileBasedParser(KeyValuePairParser parser) {
        this.parser = parser;
        metadata = buildMetadata(parser);
    }
    public Iterable<String> getIndexes() {
        return parser.findAll(INDEX_KEY);
    }

    public String getImplName() {
        return metadata.getOne(CLASS_KEY);
    }

    public String getLine() {
        return parser.getLine();
    }

    public void setImplName(String name) {
        metadata.set(CLASS_KEY,name);        
    }

    public void rewind() {
        parser.rewind();
    }

    public MultiMap<String, String> getMetaData() {
        return metadata;
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
