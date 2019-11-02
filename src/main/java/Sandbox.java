import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import SWDModelBaseObjects.Parameters;
import SWDModelReferenceClasses.SolveParameters;

public class Sandbox {

	public static void main(String[] args) {

		
		double latitude = 34.58333; // Sudbury
		
		ArrayList<Double> temps = new ArrayList<Double>();	
		
		int lineNumber = 1; // current line number
		
		String tempFileName = "/home/ellen/Documents/research/Temperatures/SantaBarbara_CA_2003.txt";
		
		Parameters params = new Parameters();
		
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
		
		int s1 = 0;
		int s2 = 0;
		
		for(int i = 0; i<364; i++){
			double fertility = SolveParameters.solveSpecificFertility(temps.get(i), params);
			 double fertilityDiapauseEffect = 1;
			 
			 	 int year = ((int) i) / 365;
				 int date = ((int) i) % 365;
				 int offset = SWDModelReferenceClasses.Daylight.getOffSet(year);
				 double hours = SWDModelReferenceClasses.Daylight.getDayLightHours(year, date + offset, latitude);
				 
				 double criticalT = params.getParameter("diapause critical temp");
				 double daylightHours = params.getParameter("diapause daylight hours");
				 
				 // NOTE: don't set s1 here since the previous value of s1 is needed to calculate s2
				 int tempS1 = SolveParameters.solveDiapauseMultS1(hours, temps.get(i), s1, s2, criticalT, daylightHours); // diapause multiplier (s1)
				 s2 = SolveParameters.solveDiapauseMultS2(hours, s1, s2, daylightHours); // s2 value for current dt
				 s1 = tempS1; // s1 value for current dt
				 
				// if (s1 != 1) {
					fertilityDiapauseEffect = s1 * SolveParameters.solveFertilityDiapauseEffect(hours);
				 //}

			 
			 fertility *= fertilityDiapauseEffect;
			 System.out.println(fertilityDiapauseEffect + ", " + temps.get(i) + ", " + hours);
			 //System.out.println(hours);
		}
		
		/*for (int i = -20; i < 40; i ++)
			System.out.println(SolveParameters.solveDev_newData(i, 1));*/
		
	}

}
