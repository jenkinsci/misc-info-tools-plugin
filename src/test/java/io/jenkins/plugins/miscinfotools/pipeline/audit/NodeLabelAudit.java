package io.jenkins.plugins.miscinfotools.pipeline.audit;

import hudson.model.Label;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Order;
import org.jvnet.hudson.test.JenkinsRule;

public class NodeLabelAudit {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    @Order(1)
    public void testNodeList() throws Exception {
        String agentLabel = "my-agent";
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "node-audit");

        jenkins.createOnlineSlave(Label.get(agentLabel));

        String pipelineScript = "echo \"${NodeLabelAudit()}\"";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        // WorkflowRun completedBuild =
        jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        // String expectedString = "TelDevBuildTools";
        // jenkins.assertLogContains(expectedString, completedBuild);
    }
}
