/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.ejb.ee.timer.lifecycle;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.enterprise.ee.cms.core.Action;
import com.sun.enterprise.ee.cms.core.GroupManagementService;
import com.sun.enterprise.ee.cms.core.MessageAction;
import com.sun.enterprise.ee.cms.core.MessageActionFactory;
import com.sun.enterprise.ee.cms.core.MessageSignal;
import com.sun.enterprise.ee.cms.core.Signal;

import com.sun.ejb.spi.distributed.DistributedReadOnlyBeanNotifier;
import com.sun.ejb.spi.distributed.DistributedEJBServiceFactory;
import com.sun.ejb.spi.distributed.DistributedReadOnlyBeanService;

import com.sun.enterprise.config.serverbeans.*;

import com.sun.logging.LogDomains;

class ReadOnlyBeanMessageActionFactoryImpl
    implements MessageActionFactory, DistributedReadOnlyBeanNotifier
{
    protected static final Logger _logger = LogDomains.getLogger(LogDomains.EJB_LOGGER);

    private static DistributedReadOnlyBeanService _readOnlyBeanService = DistributedEJBServiceFactory
            .getDistributedEJBService().getDistributedReadOnlyBeanService();

    private GroupManagementService gms;

    private String componentName;

    ReadOnlyBeanMessageActionFactoryImpl(GroupManagementService gms,
            String componentName) {
        this.gms = gms;
        this.componentName = componentName;
        _readOnlyBeanService.setDistributedReadOnlyBeanNotifier(this);
    }

    public Action produceAction() {
        return new MessageAction() {
            public void consumeSignal(Signal signal) {
                try {
                    signal.acquire();
                    MessageSignal messageSignal = (MessageSignal) signal;
                    byte[] payload = messageSignal.getMessage();
                    int size = payload.length;
                    long ejbID = bytesToLong(payload, 0);
                    if (size == 8) {
                        _logger.log(Level.WARNING, "ReadOnlyBeanMessageActionFactoryImpl: "
                                + " Got message for ejbID: " + ejbID);
                        _readOnlyBeanService.handleRefreshAllRequest(ejbID);
                    } else {
                        byte[] pkData = new byte[size - 8];
                        System.arraycopy(payload, 8, pkData, 0, pkData.length);
                        _readOnlyBeanService
                                .handleRefreshRequest(ejbID, pkData);
                        _logger.log(Level.WARNING, "ReadOnlyBeanMessageActionFactoryImpl: "
                                + " Handled message for ejbID: " + ejbID);
                    }
                } catch (Exception ex) {
                    _logger.log(Level.WARNING,
                            "ReadOnlyBeanMessageActionFactoryImpl: "
                                    + "Got exception while handling message",
                            ex);
                } finally {
                    try {
                        signal.release();
                    } catch (Exception ex) {
                        _logger.log(Level.WARNING,
                                "ReadOnlyBeanMessageActionFactoryImpl: "
                                + "Got exception while releasing signal", ex);
                    }
                }
            }
        };
    }

    /**
     * This is called by the container after it has called refresh
     * 
     * @param ejbID
     *            the ejbID that uniquely identifies the container
     * @param pk
     *            The primary key of the bean(s) that is to be refreshed
     */
    public void notifyRefresh(long ejbID, byte[] pk) {
        int size = pk.length;
        byte[] payload = new byte[size + 8];

        longToBytes(ejbID, payload, 0);
        System.arraycopy(pk, 0, payload, 8, size);
        try {
            gms.getGroupHandle().sendMessage(componentName, payload);
            _logger.log(Level.WARNING, "ReadOnlyBeanMessageActionFactoryImpl: "
                    + " Sent message for ejbID: " + ejbID);
        } catch (Exception ex) {
            _logger.log(Level.WARNING, "ReadOnlyBeanMessageActionFactoryImpl: "
                    + "Got exception during notifyRefresh", ex);
        }
    }

    /**
     * This is called by the container after it has called refresh
     * 
     * @param ejbID
     *            the ejbID that uniquely identifies the container
     * @param pk
     *            The primary key of the bean(s) that is to be refreshed
     */
    public void notifyRefreshAll(long ejbID) {
        byte[] payload = new byte[8];

        longToBytes(ejbID, payload, 0);
        try {
            gms.getGroupHandle().sendMessage(componentName, payload);
        } catch (Exception ex) {
            _logger.log(Level.WARNING, "ReadOnlyBeanMessageActionFactoryImpl: "
                    + "Got exception during notifyRefreshAll", ex);
        }
    }

    /**
     * Marshal an long to a byte array. The bytes are in BIGENDIAN order. i.e.
     * array[offset] is the most-significant-byte and array[offset+7] is the
     * least-significant-byte.
     * 
     * @param array
     *            The array of bytes.
     * @param offset
     *            The offset from which to start marshalling.
     */
    private static void longToBytes(long value, byte[] array, int offset) {
        array[offset] = (byte) ((value >>> 56) & 0xFF);
        array[offset + 1] = (byte) ((value >>> 48) & 0xFF);
        array[offset + 2] = (byte) ((value >>> 40) & 0xFF);
        array[offset + 3] = (byte) ((value >>> 32) & 0xFF);
        array[offset + 4] = (byte) ((value >>> 24) & 0xFF);
        array[offset + 5] = (byte) ((value >>> 16) & 0xFF);
        array[offset + 6] = (byte) ((value >>> 8) & 0xFF);
        array[offset + 7] = (byte) ((value >>> 0) & 0xFF);
    }

    /**
     * Unmarshal a byte array to an long. Assume the bytes are in BIGENDIAN
     * order. i.e. array[offset] is the most-significant-byte and
     * array[offset+7] is the least-significant-byte.
     * 
     * @param array
     *            The array of bytes.
     * @param offset
     *            The offset from which to start unmarshalling.
     */
    private static long bytesToLong(byte[] array, int offset) {
        long l1, l2;

        l1 = (long) bytesToInt(array, offset) << 32;
        l2 = (long) bytesToInt(array, offset + 4) & 0xFFFFFFFFL;

        return (l1 | l2);
    }

    /**
     * Unmarshal a byte array to an integer. Assume the bytes are in BIGENDIAN
     * order. i.e. array[offset] is the most-significant-byte and
     * array[offset+3] is the least-significant-byte.
     * 
     * @param array
     *            The array of bytes.
     * @param offset
     *            The offset from which to start unmarshalling.
     */
    private static int bytesToInt(byte[] array, int offset) {
        int b1, b2, b3, b4;

        b1 = (array[offset] << 24) & 0xFF000000;
        b2 = (array[offset + 1] << 16) & 0x00FF0000;
        b3 = (array[offset + 2] << 8) & 0x0000FF00;
        b4 = (array[offset + 3] << 0) & 0x000000FF;

        return (b1 | b2 | b3 | b4);
    }
}
