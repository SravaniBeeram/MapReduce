package edu.assignment1.weatherdata;

import java.util.*;

import static edu.assignment1.weatherdata.InputFileCopy.weatherData;

public class SequentialCal {

  static Map<String,StationData> sequentialList; //data structure to store station details

  static List<Long> runTimes = new ArrayList<>(); //to store run times


   public SequentialCal(){

    for(int i=0;i < 10;i++) {

       sequentialList = new HashMap<String, StationData>();

       long startTime = System.currentTimeMillis();

       for (String record : weatherData) {

          if (record.contains("TMAX")) {  //process only tmax records
             String[] recordData = record.split(",");
             String stationID = recordData[0];
             double stationTemp = new Double(recordData[3]);

             if (sequentialList.containsKey(stationID)) {
                sequentialList.get(stationID).updateSumAndCountAndAvg(stationTemp); //as record exists update station details

             } else {
                StationData station = new StationData(stationTemp, 1);
                StationData.Fibonacci();
                sequentialList.put(stationID, station);  //insert station details
             }
          }
       }



       //end of program
       long endTime = System.currentTimeMillis();

       //total run time calculation
       runTimes.add(endTime - startTime);
    }

      Collections.sort(runTimes);
      long runtimeSum = 0;
      for(long runtime : runTimes){
         runtimeSum += runtime;
      }

      System.out.println("Sequential Min run time " +runTimes.get(0));
      System.out.println("Sequential Max run time " +runTimes.get(9));
      System.out.println("Sequential Avg time " +runtimeSum/10);

     //this.printDetails(sequentialList);

   }

   private static void printDetails(Map<String,StationData> records){

      for(String i : records.keySet()){
         System.out.println("Id " +i+ " sum " + sequentialList.get(i).getSum() +" count " +sequentialList.get(i).getCount() + " avg " +sequentialList.get(i).getAvg());
      }
   }

}
