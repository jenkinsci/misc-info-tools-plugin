# Misc Jenkins pipeline functions

This plugin's functionality is meant to make it a lot easier to trigger dynamic related checks when it comes to massive numbers of multi branch pipelines. When you have jobs with slightly different names coming and going in the thousands, having to write multiple script rest calls gets rather cumbersome. The ability to use just 2 key words to sanity check the related jobs is very handy.

This project provides useful information expressed through functions in a Jenkinsfile pipeline.  The features include:

  1. Provides a list of job paths based on regular expression includes/excludes lists.
  2. Getting an related job build number
  3. Getting the hostname of the build node your job is running on
  4. Shutting down a build if based on the health/build status of multiple builds
  5. Providing a way to get the label of the node this job ran on

# Precautions when using findJobs(includes,excludes)

The findJobs(includes,excludes) can expose paths to projects your user user does not have access to. It is recommended that the "authorize-project" plugin be installed and configured to prevent unrestricted access to projects.

The following considerations should be taken into account when installing this plugin:

  1. Any User who can configure a pipeline can by default see a listing of all jobs in Jenkins that the system user can read.
  2. Any User who can commit changes or create Jenkinsfile from SCM can by default list all the projects the system user can read.

It is strongly recommended that the "authorize-project" be installed and configured before this plugin is installed.

## Functions

### findJobs(includes,excludes)

This method provides a way to dynamically list projects using regular expressions and provides the list of project path names as the return value.  The includes/excludes list are both are optional, when provided they are compiled as regular expressions and used to compare job paths for matches.

```
  ArrayList<String> findJobs(ArrayList<String> includes, ArrayList<String> excludes)
```

### getLastSuccessfulBuildNumber(job)

This method provides the build number of the given job by job path.

```
  int getLastSuccessfulBuildNumber(String job)
```

This method returns the last BUILD_ID of the given job.

### getCurrentBuildHost()

This method returns the name of the current build host from Jenkins.  This method exists because of the dind or Docker inside docker hostname delema.. and the "Who's hostname is this really problem it creates". Often times this is required when building system packages for red-hat or Debian, the build node hostname is typically a required argument for the build tools.  This will return the same value. 
Notes about this method:

This method has 2 use cases:

  1. In the pre-pipeline it will return the hostname of the Jenkins controller
  2. When running in a stage on an agent it will return the hostname of that agent.

```
 String getCurrentBuildHost()
```

### relatedJobChecks(deps)

This method exists because when builds can have multiple related triggers, it is nice to make sure all related job are working, exist and are healthy, along with reducing Jenkins cluster load.  This functionality grows in value when a job can add or remove new related triggers on the fly.

Arguments: Takes a list of job paths along with an optional set of boolean flags

| Argument | Required | type | Description |
| :----: | :-------: | :---: | :---: |
| deps | yes | ArrayList of String | Each element should be a job path to check |
| jobExists | no | boolean |  marks this job as failed, if any of the jobs listend in deps do not exists |
| isBuilding | no | boolean | aborts this job, if any of the of the jobs listed in deps jobs are building |
| inQueue | no | boolean | aborts this job, if any of the jobs listed in deps are in the queue |
| hasRun | no | boolean | aborts this job if, this job if any job listed in deps has never run has never run |
| isSuccess | no | boolean | aborts this job if, any of the last jobs are not is not in a state of success |

Default use case

```
  relatedJobChecks(['path/to/job','path/to/another/job'])
```

Example showing all options

```
  relatedJobChecks deps: ['path/to/job','path/to/another/job'], 
    // all of these are optional ( default is always true )
    jobExists:  true, // fails if the a job has never run
    isBuilding: true, // aborts if the job is building
    inQueue:    true, // aborts if the job is in que
    hasRun:     true, // aborts if the a job has never run
    isSuccess:  true  // aborts if the last jobs is not in a state of success
```

### getAllLabelsForAllNodes() 

This method provides the node to list of node labels.

```
  HashMap<String,ArrayList<String>> getAllLabelsForAllNodes()
```

## Example

Jenkinsfile Example with all methods included.

```
// includes list related job pattern matches
def includes=[/^.*evaluation\/.*_.*$/];

// excludes list related job pattern matches
def excludes=[/^.*custom_plugin_tests.*$/];

// dynamically find our related jobs
def fulldeps=findJobs(
  // array list of patterns to match related projects
  includes: includes, 

  // array list of patterns to exclude related projects
  excludes: excludes
);

// Dynamically rebuild our relatedProjects
def deplist=fulldeps.join(',');

pipeline {
  triggers {
    // Apply our dynamic list of related projects to watch
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
        relatedJobChecks(fulldeps);
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
          echo "Discovered related jobs:"
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

This project is maintained in the following [Github Project](https://github.com/akalinux/misc-jenkins-info-tools-plugin).
Please create a pull request to submit changes! 

Note, this will need to be changed if/when this is hosted by jenkins.

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

