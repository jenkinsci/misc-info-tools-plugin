package io.jenkins.plugins.miscjenkinsinfotools.pipeline.netInfo;

import hudson.model.Label;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class GetCurrentBuildHostTests {

    private JenkinsRule jenkins;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    void testEmptyDeps() throws Exception {
        String agentLabel = "my-agent";
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-1");

        jenkins.createOnlineSlave(Label.get(agentLabel));

        String pipelineScript = "echo \"${getCurrentBuildHost()}\"";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        String expectedString = java.net.InetAddress.getLocalHost().getHostName();
        jenkins.assertLogContains(expectedString, completedBuild);
    }
}
