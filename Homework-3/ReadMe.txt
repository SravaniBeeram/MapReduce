Unzip HW3

It contains 

source code folder which contains 8 java code files 

an output folder and contains 2 part-r files for output of run-1 and run-2

a syslog folder containing the syslog files for each run.

a Makefile and a pom.xml which is used to run the code on standalone mode and on AWS.

-- This program generates 13 output folders. 
output-run00 : output-run10 consists of preprocessing
output and 10 iterations output . 
output-DeltaAdd consists output after adding the final delta value
output consists if the final top 100 page ranks

--------------------------------------
STANDALONE MODE
---------------------------------------

Building and execution of PageRank in Standalone mode.

1. Create a new Maven Project in the IDE.

2. Give the Group ID and the artifact ID.

3. A pom.xml will be created, add the required dependencies. 

4. The src/main/java should contain the source code files.
	
5. Create an input folder which should contain the input file (provided in the assignment).
wikipedia-simple-html.bz2

6. Include the config folder 

7. In the Makefile for standalone mode change
hadoop.root = hadoop location on your local file system
jar.name = jar file that will be created
jar.path = target/${jar.name}
job.name = change it to the path of your main file

8. pom.xml, Makefile, src, config and input folders should be in the same project directory.

After the above steps:

Run 2 commands into the terminal of the IDE

1. make switch-standalone.
2. make alone.

Once the make alone finishes execution we will get a target folder and an output folder. 
The target and output folders are deleted and created every time we run make alone.

The target folder contains the .class files and .jar file.

The output folder contains the part-r-0000* files. Number of part-r files depends on number of reducers.

---------------------------------------------
AWS EMR
---------------------------------------------

1. Add the following plugin in pom.xml

	<plugin>
	    <groupId>org.apache.maven.plugins</groupId>
	    <artifactId>maven-jar-plugin</artifactId>
	    <version>2.4</version>
	    <configuration>
		<archive>
		     <manifest>
		         <mainClass>{package_name.name_of_main_class}</mainClass>
		     </manifest>
		</archive>
	    </configuration>
	</plugin>

This tells where to find the main class of the program you want to execute.

2. In the Makefile for AWS execution change

	aws.region = to the region of your aws cluster
	aws.bucket.name = bucket_name you want to create on S3
	aws.subnet.id = subnet_id of your region from VPC subnet list
	aws.input = name of the input folder on S3
	aws.output = name of the output folder on S3
	aws.log.dir = name of the log folder
	aws.num.nodes =  number of worker machines
	aws.instance.type = type of the machine to use.

For run-1 your aws.num.nodes=5 and aws.instance.type=m4.large
For run-2 your aws.num.nodes=10 and aws.instance.type=m4.large

3. On the terminal go to the directory where your source code, Makefile, pom.xml, input folder and .jar file exists.

Upload data on S3 with : make upload-input-aws
Upload the 4 files mentioned in the assignment (full wiki data set for 2006)

This will upload the data in your input folder and will make a bucket on S3 with the name as in your aws.bucket.name (in Makefile)

4. Log into the console on AWS.

5. Run ‘make cloud’ on the terminal to launch your cluster on EMR.

6. Go to the AWS console, to the cluster to see how the Cluster is running, it will take about 70-80 minutes for run1 and 40-50 minutes for run2. Once it’s terminated with steps completed, check the syslog.

7. Go to S3 and in the output you will see a part-r-0000* file which will have your final 100 pages and page ranks. Download that part-r-0000*.

The commands executed from step 3-7 should all be executed in that same directory on the terminal.

———————————————————————————————————————————————————
DIRECTORY STRUCTURE OF THE PROGRAMS
———————————————————————————————————————————————————
For the Intellij IDE

1. File Structure

a. Main Files: 

1. pageRankJob.java: Class from where all jobs are called

2. pageData.java: This class stores details about the page: page names, link page names and page rank

3. preProcessing.java : This performs first pre-processing job. In this map reduce job we parse the bz2 file , get page names with their link pagenames.

4. pageRankCal.java: The page ranks for 10 iterations are calculated.

5. deltaAdd.java: The page rank for the final iteration. Since delta updation of page ranks is performed in the map task in the previous file, we need an extra map task for the final correct page ran of the 10th iteration. There is no  reduce task in this program, so by default the reduce tasks will be set to 1 and identity reducer will run.

6. TopK.java: Class which calculates the top 100 page ranks and their pages.

7. Bz2WikiParser.java: Parsing the compressed Bz2 file. Called from PageRankPreProcess.java

8. HTMLParser.java : Class which parses the file into human readable format. This is not a part of the main code and just for the user to read and determine the contents of the XML. 


For a normal run (not from the terminal) on the IDE,

Run->Edit Configurations->Applications
Click on the configuration tab on the right side pane
1. Enter your path for your Main class
2. Program arguments : input output (folders from input will be taken for the code and output generated respectively)

Then click on the run button for a successful run

If you get an error “log4j:WARN No appenders could be found for logger (org.apache.hadoop.metrics2.lib.MutableMetricsFactory)” ; include the log4j.properties file in src/main/resources/

Add the following into the log4j.properties file:

hadoop.root.logger=DEBUG, console
log4j.rootLogger = DEBUG, console
log4j.appender.console=org.apache.log4j.ConsoleAppender
log4j.appender.console.target=System.out
log4j.appender.console.layout=org.apache.log4j.PatternLayout
log4j.appender.console.layout.ConversionPattern=%d{yy/MM/dd HH:mm:ss} %p %c{2}: %m%n⁠⁠⁠⁠
