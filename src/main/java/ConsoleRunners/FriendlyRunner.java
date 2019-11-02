package ConsoleRunners;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import org.jfree.data.xy.XYSeries;

import SWDModelSimulators.SWDSimulatorSingle;
 

/**
 * Command-line runner which is easier to use - reads runner parameters in from a file 
 * (configRunner.txt) so it is simple for the user to change.  
 * No command-line arguments.
 * Runs the simulation for 1 year with the specified parameters and prints the
 * output to a file (name corresponds to the input parameters).
 * Note: all output files are stored in a directory named DATA.
 * 
 * @author Ellen Arteca
 *
 */
public class FriendlyRunner {
	
	public static void main(String[] args) {
		
		// files to read simulator and runner parameters from
		String configParamsName = "configParams.txt";
		String configRunnerName = "configRunner.txt";
		
		// default values for simulation parameters:
		double runTime = 365;
		int startDay = 0;
		String stage = "eggs";
		double initPop = 10;
		String tempFileName = "tempToronto2012.txt";
		double dt = 0.05;
		double daylightHours = 10;
		double criticalT = 18;
		double harvestLag = 50;
		double gtMultiplier = 4;
		

		String[] names = {"eggs", "instar1", "instar2", "instar3", "pupae", "males", "females1", "females2", "females3", "females4", "females5","females6", "females6"};
		
		
		// read runner params (from file)
		// error checking included..
		
		int lineNumber = 1;
		
		try {
			
			Scanner runnerIn = new Scanner(new File(configRunnerName));
			
			// read in all the parameters for the runner
			// if some are not present, the defaults are used (as specified above)
			while (runnerIn.hasNextLine()) {
				String[] line = runnerIn.nextLine().split(": ");
				try {
					if (line[0].equals("initial population")) {
						double val = Double.parseDouble(line[1]);
						if (val < 0)
							throw new NumberFormatException(); // initial population must be positive
						initPop = val;
					}
					
					else if (line[0].equals("stage")) {
						String val = line[1].toLowerCase();
						boolean ok = false;
						for (int j = 0; j < names.length; j ++) {
							if (val.equals(names[j]))
								ok = true;
						}
						if (!ok)
							throw new NumberFormatException(); // stage must be a valid SWD life stage
						stage = val;
					}
					
					else if (line[0].equals("runtime")) {
						double val = Double.parseDouble(line[1]);
						if (val < 0)
							throw new NumberFormatException(); // days to run the simulation must be positive
						runTime = val;
					}
					
					else if (line[0].equals("injection date")) {
						int val = (int)(Double.parseDouble(line[1]));
						if (val < 0)
							throw new NumberFormatException(); // date to add the flies must be a positive integer
						startDay = val;
					}

					else if (line[0].equals("dt")) {
						double val = Double.parseDouble(line[1]);
						if (val <= 0)
							throw new NumberFormatException(); // integration step must be positive, > 0
						dt = val;
					}
					
					else if (line[0].equals("fruit harvest time lag")) {
						double val = Double.parseDouble(line[1]);
						if (val < 0 || val > 365)
							throw new NumberFormatException(); // harvest time lag must be between 0 and 364 inclusive
						harvestLag = val;
					}
					
					else if (line[0].equals("fruit gt multiplier")) {
						double val = Double.parseDouble(line[1]);
						if (val <= 0)
							throw new NumberFormatException(); // gt multiplier must be positive, > 0
						gtMultiplier = val;
					}
					
					else if (line[0].equals("diapause critical temp")) {
						double val = Double.parseDouble(line[1]);
						criticalT = val;
					}
					
					else if (line[0].equals("diapause daylight hours")) {
						double val = Double.parseDouble(line[1]);
						if (val < 0 || val > 24)
							throw new NumberFormatException(); // daylight hours must be between 0 and 24 inclusive
						daylightHours = val;
					}
					
					else if (line[0].equals("temp file")) { // file to read temperature data from
						String val = line[1];
						tempFileName = val;
					}
					
				} catch(ArrayIndexOutOfBoundsException e) {
					System.out.println("Error - missing argument on line " + lineNumber + " of runner config file");
				} catch(NumberFormatException e) {
					System.out.println("Error - in runner config file, line " + lineNumber + "\n" +
										"Recall: initial population is a positive value\n" +
										"stage must be a valid lifestage as listed\n" + 
										"runtime must be a positive integer\n" + 
										"injection date must be a positive integer\n" +
										"integration step (dt) must be positive (>0)\n" +
										"diapause daylight hours must be between 0 and 24 inclusive\n" + 
										"fruit harvest time lag must be between 0 and 365 inclusive\n" + 
										"fruit gt multiplier must be positive");
				}
				lineNumber ++;
			}
			
			runnerIn.close();
			
		} catch (NullPointerException error) { // if no file was chosen
			return;
		} catch(FileNotFoundException error) {
			System.out.println("Error - runner config file (" + configRunnerName + ") not found");
			return;
		}
		
		// read temperature data from specified file (one data point per line)
		
		ArrayList<Double> temps = new ArrayList<Double>();	

		lineNumber = 1; // current line number
		
		try {
			
			Scanner tempsIn = new Scanner(new File(tempFileName));
			while (tempsIn.hasNextLine()) {
				String line = tempsIn.nextLine();
				temps.add(Double.parseDouble(line)); // read in new temperature values
				lineNumber ++;
			}
			tempsIn.close();
			if (temps.size() == 0) // if there was no temperature data 
				throw new NumberFormatException();
			
		} catch(IOException error) {
			System.out.println("Error - temperature file (" + tempFileName + ") not found");
			return;
		} catch(NullPointerException error) { // if no file was selected
			return;
		} catch(NumberFormatException error) { // if there was a formatting error in the file
			System.out.println("Input error - temperature values are numbers, one per line\nError on line " + lineNumber); 
			return;
		}
		
		SWDSimulatorSingle sim = new SWDSimulatorSingle(dt, configParamsName); // initialize simulator
		
		
		boolean ignoreFruit = false;
		boolean ignoreDiapause = false;
		
		
		sim.setSingleParameter("initial " + stage, initPop);
		sim.setSingleParameter("fruit gt multiplier", gtMultiplier);
		sim.setSingleParameter("fruit time lag", harvestLag);
		sim.setSingleParameter("diapause critical temp", criticalT);
		sim.setSingleParameter("diapause daylight hours", daylightHours);
		
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
		
		String dataFile = "DATA/output___" + initPop + stage + "_addedDay" + startDay + "_" + 
										"harvLag" + harvestLag + "_gtMult" + gtMultiplier + "_" +
										"tCrit" + criticalT + "_lightHours" + daylightHours + "_" +
									runTime + "daysRun.txt";
		
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
			System.out.println("Error - output file not found");
		}

		System.out.println("\n\nProgram Done!");

	}
}
