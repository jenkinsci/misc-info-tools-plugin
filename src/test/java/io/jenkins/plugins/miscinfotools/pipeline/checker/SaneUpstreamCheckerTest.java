package io.jenkins.plugins.miscinfotools.pipeline.checker;

import hudson.model.Label;
import hudson.model.Result;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Order;
import org.jvnet.hudson.test.JenkinsRule;

public class SaneUpstreamCheckerTest {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    @Order(1)
    public void testEmptyDeps() throws Exception {
        String agentLabel = "my-agent";
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-1");

        jenkins.createOnlineSlave(Label.get(agentLabel));

        String pipelineScript = "node { checkUpStreamJobs([]);}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        String expectedString = "All requested jobs look good!";
        jenkins.assertLogContains(expectedString, completedBuild);
    }

    @Test
    @Order(2)
    public void testJobNeverRan() throws Exception {
        Jenkins.get().createProject(WorkflowJob.class, "test-1");
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-2");
        String pipelineScript = "node { checkUpStreamJobs(['test-1']);}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.buildAndAssertStatus(Result.ABORTED, job);
        String expectedString = "test-1, has never run!";
        jenkins.assertLogContains(expectedString, completedBuild);
    }

    @Test
    @Order(3)
    public void testJobDoesNotExist() throws Exception {

        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-2");
        String pipelineScript = "node { checkUpStreamJobs(['NoExists']);}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.buildAndAssertStatus(Result.FAILURE, job);
        String expectedString = "Job: NoExists, does not exist!";
        jenkins.assertLogContains(expectedString, completedBuild);
    }

    @Test
    @Order(4)
    public void testEmptyDepsNoNode() throws Exception {
        String agentLabel = "my-agent";
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-1");

        jenkins.createOnlineSlave(Label.get(agentLabel));

        String pipelineScript = "checkUpStreamJobs([]);";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        String expectedString = "All requested jobs look good!";
        jenkins.assertLogContains(expectedString, completedBuild);
    }
}
