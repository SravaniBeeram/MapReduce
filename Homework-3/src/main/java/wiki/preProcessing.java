package wiki;


import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import java.io.*;
import java.util.*;

/**
 * Created by sravani on 2/17/17.
 */

public class preProcessing {


    public static class preProcessingMapper extends Mapper<Object,Text,Text,Text> {


        public void map(Object key,Text value,Context context) throws IOException{

            try {

                //calling WikiParser with value as input to parse the data
                //returns pageData data structure containing pageName and corresponding links
                pageData  p = new Bz2WikiParser().parse(value.toString());

                if(p != null) {

                    for(String lp : p.linkPageNames){

                        //for each page in linkpages emit linkPageName and empty linkpagelist(adjacency list)
                        if(!lp.equals("[]"))

                            context.write(new Text(lp), new Text("[]"));
                    }

                     //Emit pageName and empt adjacency list
                     context.write(new Text(p.pageName),new Text("[]"));

                    //emit pageName and its entire adjacency list
                     context.write(new Text(p.pageName),new Text(p.linkPageNames.toString()));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    public static class preProcessingReducer extends Reducer<Text,Text,Text,Text> {


        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

            String rec = "";

            for (Text s : values) {

               //append all the linkpages
               if(!s.toString().equals("[]") )
                   rec = rec + s.toString();

            }

            if(rec.equals("")) {

                rec = "[]";   //empty linkpages

            }

            rec = rec + "{-0.0}"; //appending initial pageRank to the linkpages


            //incrementing the total record counter by 1
            context.getCounter(pageRankJob.PAGE_COUNTER.TotalRecords).increment(1);

            context.write(key,new Text(rec));


        }
    }
}
