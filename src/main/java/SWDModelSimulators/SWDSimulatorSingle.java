package SWDModelSimulators;

import java.util.*;

import org.jfree.data.xy.XYSeries;

import SWDModelBaseObjects.Parameters;
import SWDModelBaseObjects.SWDCellSingle;
import SWDModelReferenceClasses.UtilityMethods;

/**
 * This class describes an SWDSimulatorSingle object, which is a simulator to run 
 * its cell datafield under various specified conditions.  
 * There are accessors for all the cell's accessors (to access the cell's and its
 * population's parameters).  
 * There are also methods to reset the parameters (i.e. the parameter map) either 
 * with arguments or by reading in from a file).
 * There are also mutators to run the cell for a specified time interval under 
 * specified temperature conditions.
 * 
 * @author Ellen Arteca
 *
 */

public class SWDSimulatorSingle { 
	
	private SWDCellSingle cell; // cell to run the simulation on
	
	private double timeStep; // current timestep
	private double dt = 0.05; // integration step (defaults to 0.05)
	
	private boolean injectFlies = false;
	
	/**
	 * Constructor to initialize the simulator object.  Reads in parameters from a specified
	 * file (if present).  If not reverts to default parameters.  
	 * Initializes cell with these parameters.
	 * @param dt - step for numeric integration (Euler's method)
	 * @param fileName - file to read parameters from
	 */
	public SWDSimulatorSingle(double dt, String fileName) {
		this.dt = dt;
		cell = new SWDCellSingle(new Parameters()); // initialize cell with default parameters
		setConfigParams(fileName); // try to read from config.txt if it exists
	}
	
	/**
	 * Constructor to initialize the simulator object.  Defaults to reading from config.txt if present in the
	 * current directory.  If not reverts to default parameters.  
	 * @param dt - step for numeric integration (Euler's method)
	 */
	public SWDSimulatorSingle(double dt) {
		this(dt, "config.txt");
	}
	
	/**
	 * Constructor to initialize the simulator object.  Sets parameters to those of the specified parameters object.
	 * @param dt - step for numeric integration (Euler's method)
	 * @param params - parameters object
	 */
	public SWDSimulatorSingle(double dt, Parameters params) {
		this.dt = dt;
		cell = new SWDCellSingle(params);
	}
	
	/**
	 * Constructor to initialize the simulator object, taking in all arguments for the parameters.
	 * @param latitude - latitude of the cell
	 * @param temp - constant temperature for the simulation (can be ignored when running simulations if variable temperatures are used)
	 * @param dt - step for numeric integration (Euler's method)
	 * @param maleProportion - percentage of adult flies which are male (value between 0 and 1 inclusive)
	 * @param tfinish - number of timesteps to run the simulation for
	 * @param initialEggs - initial number of eggs in the cell
	 * @param initialInst1 - initial number of instar 1 in the cell
	 * @param initialInst2 - initial number of instar 2 in the cell
	 * @param initialInst3 - initial number of instar 3 in the cell
	 * @param initialPupae - initial number of pupae in the cell
	 * @param initialMales - initial number of adult males in the cell
	 * @param initialFemales - initial number of adult females in the cell
	 * @param devMax - array of maximum development rates for all the lifestages (not including advanced adults)
//	 * @param devBeta - array of development betas for all the lifestages (not including adults)
//	 * @param devTmin - array of minimum temperatures for development for all lifestages (not including adults)
//	 * @param devTref - array of reference temperatures for development for all lifestages (not including adults)
//	 * @param devTmax - array of maximum temperatures for development for all lifestages (not including adults)
//	 * @param devQ - array of development q's for all the lifestages (not including adults)
//	 * @param amplitudeFert - amplitude of the curve
//	 * @param var1Fert - multiplier on the exponent
//	 * @param avgFert - average/mean temperature
//	 * @param stdevFert - standard deviation for temperature
//	 * @param TminFert - minimum temperature for reproduction
//	 * @param TmaxFert - maximum temperature for reproduction
	 * @param mortMax - array of maximum mortality rates for all lifestages
	 * @param mortTmin - array of minimum temperatures for all lifestages
	 * @param mortTmax - array of maximum temperatures for all lifestages
	 * @param mortTau - array of mortality tau's for all lifestages
	 * @param mortBeta0 - array of mortality beta0's for all lifestages
	 * @param mortBeta1 - array of mortality beta1's for all lifestages
	 * @param mortBeta2 - array of mortality beta2's for all lifestages
	 * @param mortBeta3 - array of mortality beta3's for all lifestages
	 * @param mortalitiesPredation - stage-specific mortality rate due to predation (same rate for all female lifestages)
	 * @param eggViabilities - viability of the eggs from the different female lifestages
//	 * @param femStageDevRates - female-stage constant development rates
	 * @param fruitN - value affecting how the fruit quality affect the flies
	 * @param fruitM - value determining how much effect the fruit quality has on the flies
	 * @param timeLag - the number of timesteps above the cutoff before the quality begins to decrease
	 * @param fruitBaseTemp - the minimum temperature for the fruit
	 * @param gtMultiplier - the multiplier for the g(T) result (temperature dependent measure of development rate)
	 * @param fruitHarvestCutoff - the fruit quality cutoff (timelag timesteps after reaching this cutoff, the quality begins to decrease)
	 * @param fruitHarvestDrop - the fruit quality drop (determines how fruit quality decreases per timestep after passing cutoff)
	 * @param criticalT - critical temperature for the diapause model
	 * @param daylightHours - daylight hours cutoff for the diapause model
	 */
	public SWDSimulatorSingle(double latitude, double temp, double dt, double maleProportion, double tfinish,
							double initialEggs, double initialInst1, double initialInst2, double initialInst3, 
							double initialPupae, double initialMales, double[] initialFemales,
							double[] devMax, 
							//double[] devBeta, double[] devTmin, double[] devTref, double[] devTmax, double[] devQ, 
							//double amplitudeFert, double var1Fert, double avgFert, double stdevFert, double TminFert, 
							double TmaxFert,
							double[] mortMax, double[] mortTmin, double[] mortTmax, double[] mortTau, 
							double[] mortBeta0, double[] mortBeta1, double[] mortBeta2, double[] mortBeta3,
							double[] mortalitiesPredation, double[] eggViabilities, //double[] femStageDevRates, 
							double fruitN, double fruitM, double timeLag, double fruitBaseTemp, double gtMultiplier, 
							double fruitHarvestCutoff, double fruitHarvestDrop,
							double criticalT, double daylightHours) {
		
		this.dt = dt;
		cell = new SWDCellSingle(new Parameters()); // initialize cell with default parameters (initialized here to avoid NullPointer when calling setArgumentedParams
		this.setArgumentedParams(latitude, temp, maleProportion, tfinish, 
				initialEggs, initialInst1, initialInst2, initialInst3, initialPupae, initialMales, initialFemales,
				devMax, //devBeta, devTmin, devTref, devTmax, devQ, 
				//amplitudeFert, var1Fert, avgFert, stdevFert, TminFert, 
				TmaxFert,
				mortMax, mortTmin, mortTmax, mortTau, mortBeta0, mortBeta1, mortBeta2, mortBeta3,
				mortalitiesPredation, eggViabilities, //femStageDevRates, 
				fruitN, fruitM, timeLag, fruitBaseTemp, gtMultiplier, fruitHarvestCutoff, fruitHarvestDrop,
				criticalT, daylightHours); // reset parameters in the simulator and the cell to those specified
	}
	
	/**
	 * No argument constructor, initializes everything to default values, default dt = 0.05.  
	 */
	public SWDSimulatorSingle() {
		dt = 0.05;
		cell = new SWDCellSingle(new Parameters()); // initialize cell with default parameters
	}
	
	/**
	 * Method to set the parameters to their default values.
	 */
	public void setDefaultParams() {
		Parameters params = new Parameters(); // default params
		cell.resetCellParams(params, true); // reset the fruit parameters
	}
	
	/**
	 * Method to reset a specific parameter to a specified value.
	 * @param parameter - the name (key in the map) of the parameter to reset
	 * @param newValue - new value for this parameter
	 * @return a String message describing the outcome ("Success!" if everything worked, 
	 * 			or an appropriate error message if something went wrong)
	 */
	public String setSingleParameter(String param, double newVal) {
		return cell.setSingleParameter(param, newVal);
	}
	
	/**
	 * Method to get the value of a specific parameter.
	 * @param param - the parameter to get the value of
	 * @return the value of the parameter
	 */
	public double getSpecificParameter(String param) {
		return cell.getSpecificParameter(param);
	}
	
	/**
	 * Method to reset the parameters in the cell to those specified.
	 * Note that maps don't allow duplicate keys, so adding the new parameters automatically replaces the old
	 * parameters with the same key.
	 * @param latitude - latitude of the cell
	 * @param temp - constant temperature for the simulation (can be ignored when running simulations if variable temperatures are used)
	 * @param maleProportion - percentage of adult flies which are male (value between 0 and 1 inclusive)
	 * @param tfinish - number of timesteps to run the simulation for
	 * @param initialEggs - initial number of eggs in the cell
	 * @param initialInst1 - initial number of instar 1 in the cell
	 * @param initialInst2 - initial number of instar 2 in the cell
	 * @param initialInst3 - initial number of instar 3 in the cell
	 * @param initialPupae - initial number of pupae in the cell
	 * @param initialMales - initial number of adult males in the cell
	 * @param initialFemales - initial number of adult females in the cell
	 * @param devMax - array of maximum development rates for all the lifestages (not including adults)
//	 * @param devBeta - array of development betas for all the lifestages (not including adults)
//	 * @param devTmin - array of minimum temperatures for development for all lifestages (not including adults)
//	 * @param devTref - array of reference temperatures for development for all lifestages (not including adults)
//	 * @param devTmax - array of maximum temperatures for development for all lifestages (not including adults)
//	 * @param devQ - array of development q's for all the lifestages (not including adults)
//	 * @param amplitudeFert - amplitude of the curve
//	 * @param var1Fert - multiplier on the exponent
//	 * @param avgFert - average/mean temperature
//	 * @param stdevFert - standard deviation for temperature
//	 * @param TminFert - minimum temperature for reproduction
//	 * @param TmaxFert - maximum temperature for reproduction
	 * @param mortMax - array of maximum mortality rates for all lifestages
	 * @param mortTmin - array of minimum temperatures for all lifestages
	 * @param mortTmax - array of maximum temperatures for all lifestages
	 * @param mortTau - array of mortality tau's for all lifestages
	 * @param mortBeta0 - array of mortality beta0's for all lifestages
	 * @param mortBeta1 - array of mortality beta1's for all lifestages
	 * @param mortBeta2 - array of mortality beta2's for all lifestages
	 * @param mortBeta3 - array of mortality beta3's for all lifestages
	 * @param mortalitiesPredation - stage-specific mortality rate due to predation (same rate for all female lifestages)
	 * @param eggViabilities - viability of the eggs from the different female lifestages
//	 * @param femStageDevRates - female-stage constant development rates
	 * @param fruitN - value affecting how the fruit quality affect the flies
	 * @param fruitM - value determining how much effect the fruit quality has on the flies
	 * @param timeLag - the number of timesteps above the cutoff before the quality begins to decrease
	 * @param fruitBaseTemp - the minimum temperature for the fruit
	 * @param gtMultiplier - the multiplier for the g(T) result (temperature dependent measure of development rate)
	 * @param fruitHarvestCutoff - the fruit quality cutoff (timelag timesteps after reaching this cutoff, the quality begins to decrease)
	 * @param fruitHarvestDrop - the fruit quality drop (determines how fruit quality decreases per timestep after passing cutoff)
	 * @param criticalT - critical temperature for the diapause model
	 * @param daylightHours - daylight hours cutoff for the diapause model
	 * @return a String message describing the outcome ("Success!" if everything worked, 
	 * 			or an appropriate error message if something went wrong)
	 */
	public String setArgumentedParams(double latitude, double temp, double maleProportion, double tfinish,
			double initialEggs, double initialInst1, double initialInst2, double initialInst3, 
			double initialPupae, double initialMales, double[] initialFemales,
			double[] devMax, 
			//double[] devBeta, double[] devTmin, double[] devTref, double[] devTmax, double[] devQ, 
			//double amplitudeFert, double var1Fert, double avgFert, double stdevFert, double TminFert, 
			double TmaxFert,
			double[] mortMax, double[] mortTmin, double[] mortTmax, double[] mortTau, 
			double[] mortBeta0, double[] mortBeta1, double[] mortBeta2, double[] mortBeta3,
			double[] mortalitiesPredation, double[] eggViabilities, //double[] femStageDevRates, 
			double fruitN, double fruitM, double timeLag, double fruitBaseTemp, double gtMultiplier, 
			double fruitHarvestCutoff, double fruitHarvestDrop,
			double criticalT, double daylightHours) {
		
			Parameters params = cell.getParams(); // current cell parameters
		
			String status = params.setArgumentedParams(latitude, temp, maleProportion, tfinish, initialEggs, initialInst1, initialInst2, initialInst3, 
					initialPupae, initialMales, initialFemales, devMax, //devBeta, devTmin, devTref, devTmax, devQ, 
					//amplitudeFert, var1Fert, avgFert, stdevFert, TminFert, 
					TmaxFert, 
					mortMax, mortTmin, mortTmax, mortTau, mortBeta0, mortBeta1, 
					mortBeta2, mortBeta3, mortalitiesPredation, eggViabilities, //femStageDevRates, 
					fruitN, fruitM, timeLag, fruitBaseTemp, 
					gtMultiplier, fruitHarvestCutoff, fruitHarvestDrop,
					criticalT, daylightHours);
			
			if (status.equals("Success!"))
				cell.resetCellParams(params, true); // once params has been reset, reset the cell parameters
			return status;
	}
	
	 /**
	 * Resets parameters for the life sciences...  
	 * Method to reset the parameters in the cell to those specified.
	 * Note that maps don't allow duplicate keys, so adding the new parameters automatically replaces the old
	 * parameters with the same key.
	 * @param latitude - latitude of the cell
	 * @param temp - constant temperature for the simulation (can be ignored when running simulations if variable temperatures are used)
	 * @param maleProportion - percentage of adult flies which are male (value between 0 and 1 inclusive)
	 * @param tfinish - number of timesteps to run the simulation for
	 * @param initialEggs - initial number of eggs in the cell
	 * @param initialInst1 - initial number of instar 1 in the cell
	 * @param initialInst2 - initial number of instar 2 in the cell
	 * @param initialInst3 - initial number of instar 3 in the cell
	 * @param initialPupae - initial number of pupae in the cell
	 * @param initialMales - initial number of adult males in the cell
	 * @param initialFemales - initial number of adult females in the cell
	 * @param devMax - array of maximum development rates for all the lifestages (not including adults)
//	 * @param devTmin - array of minimum temperatures for development for all lifestages (not including adults)
//	 * @param devTmax - array of maximum temperatures for development for all lifestages (not including adults)
	 * @param mortMax - array of maximum mortality rates for all lifestages
	 * @param mortTmin - array of minimum temperatures for all lifestages
	 * @param mortTmax - array of maximum temperatures for all lifestages
	 * @param fruitM - value determining how much effect the fruit quality has on the flies
	 * @param timeLag - the number of timesteps above the cutoff before the quality begins to decrease
	 * @param fruitBaseTemp - the minimum temperature for the fruit
	 * @param gtMultiplier - the multiplier for the g(T) result (temperature dependent measure of development rate)
	 * @param fruitHarvestCutoff - the fruit quality cutoff (timelag timesteps after reaching this cutoff, the quality begins to decrease)
	 * @param fruitHarvestDrop - the fruit quality drop (determines how fruit quality decreases per timestep after passing cutoff)
	 * @param criticalT - critical temperature for the diapause model
	 * @param daylightHours - daylight hours cutoff for the diapause model
	 * @return a String message describing the outcome ("Success!" if everything worked, 
	 * 			or an appropriate error message if something went wrong)
	 */
	public String setBioArgumentedParams(double latitude, double temp, double maleProportion, double tfinish,
			double initialEggs, double initialInst1, double initialInst2, double initialInst3, 
			double initialPupae, double initialMales, double[] initialFemales,
			double[] devMax, //double[] devTmin, double[] devTmax,  
			double[] mortMax, double[] mortTmin, double[] mortTmax, 
			double[] mortalitiesPred, double[] eggViabilities, //double[] femaleConstantDevRates, 
			double fruitN, double fruitM, double timeLag, double fruitBaseTemp, double gtMultiplier, 
			double fruitHarvestCutoff, double fruitHarvestDrop,
			double criticalT, double daylightHours) {
		
			Parameters params = cell.getParams(); // current cell parameters
		
			String status = params.setBioArgumentedParams(latitude, temp, maleProportion, tfinish, initialEggs, initialInst1, 
					initialInst2, initialInst3, initialPupae, initialMales, initialFemales, 
					devMax, //devTmin, devTmax, 
					mortMax, mortTmin, mortTmax, 
					mortalitiesPred, eggViabilities, //femaleConstantDevRates,
					fruitN, fruitM, timeLag, fruitBaseTemp, gtMultiplier, fruitHarvestCutoff, fruitHarvestDrop,
					criticalT, daylightHours);
			
			if (status.equals("Success!"))
				cell.resetCellParams(params, true); // once paramMap has been reset, reset the cell parameters
			return status;
	}
	
	
	/**
	 * Method to reset the parameters in the map to those contained in the file.  Note that this file does
	 * not have to contain all the parameters: the method will replace those present and leave the other 
	 * values unmodified.
	 * @param configFileName - path to the file chosen
	 * @return a String message of the outcome of the reconfiguration (if there was an error, specify what
	 * 			the error was and which line it occurred at; if nothing changed report this; or if everything
	 * 			was fine return "Success!")
	 */
	public String setConfigParams(String configFileName) { // returns an error message, or "Success!" if everything works
		
		Parameters params = cell.getParams(); // current cell parameters

		String toReturn = params.setConfigParams(configFileName);
		if (toReturn.equals("Success!"))
			cell.resetCellParams(params, true); // apply changes to the cell, reset parameters to those read in (including fruit params)
		
		return toReturn;
	}
	
	/**
	 * Method to reset the parameters the parameters in the simulator's map to those contained in the
	 * incoming map.  Note that this map does not have to contain all the parameters: the method will replace 
	 * those present and leave the other values unmodified. 
	 * @param inputMap - the incoming map of parameters to reset
	 */
	public String setMapParams(Map<String, Double> inputMap) {
		Parameters params = cell.getParams(); // current cell parameters
		
		String toReturn = params.setMapParams(inputMap, true);
		if (toReturn.equals("Success!"))
			cell.resetCellParams(params, true); // reset fruit params in the single cell simulator
		
		return toReturn;
	}
	
	/**
	 * Method to run the simulation for the specified number of timesteps (advances time in the cell by the 
	 * specified interval and runs all the life processes accordingly).
	 * Note: this assumes temperature is constant during runtime.
	 * @param temperature - the current/constant temperature of the cell during the time to run for
	 * @param numTimeSteps - the length of time (i.e. the number of timesteps) to run for
	 * @param ignoreFruit - boolean to determine whether or not to ignore the effect of fruit quality on the flies
	 * @param ignoreDiapause - boolean to determine whether or not to ignore the effect of diapause on the flies
	 * @throws IllegalArgumentException if the time to run the simulation for is negative
	 */
	public void run(double temperature, double numTimeSteps, boolean ignoreFruit, boolean ignoreDiapause) { // run the model with a constant temperature
		
		if (numTimeSteps < 0)
			throw new IllegalArgumentException("no negative time.");
		
		for (double i = 0; UtilityMethods.round2Decimals(i) < numTimeSteps; i += this.dt) { // run from time = 0 to the specified number of timesteps
			cell.stepFoward(temperature, ignoreFruit, ignoreDiapause, dt, timeStep); // run the cell with specified temperature!
			timeStep += dt;
		}
	}
	
	/**
	 * Method to run the simulation for the specified number of timesteps (advances time in the cell by the
	 * specified interval and runs all the life processes accordingly).
	 * Note: this runs with variable temperatures (assumes one temperature value per timestep)
	 * @param temperatures - ArrayList of temperature values, one per timestep 
	 * @param numTimeSteps - the length of time (i.e. the number of timesteps) to run for
	 * @param ignoreFruit - boolean to determine whether or not to ignore the effect of fruit quality on the flies
	 * @param ignoreDiapause - boolean to determine whether or not to ignore the effect of diapause on the flies
	 * @param startDay - day to inject the initial populations
	 * @throws IllegalArgumentException if the time to run the simulation for is negative
	 */
	public void run(ArrayList<Double> temperatures, double numTimeSteps, boolean ignoreFruit, boolean ignoreDiapause, int startDay) { 
		
		if (numTimeSteps < 0)
			throw new IllegalArgumentException("no negative time.");
		
		int index = ((int) timeStep) % temperatures.size(); // current timestep of the cell, to match with the corresponding temperature value in the list
		
		if (startDay >= 0)
			cell.setAddInitPop(true); // if valid startday, set variable to ensure that init pop isn't added before injection date, regardless of diapause
		
		for (double i = 0; UtilityMethods.round2Decimals(i) < numTimeSteps; i += dt) {
			if ((int) index >= temperatures.size()) // if there is not enough temperature data, it reloops to the beginning
				index = 0;
			if ((int) timeStep == startDay && !injectFlies) {
				injectFlies = true;
				cell.readInitFlies(); // read in initial populations on the chosen date
			}
			cell.stepFoward(temperatures.get((int) index), ignoreFruit, ignoreDiapause, dt, timeStep); // assume temperature given daily
			index += dt;
			timeStep += dt;
		}
	}
	
	/**
	 * Method to reset the value of dt (the integration step)
	 * @param dt - the new value to reset the integration step to
	 */
	public void setDT(double dt) {this.dt = dt; }
	
	/**
	 * Method to return a copy of the Parameters object in the cell (i.e. all the 
	 * parameters used in the cell).
	 * @return a copy of the Parameters object in the cell
	 */
	public Parameters getParams() { return cell.getParams(); }
	
	// -----------------------------------------------------------------------------------------FRUIT
	
	/**
	 * Method to return the current fruit quality of the cell
	 * @return current fruit quality of the cell
	 */
	public double getFruitQuality() {return cell.getFruitQuality(); }
	
	/**
	 * Method to return the day where the fruit quality reached 1.
	 * @return first day the fruit quality reached it's max.
	 */
	public double getDayCrossedMaxFruit() { return cell.getDayCrossedMaxFruit(); }
	
	// -----------------------------------------------------------------------------------------THRESHOLD POPULATION
	
	/**
	 * Method to return the day at which the total cumulative female population (for all 7 female 
	 * lifestages) passed the threshold population at the specified index.
	 * @param index - the specified index
	 * @return the day the threshold population was passed
	 */
	public double getThresholdPopDay(int index) { 
		return cell.getThresholdPopDay(index);
	}
	
	/**
	 * Method to set the threshold population at the specified index.
	 * @param index - the specified index
	 * @param thresholdPop - the threshold population specified
	 */
	public void setThresholdPopDay(int index, double thresholdPop) { 
		cell.setThresholdPopDay(index, thresholdPop); 
	}

	
	// -----------------------------------------------------------------------------------------CURRENT POPULATIONS
	
	/**
	  * Method to return the current number of eggs in the population
	  * @return the number of eggs currently in the population
	  */
	public double getEggs() {return cell.getEggs(); }
	
	/**
	  * Method to return the current number of instar 1 in the population
	  * @return the number of instar 1 currently in the population
	  */
	public double getInst1() {return cell.getInst1(); }
	
	/**
	  * Method to return the current number of instar 2 in the population
	  * @return the number of instar 2 currently in the population
	  */
	public double getInst2() {return cell.getInst2(); }
	
	/**
	  * Method to return the current number of instar 3 in the population
	  * @return the number of instar 3 currently in the population
	  */
	public double getInst3() {return cell.getInst3(); }
	
	/**
	  * Method to return the current number of pupae in the population
	  * @return the number of pupae currently in the population
	  */
	public double getPupae() {return cell.getPupae(); }
	
	/**
	  * Method to return the current number of males in the population
	  * @return the number of males currently in the population
	  */
	public double getMales() {return cell.getMales(); }
	
	/**
	  * Method to return the current number of females in the population
	  * @return the number of females currently in the population
	  */
	public double getFemales() {return cell.getFemales(); }
	
	/**
	 * Method to return the current number of adult females of each stage in 
	 * the cell
	 * @return the current number of adult females of each stage in the cell
	 */
	public double[] getFemStages() {return cell.getFemStages(); }
	
	/**
	  * Method to return the current total number of bugs in the population
	  * @return the total number of flies at all life stages currently in the population
	  */
	public double getTotalPopulation() {return cell.getTotalPopulation(); }
	
	// -----------------------------------------------------------------------------------------MAXIMUM POPULATIONS
	
	/**
	 * Method to return the maximum female population 
	 * Note: this is the cumulative sum of all female stages
	 * @return the maximum female population
	 */
	public double getMaxFemales() { return cell.getMaxFemales(); }
	
	/**
	 * Method to return the maximum male population
	 * @return the maximum male population
	 */
	public double getMaxMales() { return cell.getMaxMales(); }
	
	/**
	 * Method to return the maximum pupae population
	 * @return the maximum pupae population
	 */
	public double getMaxPupae() { return cell.getMaxPupae(); }
	
	/**
	 * Method to return the maximum instar3 population
	 * @return the maximum instar3 population
	 */
	public double getMaxInst3() { return cell.getMaxInst3(); }
	
	/**
	 * Method to return the maximum instar2 population
	 * @return the maximum instar2 population
	 */
	public double getMaxInst2() { return cell.getMaxInst2(); }
	
	/**
	 * Method to return the maximum instar1 population
	 * @return the maximum instar1 population
	 */
	public double getMaxInst1() { return cell.getMaxInst1(); }
	
	/**
	 * Method to return the maximum egg population
	 * @return the maximum egg population
	 */
	public double getMaxEggs() { return cell.getMaxEggs(); }
	
	// -----------------------------------------------------------------------------------------MAXIMUM POPULATIONS TIMESTEP
	
	/**
	 * Method to return the date for the maximum female population 
	 * Note: this considers the cumulative sum of all female stages
	 * @return the date for the maximum female population
	 */
	public double getDayMaxFemales() { return cell.getDayMaxFemales(); }
	
	/**
	 * Method to return the date for the maximum male population
	 * @return the date for the maximum male population
	 */
	public double getDayMaxMales() { return cell.getDayMaxMales(); }
	
	/**
	 * Method to return the date for the maximum pupae population
	 * @return the date for the maximum pupae population
	 */
	public double getDayMaxPupae() { return cell.getDayMaxPupae(); }
	
	/**
	 * Method to return the date for the maximum instar3 population
	 * @return the date for the maximum instar3 population
	 */
	public double getDayMaxInst3() { return cell.getDayMaxInst3(); }
	
	/**
	 * Method to return the date for the maximum instar2 population
	 * @return the date for the maximum instar2 population
	 */
	public double getDayMaxInst2() { return cell.getDayMaxInst2(); }
	
	/**
	 * Method to return the date for the maximum instar1 population
	 * @return the date for the maximum instar1 population
	 */
	public double getDayMaxInst1() { return cell.getDayMaxInst1(); }
	
	/**
	 * Method to return the date for the maximum egg population
	 * @return the date for the maximum egg population
	 */
	public double getDayMaxEggs() { return cell.getDayMaxEggs(); }
	
	// -----------------------------------------------------------------------------------------CUMULATIVE POPULATIONS
	
	/**
	 * Method to return the total cumulative female population 
	 * Note: this is the cumulative sum of all female stages
	 * @return the total cumulative female population
	 */
	public double getTotFemales() { return cell.getTotFemales(); }
	
	/**
	 * Method to return the total cumulative male population
	 * @return the total cumulative male population
	 */
	public double getTotMales() { return cell.getTotMales(); }
	
	/**
	 * Method to return the total cumulative pupae population
	 * @return the total cumulative pupae population
	 */
	public double getTotPupae() { return cell.getTotPupae(); }
	
	/**
	 * Method to return the total cumulative instar3 population
	 * @return the total cumulative instar3 population
	 */
	public double getTotInst3() { return cell.getTotInst3(); }
	
	/**
	 * Method to return the total cumulative instar2 population
	 * @return the total cumulative instar2 population
	 */
	public double getTotInst2() { return cell.getTotInst2(); }
	
	/**
	 * Method to return the total cumulative instar1 population
	 * @return the total cumulative instar1 population
	 */
	public double getTotInst1() { return cell.getTotInst1(); }
	
	/**
	 * Method to return the maximum egg population
	 * @return the maximum egg population
	 */
	public double getTotEggs() { return cell.getTotEggs(); }
		
	
	
	// -----------------------------------------------------------------------------------------TEMPERATURE
	/**
	 * Method to return the current temperature of the cell
	 * @return the temperature of the cell at the current timestep
	 */
	public double getTemperature() {return cell.getTemperature(); }
	
	// -----------------------------------------------------------------------------------------TIMESTEP
	
	/**
	  * Method to return the current timestep
	  * @return the current timestep
	  */
	public double getTimeStep() {return timeStep; }
	
	/**
	  * Reset to simulation to its state at timestep 0.
	  */
	public void resetTime() {
		timeStep = 0;
		cell.resetTime(); // reset the cell to timestep 0
		injectFlies = false;
	}
	
	/**
	  * Method to return the day the diapause threshold was crossed
	  * @return the day the diapause threshold was crossed (-1 if never crossed)
	  */
	public int getCrossedDiapDay() {
		return cell.getCrossedDiapDay();
	}
	
	// -----------------------------------------------------------------------------------------DATA SERIES
	
	/**
	 * Method to return the series of data points (current population for each timestep up to the current
	 * timestep) for eggs.
	 * @return the current data series for eggs
	 */
	public XYSeries getEggSeries() {return cell.getEggSeries(); }
	
	/**
	 * Method to return the series of data points (current population for each timestep up to the current
	 * timestep) for instar1.
	 * @return the current data series for instar1
	 */
	public XYSeries getInst1Series() {return cell.getInst1Series(); }
	
	/**
	 * Method to return the series of data points (current population for each timestep up to the current
	 * timestep) for instar2.
	 * @return the current data series for instar2
	 */
	public XYSeries getInst2Series() {return cell.getInst2Series(); }
	
	/**
	 * Method to return the series of data points (current population for each timestep up to the current
	 * timestep) for instar3.
	 * @return the current data series for instar3
	 */
	public XYSeries getInst3Series() {return cell.getInst3Series(); }
	
	/**
	 * Method to return the series of data points (current population for each timestep up to the current
	 * timestep) for pupae.
	 * @return the current data series for pupae
	 */
	public XYSeries getPupaeSeries() {return cell.getPupaeSeries(); }
	
	/**
	 * Method to return the series of data points (current population for each timestep up to the current
	 * timestep) for adult males.
	 * @return the current data series for adult males
	 */
	public XYSeries getMalesSeries() {return cell.getMalesSeries(); }
	
	/**
	 * Method to return the series of data points (current population for each timestep up to the current
	 * timestep) for adult females.
	 * @return the current data series for adult females
	 */
	public XYSeries getFemalesSeries() {return cell.getFemalesSeries(); }
	
	/**
	 * Method to return the series of data points (current fruit quality for each timestep up to the current
	 * timestep).
	 * @return the current data series for fruit quality
	 */
	public XYSeries getFruitQualitySeries() {return cell.getFruitQualitySeries(); }
	
	/**
	 * Method to return the series of data points (curren population for each timestep up to he current timestep)
	 * for the selected adult female lifestage.
	 * @param index - the selected female lifestage
	 * @return the current data series for the selected female lifestage
	 */
	public XYSeries getFemaleStageSeries(int index) {return cell.getFemaleStageSeries(index); }
	
}