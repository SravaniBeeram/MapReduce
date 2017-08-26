package edu.assignment1.weatherdata;

import java.util.*;
import static edu.assignment1.weatherdata.InputFileCopy.weatherData;


public class NoSharing {

    static Map<String, StationData> noSharingData;   //data structure to store station details

    static List<Long> runTimes = new ArrayList<>(); //array list to store run times

    public NoSharing(int thread_count) throws InterruptedException {

        for (int i = 0; i < 10; i++) {

            noSharingData = new HashMap<>();

            long startTime = System.currentTimeMillis();

            int start = 0;
            int end = weatherData.size() / thread_count;
            NoSharingThread cl = new NoSharingThread();
            NoSharingThread[] th = cl.threadArray(thread_count); //create array of threads based on the thread count

            //below code assigns data to each thread
            for(int j=0;j<thread_count;j++){
                th[j] = new NoSharingThread(start,end);
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

            //Adding two seperate datastructures into a single one
            noSharingData.putAll(th[0].getNoSharing());


            for(int k= 1;k<thread_count;k++) {


                //compare main data record entry with remaining data record entries
                for (String data : th[k].getNoSharing().keySet()) {

                    //if contains record
                    if (noSharingData.containsKey(data)) {

                        int count = th[k].getNoSharing().get(data).getCount();
                        double sum = th[k].getNoSharing().get(data).getSum();

                        noSharingData.get(data).updateSumAndCountAndAvg(sum, count);
                    } else {
                        noSharingData.put(data, th[k].getNoSharing().get(data));
                    }
                }
            }

            long endTime = System.currentTimeMillis();

            runTimes.add(endTime - startTime);

        }

        Collections.sort(runTimes);
        long runtimeSum = 0;

        for (long runtime : runTimes) {
            runtimeSum += runtime;
        }

        System.out.println("NoSharing Min run time " + runTimes.get(0));
        System.out.println("NoSharing Max run time " + runTimes.get(9));
        System.out.println("NoSharing Avg time " + runtimeSum / 10);

        // this.printDetails(noSharingData);

    }

    private static void printDetails(Map<String, StationData> records) {

        for (String i : records.keySet()) {
            System.out.println("Id " + i + " sum " + noSharingData.get(i).getSum() + " count " + noSharingData.get(i).getCount() + " avg " + noSharingData.get(i).getAvg());
        }

    }


    class NoSharingThread extends Thread{
        private Map<String,StationData> localNoSharing = new HashMap<>();

        private int min;
        private int max;

        public NoSharingThread(int min,int max){
            this.min = min;
            this.max = max;
        }

        public NoSharingThread(){};

        public void run() {


            for (int j = min; j < max; j++) {

                String data = weatherData.get(j);

                //only TMAX records are to be processed
                if (data.contains("TMAX")) {
                    String[] recordData = data.split(",");
                    String stationID = recordData[0];
                    double stationTemp = new Double(recordData[3]);


                    if (noSharingData.containsKey(stationID)) {

                        noSharingData.get(stationID).updateSumAndCountAndAvg(stationTemp); //updating station details

                    } else {

                        //station id not present so we are inserting the details
                        StationData station = new StationData(stationTemp, 1);
                        noSharingData.put(stationID, station);

                    }
                }
            }
        }
                public NoSharingThread[] threadArray(int thread_count){
                    NoSharingThread[] ct = new NoSharingThread[thread_count];
                    return ct;

            }
            public Map<String,StationData> getNoSharing(){
                return noSharingData;
            }            }
        }



