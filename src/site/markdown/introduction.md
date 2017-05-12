[//]: # " DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER. "
[//]: # "  "
[//]: # " Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved. "
[//]: # "  "
[//]: # " The contents of this file are subject to the terms of either the GNU "
[//]: # " General Public License Version 2 only (''GPL'') or the Common Development "
[//]: # " and Distribution License(''CDDL'') (collectively, the ''License'').  You "
[//]: # " may not use this file except in compliance with the License.  You can "
[//]: # " obtain a copy of the License at "
[//]: # " https://oss.oracle.com/licenses/CDDL+GPL-1.1 "
[//]: # " or LICENSE.txt.  See the License for the specific "
[//]: # " language governing permissions and limitations under the License. "
[//]: # "  "
[//]: # " When distributing the software, include this License Header Notice in each "
[//]: # " file and include the License file at LICENSE.txt. "
[//]: # "  "
[//]: # " GPL Classpath Exception: "
[//]: # " Oracle designates this particular file as subject to the ''Classpath'' "
[//]: # " exception as provided by Oracle in the GPL Version 2 section of the License "
[//]: # " file that accompanied this code. "
[//]: # "  "
[//]: # " Modifications: "
[//]: # " If applicable, add the following below the License Header, with the fields "
[//]: # " enclosed by brackets [] replaced by your own identifying information: "
[//]: # " ''Portions Copyright [year] [name of copyright owner]'' "
[//]: # "  "
[//]: # " Contributor(s): "
[//]: # " If you wish your version of this file to be governed by only the CDDL or "
[//]: # " only the GPL Version 2, indicate your decision by adding ''[Contributor] "
[//]: # " elects to include this software in this distribution under the [CDDL or GPL "
[//]: # " Version 2] license.''  If you don't indicate a single choice of license, a "
[//]: # " recipient has the option to distribute your version of this file under "
[//]: # " either the CDDL, the GPL Version 2 or to extend the choice of license to "
[//]: # " its licensees as provided above.  However, if you add GPL Version 2 code "
[//]: # " and therefore, elected the GPL Version 2 license, then the option applies "
[//]: # " only if the new code is made subject to such option by the copyright "
[//]: # " holder. "

## Introduction to HK2

This page describes the HK2 2.0 API, which is based on JSR-330 standard annotations.
Also, Habitat has been replaced with a new interface called [ServiceLocator][servicelocator].

+ [Getting Started](introduction.html#Getting_Started)
+ [Named Services](introduction.html#Named_Services)
+ [Qualified Services](introduction.html#Qualified_Services)
+ [Basic Injection](introduction.html#Basic_Injection)
+ [Injection by name](introduction.html#Injection_by_name)
+ [Injection by qualifier](introduction.html#Injection_by_qualifier)
+ [Provider injection](introduction.html#Provider_injection)
+ [IterableProvider injection](introduction.html#IterableProvider_injection)
+ [Iterable injection](introduction.html#Iterable_injection)
+ [Conclusion](introduction.html#Conclusion)

HK2 is a declarative framework for services using annotations like [Contract][contract] and [Service][service].
This page is intended to show simple usages of HK2 mainly using the standard JSR-330 API.

For information about using the HK2 programmatic API see [this page][api-overview].
For information about HK2 extensibility options see [this page][extensibility]
For information about JSR-330 see [this site][jsr330].

This page assumes that you are using the HK2 provided ability to automatically find and
register services in an HK2 registry.  For more information on how to control what services
are automatically bound to what registries see TBD.

### Getting Started

In order to mark a concrete implementation class as one that should be available 
as a service you annotate your class with [@Service][service].
 
```java
@Service
public class Foo {
}
```java

By default Foo will be advertised as itself and by any interfaces that are marked with [@Contract][contract].
Lets make Foo an interface that is a Contract and create an implementation of Foo:
 
```java
@Contract
public interface Foo {
}

@Service
public class FooImpl implements Foo {
}
```java

The FooImpl class will be placed into the registry advertised under both FooImpl and Foo.
 
### Named Services
 
In order to differentiate different implementations of the same interface you can name your services.
Here is an example of a class that implements a contract and has two implementations, both named differently:
 
```java
@Contract
public interface Book {
}

@Service @Named
public class MobyDick implements Book {
}

@Service @Named
public class ParadiseLost implements Book {
}
```java

The two classes, MobyDick and ParadiseLost, will be added to the service registry with the names
"MobyDick" and "ParadiseLost".  If you use the [Named][named]
qualifier without specifying a name then the name you get is the class name without the package.
 
### Qualified Services

Services can also be qualified with annotations called qualifiers.  Qualifiers are annotations that are themselves
annotated with [@Qualifier][qualifier].  Here is an
example contract with three implementations:
 
```java
@Contract
public interface Color {
}

@Service @Blue
public class BlueColor implements Color {
}

@Service @Red
public class RedColor implements Color {
}

@Service @Yellow
public class YellowColor implements Color {
}
```java

The Blue annotation is defined like this:

```java
@Qualifier
@Retention(RUNTIME)
@Target( { TYPE, METHOD, FIELD, PARAMETER })
public @interface Blue {
}
```java

It is an exercise left up to the reader to implement the Red and Yellow annotations.
 
### Basic Injection

Lets make our example a little more interesting by injecting a book into Foo.  This is done using the
JSR-330 standard [Inject][inject] annotation:
 
```java
@Service
public class FooImpl implements Foo {
  @Inject
  private Book book;
  
  ...
}
```java

Upon construction the book field will be filled in by HK2.  You can also inject into the constructor of FooImpl, or
use an initializer method.  In both of those cases the constructor or method must be annotated with
[@Inject][inject] in order to tell HK2 which is the proper constructor or method to call.

Here is FooImpl implemented with constructor injection:
 
```java
@Service
public class FooImpl implements Foo {
  private final Book book;
  
  @Inject
  public FooImpl(Book book) {
      // constructor injected!
      this.book = book;
  }
}
```java

Here is FooImpl implemented with initializer method injection:
 
```java
@Service
public class FooImpl implements Foo {
  private Book book;
  
  @Inject
  public void setBook(Book book) {
      // initializer method injected!
      this.book = book;
  }
}
```java

In all three of the above cases (field injected, constructor injected or initializer method injected) the injection will occur
prior to the postConstruct method of FooImpl.  In this example we use the injected book in the postConstruct method:
 
```java
@Service
public class FooImpl implements Foo {
  @Inject
  private Book book;
  
  @PostConstruct
  private void postConstruct() {
      book.doSomething();
  }
}
```java

### Injection by name

The astute observer of our example will have noticed that when injecting a Book into FooImpl that we never selected which book we wanted.
That can be fixed by using the [Named][named] qualifier at the point of injection.  Lets fix the example by injecting both of the Books we defined earlier:
 
```java
@Service
public class FooImpl implements Foo {
  @Inject @Named("MobyDick")
  private Book mobyDick;
  
  @Inject @Named("ParadiseLost")
  private Book paradiseLost;
}
```java

The implementation of Book given the name "MobyDick" will be injected into the mobyDick field, and the implementation of
Book given the name "ParadiseLost" will be injected into the paradiseLost field.
 
### Injection by qualifier

Injections can also be more specifically chosen by using qualifiers.  In the previous example we created three implementations
of Color, each of which was qualified with a qualifier.  Here we create a class called ColorMixer which injects the colors
in an initializer method, which also demonstrates that an initializer method (or constructor) can take more than one
parameter:

```java
@Service
public class ColorMixer {
    private Color red;
    private Color blue;
    private Color yellow;
  
    @Inject
    private void addPrimaries(
            @Red Color red,
            @Blue Color blue,
            @Yellow Color yellow) {
      this.red = red;
      this.blue = blue;
      this.yellow = yellow;
   }
}
```java

Note that the qualifiers can go on the parameters of the initializer method addPrimaries.  In the above example the RedColor,
BlueColor and YellowColor services will be injected into the proper fields of the initializer.
 
### Provider injection

There are times when your code would like finer control over when a instance of a service is created.
Anywhere that you can inject a service, you can also inject a [Provider][atinjectprovider].
When you inject a [Provider][atinjectprovider] for a service rather than
the service itself the system will potentially delay the creation of the service until the 
[get][providerget] method of the [Provider][provider] has been called.
 
Lets go back to our ColorMixer example, and inject providers (into fields this time) for our primaries.
 
```java
@Service
public class ColorMixer {
    @Inject @Red
    private Provider<Color> redProvider;
    
    @Inject @Blue
    private Provider<Color> blueProvider;
    
    @Inject @Yellow
    private Provider<Color> yellowProvider;
}
```java

This service can then get the color implementations later.  In this method of ColorMixer we create purple by getting the
red and blue colors:

```java
@Service
public class ColorMixer {
    ...
    
    public Color makePurple() {
      return mix(redProvider.get(), blueProvider.get());
    }
}
```java

Note that if no-one ever makes a color that involves using yellow, that the YellowColor implementation class will never
be created, since no-one ever called the [get][providerget] method of the yellowProvider field.

The value passed into any [Provider][provider] injection point will be an
instance of [IterableProvider][iterableprovider].
 
### IterableProvider injection

It is often the case that a single contract has more than one implementation.  Sometimes it is useful to get access to
all of the implementations of the contract.
This can be done by using an [IterableProvider][iterableprovider].
[IterableProvider][iterableprovider] extends [Provider][provider] and also implements [Iterable][iterable].
Anywhere a service can be injected an [IterableProvider][iterableprovider] for that service can be injected.

In this example we create a Library service that wants to be able to list all of the books it carries:

```java
@Service
public class Library {
    @Inject
    private IterableProvider<Book> allBooks;
    
    public LinkedList<Book> getAllBooks() {
        LinkedList<Book> retVal = new LinkedList<Book>();
        
        for (Book book : allBooks) {
            retVal.add(book);
        }
        
        return retVal;
    }
}
```java

Since [IterableProvider][iterableprovider] implements [Iterable][iterable] 
it can be used in Java for/while loops, as demonstrated in the above example.

Another feature of the [IterableProvider][iterableprovider] is that it can
be used to further narrow down the selection criteria at run time.
In our above example we can progrommatically choose the book we are interested 
in based on a name passed into a method.  Here is how it would look:
 
```java
@Service
public class Library {
    @Inject
    private IterableProvider<Book> allBooks;
    
    public Book findBook(String name) {
        return allBooks.named(name).get();
    }
}
```java

In the above example we call the [named][iterableprovidernamed] method [IterableProvider][iterableprovider]
in order to select the book with the given name.
The call to [get][providerget] then just returns the book with the given name.

### Iterable injection

[Iterable][iterable] can be used as an injection point rather than [IterableProvider][iterableprovider].  The following code will
work as expected:

```java
@Service
public class Library {
    @Inject
    private Iterable<Book> allBooks;
    
    public LinkedList<Book> getAllBooks() {
        LinkedList<Book> retVal = new LinkedList<Book>();
        
        for (Book book : allBooks) {
            retVal.add(book);
        }
        
        return retVal;
    }
}
```java

The value passed into any [Iterable][iterable] injection point will be an
instance of [IterableProvider][iterableprovider].
 
### Conclusion

The majority of usages of HK2 should use standard JSR-330 annotations along with
[@Service][service] and [@Contract][contract].
In some cases code will also use [IterableProvider][iterableprovider] as outlined above.
In even rarer cases the code may need extremely specialized control over HK2 by using the
programmatic API, as described [here][api-overview].

We have gone through many simple examples which have shown the basic functionality of HK2 and JSR-330 in your
applications.  Hopefully they have provided you with knowledge about how these annotations work and how they
can be used in your own applications.

[servicelocator]: apidocs/org/glassfish/hk2/api/ServiceLocator.html
[contract]: apidocs/org/jvnet/hk2/annotations/Contract.html
[service]: apidocs/org/jvnet/hk2/annotations/Service.html
[jsr330]: http://www.jcp.org/en/jsr/detail?id=330
[api-overview]: ./api-overview.html
[extensibility]: ./extensibility.html
[inject]: http://docs.oracle.com/javaee/6/api/javax/inject/Inject.html
[iterable]: http://docs.oracle.com/javase/6/docs/api/java/lang/Iterable.html
[iterableprovidernamed]: apidocs/org/glassfish/hk2/api/IterableProvider.html#named%28java.lang.String%29
[iterableprovider]: apidocs/org/glassfish/hk2/api/IterableProvider.html
[providerget]: http://atinject.googlecode.com/svn/trunk/javadoc/javax/inject/Provider.html#get%28%29
[provider]: http://atinject.googlecode.com/svn/trunk/javadoc/javax/inject/Provider.html
[named]: http://docs.oracle.com/javaee/6/api/javax/inject/Named.html
[qualifier]: http://docs.oracle.com/javaee/6/api/javax/inject/Qualifier.html
[atinjectprovider]: http://atinject.googlecode.com/svn/trunk/javadoc/javax/inject/Provider.html
