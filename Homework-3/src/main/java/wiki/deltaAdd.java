package wiki;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by sravani on 2/22/17.
 */

public class deltaAdd {

    static double d = 0.15;

    //A mapper class to ensure last delta calculated is updated to all records
    public static class deltaCheckMapper extends Mapper<Object, Text, Text, Text> {

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            //Getting the recordCount from the counter
            int RecordsCount = Integer.parseInt(context.getConfiguration().get("recordCount"));

            String[] valStr = value.toString().split("\n");

            //Getting the delta from the counter
            double del = Double.parseDouble(context.getConfiguration().get("delta"));

            for (String val : valStr) {

                //parsing pageName,linkPageNames and pageRank for each value
                String pageName = "";
                pageName = val.substring(0, val.indexOf("[") - 1);
                String pageLinks = val.substring(val.indexOf("[") + 1, val.indexOf("]"));
                String rank = val.substring(val.indexOf("{") + 1, val.indexOf("}"));
                String[] pages = pageLinks.split(", ");

                double pageRank = Double.parseDouble(rank);

                //updating pageRAnk with delta
                if (del != 0.0) {
                    pageRank += (1 - d) * (del / RecordsCount);
                }

                pageData pd = new pageData(Arrays.asList(pages), pageRank);
                context.write(new Text(pageName), new Text(pd.toString()));

            }
        }
    }
}
