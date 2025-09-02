package io.jenkins.plugins.miscinfotools.pipeline.upstream.buildnumber;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

public class GetUpstreamBuildNumber extends Step implements Serializable {

    private static final long serialVersionUID = 1505586566993544821L;

    private String job = null;
    ;

    @DataBoundConstructor
    public GetUpstreamBuildNumber(String jobName) {
        this.job = jobName;
    }

    private Integer getBuildNumber() {
        Jenkins server = Jenkins.getInstanceOrNull();
        if (server == null) return null;
        if (job == null) return null;
        Job<?, ?> build = (Job<?, ?>) server.getItemByFullName(job);
        if (build == null) return null;
        Run<?, ?> lastBuild = build.getLastSuccessfulBuild();
        if (lastBuild == null) return null;
        return lastBuild.getNumber();
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new StepExecutionImpl(this, context);
    }

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "GetUpsteamBuildNumber";
        }

        @Override
        public String getDisplayName() {
            return "Upstream build number getter";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.emptySet();
        }
    }

    private static class StepExecutionImpl extends SynchronousStepExecution<Integer> {

        private static final long serialVersionUID = GetUpstreamBuildNumber.serialVersionUID;
        private final GetUpstreamBuildNumber step;

        StepExecutionImpl(GetUpstreamBuildNumber step, StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected Integer run() throws Exception {
            return step.getBuildNumber();
        }
    }
}
