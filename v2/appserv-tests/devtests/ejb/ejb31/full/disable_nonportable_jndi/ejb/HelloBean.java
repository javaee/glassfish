package com.acme;

import javax.ejb.*;
import javax.annotation.*;

@Stateless(mappedName="HH")
public class HelloBean implements Hello {
    public String hello() {
	return "hello, world\n";
    }

}
