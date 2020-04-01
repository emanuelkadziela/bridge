# bridge
 Contract Bridge Game

In order to run this code, you need at least Java SDK 1.8 and Maven 3.6

Clone the repo, navigate to where the pom.xml file ends up and run "mvn clean install", 
which will compile the code and build an executable jar containing tomcat.

From the same directory, you can now run java -jar target/bridge-0.0.1-SNAPSHOT.jar (backslash if on Windows)
and watch the server start up.

Navigate to locahost:8080 and you should be looking at what we built so far.

Run mvn javadoc:javadoc to generate JavaDocs, which you can find under target/site/apidocs/index.html once generated. 
(I will try to document more and better, so these will have more information over time).