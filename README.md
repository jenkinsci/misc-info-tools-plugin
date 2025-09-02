# Misc Jenkins pipeline functions

This project provides useful information expressed through functions in a Jenkinsfile pipeline.  The features include:

  1. Provides a list of job paths based on regular expression white/black lists.
  2. Getting an upstream job build number
  3. Getting the hostname of the build node your job is running on
  4. Shutting down a build if based on the health/build status of mutiple builds
  5. Providing a way to the list of installed plugins and thier versions in Jenkins

## Functions

### UpstreamJobFinder(whitelist,blacklist)

This method provides a why to dynamically list projects using regular expressions and provides the list of project path names as the return value.  The white/black list are both are optional, when provided they are compiled as regular expressions and used to compaire job paths for matches.

```
  ArrayList<String>  UpstreamJobFinder(ArrayList<String> whitelist, ArrayList<String> blacklist)
```

### GetUpsteamBuildNumber(jobName)

This method provides the build numbner of the given job by job path.

```
  int GetUpsteamBuildNumber(String jobName)
```

This method returns the last BUILD_ID of the given job.

### GetCurrentBuildHost()

This method returns the name of the current build host from jenkins.  This method exists because java.net.InetAddress.getLocalHost().getHostName() is restricted by default in a Jenkins pipeline.  This will returns the same value.  Often times this is required when building system packages for redhat or debian, the build node hostname is typically a required argument for the build tools.

```
 String GetCurrentBuildHost()
```

### checkUpStreamJobs(deps)

This method takes a list of job paths and checks for the following.

  1. If any of the jobs are null: throws exception halts the job as FAILED
  2. If any of the jobs are building: thows exception and halts the job as ABORTED
  3. If any of the are not in state success: throws exception and halts the job as ABORTED
  4. If any of the jobs have never built throws an exception and halts the job ABORTED

```
  void checkUpStreamJobs(ArrayList<String> deps)
```

### PluginAudit()

```
  ArrayList<HashMap<String, String>> PluginAudit()
```

## Getting started

Jenkinsfile Example

```
// white list upstream job pattern matches
def whitelist=[/^.*evaluation\/.*_.*$/];

// black list upstream job pattern matches
def blacklist=[/^.*custom_plugin_tests.*$/];

// dynamically find our upstream jobs
def fulldeps=UpstreamJobFinder(
  // array list of patterns to match upstream projects
  whitelist: whitelist, 

  // array list of patterns to exclude upstream projects
  blacklist: blacklist
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
        echo "Last Build was: ${GetUpsteamBuildNumber(env.JOB_NAME)}";
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
        echo "${GetCurrentBuildHost()}"
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
          echo "Whitelist regular expressions:"
          for( String str : whitelist ) {
            echo "  ${str}"
          }
          echo "Blacklist regular expressions:"
          for( String str : blacklist ) {
            echo "  ${str}"
          }
        } 
      }
    }

    stage("Plugin Audit") {
      steps {
        // ensure the csv is cleaned up before we start
        sh 'rm -f plugins.csv';
        script {
          def data='"Plugin","Version"\n';
          for(row in PluginAudit()) {
            // for each plugin, write out its name and version
            data +='"'+row['Plugin'] +'","'+row['Version'] +'"\n';
          }
          writeFile(file: 'PluginAudit.csv', text: data);
        }
      }
    }

    stage("Archive the Plugin Audit") {
      steps {
        archiveArtifacts artifacts: '*.csv', followSymlinks: false
      }
    }

    stage("Node list label dump") {
      steps {
        script {
          def audit=NodeLabelAudit();
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

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

## Notes

To work around various transpartent proxies the following maven.config.

```
  -Dmaven.resolver.transport=wagon
  -Dmaven.wagon.http.ssl.insecure=true
  -Dmaven.wagon.http.ssl.allowall=true
  -Dmaven.wagon.http.ssl.ignore.validity.dates=true
  -Pconsume-incrementals
  -Pmight-produce-incrementals
```

## TO RUN THIS PROJECT LOCALLY

Notes: the -Djetty.port=5000 sets the local listener port!

```
  mvn clean test hpi:hpi
  mvn hpi:run -Dport=5000
```

## To force a new version

In this example 1.0.1 is the new version!

```
  mvn versions:set -DnewVersion=1.0.1
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
