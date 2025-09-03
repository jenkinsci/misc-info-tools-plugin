package io.jenkins.plugins.miscinfotools.pipeline.upstream.checker;
/*
 * This class is based on the following documentation
 * https://www.jenkins.io/doc/developer/plugin-development/pipeline-integration/
 *
 */

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * This class solves the orthogonal problem of checking the status of multiple upstream jobs for successful build status.
 */
public class SaneUpstreamChecker extends Step {

    private ArrayList<String> deps;

    /**
     * This option sets the list of upstream projects that need to be validated for successful builds before this job executes.
     * @param
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
    public SaneUpstreamChecker(ArrayList<String> deps) {
        this.deps = deps;
    }

 


    @Override
    public StepExecution start(StepContext context) throws Exception {
        Jenkins server = Jenkins.getInstanceOrNull();
        if (server == null) ;
        for (String jobName : deps) {
            //listener.getLogger().println("Testing Job: " + jobName);
            Job<?, ?> job = (Job<?, ?>) server.getItemByFullName(jobName, Job.class);
            if (job == null) {
              context.setResult(Result.FAILURE);
                throw new AbortException("Job: " + jobName + ", does not exist!");
            } else if (job.isBuilding()) {
              context.setResult(Result.ABORTED);
                throw new InterruptedException("Job: " + jobName + ". is currently building!");
            } else if (job.isInQueue()) {
              context.setResult(Result.ABORTED);
                throw new InterruptedException("Job: " + jobName + ", is currently in queue!");
            }
            Run<?, ?> last = job.getLastCompletedBuild();
            if (null == last) {
              context.setResult(Result.ABORTED);
                throw new InterruptedException("Job: " + jobName + ", has never run!");
            } else if (Result.SUCCESS != last.getResult()) {
              context.setResult(Result.ABORTED);
                throw new InterruptedException("Job: " + jobName + ", is not in state SUCCESS!");
            }
            //listener.getLogger().println("Done testing Job: " + jobName);
        }
        //return "All requested jobs look good!";
        return null;
    }

    @Symbol("checkUpStreamJobs")
    @Extension
    public static class DescriptorImpl extends StepDescriptor  {

        @Override
        public String getDisplayName() {
            return "Sane Upstream Job Chekcer";
        }


        @Override
        public String getFunctionName() {
          
          return "checkUpStreamJobs";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
          return Collections.singleton(TaskListener.class);
        }
    }

   
}
