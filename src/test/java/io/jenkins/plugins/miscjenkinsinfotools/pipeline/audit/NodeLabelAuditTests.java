package io.jenkins.plugins.miscjenkinsinfotools.pipeline.audit;

import hudson.model.Label;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class NodeLabelAuditTests {

    private JenkinsRule jenkins;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    void testNodeList() throws Exception {
        String agentLabel = "my-agent";
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "node-audit");

        jenkins.createOnlineSlave(Label.get(agentLabel));

        String pipelineScript = "echo \"${getAllLabelsForAllNodes()}\"";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        // WorkflowRun completedBuild =
        jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        // String expectedString = "TelDevBuildTools";
        // jenkins.assertLogContains(expectedString, completedBuild);
    }
}
