/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.admin.servermgmt.services;

import com.sun.enterprise.universal.PropertiesDecoder;
import java.util.*;
import java.util.Map;

/**
 *
 * @author bnevins
 */
public abstract class ServiceAdapter implements Service{
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AppserverServiceType getType() {
        return type;
    }

    public void setType(AppserverServiceType type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getFQSN() {
        throw new UnsupportedOperationException("getFQSN not supported for this Platform Service");
    }

    public void setFQSN() {
        // NOOP
    }

    public String getAsadminPath() {
        return asadminPath;
    }

    public void setAsadminPath(String path) {
        asadminPath = path;
    }

    public String getPasswordFilePath() {
        return passwordFile;
    }

    public void setPasswordFilePath(String path) {
        passwordFile = path;
    }

    public int getTimeoutSeconds() {
        throw new UnsupportedOperationException("getTimeoutSeconds() is not supported on this platform");
    }

    public void setTimeoutSeconds(int number) {
        throw new UnsupportedOperationException("setTimeoutSeconds() is not supported on this platform");
    }

    public String getOSUser() {
        return user;
    }

    public void setOSUser() {
        // it has been done already...
    }

    public String getServiceProperties() {
        return flattenedServicePropertes;
    }

    public void setServiceProperties(String cds) {
        flattenedServicePropertes = cds;
    }

    public Map<String, String> tokensAndValues() {
        return PropertiesDecoder.unflatten(flattenedServicePropertes);
    }

    public String getManifestFilePath() {
        throw new UnsupportedOperationException("getManifestFilePath() is not supported in this platform.");
    }

    public String getManifestFileTemplatePath() {
        throw new UnsupportedOperationException("getManifestFileTemplatePath() is not supported in this platform.");
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    private static boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    private final String    user = System.getProperty("user.name");

    private String          date = new Date().toString();    // default date string
    private String          location;
    private String          name;
    private String          asadminPath;
    private String          passwordFile;
    private String          flattenedServicePropertes;
    private boolean         trace;
    private boolean         dryRun;

    private AppserverServiceType type = AppserverServiceType.Domain;
}
