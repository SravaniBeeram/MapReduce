package weatherData.mapReduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by sravani on 2/9/17.
 */
public class SecondarySort {

    public static class TokenizerMapper extends Mapper<Object,Text,CompositeKey,StationData> {


        //emits stationId as key and StationData as value
        public void map(Object key,Text value,Context context) throws IOException,InterruptedException
        {
            String[] data = value.toString().split("\n");

            for(String rec : data){

                //process only records which contain TMIN ot TMAX
                if(rec.contains("TMIN") || rec.contains("TMAX")){

                    String[]  recData = rec.split(",");

                    //extracting station id from the record
                    String stationId = recData[0] ;

                    //extracting temperature from the record
                    double temp = Double.parseDouble(recData[3]);

                    //extracting year from the record
                    Integer year =  Integer.parseInt(recData[1].substring(0,4));

                    //Creating a dataStructure for station id and year
                    CompositeKey CK = new CompositeKey(stationId,year);

                    // If record contains TMIN data then emit key(stationId) and StationData(update with TMIN details)

                    if (recData[2].equals("TMIN")){

                        context.write(CK,new StationData(temp,0,1,0));

                    }
                    // If record contains TMMAX data then emit key(stationId) and StationData(update with TMIN details)
                    else if(recData[2].equals("TMAX")){

                        context.write(CK,new StationData(0,temp,0,1));
                    }
                }
            }
        }
    }


    //Below partitions station id using hash code
    public static class AvgPartitioner extends Partitioner<CompositeKey,StationData> {

        @Override
        public int getPartition(CompositeKey key,StationData value,int numReduceTasks){

            return Math.abs(key.getStationId().hashCode() % numReduceTasks);
        }
    }


    // This comparator helps in sorting  based on stationID first and then on year when the data was recorded
    public static class AvgSortComparator extends WritableComparator {


        protected  AvgSortComparator(){
            super(CompositeKey.class,true);
        }


        @Override
        public int compare(WritableComparable w1, WritableComparable w2){
            CompositeKey key1 = (CompositeKey) w1;
            CompositeKey key2 = (CompositeKey) w2;

            int res = key1.getStationId().compareTo(key2.getStationId());

            if(res == 0) {
                return key1.getYear().compareTo(key2.getYear());
            }

            return res;

        }
    }

    // Below comparator helps in final grouping at reducer input based on StationId
    //It doesnot consider year .SO,if two keys with same stationId are considered identical
    //no matter the year value
    public static class AvgGroupComparator extends WritableComparator{

        protected  AvgGroupComparator(){

            super(CompositeKey.class,true);
        }


        public int compare(WritableComparable w1,WritableComparable w2){

            CompositeKey key1 = (CompositeKey) w1;
            CompositeKey key2 = (CompositeKey) w2;
            return key1.getStationId().compareTo(key2.getStationId());
        }
    }

    //Below Reducer class takes Input key as CompositeKey and values - StationData and outputs Text as key containing required details and NullWritable as value
    public static class AvgReducer extends Reducer<CompositeKey, StationData, Text, NullWritable> {

        private NullWritable res = NullWritable.get();

        public void reduce(CompositeKey key, Iterable<StationData> values, Context context) throws IOException, InterruptedException {

            double minTempSum = 0;
            double maxTempSum = 0;
            int minCount = 0;
            int maxCount = 0;

            ArrayList<String> al = new ArrayList<>();

            int prev_year = key.getYear();;

            for (StationData s : values) {

                // as current data year is same as previous data add the values
                if (prev_year == key.getYear())
                {
                    minTempSum += s.getMinTemp();
                    minCount   += s.getMinCount();

                    maxTempSum += s.getMaxTemp();
                    maxCount   += s.getMaxCount();

                }
                //Else, as it is a new year we need to get the initial values
                else
                {
                    prev_year = key.getYear();
                    minTempSum = s.getMinTemp();
                    minCount = s.getMinCount();

                    maxTempSum = s.getMaxTemp();
                    maxCount = s.getMaxCount();

                }

                StringBuilder rec = new StringBuilder();


                //Creating data - appending year ,min and max temperature
                rec.append("(")
                   .append(key.getYear())
                   .append(",")
                   .append(minCount == 0 ? "None" : minTempSum / minCount)
                   .append(",")
                   .append(maxCount == 0 ? "None" : maxTempSum / maxCount)
                   .append(")" );

                //Converting String builder to string
                String Rec = rec.toString();

                //flag to check the insertion of data in the arraylist
                int i = -1;


                //adding initial data to array
                if (al.size() == 0)
                {
                    al.add(Rec);

                } else {

                    for (String sb : al) {

                        //checking each value in the array if it contains year
                        if (sb.contains(Rec.substring(1, 5)))
                        {
                            //if contains, get the index
                            i = al.indexOf(sb);
                            break;
                        }
                    }

                    // if year is already present in array update the data
                    if (i >= 0) {
                        al.set(i, Rec);
                        i = -1;

                    }
                    //as year is not present add the data to array
                    else {
                        al.add(Rec);
                    }
                }
            }

            //appending station id to the data
            String output = key.getStationId() + "," + al.toString();
            context.write(new Text(output), res);
        }
    }



    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "SecondarySort");
        job.setJarByClass(SecondarySort.class);

        job.setMapperClass(TokenizerMapper.class);
        job.setSortComparatorClass(AvgSortComparator.class);
        job.setGroupingComparatorClass(AvgGroupComparator.class);
        job.setReducerClass(AvgReducer.class);


        job.setMapOutputKeyClass(CompositeKey.class);
        job.setMapOutputValueClass(StationData.class);

        job.setPartitionerClass(AvgPartitioner.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);

        job.setNumReduceTasks(5);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        Path outPath = new Path(args[1]);
        FileOutputFormat.setOutputPath(job, outPath);
        outPath.getFileSystem(conf).delete(outPath,true);
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }


}
