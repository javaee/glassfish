/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/NotificationListenerInfo.java,v 1.1 2005/11/08 22:40:24 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2005/11/08 22:40:24 $
 */
package com.sun.cli.jmxcmd.util.jmx;

import javax.management.NotificationListener;
import javax.management.NotificationFilter;


/**
    A immutable 3-tuple for tracking this stuff
 */
public class NotificationListenerInfo
{
    private final NotificationListener mListener;
    private final NotificationFilter   mFilter;
    private final Object               mHandback;
    
    public NotificationListenerInfo(
        NotificationListener listener,
        NotificationFilter   filter,
        Object               handback )
    {
        mListener   = listener;
        mFilter     = filter;
        mHandback   = handback;
    }
    
    public NotificationListener getListener()   { return mListener; }
    public NotificationFilter   getFilter()     { return mFilter; }
    public Object               getHandback()   { return mHandback; }

}
