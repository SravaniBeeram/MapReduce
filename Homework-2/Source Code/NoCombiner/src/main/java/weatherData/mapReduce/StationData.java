package weatherData.mapReduce;


import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by sravani on 2/1/17.
 */
public class StationData implements Writable{

    double minTemp;
    double maxTemp;
    boolean isMinTemp;


    public StationData(){

    }

    public Boolean isMinTemp(){

        return isMinTemp;

    }

    public double getMinTemp(){
        return  minTemp;
    }

    public double getMaxTemp(){
        return  maxTemp;
    }

    public StationData(double min, double max, boolean isMin){
        this.minTemp = min;
        this.maxTemp = max;
        this.isMinTemp = isMin;
    }


    public void readFields(DataInput i) throws IOException{
        this.minTemp = i.readDouble();
        this.maxTemp = i.readDouble();
        this.isMinTemp = i.readBoolean();

    }


    public void write(DataOutput o) throws IOException{
        o.writeDouble(minTemp);
        o.writeDouble(maxTemp);
        o.writeBoolean(isMinTemp);

    }



}
