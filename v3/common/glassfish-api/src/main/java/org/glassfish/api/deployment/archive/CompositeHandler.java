package org.glassfish.api.deployment.archive;

import org.jvnet.hk2.annotations.Contract;

/**
 * Composite handlers are just like any other ArchiveHandler except they
 * get a chance at looking at the archive before the archive handlers do
 *
 * The main reason for this tag interface is that some archive handlers might
 * be tricked into thinking a composite archive is theirs when in fact they only
 * own a part of it.
 *
 * For instance, take a war file inside an ear file. and asssume that the war file
 * contains some .jsp files. The archive handler responsible for handling the war
 * file could be fooled into thinking the ear file is a war file since it contains
 * jsp files, yet in reality, it only owns one of the sub archive bundled inside
 * the composite ear file.
 *
 * @author Jerome Dochez 
 */
@Contract
public interface CompositeHandler extends ArchiveHandler {

    public boolean accept(ReadableArchive source, String entryName);
    
}
