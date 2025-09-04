package io.jenkins.plugins.miscinfotools.pipeline.upstream.checker;
/*
 * This class is based on the following documentation
 * https://www.jenkins.io/doc/developer/plugin-development/pipeline-integration/
 *
 */

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.AbortException;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;

/**
 * This class solves the orthogonal problem of checking the status of multiple upstream jobs for successful build status.
 */
public class SaneUpstreamChecker extends Step {

    private ArrayList<String> deps;
    
    private boolean isBuilding=false;
    
    public boolean getIsBuilding() { return isBuilding; }
    
    @DataBoundSetter
    public void setIsBuilding(boolean isBuilding) {
      this.isBuilding=isBuilding;
    }

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
        return new SanityStep(context,isBuilding);
    }

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

    public class SanityStep extends SynchronousStepExecution<Void>  {

      private static final long serialVersionUID = 1L;
      private StepContext context;
      private boolean isBuilding;
      protected SanityStep(StepContext context,boolean isBuilding) {
        super(context);
        this.isBuilding=isBuilding;
        this.context=context;
      }

      @Override
      protected Void run() throws Exception {
        Jenkins server = Jenkins.getInstanceOrNull();
        if (server == null) return null;
        PrintStream logger=getContext().get(TaskListener.class).getLogger();
        for (String jobName : deps) {
            logger.println("Testing Job: "+jobName);
            Job<?, ?> job = (Job<?, ?>) server.getItemByFullName(jobName, Job.class);
            if (job == null) {
              context.setResult(Result.FAILURE);
                throw new AbortException("Job: " + jobName + ", does not exist!");
            } else if (!isBuilding && job.isBuilding()) {
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
        }
        logger.println("All requested jobs look good!");
        return null;
      }


    } 
}
