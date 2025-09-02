package io.jenkins.plugins.miscinfotools.pipeline.audit;

import hudson.Extension;
import hudson.PluginWrapper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

public class PluginAudit extends Step implements Serializable {

    private static final long serialVersionUID = 1505586566993544821L;

    @DataBoundConstructor
    public PluginAudit() {}

    private ArrayList<HashMap<String, String>> getList() {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        Jenkins server = Jenkins.getInstanceOrNull();
        if (server == null) return list;
        for (PluginWrapper plugin : Jenkins.get().pluginManager.getPlugins()) {
            HashMap<String, String> row = new HashMap<String, String>();
            list.add(row);
            row.put("Plugin", plugin.getDisplayName());
            row.put("Version", plugin.getVersion());
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
            return "PluginAudit";
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

    private static class StepExecutionImpl extends SynchronousStepExecution<ArrayList<HashMap<String, String>>> {

        private static final long serialVersionUID = PluginAudit.serialVersionUID;
        private final PluginAudit step;

        StepExecutionImpl(PluginAudit step, StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected ArrayList<HashMap<String, String>> run() throws Exception {
            return step.getList();
        }
    }
}
