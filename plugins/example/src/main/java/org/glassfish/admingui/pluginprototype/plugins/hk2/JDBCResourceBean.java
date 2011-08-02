/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.pluginprototype.plugins.hk2;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

/**
 *
 * @author sumasri
 */
@ManagedBean(eager = true, name = "jdbcResource")
@SessionScoped
public class JDBCResourceBean implements ValueChangeListener {

    private String jndiName = "JDBC Resource1";
    private String description = "JDBC Resource1 description";
    private List<Prop> props;
    private List<String> resList = new ArrayList<String>();
    private Map<String, Map> jdbcResInfo = new HashMap<String, Map>();

    public JDBCResourceBean() {
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> jdbcResInfoMap = new HashMap<String, Object>();
            jdbcResInfoMap.put("description", "JDBC Resource" + i + " description");
            jdbcResInfo.put("JDBC Resource" + i, jdbcResInfoMap);

            List<Prop> properties = new ArrayList<Prop>();
            properties.add(new Prop("Key0", "val0", "desc0", false));
            properties.add(new Prop("Key1", "val1", "desc1", false));
            jdbcResInfoMap.put("props", properties);
            resList.add("JDBC Resource" + i);
            if (i == 1) {
                this.props = properties;
            }
        }
    }

    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String res) {
        jndiName = res;
        description = (String) jdbcResInfo.get(res).get("description");
    }

    public String getDescription() {
        return (String) jdbcResInfo.get(jndiName).get("description");
    }

    public void setDescription(String desc) {
        description = desc;
        jdbcResInfo.get(jndiName).put("description", desc);
    }

    public List<String> getResList() {
        return resList;
    }

    public void setProps(List<Prop> properties) {
        props = properties;
        jdbcResInfo.get(jndiName).put("props", properties);
    }

    public List<Prop> getProps() {
        return props;
    }

    public Prop addDummyProp() {
        Prop p = new Prop();
        p.addNewProp();
        props.add(p);
        return null;
    }

    public void delProp() {
        for (Prop prop : props) {
            if (prop.selected == true) {
                props.remove(prop);
            }
        }
    }

    @Override
    public void processValueChange(ValueChangeEvent event) throws AbortProcessingException {
        setJndiName(event.getNewValue().toString());
        FacesContext.getCurrentInstance().renderResponse();

    }

    public static class Prop {

        String name;
        String value;
        String description;
        Boolean selected;

        public Prop() {
        }

        public Prop(String name, String value, String description, Boolean selected) {
            this.name = name;
            this.value = value;
            this.description = description;
            this.selected = selected;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String val) {
            this.value = val;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String desc) {
            this.description = desc;
        }

        public Boolean getSelected() {
            return selected;
        }

        public void setSelected(Boolean sel) {
            this.selected = sel;
        }

        public void addNewProp() {
            name = null;
            value = null;
            description = null;
            selected = false;
        }
    }
}
