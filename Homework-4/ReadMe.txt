
Unzip "CS6240_Sravani_Beeram_HW_4”

There is a source code folder. Inside that go to /src/main/code which has a package mapReduce containing the source code. There will also be a Makefile and a pom.xml inside the source code folder.The Makefile and a pom.xml is used to run the code on standalone mode and on AWS.

There is output folder. This contains 1 part file for output of run-1(1 Master and 5 workers) and run-2(1 Master and 10 workers), each

There is an output files for report folder. This contains output files for final question asked in the report

There is a stderr folder containing the stderr files for each run.


—————————————————
STANDALONE MODE
—————————————————
Building and execution of PageRank in Standalone mode.

1. Create a new Maven Project in the IDE.

2. Give the Group ID and the artifact ID.

3. A pom.xml will be created, add the required dependencies. 

4. The src/main/code contain the source code files.
	
5. Create an input folder which should contain the input file (provided in the assignment)

6. Include the config folder which has the stand-alone configurations: core-site.xml, hdfs-site.xml, mapred-site.xml, yarn-site.xml. 

7. In the Makefile for standalone mode change

spark.root = spark location on your local system
jar.name = jar file that will be created
jar.path = target/${jar.name}
job.name = change it to the path of your main file

Updated alone command (used spark-submit) : ${spark.root}/bin/spark-submit --class ${job.name} --master local[*] ${jar.path} ${local.input} ${local.output}

8. pom.xml, Makefile, src, config and input folders should be in the same project directory.

After the above steps:
Run 2 commands into the terminal of the IDE
1. make switch-standalone.

2. make alone.

Once the make alone finishes execution we will get a target folder and an output folder. The target and output folders are deleted and created every time we run make alone.

The target folder contains the .class files and .jar file.

We will get an output folder which will have the top 100 records with pages and page ranks in descending order.

—————————————————
AWS EMR
—————————————————

1. IN the pom.xml I have added scald-maven-plugin, replaced maven-shade-plugin with maven-assembly-plugin, replaced hadoop dependencies with scala and spark dependencies

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

Updated the cloud command (applicationsName, steps and added configurations option):

--applications Name=Spark \
--steps '[{"Name":"Spark Program", "Args":["--class", "${job.name}", "--master", "yarn", "--deploy-mode", "cluster", "s3://${aws.bucket.name}/${jar.name}", "s3://${aws.bucket.name}/${aws.input}","s3://${aws.bucket.name}/${aws.output}"],"Type":"Spark","Jar":"s3://${aws.bucket.name}/${jar.name}","ActionOnFailure":"TERMINATE_CLUSTER"}]' \
--log-uri s3://${aws.bucket.name}/${aws.log.dir} \

3. Update setMaster("local") to setMaster("yarn")

4.On the terminal go to the directory where your source code, Makefile, pom.xml, input folder and .jar file exists. In the empty input folder add the 4 files mentioned in the assignment (full wiki data set for 2006).
Upload data on S3 with : make upload-input-aws

This will upload the data in your input folder and will make a bucket on S3 with the name as in your aws.bucket.name (in Makefile)

4. Log into the console on AWS.

5. Run ‘make cloud’ on the terminal to launch your cluster on EMR.

6. Go to the AWS console, to the cluster to see how the Cluster is running. Once it’s terminated with steps completed, check the syslog.

7. Go to S3 and in the output folder you will see a part-0000 file which will have your final 100 pages and page ranks. Download that part-0000.

The commands executed from step 3-7 should all be executed in that same directory on the terminal.

———————————————————————————————————————————————————
DIRECTORY STRUCTURE OF THE PROGRAMS
———————————————————————————————————————————————————
For the Intellij IDE

1. File Structure : PageRankScala/src/main/code

a. Main Files: 

1. Bz2WikiParser.java: Parsing the compressed Bz2 file and returning a string of page names and outlines (for each line) separated by a delimiter. It is called from PageRankMain.scala

2. pageRank.scala: The main file, where an object of PageRankMain is created, and all preprocessing, calculating page ranks for 10 iterations and top K calculation is done here.

For a normal run (not from the terminal) on the IDE,

Run->Edit Configurations->Applications
Click on the configuration tab on the right side pane
1. Enter your path for your Main class
2. Program arguments : input output (folders from input will be taken for the code and output generated respectively)

Then click on the run button for a successful run

The output folder will have the the final top k output (output)⁠⁠⁠⁠
