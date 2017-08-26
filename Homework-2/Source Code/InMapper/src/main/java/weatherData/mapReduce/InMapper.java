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
import java.util.HashMap;

/**
 * Created by sravani on 2/4/17.
 */

public class InMapper {

    public static class TokenizerMapper extends Mapper<Object, Text, Text, StationData> {

        //Creating hashmap
        HashMap<Text,StationData> weather_data;

        private Text stationId ;


        //setup calls before map and initializes hashmap weather_data
        public void setup(Context context) {
            weather_data = new HashMap<>();
        }


        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            String[] data = value.toString().split("\n");

            stationId = new Text();
            for (String rec : data) {

                //process records that contain only TMIN or TMAX
                if (rec.contains("TMIN") || rec.contains("TMAX")) {

                    String[] recData = rec.split(",");
                    stationId.set(recData[0]);
                    double temp = Double.parseDouble(recData[3]);


                    if (recData[2].equals("TMIN")) {


                        //if station id is already present in the hashmap then update the sum and count
                        if(weather_data.containsKey(stationId))
                        {
                            weather_data.get(stationId).updateMin(temp);

                        }
                        else {
                            //if station id is not present then insert the record in hashmap
                            weather_data.put(stationId, new StationData(temp, 0, 1, 0));
                        }

                    } else if (recData[2].equals("TMAX")) {

                        //if station id is already present in the hashmap then update the sum and count
                        if(weather_data.containsKey(stationId))
                        {
                            weather_data.get(stationId).updateMax(temp);

                        }
                        else
                        {
                            //if station id is not present then insert the record in hashmap
                            weather_data.put(stationId, new StationData(0, temp, 0, 1));
                        }

                    }
                }
            }
        }

        //cleanup emits the hashmap records as key-value(stationId,stationData) pair
        public void cleanup(Context context) throws IOException, InterruptedException {


            for (Text s : weather_data.keySet()) {

                context.write(s,weather_data.get(s));
            }
        }
    }

    //Reducer outputs Text as key with required details and Nullwritable as value
    public static class AvgReducer extends Reducer<Text, StationData, Text, NullWritable> {

        private NullWritable res = NullWritable.get();

        public void reduce(Text key, Iterable<StationData> values, Context context) throws IOException, InterruptedException {

            double minTempSum = 0;
            double maxTempSum = 0;
            int minCount = 0;
            int maxCount = 0;

            //To retrieve temperature and count for key (stationid)
            for (StationData s : values) {

                minTempSum += s.getMinTemp();
                minCount += s.getMinCount();

                maxTempSum += s.getMaxTemp();
                maxCount += s.getMaxCount();
            }

            String output;

            //checks to handle NAN - if data doesn't contain either min temp or max temp
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
        Job job = Job.getInstance(conf, "InMapper");
        job.setJarByClass(InMapper.class);
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
