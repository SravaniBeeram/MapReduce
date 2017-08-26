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
public class NoCombiner {

    public static class TokenizerMapper
            extends Mapper<Object,Text,Text,StationData> {

        private Text word = new Text();

        // Map function which emits StationId as key and customized StationData class as value
        public void map(Object Key,Text value,Context context) throws IOException,InterruptedException
        {

            //Split the input based on new line
            String[] data = value.toString().split("\n");

            for(String rec : data){

                //considering only the records that contain  TMIN or TMAX
                if(rec.contains("TMIN") || rec.contains("TMAX")){

                    String[] recData =  rec.split(",");


                    if(recData[2].equals("TMIN")){

                        //stationId as key and min temp as value
                        word.set(recData[0]);
                        double temp = Double.parseDouble(recData[3]);

                        //emitting key-value pair
                        context.write(word,new StationData(temp,0,true));

                    }
                    else if(recData[2].equals("TMAX")) {


                        word.set(recData[0]);
                        double temp = Double.parseDouble(recData[3]);

                        //emitting corresponding key-value pair
                        context.write(word,new StationData(0, temp,false));

                    }
                }
            }
        }

    }

    //Reducer class which takes input as Text,StationData and emits Text as key with details and NullWritable as value
    public static class AvgReducer extends Reducer<Text,StationData,Text,NullWritable> {

        private NullWritable res = NullWritable.get();


        public void reduce(Text key, Iterable<StationData> values, Context context) throws IOException,InterruptedException{

            double minTempSum=0;
            double maxTempSum=0;
            int minCount=0;
            int maxCount=0;

            for(StationData s : values){

                //if data contains minTemp then add the temperature and increase the mincount
                if(s.isMinTemp()){
                    minTempSum += s.getMinTemp();
                    minCount++;

                }
                //else if data contains maxTemp, add the temperature and increase the maxcount
                else{
                    maxTempSum += s.getMaxTemp();
                    maxCount++;
                }
            }

            String output;

            //Handling the NAN cases
            if(minCount == 0){
                output = key.toString() + "," + " No minTemp"+"," +(maxTempSum/maxCount);

            } else if(maxCount == 0){
                output = key.toString() + "," + (minTempSum/minCount) + "," +"No maxTemp";

            }else{
                output = key.toString() + "," + (minTempSum/minCount) + "," +(maxTempSum/maxCount);

            }


            //emitting key with details and NullWritable as value
            context.write(new Text(output),res);

        }


    }



    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "no combiner");
        job.setJarByClass(NoCombiner.class);
        job.setMapperClass(TokenizerMapper.class);
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
