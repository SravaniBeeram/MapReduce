package edu.assignment1.weatherdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.assignment1.weatherdata.InputFileCopy.weatherData;


public class CoarseLock {

    static Map<String,StationData> coarseLockData;  //datastructure to store station details


    static List<Long> runTimes = new ArrayList<>();

    public CoarseLock(int thread_count) throws InterruptedException {

        for(int i=0; i<10; i++){

            coarseLockData = new HashMap<>();

            long startTime = System.currentTimeMillis();

            int start = 0;
            int end = weatherData.size() / thread_count;
            coarseLockThread cl = new coarseLockThread();
            coarseLockThread[] th = cl.threadArray(thread_count);   //creates thread array's based on the input thread count

            for(int j=0;j<thread_count;j++){   //process each thread
                th[j] = new coarseLockThread(start,end);
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

            runTimes.add(endTime - startTime);

        }

        Collections.sort(runTimes);
        long runtimeSum = 0;
        for(long runtime : runTimes){
            runtimeSum += runtime;
        }

        System.out.println("Coarse Min run time " +runTimes.get(0));
        System.out.println("Coarse Max run time " +runTimes.get(9));
        System.out.println("Coarse Avg time " +runtimeSum/10);

        //this.printDetails(coarseLockData);

    }

    private static void printDetails(Map<String,StationData> records){

        for(String i : records.keySet()){
            System.out.println("Id " +i+ " sum " + coarseLockData.get(i).getSum() +" count " +coarseLockData.get(i).getCount() + " avg " +coarseLockData.get(i).getAvg());
        }
    }

     public static void coarseUpdate(int index){

        String data = weatherData.get(index);


        if (data.contains("TMAX")) {     //process records which contain TMAX
            synchronized (CoarseLock.coarseLockData) {
                String[] recordData = data.split(",");
                String stationID = recordData[0];
                double stationTemp = new Double(recordData[3]);

                if (coarseLockData.containsKey(stationID)) {
                    coarseLockData.get(stationID).updateSumAndCountAndAvg(stationTemp);  //as record exists it updates station details
                } else {
                    StationData station = new StationData(stationTemp, 1); //as record doesn't exist it inserts the record
                    StationData.Fibonacci();
                    coarseLockData.put(stationID, station);
                }
            }

        }
    }
}

class coarseLockThread extends Thread{

    private int min;
    private int max;

    public coarseLockThread(int min,int max){

        this.min = min;
        this.max = max;

    }

    public coarseLockThread(){};

    public void run(){

        // to iterate over all the records
        for(int i = min;i<max;i++){
            CoarseLock.coarseUpdate((i));
        }
    }

    public coarseLockThread[] threadArray(int thread_count){
        coarseLockThread[] ct = new coarseLockThread[thread_count];
        return ct;
    }
}
