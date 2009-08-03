package org.glassfish.jta.admin.cli;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;

@Service(name = "rollback-transaction")
@Scoped(PerLookup.class)
@I18n("rollback.transaction")
public class RollbackTransaction implements AdminCommand {
    private static StringManager localStrings =
            StringManager.getManager(RollbackTransaction.class);

    @Param(optional = true)
    String target = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;

    @Param(name = "transaction_id")
    String txnId;

    @Inject
    JavaEETransactionManager transactionMgr;

    /**
     * Executes the command
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        try {
            transactionMgr.forceRollback(txnId);
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (Exception e) {
            report.setMessage(localStrings.getString("rollback.transaction.failed"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }
}
