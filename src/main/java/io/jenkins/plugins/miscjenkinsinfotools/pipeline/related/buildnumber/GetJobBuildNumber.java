package io.jenkins.plugins.miscjenkinsinfotools.pipeline.related.buildnumber;

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
import org.kohsuke.stapler.DataBoundSetter;

public class GetJobBuildNumber extends Step implements Serializable {

    private static final long serialVersionUID = 1505586566993544821L;

    private String job = null;

    @DataBoundConstructor
    public GetJobBuildNumber(String job) {
        this.job = job;
    }

    @DataBoundSetter
    public void setJob(String job) {
        this.job = job;
    }

    public String getJob() {
        return this.job;
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
            return "getLastSuccessfulBuildNumber";
        }

        @Override
        public String getDisplayName() {
            return "Job build number getter";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.emptySet();
        }
    }

    private static class StepExecutionImpl extends SynchronousStepExecution<Integer> {

        private static final long serialVersionUID = GetJobBuildNumber.serialVersionUID;
        private final GetJobBuildNumber step;

        StepExecutionImpl(GetJobBuildNumber step, StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected Integer run() throws Exception {
            return step.getBuildNumber();
        }
    }
}
