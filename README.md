# Autograder

Welcome to the Autograder! This is a project of love, developed over the 3 years I was the lab instructor at the University of Utah for their Data Structures and Algorithms course. 

## Deployment 

_In development_
***
## Development

The autograder was developed as a Java Maven project. Fortunately, the tool dependency is light
* [Java 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html "Java 8 download") or higher
* [Maven 3](https://maven.apache.org/download.cgi "Maven Download")

##### Setup (Eclipse specific)

Clone this repository. In the root directory, run 

    mvn eclipse:eclipse
then

    mvn clean install -DskipTests
    
I'll fix those tests one day

From your Project Manager view in eclipse, right click on empty space, select "Import", and under "General" select "Existing Projects into Workspace." Browse to the root directory of this repo, and the rest should fall into place. 

***

#### Configuration
Please see the [Configuration Documentation](src/dist/README.md)
