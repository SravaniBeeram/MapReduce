package wiki;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.*;

/**
 * Created by sravani on 2/20/17.
 */


public class pageRankCal {

    static  double alpha = 0.15;

    public static class pageRankCalMapper extends Mapper<Object,Text,Text,Text>{

        public void map(Object key,Text value,Context context) throws IOException, InterruptedException {

          //Getting the totalrecords count from the counter
          int RecordsCount = Integer.parseInt(context.getConfiguration().get("recordCount"));

          String[] valStr = value.toString().split("\n");

          //Getting the delta value from the counter
          double del = Double.parseDouble(context.getConfiguration().get("delta"));


            for(String val : valStr) {

              //parsing each value to retrieve pageName,linkPages and pageRank
              String pageName = "";
              pageName=  val.substring(0, val.indexOf("[")-1);
              String pageLinks = val.substring(val.indexOf("[") + 1, val.indexOf("]"));
              String rank = val.substring(val.indexOf("{") + 1, val.indexOf("}"));
              String[] pages = pageLinks.split(", ");

              double pageRank=0.0 ;

              //if initial rank value then set pageRank to 1 over total record count else set pageRank to rank
              if(rank.equals("-0.0")){

                   pageRank = 1.0/RecordsCount;

              }else{

                  pageRank = Double.parseDouble(rank);
              }

              //if delta value is not initial value then update the pageRank by adding dangling nodes
              if(del != -0.0){
                  pageRank += (1-alpha) * (del/RecordsCount);
              }

              pageData pd = new pageData(Arrays.asList(pages),pageRank);
              //emit page and its corresponding linkpages,pageRank
              context.write(new Text(pageName),new Text(pd.toString()));

              //if it is a dangling node then emit dummy and  its pageRank
              if(pages.length == 1 && pages[0].equals("")){

                  context.write(new Text("dummy"),new Text(Double.toString(pageRank)));
              }

              else{

                  for (String p : pages) {

                      double outlinkRank = pageRank / pages.length;
                      //emit each page of linkPages with its pageRank
                      context.write(new Text(p), new Text(Double.toString(outlinkRank)));

                  }
              }
          }
        }
    }


    public  static class pageRankCalReducer extends Reducer<Text,Text,Text,Text> {

        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

            double s = 0.0;
            double del = 0.0;
            pageData p1 = null;

            //Getting recordsCount from the counter
            int RecordsCount = Integer.parseInt(context.getConfiguration().get("recordCount"));


            //if key is dummy update the delta counter
            if (key.toString().equals("dummy")) {

                for (Text v : values) {
                    String rank = v.toString();
                    del += Double.parseDouble(rank);
                    context.getCounter(pageRankJob.PAGE_COUNTER.Delta).setValue(Double.doubleToLongBits(del));
                }

            } else {

                for (Text v : values) {

                    String rec = v.toString();

                    //compute sum of all ranks
                    try{

                        double rank = Double.parseDouble(rec);
                        s += rank;

                    } catch (NumberFormatException e) {

                        //parsing each value to retirieve pageName,pageRAnk and linkPages
                        String pageLinks = rec.substring(rec.indexOf("[") + 1, rec.indexOf("]"));

                        //used , as a delimiter  as I didn't consider the pagenames starting with ,
                        //I didn't find any major change in the rankings after changing and running in the local
                        //As I already ran the job in AWS i didn't run it again as i didn't observe much changes in the local
                        String[] pages = pageLinks.split(", ");
                        String pageRank = rec.substring(rec.indexOf("{") + 1, rec.indexOf("}"));
                        pageData p2 = new pageData(Arrays.asList(pages), Double.parseDouble(pageRank));
                        p1 = new pageData(p2.linkPageNames,p2.pageRank);

                    }
                }

                //calculate page rank
                p1.pageRank = (alpha/RecordsCount) + (1 - alpha) * (s);

                //append pageRank to the listPages and elit along with the key
                String out = p1.linkPageNames.toString() + "{" + p1.pageRank + "}";
                context.write(key, new Text(out));
            }

        }
    }

}
