package SWDModelReferenceClasses;

public class Daylight {
	
	/**
	 * This is a pretty cheap method to determine the date, in December, when the winter
	 * solstice occurs.  This could be changed to a computation at a later point.
	 * @param year The year of interest.
	 * @return the date in December on which the solstice occurs.
	 */
	public static int getSolsticeDay(int year){
		
		int[] dates = {21,21,22,22,21,21,22,22,21,21,21,22,21,21,21,22,21,21,21,22,21};
		if(year < 2000 || year > 2020)
			return 21; // instead of crashing the program, just return 21 (which seems to be the mode anyway)
		else return dates[year-2000];
	}
	
	/**
	 * Another cheap method designed to give us the offset that needs to be added to the 
	 * model timestep because of the solstice start date requirement.
	 * @param year The year of interest.
	 * @return the offset between the solstice date in December and Jan 1 when the model 
	 * starts.
	 */
	public static int getOffSet(int year){
		int solstice = getSolsticeDay(year);
		return 31 - solstice;
	}
	
	/**
	 * This method makes a few assumptions, not the least of which is that date 0
	 * corresponds to Jan 1.  Since this method requires the solstice to be date 0
	 * we must first compute the offset between the solstice and Jan 1.  It is assumed
	 * that the offset has been pre-computed and is not called in this method.  This
	 * will save considerable computation time over long runs. 
	 * 
	 * 
	 * TODO: This method needs review for multi-year runs, there may be issues with
	 * the 0.25 days/4 years.
	 * 
	 * Much of this formula is derived from: http://www.gandraxa.com/length_of_day.xml
	 * 
	 * @param year The year of interest.
	 * @param date This corresponds to the day of interest, most likely a timestep.
	 * @return The number of hours of daylight.
	 */
	public static double getDayLightHours(int year, int date, double latitude){
		final double AXIS = 23.439;  //Earth's axis.
		
		double j = Math.PI/182.625;
		double m = 1-Math.tan(Math.toRadians(latitude))*Math.tan(Math.toRadians(AXIS)*Math.cos(j*date));
		
		/*
		 * From http://www,gandraxa.com/length_of_day.xml
		 * (to avoid future NaN's...)
		 * 
		 * Adjust range:
		 * 1. if m is negative, then the sun never appears the whole day long (polar winter): m must be adjusted 
		 * to 0 (the sun can not shine less than 0 hours).
		 * 2. if m is larger than 2, the "sun circle" does not intersect with the planet's surface and the sun is 
		 * shining the whole day (polar summer): m must be adjusted to 2 (the sun can not shine for more than 24 hours).
		 */
		
		if (m > 2)
			m = 2;
		if (m < 0)
			m = 0;
		
		double h = (Math.toDegrees(Math.acos(1-m))/180)*24;
		return h;
	}
	

}
