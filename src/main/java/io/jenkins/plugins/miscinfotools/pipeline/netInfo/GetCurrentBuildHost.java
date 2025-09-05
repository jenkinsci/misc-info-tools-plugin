package io.jenkins.plugins.miscinfotools.pipeline.netInfo;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import jenkins.security.Roles;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.DataBoundConstructor;

public class GetCurrentBuildHost extends Step implements Serializable {

    private static final long serialVersionUID = 1505586566993544821L;

    @DataBoundConstructor
    public GetCurrentBuildHost() {}

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new StepExecutionImpl(this, context);
    }

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "getCurrentBuildHost";
        }

        @Override
        public String getDisplayName() {
            return "Get the Build Hostname";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.emptySet();
        }
    }

    private static class StepExecutionImpl extends SynchronousNonBlockingStepExecution<String> {

        private static final long serialVersionUID = GetCurrentBuildHost.serialVersionUID;

        StepExecutionImpl(GetCurrentBuildHost step, StepContext context) {
            super(context);
        }

        @Override
        protected String run() throws Exception {
            FilePath workspace = getContext().get(FilePath.class);
            TaskListener listener = getContext().get(TaskListener.class);

            if (workspace == null) {
                return java.net.InetAddress.getLocalHost().getHostName();
            }
            if (listener == null) {
                return java.net.InetAddress.getLocalHost().getHostName();
            }
            return workspace.act(new AgentCallable(listener));
        }
    }

    private static class AgentCallable implements Callable<String, IOException>, Serializable {
        private static final long serialVersionUID = 1L;
        // private final TaskListener listener;

        AgentCallable(TaskListener listener) {
            // this.listener = listener;
        }

        @Override
        public String call() throws IOException {
            // listener.getLogger().println("Executing remotely on the agent.");
            return java.net.InetAddress.getLocalHost().getHostName();
        }

        @Override
        public void checkRoles(RoleChecker checker) throws SecurityException {
          // based on: https://www.jenkins.io/doc/developer/security/remoting-callables/#minimal-safe-implementations
          // I mean we are just doing a hostname lookup on the build node.
          checker.check(this,Roles.SLAVE);
        }
    }
}
