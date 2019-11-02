package ConsoleRunners;

import java.util.Map;
import java.io.*;
import java.util.ArrayList;

import org.jfree.data.xy.XYSeries;

import SWDModelSimulators.SWDSimulatorSingle;

/**
 * Command-line runner for the SWDSimulator.  Takes command-line arguments for 
 * the runner parameters (initial population, stage, and injection date).
 * Runs the simulation for 1 year with the specified parameters and prints the
 * output to a file (name corresponds to the input parameters).
 * Note: all output files are stored in a directory named DATA.
 * 
 * @author Ellen Arteca
 *
 */
public class Runner {
	
	public static void main(String[] args) {
		
		double dt = 0.05;
		String fileName = "configParams.txt"; // simulator parameters
		
		SWDSimulatorSingle sim = new SWDSimulatorSingle(dt, fileName);
		
		// i'm going with - args[0] is the population number
		//					args[1] is the stage (1 is eggs, 2 is adults (i.e. females))
		//					args[2] is the date to add the flies
		// they'll all read automatically from tempToronto2012.txt
		// in fact, i'm going to store this in another file as an arraylist
		// they all run for 365 days
		
		double runTime = 365; // run simulation for a year
		boolean ignoreFruit = false;
		boolean ignoreDiapause = false;
		int startDay = Integer.parseInt(args[2]); // injection date
		
		String[] names = {"eggs", "instar1", "instar2", "instar3", "pupae", "males", "females"};
		
		Map<String, Double> map = sim.getParams().getMap();
		if (Integer.parseInt(args[1]) == 1)
			map.put("initial eggs", Double.parseDouble(args[0]));
		else
			map.put("initial females1", Double.parseDouble(args[0]));
		
		sim.setMapParams(map);

		ArrayList<Double> temps = temperatures.toronto;	// hardcode to toronto's temperatures in 2012	

		for (double i = 0; i < runTime; i += dt) {
			sim.run(temps, dt, ignoreFruit, ignoreDiapause, startDay); // run the simulator
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
		
		String dataFile = "DATA/Toronto_2012_" + startDay + "_" + args[0] + "_" + (args[1].equals("1") ? "eggs" : "females") + ".txt";
		
		try {
			PrintWriter fileOut = new PrintWriter(new File(dataFile));
			fileOut.print("Time:" + "\t");
			
			// all the series have the same number of data points
			
			// print daily data
			for (int j = 0; j < 7; j ++) { // print data labels
					fileOut.print(names[j] + ":\t");
			}
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
		
			fileOut.close();
		} catch (NullPointerException error) { // if no file was chosen
			return;
		} catch(FileNotFoundException error) {
			System.out.println("Error - file not found");
		}

	}
}