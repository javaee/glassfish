/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.console.rest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.Map;
import java.util.HashMap;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collection;

import org.glassfish.admingui.console.util.GuiUtil;
import org.glassfish.admingui.console.util.Constants;


//import static org.glassfish.api.ActionReport.ExitCode;

public class JerseyClient {
    public static final String FORM_ENCODING = "application/x-www-form-urlencoded";
    //default to .json instead of .xml
    public static final String RESPONSE_TYPE = "application/json";
    public static final String GUI_TOKEN_FOR_EMPTY_PROPERTY_VALUE = "()";
    public static final Client JERSEY_CLIENT = Client.create();

    private static final String REST_TOKEN_COOKIE = "gfresttoken";

    public static RestResponse get(String address) {
        return get(address, new HashMap<String, Object>());
    }

    public static RestResponse get(String address, Map<String, Object> payload) {
        if (address.startsWith("/")) {
            address = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("REST_URL") + address;
        }
        WebResource webResource = JERSEY_CLIENT.resource(address).queryParams(buildMultivalueMap(payload));
        ClientResponse resp = webResource
                .cookie(new Cookie(REST_TOKEN_COOKIE, getRestToken()))
                .accept(RESPONSE_TYPE).get(ClientResponse.class);
        return RestResponse.getRestResponse(resp);
    }

    public static RestResponse post(String address, Object payload, String contentType) {
        WebResource webResource = JERSEY_CLIENT.resource(address);
        if (contentType == null) {
            contentType = MediaType.APPLICATION_JSON;
        }
        if (payload instanceof Map) {
            payload = buildMultivalueMap((Map<String, Object>)payload);
        }
        ClientResponse cr = webResource.header("Content-Type", contentType)
                .cookie(new Cookie(REST_TOKEN_COOKIE, getRestToken()))
//                .header("Content-type", MediaType.APPLICATION_FORM_URLENCODED)
                .accept(RESPONSE_TYPE).post(ClientResponse.class, payload);
        RestResponse rr = RestResponse.getRestResponse(cr);
        return rr;
    }

    public static RestResponse post(String address, Map<String, Object> payload) {
        WebResource webResource = JERSEY_CLIENT.resource(address);
        MultivaluedMap formData = buildMultivalueMap(payload);
        ClientResponse cr = webResource
                .cookie(new Cookie(REST_TOKEN_COOKIE, getRestToken()))
//                .header("Content-type", MediaType.APPLICATION_FORM_URLENCODED)
                .accept(RESPONSE_TYPE).post(ClientResponse.class, formData);
        RestResponse rr = RestResponse.getRestResponse(cr);
        return rr;
    }

    public static RestResponse put(String address, Map<String, Object> payload) {
        WebResource webResource = JERSEY_CLIENT.resource(address);
        MultivaluedMap formData = buildMultivalueMap(payload);
        ClientResponse cr = webResource
                .cookie(new Cookie(REST_TOKEN_COOKIE, getRestToken()))
//                .header("Content-type", MediaType.APPLICATION_FORM_URLENCODED)
                .accept(RESPONSE_TYPE).put(ClientResponse.class, formData);
        RestResponse rr = RestResponse.getRestResponse(cr);
        return rr;
    }

    public static RestResponse delete(String address, Map<String, Object> payload) {
        WebResource webResource = JERSEY_CLIENT.resource(address);
        ClientResponse cr = webResource.queryParams(buildMultivalueMap(payload))
                .cookie(new Cookie(REST_TOKEN_COOKIE, getRestToken()))
                .accept(RESPONSE_TYPE).delete(ClientResponse.class);
        checkStatusForSuccess(cr);
        return RestResponse.getRestResponse(cr);
    }

    public static RestResponse options(String address, String responseType) {
        WebResource webResource = JERSEY_CLIENT.resource(address);
        ClientResponse cr = webResource
                .cookie(new Cookie(REST_TOKEN_COOKIE, getRestToken()))
                .accept(responseType).options(ClientResponse.class);
        checkStatusForSuccess(cr);
        return RestResponse.getRestResponse(cr);
    }

    public static void checkStatusForSuccess(ClientResponse cr) {
        int status = cr.getStatus();
        if ((status < 200) || (status > 299)) {
            throw new RuntimeException(cr.toString());
        }
    }
    //******************************************************************************************************************
    // Jersey client methods
    //******************************************************************************************************************
     public static void initialize(Client client){
        if (client == null){
            client = JERSEY_CLIENT;
        }
        /*
        try{
            Habitat habitat = SecurityServicesUtil.getInstance().getHabitat();
            SecureAdmin secureAdmin = habitat.getComponent(SecureAdmin.class);
            HTTPSProperties httpsProperties = new HTTPSProperties(new BasicHostnameVerifier(),
                habitat.getComponent(SSLUtils.class).getAdminSSLContext(SecureAdmin.Util.DASAlias(secureAdmin), null ));
            client.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, httpsProperties);
        }catch(Exception ex){
            GuiUtil.getLogger().warning("RestUtil.initialize() failed");
            if (GuiUtil.getLogger().isLoggable(Level.FINE)){
                ex.printStackTrace();
            }
        }
         *
         */
    }
    /**
     *        <p> This method returns the value of the REST token if it is
     *            successfully set in session scope.</p>
     */
    private static String getRestToken() {
        String token = null;
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx != null) {
            token = (String) ctx.getExternalContext().getSessionMap().
                    get(Constants.REST_TOKEN);
        }
        return token;
    }

        protected static MultivaluedMap buildMultivalueMap(Map<String, Object> payload) {
        MultivaluedMap formData = new MultivaluedMapImpl();
        for (final Map.Entry<String, Object> entry : payload.entrySet()) {
            final Object value = entry.getValue();
            final String key = entry.getKey();
            if (value instanceof Collection) {
                for (Object obj : ((Collection) value)) {
                    try {
                        formData.add(key, obj);
                    } catch (ClassCastException ex) {
                        Logger logger = GuiUtil.getLogger();
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.log(Level.FINEST,
                                    GuiUtil.getCommonMessage("LOG_BUILD_MULTI_VALUE_MAP_ERROR", new Object[]{key, obj}));
                        }

                        // Allow it to continue b/c this property most likely
                        // should have been excluded for this request
                    }
                }
            } else {
                //formData.putSingle(key, (value != null) ? value.toString() : value);
                try {
                    formData.putSingle(key, value);
                } catch (ClassCastException ex) {
                    Logger logger = GuiUtil.getLogger();
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.log(Level.FINEST,
                                GuiUtil.getCommonMessage("LOG_BUILD_MULTI_VALUE_MAP_ERROR" , new Object[]{key, value}));
                    }
                    // Allow it to continue b/c this property most likely
                    // should have been excluded for this request
                }
            }
        }
        return formData;
    }

}
