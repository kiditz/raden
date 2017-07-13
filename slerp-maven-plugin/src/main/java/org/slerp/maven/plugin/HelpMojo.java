package org.slerp.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "help", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class HelpMojo extends AbstractMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Entity       : mvn slerp:entity");		
		getLog().info("Function     : mvn slerp:function");
		getLog().info("Transaction  : mvn slerp:transaction");
		getLog().info("Unit Test    : mvn slerp:test");
		getLog().info("Rest A.P.I   : mvn slerp:rest");
	}

}
