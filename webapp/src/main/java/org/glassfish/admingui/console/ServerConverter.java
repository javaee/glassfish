/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.console;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author jdlee
 */
@FacesConverter("org.glassfish.admingui.server.converter")
public class ServerConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String string) {
        while (string.startsWith(",")) {
            string = string.substring(1);
        }

        while (string.endsWith(",")) {
            string = string.substring(0, string.length()-1);
        }
        return string.split(",");
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        String result = null;
        if (o instanceof String[]) {
            String sep = "";
            result = "";
            for (String item : (String[])o) {
                result += sep + item;
                sep = ",";
            }
        }
        return result;
    }

}
