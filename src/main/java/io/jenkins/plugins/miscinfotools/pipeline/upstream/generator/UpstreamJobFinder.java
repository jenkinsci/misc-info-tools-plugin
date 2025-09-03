package io.jenkins.plugins.miscinfotools.pipeline.upstream.generator;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.Job;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.springframework.security.access.AccessDeniedException;
// import org.springframework.security.core.Authentication;

public class UpstreamJobFinder extends Step implements Serializable {

    private static final long serialVersionUID = 1505586566993544821L;
    private ArrayList<String> includes;
    private ArrayList<String> excludes;

    @DataBoundSetter
    public void setIncludes(ArrayList<String> includes) {
        if (includes == null) throw new NullPointerException();
        this.includes = includes;
    }

    public ArrayList<String> getIncludes() {
        return this.includes;
    }

    public ArrayList<String> getExcludes() {
        return this.excludes;
    }

    @DataBoundSetter
    public void setExcludes(ArrayList<String> excludes) {
        if (excludes == null) {
            this.excludes = new ArrayList<String>();
            return;
        }
        this.excludes = excludes;
    }

    @DataBoundConstructor
    public UpstreamJobFinder(ArrayList<String> includes, ArrayList<String> excludes) {
        this.setIncludes(includes);
        this.setExcludes(excludes);
    }

    // private ArrayList<String> getList(@SuppressWarnings("rawtypes") Job currentJob) {
    private ArrayList<String> getList() {
        ArrayList<String> list = new ArrayList<String>();
        // if (includes.isEmpty() || currentJob == null) return list;
        if (includes.isEmpty()) return list;
        // TriggeringUsersAuthorizationStrategy t = new TriggeringUsersAuthorizationStrategy();
        ArrayList<Pattern> includes = new ArrayList<Pattern>();
        ArrayList<Pattern> excludes = new ArrayList<Pattern>();
        for (String str : this.includes) {
            if (str.equals("")) continue;
            includes.add(Pattern.compile(str));
        }
        if (includes.isEmpty()) return list;
        for (String str : this.excludes) {
            if (str.equals("")) continue;
            excludes.add(Pattern.compile(str));
        }

        Jenkins server = Jenkins.getInstanceOrNull();
        // stop here if we have no instance of jenkins
        if (server == null) return list;

        for (Job<?, ?> job : server.getAllItems(Job.class)) {

            String name = job.getFullName();
            boolean matchOk = false;
            for (Pattern ok : includes) {
                if (ok.matcher(name).matches()) {
                    matchOk = true;
                    break;
                }
            }

            if (!matchOk) continue;

            for (Pattern notOk : excludes) {
                if (notOk.matcher(name).matches()) {
                    matchOk = false;
                    break;
                }
            }
            if (!matchOk) continue;
            // Authentication authOk = t.authenticate(job,job.getQueueItem());
            // if (authOk == null || !authOk.isAuthenticated()) continue;

            try {
                job.checkPermission(Item.READ);
            } catch (AccessDeniedException e) {
                // skip this job if the user cannot read it
                continue;
            }
            list.add(name);
        }
        return list;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {

        return new StepExecutionImpl(this, context);
    }

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public String getDisplayName() {
            return "Upstream list generator";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.emptySet();
        }

        @Override
        public String getFunctionName() {
            return "upstreamJobFinder";
        }
    }

    private static class StepExecutionImpl extends SynchronousStepExecution<ArrayList<String>> {

        private static final long serialVersionUID = UpstreamJobFinder.serialVersionUID;
        private final UpstreamJobFinder step;
        // @SuppressWarnings("rawtypes")
        // private Job currentJob=null;

        StepExecutionImpl(UpstreamJobFinder step, StepContext context) {
            super(context);
            // try {
            // @SuppressWarnings("rawtypes")
            // Run run = context.get(Run.class);
            // currentJob = run.getParent();
            // } catch (IOException e) {
            // not worried about this
            // } catch (InterruptedException e) {
            // not worried about this
            // }
            this.step = step;
        }

        @Override
        protected ArrayList<String> run() throws Exception {
            // return step.getList(currentJob);
            return step.getList();
        }
    }
}
