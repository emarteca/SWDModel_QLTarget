package ConsoleRunners;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import SWDModelBaseObjects.Parameters;
import SWDModelReferenceClasses.UtilityMethods;

/**
 * Batch-runner that 8 uses threads!
 * This really cuts down on the time needed, since 8 simulations can run concurrently.
 * The file IO (printing output) can't be done concurrently but the actual simulation
 * is the part that takes the most time. 
 * 
 * Population Simulations
 * This runs the simulation for all 365 injection dates, populations 10, 100, 1000, 10000
 * and for eggs and females1 as stages.  It's hardcoded to run for 365 days, with a dt of 
 * 0.05 and reading simulation parameters from configParams.txt and temperatures for 
 * Toronto 2012 (from the temperatures.java).
 * 
 * Fruit Simulations
 * This runs the simulation for all 365 harvest lags (jumps of 5), gt multipliers between 1 
 * and 10 (jumps of 0.25).  It's hardcoded to run for 365 days, with initial egg population of
 * 100 at injection date 0; reads simulation parameters from configParams.txt and temperatures
 * for Toronto 2012 (from temperatures.java).
 * 
 * Diapause Simulations
 * This runs the simulation for critical temperatures from 0 to 37, and daylight hours from
 * 0 to 24.  It's hardcoded to run for 365 days, with initial egg population of 100 at injection
 * date 0; reads simulation parameters from configParams.txt and temperatures for Toronto 2012
 * (from temperatures.java).
 * 
 * @author Ellen Arteca
 *
 */
public class ThreadedBatchRunner {
	
	// general running parameters
	
	private static int NUM_THREADS = 8;
	private static double[] initialPops = {10, 100, 1000, 10000};
	
	private static double dt = 0.05;
	private static double runTime = 365;
	private static String configFileName = "configParams.txt";
	
	/**
	 * Method to run the simulations for population model sensitivity tests.
	 */
	public static void runPopulationSims() {
		
		double gtMultiplier = 4;
		double harvestLag = 50;
		
		double criticalT = 18;
		double daylightHours = 10;

		boolean ignoreFruit = true;
		boolean ignoreDiap = true;

		Parameters params = new Parameters(configFileName); // all simulations have the same general parameters
		
		// for threading, each injection date has 8 options (4 initial populations, for eggs and females1)
		// each of these options is a thread
		// these thread objects have simulators, which are reset for every new injection date
		
		ThreadSim[] tSims = new ThreadSim[NUM_THREADS];
		for (int i = 0; i < NUM_THREADS; i ++) {
			tSims[i] = new ThreadSim(temperatures.clark, params);
		}
			
		// run the simulations!
		for (int startDay = 0; startDay < 365; startDay ++) {
			ExecutorService exe = Executors.newFixedThreadPool(NUM_THREADS); // can only have NUM_THREADS threads running concurrently 
			
			int threadNum = 0;
			for (int iP = 0; iP < initialPops.length; iP ++) {
				for (int s = 0; s < 2; s ++) {
					String stage = (s == 0) ? "eggs" : "females1";
					// reset and run the thread with the specified parameters
					tSims[threadNum].resetParams(dt, runTime, startDay, initialPops[iP], stage,
												gtMultiplier, harvestLag, criticalT, daylightHours);
					tSims[threadNum].resetSimulationTitle("population", ignoreFruit, ignoreDiap);
					exe.execute(tSims[threadNum]); // calls .run() for the thread
					threadNum ++;
				}
			}
		
			// make sure all the threads have terminated before starting the next round of simulations
			exe.shutdown();
			while (!exe.isTerminated()) {}	
			
			System.out.println("Done threading for startDay: " + startDay);
		}
		
		System.out.println("\n\nProgram Done!!");
		
	}
	
	/**
	 * Methods to run the simulations for fruit model sensitivity tests.
	 */
	public static void runFruitSims() {
		
		double initPop = 10;
		int startDay = 0;
		String stage = "females1";
		
		double criticalT = 18;
		double daylightHours = 10;

		boolean ignoreFruit = false;
		boolean ignoreDiap = true;

		Parameters params = new Parameters(configFileName); // all simulations have the same general parameters
		
		// for threading, each injection date has 8 options (4 initial populations, for eggs and females1)
		// each of these options is a thread
		// these thread objects have simulators, which are reset for every new injection date
		
		ThreadSim[] tSims = new ThreadSim[NUM_THREADS];
		for (int i = 0; i < NUM_THREADS; i ++) {
			tSims[i] = new ThreadSim(temperatures.clark, params);
		}
			
		// run the simulations!
		for (double gtMultiplier = 1; gtMultiplier <= 10; gtMultiplier = UtilityMethods.round2Decimals(gtMultiplier + 0.25)) {
			
			for (int harvestLag = 0; harvestLag < 366; harvestLag += 40) {
				ExecutorService exe = Executors.newFixedThreadPool(NUM_THREADS); // can only have NUM_THREADS threads running concurrently 
				
				int threadNum = 0;
				for (int i = 0; i < 40 && harvestLag + i < 366; i += 5) {
					// reset and run the thread with the specified parameters
					tSims[threadNum].resetParams(dt, runTime, startDay, initPop, stage,
												gtMultiplier, harvestLag + i, criticalT, daylightHours);
					tSims[threadNum].resetSimulationTitle("fruit", ignoreFruit, ignoreDiap);
					exe.execute(tSims[threadNum]); // calls .run() for the thread
					threadNum ++;
				}
				// make sure all the threads have terminated before starting the next round of simulations
				exe.shutdown();
				while (!exe.isTerminated()) {}	
				
				System.out.println("gtMultiplier: " + gtMultiplier + "Done threading for harvestLag: " + harvestLag);
			}
		
		}
		
		System.out.println("\n\nProgram Done!!");
		
	}
	
	/**
	 * Method to run the simulations for diapause model sensitivity tests.
	 */
	public static void runDiapauseSims() {
		
		double initPop = 10;
		int startDay = 75;
		String stage = "females1";
		
		double harvestLag = 50;
		double gtMultiplier = 4;

		boolean ignoreFruit = true;
		boolean ignoreDiap = false;

		Parameters params = new Parameters(configFileName); // all simulations have the same general parameters
		
		// for threading, each injection date has 8 options (4 initial populations, for eggs and females1)
		// each of these options is a thread
		// these thread objects have simulators, which are reset for every new injection date
		
		ThreadSim[] tSims = new ThreadSim[NUM_THREADS];
		for (int i = 0; i < NUM_THREADS; i ++) {
			tSims[i] = new ThreadSim(temperatures.hillsborough, params);
		}
			
		// run the simulations!
		for (int criticalT = 0; criticalT < 38; criticalT ++) {
			
			for (int daylightHours = 0; daylightHours <= 24; daylightHours += NUM_THREADS) {
				ExecutorService exe = Executors.newFixedThreadPool(NUM_THREADS); // can only have NUM_THREADS threads running concurrently 
				
				int threadNum = 0;
				for (int i = 0; i < NUM_THREADS && daylightHours + i <= 24; i ++) {
					// reset and run the thread with the specified parameters
					tSims[threadNum].resetParams(dt, runTime, startDay, initPop, stage,
												gtMultiplier, harvestLag, criticalT, daylightHours + i);
					tSims[threadNum].resetSimulationTitle("diapause", ignoreFruit, ignoreDiap);
					exe.execute(tSims[threadNum]); // calls .run() for the thread
					threadNum ++;
				}
				// make sure all the threads have terminated before starting the next round of simulations
				exe.shutdown();
				while (!exe.isTerminated()) {}
				
				System.out.println("criticalT: " + criticalT + "Done threading for daylightHours: " + daylightHours);
			}
		
		}
		
		System.out.println("\n\nProgram Done!!");
		
	}
	
	
	public static void main(String[] args) {
		if (args.length > 0) {
			if (args[0].equals("fruit"))
				runFruitSims();
			else if (args[0].equals("population"))
				runPopulationSims();
			else if (args[0].equals("diapause"))
				runDiapauseSims();
		}
		else {
			runDiapauseSims();
		}
	}

}
