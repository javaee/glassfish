/**
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
* Copyright 2009 Sun Microsystems, Inc. All rights reserved.
* Generated code from the com.sun.enterprise.config.serverbeans.*
* config beans, based on  HK2 meta model for these beans
* see generator at org.admin.admin.rest.GeneratorResource
* date=Mon May 11 13:27:46 PDT 2009
* Very soon, this generated code will be replace by asm or even better...more dynamic logic.
* Ludovic Champenois ludo@dev.java.net
*
**/
package org.glassfish.admin.rest.resources;
import com.sun.enterprise.config.serverbeans.*;
import javax.ws.rs.*;
import org.glassfish.admin.rest.TemplateResource;
import com.sun.enterprise.config.serverbeans.JavaConfig;
public class JavaConfigResource extends TemplateResource<JavaConfig> {

@Path("commands/create-profiler ")
@GET
@Produces({javax.ws.rs.core.MediaType.TEXT_HTML, javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML})
public org.jvnet.hk2.config.Dom execCreateProfiler(
	 @QueryParam("classpath")  @DefaultValue("")  String Classpath 
 ,
	 @QueryParam("enabled")  @DefaultValue("true")  String Enabled 
 ,
	 @QueryParam("nativelibrarypath")  @DefaultValue("")  String Nativelibrarypath 
 ,
	 @QueryParam("profiler_name")  @DefaultValue("")  String Profiler_name 
 ,
	 @QueryParam("property")  @DefaultValue("")  String Property 
 ,
	 @QueryParam("target")  @DefaultValue("")  String Target 
 	) {
	java.util.Properties p = new java.util.Properties();
	p.put("classpath", Classpath);
	p.put("enabled", Enabled);
	p.put("nativelibrarypath", Nativelibrarypath);
	p.put("profiler_name", Profiler_name);
	p.put("property", Property);
	p.put("target", Target);
	org.glassfish.api.ActionReport ar = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.ActionReport.class);
	org.glassfish.api.admin.CommandRunner cr = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.admin.CommandRunner.class);
	cr.doCommand("create-profiler", p, ar);
	System.out.println("exec command =" + ar.getActionExitCode());
	return get(1);
}
	@Path("profiler/")
	public ProfilerResource getProfilerResource() {
		ProfilerResource resource = resourceContext.getResource(ProfilerResource.class);
		resource.setEntity(getEntity().getProfiler() );
		return resource;
	}
	@Path("property/")
	public ListPropertyResource getPropertyResource() {
		ListPropertyResource resource = resourceContext.getResource(ListPropertyResource.class);
		resource.setEntity(getEntity().getProperty() );
		return resource;
	}
}
