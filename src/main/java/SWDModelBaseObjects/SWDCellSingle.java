package SWDModelBaseObjects;

import java.util.Map;

import org.jfree.data.xy.XYSeries;

import SWDModelReferenceClasses.EulersMethod;
import SWDModelReferenceClasses.SolveParameters;
import SWDModelReferenceClasses.UtilityMethods;

/**
 * This class describes an SWDCellSingle object, modeling the environment for
 * an SWDPopulation.  This class has a Parameters object representing the various 
 * parameters to calculate the vitals for the flies and the fruit quality, given 
 * the temperature of the cell.
 * There is a general accessor for each of the datafields (from the parameters object),
 * and accessors for the population's datafields (using the population's accessors).  
 * There is also a mutator to reset the timestep back to 0, and a mutator to reset 
 * all of the cell's parameters to those specified in the Parameters object passed in.
 * Methods described in-code.
 * 
 * @author Ellen Arteca
 *
 */

public class SWDCellSingle {
	
	private double temp; // temperature of the cell during the current timestep
	private SWDPopulation population; // population of the cell (for all lifestages - for details see SWDPopulation class)
	
	private double currentFruitQ; // current fruit quality
	private double dayCrossedMaxFruit = -1;
	
	private double[] fruitQualities = new double[365]; // the fruit qualities for one full year
	private boolean killAllFruit; // has fruit quality passed the cutoff (during the current year)? true or false 
	private boolean ignoreFruit = true; // on startup, the default is to ignore the fruit
	private boolean ignoreDiapause = true;

	// each series keeps all the data for its respective lifestage (or fruit quality) up to the current timestep
	// there is one data point for every dt
	private XYSeries eggSeries, inst1Series, inst2Series, inst3Series, pupaeSeries, maleSeries, femaleSeries, fruitQualitySeries;
	private XYSeries[] femaleStageSeries;
	
	private Parameters params; // parameters for all life processes stored 
	
	private double maxEggs, maxInst1, maxInst2, maxInst3, maxPupae, maxMales, maxFemales; // max population of each respective lifestage
	private double maxEggsDay, maxInst1Day, maxInst2Day, maxInst3Day, maxPupaeDay, maxMalesDay, maxFemalesDay; // timestep where the max occured
	
	private double totEggs, totInst1, totInst2, totInst3, totPupae, totMales, totFemales;
	
	private double thresholdPop[] = new double[10];
	private double thresholdPopDay[] = new double[10];
	
	/**
	  * Constructor to initialize the cell object.  Parameters set to those specified
	  * in the Parameters object passed in.
	  * @param params - lists all the datafields for the population and cell objects by name
	  */
	public SWDCellSingle(Parameters params) {
		this.params = new Parameters(params); // set Parameters datafield to a copy of the Parameters passed in
		
		fruitQualities[0] = 0.05; // fruit quality starts at 0.05, at the beginning of the year
		currentFruitQ = 0.05; // default starting fruit quality of 0.05
		
		temp = params.getParameter("constant temp"); // initial temperature of the cell
		
		initializeSeries();
		
		killAllFruit = false; // fruit quality has not passed the cutoff yet
		
		population = new SWDPopulation(params); // initialize population with parameters specified in the Parameters 
		resetCellParams(params, true); // set cell datafields to the values specified in the Parameters -> reset the fruit params
		
		for (int i = 0; i < thresholdPop.length; i ++)
			thresholdPopDay[i] = -1;
	}
	
	/**
	  * Constructor to initialize the cell object.  Parameters set to those specified
	  * in the Parameters object passed in.  Also takes in an initial temperature to initialize the temperature datafield.
	  * @param params - lists all the datafields for the population and cell objects by name
	  * @param temp - initial temperature of the cell
	  */
	public SWDCellSingle(double temp, Parameters params) { 
		this(params);
		this.temp = temp; // set temperature of the cell to that explicitly specified
	}
	
	/**
	  * Default no-argument constructor, sets the datafields to default values 
	  */
	public SWDCellSingle() {
		killAllFruit = false; // fruit quality has not passed the cutoff yet
		
		currentFruitQ = 0.05; // default starting fruit quality of 0.05
		
		temp = 15; // default initial temperature of the cell is 15 deg C
		
		initializeSeries();
		
		params = new Parameters(); // default parameters
		
		population = new SWDPopulation(); // initialize population to default population
		
		for (int i = 0; i < thresholdPop.length; i ++)
			thresholdPopDay[i] = -1;
		
	}
	
	/**
	 * Method to reset a specific parameter to a specified value.
	 * @param parameter - the name (key in the map) of the parameter to reset
	 * @param newValue - new value for this parameter
	 * @return a String message describing the outcome ("Success!" if everything worked, 
	 * 			or an appropriate error message if something went wrong)
	 */
	public String setSingleParameter(String param, double newVal) {
		return params.setParameter(param, newVal);
	}
	
	/**
	 * Method to get the value of a specific parameter.
	 * @param param - the parameter to get the value of
	 * @return the value of the parameter
	 */
	public double getSpecificParameter(String param) {
		return params.getParameter(param);
	}
	
	/**
	  * This method advances the time for the cell, by setting the temperature of the cell to the specified current
	  * temperature of the cell and updating for the population.
	  * Note: the time is advanced by one integration step (dt).
	  * @param temperature - the temperature of the cell during the current timestep
	  * @param ignoreFruit - boolean to determine whether or not to ignore the effect of fruit quality on the flies
	  * @param ignoreDiapause - boolean to determine whether or not to ignore the effect of diapause on the flies
	  * @param dt - step for numeric integration (Euler's method)
	  * @param timeStep - current timestep of the simulation
	  */
	public void stepFoward(double temperature, boolean ignoreFruit, boolean ignoreDiapause, double dt, double timeStep) {	
		// reset datafields to those passed in
		this.ignoreFruit = ignoreFruit;
		this.ignoreDiapause = ignoreDiapause;
		temp = temperature;
		
		// get fruit parameters for the calculations
		double fruitBaseTemp = params.getParameter("fruit base temp");
		double fruitTimeLag = params.getParameter("fruit time lag");
		double fruitHarvestCutoff = params.getParameter("fruit harvest cutoff");
		
		double gt = SolveParameters.getGT(fruitBaseTemp, temperature);
		
		int index = ((int) timeStep) % 365;
		double fruitQLag = 0.05;
		if (index - fruitTimeLag > 0) { // if timelag timesteps have passed in the year
			fruitQLag = fruitQualities[(int) (index - fruitTimeLag)]; // get the fruit quality timelag timesteps ago
			if (fruitQLag > fruitHarvestCutoff) // if it was greater than the cutoff, fruit quality begins to decrease
				killAllFruit = true;
		} else // otherwise, the year has restarted and fruit quality can increase again
			killAllFruit = false;
		if (index == 0) { // if it's the beginning of the year, fruit quality restarts at 0.05
			currentFruitQ = 0.05;
		}
		
		if (killAllFruit)
			fruitQLag = 1; // this is so that the fruit quality does not start increasing again during the year, after quality has reached the cutoff
		
		currentFruitQ = EulersMethod.getFruitQuality(gt, currentFruitQ, fruitQLag, dt, params); // calculate current fruit quality
		
		if (UtilityMethods.round2Decimals(currentFruitQ) == 1 && dayCrossedMaxFruit == -1)
			dayCrossedMaxFruit = timeStep;
		
		fruitQualities[((int) timeStep) % 365] = currentFruitQ; // store the fruit quality for the current timestep in the array
		// Note: only one fruit quality is stored per timestep (i.e. not one value per dt)
		
		population.computePopulation(temperature, currentFruitQ, params, ignoreFruit, ignoreDiapause, dt, timeStep); // update the population
		
		// update the stage-specific population data series
		eggSeries.add(timeStep, getEggs());
		inst1Series.add(timeStep, getInst1());
		inst2Series.add(timeStep, getInst2());
		inst3Series.add(timeStep, getInst3());
		pupaeSeries.add(timeStep, getPupae());
		maleSeries.add(timeStep, getMales());
		femaleSeries.add(timeStep, getFemales());
		
		double[] femStagePopulation = getFemStages();
		for (int i = 0; i < femaleStageSeries.length; i ++)
			femaleStageSeries[i].add(timeStep, femStagePopulation[i]);
		
		fruitQualitySeries.add(timeStep, currentFruitQ); // update fruit quality data series
		
		totEggs += getEggs() * dt;
		totInst1 += getInst1() * dt;
		totInst2 += getInst2() * dt;
		totInst3 += getInst3() * dt;
		totPupae += getPupae() * dt;
		totMales += getMales() * dt;
		totFemales += getFemales() * dt;
		
		if (maxEggs < getEggs()) {
			maxEggs = getEggs();
			maxEggsDay = timeStep;
		}
		
		if (maxInst1 < getInst1()) {
			maxInst1 = getInst1();
			maxInst1Day = timeStep;
		}
		
		if (maxInst2 < getInst2()) {
			maxInst2 = getInst2();
			maxInst2Day = timeStep;
		}
		
		if (maxInst3 < getInst3()) {
			maxInst3 = getInst3();
			maxInst3Day = timeStep;
		}
		
		if (maxPupae < getPupae()) {
			maxPupae = getPupae();
			maxPupaeDay = timeStep;
		}
		
		if (maxMales < getMales()) {
			maxMales = getMales();
			maxMalesDay = timeStep;
		}
		
		if (maxFemales < getFemales()) {
			maxFemales = getFemales();
			maxFemalesDay = timeStep;
		}
		
		for (int i = 0; i < thresholdPop.length; i ++) {
			if (getFemales() >= thresholdPop[i] && thresholdPopDay[i] < 0) {
				thresholdPopDay[i] = timeStep;
			}
		}
		
		
	}
	
	// -----------------------------------------------------------------------------------------FRUIT
	
	/**
	 * Method to return whether the effect of fruit quality on the flies is being ignored
	 * @return is the fruit quality ignored? true or false
	 */
	public boolean getIgnoreFruit() { return ignoreFruit; }
	
	/**
	 * Method to return the current fruit quality of the cell
	 * @return current fruit quality of the cell
	 */
	public double getFruitQuality() {return currentFruitQ; }
	
	/**
	 * Method to return the day where the fruit quality reached 1.
	 * @return first day the fruit quality reached it's max.
	 */
	public double getDayCrossedMaxFruit() { return dayCrossedMaxFruit; }
	
	
	// -----------------------------------------------------------------------------------------TEMPERATURE
	
	/**
	 * Method to return the current temperature of the cell
	 * @return the temperature of the cell at the current timestep
	 */
	public double getTemperature() {return temp; }
	
	// -----------------------------------------------------------------------------------------THRESHOLD POPULATION
	
	/**
	 * Method to return the day at which the total cumulative female population (for all 7 female 
	 * lifestages) passed the threshold population at the specified index.
	 * @param index - the specified index
	 * @return the day the threshold population was passed
	 * @throws IllegalArgumentException if the index is out of range
	 */
	public double getThresholdPopDay(int index) { 
		if (index < 0 || index >= thresholdPop.length)
			throw new IllegalArgumentException("Index out of range!! (Should be between 0 and 9 inclusive)");
		return thresholdPopDay[index]; 
	}
	
	/**
	 * Method to set the threshold population at the specified index.
	 * @param index - the specified index
	 * @param thresholdPop - the threshold population specified
	 * @throws IllegalArgumentException if the index is out of range
	 */
	public void setThresholdPopDay(int index, double thresholdPop) { 
		if (index < 0 || index >= this.thresholdPop.length)
			throw new IllegalArgumentException("Index out of range!! (Should be between 0 and 9 inclusive)");
		this.thresholdPop[index] = thresholdPop; 
	}
	
	// -----------------------------------------------------------------------------------------TIMESTEP
	
	/**
	 * Reads the initial populations from file
	 */
	public void readInitFlies() {
		population.readPopulation(params);
	}
	
	/**
	  * Method to set the addInitPop boolean to be true - this is done initially to ensure that 
	  * if a valid injection date is passed in, the population is not added even if diapause
	  * threshold is crossed, until readPopulation is called.
	  */
	 public void setAddInitPop(boolean toSet) {
		 population.setAddInitPop(toSet);
	 }
	
	/**
	  * Method to return the day the diapause threshold was crossed
	  * @return the day the diapause threshold was crossed (-1 if never crossed)
	  */
	public int getCrossedDiapDay() {
		return population.getCrossedDiapDay();
	}
	
	/**
	 * Resets the cell to its state at timestep 0.
	 */
	public void resetTime() {
		population.resetPopulation(); // reset the population
		
		dayCrossedMaxFruit = -1;
		
		for (int i = 0; i < thresholdPop.length; i ++)
			thresholdPopDay[i] = -1;
		
		// reset the data series
		eggSeries.clear();
		inst1Series.clear();
		inst2Series.clear();
		inst3Series.clear();
		pupaeSeries.clear();
		maleSeries.clear();
		femaleSeries.clear();
		
		for (int i = 0; i < femaleStageSeries.length; i ++)
			femaleStageSeries[i].clear();
		
		fruitQualitySeries.clear();
		
		/*maxEggs = params.getParameter("initial eggs");
		maxInst1 = params.getParameter("initial instar1");
		maxInst2 = params.getParameter("initial instar2");
		maxInst3 = params.getParameter("initial instar3");
		maxPupae = params.getParameter("initial pupae");
		maxMales = params.getParameter("initial males");
		maxFemales = UtilityMethods.sumDoubleArray(params.getInitialFemales());*/
		
		// now, reset max populations to 0, since they're all 0 at timestep 0
		maxEggs = 0;
		maxInst1 = 0;
		maxInst2 = 0;
		maxInst3 = 0;
		maxPupae = 0;
		maxMales = 0;
		maxFemales = 0;
		
		// reset the maximum population date to timestep 0
		maxEggsDay = 0;
		maxInst1Day = 0;
		maxInst2Day = 0;
		maxInst3Day = 0;
		maxPupaeDay = 0;
		maxMalesDay = 0;
		maxFemalesDay = 0;
		
		totEggs = 0;
		totInst1 = 0;
		totInst2 = 0;
		totInst3 = 0;
		totPupae = 0;
		totMales = 0;
		totFemales = 0;
	}
	
	
	
	// -----------------------------------------------------------------------------------------CURRENT POPULATIONS
	
	/**
	  * Method to return the current number of eggs in the population
	  * @return the number of eggs currently in the population
	  */
	public double getEggs() {return population.getEggs(); }
	
	/**
	  * Method to return the current number of instar 1 in the population
	  * @return the number of instar 1 currently in the population
	  */
	 public double getInst1() {return population.getInst1(); }
	 
	 /**
	  * Method to return the current number of instar 2 in the population
	  * @return the number of instar 2 currently in the population
	  */
	 public double getInst2() {return population.getInst2(); }
	 
	 /**
	  * Method to return the current number of instar 3 in the population
	  * @return the number of instar 3 currently in the population
	  */
	 public double getInst3() {return population.getInst3(); }
	
	/**
	  * Method to return the current number of pupae in the population
	  * @return the number of pupae currently in the population
	  */
	public double getPupae() {return population.getPupae(); }
	
	/**
	  * Method to return the current number of adult males in the population
	  * @return the number of adult males currently in the population
	  */
	public double getMales() {return population.getMales(); }
	
	/**
	  * Method to return the current number of adult females in the population
	  * @return the number of adult females currently in the population
	  */
	public double getFemales() {return population.getFemales(); }
	
	/**
	  * Method to return the current total number of bugs in the population
	  * @return the total number of flies at all life stages currently in the population
	  */
	public double getTotalPopulation() {return population.getTotalPopulation(); }
	
	/**
	 * Method to return the current number of adult females of each stage in 
	 * the cell
	 * @return the current number of adult females of each stage in the cell
	 */
	public double[] getFemStages() {return population.getFemStages(); }
	
	
	// -----------------------------------------------------------------------------------------MAXIMUM POPULATIONS
	
	/**
	 * Method to return the maximum female population 
	 * Note: this is the cumulative sum of all female stages
	 * @return the maximum female population
	 */
	public double getMaxFemales() { return maxFemales; }
	
	/**
	 * Method to return the maximum male population
	 * @return the maximum male population
	 */
	public double getMaxMales() { return maxMales; }
	
	/**
	 * Method to return the maximum pupae population
	 * @return the maximum pupae population
	 */
	public double getMaxPupae() { return maxPupae; }
	
	/**
	 * Method to return the maximum instar3 population
	 * @return the maximum instar3 population
	 */
	public double getMaxInst3() { return maxInst3; }
	
	/**
	 * Method to return the maximum instar2 population
	 * @return the maximum instar2 population
	 */
	public double getMaxInst2() { return maxInst2; }
	
	/**
	 * Method to return the maximum instar1 population
	 * @return the maximum instar1 population
	 */
	public double getMaxInst1() { return maxInst1; }
	
	/**
	 * Method to return the maximum egg population
	 * @return the maximum egg population
	 */
	public double getMaxEggs() { return maxEggs; }
	
	// -----------------------------------------------------------------------------------------MAXIMUM POPULATIONS TIMESTEP
	
	/**
	 * Method to return the date for the maximum female population 
	 * Note: this considers the cumulative sum of all female stages
	 * @return the date for the maximum female population
	 */
	public double getDayMaxFemales() { return maxFemalesDay; }
	
	/**
	 * Method to return the date for the maximum male population
	 * @return the date for the maximum male population
	 */
	public double getDayMaxMales() { return maxMalesDay; }
	
	/**
	 * Method to return the date for the maximum pupae population
	 * @return the date for the maximum pupae population
	 */
	public double getDayMaxPupae() { return maxPupaeDay; }
	
	/**
	 * Method to return the date for the maximum instar3 population
	 * @return the date for the maximum instar3 population
	 */
	public double getDayMaxInst3() { return maxInst3Day; }
	
	/**
	 * Method to return the date for the maximum instar2 population
	 * @return the date for the maximum instar2 population
	 */
	public double getDayMaxInst2() { return maxInst2Day; }
	
	/**
	 * Method to return the date for the maximum instar1 population
	 * @return the date for the maximum instar1 population
	 */
	public double getDayMaxInst1() { return maxInst1Day; }
	
	/**
	 * Method to return the date for the maximum egg population
	 * @return the date for the maximum egg population
	 */
	public double getDayMaxEggs() { return maxEggsDay; }
	
	// -----------------------------------------------------------------------------------------CUMULATIVE POPULATIONS
	
	/**
	 * Method to return the total cumulative female population 
	 * Note: this is the cumulative sum of all female stages
	 * @return the total cumulative female population
	 */
	public double getTotFemales() { return totFemales; }
	
	/**
	 * Method to return the total cumulative male population
	 * @return the total cumulative male population
	 */
	public double getTotMales() { return totMales; }
	
	/**
	 * Method to return the total cumulative pupae population
	 * @return the total cumulative pupae population
	 */
	public double getTotPupae() { return totPupae; }
	
	/**
	 * Method to return the total cumulative instar3 population
	 * @return the total cumulative instar3 population
	 */
	public double getTotInst3() { return totInst3; }
	
	/**
	 * Method to return the total cumulative instar2 population
	 * @return the total cumulative instar2 population
	 */
	public double getTotInst2() { return totInst2; }
	
	/**
	 * Method to return the total cumulative instar1 population
	 * @return the total cumulative instar1 population
	 */
	public double getTotInst1() { return totInst1; }
	
	/**
	 * Method to return the maximum egg population
	 * @return the maximum egg population
	 */
	public double getTotEggs() { return totEggs; }
	
	
	
	/**
	 * Method to return a copy of the Parameters object in the cell (i.e. all the 
	 * parameters used in the cell).
	 * @return a copy of the Parameters object in the cell
	 */
	public Parameters getParams() {return new Parameters(params); }
	
	 /**
	  * Resets the parameters (in the Parameters object) to those values specified in the 
	  * Parameters object supplied.
	  * @param params - lists all the datafields for the population and cell objects by name
	  * @param resetFruitParams - do the fruit parameters get reset? true or false
	  */
	public void resetCellParams(Parameters params, boolean resetFruitParams) {
		this.params.setCopyParams(params, resetFruitParams);
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
	public String resetFruitParams(Map<String, Double> fruitMap) {
		return params.resetFruitParams(fruitMap);
	}
	
	/**
	 * Method to initialize the series to keep track of lifestage-specific and fruit quality 
	 * data points.  Avoids repetition since this method is called in the various constructors.
	 */
	private void initializeSeries() {
		
		// data points for each respective lifestage, and fruit quality, over time
		eggSeries = new XYSeries("eggs");
		inst1Series = new XYSeries("instar1");
		inst2Series = new XYSeries("instar2");
		inst3Series = new XYSeries("instar3");
		pupaeSeries = new XYSeries("pupae");
		maleSeries = new XYSeries("males");
		femaleSeries = new XYSeries("females");
		fruitQualitySeries = new XYSeries("fruit quality");
		
		femaleStageSeries = new XYSeries[7];
		
		for (int i = 0; i < femaleStageSeries.length; i ++) // one series for each female stage
			femaleStageSeries[i] = new XYSeries("females" + (i + 1));
		
		
	}
	
	/**
	 * Method to return the series of data points (current population for each timestep up to the current
	 * timestep) for eggs.
	 * @return the current data series for eggs
	 */
	public XYSeries getEggSeries() {return UtilityMethods.cloneSeries(eggSeries, "Eggs"); }
	
	/**
	 * Method to return the series of data points (current population for each timestep up to the current
	 * timestep) for instar1.
	 * @return the current data series for instar1
	 */
	public XYSeries getInst1Series() {return UtilityMethods.cloneSeries(inst1Series, "Instar 1"); }
	
	/**
	 * Method to return the series of data points (current population for each timestep up to the current
	 * timestep) for instar2.
	 * @return the current data series for instar2
	 */
	public XYSeries getInst2Series() {return UtilityMethods.cloneSeries(inst2Series, "Instar 2"); }
	
	/**
	 * Method to return the series of data points (current population for each timestep up to the current
	 * timestep) for instar3.
	 * @return the current data series for instar3
	 */
	public XYSeries getInst3Series() {return UtilityMethods.cloneSeries(inst3Series, "Instar 3"); }
	
	/**
	 * Method to return the series of data points (current population for each timestep up to the current
	 * timestep) for pupae.
	 * @return the current data series for pupae
	 */
	public XYSeries getPupaeSeries() {return UtilityMethods.cloneSeries(pupaeSeries, "Pupae"); }
	
	/**
	 * Method to return the series of data points (current population for each timestep up to the current
	 * timestep) for adult males.
	 * @return the current data series for adult males
	 */
	public XYSeries getMalesSeries() {return UtilityMethods.cloneSeries(maleSeries, "Males"); }
	
	/**
	 * Method to return the series of data points (current population for each timestep up to the current
	 * timestep) for adult females.
	 * @return the current data series for adult females
	 */
	public XYSeries getFemalesSeries() {return UtilityMethods.cloneSeries(femaleSeries, "Females"); }
	
	/**
	 * Method to return the series of data points (current fruit quality for each timestep up to the current
	 * timestep).
	 * @return the current data series for fruit quality
	 */
	public XYSeries getFruitQualitySeries() {return UtilityMethods.cloneSeries(fruitQualitySeries, "Fruit Quality"); }
	
	/**
	 * Method to return the series of data points (current population for each timestep up to the current timestep)
	 * for the selected adult female lifestage.
	 * @param index - the selected female lifestage
	 * @return the current data series for the selected female lifestage
	 * @throws IllegalArgumentException if the index is out of bounds
	 */
	public XYSeries getFemaleStageSeries(int index) {
		if (index < 0 || index >= femaleStageSeries.length)
			throw new IllegalArgumentException("index out of bounds!");
		return UtilityMethods.cloneSeries(femaleStageSeries[index], "Females" + (index + 1));
	}
}