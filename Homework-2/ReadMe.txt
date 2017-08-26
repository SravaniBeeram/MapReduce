
1.Unzip Homework-2

2.In Source COde folder, each folder namely NoCombiner,Combiner,InMapper,SecondarySort have their respective source code, pom.xml and MakeFile

3.Output files folder has NoCombiner,Combiner,InMapper,SecondarySort folders with their respective output folders

4.Syslog files folder has syslog's of NoCombiner,Combiner,InMapper,SecondarySort

I created individual maven projects for each program.Please follow below for each individual project

-----------------------------------------------
StandAlone Mode
-----------------------------------------------

Building and execution of four programs in Standalone mode

1.Create a new Maven Project in the IDE

2.Give GroupId and artifact Id

3.Pom.xml will be created -  add all the required dependencies

4.src/main.java should contain the source code files provided in folder respective for each code
Package for all source code: weatherData.mapReduce

5.Create an input folder which should contain the :
1991.csv for NoCombiner,Combiner,InMapper
1880.csv - 1889.csv for SecondarySort

6.Include the config folder which has stand-alone configurations

7.pom.xml,MakeFile,src,config and input folder should be in same project directory


Next,Run below 2 commands in the terminal
1. make switch-standalone
2.make alone

Once make alone finishes execution we will get a target folder and an output folder. The target and output folders are deleted and created everytime we run make alone.

---------------------------------------------
AWS EMR
---------------------------------------------

1.Log into the console on AWS

2.Add the following plugin in pom.xml to specify the main class location
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

3.On the terminal go to the directory which contains source code, Makefile, pom.xml, input folder and .jar file
Upload input data on S3 with : make upload-input-aws

This uploads the data and create's an aws bucket


4.Then Run make cloud on the terminal to launch cluster on EMR

5.Go to AWS console to check cluster run.Once terminated succesfully check the syslog

6.Go to S3 and output folder in it to check the output

-----------------------------------------------
Programs Structure
-----------------------------------------------

1.Source code can be found in below locations

Combiner      : Combiner/src/main/java/weatherData/mapReduce
NoCombiner    : NoCombiner/src/main/java/weatherData/mapReduce
InMapper      : InMapper/src/main/java/weatherData/mapReduce
SecondarySort : Secondary Sort/src/main/java/weatherData/mapReduce


2.Files Description

Main files

1. NoCombiner.java
2. Combiner.java
3. InMapper.java
4. SecondarySort.java

For normal run (not from the terminal) on the IDE,

Run->Edit Configurations->Applications
Click on the configuration tab on the right side pane
1. Enter your path for your Main class
2. Program arguments : input output (folders from input will be taken for the code and output generated respectively)

Then click on the run button for a successful run

If you get an error “log4j:WARN No appenders could be found for logger (org.apache.hadoop.metrics2.lib.MutableMetricsFactory)” ; include the log4j.properties file in src/main/resources

Add the following into the log4j.properties file:

hadoop.root.logger=DEBUG, console
log4j.rootLogger = DEBUG, console
log4j.appender.console=org.apache.log4j.ConsoleAppender
log4j.appender.console.target=System.out
log4j.appender.console.layout=org.apache.log4j.PatternLayout
log4j.appender.console.layout.ConversionPattern=%d{yy/MM/dd HH:mm:ss} %p %c{2}: %m%n


b.StationData.java
This class is a user defined structure which keeps track of the sum of TMAX and the count, which is in turn used to calculate average

c.For SecondarySort we have CompositeKey.java

This class is a user defined structure for storing the composite key of (stationID, year) used for Secondary sort




