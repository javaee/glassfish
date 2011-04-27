package org.glassfish.vmcluster.libvirt;

import org.glassfish.vmcluster.spi.VirtException;
import org.jvnet.hk2.annotations.Contract;
import org.w3c.dom.Node;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: 3/1/11
 * Time: 8:50 PM
 * To change this template use File | Settings | File Templates.
 */
@Contract
public interface DiskReference {

    Node save(String path, Node parent, int position) throws VirtException;
}
