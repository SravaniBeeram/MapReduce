package weatherData.mapReduce;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by sravani on 2/7/17.
 */
public class CompositeKey implements Writable,WritableComparable <CompositeKey>{

    String stationId;
    Integer year;


    public CompositeKey(){}


    public CompositeKey(String id, Integer year){

        this.stationId = id;
        this.year = year;
    }

    public String getStationId(){
        return this.stationId;

    }

    public Integer getYear(){
        return  this.year;
    }


    public void readFields(DataInput i) throws IOException {
        this.stationId = WritableUtils.readString(i);
        this.year = i.readInt();

    }


    public void write(DataOutput o) throws IOException{
        WritableUtils.writeString(o,stationId);
        o.writeInt(year);

    }

    public int compareTo(CompositeKey obj) {

        int res = stationId.compareTo(obj.stationId);
        if( 0 == res){
            res = year.compareTo(obj.year);
        }
        return res;
    }
}
