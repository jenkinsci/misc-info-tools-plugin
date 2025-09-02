package io.jenkins.plugins.miscinfotools.pipeline.audit;

import hudson.model.Label;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Order;
import org.jvnet.hudson.test.JenkinsRule;

public class PluginAuditTest {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    @Order(1)
    public void testEmptyDeps() throws Exception {
        String agentLabel = "my-agent";
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-1");

        jenkins.createOnlineSlave(Label.get(agentLabel));

        String pipelineScript =
                "for(row in PluginAudit()) {echo 'Plugin:  '+row['Plugin']; echo 'Version: '+row['Version']}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        String expectedString = "misc-info-tools";
        jenkins.assertLogContains(expectedString, completedBuild);
    }
}
