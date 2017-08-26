package edu.assignment1.weatherdata;


import java.io.BufferedReader;
import java.util.*;
import java.io.*;
import java.util.zip.GZIPInputStream;

public class InputFileCopy {

    static List<String> weatherData = new ArrayList<>();

    public static void main(String args[]) throws Exception {

        // below code unzips the input file and loads the data into weatherData arraylist
        FileInputStream inputFile = new FileInputStream("/home/sravani/IdeaProjects/Assignment-1/input/1912.csv.gz");
        GZIPInputStream unzippedFile = new GZIPInputStream(inputFile);
        BufferedReader br = new BufferedReader(new InputStreamReader(unzippedFile));
        String data;
        try {
            while((data = br.readLine()) != null){
                weatherData.add(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //int th = Runtime.getRuntime().availableProcessors();
        //System.out.println("processors "+th);

        int thread_count = 2;  //no of threads

        new SequentialCal();
        System.out.println();

        new NoLock(thread_count);
        System.out.println();

        new CoarseLock(thread_count);
        System.out.println();

        new FineLock(thread_count);
        System.out.println();

        new NoSharing(thread_count);


    }

}
