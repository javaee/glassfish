/*
 * UserBean.java
 *
 * Created on December 4, 2007, 1:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package web.jsf.bean;

/**
 *
 * @author Deepa Singh
 */
public class UserBean {
    
    private String name;
    private String birthday;
    
    /** Creates a new instance of UserBean */
    public UserBean() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
    
}
