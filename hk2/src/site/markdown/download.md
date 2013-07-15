## How to Download

## Jersey 2.x

Jersey 2.0, that implements [JAX-RS 2.0 API][jaxrs-2.0] API is the most recent release of Jersey.
To see the details about all changes, bug fixed and updates, please check the [Jersey 2.0 Release Notes][rn-2.0].

For the convenience of non-maven developers the following links are provided:

*   [<var class="icon-cloud-download"></var> Jersey JAX-RS 2.0 RI bundle][zip-2.x] bundle contains
    the JAX-RS 2.0 API jar, all the core Jersey module jars as well as all the required 3rd-party
    dependencies.
*   [<var class="icon-cloud-download"></var> Jersey 2.0 Examples bundle][examples-2.x] provides
    convenient access to the Jersey 2 examples for off-line browsing.

All the Jersey 2 release binaries, including the source & apidocs jars, are available for
download under the Jersey 2 maven root group identifier `org.glassfish.jersey` from the 
[central maven repository][mvn-central] as well as from the [java.net maven repository][mvn-jvn].

Chances are you are using Apache Maven as a build & dependency management tool for your project.
If you do, there is a very easy and convenient way to start playing with Jersey 2.0 by generating
the skeleton application from one of the Jersey 2 maven archetypes that we provide.
For instance, to create a Jersey 2.0 application using the Grizzly 2 HTTP server container, use

```bash
mvn archetype:generate -DarchetypeGroupId=org.glassfish.jersey.archetypes \
    -DarchetypeArtifactId=jersey-quickstart-grizzly2 -DarchetypeVersion=2.0
```

If you want to create a Servlet container deployable Jersey 2.0 web application instead, use

```bash
mvn archetype:generate -DarchetypeGroupId=org.glassfish.jersey.archetypes \
    -DarchetypeArtifactId=jersey-quickstart-webapp -DarchetypeVersion=2.0
```

Maven users may also be interested in the list of all [Jersey 2 modules and dependencies][deps-2.x]

### Jersey 1.x

Jersey 1.17.1 is the latest released version of Jersey 1.x. For the convenience of non-maven developers
the following links are provided:

*   [<var class="icon-cloud-download"></var> Jersey 1.17.1 ZIP bundle][zip-1.x] contains the Jersey
    jars, core dependencies (it does not provide dependencies for third party jars beyond those for JSON
    support) and JavaDoc.
*   [<var class="icon-cloud-download"></var> Jersey 1.17.1 JAR bundle][jar-1.x] is a single-JAR Jersey
    bundle to avoid the dependency management of multiple Jersey module JARs.

[mvn-central]: http://repo1.maven.org/maven2/org/glassfish/jersey/
[mvn-jvn]: https://maven.java.net/content/repositories/releases/org/glassfish/jersey/

[zip-1.x]: http://repo1.maven.org/maven2/com/sun/jersey/jersey-archive/1.17.1/jersey-archive-1.17.1.zip
[jar-1.x]: http://repo1.maven.org/maven2/com/sun/jersey/jersey-bundle/1.17.1/jersey-bundle-1.17.1.jar
[deps-1.x]: https://jersey.java.net/documentation/1.17/chapter_deps.html

[jaxrs-2.0]: http://jax-rs-spec.java.net/
[zip-2.x]: http://repo1.maven.org/maven2/org/glassfish/jersey/bundles/jaxrs-ri/2.0/jaxrs-ri-2.0.zip
[examples-2.x]: http://repo1.maven.org/maven2/org/glassfish/jersey/bundles/jersey-examples/2.0/jersey-examples-2.0-all.zip
[deps-2.x]: https://jersey.java.net/documentation/latest/modules-and-dependencies.html
[rn-2.0]: https://jersey.java.net/release-notes/2.0.html
