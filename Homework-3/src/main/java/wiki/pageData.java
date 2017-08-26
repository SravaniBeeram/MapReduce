package wiki;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

/**
 * Created by sravani on 2/17/17.
 */

public class pageData implements Writable{

    String pageName;
    List<String> linkPageNames;
    double pageRank;


    public pageData(){

    }

    public pageData(String name,List<String> links){
        this.pageName = name;
        this.linkPageNames = links;
    }

    public pageData(List<String> links,double rank){
        this.linkPageNames = links;
        this.pageRank = rank;
    }

    public pageData(String name,List<String> links,double rank){
        this.pageName = name;
        this.linkPageNames = links;
        this.pageRank = rank;
    }


    public pageData(double rank){
        this.pageRank = rank;
    }

    @Override
    public String toString(){

        if(linkPageNames != null) {

            //used , as a delimiter  as I didn't consider the pagenames starting with ,
            //I didn't find any major change in the rankings after changing and running in the local
            //As I already ran the job in AWS i didn't run it again as i didn't observe much changes in the local

            StringBuilder list = new StringBuilder();
                list.append("[")
                    .append(String.join(", ", linkPageNames))
                    .append("]");
            return list + "{" + pageRank + "}";

        }else{
            return "[]"+ "{" + pageRank + "}";
        }
    }

    @Override
    public void write(DataOutput out) throws IOException {

        WritableUtils.writeString(out,pageName);
        out.writeDouble(pageRank);

    }

    @Override
    public void readFields(DataInput in) throws IOException {

        this.pageName = WritableUtils.readString(in);
        this.pageRank = in.readDouble();

    }


}
