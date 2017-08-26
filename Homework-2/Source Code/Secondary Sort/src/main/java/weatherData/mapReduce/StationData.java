package weatherData.mapReduce;


import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by sravani on 2/4/17.
 */
public class StationData implements Writable{

    double minTemp;
    double maxTemp;
    int   minCount;
    int   maxCount;

    public StationData(){

    }

    public int getMinCount(){

        return minCount;

    }

    public int getMaxCount(){
        return maxCount;
    }

    public double getMinTemp(){
        return  minTemp;
    }

    public double getMaxTemp(){
        return  maxTemp;
    }

    public void updateMin(double temp){
        this.minTemp += temp;
        this.minCount++;
    }

    public void updateMax(double temp){
        this.maxTemp += temp;
        this.maxCount++;
    }

    public StationData(double min, double max, int minCount, int maxCount){
        this.minTemp = min;
        this.maxTemp = max;
        this.minCount = minCount;
        this.maxCount = maxCount;
    }

    public void updateStationData(double min, double max, int minCount,int maxCount){
        this.minTemp += min;
        this.maxTemp += max;
        this.minCount += minCount;
        this.maxCount += maxCount;
    }


    public void readFields(DataInput i) throws IOException{
        this.minTemp = i.readDouble();
        this.maxTemp = i.readDouble();
        this.minCount = i.readInt();
        this.maxCount = i.readInt();

    }


    public void write(DataOutput o) throws IOException{
        o.writeDouble(minTemp);
        o.writeDouble(maxTemp);
        o.writeInt(minCount);
        o.writeInt(maxCount);

    }



}
