package SWDModelBaseObjects;

import SWDModelReferenceClasses.EulersMethod;
import SWDModelReferenceClasses.SolveParameters;
import SWDModelReferenceClasses.UtilityMethods;

/**
 * This class describes an SWDPopulation object.  This object tracks the populations of 
 * the various different life stages for the flies (egg, pupae, instar1, instar2, instar3, 
 * males, and all female stages) over time.
 * There are various accessors to see the current values for each lifestage population, 
 * and a method for running the lifestages for dt timesteps (where dt is the integration
 * step, for Euler's method of numerical integration).  There is also a mutator for 
 * reseting the current life-stage specific populations to their respective initial populations.
 * The population object also has datafields for the initial populations of each life stage, 
 * and there are accessors for each one.
 * Methods described in-code.
 *
 * @author Ellen Arteca
 *
 */

public class SWDPopulation {
	
	 // datafields 
	
	 private double currentEggs, currentInst1, currentInst2, currentInst3, currentPupae, currentMales; // the current stage-specific populations
	 private double[] currentFemaleStages = new double[7]; // female-stage specific populations
	 
	 //public static double[] fec = new double[365]; // TODO array of diapause multipliers for each day simulation is run, for printing
	 // initially, s1 and s2 are both 0 (start not in diapause)
	 private int s1 = 0;
	 private int s2 = 0;
	 private boolean crossedDiapause = false;
	 private boolean addInitPop = false;
	 
	 private int crossedDiapDay = -1;
	 
	  
	 /**
	  * Constructor to initialize the population object.  Parameters set to those specified
	  * in the Parameters object passed in.
	  * @param params - lists all the datafields for the population and cell objects by name
	  */
	 public SWDPopulation(Parameters params) {
		 resetPopulation(); // to make sure current populations are set to the initial populations specified
		 
	 }
	 
	 /**
	  * Default no-argument constructor, sets the datafields to default values 
	  * (1 egg, no other life stages present).
	  */
	 public SWDPopulation() { 
		 currentEggs = 1;
		 currentInst1 = 0;
		 currentInst2 = 0;
		 currentInst3 = 0;
		 currentPupae = 0;
		 currentMales = 0;
		 
		 for (int i = 0; i < currentFemaleStages.length; i ++)
			 currentFemaleStages[i] = 0;
		 	 
	 }
	 
	 // -----------------------------------------------------------------------------------------CURRENT POPULATIONS
	 
	 /**
	  * Method to return the current number of eggs in the population
	  * @return the number of eggs currently in the population
	  */
	 public double getEggs() {return currentEggs; }
	 
	 /**
	  * Method to return the current number of instar 1 in the population
	  * @return the number of instar 1 currently in the population
	  */
	 public double getInst1() {return currentInst1; }
	 
	 /**
	  * Method to return the current number of instar 2 in the population
	  * @return the number of instar 2 currently in the population
	  */
	 public double getInst2() {return currentInst2; }
	 
	 /**
	  * Method to return the current number of instar 3 in the population
	  * @return the number of instar 3 currently in the population
	  */
	 public double getInst3() {return currentInst3; }
	
	 /**
	  * Method to return the current number of pupae in the population
	  * @return the number of pupae currently in the population
	  */
	 public double getPupae() {return currentPupae; }
	 
	 /**
	  * Method to return the current number of adult males in the population
	  * @return the number of adult males currently in the population
	  */
	 public double getMales() {return currentMales; }
	 
	 /**
	  * Method to return the current number of adult females in the population 
	  * (the sum of the populations for each female stage).
	  * @return the number of adult females currently in the population
	  */
	 public double getFemales() {return UtilityMethods.sumDoubleArray(currentFemaleStages); } 
	 
	 /**
	  * Method to return the array of current number of adult females of the
	  * different female lifestages in the population.
	  * @return the number of adult females, per stage currently in the population
	  */
	 public double[] getFemStages() {return UtilityMethods.copyDoubleArray(currentFemaleStages); }
	 
	
	 /**
	  * Method to return the current total number of bugs in the population
	  * @return the total number of flies at all life stages currently in the population
	  */
	 public double getTotalPopulation() { 
		 return currentEggs + currentInst1 + currentInst2 + currentInst3 + currentPupae + currentMales + getFemales();
	 }
	 
	 // -----------------------------------------------------------------------------------------TIMESTEP	
	 
	 /**
	  * Method to reset the populations to their initial values
	  * @param params - parameters object, including the initial population values
	  */
	 public void readPopulation(Parameters params) {
		 
		 addInitPop = true;
		 
		 currentEggs = params.getParameter("initial eggs");
		 currentInst1 = params.getParameter("initial instar1");
		 currentInst2 = params.getParameter("initial instar2");
		 currentInst3 = params.getParameter("initial instar3");
		 currentPupae = params.getParameter("initial pupae");
		 currentMales = params.getParameter("initial males");
		 
		 for (int i = 0; i < currentFemaleStages.length; i ++)
			 currentFemaleStages[i] = params.getParameter("initial females" + (i + 1));
	 }
	 
	 /**
	  * Method to set the addInitPop boolean to be true - this is done initially to ensure that 
	  * if a valid injection date is passed in, the population is not added even if diapause
	  * threshold is crossed, until readPopulation is called.
	  */
	 public void setAddInitPop(boolean toSet) {
		 addInitPop = toSet;
	 }
	 
	 /**
	  * Method to return the day the diapause threshold was crossed
	  * @return the day the diapause threshold was crossed (-1 if never crossed)
	  */
	 public int getCrossedDiapDay() {
		 return crossedDiapDay;
	 }
	 
	 /**
	  * Resets the population to its state at timestep 0. - all the populations are reset to their initial values.
	  */
	 public void resetPopulation() {
		 /*currentEggs = params.getParameter("initial eggs");
		 currentInst1 = params.getParameter("initial instar1");
		 currentInst2 = params.getParameter("initial instar2");
		 currentInst3 = params.getParameter("initial instar3");
		 currentPupae = params.getParameter("initial pupae");
		 currentMales = params.getParameter("initial males");
		 
		 for (int i = 0; i < currentFemaleStages.length; i ++)
			 currentFemaleStages[i] = params.getParameter("initial females" + (i + 1));*/
		 // now, start all stages at population 0 on timestep 0
		 
		 crossedDiapDay = -1;
		 crossedDiapause = false;
		 addInitPop = false;		
		 
		 s1 = 0;
		 s2 = 0;
		 
		 currentEggs = 0;
		 currentInst1 = 0;
		 currentInst2 = 0;
		 currentInst3 = 0;
		 currentPupae = 0;
		 currentMales = 0;
		 
		 for (int i = 0; i < currentFemaleStages.length; i ++)
			 currentFemaleStages[i] = 0;
		 
		 
	 }
	 
	 /**
	  * This method advances the time for the population and runs all the life processes accordingly (i.e. laying 
	  * eggs, developing to the next lifestage, dying, etc.).  Takes in the arguments needed to calculate fertility
	  * rate, development rate for all lifestages (except for the adults) and mortality rate for all lifestages as
	  * a Parameters object.
	  * Note: the time is advanced by one integration step.
	  * @param temperature - current temperature during this timestep
	  * @param fruitQuality - value between 0 and 1 inclusive representing fruit quality (affects mortality and development)
	  * @param ignoreFruit - boolean to determine whether or not to ignore the effect of fruit quality on the flies
	  * @param ignoreDiapause - boolean to determine whether or not to ignore the effect of diapause on the flies
	  * @param dt - step for numeric integration (Euler's method)
	  * @param timeStep - current step for the simulation
	  * @throws IllegalArgumentException if the male proportion is an invalid value (i.e. not between 0 and 1 inclusive).
	  */
	 public void computePopulation(double temperature, double fruitQuality, Parameters params, boolean ignoreFruit, boolean ignoreDiapause, double dt, double timeStep) { 
		 
		 // note: in order of indices: 0-eggs, 1-instar1, 2-instar2, 3-instar3, 4-pupae, 5-males, 6-females
		 
		// calculate fertility rate
		 double fertility = SolveParameters.solveSpecificFertility(temperature, params);
		 double fertilityDiapauseEffect = 1;
		 
		 if (!ignoreDiapause) { 
			 int year = ((int) timeStep) / 365;
			 int date = ((int) timeStep) % 365;
			 int offset = SWDModelReferenceClasses.Daylight.getOffSet(year);
			 double latitude = params.getParameter("latitude");
			 double hours = SWDModelReferenceClasses.Daylight.getDayLightHours(year, date + offset, latitude);
			 
			 double criticalT = params.getParameter("diapause critical temp");
			 double daylightHours = params.getParameter("diapause daylight hours");
			 
			 // NOTE: don't set s1 here since the previous value of s1 is needed to calculate s2
			 int tempS1 = SolveParameters.solveDiapauseMultS1(hours, temperature, s1, s2, criticalT, daylightHours); // diapause multiplier (s1)
			 s2 = SolveParameters.solveDiapauseMultS2(hours, s1, s2, daylightHours); // s2 value for current dt
			 s1 = tempS1; // s1 value for current dt
			 
			 fertilityDiapauseEffect = s1 * SolveParameters.solveFertilityDiapauseEffect(hours);

			 
			 if (s1 == 0 && !crossedDiapause && !addInitPop)
				 return;
			 if (s1 != 0 && !crossedDiapause) {
				 if (!addInitPop)
					 readPopulation(params);
				 crossedDiapause = true;
				 crossedDiapDay = (int) (timeStep);
			 }
			 
		 }
		 
		 fertility *= fertilityDiapauseEffect;
		 //fec[((int)timeStep) % 365] = fertilityDiapauseEffect;//TODO
				 
		 
		 double[] devMaxes = params.getArrayParameters("development max");
		 
		 double[] devRate = new double[11]; // development rates per stage
		 double[] mortalityNat = new double[13]; // mortality rates per stage, due to natural causes (food, etc.)
		 
		 double fruitQConstant = 0.5; // default value taken from the aphid paper
		 
		 double fruitEffectDevelopment = SolveParameters.solveDevelopmentPlantEffect(fruitQConstant, fruitQuality, params);
		 
		 for (int i = 0; i < 13; i ++) { // calculate the stage-specific mortality and development rates
			 if (i <= 4) // no development rates for adults
				 devRate[i] = SolveParameters.solveDev_Briere_Juvenile(temperature, devMaxes[i]); 
				 //devRate[i] = SolveParameters.solveDev_newData(temperature, devMaxes[i]);
			 else if (i > 5 && i < 12)
				 devRate[i - 1] = devMaxes[i - 1]; // female development is independent of temperature
			 mortalityNat[i] = SolveParameters.solveMortality(temperature, params, params.getStage(i));
			 
			 double fruitEffectMortality = SolveParameters.solveMortalityPlantEffect(fruitQConstant, fruitQuality, params, params.getStage(i));
			 
			 // plant effect is multiplicative on development, and summative on mortality
			 
			 if (ignoreFruit) { // if the user chose to ignore the effect of fruit quality on the flies
				 fruitEffectDevelopment = 1; // since fruit is a multiplier for development, to ignore fruit the multiplier is 1
				 fruitEffectMortality = 0; // since fruit is a sum on mortality, to ignore fruit the added factor is 0
			 }
			 
			 if (i < 5) // no development rate for adults
				 devRate[i] *= fruitEffectDevelopment; // fruit has a multiplicative effect on development rate
			 /*else if (i > 5 && i < 12)
				 devRate[i - 1] *= fruitEffectDevelopment; */
			 mortalityNat[i] += fruitEffectMortality; // fruit has a summative effect on mortality rate
		 }
		 
		 double[] tempFemalesPopulation = UtilityMethods.copyDoubleArray(currentFemaleStages); // ensure to use populations from the timestep before
		
		 // calculate the current populations of all the lifestages
		 
		 //double[] femStageConstantDev = params.getArrayParameters("constant dev rate");
		 double[] eggViabilities = params.getArrayParameters("egg viability");
		 double[] mortalitiesPred = params.getArrayParameters("mortality due to predation");
		
		 double maleProportion = params.getParameter("male proportion");
		 double maleProportion_advFemStage = 0; // no males develop from previous female lifestages
		
		 currentFemaleStages[0] = EulersMethod.getFemalesX(maleProportion, devRate[4], currentPupae, 
												mortalityNat[6], mortalitiesPred[6], devRate[5], currentFemaleStages[0], dt);
		
		 // here, it's ok to go in order since all the populations from the previous timestep are saved in femalesCurrentTimestep
		
		 for (int i = 1; i < currentFemaleStages.length; i ++) {
			 if (i < currentFemaleStages.length - 1) {
				 currentFemaleStages[i] = EulersMethod.getFemalesX(maleProportion_advFemStage, devRate[i + 4], tempFemalesPopulation[i - 1],
																		mortalityNat[i + 6], mortalitiesPred[i + 6], devRate[i + 5], 
																		currentFemaleStages[i], dt);
			 }
			 else {
				 currentFemaleStages[i] = EulersMethod.getFemalesX(maleProportion_advFemStage, devRate[i + 4], tempFemalesPopulation[i - 1],
						mortalityNat[i + 6], mortalitiesPred[i + 6], 0, // development rate N/A for females stage 7 
						currentFemaleStages[i], dt);
			 }
				
		 }
		 
		 // note: in order of indices: 0-eggs, 1-instar1, 2-instar2, 3-instar3, 4-pupae, 5-males, 6-females
		
		 // computed in reverse order to avoid excess use of temporary variables
		 currentMales = EulersMethod.getMales(devRate[4], maleProportion, mortalityNat[5], mortalitiesPred[5], currentPupae, currentMales, dt);
		 currentPupae = EulersMethod.getPupae(devRate[3], mortalityNat[4], mortalitiesPred[4], devRate[4], currentInst3, currentPupae, dt);
		 currentInst3 = EulersMethod.getInstX(devRate[2], mortalityNat[3], mortalitiesPred[3], devRate[3], currentInst2, currentInst3, dt);
		 currentInst2 = EulersMethod.getInstX(devRate[1], mortalityNat[2], mortalitiesPred[2], devRate[2], currentInst1, currentInst2, dt);
		 currentInst1 = EulersMethod.getInstX(devRate[0], mortalityNat[1], mortalitiesPred[1], devRate[1], currentEggs, currentInst1, dt);
		 currentEggs = EulersMethod.getEggs(fertility, eggViabilities, tempFemalesPopulation, currentEggs, mortalityNat[0], mortalitiesPred[0], devRate[0], dt);

		 
	 }
	 
}
