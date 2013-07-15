Developing RESTful Web services that seamlessly support exposing your data in a
variety of representation media types and abstract away the low-level details
of the client-server communication is not an easy task without a good toolkit.
In order to simplify development of RESTful Web services and their clients in Java,
a standard and portable [JAX-RS API][jax-rs] has been designed.
Jersey RESTful Web Services framework is open source, production quality,
framework for developing RESTful Web Services in Java that provides support for
JAX-RS APIs and serves as a JAX-RS (JSR 311 & JSR 339) Reference Implementation.

Jersey framework is more than the JAX-RS Reference Implementation. Jersey provides
it's own [API][jersey-api] that extend the JAX-RS toolkit with additional features
and utilities to further simplify RESTful service and client development. Jersey
also exposes numerous extension SPIs so that developers may extend Jersey to best
suit their needs.

Goals of Jersey project can be summarized in the following points:

*   Track the JAX-RS API and provide regular releases of production quality
    Reference Implementations that ships with GlassFish;
*   Provide APIs to extend Jersey & Build a community of users and developers;
    and finally
*   Make it easy to build RESTful Web services utilising Java and the
    Java Virtual Machine.

---

<h2><a class="headerlink" href="https://jersey.java.net/documentation/latest/getting-started.html">
    <var class="icon-compass"></var> Get Started
</a></h2>

[Learn][quick] how to use Jersey in your projects.


<h2><a class="headerlink" href="https://jersey.java.net/documentation/latest/index.html">
    <var class="icon-book"></var> Documentation
</a></h2>

Read [latest Jersey User Guide][docindex] or browse [latest Jersey API][jersey-api].

Jersey 1.x users may access the [Jersey 1.17 User Guide][docindex-1.x] and [Jersey 1.17 API][jersey-1.x-api].


<h2><a class="headerlink" href="download.html">
    <var class="icon-cloud-download"></var> Download
</a></h2>

Jersey is distributed mainly via Maven and it offers some extra modules.
Check the [How to Download][dwnld] page or see our list of [dependencies][deps] for details.


<h2><a class="headerlink" href="related.html">
    <var class="icon-tags"></var> Related Projects
</a></h2>

List of projects related to Jersey.


<h2><a class="headerlink" href="contribute.html">
    <var class="icon-group"></var> Contribute
</a></h2>

[Learn][contpage] how you can contribute to the project by:
<ul class="icons-ul">
    <li><var class="icon-li icon-bug"></var> Reporting issues</li>
    <li><var class="icon-li icon-code-fork"></var> Submitting patches</li>
    <li><var class="icon-li icon-eye-open"></var> Reviewing code</li>
</ul>


<h2><a class="headerlink" href="bloggers.html">
    <var class="icon-rss"></var> Developer Blogs
</a></h2>

Find out what our developers [blog][bloggers] about.

[jax-rs]: http://jax-rs-spec.java.net/
[jersey-api]: https://jersey.java.net/apidocs/latest/jersey/index.html
[jersey-1.x-api]: https://jersey.java.net/apidocs/1.17/jersey/index.html
[contpage]: contribute.html
[quick]: https://jersey.java.net/documentation/latest/getting-started.html
[dwnld]: download.html
[deps]: https://jersey.java.net/documentation/latest/modules-and-dependencies.html
[docindex]: https://jersey.java.net/documentation/latest/index.html
[docindex-1.x]: https://jersey.java.net/documentation/1.17/index.html
[bloggers]: bloggers.html
