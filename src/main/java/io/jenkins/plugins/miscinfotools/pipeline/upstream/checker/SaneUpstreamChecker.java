package io.jenkins.plugins.miscinfotools.pipeline.upstream.checker;
/*
 * This class is based on the following documentation
 * https://www.jenkins.io/doc/developer/plugin-development/pipeline-integration/
 *
 */

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import java.io.IOException;
import java.util.ArrayList;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * This class solves the orthogonal problem of checking the status of multiple upstream jobs for successful build status.
 */
public class SaneUpstreamChecker extends Builder implements SimpleBuildStep {

    private ArrayList<String> list;

    /**
     * This option sets the list of upstream projects that need to be validated for successful builds before this job executes.
     * @param
     */
    public void setDeps(ArrayList<String> deps) {
        this.list = deps;
    }

    /**
     * This method returns the current list of upstream projects that need to be validated for successful builds before this job executes.
     * @return
     */
    public ArrayList<String> getDeps() {
        return this.list;
    }

    @DataBoundConstructor
    public SaneUpstreamChecker(ArrayList<String> deps) {
        this.list = deps;
    }

    @Override
    public boolean requiresWorkspace() {
        return false;
    }

    @Override
    public void perform(Run<?, ?> build, EnvVars env, TaskListener listener) throws InterruptedException {
        listener.getLogger()
                .println("Job class is an instance of: " + build.getClass().getName());
        String msg = this.toggleState(build, listener);

        listener.getLogger().println(msg);
        try {
            build.save();
        } catch (IOException e) {
            listener.getLogger().println("Failed to save next build state" + e);
        }
    }

    @Override
    public void perform(Run<?, ?> build, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener)
            throws InterruptedException {
        this.perform(build, env, listener);
    }

    private String toggleState(Run<?, ?> build, TaskListener listener) throws InterruptedException {

        Jenkins server = Jenkins.getInstanceOrNull();
        if (server == null) return "Jenkins server offline";
        for (String jobName : list) {
            listener.getLogger().println("Testing Job: " + jobName);
            try {
                Job<?, ?> job = (Job<?, ?>) server.getItemByFullName(jobName);
                if (job == null) {
                    build.setResult(Result.FAILURE);
                    throw new InterruptedException("Job: " + jobName + ", does not exist!");
                } else if (job.isBuilding()) {
                    build.setResult(Result.ABORTED);
                    throw new InterruptedException("Job: " + jobName + ". is currently building!");
                } else if (job.isInQueue()) {
                    build.setResult(Result.ABORTED);
                    throw new InterruptedException("Job: " + jobName + ", is currently in queue!");
                }
                Run<?, ?> last = job.getLastCompletedBuild();
                if (null == last) {
                    build.setResult(Result.ABORTED);
                    throw new InterruptedException("Job: " + jobName + ", has never run!");
                } else if (Result.SUCCESS != last.getResult()) {
                    build.setResult(Result.ABORTED);
                    throw new InterruptedException("Job: " + jobName + ", is not in state SUCCESS!");
                }
                listener.getLogger().println("Done testing Job: " + jobName);
            } catch (ClassCastException e) {
                build.setResult(Result.FAILURE);
                throw new InterruptedException("Object: " + jobName + ", is not a job!");
            }
        }
        return "All requested jobs look good!";
    }

    @Symbol("checkUpStreamJobs")
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public String getDisplayName() {
            return "Sane Upstream Job Chekcer";
        }

        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
