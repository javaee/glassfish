This file describes the subdirectories of
https://svn.java.net/svn/glassfish~svn/trunk/
Last updated: 06/18/2013 by romain.grecourt@oracle.com


- api

	Some separate API definition maven projects to allow API jar files
	to be published independently of GlassFish.


- copyright-plugin

	The maven plugin that checks for the correct copyright/license
	notice in files related to the GlassFish project.


- external

	Copies of third party open source projects that are used in the
	build of GlassFish, along with tools to build those projects.
	GlassFish depends on the versions of these projects that we build,
	not directly on binaries produced by the originating project.


- fighterfish

	Modules supporting use of OSGi by Java EE applications.


- findbugs

	The common FindBugs exclude list used by GlassFish projects,
	as well as related tools used in scripts in Hudson jobs.


- maven-plugins

	A directory containing various maven plugin (embedded-glassfish and plugins used during builds)


- hudson-plugin

	Hudson plugin to help with cluster testing.


- logging-annotation-processor

	A Java annotation processor that handles logging-related annotations.


- main

	The main GlassFish project source code.


- main-docs

	Documentation resources (online help, etc.) for the GlassFish project.


- main-docs-l10n

	L10n documentation resources (online help, etc.) for the GlassFish project.


- schemas

	The JCP-defined XML schemas for many Java EE specs.


- v2

	The older GlassFish v2 source code.
	Also contains the current devtests for GlassFish.
