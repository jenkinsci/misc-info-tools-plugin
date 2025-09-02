package io.jenkins.plugins.miscinfotools.pipeline.netInfo;

import hudson.Extension;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Set;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

public class GetCurrentBuildHost extends Step implements Serializable {

    private static final long serialVersionUID = 1505586566993544821L;

    @DataBoundConstructor
    public GetCurrentBuildHost() {}

    private String getHostname() throws UnknownHostException {
        return java.net.InetAddress.getLocalHost().getHostName();
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new StepExecutionImpl(this, context);
    }

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "GetCurrentBuildHost";
        }

        @Override
        public String getDisplayName() {
            return "Build Host by Name";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.emptySet();
        }
    }

    private static class StepExecutionImpl extends SynchronousStepExecution<String> {

        private static final long serialVersionUID = GetCurrentBuildHost.serialVersionUID;
        private final GetCurrentBuildHost step;

        StepExecutionImpl(GetCurrentBuildHost step, StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected String run() throws Exception {
            return step.getHostname();
        }
    }
}
