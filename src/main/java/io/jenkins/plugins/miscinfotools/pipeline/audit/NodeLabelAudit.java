package io.jenkins.plugins.miscinfotools.pipeline.audit;

import hudson.Extension;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;
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

public class NodeLabelAudit extends Step implements Serializable {

    private static final long serialVersionUID = -4036292666957971360L;

    @DataBoundConstructor
    public NodeLabelAudit() {}

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new StepExecutionImpl(context);
    }

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "NodeLabelAudit";
        }

        @Override
        public String getDisplayName() {
            return "Node HasMap Listig tool!";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.emptySet();
        }
    }

    private static class StepExecutionImpl extends SynchronousStepExecution<HashMap<String, ArrayList<String>>> {

        private static final long serialVersionUID = NodeLabelAudit.serialVersionUID;

        StepExecutionImpl(StepContext context) {
            super(context);
        }

        @Override
        protected HashMap<String, ArrayList<String>> run() throws Exception {
            Jenkins server = Jenkins.getInstanceOrNull();
            HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();

            if (server == null) return result;
            for (Node node : server.getNodes()) {
                String name = node.getNodeName();
                ArrayList<String> list = null;
                if (result.containsKey(name)) {
                    list = result.get(name);
                } else {
                    list = new ArrayList<String>();
                    result.put(name, list);
                }
                for (LabelAtom label : node.getAssignedLabels()) {
                    String str = label.getName();
                    if (str.equals(name)) continue;
                    list.add(str);
                }
            }

            return result;
        }
    }
}
