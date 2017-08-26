package edu.assignment1.weatherdata;

import java.util.*;

import static edu.assignment1.weatherdata.InputFileCopy.weatherData;


public class FineLock {

    static Map<String, StationData> fineLockData;  //data structure to store station details

    static List<Long> runTimes = new ArrayList<>(); //array list to store run times

    public FineLock(int thread_count) throws InterruptedException {

        for (int i = 0; i < 10; i++) {

            fineLockData = new HashMap<>();

            long startTime = System.currentTimeMillis();

            int start = 0;
            int end = weatherData.size()/thread_count;
            FineLockThread t = new FineLockThread();
            FineLockThread[] th = t.threadArray(thread_count); //create array of threads based on the input thread count

            // below code assigns data for each thread
            for(int j=0;j<thread_count;j++){
                th[j] = new FineLockThread(start,end);
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
        for (long runtime : runTimes) {
            runtimeSum = runtimeSum + runtime;
        }


        System.out.println("FineLock Min run time " + runTimes.get(0));
        System.out.println("FineLock Max run time " + runTimes.get(9));
        System.out.println("FineLock Avg time " + runtimeSum / 10);

        // this.printDetails(fineLockData);
    }

    private static void printDetails(Map<String, StationData> records) {

        for (String i : records.keySet()) {
            System.out.println("Id " + i + " sum " + fineLockData.get(i).getSum() + " count " + fineLockData.get(i).getCount() + " avg " + fineLockData.get(i).getAvg());
        }
    }

    public static void fineLockUpdate(int index) {

        String data = weatherData.get(index);

        //only TMAX records are to be processed
        if (data.contains("TMAX")) {
            String[] recordData = data.split(",");
            String stationID = recordData[0];
            double stationTemp = new Double(recordData[3]);


            //as station details are already present we simply update the details but the
            //data can be accessed by only one thread at a time
            if (fineLockData.containsKey(stationID)) {
                fineLockData.get(stationID).syncUpdateSumAndCountAndAvg(stationTemp);
            } else {

                //station id not present so we are inserting the details
                StationData station = new StationData(stationTemp, 1);

                //to prevent the another thread's interference during updating a synchronized lock is created
                synchronized (station) {
                    if (fineLockData.containsKey(stationID)) {
                        fineLockData.get(stationID).syncUpdateSumAndCountAndAvg(stationTemp);
                    } else {
                        StationData.Fibonacci();
                        fineLockData.put(stationID, station);
                    }
                }

            }

        }
    }

}
    class FineLockThread extends Thread{

        private int min;
        private int max;

        public FineLockThread(int min,int max){

            this.min = min;
            this.max = max;
        }

        public FineLockThread(){};

        public void run(){

            for(int j=min;j<max;j++){
                FineLock.fineLockUpdate(j);
            }
        }

        public FineLockThread[] threadArray(int thread_count){
            FineLockThread[] ct = new FineLockThread[thread_count];
            return ct;
        }

    }


