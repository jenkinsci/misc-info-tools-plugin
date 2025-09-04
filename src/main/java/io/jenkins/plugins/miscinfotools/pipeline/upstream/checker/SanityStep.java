package io.jenkins.plugins.miscinfotools.pipeline.upstream.checker;

import hudson.AbortException;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;

public class SanityStep extends SynchronousStepExecution<Void> implements Serializable {

    private static final long serialVersionUID = 1L;
    private StepContext context;
    private boolean isBuilding;
    private boolean inQueue;
    private boolean isSuccess;
    private boolean jobExists;
    private boolean hasRun;

    private ArrayList<String> deps;

    protected SanityStep(
            StepContext context,
            boolean isBuilding,
            ArrayList<String> deps,
            boolean inQueue,
            boolean isSuccess,
            boolean jobExists,
            boolean hasRun) {
        super(context);
        this.isBuilding = isBuilding;
        this.context = context;
        this.deps = deps;
        this.inQueue = inQueue;
        this.isSuccess = isSuccess;
        this.jobExists = jobExists;
        this.hasRun = hasRun;
    }

    @Override
    protected Void run() throws Exception {
        Jenkins server = Jenkins.getInstanceOrNull();
        if (server == null) return null;
        PrintStream logger = getContext().get(TaskListener.class).getLogger();
        for (String jobName : deps) {
            logger.println("Testing Job: " + jobName);
            Job<?, ?> job = (Job<?, ?>) server.getItemByFullName(jobName, Job.class);
            if (jobExists && job == null) {
                context.setResult(Result.FAILURE);
                throw new AbortException("Job: " + jobName + ", does not exist!");
            } else if (isBuilding && job.isBuilding()) {
                context.setResult(Result.ABORTED);
                throw new InterruptedException("Job: " + jobName + ". is currently building!");
            } else if (inQueue && job.isInQueue()) {
                context.setResult(Result.ABORTED);
                throw new InterruptedException("Job: " + jobName + ", is currently in queue!");
            }
            Run<?, ?> last = job.getLastCompletedBuild();
            if (hasRun && null == last) {
                context.setResult(Result.ABORTED);
                throw new InterruptedException("Job: " + jobName + ", has never run!");
            } else if (isSuccess && Result.SUCCESS != last.getResult()) {
                context.setResult(Result.ABORTED);
                throw new InterruptedException("Job: " + jobName + ", is not in state SUCCESS!");
            }
        }
        logger.println("All requested jobs look good!");
        return null;
    }
}
