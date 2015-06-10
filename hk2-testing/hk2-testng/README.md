[//]: # ( DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER. )
[//]: # (  )
[//]: # ( Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved. )
[//]: # (  )
[//]: # ( The contents of this file are subject to the terms of either the GNU )
[//]: # ( General Public License Version 2 only ("GPL") or the Common Development )
[//]: # ( and Distribution License("CDDL") (collectively, the "License").  You )
[//]: # ( may not use this file except in compliance with the License.  You can )
[//]: # ( obtain a copy of the License at )
[//]: # ( https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html )
[//]: # ( or packager/legal/LICENSE.txt.  See the License for the specific )
[//]: # ( language governing permissions and limitations under the License. )
[//]: # (  )
[//]: # ( When distributing the software, include this License Header Notice in each )
[//]: # ( file and include the License file at packager/legal/LICENSE.txt. )
[//]: # (  )
[//]: # ( GPL Classpath Exception: )
[//]: # ( Oracle designates this particular file as subject to the "Classpath" )
[//]: # ( exception as provided by Oracle in the GPL Version 2 section of the License )
[//]: # ( file that accompanied this code. )
[//]: # (   )
[//]: # ( Modifications: )
[//]: # ( If applicable, add the following below the License Header, with the fields )
[//]: # ( enclosed by brackets [] replaced by your own identifying information: )
[//]: # ( "Portions Copyright [year] [name of copyright owner]" )
[//]: # (  )
[//]: # ( Contributor(s): )
[//]: # ( If you wish your version of this file to be governed by only the CDDL or )
[//]: # ( only the GPL Version 2, indicate your decision by adding "[Contributor] )
[//]: # ( elects to include this software in this distribution under the [CDDL or GPL )
[//]: # ( Version 2] license."  If you don't indicate a single choice of license, a )
[//]: # ( recipient has the option to distribute your version of this file under )
[//]: # ( either the CDDL, the GPL Version 2 or to extend the choice of license to )
[//]: # ( its licensees as provided above.  However, if you add GPL Version 2 code )
[//]: # ( and therefore, elected the GPL Version 2 license, then the option applies )
[//]: # ( only if the new code is made subject to such option by the copyright )
[//]: # ( holder. )

# hk2-testng


This project adds HK2 Dependency Injection support to TestNG. It provides the ability to inject your test classes with HK2 services defined in inhibitent files and/or custom binders.

## Usage


### Service Discovery

By default the @HK2 annotation creates a new service locator with the name "hk2-testng-locator" and populates it with services defined in "META-INF/hk2-locator/default" classpath inhabitant files. Simply annotate your test class with @HK2 like this to inject discovered services:


```java
@HK2
public class PrimaryInjectionTest {

    @Inject
    PrimaryService primaryService;

    @Test
    public void assertPrimaryServiceInjecton() {
        assertThat(primaryService).isNotNull();
    }

    @Test
    public void assertSecondaryService() {
        assertThat(primaryService.getSecondaryService()).isNotNull();
    }
}
```

### Custom Binders Without Service Discovery

If service discovery and population is not desirable then you can turn it off by setting the populate parameter to false and defining your own Binder class(es):


```java
@HK2(populate = false, binders = {CombinedBinder.class})
public class BinderInjectionTest {

    @Inject
    PrimaryService primaryService;

    @Test
    public void assertPrimaryServiceInjecton() {
        assertThat(primaryService).isNotNull();
    }

    @Test
    public void assertSecondaryService() {
        assertThat(primaryService.getSecondaryService()).isNotNull();
    }
}
```

### Service Discovery and Custom Binders

You can also use both service population and your own binder class(es). Simply insure that populate flag is set to true (by default set to true) and specify your binders like this:

```java
@HK2(binders = {PrimaryBinder.class, SecondaryBinder.class})
public class MultipleBinderInjectionTest {

    @Inject
    PrimaryService primaryService;

    @Test
    public void assertPrimaryServiceInjecton() {
        assertThat(primaryService).isNotNull();
    }

    @Test
    public void assertSecondaryService() {
        assertThat(primaryService.getSecondaryService()).isNotNull();
    }
}
```

The above will create a single service locator instance named "hk2-testng-locator" that contains all the discovered services as well as services defined in your binder.


### Custom Service Locator Name

Finally if you wish to use a custom service locator name you can by specifying @HK2 annotation's value parameter:

```java
@HK2("custom")
public class CustomLocatorNameTest {

    @Inject
    ServiceLocator sericeLocator;

    @Inject
    PrimaryService primaryService;

    @Test
    public void assertPrimaryServiceInjecton() {
        assertThat(primaryService).isNotNull();
    }

    @Test
    public void assertSecondaryService() {
        assertThat(primaryService.getSecondaryService()).isNotNull();
    }

    @Test
    public void assertServiceLocatorIsCustom() {
        assertThat(sericeLocator.getName())
                .isEqualTo("custom");
    }
}
```

Note that if two test classes are annotated with @HK2("custom") then only one service locator will be created and the tests will share this service locator. 


### Isolated Service Locators

If you wish to use an isolated service locators per test or for certain tests then you will need to define a unique service locator name for these test classes:


```java
@HK2("serviceLocatorNameA")
public class Isolated1Test {
  ...
}

@HK2("serviceLocatorNameB")
public class Isolated2Test {
  ....
}
```
