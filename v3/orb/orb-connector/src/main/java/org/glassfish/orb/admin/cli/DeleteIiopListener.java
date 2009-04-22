/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.orb.admin.cli;

import java.beans.PropertyVetoException;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.IiopListener;
import com.sun.enterprise.config.serverbeans.IiopService;

import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.universal.glassfish.SystemPropertyConstants;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.util.List;


@Service(name="delete-iiop-listener")
@Scoped(PerLookup.class)
@I18n("delete.iiop.listener")
public class DeleteIiopListener implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(DeleteIiopListener.class);

    @Param(name="listener_id", primary=true)
    String listener_id;

    @Param(optional=true)
    String target = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;

    @Inject
    Configs configs;

    @Inject
    Servers servers;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        List<Config> configList = configs.getConfig();
        Config config = configList.get(0);
        IiopService iiopService = config.getIiopService();


        if(!isIIOPListenerExists(iiopService)) {
            report.setMessage(localStrings.getLocalString("delete.iiop.listener" +
                    ".notexists", "IIOP Listener {0} does not exist.", listener_id));
            report.setActionExitCode(ExitCode.FAILURE);
            return;
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<IiopService>() {
                public Object run(IiopService param) throws PropertyVetoException,
                        TransactionFailure {
                    List<IiopListener> listenerList = param.getIiopListener();
                    for (IiopListener listener : listenerList) {
                        String currListenerId = listener.getId();
                        if (currListenerId != null && currListenerId.equals
                                (listener_id)) {
                            listenerList.remove(listener);
                            break;
                        }
                    }
                    return listenerList;
                }
            }, iiopService);
            report.setMessage(localStrings.getLocalString(
                    "delete.iiop.listener.success",
                    "IIOP Listener {0} deleted", listener_id));
            report.setActionExitCode(ExitCode.SUCCESS);
        } catch(TransactionFailure e) {
            String actual = e.getMessage();
            report.setMessage(localStrings.getLocalString("delete.iiop.listener" +
                    ".fail", "failed", listener_id, actual));
            report.setActionExitCode(ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }

    private boolean isIIOPListenerExists(IiopService iiopService) {

        for (IiopListener listener : iiopService.getIiopListener()) {
            String currListenerId = listener.getId();
            if (currListenerId != null && currListenerId.equals(listener_id)) {
                return true;
            }
        }
        return false;
    }

}
