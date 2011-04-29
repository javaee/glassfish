package org.glassfish.virtualization.libvirt;

import org.glassfish.virtualization.spi.VirtException;
import org.jvnet.hk2.annotations.Service;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: 3/14/11
 * Time: 3:46 PM
 * To change this template use File | Settings | File Templates.
 */
@Service(name="cdrom")
public class CDRomDisk implements DiskReference {

   @Override
    public Node save(String path, Node parent, int position) throws VirtException {
        char diskId='c';
        for (int i=0;i<position;diskId++,i++) {
            // do nothing
        }

        Element diskNode = parent.getOwnerDocument().createElement("disk");

        diskNode.setAttribute("type", "file");
        diskNode.setAttribute("device","cdrom");
        Element driverNode = parent.getOwnerDocument().createElement("driver");
        driverNode.setAttribute("name", "qemu");
        driverNode.setAttribute("type", "raw");
        diskNode.appendChild(driverNode);
        Element sourceNode = parent.getOwnerDocument().createElement("source");
        sourceNode.setAttribute("file", path);
        diskNode.appendChild(sourceNode);
        Element targetNode = parent.getOwnerDocument().createElement("target");
        targetNode.setAttribute("dev", "hd"+diskId);
        targetNode.setAttribute("bus", "ide");
        diskNode.appendChild(targetNode);
        Element readOnlyNode = parent.getOwnerDocument().createElement("readonly");
       diskNode.appendChild(readOnlyNode);
       Element aliasNode = parent.getOwnerDocument().createElement("alias");
       aliasNode.setAttribute("name", "ide0-" + (position+1) + "-0");
       diskNode.appendChild(aliasNode);
        Element addressNode = parent.getOwnerDocument().createElement("address");
        addressNode.setAttribute("type", "drive");
        addressNode.setAttribute("controller", "0");
        addressNode.setAttribute("bus", "" + (position+1));
        addressNode.setAttribute("unit", "0");
        diskNode.appendChild(addressNode);

        return diskNode;

    }
}
