package wiki;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.TreeMap;

/**
 * Created by sravani on 2/22/17.
 */
public class Topk {

    public static class topKMapper extends Mapper<Object, Text, Text, Text> {

        // Our output key and value Writables
        private TreeMap<Double, Text> repToRecordMap = new TreeMap<Double, Text>();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            // Parse the input string into a nice map

            String data     = value.toString();
            String pageName =  data.substring(0, data.indexOf("[")-1);
            String pageRank = data.substring(data.indexOf("{") + 1, data.indexOf("}"));



            double number = Double.parseDouble(pageRank);
            repToRecordMap.put(number, new Text(pageName +"{"+ pageRank  +"}"));


            if (repToRecordMap.size() > 100) {
                repToRecordMap.remove(repToRecordMap.firstKey());
            }
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {

            for (Text t : repToRecordMap.values()) {

                context.write(new Text("dummy"),t);
            }
        }
    }

    public static class topKReducer extends
            Reducer<Text, Text, NullWritable, Text> {

        private TreeMap<Double, Text> repToRecordMap = new TreeMap<Double, Text>();

        @Override
        public void reduce(Text key, Iterable<Text> values,
                           Context context) throws IOException, InterruptedException {

            for (Text value : values) {

                String val = value.toString();
                String pageName = val.substring(0,val.indexOf("{"));
                String pageRank = val.substring(val.indexOf("{") + 1, val.indexOf("}"));


                double number = Double.parseDouble(pageRank);
                repToRecordMap.put(number, new Text(pageName +"  " +"{"+ pageRank +"}"));

                if (repToRecordMap.size() > 100) {
                    repToRecordMap.remove(repToRecordMap.firstKey());
                }
            }

            for (Text t : repToRecordMap.descendingMap().values()) {
                context.write(NullWritable.get(), t);
            }
        }
    }

}
