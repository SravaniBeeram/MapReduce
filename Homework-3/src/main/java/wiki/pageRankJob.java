package wiki;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;



/**
 * Created by sravani on 2/21/17.
 */


// Driver job to run multiple map reduce jobs
// FOr job 3 I have only mapper task and I have not set reducetask number to 0 to job 3.
// So it runs identity reducer

public class pageRankJob  extends Configured implements Tool{

    public enum PAGE_COUNTER{

        Delta,
        TotalRecords;

    }

    static double deltaVal = -0.0;
    static  long recTotal ;

    private static NumberFormat nf = new DecimalFormat("00");



    public  static void main(String[] args) throws Exception{

        System.exit(ToolRunner.run(new Configuration(),new pageRankJob(),args));

    }

    @Override
    public int run(String[] args) throws Exception {


        //starting job to pre-process data
        String preProcessingPath = args[1] + "-run00";
        boolean isCompleted = runPreProcessing(args[0],preProcessingPath);
        if(!isCompleted) return 1;


        // starting job 2 to calculate page rank
        String resultPath = "";

        //running the iterations for 10 times
        for(int i = 0; i < 10 ; i++){

            String inputPath = args[1] + "-run" + nf.format(i);

            resultPath = args[1] + "-run" + nf.format(i+1);

            isCompleted = runRankCal(inputPath,resultPath);

            if(!isCompleted) return 1;

        }

        //job 3 contains only mapper task
        //This job is to ensure last delta calculated is updated to all records

        String outPath = args[1] + "-DeltaAdd" ;
        isCompleted = finalDeltaCheck(resultPath,outPath);

        if(!isCompleted) return 1;


        //top k job to extract top 100 pages
        isCompleted = runTopK(outPath,args[1]);

        if(!isCompleted) return 1;


        return 0;
    }

    public boolean runPreProcessing(String inputPath,String outputPath) throws IOException, ClassNotFoundException, InterruptedException {

        Configuration conf = new Configuration();
        //setting configuration for counter
        conf.setLong("recordCount",recTotal);
        Job job = Job.getInstance(conf, "Pre processing");

        job.setJarByClass(pageRankJob.class);
        job.setMapperClass(preProcessing.preProcessingMapper.class);
        job.setReducerClass(preProcessing.preProcessingReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        Path outPath = new Path(outputPath);
        FileOutputFormat.setOutputPath(job, outPath);
        outPath.getFileSystem(conf).delete(outPath,true);

        boolean s =  job.waitForCompletion(true);

        if(!s){
            return false;
        }

        //getting TotalRecord counter value
        recTotal = job.getCounters().findCounter(PAGE_COUNTER.TotalRecords).getValue();

        return true;

    }



    public boolean runRankCal(String inputPath,String outputPath) throws IOException, ClassNotFoundException, InterruptedException {

        Configuration conf = new Configuration();
        //setting configuration for recordCount and delta counter
        conf.setInt("recordCount",(int)recTotal);
        conf.setDouble("delta",deltaVal);
        Job cal = Job.getInstance(conf, "PageRank Calculation");

        cal.setJarByClass(pageRankJob.class);
        cal.setMapperClass(pageRankCal.pageRankCalMapper.class);
        cal.setReducerClass(pageRankCal.pageRankCalReducer.class);

        cal.setMapOutputKeyClass(Text.class);
        cal.setMapOutputValueClass(Text.class);

        cal.setOutputKeyClass(Text.class);
        cal.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(cal, new Path(inputPath));
        Path outPath = new Path(outputPath);
        FileOutputFormat.setOutputPath(cal, outPath);
        outPath.getFileSystem(conf).delete(outPath,true);

        boolean success = cal.waitForCompletion(true);

        if(!success)
            return false;

        //getting the value for delta counter
        deltaVal = Double.longBitsToDouble(cal.getCounters().findCounter(PAGE_COUNTER.Delta).getValue());

        return  true;

    }


    public boolean finalDeltaCheck(String inputPath,String outputPath) throws IOException, ClassNotFoundException, InterruptedException {

        Configuration conf = new Configuration();
        //setting configuration for recordCount and delta counter
        conf.setDouble("delta",deltaVal);
        conf.setInt("recordCount",(int)recTotal);
        Job job = Job.getInstance(conf, "Delta Check");

        job.setJarByClass(pageRankJob.class);
        job.setMapperClass(deltaAdd.deltaCheckMapper.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        Path outPath = new Path(outputPath);
        FileOutputFormat.setOutputPath(job, outPath);
        outPath.getFileSystem(conf).delete(outPath,true);

        return job.waitForCompletion(true);

    }

    public boolean runTopK(String inputPath,String outputPath) throws  IOException,ClassNotFoundException,InterruptedException{

            Configuration conf = new Configuration();
            Job job = Job.getInstance(conf,"Top K");

            job.setJarByClass(pageRankJob.class);
            job.setMapperClass(Topk.topKMapper.class);
            job.setReducerClass(Topk.topKReducer.class);

            job.setNumReduceTasks(1);

            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);

            job.setOutputKeyClass(NullWritable.class);
            job.setOutputValueClass(Text.class);

            FileInputFormat.addInputPath(job, new Path(inputPath));
            Path outPath = new Path(outputPath);
            FileOutputFormat.setOutputPath(job, outPath);
            outPath.getFileSystem(conf).delete(outPath,true);

            return job.waitForCompletion(true);
        }

}
