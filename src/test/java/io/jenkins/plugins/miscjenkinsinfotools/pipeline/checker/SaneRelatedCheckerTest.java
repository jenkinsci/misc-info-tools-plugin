package io.jenkins.plugins.miscjenkinsinfotools.pipeline.checker;

import hudson.model.Label;
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
class SaneRelatedCheckerTest {

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

        String pipelineScript = "node { relatedJobChecks([]);}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        String expectedString = "All requested jobs look good!";
        jenkins.assertLogContains(expectedString, completedBuild);
    }

    @Test
    void testJobNeverRan() throws Exception {
        Jenkins.get().createProject(WorkflowJob.class, "test-1");
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-2");

        String pipelineScript =
                """
                node {
                  relatedJobChecks deps: ['test-1'],
                    // all of these are optional ( default is always true )
                    jobExists: true,            // aborts if the a job has never run
                    isBuilding: true,           // aborts if the job is building
                    inQueue: true,              // aborts if the job is in que
                    hasRun: true,               // aborts if the a job has never run
                    isSuccess: true             // aborts if the last jobs is not in a state of success
                }""";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.buildAndAssertStatus(Result.ABORTED, job);
        String expectedString = "test-1, has never run!";
        jenkins.assertLogContains(expectedString, completedBuild);
    }

    @Test
    void testJobDoesNotExist() throws Exception {

        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-2");
        String pipelineScript = "node { relatedJobChecks(['NoExists']);}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.buildAndAssertStatus(Result.FAILURE, job);
        String expectedString = "Job: NoExists, does not exist!";
        jenkins.assertLogContains(expectedString, completedBuild);
    }

    @Test
    void testEmptyDepsNoNode() throws Exception {
        String agentLabel = "my-agent";
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-1");

        jenkins.createOnlineSlave(Label.get(agentLabel));

        String pipelineScript = "relatedJobChecks([]);";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        String expectedString = "All requested jobs look good!";
        jenkins.assertLogContains(expectedString, completedBuild);
    }
}
