package SWDModelReferenceClasses;

import SWDModelBaseObjects.Parameters;

import java.math.BigDecimal;

/**
 * This class contains static methods for calculating various life parameters for the flies.
 * Specifically, these methods solve for the temperature dependent vitals -> fertility, 
 * mortality, and development rates. 
 * This class also contains static methods to calculate the effect of the plants on the flies,
 * specifically on their development and mortality rates; and also the effect of diapause on
 * fecundity (dependent on the number of daylight hours) and helper methods. 
 * Note: these methods are not lifestage-specific.  When called, the parameters for each
 * specific lifestage are passed in to perform the calculation.
 * 
 * @author Ellen Arteca
 * 
 */

public class SolveParameters {
	
	
	/**
	 * This method calculates the current fecundity value for the flies, given the various parameters and 
	 * the current temperature of the environment.  
	 * Note: this is approximated by a compact continuous curve that is estimated from laboratory results.
	 */
	public static double solveSpecificFertility(double T, Parameters params) {
		
		double d = 5.88;
		double l = 52.68;
		
		if (T > params.getParameter("fertility tmax"))
			return 0;
		
		if((Math.pow(T,2) + Math.pow(d,2)) < Math.pow(l,2)){
			BigDecimal temp1 = new BigDecimal(3.3315e-304);
			BigDecimal temp2 = new BigDecimal(2740.50-Math.pow((-23.26+T),2));
			BigDecimal temp3 = null;
			if (temp2.doubleValue() <= 0)
				return 0;
		    temp3 = SWDModelReferenceClasses.BigDecimalUtils.exp(SWDModelReferenceClasses.BigDecimalUtils.ln(temp2,20).multiply(BigDecimal.valueOf(88.38)), 20);
			
			BigDecimal fertility = temp1.multiply(temp3);
			return fertility.doubleValue();
		}
		else 
			return 0;
	}
	
	/**
	 * Method to return the effect of diapause on fecundity (this is a multiplier for the 
	 * temperature-dependent fecundity value calculated).
	 * Note: the fecundity diapause effect is dependent on the current daylight hours
	 * @param hours - current number of daylight hours
	 * @return the diapause multilpier for fecundity
	 */
	public static double solveFertilityDiapauseEffect(double hours) {
		/*
		 * So, as I understand this:
		 * 1. solve the logistic function for the value of the the number of females in diapause 
		 * 2. the effect on the fecundity is 100 - this value
		 * 3. then, it is a multiplier for the fecundity (but divide by 100 first)
		 */
		
		final double A = 0.04056;
		final double K = 99.8;
		final double v = 1.2428535918;
		final double M = 0;
		final BigDecimal Q = new BigDecimal(3.23967951563418E-016);
		final double B = -2.871323611;
		
		BigDecimal exp = Q.multiply(BigDecimal.valueOf(Math.exp(-B * (hours - M))));	
		// then, this value should be an ok value for Java doubles to store
		
		double denom = 1 + exp.doubleValue();
		double effect = A + (K - A) / Math.pow(denom, (1/v));
		
		// then, effect is a value between 0 and 100, where 100 is all females are in diapause
		effect = 100 - effect;
		// now, turn this into a percentage
		effect /= 100;
		
		return effect;
	}
	
	/**
	 * Method to return the current timestep value of s1 (for the diapause mutliplier for fecundity - as given
	 * in the new equation).  Note that this is a step function whose value is either 0 or 1.
	 * @param hours - daylight hours
	 * @param temp - current temperature
	 * @param s1prev - s1 during previous timestep
	 * @param s2prev - s2 during previous timestep
	 * @param tCrit - critical temperature for the diapause model
	 * @param daylightHours - daylight hours cutoff for the diapause model
	 * @return s1 value for current dt
	 */
	public static int solveDiapauseMultS1(double hours, double temp, int s1prev, int s2prev, double tCrit, double daylightHours) {
		if (s1prev * s2prev > 0 && hours < daylightHours)
			return 0;
		else if (s2prev == 0 && temp > tCrit)
			return 1;
		else
			return s1prev;
	}
	
	/**
	 * Method to return the current timestep value of s2 (check for the diapause mutliplier for fecundity - as given
	 * in the new equation).  Note that this, too, is a step function whose value is either 0 or 1.
	 * @param hours - daylight hours
	 * @param s1prev - s1 during previous timestep
	 * @param s2prev - s2 during previous timestep
	 * @param daylightHours - daylight hours cutoff for the diapause model
	 * @return s2 value for current dt
	 */
	public static int solveDiapauseMultS2(double hours, int s1prev, int s2prev, double daylightHours) {
		if (s1prev == 0)
			return 0;
		else if (hours >= daylightHours)
			return 1;
		else
			return s2prev;
	}
	
	/**
	 * This method calculates the current development rate, given the various parameters (for a specific
	 * lifestage, excluding adults) and the current temperature of the environment.
	 * @param maxDev - maximum development rate 
	 * @param betaD - development beta	
	 * @param qD - development q
	 * @param Tmin - minimum temperature for development
	 * @param Tmax - maximum temperature for development
	 * @param Tref - reference temperature for development
	 * @param T - current temperature of the cell/environment
	 * @return the development rate of the lifestage, given the current parameters
	 */
	public static double solveDevelopment(double T, Parameters params, String stage) {
		
		double Tmin = params.getParameter(stage + " development tmin");
		double Tmax = params.getParameter(stage + " development tmax");
		double qD = params.getParameter(stage + " development q");
		double Tref = params.getParameter(stage + " development tref");
		double maxDev = params.getParameter(stage + " development max");
		double betaD = params.getParameter(stage + " development beta");
		
		if (!(Tmin <= T && T <= Tmax)) // if temperature is not within tolerable range, development rate == 0
			return 0;
		
		double num = Math.pow((T - Tmin), qD) * (Tmax - T);
		double denom = Math.pow((Tref - Tmin), qD) * (Tmax - Tmin);
		return maxDev * betaD * num / denom;
	}
	
	/**
	 * Method to return the new temperature-dependent development rate.  This is actually the same
	 * function, just scaled differently depending on the development rate at optimal temperature 
	 * for each life-stage
	 * @param T - current temperature
	 * @param devMult - development rate for current stage at optimal temperature (a scalar for the function)
	 * @return the development rate of the lifestage at the current temperature
	 */
	public static double solveDev_newData(double T, double devMult) {
		// here, we're using the values from the calculated equation for egg-to-adult development rate
		// the equation is in the form: d = a + b(T-T0) + c(T-T0)^2 + d(T-T0)^3 + e(T-T0)^4 + f(T-T0)^5
		
		if (T > 35 || T < 5)
			return 0;
		
		// these are constants for all curves!!
		double[] coeffs = {0.06954386496, // a --> calculated to restore the T-T0 pattern
						   0.0065184, // b
						   3.1127e-5, // c
						   -0.0000251, // d
						   -2.0431e-6, // e
						   -5.5598e-8}; // f
		double T0 = 21.2644;
		
		double devRate = 0;
		for (int i = 0; i < coeffs.length; i ++) {
			devRate += coeffs[i] * Math.pow((T - T0), i);
		}
		
		devRate *= devMult * 10; // TODO shouldn't this be a value between 0 and 1??*/
		if (devRate < 0)
			devRate = 0;
		if (devRate > devMult)
			devRate = devMult;
		return devRate;
		
	}
	
	/**
	 * Method to return the new BRIERE temperature-dependent development rate.  
	 * @param T - current temperature
	 * @param devMult - development rate for current stage at optimal temperature (a scalar for the function)
	 * @return the development rate of the lifestage at the current temperature
	 */
	public static double solveDev_Briere_Juvenile(double T, double devMult) {
		// here, we're using the values from the calculated equation for egg-to-adult development rate
		// the equation is in the form: 1/d = aT(T-T0)sqrt(TL-T)
		
		//TODO: really should not be hardcoded i guess...
		double a = 0.0001113;
		double T0 = 9.8504;
		double TL = 30.99;
		
		if (T > TL || T < T0)
			return 0;
		
		
		double devRate = a * T * (T-T0) * Math.sqrt(TL - T);

		devRate /= devMult; // TODO shouldn't this be a value between 0 and 1??*/
		//if (devRate < 0)
		//	devRate = 0;
		//if (devRate > devMult)
		//	devRate = devMult;
		return devRate;
		
	}
	
	/**
	 * Method to return the new BRIERE temperature-dependent development rate.  
	 * @param T - current temperature
	 * @param devMult - development rate for current stage at optimal temperature (a scalar for the function)
	 * @return the development rate of the lifestage at the current temperature
	 */
	public static double solveDev_Briere_Pupa(double T, double devMult) {
		// here, we're using the values from the calculated equation for egg-to-adult development rate
		// the equation is in the form: 1/d = aT(T-T0)sqrt(TL-T)
		
		//TODO: really should not be hardcoded i guess...
		double a = 0.0001687;
		double T0 = 8.0139;
		double TL = 31.304;
		
		if (T > TL || T < T0)
			return 0;
		
		
		double devRate = a * T * (T-T0) * Math.sqrt(TL - T);
			
		devRate *= devMult * 10; // TODO shouldn't this be a value between 0 and 1??*/
		//if (devRate < 0)
		//	devRate = 0;
		//if (devRate > devMult)
		//	devRate = devMult;
		return devRate;
		
	}

	
	/**
	 * This method calculates the current mortality rate, given the various parameters (for a specific 
	 * lifestage) and the current temperature of the environment.
	 * @param maxM - maximum mortality rate
	 * @param Tlower - minimum temperature for lifestage
	 * @param Tupper - maximum temperature for lifestage
	 * @param T - current temperature of the cell/environment
	 * @param tau - mortality tau
	 * @param betas - array of mortality betas (beta0, beta1, beta2, beta3 in this order) for the lifestage
	 * @return the mortality rate of the lifestage, given the current parameters
	 * @throws IllegalArgumentException if an incorrect number of betas (i.e. not 4) is provided
	 */
	public static double solveMortality(double T, Parameters params, String stage) {		
		
		double[] betas = params.getMortBetas(stage);
		double Tlower = params.getParameter(stage + " mortality min temp");
		double Tupper = params.getParameter(stage + " mortality max temp");
		double maxM = params.getParameter(stage + " mortality max");
		double tau = params.getParameter(stage + " mortality tau");
		
		if ((!(Tlower <= T && T <= Tupper))) // if temperature is not within tolerable range, max mortality is reached
			return maxM; 
		
		double mortality = 0; // mortality is a sum, initialize outside the loop
		
		for (int i = 0; i < 4; i ++) 
			mortality += betas[i] * Math.pow((T - tau), i);
		
		/*if (mortality > maxM) // this is so mortality does not go above max mortality, which happens when max mortality is very small
			mortality = maxM;*/
		
		return mortality;
	}
	
	/**
	 * Method to return the growth time (timesteps until 100 % growth) at the specified temperature.
	 * Note: this is a reciprocal estimation of the function produced with the sour cherry data.
	 * @param baseTemp - minimum temperature for growth
	 * @param currentTemp - current temperature of the cell/environment
	 * @return the growth time at this temperature
	 */
	public static double getGT(double baseTemp, double currentTemp) {
		if (currentTemp <= baseTemp)
			return Double.NaN;
		double gt = 1100 / (currentTemp - baseTemp) + 30; // a reciprocal function estimation (1 asymptote)
		return gt;
		
	}
	
	/**
	 * Method to return the effect of fruit quality on the development rate for the specified lifestage.
	 * @param m - the m parameter for fruit quality effect on the flies
	 * @param fruitQConstant - constant denominator for the ratio, taken from the aphid paper 
	 * @param currentQuality - fruit quality for the current timestep
	 * @param n - the n parameter for the fruit quality
	 * @return the effect of current fruit quality on the development rate
	 * @throws IllegalArgumentException if the fruit quality m parameter is not between 0 and 1 inclusive
	 */
	public static double solveDevelopmentPlantEffect(double fruitQConstant, double currentQuality, Parameters params) {
		
		double m = params.getParameter("fruit m");
		double n = params.getParameter("fruit n");
		
		if (! (0 <= m && m <= 1))
			throw new IllegalArgumentException("m is between 0 and 1 inclusive");
		
		double ratio = Math.pow((currentQuality / fruitQConstant), n);
		double effect = m * ratio * Math.pow((1 + ratio), -1) + 1 - m;
		
		return effect;
	}
	
	/**
	 * Method to return the effect of fruit quality on the mortality rate for the specified lifestage.
	 * @param maxMort - maximum mortality for the specified lifestage
	 * @param fruitQConstant - constant denominator for the ratio, taken from the aphid paper 
	 * @param currentQuality - fruit quality for the current timestep
	 * @param n - the n parameter for the fruit quality
	 * @return the effect of current fruit quality on the development rate
	 */
	public static double solveMortalityPlantEffect(double fruitQConstant, double currentQuality, Parameters params, String stage) {
		
		double n = params.getParameter("fruit n");
		double maxMort = params.getParameter(stage + " mortality max");
		
		double m = 0.1 * maxMort;
		
		double ratio = Math.pow((currentQuality / fruitQConstant), n);
		double effect = m * Math.pow((1 + ratio), -1);
		
		return effect;
	}
	
}