function fixedExpandCollapse(treeNode, imageId, event) {
    var tree = this.getTree(treeNode);
    var childNodes = document.getElementById(treeNode.id+"_children");
    if (childNodes) {
        // Get the event source
        if (!event) {
            event = window.event;
        }

        var elt = document.getElementById(imageId);
        if (elt.id.indexOf("_image") < 0) {
            var baseId = elt.id.substring(0, elt.id.lastIndexOf(":") + 1);
            elt = document.getElementById(baseId + "turner:turner_image");
        }

        this.unhighlightParent(this.getSelectedTreeNode(tree.id));

        // Change the style to cause the expand / collapse & switch the image
        var display = childNodes.style.display;
        if (display == "none") {
            childNodes.style.display = "block";
            if (elt && elt.src) {
                elt.src = elt.src.replace("tree_handleright", "tree_handledown");
            }
        } else {
            childNodes.style.display = "none";
            if (elt && elt.src) {
                elt.src = elt.src.replace("tree_handledown", "tree_handleright");
            }
        }

        // Last, update the visible parent of the selected node if now hidden
        this.highlightParent(this.getSelectedTreeNode(tree.id));
    }
    return true;
}

/**
 *    This method was butchered... it now does nothing like what the name
 *  implies, it returns tree/false, <b>NOT</b> a node.  It doesn't find
 *  any nodes by property (although it does still serach by type.
 */
function fixedFindNodeByTypeAndProp(node) {
    if (node == null) {
        return true;
    }

    // First check to see if node is a handler image.
    // Then check if it is of the right type. "RIGHT" icon
    // type indicates the node is not expanded.
    if (node.nodeName == "IMG") {
        var propName = 'src';
        var propVal = 'tree_handle';
        if (node[propName].indexOf(propVal) > -1) {
            //return node;
            // Do check:
            return (node[propName].indexOf("tree_handleright") == -1);
        }
    }

    // Not what we want, walk its children if any
    // Return true for when null conditions arise.
    var nodeList = node.childNodes;
    if (!nodeList || (nodeList.length == 0)) {
        return true;
    }
    var result;
    for (var count = 0; count<nodeList.length; count++) {
        // Recurse
        result = this.findNodeByTypeAndProp(nodeList[count]);
        if (result) {
            // Propagate the result
            return result;
        }
    }

    // Not found
    return true;
}

function fixedIsTreeHandle(event) {
    if (!event) {
        event = window.event;
        if (!event) {
            return false;
        }
    }
    var elt = (event.target) ? event.target : event.srcElement;

    if (elt.nodeName == "IMG") {
        var url = new String(elt.src);
        if ((url.indexOf("tree_handle") > 0) && (url.indexOf("theme") > 0)) {
            // This is a tree handle
            return true;
        }
    } else if (elt.nodeName == "A") {
        // User might have been pressing enter around an image.
        // Note: I have never managed to get control to come here.
        var aID = elt.id;
        var lastIndex = aID.lastIndexOf("_handle");
        if (lastIndex == -1) {
            return false;
        }
        var result = aID.substring(lastIndex, aID.length - 1);
        if (result == "_handle") {
            return true;
        }
    }

    // Not a tree handle
    return false;
}