/*--------------------------------------------------------------------
  @Name    : MathFunctions.java
  @Author  : Arun Goel
  @Purpose : Bean for different Math Operations
  @Modification History:
    Initial Version --> 07/20/12 --> Arun Goel

----------------------------------------------------------------------*/
package pac1.Bean;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MathFunctions
{
  static String className = "MathFunctions";
  static DecimalFormat df = new DecimalFormat("#.###");

  public MathFunctions()
  {

  }

  /*
   * To calculate the Average Variance by passing all double values and mean
   * @itemValues - double array which will have the values for which need to calculate the variance
   * @mean - double value which will have the mean of all values
   */
  public static double calAverageVariance(double[] itemValues, double mean)
  {
    Log.debugLog(className, "calAverageVariance", "", "", "Method called. itemValues length = " + itemValues.length + ", mean = " + mean);
    double variance = 0.0; //it will have the average variance
    double sumOfIndividualVariances = 0.0; //it will have the sum of all variances

    //by this loop we will find the variance of each value and sum in to the variable to take out the average
    for(int i = 0; i < itemValues.length; i++)
    {
      sumOfIndividualVariances = sumOfIndividualVariances + Math.pow((itemValues[i] - mean), 2);
    }
    Log.debugLog(className, "calAverageVariance", "", "", "sumOfIndividualVariances = " + sumOfIndividualVariances);
    variance = sumOfIndividualVariances / itemValues.length; //geting the average of all variances
    Log.debugLog(className, "calAverageVariance", "", "", "Average Variance = " + variance);
    return variance;
  }

  /*
   * To calculate the Average Variance by passing all double values
   * We need to calculate the mean first then we will call another method by passing values and mean
   * @itemValues - double array which will have the values for which need to calculate the variance
   */
  public static double calAverageVariance(double[] itemValues)
  {
    Log.debugLog(className, "calAverageVariance", "", "", "Method called. itemValues length = " + itemValues.length);
    double mean = 0.0;

    double sum = 0;
    for(int i = 0; i < itemValues.length; i++)
    {
      sum += itemValues[i];
    }
    Log.debugLog(className, "calAverageVariance", "", "", "Sum of all values = " + sum);
    mean = sum / itemValues.length;
    Log.debugLog(className, "calAverageVariance", "", "", "Mean = " + mean);
    return calAverageVariance(itemValues, mean);
  }

  /*
   * To calculate the Standard Deviation by passing variance
   * @variance - double value of variance
   */
  public static double calStdDev(double variance)
  {
    Log.debugLog(className, "calStdDev", "", "", "Method called. variance = " + variance);
    double stdDev = Math.sqrt(variance);
    Log.debugLog(className, "calStdDev", "", "", "stdDev = " + stdDev);
    return stdDev;
  }

  /*
   * To calculate the variance and standard deviation by passing values and mean
   * it will return a array list which will have the average variance on 0th
   * and average stddev on 1st index of array list
   * @itemValues - double array which will have the values
   * @mean - double values which will have the mean of all values
   */
  public static ArrayList calVarianceAndStdDev(double[] itemValues, double mean)
  {
    Log.debugLog(className, "calVarianceAndStdDev", "", "", "Method called. itemValues length = " + itemValues.length + ", mean = " + mean);
    ArrayList arrVarianceStdDev = new ArrayList(); //array list to keep average variance and stddev
    double sumOfIndividualVariances = 0.0; //it will have the sum of all variances

    for(int i = 0; i < itemValues.length; i++)
    {
      double individualVariance = Math.pow((itemValues[i] - mean), 2); //calculate variance of each item
      sumOfIndividualVariances = sumOfIndividualVariances + individualVariance;
    }
    Log.debugLog(className, "calVarianceAndStdDev", "", "", "sumOfIndividualVariances = " + sumOfIndividualVariances);
    
    double VMR = 0;
    double std_dev = 0;
    if(itemValues.length -1 > 0)
    {
      VMR = sumOfIndividualVariances/(itemValues.length-1);
      std_dev = Math.sqrt(VMR);
    }
    
    Log.debugLog(className, "calVarianceAndStdDev", "", "", "VMR = " + VMR);
    Log.debugLog(className, "calVarianceAndStdDev", "", "", "std_dev = " + std_dev);
    
    arrVarianceStdDev.add(df.format(VMR));
    arrVarianceStdDev.add(df.format(std_dev));

    return arrVarianceStdDev;
  }

  /*
   * To calculate the variance and standard deviation by passing values
   * It will calculate the mean of all values
   * it will return a array list which will have the average variance on 0th
   * and average stddev on 1st index of array list
   * @itemValues - double array which will have the values
   */
  public static ArrayList calVarianceAndStdDev(double[] itemValues)
  {
    Log.debugLog(className, "calVarianceAndStdDev", "", "", "Method called. itemValues length = " + itemValues.length);
    ArrayList arrVarianceStdDev = new ArrayList();

    double sum = 0;
    double sumOfSquraeOfEachDataItem = 0;
    for(int i = 0; i < itemValues.length; i++)
    {
      sum += itemValues[i];
      sumOfSquraeOfEachDataItem += Math.pow(itemValues[i], 2);
    }
    double VMR = 0;
    double mean = 0;
    double Variance = 0;
    double std_dev = 0;
    if((itemValues.length-1) > 0)
    {
      Variance = Math.abs((sumOfSquraeOfEachDataItem - (Math.pow(sum, 2)/itemValues.length))/(itemValues.length-1));
      mean = sum/itemValues.length;
      //VMR - Variance to Mean Ratio
      if(mean != 0)
        VMR = Variance/mean;
      else
    	VMR = 0;
      std_dev = Math.sqrt(Variance);
    }
    
    Log.debugLog(className, "calVarianceAndStdDev", "", "", "VMR = " + VMR);
    Log.debugLog(className, "calVarianceAndStdDev", "", "", "std_dev = " + std_dev);
    
    arrVarianceStdDev.add(df.format(VMR));
    arrVarianceStdDev.add(df.format(std_dev));
    return arrVarianceStdDev;
  }

  public static void main(String[] args)
  {
    double[] test = new double[]{9, 2, 5, 4, 12, 7, 8, 11, 9, 3, 7, 4, 12, 5, 4, 10, 9, 6, 9, 4 };
    ArrayList result = MathFunctions.calVarianceAndStdDev(test);
    System.out.println(result.get(0).toString());
    System.out.println(result.get(1).toString());
  }
}
