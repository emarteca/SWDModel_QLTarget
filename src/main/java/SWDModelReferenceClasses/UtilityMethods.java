package SWDModelReferenceClasses;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.jfree.data.xy.XYSeries;

/**
 * This class contains static methods used throughout the SWDModel project which are not specific
 * to one class or otherwise do not logically belong in any specific class.
 * 
 * @author Ellen Arteca
 * 
 */

public class UtilityMethods {
	
	/**
	 * This method takes in a double and rounds it to 2 decimal places.
	 * @param toRound - the floating-point number to round to 2 decimal places
	 * @return the rounded value
	 */
	public static double round2Decimals(double toRound) {
		int round = (int) (toRound * 1000);
		// the if-else ensures standard rounding rules apply
		// i.e. 2.555 rounds to 2.56
		// and 2.554 rounds to 2.55
		if (round % 10 >= 5) 
			round = round/10 + 1;
		else
			round = round/10;
		return round/100. ;

	}
	
	/**
	 * This method converts a number day (for ex. day 100) into a conventional Julian day.
	 * Assumes day 1 is January 1.  
	 * Note: the startYear parameter is the first year to start counting at (for ex. day 100
	 * with a startYear of 2000, means that the method will return the corresponding Julian day
	 * specifically in 2000).
	 * If a starYear of 1 is provided, it is assumed that there is no relevant year data provided
	 * and so the Julian date returned is not year-specific.  Note that 1 is chosen as representative
	 * of 'no data' so that the default is not a leap year (year 0 is a leap year, since 0 is a 
	 * multiple of 4).
	 * @param date - the number day to convert
	 * @param startYear - the starting year of the data
	 * @return a String representing the corresponding Julian day (for ex. January 12, 2001)
	 */
	public static String dayToCalendar(int date, int startYear) {
		GregorianCalendar gregorian = new GregorianCalendar();
		gregorian.set(GregorianCalendar.YEAR, (startYear + ((int)date/365)));
		gregorian.set(GregorianCalendar.DAY_OF_YEAR, (date % 365));
		int monthNum = gregorian.get(GregorianCalendar.MONTH);
	    DateFormatSymbols dfs = new DateFormatSymbols();
	    String[] months = dfs.getMonths();
	    String month = "Error!";
	    if (monthNum >= 0 && monthNum <= 11 ) {
	    	month = months[monthNum];
	    }
	    
	    String toDisplay = month + " " + gregorian.get(GregorianCalendar.DAY_OF_MONTH);
	    if (startYear > 1)
	    	toDisplay += "," + gregorian.get(GregorianCalendar.YEAR);
	    
		return toDisplay;
	}
	
	/**
	 * This method returns a copy of an XYSeries, to avoid data leaks.
	 * @param toClone - the XYSeries to copy
	 * @param name - the name of the XYSeries to copy
	 * @return a copy of toClone
	 */
	public static XYSeries cloneSeries(XYSeries toClone, String name) { // let's avoid data leaks!
		XYSeries clone = new XYSeries(name);

		for (int i = 0; i < toClone.getItemCount(); i ++)
			clone.add(toClone.getX(i), toClone.getY(i));
		return clone;

	}
	
	/**
	 * This method returns a combined version of 2 XYSeries - starting at the end of the original 
	 * series, it reads in from the corresponding index of the new series and adds the remainder
	 * to the end of the orignal series.
	 * For example, if originalSeries was {1, 2, 3, 4} and newSeries was {5, 6, 7, 8, 9, 10}, then
	 * the returned series would be {1, 2, 3, 4, 9, 10}
	 * @param originalSeries - starting data series
	 * @param newSeries - new data series
	 * @param name - name of the series
	 * @return a combined version of originalSeries and newSeries
	 */
	public static XYSeries combineSeries(XYSeries originalSeries, XYSeries newSeries, String name) {
		XYSeries combined = cloneSeries(originalSeries, name); // copy of original series (avoids data leaks)
		
		int endIndex = originalSeries.getItemCount(); // the end of originalSeries (index to start reading from in newSeries)
		
		for (int i = endIndex; i < newSeries.getItemCount(); i ++) {
			combined.add(newSeries.getX(i), newSeries.getY(i)); // starting at the end index of originalSeries, read in the next values from newSeries
		}
		return combined;
	}
	
	/**
	 * This method returns a copy of an ArrayList of doubles, to avoid data leaks.
	 * @param toCopy - the ArrayList to copy
	 * @return a copy of toCopy
	 */
	public static ArrayList<Double> copyArrayList(ArrayList<Double> toCopy) { // no data leaks!!!!
		ArrayList<Double> copiedList = new ArrayList<Double>(toCopy.size());
		
		for (int i = 0; i < toCopy.size(); i ++)
			copiedList.add(toCopy.get(i));
		
		return copiedList;
	}
	
	/**
	 * This method returns a copy of a Map of Doubles with String keys, to avoid data leaks.
	 * @param toCopy - the Map to copy
	 * @return a copy of toCopy
	 */
	public static Map<String, Double> copyMap(Map<String, Double> toCopy) { // must avoid data leaks!
		Map<String, Double> copiedMap = new HashMap<String, Double>();
		for (String key: toCopy.keySet()) 
			copiedMap.put(key, toCopy.get(key));
		
		return copiedMap;
	}
	
	/**
	 * This method returns an ArrayList of doubles with the same data as the incoming array of doubles
	 * (basically, converts an array of doubles into an ArrayList of doubles).
	 * @param toConvert - the array to convert
	 * @return an ArrayList with the same data as toConvert
	 */
	public static ArrayList<Double> convertArray_ArrayList(double[] toConvert) {
		ArrayList<Double> convertedList = new ArrayList<Double>(toConvert.length);
		
		for (int i = 0; i < toConvert.length; i ++)
			convertedList.add(toConvert[i]);
		
		return convertedList;
	}
	
	/**
	 * This method copies an array of doubles, to avoid data leaks
	 * @param toCopy - the array to copy
	 * @return a copy of toCopy
	 */
	public static double[] copyDoubleArray(double[] toCopy) {
		double[] copiedArray = new double[toCopy.length];
		
		for(int i = 0; i < toCopy.length; i ++)
			copiedArray[i] = toCopy[i];
		
		return copiedArray;
	}
	
	/**
	 * This method copies an array of booleans, to avoid data leaks
	 * @param toCopy - the array to copy
	 * @return a copy of toCopy
	 */
	public static boolean[] copyBooleanArray(boolean[] toCopy) {
		boolean[] copiedArray = new boolean[toCopy.length];
		
		for (int i = 0; i < toCopy.length; i ++) 
			copiedArray[i] = toCopy[i];
		
		return copiedArray;
	}
	
	/**
	 * Method to calculate the sum of the values stored in an array of 
	 * doubles.
	 * @param toSum - the array to calculate to sum of
	 * @return the sum of the values in toSum
	 */
	public static double sumDoubleArray(double[] toSum) {
		double sum = 0;
		for (int i = 0; i < toSum.length; i ++)
			sum += toSum[i];
		return sum;
	}


}