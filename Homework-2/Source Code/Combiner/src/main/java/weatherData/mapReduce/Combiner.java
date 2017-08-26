package weatherData.mapReduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * Created by sravani on 2/3/17.
 */

public class Combiner {

    //Mapper class
    public static class TokenizerMapper extends Mapper<Object,Text,Text,StationData>{

        private Text word = new Text();

        //emits stationId as key and StationData as value
        public void map(Object key,Text value,Context context) throws IOException,InterruptedException
        {
            String[] data = value.toString().split("\n");

            for(String rec : data){

                //process only records which contain TMIN ot TMAX
                if(rec.contains("TMIN") || rec.contains("TMAX")){

                    String[]  recData = rec.split(",");

                    if (recData[2].equals("TMIN")){

                         word.set(recData[0]);
                         double temp = Double.parseDouble(recData[3]);
                         //emit key(stationId) and value(StationData)
                         context.write(word,new StationData(temp,0,1,0));

                    }else if(recData[2].equals("TMAX")){

                        word.set(recData[0]);
                        double temp = Double.parseDouble(recData[3]);
                        //emit key(stationId) and value(StationData)
                        context.write(word,new StationData(0,temp,0,1));
                    }
                }
            }
        }
    }


    //combiner class
    public static class AvgCombiner extends Reducer<Text,StationData,Text,StationData>{

        public void reduce(Text key,Iterable<StationData> values,Context context) throws IOException,InterruptedException
        {
            double minTempSum = 0;
            double maxTempSum = 0;
            int minCount = 0;
            int maxCount = 0;

            // to retrieve temperature and count for a particular key (station id)
            for(StationData s :values){

                minTempSum += s.getMinTemp();
                minCount += s.getMinCount();

                maxTempSum += s.getMaxTemp();
                maxCount += s.getMaxCount();
            }


            StationData newData = new StationData(minTempSum,maxTempSum,minCount,maxCount);

            //emit same type as input
            context.write(key,newData);

        }
    }

    //Reducer outputs Text as key with required details and Nullwritable as value
    public  static class AvgReducer extends Reducer<Text,StationData,Text,NullWritable> {

        private  NullWritable res = NullWritable.get();

        public void reduce(Text key,Iterable<StationData> values,Context context) throws IOException,InterruptedException {

            double minTempSum = 0;
            double maxTempSum = 0;
            int minCount = 0;
            int maxCount = 0;

            //To retrieve temperature and count for key (i.e stationId)
            for (StationData s : values) {

                minTempSum += s.getMinTemp();
                minCount += s.getMinCount();

                maxTempSum += s.getMaxTemp();
                maxCount += s.getMaxCount();
            }

            String output;

            //if data doesn't contain either min temp or max temp
            if (minCount == 0) {
                output = key.toString() + "," + " None" + "," + (maxTempSum / maxCount);

            } else if (maxCount == 0) {
                output = key.toString() + "," + (minTempSum / minCount) + "," + "None";

            } else {
                output = key.toString() + "," + (minTempSum / minCount) + "," + (maxTempSum / maxCount);

            }


            context.write(new Text(output), res);
        }

    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "combiner");
        job.setJarByClass(Combiner.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(AvgCombiner.class);
        job.setReducerClass(AvgReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(StationData.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        Path outPath = new Path(args[1]);
        FileOutputFormat.setOutputPath(job, outPath);
        outPath.getFileSystem(conf).delete(outPath,true);
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

}
