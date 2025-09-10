package io.jenkins.plugins.miscjenkinsinfotools.generator;

import hudson.model.Result;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class FindJobsTests {

    private final String[] list = {"test-x-0", "test-x-1", "test-x-2"};

    private JenkinsRule jenkins;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    void testBadArgs() throws Exception {
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-b-1");
        String pipelineScript = "findJobs(includes: null)";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        jenkins.buildAndAssertStatus(Result.FAILURE, job);
    }

    @Test
    void goodArgs() throws Exception {
        for (String name : list) {
            WorkflowJob job = Jenkins.get().createProject(WorkflowJob.class, name);
            job.setDefinition(new CpsFlowDefinition("echo 'hello world'", true));
            job.save();
        }
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-b-1");
        String pipelineScript = "def noise=findJobs(includes: [/^.*test-x-.*$/]).join(',');echo noise";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.buildAndAssertStatus(Result.SUCCESS, job);
        for (String expectedString : list) {
            jenkins.assertLogContains(expectedString, completedBuild);
        }
    }

    @Test
    void excludeTest() throws Exception {
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-b-1");
        String pipelineScript =
                "def noise=findJobs(includes: [/^.*test-x-.*$/],excludes:[ /^.*x-0$/] ).join(',');echo noise";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.buildAndAssertStatus(Result.SUCCESS, job);
        String bad = "test-x-0";
        jenkins.assertLogNotContains(bad, completedBuild);
    }
}
