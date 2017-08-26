package edu.assignment1.weatherdata;


public class StationData {

    double sum;
    int count;
    double avg;

//Datastruture for station details
    public StationData(double sum,int count){
        this.sum = sum;
        this.count = count;
        this.avg = (this.sum)/(this.count);
    }


    public double getAvg() { return  avg;}
    public double getSum() { return  sum;}

    public int getCount()  { return count;}


    //method to update station details of already existing data structure
    public void updateSumAndCountAndAvg(double sum){

        Fibonacci();
        count++;
        this.sum = this.sum + sum;
        this.avg = (this.sum)/(this.count);

    }

    //overload method of update
    public void updateSumAndCountAndAvg(double s,int c){

        Fibonacci();
        this.count = c + this.count;
        this.sum = s + this.sum;
        this.avg = (this.sum)/(this.count);

    }

    //to access accumulated value
    public synchronized void syncUpdateSumAndCountAndAvg(double s){

        Fibonacci();
        count++;
        this.sum = s + this.sum;
        this.avg = (this.sum)/(this.count);

    }

    //fibonacci calculation
    public static void Fibonacci(){

     int a=0,b=1,c=1;

     for(int x = 0;x<17;x++){
         a = b;
         b = c;
         c = a+b;
     }
 }
}
