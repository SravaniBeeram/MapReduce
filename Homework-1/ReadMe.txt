StandAlone mode
---------------------------------------

1.Create a new Maven Project in the IDE.

2.Give cs6240 as the Group ID and wc as the artifact ID.

3.A pom.xml will be created - copy the rest of Joe’s pom.xml into this.

4.src/main/java should contain the WordCount.java file.

5.Create an input folder should contain the input file (provided in the assignment) to run Word Count on.

6.Copy the config folder which has the stand-alone configurations.

7.pom.xml, makefile, src, config and input folders should be in the same project directory

----------------------------------------
Makefile changes:
----------------------------------------
1.Change the hadoop.root to where it is in your local laptop
2.Change the hdfs.user.name to the locall laptop name

Once we have the config,input,src,makefile and pom.xml in the folder run below 2 commands in the 
terminal
1.make switch-standalone
2.make alone

One make alone finishes execution it generates folder and an output folder which are 
deleted and created everytime we run make alone.Target folder contains the .class of WordCount.
Output folder contains the WordCount output with part-r-000x based on reduce tasks.
In standalone we get part-r-00000

-----------------------------------------
AWS EMR
-----------------------------------------
1.Log into console and download the access key and secret access key
2.Launch an instance on EC2 and edit the security groups
3.Install SSH client
4.Install pip and install awscli  - sudo pip install awscli
5.Test if installation is done correctly using aws help
6.COnfigure aws using aws configure.Enter access key and secret,access key, region and output format - json
7.Create default roles using aws emr create-default-roles
8.Following changes are to be made in the makefile

	Change the aws.bucket.name to a preferred bucket name, aws.subnet.id to the subnet id present in the VPC subnet of your console, aws.num.nodes to 2 ( 1 master and 2 worker nodes) , 
aws-region to the region you configured and the aws.instance.type to m1.medium.

9.Add the following plugin in pom.xml
Add the following plugin in pom.xml

<plugin>
   <groupId>org.apache.maven.plugins</groupId>
   <artifactId>maven-jar-plugin</artifactId>
   <version>2.4</version>
   <configuration>
   <archive>
   <manifest>
     <mainClass>org.apache.hadoop.examples.WordCount</mainClass>         
    </manifest>
  </archive>
 </configuration>
</plugin>

This adds the maven plugin and also tells where to find the WordCount class.

10. Create an input directory, in which include the txt file obtained by unzipping the following   link: http://www.ccs.neu.edu/course/cs6240f14/

11.Upload this data on S3 with: make upload-input-aws

12.Run ‘make cloud’ on the terminal to launch your cluster on EMR.

13.Go to the aws console, to the cluster to see how the WordCount is running, it will take about 15-20 minutes to run. Once it’s terminated with steps completed, check the syslog.

14.Go to S3 and in the output you will see part-r-0000*. Download all the part-r-0000*, using ‘make download-output-aws’ which will be the outputs of all the reduce tasks.

----------------------------------------------------------
Multithreading programs
----------------------------------------------------------
1.In Sourcecodefolder ,go to edu.assignment1.weatherdata
2.Folder has below java files
   1.InputFileCopy.java - extracts input file and has calls to other programs
                          AS my laptop was running for only 2 threads i gave thread_count as 2.
                          This can be changed in this program at below location

                          int thread_count = 2;  //no of threads
   2.StationData.java   - Contains all functions to update and retrieve station data
   3.SequentialCal.java - Contains logic for sequential version
   4.NoLock.java        - Contains logic for No-lock version
   5.CoarseLock.java    - Contains logic for Coarse-Lock version
   6.FineLock.java      - Contains logic for Fine-Lock version
   7.NoLock.java        - Contains logic for No-Lock version

Note:: As input file is very large it is not included in the input folder
------
                    

 


