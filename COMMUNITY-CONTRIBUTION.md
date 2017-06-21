### How to contribute to GlassFish

#### Participate in the GlassFish Community

There are many ways to contribute to GlassFish, being involved in the GlassFish Community is probably the easiest way. For example, you can help your peers in the [GlassFish Forums](/forum.html).

#### Adopt-a-JSR

Adopt-a-JSR is an initiative by JUG leaders to encourage JUG members to get involved in a JSR and to evangelize that JSR to their JUG and the wider Java community, in order to increase grass roots participation.  
More information can be found on the [Adopt-a-JSR for Java EE](https://glassfish.java.net/adoptajsr/) page.

#### Contribute code to GlassFish

If you are a developer interested in contributing to the GlassFish effort, here are a few things you should probably know.

**Understand the Code**

Developing an application server is a big deal. Below are the links that help us organize the development of a project of this size.

*   Check out the GlassFish code from the [public SVN repository](https://java.net/projects/glassfish/sources/svn/show/trunk/main) and study it.
*   GlassFish is divided into [modules](https://glassfish.java.net/wiki-archive/ModulesAndLeads.html), with a lead for each of them.  

*   [Talk to other GlassFish developers](/forum.html) about the implementation details on our mailing lists.

**Build GlassFish**

Starting with GlassFish v3, the build infrastructure is entirely based on Maven (2).  
This [wiki page](https://glassfish.java.net/wiki-archive/FullBuildInstructions.html) should have all the details and steps required to build GlassFish.

[](testgf)**Test GlassFish**

There are several different tests that are currently available in GlassFish. QuickLook tests run in a short time and cover a broad set of features in the app server. Developer tests are also available for the different modules. Each module has more information about running those tests.

For more information, see the [GlassFish Quality Portal](https://glassfish.java.net/wiki-archive/GlassFishQuality.html).

*   [Download](http://download.java.net/glassfish/4.0.1/) a nightly or promoted build
*   Run the [QuickLook tests](http://glassfish.java.net/public/GuidelinesandConventions.html#Quicklook_Tests)
*   Join the [FishCAT](https://glassfish.java.net/fishcat/) program.  

*   [Submit a bug](https://java.net/jira/browse/GLASSFISH)

[](contribcode)**Contribute Code**

Contributing to project GlassFish can be done in various ways: bug fixes, enhancements, new features, or even whole modules.

*   All contributors must sign the [Oracle Contributor Agreement](http://www.oracle.com/technetwork/community/oca-486395.html)
*   To have commit privileges, you must first be granted the Developer role. To get started, submit some patches via email, and then ask the maintainer of the code for commit access. The maintainer will seek consensus before granting the role, but their decisions are final.
*   All code checked into the workspace must follow the [coding conventions](http://glassfish.java.net/public/GuidelinesandConventions.html#Coding_Conventions)
*   [Request the Developer role](http://java.net/projects/glassfish/watch)
*   If you don't yet have the Developer role, see the [process for providing a patch](http://glassfish.java.net/public/GuidelinesandConventions.html#Submit_a_Patch)
*   Developers must follow [commit procedures](http://glassfish.java.net/public/GuidelinesandConventions.html#Commit_Procedures) when updating the workspace

[](contribpackage)

#### Contribute GlassFish Packages

GlassFish uses [IPS/pkg(5)](http://updatecenter2.java.net) as its modules systems accessible using the update center feature of the product.  
We welcome contributions of additional packages. For more information:

*   check out the dedicated [glassfish-repo project](http://glassfish-repo.dev.java.net)
*   [browse the current "contrib" repository](http://pkg.glassfish.org/v3/contrib/) to get a sense of what's available today.
*   read [blogs about how to write IPS packages for GlassFish](http://blogs.oracle.com/alexismp/tags/gfcommunitypackages)

[](contribsample)

#### Contribute Samples

We also welcome good quality samples that demonstrate a feature or aspect of GlassFish or Java EE technology.[](contribdoc)

#### Contribute Documentation

One mark of a quality product is its supporting documentation. There are many ways to contribute GlassFish documentation, most of which are listed on the [documentation home page](http://glassfish.java.net/javaee5/docs/DocsIndex.html). Also see the [Community Docs](https://glassfish.java.net/wiki-archive/CommunityDocs.html) page on the GlassFish wiki, which focuses on the specifics of community doc contributions. Explore these pages, think about how you'd like to get involved, then do so! We welcome and encourage your contributions. Questions about how or what to contribute? Contact the [documentation mailing list](mailto:docs@glassfish.java.net).  