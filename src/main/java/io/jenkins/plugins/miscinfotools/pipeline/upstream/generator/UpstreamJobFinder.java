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

public class UpstreamJobFinder extends Step implements Serializable {

    private static final long serialVersionUID = 1505586566993544821L;
    private ArrayList<Pattern> includes;
    private ArrayList<Pattern> excludes;

    @DataBoundSetter
    public void setIncludes(ArrayList<String> includes) {
        this.includes = new ArrayList<Pattern>();
        if (includes == null) throw new NullPointerException();
        for (String str : includes) {
            this.includes.add(Pattern.compile(str));
        }
    }

    public ArrayList<String> getIncludes() {
        final ArrayList<String> list = new ArrayList<String>();
        for (Pattern regex : includes) {
            list.add(regex.pattern());
        }
        return list;
    }

    public ArrayList<String> getExcludes() {
        final ArrayList<String> list = new ArrayList<String>();
        for (Pattern regex : excludes) {
            list.add(regex.pattern());
        }
        return list;
    }

    @DataBoundSetter
    public void setExcludes(ArrayList<String> excludes) {

        this.excludes = new ArrayList<Pattern>();
        if (excludes == null) return;
        for (String str : excludes) {
            this.excludes.add(Pattern.compile(str));
        }
    }

    @DataBoundConstructor
    public UpstreamJobFinder(ArrayList<String> includes, ArrayList<String> excludes) {
        this.setIncludes(includes);
        this.setExcludes(excludes);
    }

    private ArrayList<String> getList() {
        ArrayList<String> list = new ArrayList<String>();
        if (includes.isEmpty()) return list;
        Jenkins server = Jenkins.getInstanceOrNull();
        // stop here if we have no instance of jenkins
        if (server == null) return list;

        for (Job<?, ?> job : server.getAllItems(Job.class)) {
            boolean isAdmin = false;
            try {
                server.checkPermission(Jenkins.ADMINISTER);
                isAdmin = true;
            } catch (AccessDeniedException e) {
                // nothing to do here
            }
            if (!isAdmin) {
                try {
                    job.checkPermission(Item.READ);
                    job.checkPermission(Item.DISCOVER);
                } catch (AccessDeniedException e) {
                    // skip this job if the user cannot read it and is not allowed to discover it
                    continue;
                }
            }
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
        public String getFunctionName() {
            return "upstreamJobFinder";
        }

        @Override
        public String getDisplayName() {
            return "Upstream list generator";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.emptySet();
        }
    }

    private static class StepExecutionImpl extends SynchronousStepExecution<ArrayList<String>> {

        private static final long serialVersionUID = UpstreamJobFinder.serialVersionUID;
        private final UpstreamJobFinder step;

        StepExecutionImpl(UpstreamJobFinder step, StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected ArrayList<String> run() throws Exception {
            return step.getList();
        }
    }
}
