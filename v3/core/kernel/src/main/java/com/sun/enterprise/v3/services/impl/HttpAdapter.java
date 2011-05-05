/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.v3.services.impl;

import com.sun.enterprise.config.serverbeans.VirtualServer;

/**
 *
 * @author oleksiys
 */
public interface HttpAdapter {
    public ContainerMapper getMapper();
    public VirtualServer getVirtualServer();
    public String getWebAppRootPath();
}
