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

/*
 *  SelectableJBIServiceAssemblyInfo.java
 */

package com.sun.jbi.jsf.bean;
import com.sun.jbi.jsf.util.JBIConstants;
import com.sun.jbi.jsf.util.JBILogger;
import com.sun.jbi.jsf.util.I18nUtilities;
import com.sun.jbi.jsf.util.SharedConstants;
import com.sun.jbi.ui.common.ServiceAssemblyInfo;
import java.util.logging.Logger;


public class SelectableJBIServiceAssemblyInfo
    extends ServiceAssemblyInfo
{
   
	//Get Logger to log fine mesages for debugging
	private static Logger sLog = JBILogger.getInstance();

    private final static String SERVICE_ASSEMBLY_TYPE = JBIConstants.JBI_SERVICE_ASSEMBLY_TYPE;;
    
    public SelectableJBIServiceAssemblyInfo(ServiceAssemblyInfo aSourceInfo)
    {
	super(aSourceInfo.getName(),aSourceInfo.getDescription(),aSourceInfo.getState());
    }
    
    public String getEnabled()
    {
	String state = super.getState();
	sLog.fine("SelectableJBIComponentInfo.getEnabled(), super.getState()=" + state); 
	if (SharedConstants.STATE_STARTED.equals(state))
	    {
		mEnabled = I18nUtilities.getResourceString ("jbi.operations.comp.started");
	    }
	else if (SharedConstants.STATE_STOPPED.equals(state))
	    {
		mEnabled = I18nUtilities.getResourceString ("jbi.operations.comp.stopped");
	    }
	else if (SharedConstants.STATE_SHUT_DOWN.equals(state))
	    {
		mEnabled = I18nUtilities.getResourceString ("jbi.operations.comp.shutdown");
	    }
	else
	    {
		mEnabled = "Unknown (" + state + ")"; 
	    }

	sLog.fine("SelectableJBIServiceAssemblyInfo.getEnabled(), mEnabled=" + mEnabled); 
        return mEnabled;
    }


    public boolean getSelected()
    {
        return mSelected;
    }

    public String getStatus()
    {
        return mStatus;
    }

    public String getSummaryStatus()
    {
		sLog.fine("SelectableJBIServiceAssemblyInfo.getSummaryStatus(), mSummaryStatus=" + mSummaryStatus); 
        return mSummaryStatus;
    }

    /** Sets enabled property. */
    public void setEnabled (String anEnabledState)
    {
        mEnabled = anEnabledState;
    }

    /** Sets selected property. */
    public void setSelected (boolean aSelection)
    {
        mSelected = aSelection;
    }

    /** Sets status property. */
    public void setStatus (String aStatus)
    {
        mStatus = aStatus;
    }

    /** Sets summary status property. */
    public void setSummaryStatus (String aSummaryStatus)
    {
        mSummaryStatus = aSummaryStatus;
    }

    public String getType()
    {
	return SERVICE_ASSEMBLY_TYPE;
    }
    
    private String mEnabled;
    private boolean mSelected = false;
    private String mStatus;
    private String mSummaryStatus;
}

