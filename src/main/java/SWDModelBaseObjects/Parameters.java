package SWDModelBaseObjects;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import SWDModelReferenceClasses.UtilityMethods;

/**
 * This object class represents all the parameters in the SWDCellSingle object.  The 
 * parameters are stored in a map (they are all doubles) with String keys as the name
 * of the parameter.
 * There are various methods to access the parameters - the user can choose to get a 
 * map of all the parameters, parameters in arrays, or individual parameters.  
 * There are also methods to reset the values of the parameters through various means
 * (reading from a file, a map, a Parameters object); the user can also specify to 
 * not reset the fruit parameters, only reset the fruit parameters, or reset an 
 * individual parameter.
 * There is also a method to facilitate printing of the parameters to a file (in a 
 * format so that it can later be read in).
 * If there are errors in the input, the user is notified via a String describing the 
 * error (if parameters are being reset) or an IllegalArgumentException is thrown (if 
 * parameters are being accessed).
 * Methods described in-code.
 * 
 * @author Ellen Arteca
 * @author MikeD
 *
 */
public class Parameters {
	
	private Map<String, Double> paramMap; // map containing the parameters , linked name to value
	private String[] names = {"eggs", "instar1", "instar2", "instar3", "pupae", "males", "females" }; // all the lifestages (there are 7 females)
	
	//private String[] fertilityParams = {"amplitude", "var1", "mean temp", "stdev temp", "tmin", "tmax" }; // fertility parameters
	private String[] mortalityParams = {"max", "min temp", "max temp", "tau", "beta0", "beta1", "beta2", "beta3" };  // mortality parameters
	//private String[] developmentParams = {"max", "beta", "tmin", "tref", "tmax", "q" }; // development parameters
	private String[] fruitParams = {"n", "m", "time lag", "base temp", "gt multiplier", "harvest cutoff", "harvest drop" }; // fruit parameters
	
	/**
	 * Constructor to initialize a default Parameters object.  Creates a map to store the parameters,
	 * and stores all the default parameters.
	 */
	public Parameters() {
		paramMap = new HashMap<String, Double>();
		setDefaultParams(); // set parameters to default values, in the map
	}
	
	/**
	 * Constructor to initialize a Parameters object with parameters specified from a file.
	 * Creates a map to store the parameters from the file (if any are not specified in the file,
	 * the default value for this parameter is used instead).
	 * @param fileName - file to read parameters from
	 */
	public Parameters(String fileName) {
		paramMap = new HashMap<String, Double>();
		setDefaultParams(); // set parameters to default values, in the map
		setConfigParams(fileName); // replace the default parameters with those present in the file (if present)
	}
	
	/**
	 * Constructor to initialize a Parameters object identical to another Parameters object.
	 * @param params - Parameters object to copy
	 */
	public Parameters(Parameters params) {
		paramMap = new HashMap<String, Double>();
		setDefaultParams(); // set parameters to default values in the map
		setCopyParams(params, true); // copy the values from the incoming Parameters object (including the fruit parameters)
	}
	
	/**
	 * Method to set the parameters in paramMap to their default values.
	 */
	public void setDefaultParams() {
		// fruit parameters
		paramMap.put("fruit n", 4.);
		paramMap.put("fruit m", 0.75);
		paramMap.put("fruit time lag", 50.);
		paramMap.put("fruit base temp", 4.);
		paramMap.put("fruit gt multiplier", 4.);
		paramMap.put("fruit harvest cutoff", 0.95);
		paramMap.put("fruit harvest drop", 0.1);
		
		// diapause parameters
		paramMap.put("diapause critical temp", 18.);
		paramMap.put("diapause daylight hours", 10.);
		
		// general parameters
		paramMap.put("time", 100.);
		paramMap.put("constant temp", 15.);
		paramMap.put("male proportion", 0.5);
		paramMap.put("latitude", 46.49); // Sudbury's latitude
		
		// initial populations
		paramMap.put("initial eggs", 3.);
		paramMap.put("initial instar1", 5.);
		paramMap.put("initial instar2", 7.);
		paramMap.put("initial instar3", 4.);
		paramMap.put("initial pupae", 8.);
		paramMap.put("initial males", 5.);
		
		for (int i = 0; i < 7; i ++) // initial populations of each female lifestage
			 paramMap.put("initial females" + (i + 1), 4.);
		
		// fertility parameters
		/*paramMap.put("fertility amplitude", 10.0);
		paramMap.put("fertility var1", 0.2); 
		paramMap.put("fertility mean temp", 19.6); 
		paramMap.put("fertility stdev temp", 2.0);
		paramMap.put("fertility tmin", 5.); */
		paramMap.put("fertility tmax", 30.);
		
		// stage-specific parameters
		for (int i = 0; i < 13; i ++) {
			String stage = "females" + (i - 5); 
			if (i < 6)
				stage = names[i];
			
			/*if (i < 5) { // Recall: no development rates for adults
				// development parameters
				paramMap.put(stage + " development beta", 1.);
				paramMap.put(stage + " development tmin", 10.);
				paramMap.put(stage + " development tref", 16.);
				paramMap.put(stage + " development tmax", 32.);
				paramMap.put(stage + " development q", 1.);
			}*/
			
			// mortality parameters
			paramMap.put(stage + " mortality min temp", 3.);
			paramMap.put(stage + " mortality max temp", 33.);
			paramMap.put(stage + " mortality tau", 8.1776);
			paramMap.put(stage + " mortality beta1", -0.0077);
			paramMap.put(stage + " mortality beta2", 0.00032);
			paramMap.put(stage + " mortality beta3", -0.000002);		
			
			paramMap.put(stage + " mortality due to predation", 0.); // stage-specific mortality rate due to predation
		}
		
		paramMap.put(names[0] + " development max", 0.72); // eggs
		paramMap.put(names[0] + " mortality max", 0.3288);
		paramMap.put(names[0] + " mortality beta0", 0.1602);
		
		paramMap.put(names[1] + " development max", 0.94); // inst1
		paramMap.put(names[1] + " mortality max", 0.2688);
		paramMap.put(names[1] + " mortality beta0", 0.1402);
		
		paramMap.put(names[2] + " development max", 0.68); // inst2
		paramMap.put(names[2] + " mortality max", 0.1020);
		paramMap.put(names[2] + " mortality beta0", 0.0846);
		
		paramMap.put(names[3] + " development max", 0.32); // inst3
		paramMap.put(names[3] + " mortality max", 0.1068);
		paramMap.put(names[3] + " mortality beta0", 0.0862);
		
		paramMap.put(names[4] + " development max", 0.17); // pupae
		paramMap.put(names[4] + " mortality max", 0.0303);
		paramMap.put(names[4] + " mortality beta0", 0.0607);
		
		paramMap.put(names[5] + " mortality max", 0.1398); // males
		paramMap.put(names[5] + " mortality beta0", 0.0972);
		
		paramMap.put("females1 development max", 1./80); // fem1
		paramMap.put("females1 mortality max", 0.0537); 
		paramMap.put("females1 mortality beta0", 0.0685);
		
		paramMap.put("females2 development max", 1./10); // fem2
		paramMap.put("females2 mortality max", 0.1200); 
		paramMap.put("females2 mortality beta0", 0.0906);
		
		paramMap.put("females3 development max", 1./10); // fem3
		paramMap.put("females3 mortality max", 0.4500); 
		paramMap.put("females3 mortality beta0", 0.2006);
		
		paramMap.put("females4 development max", 1./5); // fem4
		paramMap.put("females4 mortality max", 0.0); 
		paramMap.put("females4 mortality beta0", 0.0506);
		
		paramMap.put("females5 development max", 1./4); // fem5
		paramMap.put("females5 mortality max", 0.7500); 
		paramMap.put("females5 mortality beta0", 0.3006);
		
		paramMap.put("females6 development max", 1./5); // fem6
		paramMap.put("females6 mortality max", 0.6000); 
		paramMap.put("females6 mortality beta0", 0.2506);
		
		paramMap.put("females7 mortality max", 0.8367); // fem7
		paramMap.put("females7 mortality beta0", 0.3295);
		
		// female-stage specific egg viabilities
		paramMap.put("females1 egg viability", 0.832);
		paramMap.put("females2 egg viability", 0.807);
		paramMap.put("females3 egg viability", 0.763);
		paramMap.put("females4 egg viability", 0.556);
		paramMap.put("females5 egg viability", 0.324);
		paramMap.put("females6 egg viability", 0.257);
		paramMap.put("females7 egg viability", 0.);
		
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
	public String setConfigParams(String configFileName){ // returns an error message, or "Success!" if everything works
		int lineNumber = 1; // current line number
		boolean changed = false; // have any of the parameters been changed? true or false
		
		Map<String, Double> newParams = getMap(); // copy of the current parameter map
		
		try {
			Scanner fileIn = new Scanner(new File(configFileName));
			while (fileIn.hasNextLine()) {
				String line = fileIn.nextLine();
				String[] splitLine = line.split(": "); // parameters delimited by their respective labels with ": "
				
				if (newParams.containsKey((splitLine[0]).toLowerCase())) { // replace parameters in map with the corresponding ones in the file
					newParams.put((splitLine[0]).toLowerCase(), Double.parseDouble(splitLine[1]));
					changed = true; // if anything has been changed, update the flag
				}
				lineNumber ++;
			}

			fileIn.close();
			if (!changed) // if no parameters were changed, tell the user
				return "No parameters were changed (no valid input in this file).";
			String validMessage = checkMap(newParams, true, true); // check the fruit params and the non-fruit params
			
			if (!validMessage.equals("Success!"))
				return validMessage;
			
			setMapParams(newParams, true); // if it's reached this point, the parameters are all ok and the paramMap datafield can be reset
			return "Success!";
			
		} catch(FileNotFoundException e) {
			return "File not found, using default parameters."; // if the file was not found
		} catch(NumberFormatException e) {
			return "Input error - invalid value - on line " + lineNumber; // invalid input linked to valid name
		} catch (ArrayIndexOutOfBoundsException e) { 
			return "Input error - value not present - on line " + lineNumber; // missing value linked to valid name
		} catch(NullPointerException e) {
			return "No file chosen, using previous parameters"; // file chooser closed with no file chosen
		}
		
	}
	
	/**
	 * Method to reset the parameters in the map to those specified.
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
	 * @param initialFemales - array of the initial number of adult females in the cell, for each female lifestage
	 * @param devMax - array of maximum development rates for all the lifestages (not including adult males and females7)
//	 * @param devBeta - array of development betas for all the lifestages (not including adults)
//	 * @param devTmin - array of minimum temperatures for development for all lifestages (not including adults)
//	 * @param devTref - array of reference temperatures for development for all lifestages (not including adults)
//	 * @param devTmax - array of maximum temperatures for development for all lifestages (not including adults)
//	 * @param devQ - array of development q's for all the lifestages (not including adults)
//	 * @param amplitudeFert - amplitude of the fertility curve
//	 * @param var1Fert - multiplier on the exponent of the fertility curve
//	 * @param avgFert - average/mean temperature for reproduction
//	 * @param stdevFert - standard deviation for temperature for reproduction
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
	 * @param mortalitiesPredation - stage-specific mortality rates due to predation 
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
		
			// check to see if all the parameters are valid (check parameters with restrictions)
			String validMessage = checkParams(maleProportion, tfinish,								
											initialEggs, initialInst1, initialInst2, initialInst3, 
											initialPupae, initialMales, initialFemales,
											devMax,   
											mortMax, 
											mortalitiesPredation, eggViabilities, //femStageDevRates, 
											fruitM, timeLag, fruitHarvestCutoff, fruitHarvestDrop,
											daylightHours);

			if (!validMessage.equals("Success!")) // if any of the parameters were invalid, return the error message produced
				return validMessage;
		
			// if this point has been reached, all the parameters are valid
 			
			// fruit quality parameters
			paramMap.put("fruit n", fruitN);
			paramMap.put("fruit m", fruitM);
			paramMap.put("fruit time lag", timeLag);
			paramMap.put("fruit base temp", fruitBaseTemp);
			paramMap.put("fruit gt multiplier", gtMultiplier);
			paramMap.put("fruit harvest cutoff", fruitHarvestCutoff);
			paramMap.put("fruit harvest drop", fruitHarvestDrop);
			
			// diapause parameters
			paramMap.put("diapause critical temp", criticalT);
			paramMap.put("diapause daylight hours", daylightHours);

			paramMap.put("time", tfinish); // timesteps to run for
			paramMap.put("constant temp", temp); // constant temperature for the cell (ignored if temperature is changing per timestep)
			paramMap.put("male proportion", maleProportion); // male proportion
			paramMap.put("latitude", latitude);
			
			// initial populations for the fly lifestages			
			paramMap.put("initial eggs", initialEggs);
			paramMap.put("initial instar1", initialInst1);
			paramMap.put("initial instar2", initialInst2);
			paramMap.put("initial instar3", initialInst3);
			paramMap.put("initial pupae", initialPupae);
			paramMap.put("initial males", initialMales);
			
			for (int i = 0; i < 7; i ++) // initial populations of each female lifestage
				 paramMap.put("initial females" + (i + 1), initialFemales[i]);
		
			// fertility parameters
			/*paramMap.put("fertility amplitude", amplitudeFert);
			paramMap.put("fertility var1", var1Fert); 
			paramMap.put("fertility mean temp", avgFert); 
			paramMap.put("fertility stdev temp", stdevFert);
			paramMap.put("fertility tmin", TminFert);*/ 
			paramMap.put("fertility tmax", TmaxFert);
			
			for (int i = 0; i < 13; i ++) { // stage-specific parameters
				
				String stage = getStage(i); // current stage
				
				// lifestage-specific development parameters (excluding adults)
				if (i != 5 && i != 12) {
					int check = i < 11 ? i : i - 1;
					paramMap.put(stage + " development max", devMax[check]);
					/*paramMap.put(stage + " development beta", devBeta[i]);
					paramMap.put(stage + " development tmin", devTmin[i]);
					paramMap.put(stage + " development tref", devTref[i]);
					paramMap.put(stage + " development tmax", devTmax[i]);
					paramMap.put(stage + " development q", devQ[i]);*/
				}
				
				// lifestage-specific mortality parameters
				paramMap.put(stage + " mortality max", mortMax[i]);
				paramMap.put(stage + " mortality min temp", mortTmin[i]);
				paramMap.put(stage + " mortality max temp", mortTmax[i]);
				paramMap.put(stage + " mortality tau", mortTau[i]);
				paramMap.put(stage + " mortality beta0", mortBeta0[i]);
				paramMap.put(stage + " mortality beta1", mortBeta1[i]);
				paramMap.put(stage + " mortality beta2", mortBeta2[i]);
				paramMap.put(stage + " mortality beta3", mortBeta3[i]);		
				
				paramMap.put(stage + " mortality due to predation", mortalitiesPredation[i]); // stage-specific mortality rate due to predation
			}
			
			// female-stage specific parameters
			for (int i = 0; i < 7; i ++) {
				/*if (i < 6)
					paramMap.put("females" + (i + 1) + " development max", devMax[i+5]); // constant development rate for female stages
				*/
				paramMap.put("females" + (i + 1) + " egg viability", eggViabilities[i]); // egg viabilities for female stages
			}
			
			return "Success!";
	}
	
	/**
	 * Resets parameters for the life sciences (not all the parameters are specified).  
	 * Method to reset the parameters in the map to those specified.
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
	 * @param initialFemales - array of the initial number of adult females in the cell, for each female lifestage
	 * @param devMax - array of maximum development rates for all the lifestages (not including adult males and females7)
//	 * @param devTmin - array of minimum temperatures for development for all lifestages (not including adults)
//	 * @param devTmax - array of maximum temperatures for development for all lifestages (not including adults)
	 * @param mortMax - array of maximum mortality rates for all lifestages
	 * @param mortTmin - array of minimum temperatures for all lifestages
	 * @param mortTmax - array of maximum temperatures for all lifestages
	 * @param mortalitiesPred - stage-specific mortality rates due to predation 
	 * @param eggViabilities - viability of the eggs from the different female lifestages
//	 * @param femaleConstantDevRates - female-stage constant development rates
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
	public String setBioArgumentedParams(double latitude, double temp, double maleProportion, double tfinish,
			double initialEggs, double initialInst1, double initialInst2, double initialInst3, 
			double initialPupae, double initialMales, double[] initialFemales,
			double[] devMax, 
			//double[] devTmin, double[] devTmax,  
			double[] mortMax, double[] mortTmin, double[] mortTmax, 
			double[] mortalitiesPred, double[] eggViabilities, //double[] femaleConstantDevRates, 
			double fruitN, double fruitM, double timeLag, double fruitBaseTemp, double gtMultiplier, 
			double fruitHarvestCutoff, double fruitHarvestDrop,
			double criticalT, double daylightHours) {
		
			// check to see if all the parameters are valid (check parameters with restrictions)
			String validMessage = checkParams(maleProportion, tfinish,								
											initialEggs, initialInst1, initialInst2, initialInst3, 
											initialPupae, initialMales, initialFemales,
											devMax,   
											mortMax, 
											mortalitiesPred, eggViabilities, //femaleConstantDevRates, 
											fruitM, timeLag, fruitHarvestCutoff, fruitHarvestDrop,
											daylightHours);

			if (!validMessage.equals("Success!")) // if any of the parameters were invalid, return the error message produced
				return validMessage;
		
			// if this point has been reached, all the parameters are valid
		
			// fruit quality parameters
			paramMap.put("fruit n", fruitN);
			paramMap.put("fruit m", fruitM);
			paramMap.put("fruit time lag", timeLag);
			paramMap.put("fruit base temp", fruitBaseTemp);
			paramMap.put("fruit gt multiplier", gtMultiplier);
			paramMap.put("fruit harvest cutoff", fruitHarvestCutoff);
			paramMap.put("fruit harvest drop", fruitHarvestDrop);
			
			// diapause parameters
			paramMap.put("diapause critical temp", criticalT);
			paramMap.put("diapause daylight hours", daylightHours);

			paramMap.put("time", tfinish); // timesteps to run for
			paramMap.put("constant temp", temp); // constant temperature for the cell (ignored if temperature is changing per timestep)
			paramMap.put("male proportion", maleProportion); // male proportion
			paramMap.put("latitude", latitude);
			
			// initial populations for the fly lifestages			
			paramMap.put("initial eggs", initialEggs);
			paramMap.put("initial instar1", initialInst1);
			paramMap.put("initial instar2", initialInst2);
			paramMap.put("initial instar3", initialInst3);
			paramMap.put("initial pupae", initialPupae);
			paramMap.put("initial males", initialMales);
			
			for (int i = 0; i < 7; i ++) // initial populations of each female lifestage
				 paramMap.put("initial females" + (i + 1), initialFemales[i]);
		
			for (int i = 0; i < 13; i ++) { // stage-specific parameters
				
				String stage = getStage(i); // current stage
				
				// lifestage-specific development parameters (excluding adults)
				if (i != 5 && i != 12) {
					int check = i < 11 ? i : i - 1;
					paramMap.put(stage + " development max", devMax[check]);
					//paramMap.put(stage + " development tmin", devTmin[i]);
					//paramMap.put(stage + " development tmax", devTmax[i]);
				}
				
				// lifestage-specific mortality parameters
				paramMap.put(stage + " mortality max", mortMax[i]);
				paramMap.put(stage + " mortality min temp", mortTmin[i]);
				paramMap.put(stage + " mortality max temp", mortTmax[i]);	
				paramMap.put(stage + " mortality due to predation", mortalitiesPred[i]);

				if (i > 5) { // female stage-specific parameters
					paramMap.put(stage + " egg viability", eggViabilities[i - 6]);
					
					if (i < 12) // no dev rate for females7
						paramMap.put(stage + " development max", devMax[i - 1]);
				}
			}
			
			return "Success!";
		
	}
	
	/**
	 * Method to reset the parameters in the map to those contained in the incoming map.  
	 * Note that this map does not have to contain all the parameters: the method will replace 
	 * those present and leave the other values unmodified. 
	 * @param inputMap - the incoming map of parameters to reset
	 * @param resetFruitParams - reset the fruit parameters? true or false
	 * @return a String message describing the outcome ("Success!" if everything worked, 
	 * 			or an appropriate error message if something went wrong)
	 */
	public String setMapParams(Map<String, Double> inputMap, boolean resetFruitParams) {
		String validMessage = checkMap(inputMap, resetFruitParams, true); // check fruit params if resetting, always check other params
		
		if (!validMessage.equals("Success!")) // if there was an error in the map's parameters, return the message and exit the method here
			return validMessage;
		
		// if this point has been reached, all the parameters are fine
		for (String key: inputMap.keySet()) {
			if (!key.split(" ")[0].equals("fruit") || resetFruitParams) // if it's not a fruit parameter, or if we're resetting the fruit parameters
				if (paramMap.containsKey(key)) // if the parameter is present in the map
					paramMap.put(key, inputMap.get(key)); // reset the parameter to the value in the new map
		}
		
		return "Success!";
		
	}
	
	/**
	 * Method to reset the parameters in the map to those contained in the incoming Parameters object.
	 * Note: error checking is called automatically since this method calls setMapParams
	 * @param params - the incoming Parameters object to copy the parameters from
	 * @param resetFruitParams - reset the fruit parameters? true or false
	 */
	public void setCopyParams(Parameters params, boolean resetFruitParams) {
		setMapParams(params.getMap(), resetFruitParams); // reset the map with the map from the incoming parameters object
	}
	
	/**
	 * Method to return the map of parameters in the Parameters object.
	 * @return a copy of the current map of parameters
	 */
	public Map<String, Double> getMap() {
		return UtilityMethods.copyMap(paramMap); // copy the map to avoid data leaks
	}
	
	/**
	 * Method to return a specific parameter from the map.
	 * Note: if the user tries to access a parameter with a key not present in 
	 * the map, there will be a NullPointerException 
	 * @param parameter - the name (key in the map) of the parameter to return
	 * @return the value from the map corresponding to this parameter
	 * @throws IllegalArgumentException if the parameter name specified is invalid
	 */
	public double getParameter(String parameter) {
		return paramMap.get(parameter); // note: NullPointerException thrown if the String does not correspond to a specific parameter
	}
	
	/**
	 * Method to reset a specific parameter in the map to a specified value.
	 * @param parameter - the name (key in the map) of the parameter to reset
	 * @param newValue - new value for this parameter
	 * @return a String message describing the outcome ("Success!" if everything worked, 
	 * 			or an appropriate error message if something went wrong)
	 */
	public String setParameter(String parameter, double newValue) {
		Map<String, Double> checkMap = getMap(); // copy of the current map 
		
		if (!checkMap.containsKey(parameter)) // if the parameter specified is not present in the map, it's not a valid name
			return "Invalid parameter";
		
		checkMap.put(parameter, newValue); // add the new parameter to the map
		
		String validMessage = checkMap(checkMap, true, true); // check all parameters (for invalid values)
		
		if (validMessage.equals("Success!")) // if there was no error, add the parameter to the datafield map
			paramMap.put(parameter, newValue);
		
		return validMessage;
	}
	
	/**
	 * Method to reset the max mortality for a specified stage
	 * @param stage - the index of the stage
	 * @param newMaxMort - new value for max mortality
	 * @return a String message describing the outcome ("Success!" if everything worked, 
	 * 			or an appropriate error message if something went wrong)
	 */
	public String setMaxMort(int stage, double newMaxMort) {
		// check for potential errors
		if (stage > 12)
			return "Invalid stage!";
		if (newMaxMort < 0)
			return "mortality max is positive";
		
		// if nothing went wrong, update the map
		
		if (stage < 6) // not a female stage
			paramMap.put(names[stage] + " mortality max", newMaxMort);
		else  // female stage
			paramMap.put("females" + (stage - 5) + " mortality max", newMaxMort);
		
		return "Success!";
		
	}
	
	/**
	 * Method to reset the fruit parameters to the values specified in the incoming map.
	 * Note: all fruit parameters must be present in the map.  
	 * Note also: only the fruit parameters are reset.  Even if other valid parameters 
	 * are present in the incoming map, they are not reset.
	 * @param fruitMap - the map to read the new values for the fruit parameters
	 * @return a String message describing the outcome ("Success!" if everything worked, 
	 * 			or an appropriate error message if something went wrong)
	 */
	public String resetFruitParams(Map<String, Double> fruitMap) { // in case there is other information in the map... i.e. only reset fruit params
		try {
			String validMessage = checkMap(fruitMap, true, false); // check the fruit params but not the other params
			if (validMessage.equals("Success!")) { // if there were no errors, update the fruit parameters
				paramMap.put("fruit n", fruitMap.get("fruit n"));
				paramMap.put("fruit m", fruitMap.get("fruit m"));
				paramMap.put("fruit gt multiplier", fruitMap.get("fruit gt multiplier"));
				paramMap.put("fruit time lag", fruitMap.get("fruit time lag"));
				paramMap.put("fruit harvest cutoff", fruitMap.get("fruit harvest cutoff"));
				paramMap.put("fruit harvest drop", fruitMap.get("fruit harvest drop"));
				paramMap.put("fruit base temp", fruitMap.get("fruit base temp"));
			}
			return validMessage; 
		} catch(NullPointerException e) {
			return "Not all parameters were found!";
		}
	}
	
	/**
	 * Method to return an array specifying the initial number of adult females for each
	 * female lifestage.
	 * @return an array of the initial populations for each female lifestage
	 */
	public double[] getInitialFemales() {
		double[] initialFem = new double[7]; // there are 7 female lifestages
		
		for (int i = 0; i < initialFem.length; i ++)
			initialFem[i] = paramMap.get("initial females" + (i + 1));
		
		return initialFem;
		
	}
	
	/**
	 * Method to get the beta parameters (beta0, beta1, beta2, beta3) for calculating
	 * mortality for a specified lifestage, in an array.
	 * Note: if the user specifies an invalid stage, there is a NullPointerException
	 * @param stage - the lifestage to get the betas for
	 * @return an array with the 4 betas for calculating mortality for the specified stage
	 */
	public double[] getMortBetas(String stage) {
		double[] betas = new double[4];
		
		for (int i = 0; i < betas.length; i ++)
			betas[i] = paramMap.get(stage + " mortality beta" + i);
		
		return betas;
	}
	
	/**
	 * Method to return the name of the lifestage corresponding to a specified
	 * integer (the index of the stage).
	 * @param stage - the index 
	 * @return the corresponding stage
	 * @throws IllegalArgumentException if the index is invalid
	 */
	public String getStage(int stage) {
		if (stage > 12)
			throw new IllegalArgumentException("Invalid stage!");
		
		if (stage < 6) // not a female stage
			return names[stage];
		else // female stage
			return "females" + (stage - 5);
	}
	
	/**
	 * Method to return an array of parameters specified by name (i.e. the same 
	 * parameter for each applicable lifestage).
	 * For example, if the user specified "mortality max" the array returned would
	 * be the max mortality values for each of the 13 lifestages.
	 * Note: some parameters are not applicable for all lifestages.  The array returned
	 * only includes the lifestages for which the specified parameter is applicable.
	 * @param parameterName - the name of the parameter selected
	 * @return an array of the values for the specified parameter for each lifestage applicable
	 * @throws IllegalArgumentException if the parameter type is invalid
	 */
	public double[] getArrayParameters(String parameterName) {
		parameterName = parameterName.toLowerCase(); // not case-sensitive
		String type = parameterName.split(" ")[0]; // first word in the specified parameter
		
		double[] toReturn;
		
		if (type.equals("mortality") || type.equals("initial")) // mortality parameters and initial populations are applicable to all lifestages
			toReturn = new double[13];
		else if (type.equals("development")) // development parameters only applicable for the non-advanced adult stages
			toReturn = new double[11];
		else if (type.equals("egg")) // egg viability (only applicable for the female stages)
			toReturn = new double[7];
		else if (type.equals("constant")) // constant development rate, only applicable for the first 6 female stages (i.e. not the last stage)
			toReturn = new double[6];
		else
			throw new IllegalArgumentException("not a valid type"); // if the type was none of the above, then it's invalid
		
		if (toReturn.length == 13 || toReturn.length == 5 || toReturn.length == 11) { // mortality, initial populations, or development
			for (int i = 0; i < 6; i ++) {
				if (toReturn.length != 5 || i < 5) { // development parameters not applicable for adult stages
					if (type.equals("initial"))
						toReturn[i] = paramMap.get(parameterName + " " + names[i]);
					else if (type.equals("development")) {
						if (i < 5)
							toReturn[i] = paramMap.get(names[i] + " " + parameterName);
					}
					else
						toReturn[i] = paramMap.get(names[i] + " " + parameterName);
				}
			}
		}
		
		if (toReturn.length > 5) { // mortality, initial populations, constant dev rate, or egg viabilities
			for (int i = 0; i < 7; i ++) {
				if (toReturn.length == 13) { // mortality or initial populations
					if (type.equals("initial"))
						toReturn[i + 6] = paramMap.get(parameterName + " females" + (i + 1));
					else
						toReturn[i + 6] = paramMap.get("females" + (i + 1) + " " + parameterName);
				}
				else { // constant dev rate or egg viabilities
					if (toReturn.length != 6 || i < 6) { // egg viabilities not applicable for female stage 7
						if (type.equals("development")) {
							if (i < 6)
								toReturn[i + 5] = paramMap.get("females" + (i + 1) + " " + parameterName);
						}
						else
							toReturn[i] = paramMap.get("females" + (i + 1) + " " + parameterName);
					}
				}
			}
				
		}
		
		return toReturn; 
	}
	
	/**
	 * Method to print only the fruit parameters to a file.
	 * Note that the format of this file is such that the parameters can later
	 * be read in from the GUI.
	 * @param fileOut - the PrintWriter object to print with
	 */
	public void printFruitParamsToFile(PrintWriter fileOut) {
		for (int i = 0; i < fruitParams.length; i ++)
			fileOut.println("fruit " + fruitParams[i] + ": " + paramMap.get("fruit " + fruitParams[i]));
	}
	
	/**
	 * Method to print all the parameters stored in this object to a file.  
	 * Note that the format of this file is such that the parameters can later
	 * be read in from the GUI.
	 * @param fileOut - the PrintWriter object to print with
	 * @param printFruitParams - print the fruit parameters? true or false
	 */
	public void printToFile(PrintWriter fileOut, boolean printFruitParams) {
		
		if (printFruitParams) { // if the user wanted to print the fruit parameters
			for (int i = 0; i < fruitParams.length; i ++)
				fileOut.println("fruit " + fruitParams[i] + ": " + paramMap.get("fruit " + fruitParams[i]));
		}
		
		fileOut.println();
		
		// diapause parameters
		fileOut.println("diapause critical temp: " + paramMap.get("diapause critical temp"));
		fileOut.println("diapause daylight hours: " + paramMap.get("diapause daylight hours"));
		
		fileOut.println();
		
		// male proportion
		fileOut.println("male proportion: " +  paramMap.get("male proportion"));
		fileOut.println();
		
		// latitude
		fileOut.println("latitude: " + paramMap.get("latitude"));
		fileOut.println();
		
		// fertility parameters
		/*for (int i = 0; i < fertilityParams.length; i ++) 
			fileOut.println("fertility " + fertilityParams[i] + ": " + paramMap.get("fertility " + fertilityParams[i]));
		*/
		fileOut.println();
		
		// lifestage-specific parameters
		for (int i = 0; i < 13; i ++) {
			String stage = getStage(i); // current stage
			
			fileOut.println("initial " + stage + ": " + paramMap.get("initial " + stage)); // initial populations
			
			if (i != 5 && i != 12) { // recall: no development rate for adult males or females7
				// development parameters
				//for (int j = 0; j < developmentParams.length; j ++) 
					//fileOut.println(stage + " development " + developmentParams[j] + ": " + paramMap.get(stage + " development " + developmentParams[j]));
				fileOut.println(stage + " development max: " + paramMap.get(stage + " development max"));
			}
			
			// mortality parameters
			for (int j = 0; j < mortalityParams.length; j ++)  
				fileOut.println(stage + " mortality " + mortalityParams[j] + ": " + paramMap.get(stage + " mortality " + mortalityParams[j]));
			
			fileOut.println(stage + " mortality due to predation: " + paramMap.get(stage + " mortality due to predation")); 
			
			if (i > 5) { // so, if the female stages have been reached
				/*if (i < 12) // recall: no development rate for females7
					fileOut.println(stage + " development max: " + paramMap.get(stage + " development max")); // constant development rate for each female stage
				*/
				fileOut.println(stage + " egg viability: " + paramMap.get(stage + " egg viability")); // egg viability for each female stage
			}
			
			fileOut.println();
		}
	
	}
	
	/**
	 * Method to check if the parameters in a map are valid (checks the values
	 * to make sure the parameters with restrictions are not outside their
	 * acceptable range).
	 * @param toCheck - the map to check the parameters in
	 * @param checkFruitParams - check the fruit parameters? true or false
	 * @param checkNonFruitParams - check the non-fruit parameters? true or false
	 * @return a String message describing the outcome ("Success!" if everything worked, 
	 * 			or an appropriate error message if something went wrong)
	 */
	public String checkMap(Map<String, Double> toCheck, boolean checkFruitParams, boolean checkNonFruitParams) {
		
		if (checkFruitParams) { // if the user wants to check fruit parameters
			if (toCheck.get("fruit m") < 0 || toCheck.get("fruit m") > 1)
				return "m is between 0 and 1 inclusive";
			if (toCheck.get("fruit time lag") < 0)
				return "time lag is positive";
			if (! (0 <= toCheck.get("fruit harvest cutoff") && toCheck.get("fruit harvest cutoff") <= 1))
				return "fruit harvest cutoff is between 0 and 1 inclusive";
			if (! (0 <= toCheck.get("fruit harvest drop") && toCheck.get("fruit harvest drop") <= 1))
				return "fruit harvest drop is between 0 and 1 inclusive";
		}
		
		if (checkNonFruitParams) { // if the user wants to check non-fruit parameters
			
			if (toCheck.get("diapause daylight hours") < 0 || toCheck.get("diapause daylight hours") > 24 )
				return "diapause daylight hours is between 0 and 24 inclusive";
			
			if (toCheck.get("male proportion") < 0 || toCheck.get("male proportion") > 1)
				return "male proportion is between 0 and 1 inclusive";
			
			for (int i = 0; i < 13; i ++) { // stage-specific parameters
				String stage = getStage(i); // current stage
				
				if (toCheck.get("initial " + stage) < 0)
					return "initial populations are positive";
				
				if (toCheck.get(stage + " mortality max") < 0)
					return "mortality max is positive";
				if (toCheck.get(stage + " mortality due to predation") < 0)
					return "mortality due to predation is positive";
				
				if (i != 5 && i != 12) {
					if (toCheck.get(stage + " development max") < 0)
						return "development max is positive";
				}
				if (i > 5) {
					/*if (i < 12) {
						if (toCheck.get(stage + " constant dev rate") < 0)
							return "constant development rate is positive";
					}*/
					if (toCheck.get(stage + " egg viability") < 0)
						return "egg viability is positive";
				}
				
			}
		}
		
		return "Success!"; // if nothing was returned yet, then there were no errors and the parameters are all fine
	}
	
	/**
	 * Method to check if a list of parameters provided is valid.
	 * Note: only the parameters with restrictions are checked
	 * @param maleProportion - percentage of adult flies which are male (value between 0 and 1 inclusive)
	 * @param tfinish - number of timesteps to run the simulation for
	 * @param initialEggs - initial number of eggs in the cell
	 * @param initialInst1 - initial number of instar 1 in the cell
	 * @param initialInst2 - initial number of instar 2 in the cell
	 * @param initialInst3 - initial number of instar 3 in the cell
	 * @param initialPupae - initial number of pupae in the cell
	 * @param initialMales - initial number of adult males in the cell
	 * @param initialFemales - array of the initial number of adult females in the cell, for each female lifestage
	 * @param devMax - array of maximum development rates for all the lifestages (not including males or females7)
	 * @param mortMax - array of maximum mortality rates for all lifestages
	 * @param mortalitiesPred - stage-specific mortality rates due to predation 
	 * @param eggViabilities - viability of the eggs from the different female lifestages
//	 * @param femaleConstantDevRates - female-stage constant development rates
	 * @param fruitM - value determining how much effect the fruit quality has on the flies
	 * @param timeLag - the number of timesteps above the cutoff before the quality begins to decrease
	 * @param fruitHarvestCutoff - the fruit quality cutoff (timelag timesteps after reaching this cutoff, the quality begins to decrease)
	 * @param fruitHarvestDrop - the fruit quality drop (determines how fruit quality decreases per timestep after passing cutoff)
	 * @param daylightHours - daylight hours cutoff for the diapause model
	 * @return a String message describing the outcome ("Success!" if everything worked, 
	 * 			or an appropriate error message if something went wrong)
	 */
	public String checkParams(double maleProportion, double tfinish,
			double initialEggs, double initialInst1, double initialInst2, double initialInst3, 
			double initialPupae, double initialMales, double[] initialFemales,
			double[] devMax,   
			double[] mortMax, 
			double[] mortalitiesPred, double[] eggViabilities, //double[] femaleConstantDevRates, 
			double fruitM, double timeLag, double fruitHarvestCutoff, double fruitHarvestDrop,
			double daylightHours) {
		
		// daylight hours (diapause)
		if (daylightHours < 0 || daylightHours > 24 )
			return "diapause daylight hours is between 0 and 24 inclusive";
		
		// time
		if (tfinish < 0)
			return "time is positive";
		
		// fruit parameters
		if (fruitM < 0 || fruitM > 1)
			return "m is between 0 and 1 inclusive";
		if (timeLag < 0)
			return "time lag is positive";
		if (! (0 <= fruitHarvestCutoff && fruitHarvestCutoff <= 1))
			return "fruit harvest cutoff is between 0 and 1 inclusive";
		if (! (0 <= fruitHarvestDrop && fruitHarvestDrop <= 1))
			return "fruit harvest drop is between 0 and 1 inclusive";
	
		if (maleProportion < 0 || maleProportion > 1)
			return "male proportion is between 0 and 1 inclusive";
		
		if (initialEggs < 0 || initialInst1 < 0 || initialInst2 < 0 || initialInst3 < 0 || initialPupae < 0 || initialMales < 0)
			return "initial populations are positive";
		
		for (int i = 0; i < 13; i ++) {
			
			if (i != 5 && i != 12) {
				int check = i < 11 ? i : i - 1;
				if (devMax[check] < 0) 
					return "development max is positive";
			}
			
			if (mortalitiesPred[i] < 0)
				return "mortality due to predation is positive"; 
			
			if (mortMax[i] < 0)
				return "mortality max is positive";
			
			// female stage parameters
			if (i < 7) {
				if (eggViabilities[i] < 0)
					return "egg viability is positive";
				if (initialFemales[i] < 0)
					return "initial populations are positive";
			}
			
		}
	
		return "Success!"; // if nothing was returned yet, then there were no errors and the parameters are all fine
	}
	

}