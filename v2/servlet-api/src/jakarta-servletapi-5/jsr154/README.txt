$Id: README.txt,v 1.1.1.1 2003/01/27 16:07:32 ja120114 Exp $

                      Java Servlet and JSP API Classes
                      ================================

This subproject contains the compiled code for the implementation classes of
the Java Servlet and JSP APIs (packages javax.servlet, javax.servlet.http,
javax.servlet.jsp, and javax.servlet.jsp.tagext).  It includes the following
contents:


  BUILDING.txt                Instructions for building from sources
  LICENSE                     Apache Software License for this release
  README.txt                  This document
  docs/                       Documentation for this release
      api/                    Javadocs for Servlet and JSP API classes
  lib/                        Binary JAR files for this release
      servlet.jar             Binary Servlet and JSP API classes
  src/                        Sources for Servlet and JSP API classes

In general, you will need to add the "servlet.jar" file (found in the "lib"
subdirectory of this release) into the compilation class path for your
projects that depend on these APIs.

The compiled "servlet.jar" file included in this subproject is automatically
included in binary distributions of Tomcat 4.0, so you need not download this
subproject separately unless you wish to utilize the Javadocs, or peruse the
source code to see how the API classes are implemented.
