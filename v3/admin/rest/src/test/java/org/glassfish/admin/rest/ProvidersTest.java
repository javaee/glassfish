/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admin.rest;

import com.sun.jersey.api.client.ClientResponse;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jasonlee
 */
public class ProvidersTest extends RestTestBase {
    protected static final String URL_UPTIME = BASE_URL + "/uptime";
    protected static final String URL_STOP_DOMAIN = BASE_URL + "/stop";
    protected static final String URL_SERVER_LIST = BASE_URL + "/servers/server";

    @Test
    public void testActionReportResultHtmlProvider() {
        ClientResponse response = get(URL_UPTIME + ".html");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testActionReportResultXmlProvider() {
        ClientResponse response = get(URL_UPTIME + ".xml");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testActionReportResultJsonProvider() {
        ClientResponse response = get(URL_UPTIME + ".json");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testCommandResourceGetResultHtmlProvider() {
        ClientResponse response = get(URL_STOP_DOMAIN + ".html");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testCommandResourceGetResultXmlProvider() {
        ClientResponse response = get(URL_STOP_DOMAIN + ".xml");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testCommandResourceGetResultJsonProvider() {
        ClientResponse response = get(URL_STOP_DOMAIN + ".json");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testGetResultHtmlProvider() {
        ClientResponse response = get(BASE_URL + ".html");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testGetResultXmlProvider() {
        ClientResponse response = get(BASE_URL + ".xml");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testGetResultJsonProvider() {
        ClientResponse response = get(BASE_URL + ".json");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testGetResultListHtmlProvider() {
        ClientResponse response = get(URL_SERVER_LIST + ".html");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testGetResultListXmlProvider() {
        ClientResponse response = get(URL_SERVER_LIST + ".xml");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testGetResultListJsonProvider() {
        ClientResponse response = get(URL_SERVER_LIST + ".json");
        assertTrue(isSuccess(response));
    }
}