package net.munnangi.addon;

import org.jvnet.hk2.annotations.CompanionOf;
import com.sun.enterprise.config.serverbeans.JdbcResource;
import org.jvnet.hk2.annotations.Lead;
import org.glassfish.admin.runtime.annotations.*;

@MBean
@CompanionOf(JdbcResource.class)
public class AddonCompanion implements Runtime {

    @Lead
    JdbcResource jdbcres;

    /*
    public String getName() {
        System.out.println("AddonCompanion: getName: M S Reddy ...");
        System.out.println("AddonCompanion: jdbcres = " + jdbcres);
        return null;
    }
    */

    public Object getParent() {return null;}

    // POJO - START

        public String str = "M S Reddy";

        @ManagedOperation
        public void sayMSRHello() {
            System.out.println("Hello, world");
        }
        
        @ManagedAttribute
        public int getMSRX() {
            return x;
        }
        
        @ManagedAttribute
        public void setMSRX(int x) {
            this.x = x;
        }
        
        @ManagedAttribute
        public String getName() {
            return "my name is AddonCompanionMbean";
        }
        
        private int x;

    // POJO - END
}
