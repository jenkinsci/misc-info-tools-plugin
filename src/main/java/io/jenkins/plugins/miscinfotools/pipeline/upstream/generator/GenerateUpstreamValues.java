package io.jenkins.plugins.miscinfotools.pipeline.upstream.generator;

import hudson.Extension;
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

public class GenerateUpstreamValues extends Step implements Serializable {

    private static final long serialVersionUID = 1505586566993544821L;
    private ArrayList<Pattern> match;
    private ArrayList<Pattern> exclude;

    @DataBoundSetter
    public void setWhitelist(ArrayList<String> whitelist) {
        for (String str : whitelist) {
            this.match.add(Pattern.compile(str));
        }
    }

    @DataBoundSetter
    public void setBlacklist(ArrayList<String> blacklist) {
        for (String str : blacklist) {
            this.exclude.add(Pattern.compile(str));
        }
    }

    @DataBoundConstructor
    public GenerateUpstreamValues() {
        this.match = new ArrayList<Pattern>();
        this.exclude = new ArrayList<Pattern>();
    }

    private ArrayList<String> getList() {
        ArrayList<String> list = new ArrayList<String>();
        if (match.isEmpty()) return list;
        Jenkins server = Jenkins.getInstanceOrNull();
        // stop here if we have no instance of jenkins
        if (server == null) return list;

        for (Job<?, ?> job : server.getAllItems(Job.class)) {
            String name = job.getFullName();
            boolean matchOk = false;
            for (Pattern ok : match) {
                if (ok.matcher(name).matches()) {
                    matchOk = true;
                    break;
                }
            }

            if (!matchOk) continue;

            for (Pattern notOk : exclude) {
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
            return "UpstreamJobFinder";
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

        private static final long serialVersionUID = GenerateUpstreamValues.serialVersionUID;
        private final GenerateUpstreamValues step;

        StepExecutionImpl(GenerateUpstreamValues step, StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected ArrayList<String> run() throws Exception {
            return step.getList();
        }
    }
}
