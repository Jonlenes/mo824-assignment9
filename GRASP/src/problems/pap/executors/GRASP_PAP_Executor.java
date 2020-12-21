package problems.pap.executors;

import java.io.FileWriter;
import java.io.IOException;

import models.ConstructiveHeuristicType;
import models.Experiment;
import models.LocalSearchType;
import problems.pap.solvers.GRASP_PAP;
import solutions.Solution;


public class GRASP_PAP_Executor {

	public static void main(String[] args) throws IOException {
		
		// Params
		String[] instances = {"P50D50S1.pap", "P50D50S3.pap", "P50D50S5.pap",
				"P70D70S1.pap", "P70D70S3.pap", "P70D70S5.pap", "P70D100S6.pap",
				"P70D100S8.pap", "P70D100S10.pap", "P100D150S10.pap",
				"P100D150S15.pap", "P100D150S20.pap"};
		Double[] alphas = {0.1, 0.5, 0.9};
		Integer iterations = 1000;
		
		// Experiments
		Experiment[] experiments = {
				
				
				// DEFAULT
				new Experiment(LocalSearchType.FIRST_IMPROVING, ConstructiveHeuristicType.DEFAULT, "FIRST_DEFAULT"),
				new Experiment(LocalSearchType.BEST_IMPROVING,  ConstructiveHeuristicType.DEFAULT, "BEST_DEFAULT"),

				//REACTIVE GRASP + FIRST
				new Experiment(LocalSearchType.FIRST_IMPROVING, ConstructiveHeuristicType.REACTIVE_GRASP, "FIRST_REACTIVE_GRASP"),
				
				//REACTIVE GRASP + BEST
				new Experiment(LocalSearchType.BEST_IMPROVING, ConstructiveHeuristicType.REACTIVE_GRASP, "BEST_REACTIVE_GRASP"),
				
				// RANDOM_PLUS + FIRST
				new Experiment(LocalSearchType.FIRST_IMPROVING, ConstructiveHeuristicType.RANDOM_PLUS, "FIRST_RANDOM_PLUS_P_0.1", 0.1),
				new Experiment(LocalSearchType.FIRST_IMPROVING, ConstructiveHeuristicType.RANDOM_PLUS, "FIRST_RANDOM_PLUS_P_0.2", 0.2),
				new Experiment(LocalSearchType.FIRST_IMPROVING, ConstructiveHeuristicType.RANDOM_PLUS, "FIRST_RANDOM_PLUS_P_0.3", 0.3),

				// RANDOM_PLUS + BEST
				new Experiment(LocalSearchType.BEST_IMPROVING, ConstructiveHeuristicType.RANDOM_PLUS, "FIRST_RANDOM_PLUS_P_0.1", 0.1),
				new Experiment(LocalSearchType.BEST_IMPROVING, ConstructiveHeuristicType.RANDOM_PLUS, "FIRST_RANDOM_PLUS_P_0.2", 0.1),
				new Experiment(LocalSearchType.BEST_IMPROVING, ConstructiveHeuristicType.RANDOM_PLUS, "FIRST_RANDOM_PLUS_P_0.3", 0.1),
		};
		
		
		for (String instance : instances) {
			FileWriter fileWriter = new FileWriter("results/" + instance + ".txt");
			
			for (Double alpha : alphas) {
				for (Experiment experiment: experiments) {
					try {
						String expName = "ALPHA=" + alpha + "_" + experiment.getKey();
						System.out.println("\n\nINSTANCE:" + instance + "\tRUNNING EXPERIMENT: " + expName + "\n");

						GRASP_PAP grasp_pap = new GRASP_PAP(alpha, iterations, "../instances/" + instance, 
								experiment.getLocalSearchType(), experiment.getConstructiveHeuristicType(),
								experiment.getPerctRandomPlus());
						GRASP_PAP_Executor.executeInstance(expName, grasp_pap, fileWriter);

					} catch (IOException e) {
						e.printStackTrace();
						System.out.println("Error reading instance  or writing in file: "+instance);
					}
				}
			}

			fileWriter.close();
		}
	}
	
	public static void executeInstance(String title, GRASP_PAP grasp, FileWriter fileWriter) {
		
		long startTime = System.currentTimeMillis();
		Solution<Integer> bestSol = grasp.solve();
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		double time = (double)totalTime/(double)1000;
		
		System.out.println("Best Val = " + bestSol);
		System.out.println("Time = "+ time + " seg");
		
		if(fileWriter != null) {
			try {
				fileWriter.append(title + "\n");
				fileWriter.append("Best solution: " + bestSol + "\n");
				fileWriter.append("Time: " + time + "seg \n\n");
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Error writing in file: "+title);
			}
		}
	}
	
}
