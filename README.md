# Misc Jenkins pipeline functions

This plugin's functionality is meant to make it a lot easier to trigger dynamic upstream checks when it comes to massive numbers of multi branch pipelines. When you have jobs with slightly different names coming and going in the thousands, having to write multiple script rest calls gets rather cumbersome. The ability to use just 2 key words to sanity check the upstream jobs is very handy.

This project provides useful information expressed through functions in a Jenkinsfile pipeline.  The features include:

  1. Provides a list of job paths based on regular expression white/black lists.
  2. Getting an upstream job build number
  3. Getting the hostname of the build node your job is running on
  4. Shutting down a build if based on the health/build status of mutiple builds
  5. Providing a way to get the label of the node this job ran on


# Precautions when using findJobs(includes,excludes)

The upstreamJobFinder(includes,excludes) can expose paths to projects your user user does not have access to. It is recommended that the "authorize-project" plugin be installed and configured to prevent unrestricted access to projects.

The following considerations should be taken into account when installing this plugin:

  1. Any User who can configure a pipeline can by default see a listing of all jobs in jenkins that the system user can read.
  2. Any User who can commit changes or create Jenkinsfile from SCM can by default list all the projects the system user can read.

It is strongly recommended that the "authorize-project" be installed and configured before this plugin is installed.

## Functions

### findJobs(includes,excludes)

This method provides a way to dynamically list projects using regular expressions and provides the list of project path names as the return value.  The white/black list are both are optional, when provided they are compiled as regular expressions and used to compaire job paths for matches.

```
  ArrayList<String> findJobs(ArrayList<String> includes, ArrayList<String> excludes)
```

### getLastSuccessfulBuildNumber(job)

This method provides the build numbner of the given job by job path.

```
  int getLastSuccessfulBuildNumber(String job)
```

This method returns the last BUILD_ID of the given job.

### getCurrentBuildHost()

This method returns the name of the current build host from jenkins.  This method exists because of the dind or Docker inside docker hostname delema.. and its "Who's hostname is this really delema" and sine java.net.InetAddress.getLocalHost().getHostName() is restricted by default in a Jenkins pipeline.  Often times this is required when building system packages for redhat or debian, the build node hostname is typically a required argument for the build tools.  This will return the same value.

Notes about this method:

This method has 2 use cases:

  1. In the pre-pipeline it will return the hostname of the Jenkins controler
  2. When running in a stage on an agent it will return the hostname of that agent.

```
 String getCurrentBuildHost()
```

### checkUpStreamJobs(deps,isBuilding)

This method exists because when builds can have multiple upstream triggers, it is nice to make sure all upstream job are working, exist and are healthy, along with reducing jenkins cluster load.  This functionality grows in value when a job can add or remove new upstream triggers on the fly.

Arguments: Takes a list of job paths along with an optional "isBuilding" flag and checks for the following.

  1. If any of the jobs are null: throws exception halts the job as FAILED
  2. If any of the jobs are building: thows exception and halts the job as ABORTED
  3. If any of the are not in state success: throws exception and halts the job as ABORTED
    This can be cahnged with the isBuilding flag.  
      When set to true (default false) 
  4. If any of the jobs have never built throws an exception and halts the job ABORTED


Default use case

```
  checkUpStreamJobs(['Node-Label-Audit'])
```

To allow concurrent build checking.

```
  checkUpStreamJobs deps: ['Node-Label-Audit'], isBuilding:true
```

### getAllLabelsForAllNodes() 

This method provides the node to list of node labels.

```
  HashMap<String,ArrayList<String>> getAllLabelsForAllNodes()
```

## Example

Jenkinsfile Example with all methods incldued.

```
// white list upstream job pattern matches
def includes=[/^.*evaluation\/.*_.*$/];

// black list upstream job pattern matches
def excludes=[/^.*custom_plugin_tests.*$/];

// dynamically find our upstream jobs
def fulldeps=upstreamJobFinder(
  // array list of patterns to match upstream projects
  includes: includes, 

  // array list of patterns to exclude upstream projects
  excludes: excludes
);

// Dynamically rebuild our upstreamProjects
def deplist=fulldeps.join(',');

pipeline {
  triggers {
    // Apply our dynamic list of upstream projects to watch
    upstream(upstreamProjects: deplist, threshold: hudson.model.Result.SUCCESS)
  }

  agent any

  stages {
    stage("Job Check") {
      steps {
        echo "Our Job is: ${env.JOB_NAME}";
        echo "Last Build was: ${getLastSuccessfulBuildNumber(env.JOB_NAME)}";
      }
    }

    stage("Job sanity check") {
      steps {
        // evaluate if this job should be running at all
        checkUpStreamJobs(fulldeps);
      }
    }

    stage("get current buld host") {
      steps {
        echo "${getCurrentBuildHost()}"
      }
    }

    stage("generator output") {
      steps {
        script {
          echo "Deplist string: \n  ${deplist}";
          echo "Discovered upstream jobs:"
          for( String str : fulldeps ) {
            echo "  ${str}"
          }
          echo "Include regular expressions:"
          for( String str : includes ) {
            echo "  ${str}"
          }
          echo "Exclude regular expressions:"
          for( String str : excludes ) {
            echo "  ${str}"
          }
        } 
      }
    }

    stage("Node list label dump") {
      steps {
        script {
          def audit=getAllLabelsForAllNodes();
          def keys=audit.keySet();

          for( String key : keys ) {
            echo "Node: ${key}"
            for( String label : audit[key]) {
              echo "  Label: ${label}"
            }
          }
        }
      }
    }
  }
}

```

## Contributing

This project is maintained in the following [Github Project](https://github.com/akalinux/misc-jenkins-info-tools).
Please create a pull request to submit changes! 

Note, this will need to be changed if/when this is hosted by jenkins.

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

## TO RUN THIS PROJECT LOCALLY

If you want to run/test a local jenkins with this plugin, here are the steps.  Notes: the -Djetty.port=5000 sets the local listener port!

```
  mvn clean test hpi:hpi
  mvn hpi:run -Dport=5000
```

## Cleaning the project

To clean the project ( revert to a fresh build status ).

```
  mvn clean
```

## Running the unit tests

To execute the unit tests

```
  mvn test 
```
The artifact produced by this project is a .hpi file.  The .hpi extention represents a compiled jenkins plugin.

### Building the jenkins plugin

The jenkins plugin can be built by issuing the following command.

```
  mvn hpi:hpi
```

### Production builds should run the follwowing

If you are trying to compile a safe to use/working hpi file please use the following:

```
  mvn clean test verify hpi:hpi
```
