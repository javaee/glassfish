package test3;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Element;

import java.util.List;

public interface Role {

    @Attribute
    String getName();

    void setName(String name);

    @Element
    List<String> getUsers();
}
