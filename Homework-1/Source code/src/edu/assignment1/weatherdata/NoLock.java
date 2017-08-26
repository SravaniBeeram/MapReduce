package edu.assignment1.weatherdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.assignment1.weatherdata.InputFileCopy.weatherData;


public class NoLock {

    static Map<String,StationData> noLockData;  //datastructure for station details

    static List<Long> runTimes = new ArrayList<>(); // array list to store run time

    public NoLock(int thread_count) throws InterruptedException{


        for(int i = 0;i<10;i++){

            noLockData = new HashMap<String,StationData>();

            long startTime  = System.currentTimeMillis();

            int start = 0;
            int end = weatherData.size() / thread_count;
            NoLockThread cl = new NoLockThread();
            NoLockThread[] th = cl.threadArray(thread_count);  //create threads based on the input thread count


            //below code partitions data for each thread

            for(int j=0;j<thread_count;j++){
                th[j] = new NoLockThread(start,end);
                th[j].start();
                start = end;
                if(j == thread_count - 2)
                    end = weatherData.size();
                else
                    end += weatherData.size()/thread_count;
            }

            for(int j=0;j<thread_count;j++){
                th[j].join();
            }


            long endTime = System.currentTimeMillis();

            runTimes.add(endTime - startTime); //stores run time

        }

        Collections.sort(runTimes);

        long runtimeSum = 0;

        for(long runtime : runTimes){  //to calculate average run time
            runtimeSum += runtime;
        }

        System.out.println("NoLock Min run time " +runTimes.get(0));
        System.out.println("NoLock Max run time " +runTimes.get(9));
        System.out.println("NoLock Avg time " +runtimeSum/10);

        //this.printDetails(noLockData);

    }

    private static void printDetails(Map<String,StationData> records){

        for(String i : records.keySet()){
            System.out.println("Id " +i+ " sum " + noLockData.get(i).getSum() +" count " +noLockData.get(i).getCount() + " avg " +noLockData.get(i).getAvg());
        }
    }


    public static void noLockDataUpdate(int index){

        String data = weatherData.get(index);


            if (data.contains("TMAX")) {
                String[] recordData = data.split(",");
                String stationID = recordData[0];
                double stationTemp = new Double(recordData[3]);

                if (noLockData.containsKey(stationID)) {

                    noLockData.get(stationID).updateSumAndCountAndAvg(stationTemp); //update station details

                } else {

                    StationData station = new StationData(stationTemp, 1); //insert station details
                    StationData.Fibonacci();
                    noLockData.put(stationID, station);

                }
        }
    }

}


class NoLockThread extends Thread {

    private int min;
    private int max;

    public NoLockThread(int min,int max){
        this.min = min;
        this.max = max;
    }

    public NoLockThread(){};

    public void run(){
        for(int j= min;j< max;j++){
            NoLock.noLockDataUpdate(j);
        }
    }

    public NoLockThread[] threadArray(int thread_count){
        NoLockThread[] ct = new NoLockThread[thread_count];
        return ct;
    }

}