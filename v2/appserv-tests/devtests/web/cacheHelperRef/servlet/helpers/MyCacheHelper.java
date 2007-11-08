/*
 * Test Custom Servlet Caching
 * Author Davis Nguyen
 */

package helpers;

import com.sun.appserv.web.cache.*;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class MyCacheHelper implements CacheHelper {

    // Values is initialized  to CacheAble
    private static String cacheKey="myKey"; 
    private static boolean isCacheAble=true;
    private static boolean isRefreshNeeded=false;
    private static int timeOut=20;
  
    
    public void init (ServletContext context, Map props) throws Exception {
        System.out.println("myCacheHelper2:init");
        // Nothing to initialize
    }
    
    public void destroy() throws Exception {
        System.out.println("myCacheHelper2:exit");
    }
    
    public String getCacheKey(HttpServletRequest req) {
        System.out.println("myCacheHelper2:getCacheKey");
        String key = req.getParameter("cacheKey");
        if (cacheKey != null) // key not null
            return key;
        else
            // If user do not enter a key, the default value is "myKey"
        return cacheKey;
    }
    
    public int getTimeout(HttpServletRequest req) {
        String timeOutStr = null;
        int time = 0;
        System.out.println("myCacheHelper2:getTimeout");
        timeOutStr = req.getParameter("timeOut");
        if (timeOutStr != null){
            try {
                time = Integer.parseInt(timeOutStr);
            } catch (NumberFormatException nfe) {
                System.out.println("Number Format Exception Occurs: "
                                   + nfe.getMessage());
            }
            // Negative time out value treat as 0 
            if (time > 0)
                return time;
            else return 0;
        }
        // if user do not enter time out, use default timeOut value
        else return timeOut;
    }
    
    public boolean isCacheable(HttpServletRequest req) {
        String isCacheAbleStr = null;
        System.out.println("myCacheHelper2:isCacheable");
        isCacheAbleStr = (String)req.getParameter("isCacheAble");
        // Check if isCacheAble null 
        if ((isCacheAbleStr != null)
                && (isCacheAbleStr.compareTo("false") != 0))
            return false;
        else return isCacheAble;
    }
    
    public boolean isRefreshNeeded(HttpServletRequest req) {
        String isRefreshNeededStr = null;
        System.out.println("myCacheHelper2:isRefreshNeeded");
        isRefreshNeededStr = req.getParameter("isRefreshNeeded");
        if ((isRefreshNeededStr != null)
                && (isRefreshNeededStr.compareTo("true") !=0))
            return true;
        else return isRefreshNeeded;
    }   
}

