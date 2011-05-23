/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admin.rest.generator;

class CollectionLeafMetaData {
    String postCommandName;
    String deleteCommandName;
    String displayName;

    CollectionLeafMetaData(String postCommandName, String deleteCommandName, String displayName) {
        this.postCommandName = postCommandName;
        this.deleteCommandName = deleteCommandName;
        this.displayName = displayName;
    }

}
