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

package com.sun.enterprise.config.serverbeans.validation;

import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * FrameHolderBuilder.java
 * Instances of the class are responsible for handling a SAX input
 * stream to build a series of connected Frames to hold the
 * system-property definitions found inside the XML. 
 *
 * The Frames are connected by an inheritance pattern - it is this
 * class that knows what that inheritance pattern is. As of the time
 * of writing the pattern is:
 * <verbatim>
 * server [-> cluster] -> config -> domain
 * </verbatim>
 * With the cluster inheritance being optional, and only occuring when
 * a cluster refers to a server. (So, a server might look as if its
 * inheriting from one config, but a cluster can override the
 * server's own settings and cause the server to inherit from the
 * cluster and thus from some other config). 
 *
 * The inheritance hierarchy between configs and domains is
 * implicit. Between the other elements it is explicit, and is
 * achieved through the XML attributes "config-ref", "server-ref".
 *
 * This system will function even if the XML is quite poorly formed,
 * but the results will be peculiar, to say the least! The best
 * debugging method is to extract each frame from the resulting
 * FrameHolder and print it out - that gives you a pretty good picture
 * of what the inheritance tree looks like (at least in terms of
 * properties!).
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.3 $
 */

// Upon revisiting this code I can't help but believe that this class
// should be called something slightly different. It appears to be an
// inheritance manager, and tightly tied into the Framer concept. I'm
// unsure why I built it this way!
public class FrameHolderBuilder extends Framer
{
    protected final Frame getClusterFrame(Attributes atts){
        final Frame f = super.getClusterFrame(atts);
        inheritFromConfigPerhaps(f, atts);
//         final String sr = atts.getValue(NAMESPACE, SERVER_REF);
//         if (sr != null){
//             frameHolder.getServerFrame(sr).inheritFrom(f);
//         }
        return f;
    }

    protected final void handleStartServerRefEvent(Attributes atts){
        final String sr = atts.getValue(NAMESPACE, NAME);
        if (sr != null){
            frameHolder.getServerFrame(sr).inheritFrom(currentFrame());
        }
    }
    
    protected final Frame getConfigFrame(Attributes atts){
        final Frame f = super.getConfigFrame(atts);
        f.inheritFrom(frameHolder.getDomainFrame());
        return f;
    }
    
    protected final Frame getServerFrame(Attributes atts){
        final Frame f = super.getServerFrame(atts);
        return inheritFromConfigPerhaps(f, atts);
    }

    private final Frame inheritFromConfigPerhaps(Frame f, Attributes atts){
        final String cr = atts.getValue(NAMESPACE, CONFIG_REF);
        if (cr != null){
            f.inheritFrom(frameHolder.getConfigFrame(cr));
        }
        return f;
    }

}



