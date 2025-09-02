package io.jenkins.plugins.miscinfotools.generator;

import hudson.model.Result;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Order;
import org.jvnet.hudson.test.JenkinsRule;

public class GenerateUpstreamValuesTest {

    private final String[] list = {"test-x-0", "test-x-1", "test-x-2"};

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    @Order(1)
    public void testBadArgs() throws Exception {
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-b-1");
        String pipelineScript = "UpstreamJobFinder(whitelist: null)";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        jenkins.buildAndAssertStatus(Result.FAILURE, job);
    }

    @Test
    @Order(2)
    public void goodArgs() throws Exception {
        for (String name : list) {
            WorkflowJob job = Jenkins.get().createProject(WorkflowJob.class, name);
            job.setDefinition(new CpsFlowDefinition("echo 'hello world'", true));
            job.save();
        }
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-b-1");
        String pipelineScript = "def noise=UpstreamJobFinder(whitelist: [/^.*test-x-.*$/]).join(',');echo noise";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.buildAndAssertStatus(Result.SUCCESS, job);
        for (String expectedString : list) {
            jenkins.assertLogContains(expectedString, completedBuild);
        }
    }

    @Test
    @Order(3)
    public void excludeTest() throws Exception {
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-b-1");
        String pipelineScript =
                "def noise=UpstreamJobFinder(whitelist: [/^.*test-x-.*$/],blacklist:[ /^.*x-0$/] ).join(',');echo noise";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.buildAndAssertStatus(Result.SUCCESS, job);
        String bad = "test-x-0";
        jenkins.assertLogNotContains(bad, completedBuild);
    }
}
