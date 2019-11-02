package SWDModelReferenceClasses;

import SWDModelBaseObjects.Parameters;

/**
 * This class contains static methods for calculating various life parameters for the flies, 
 * where there are now 7 stages of female development (i.e. the female lifestage is divided 
 * into 7 different lifestages).
 * Specifically, these methods solve coupled DE with Euler's method of numerical integration.
 * Note: default integration step = 0.05
 * 
 * @author Ellen Arteca
 *
 */

public class EulersMethod {
	
	/**
	 * Method to solve for the number of eggs given the values for the previous timestep.
	 * This uses Euler's method of numerical integration to solve this differential equation (part of a system 
	 * of coupled DE).
	 * @param fertility - fertility at this timestep
	 * @param eggViabilities - viability of the eggs from the different female lifestages
	 * @param femStagePopulations - populations of the different female lifestagesfrom the previous timestep
	 * @param eggsI - number of eggs in the previous timestep
	 * @param eggMortalityNat - egg mortality rate during this timestep, due to natural causes (food, etc.)
	 * @param eggMortalityPred - egg mortality rate during this timestep, due to predation
	 * @param eggDevelopment - egg development rate during this timestep
	 * @param step - integration step
	 * @return the number of eggs in the current timestep
	 * @thrown IllegalArgumentException if the number of egg viabilities is different from the number of female lifestage
	 * 								populations, or if there is an incorrect number of them
	 */
	public static double getEggs(double fertility, double[] eggViabilities, double[] femStagePopulations, double eggsI, 
												double eggMortalityNat, double eggMortalityPred, double eggDevelopment, double step) {
		if (eggViabilities.length != femStagePopulations.length || eggViabilities.length != 7)
			throw new IllegalArgumentException("Incorrect/inconsistent number of egg viabilities and/or female stage populations"); 
		double dE_dt = 0;
		for (int i = 0; i < eggViabilities.length; i ++)
			dE_dt += fertility * eggViabilities[i] * femStagePopulations[i];
		dE_dt -= eggsI * (eggMortalityNat + eggMortalityPred + eggDevelopment);
		return eggsI + dE_dt * step;
	}
	
	/**
	 * Method to solve for the number of instar x, where x = 2, 3, given the values for the previous timestep.  
	 * This uses Euler's method of numerical integration to solve this differential equation (part of a system 
	 * of coupled DE).
	 * @param stageX_1Development - stage x - 1 development rate during this timestep
	 * @param instXMortalityNat - instar x mortality rate at this timestep, due to natural causes (food, etc.)
	 * @param instXMortalityPred - instar x mortality rate during this timestep, due to predation
	 * @param instXDevelopment - instar x development rate for this timestep
	 * @param stageX_1I - number of stage x - 1 in the previous timestep
	 * @param instXI - number of instar x in the previous timestep
	 * @param step - integration step
	 * @return the number of instar x in the current timestep
	 */
	public static double getInstX(double stageX_1Development, double instXMortalityNat, double instXMortalityPred, double instXDevelopment, 
									double stageX_1I, double instXI, double step) {
		double dIX_dt = stageX_1Development * stageX_1I - instXI * (instXMortalityNat + instXMortalityPred + instXDevelopment); // instar differential equation
		return instXI + dIX_dt * step; // Euler's method
	}

	/**
	 * Method to solve for the number of pupae given the values for the previous timestep.  This uses Euler's method
	 * of numerical integration to solve this differential equation (part of a system of coupled DE).
	 * @param inst3Development - instar 3 development rate for this timestep
	 * @param pupaeMortalityNat - pupae mortality rate at this timestep, due to natural causes (food, etc.)
	 * @param pupaeMortalityPred - pupae mortality rate during this timestep, due to predation
	 * @param pupaeDevelopment - pupae development rate for this timestep
	 * @param inst3I - number of instar 3 in the previous timestep
	 * @param pupaeI - number of pupae in the previous timestep
	 * @param step - integration step
	 * @return the number of pupae in the current timestep
	 */
	public static double getPupae(double inst3Development, double pupaeMortalityNat, double pupaeMortalityPred, double pupaeDevelopment, 
									double inst3I, double pupaeI, double step) {
		double dP_dt = inst3Development * inst3I - pupaeI * (pupaeMortalityNat + pupaeMortalityPred + pupaeDevelopment); // pupae differential equation
		return pupaeI + dP_dt * step; // Euler's method
	}
	
	/**
	 * Method to solve for the number of males given the values for the previous timestep.  This uses Euler's method
	 * of numerical integration to solve this differential equation (part of a system of coupled DE).
	 * @param pupaeDevelopment - pupae development rate for this timestep
	 * @param maleProportion - percentage of adults which are male (value between 0 and 1 inclusive)
	 * @param malesMortalityNat - male mortality rate at this timestep, due to natural causes (food, etc.)
	 * @param malesMortalityPred - male mortality rate during this timestep, due to predation
	 * @param pupaeI - number of pupae in the previous timestep
	 * @param malesI - number of males in the previous timestep
	 * @param step - integration step
	 * @return the number of males in the current timestep
	 */
	public static double getMales(double pupaeDevelopment, double maleProportion, double malesMortalityNat, double malesMortalityPred, 
									double pupaeI, double malesI, double step) {
		double dM_dt = maleProportion * pupaeDevelopment * pupaeI - malesI * (malesMortalityNat + malesMortalityPred); // male differential equation
		return malesI + dM_dt * step; // Euler's method
	}
	
	/**
	 * Method to solve for the number of females x, where x = 1, 2, 3, 4, 5, 6, 7, given the values for the previous timestep.
	 * This uses Euler's method of numerical integration to solve this differential equation (part of a system of 
	 * coupled DE).
	 * @param maleProportion - percentage of adult which are male (value between 0 and 1 inclusive) -> only applicable for females1 (i.e. 0 for the rest)
	 * @param stageX_1Development - stage x-1 development rate for this timestep
	 * @param stageX_1I - number of stage x-1 in the previous timestep
	 * @param femalesXMortalityNat - females x mortality rate at this timestep, due to natural causes (food, etc.)
	 * @param femalesXMortalityPred - females x mortality rate during this timestep, due to predation
	 * @param femalesXDevelopment - females x development rate during this timestep
	 * @param femalesXI - number of females x in the previous timestep
	 * @param step - integration step
	 * @return the number of femalesX in the current timestep
	 */
	public static double getFemalesX(double maleProportion, double stageX_1Development, double stageX_1I, double femalesXMortalityNat, 
										double femalesXMortalityPred, double femalesXDevelopment, double femalesXI, double step) {
		double dFX_dt = (1 - maleProportion) * stageX_1Development * stageX_1I - femalesXI * (femalesXMortalityNat + femalesXMortalityPred + femalesXDevelopment);
		return femalesXI + dFX_dt * step;
	}
	
	/**
	 * Method to solve for the fruit quality given the values for the previous timestep.
	 * This uses Euler's method of numerical integration to solve this differential equation.
	 * @param fruitQualityI - fruit quality from the previous timestep
	 * @param gt - potential fruit increase (temperature-based)
	 * @param fruitQLag - fruit quality timelag timesteps ago 
	 * @param step - integration step
	 * @param fruitHarvestCutoff - the fruit quality cutoff (timelag timesteps after reaching this cutoff, the quality begins to decrease)
	 * @param fruitHarvestDrop - the fruit quality drop (determines how fruit quality decreases per timestep after passing cutoff)
	 * @return the fruit quality in the current timestep
	 */
	public static double getFruitQuality(double gt, double fruitQualityI, double fruitQLag, double step, Parameters params) {
		
		double fruitHarvestCutoff = params.getParameter("fruit harvest cutoff");
		double fruitHarvestDrop = params.getParameter("fruit harvest drop");
		double gtMultiplier = params.getParameter("fruit gt multiplier");
		
		double fruitHarvest = 0;
		if (fruitQLag > fruitHarvestCutoff) // if the fruit quality lag steps ago if above the cutoff, harvest the drop
			fruitHarvest = fruitHarvestDrop;
		double dFr_dt = fruitQualityI * (gtMultiplier/gt - fruitHarvest);
		
		if (Double.isNaN(gt)) // if the gt multiplier is NaN, then treat it as 0
			dFr_dt = fruitQualityI * (-fruitHarvest);
		
		double fruitDiam = fruitQualityI + dFr_dt * step;
		
		if (fruitDiam < 0.05) // min fruit quality is 0.05
			fruitDiam = 0.05;
		if (fruitDiam > 1) // max fruit quality is 1
			fruitDiam = 1;
		
		return fruitDiam;
	}
}