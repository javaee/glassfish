/*
 * Copyright © 2003 Sun Microsystems, Inc.  All rights reserved.  U.S.
 * Government Rights - Commercial software.  Government users are subject
 * to the Sun Microsystems, Inc. standard license agreement and
 * applicable provisions of the FAR and its supplements.  Use is subject
 * to license terms.
 *
 * This distribution may include materials developed by third parties.
 * Sun, Sun Microsystems, the Sun logo, Java and J2EE are trademarks
 * or registered trademarks of Sun Microsystems, Inc. in the U.S. and
 * other countries.
 *
 * Copyright © 2003 Sun Microsystems, Inc. Tous droits réservés.
 *
 * Droits du gouvernement américain, utilisateurs gouvernementaux - logiciel
 * commercial. Les utilisateurs gouvernementaux sont soumis au contrat de
 * licence standard de Sun Microsystems, Inc., ainsi qu'aux dispositions
 * en vigueur de la FAR (Federal Acquisition Regulations) et des
 * suppléments à celles-ci.  Distribué par des licences qui en
 * restreignent l'utilisation.
 *
 * Cette distribution peut comprendre des composants développés par des
 * tierces parties. Sun, Sun Microsystems, le logo Sun, Java et J2EE
 * sont des marques de fabrique ou des marques déposées de Sun
 * Microsystems, Inc. aux Etats-Unis et dans d'autres pays.
 */


package request;

import java.io.Serializable;

public class VendorRequest implements Serializable {

    public int vendorId;
    public String name;
    public String address;
    public String contact;
    public String phone;

    public VendorRequest(int vendorId, String name, String address,
            String contact, String phone) {

        this.vendorId = vendorId;
        this.name = name;
        this.address = address;
        this.contact = contact;
        this.phone = phone;
    }
}
