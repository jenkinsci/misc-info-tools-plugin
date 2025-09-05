# CLI Build notes

Notes on how to build this from the command line.

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
The artifact produced by this project is a .hpi file.  The .hpi extension represents a compiled jenkins plugin.

### Building the jenkins plugin

The jenkins plugin can be built by issuing the following command.

```
  mvn hpi:hpi
```

### Production builds should run the following

If you are trying to compile a safe to use/working hpi file please use the following:

```
  mvn clean test verify hpi:hpi
```
