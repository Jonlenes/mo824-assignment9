package problems.pap.solvers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import metaheuristics.grasp.AbstractGRASP;
import models.ConstructiveHeuristicType;
import models.LocalSearchType;
import models.Triple;
import models.Candidate;
import problems.pap.PAP;
import solutions.Solution;


public class GRASP_PAP extends AbstractGRASP<Triple> {

	private LocalSearchType localSearchType;

	private int[] roomAvailability;
	
	public GRASP_PAP(Double alpha, Integer iterations, String filename, LocalSearchType localSearchType, 
			ConstructiveHeuristicType constructionType) throws IOException {
		super(new PAP(filename), alpha, iterations);
		this.localSearchType = localSearchType;
		roomAvailability = new int[ObjFunction.getValue("T")];
		Arrays.fill(roomAvailability, ObjFunction.getValue("S"));
	}

	@Override
	public ArrayList<Triple> makeCL() {

		ArrayList<Triple> _CL = new ArrayList<Triple>();
		for (int p = 0; p < ObjFunction.getValue("P"); p++) {
			for (int d = 0; d < ObjFunction.getValue("D"); d++) {
				// IMPORTANT: here CL its combinations of (p, d) and t represent
				// the total of needed periods for that course (Hd)
				_CL.add(new Triple(p, d, ObjFunction.getHd(d)));
			}
		}

		return _CL;
	}

	@Override
	public ArrayList<Triple> makeRCL() {
		ArrayList<Triple> _RCL = new ArrayList<Triple>();
		return _RCL;
	}

	@Override
	public void updateCL() {
		return;
	}

	@Override
	public Solution<Triple> createEmptySol() {
		Solution<Triple> sol = new Solution<Triple>();
		sol.cost = Double.NEGATIVE_INFINITY;
		return sol;
	}

	@Override
	public Solution<Triple> localSearch() {
		if(this.localSearchType.equals(LocalSearchType.FIRST_IMPROVING)) {
			return this.firstImprovingLocalSearch();
		}
		
		return this.bestImprovingLocalSearch();
	}
	
	private Candidate evaluateInsertions(Candidate cand, Solution<Triple> currentSol, Boolean first, double bestCost) {
		double deltaCost = 0;
		for (Triple candIn : CL) {
			deltaCost = ObjFunction.evaluateInsertionCost(candIn, currentSol);
			if (deltaCost < bestCost) {
				cand.setDeltaCost(deltaCost);
				cand.setTriple(candIn);
				bestCost = deltaCost;
				if (first) break;
			}
		}
		return cand;
	}
	
	private Candidate evaluateRemovals(Candidate cand, Solution<Triple> currentSol, Boolean first, double bestCost) {
		double deltaCost = 0;
		for (Triple candOut : currentSol) {
			deltaCost = ObjFunction.evaluateRemovalCost(candOut, currentSol);
			if (deltaCost < bestCost) {
				cand.setDeltaCost(deltaCost);
				cand.setTriple(candOut);
				bestCost = deltaCost;
				if (first) break;
			}
		}
		return cand;
	}
	
	private Candidate[] evaluateExchanges(Candidate bestCandIn, Candidate bestCandOut, Solution<Triple> currentSol, Boolean first, double bestCost) {
		double deltaCost = 0;
		for (Triple candIn : CL) {
			for (Triple candOut : currentSol) {
				deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, currentSol);
				if (deltaCost < bestCost) {
					bestCandIn.setTriple(candIn);
					bestCandOut.setTriple(candOut);
					bestCandIn.setDeltaCost(deltaCost);
					bestCandOut.setDeltaCost(deltaCost);
					bestCost = deltaCost;
					if (first) break;
				}
			}
		}
		return new Candidate[] {bestCandIn, bestCandOut};
	}
	
	private void updateCurrentSolution(Candidate bestCandIn, Candidate bestCandOut) {
		Double minDeltaCost = Math.min(bestCandIn.getDeltaCost(), bestCandOut.getDeltaCost());
		if (minDeltaCost < -Double.MIN_VALUE) {
			if (bestCandOut.getTriple() != null) {
				currentSol.remove(bestCandOut.getTriple());
				CL.add(bestCandOut.getTriple());
			}
			if (bestCandIn.getTriple() != null) {
				currentSol.add(bestCandIn.getTriple());
				CL.remove(bestCandIn.getTriple());
			}
			
			ObjFunction.evaluate(currentSol);
		}
	}
	
	public Solution<Triple> firstImprovingLocalSearch(){
		Double minDeltaCost = -Double.MIN_VALUE;
		
		updateCL();
		Candidate bestCandIn = new Candidate(), bestCandOut = new Candidate();
			
		// Evaluate insertions
		bestCandIn = evaluateInsertions(bestCandIn, currentSol, true, minDeltaCost);
		if (bestCandIn.getDeltaCost() < minDeltaCost) {
			minDeltaCost = bestCandIn.getDeltaCost();
			bestCandOut.setTriple(null);
		}
		
		// Evaluate removals
		bestCandOut = evaluateRemovals(bestCandOut, currentSol, true, minDeltaCost);
		if (bestCandOut.getDeltaCost() < minDeltaCost) {
			minDeltaCost = bestCandIn.getDeltaCost();
			bestCandIn.setTriple(null);
		}
		
		// Evaluate exchanges
		Candidate[] inOut = evaluateExchanges(bestCandIn, bestCandOut, currentSol, true, minDeltaCost);
		if (inOut[0].getDeltaCost() < minDeltaCost) {
			bestCandIn = inOut[0];
			bestCandOut = inOut[1];
			minDeltaCost = bestCandIn.getDeltaCost();
		}
		
		updateCurrentSolution(bestCandIn, bestCandOut);
		
		return null;
	}
	
	public Solution<Triple> bestImprovingLocalSearch(){
		Double minDeltaCost;
		Candidate bestCandIn, bestCandOut;

		do {
			bestCandOut = new Candidate();
			bestCandIn = new Candidate();
			minDeltaCost = Double.POSITIVE_INFINITY;
			updateCL();
				
			// Evaluate insertions
			bestCandIn = evaluateInsertions(bestCandIn, currentSol, false, minDeltaCost);
			if (bestCandIn.getDeltaCost() < minDeltaCost) {
				minDeltaCost = bestCandIn.getDeltaCost();
				bestCandOut.setTriple(null);
			}
			 
			// Evaluate removals
			bestCandOut = evaluateRemovals(bestCandOut, currentSol, false, minDeltaCost);
			if (bestCandOut.getDeltaCost() < minDeltaCost) {
				minDeltaCost = bestCandIn.getDeltaCost();
				bestCandIn.setTriple(null);
			}
			
			// Evaluate exchanges
			Candidate[] inOut = evaluateExchanges(bestCandIn, bestCandOut, currentSol, false, minDeltaCost);
			if (inOut[0].getDeltaCost() < minDeltaCost) {
				bestCandIn = inOut[0];
				bestCandOut = inOut[1];
				minDeltaCost = bestCandIn.getDeltaCost();
			}
			
			updateCurrentSolution(bestCandIn, bestCandOut);
			
		} while (minDeltaCost < -Double.MIN_VALUE);

		return null;

	}

	@Override
	public Solution<Triple> constructiveHeuristic() {
		return this.defaultConstructiveHeuristic();
	}
	
	private void chooseCandidateRandomly(List<Triple> candidates) {
		int rndIndex = rng.nextInt(candidates.size());
		Triple inCand = candidates.get(rndIndex);
		CL.remove(inCand);

		int otherClassesPeriods = 0;
		for (Triple sol:currentSol) {

			//Guarantee that the subject is not already being teached by other professor
			if (sol.getD().equals(inCand.getD()))
				return;

			//Count other classes periods
			if (sol.getP().equals(inCand.getP()))
				otherClassesPeriods += sol.getT();
		}

		Integer[] disp = ObjFunction.getRpt(inCand.getP());
		//Create a copy of the rooms so it only gets updated if all periods of this subject can be properly allocated
		int[] roomCopy = roomAvailability.clone();
		ArrayList<Integer> assignedPeriods = new ArrayList<Integer>();

		for (int i=0; i<disp.length; i++) {
			//All periods assigned
			if (inCand.getT() == 0)
				break;

			//Check if the professor is available on the period
			if (disp[i] == 1) {
				//Check if the professor is not already assigned on the period for other classes
				if (otherClassesPeriods > 0) {
					--otherClassesPeriods;
				} else if (roomCopy[i] > 0) {
					//Assign the professor to teach the class on this period, if there is an available room
					inCand.setT(inCand.getT()-1);
					assignedPeriods.add(new Integer(i));
					--roomCopy[i];
				}
			}
		}

		if(inCand.getT() == 0){
			for (int classPeriod:assignedPeriods) {
				Triple cand = new Triple(inCand.getP(), inCand.getD(), classPeriod);
				currentSol.add(cand);
			}
			roomAvailability = roomCopy;
			ObjFunction.evaluate(currentSol);
		}
	}
	
	public Solution<Triple> defaultConstructiveHeuristic() {
		CL = makeCL();
		RCL = makeRCL();
		currentSol = createEmptySol();
		currentCost = Double.POSITIVE_INFINITY;
		Integer[][] apd = ObjFunction.getApd();
		
		/* Main loop, which repeats until the stopping criteria is reached. */
		while (!constructiveStopCriteria()) {
			
			// Sort pair (p, d) by Apd
			CL.sort((p1, p2) -> Integer.compare(apd[p2.getP()][p2.getD()], apd[p1.getP()][p1.getD()]));
			currentCost = ObjFunction.evaluate(currentSol);
			updateCL();
			
			// Get min and max costs
			Triple first = CL.get(0);
			Triple last = CL.get(CL.size() - 1);
			int maxCost = -apd[last.getP()][last.getD()];
			int minCost = -apd[first.getP()][first.getD()];
			
			/*
			 * Among all candidates, insert into the RCL those with the highest
			 * performance using parameter alpha as threshold.
			 */
			for (int i = 0; i < CL.size(); ++i) {
				Triple c = CL.get(i);
				Integer cost = -apd[c.getP()][c.getD()];
				if (cost <= minCost + alpha * (maxCost - minCost)) {
					RCL.add(c);
				}
			}
			
			/*Stop when RCL is empty */
			if (RCL.isEmpty()) {
				break;
			}

			/* Choose a candidate randomly from the RCL */
			chooseCandidateRandomly(RCL);
			RCL.clear();

		}

		return currentSol;
	}
	
	@Override
	public Solution<Triple> solve() {
		return super.solve();
	}
	
	@Override
	public Boolean constructiveStopCriteria() {
		return (currentCost > currentSol.cost) ? false : true;
	}
}
