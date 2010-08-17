package com.sun.s1asdev.custom.resource;


public class CustomResourceJavaBean implements java.io.Serializable {

    private String property;

    public CustomResourceJavaBean() {
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getProperty() {

        return property;
    }
    public String toString() {
        return property;
    }
}

