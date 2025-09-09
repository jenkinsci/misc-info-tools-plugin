package io.jenkins.plugins.miscjenkinsinfotools.pipeline.related.checker;
/*
 * This class is based on the following documentation
 * https://www.jenkins.io/doc/developer/plugin-development/pipeline-integration/
 *
 */

import hudson.Extension;
import hudson.model.TaskListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * This class solves the orthogonal problem of checking the status of multiple upstream jobs for successful build status.
 */
public class SaneRelatedJobChecker extends Step implements Serializable {

    private static final long serialVersionUID = -3209060662059512132L;

    private ArrayList<String> deps;

    private boolean isBuilding = true;
    private boolean inQueue = true;
    private boolean isSuccess = true;
    private boolean jobExists = true;
    private boolean hasRun = true;

    public boolean getHasRun() {
        return hasRun;
    }

    @DataBoundSetter
    public void setHasRun(boolean hasRun) {
        this.hasRun = hasRun;
    }

    public boolean getJobExists() {
        return jobExists;
    }

    @DataBoundSetter
    public void setJobExists(boolean jobExists) {
        this.jobExists = jobExists;
    }

    public boolean getInQueue() {
        return inQueue;
    }

    @DataBoundSetter
    public void setInQueue(boolean inQueue) {
        this.inQueue = inQueue;
    }

    public boolean getIsBuilding() {
        return isBuilding;
    }

    @DataBoundSetter
    public void setIsBuilding(boolean isBuilding) {
        this.isBuilding = isBuilding;
    }

    /**
     * This option sets the list of upstream projects that need to be validated for successful builds before this job executes.
     */
    public void setDeps(ArrayList<String> deps) {
        this.deps = deps;
    }

    /**
     * This method returns the current list of upstream projects that need to be validated for successful builds before this job executes.
     * @return
     */
    public ArrayList<String> getDeps() {
        return this.deps;
    }

    @DataBoundConstructor
    public SaneRelatedJobChecker(ArrayList<String> deps) {
        this.deps = deps;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new SanityStep(context, isBuilding, deps, inQueue, isSuccess, jobExists, hasRun);
    }

    public boolean getIsSuccess() {
        return isSuccess;
    }

    @DataBoundSetter
    public void setIsSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        @Override
        public String getDisplayName() {
            return "Related Job Checks";
        }

        @Override
        public String getFunctionName() {
            return "relatedJobChecks";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.singleton(TaskListener.class);
        }
    }
}
