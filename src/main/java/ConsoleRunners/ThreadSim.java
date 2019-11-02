package ConsoleRunners;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.jfree.data.xy.XYSeries;

import SWDModelBaseObjects.Parameters;
import SWDModelReferenceClasses.UtilityMethods;
import SWDModelSimulators.SWDSimulatorSingle;

/**
 * Wrapper class for the single-cell simulator; extends Thread to allow for
 * threading of the batch-runner (so multiple simulations can be run at once).
 * 
 * Once run() has been called, the simulator runs with the specified parameters
 * and prints its data to a file corresponding to its running params.
 * This output file is created in a directory named DATA.
 * 
 * @author Ellen Arteca
 *
 */
public class ThreadSim extends Thread {
	
	// datafields
	private double dt = 0.05;
	private double runTime = 365;
	
	private int startDay = 0;
	private double initPop = 10;
	private String stage = "eggs";
	
	private ArrayList<Double> temps = UtilityMethods.copyArrayList(temperatures.toronto);
	
	private double gtMultiplier = 4;
	private double harvestLag = 50;
	private double criticalT = 18;
	private double daylightHours = 10;
	
	private double firstInitPop = 0;
	private double firstGtMultiplier = 0;
	private double firstHarvestLag = 0;
	private double firstCriticalT = 0;
	private double firstDaylightHours = 0;
	
	private SWDSimulatorSingle sim;
	
	private String[] names = {"eggs", "instar1", "instar2", "instar3", "pupae", "males", "females1", "females2", "females3", 
								"females4", "females5", "females6", "females7"};
	
	private String dataFile = "DATA/output___" + initPop + stage + "_addedDay" + startDay + "_" + runTime + "daysRun.txt";


	private boolean ignoreFruit = true;
	private boolean ignoreDiap = true;	
	
	/**
	 * Constructor to set the simulation parameters and the temperatures to run with.
	 * @param temps - temperatures per day
	 * @param params - simulation parameters
	 * @throws IllegalArgumentException if there is no temperature data in the arraylist passed in
	 */
	public ThreadSim(ArrayList<Double> temps, Parameters params) {
		if (temps.size() == 0) 
			throw new IllegalArgumentException("No temperature data!");
		this.temps = UtilityMethods.copyArrayList(temps);
		sim = new SWDSimulatorSingle(dt, params);
	}
	
	/**
	 * Method to reset the parameters changed in the batch simulator.
	 * This ensures that the simulator can be reset instead of removing it and creating
	 * a new thread for every simulation.
	 * @param dt - integration step
	 * @param runTime - time to run the simulation for
	 * @param startDay - injection date
	 * @param initPop - initial population to be injected
	 * @param stage - stage to be injected
	 * @param gtMultiplier - multiplier for the gt function (fruit model)
	 * @param harvestLag - time spent at full harvest (fruit model)
	 * @param criticalT - critical temperature for the diapause model
	 * @param daylightHours - cutoff point for daylight hours for the diapause model
	 * @throws IllegalArgumentException if any of the arguments are invalid
	 */
	public void resetParams(double dt, double runTime, int startDay, double initPop, String stage,
							double gtMultiplier, double harvestLag, double criticalT, double daylightHours) {
		// make sure all the parameters are valid
		if (!validParams(dt, runTime, startDay, initPop, stage, gtMultiplier, harvestLag, criticalT, daylightHours))
			throw new IllegalArgumentException("Error in parameters!");
		// reset parameters
		this.dt = dt;
		this.runTime = runTime;
		this.startDay = startDay;
		this.initPop = initPop;
		this.stage = stage;
		this.gtMultiplier = gtMultiplier;
		this.harvestLag = harvestLag;
		this.criticalT = criticalT;
		this.daylightHours = daylightHours;
		
		firstInitPop = sim.getSpecificParameter("initial " + stage); // get original initial population for this stage
																	 // this will be the value specified in the config file
		firstGtMultiplier = sim.getSpecificParameter("fruit gt multiplier");
		firstHarvestLag = sim.getSpecificParameter("fruit time lag");
		firstCriticalT = sim.getSpecificParameter("diapause critical temp");
		firstDaylightHours = sim.getSpecificParameter("diapause daylight hours");
		
	}
	
	/**
	 * Method to reset the filename for the output; depending on the type of simulation being run.
	 * @param type - the type of simulation being run
	 * @throws IllegalArgumentException if the type of simulation is invalid
	 */
	public void resetSimulationTitle(String type, boolean ignoreFruit, boolean ignoreDiap) {
		this.ignoreFruit = ignoreFruit;
		this.ignoreDiap = ignoreDiap;
		type = type.toLowerCase();
		if (type.equals("population"))
			dataFile = "DATA/output___" + initPop + stage + "_addedDay" + startDay + "_" + runTime + "daysRun.txt";
		else if (type.equals("fruit"))
			dataFile = "DATA/f_output___gtMult" + gtMultiplier + "_harvestLag" + harvestLag + "_" + runTime + "daysRun.txt";
		else if (type.equals("diapause"))
			dataFile = "DATA/d_output___tCrit" + criticalT + "_daylightHours" + daylightHours + "_" + runTime + "daysRun.txt";
		else
			throw new IllegalArgumentException(type + " - not a valid simulation type!");
	}
	
	/**
	 * Method to run the simulation - gets called when the ExecutorService calls .execute(thisThread)
	 * It resets the parameters in the actual simulator (instead of just storing them as datafields as
	 * in the resetParams method) and runs the simulator for runTime days.
	 * Then, it resets the initial population to that originally specified in the config file (by the 
	 * parameters passed into the constructor on initialization).
	 */
	@Override
	public void run() {
		sim.resetTime(); // reset the simulator (time 0, and reset cumulative variables)
		
		// reset simulation parameters to specified simulation parameters
		sim.setSingleParameter("initial " + stage,  initPop);
		sim.setSingleParameter("fruit gt multiplier", gtMultiplier);
		sim.setSingleParameter("fruit time lag", harvestLag);
		sim.setSingleParameter("diapause critical temp", criticalT);
		sim.setSingleParameter("diapause daylight hours", daylightHours);
		sim.setDT(dt);
		
		
		//boolean ignoreFruit = false;
		//boolean ignoreDiapause = false;

		for (double i = 0; i < runTime; i += dt) {
			sim.run(temps, dt, ignoreFruit, ignoreDiap, startDay); // run the simulator
		}
		
		
		XYSeries[] toPrint = new XYSeries[8]; // array of data series for all lifestages, and fruit quality, vs time
		
		toPrint[0] = sim.getEggSeries();
		toPrint[1] = sim.getInst1Series();
		toPrint[2] = sim.getInst2Series();
		toPrint[3] = sim.getInst3Series();
		toPrint[4] = sim.getPupaeSeries();
		toPrint[5] = sim.getMalesSeries();
		toPrint[6] = sim.getFemalesSeries();
		toPrint[7] = sim.getFruitQualitySeries();
		
		try {
			for (int i = 0; i < 8; i ++) {
				if (toPrint[i] == null) // check to see if any of the series have not yet been initialized
					throw new NullPointerException();
				if (toPrint[i].getItemCount() == 0) // check to see if any of the series have no data points
					throw new NullPointerException();
			}
		} catch(NullPointerException error) { // thrown if none of the series have any points yet i.e. the simulation has not been run
			System.out.println("No data yet!  Cannot proceed.");
			return;
		}
		
		
		try {
			PrintWriter fileOut = new PrintWriter(new File(dataFile));
			fileOut.print("Time:" + "\t");
			
			// all the series have the same number of data points
			
			// print daily data
			for (int j = 0; j < 6; j ++) { // print data labels
					fileOut.print(names[j] + ":\t");
			}
			fileOut.print("females:\t");
			
			fileOut.println();
			for (int i = 0; i < toPrint[0].getItemCount(); i +=20) { // += 20 so it prints every 20th datapoint (i.e. once per day)
				fileOut.print(toPrint[0].getX(i) + "\t"); // print the timestep (same for all series)
				for (int j = 0; j < 7; j ++) {
						fileOut.print(toPrint[j].getY(i) + "\t"); // print the corresponding value for the selected series
				}
				fileOut.println();
			}
			
			// print overall data
			fileOut.println("\n\nTotal Cumulative Populations");
			fileOut.print("\n\t" + sim.getTotEggs() + "\t" + sim.getTotInst1() + "\t" + sim.getTotInst2() + 
							"\t" + sim.getTotInst3() + "\t" + sim.getTotPupae() + "\t" + sim.getTotMales() + 
							"\t" + sim.getTotFemales());
			fileOut.println("\n\nPeak Populations");
			fileOut.print("\n\t" + sim.getMaxEggs() + "\t" + sim.getMaxInst1() + "\t" + sim.getMaxInst2() + 
					"\t" + sim.getMaxInst3() + "\t" + sim.getMaxPupae() + "\t" + sim.getMaxMales() + 
					"\t" + sim.getMaxFemales());
			fileOut.println("\n\nPeak Populations Day");
			fileOut.print("\n\t" + sim.getDayMaxEggs() + "\t" + sim.getDayMaxInst1() + "\t" + sim.getDayMaxInst2() + 
					"\t" + sim.getDayMaxInst3() + "\t" + sim.getDayMaxPupae() + "\t" + sim.getDayMaxMales() + 
					"\t" + sim.getDayMaxFemales());
			
			fileOut.println("\n\nDay diapause crossed: " + sim.getCrossedDiapDay());
			
			fileOut.close();
		} catch (NullPointerException error) { // if no file was chosen
			return;
		} catch(FileNotFoundException error) {
			System.out.println("Error - output file not found");
		}
		
		
		// reset parameters to their original values (as specified in the config file)
		sim.setSingleParameter("initial " + stage, firstInitPop); 
		sim.setSingleParameter("fruit gt multiplier", firstGtMultiplier);
		sim.setSingleParameter("fruit time lag", firstHarvestLag);
		sim.setSingleParameter("diapause critical temp", firstCriticalT);
		sim.setSingleParameter("diapause daylight hours", firstDaylightHours);
	}
	
	/**
	 * Method to check if the parameters are valid.
	 * @param dt - integration step
	 * @param runTime - days to run the simulation for
	 * @param startDay - injection date
	 * @param initPop - initial population of stage
	 * @param stage - lifestage to be injected
	 * @param gtMultiplier - multiplier for the gt function (fruit model)
	 * @param harvestLag - time spent at full harvest (fruit model)
	 * @param criticalT - critical temperature for the diapause model
	 * @param daylightHours - cutoff point for daylight hours for the diapause model
	 * @return true if the parameters are valid, false otherwise
	 */
	private boolean validParams(double dt, double runTime, int startDay, double initPop, String stage, 
								double gtMultiplier, double harvestLag, double critcalT, double daylightHours) {
		if (dt <= 0 || runTime < 0 || initPop < 0) // positive values (dt > 0)
			return false;
		if (gtMultiplier <= 0 || harvestLag < 0 || harvestLag > 365 || daylightHours < 0 || daylightHours > 24)
			return false;
		for (int j = 0; j < names.length; j ++) { // stage must be a valid swd lifestage
			if (stage.equals(names[j]))
				return true;
		}
		
		return false;
	}

}
