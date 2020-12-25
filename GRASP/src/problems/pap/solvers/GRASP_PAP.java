package problems.pap.solvers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import metaheuristics.grasp.AbstractGRASP;
import models.ConstructiveHeuristicType;
import models.LocalSearchType;
import models.Candidate;
import problems.pap.PAP_Inverse;
import solutions.Solution;


public class GRASP_PAP extends AbstractGRASP<Integer> {

	private LocalSearchType localSearchType;
	private ConstructiveHeuristicType constructionType;
	private double perctRandomPlus;
	
	public GRASP_PAP(Double alpha, Integer iterations, String filename, LocalSearchType localSearchType, 
			ConstructiveHeuristicType constructionType) throws IOException {
		super(new PAP_Inverse(filename), alpha, iterations);
		this.localSearchType = localSearchType;
		this.constructionType = constructionType;
		
	}
	
	public GRASP_PAP(Double alpha, Integer iterations, String filename, LocalSearchType localSearchType, 
			ConstructiveHeuristicType constructionType, double perctRandomPlus) throws IOException {
		super(new PAP_Inverse(filename), alpha, iterations);
		this.localSearchType = localSearchType;
		this.constructionType = constructionType;
		this.perctRandomPlus = perctRandomPlus;
		
	}

	@Override
	public List<Integer> makeCL() {

		List<Entry<Integer,Integer>> _CL = new ArrayList<Entry<Integer,Integer>>();
		for (int i = 0; i < ObjFunction.getDomainSize(); i++) {
			Integer cand = i;
			_CL.add(cand);
		}

		return _CL;
	}

	@Override
	public ArrayList<Integer> makeRCL() {
		
		ArrayList<Integer> _RCL = new ArrayList<Integer>();

		return _RCL;
	}

	@Override
	public void updateCL() {
		// TODO Auto-generated method stub
	}

	@Override
	public Solution<Integer> createEmptySol() {
		Solution<Integer> sol = new Solution<Integer>();
		sol.cost = 0.0;
		return sol;
	}

	@Override
	public Solution<Integer> localSearch() {
		if(this.localSearchType.equals(LocalSearchType.FIRST_IMPROVING)) {
			return this.firstImprovingLocalSearch();
		}
		
		return this.bestImprovingLocalSearch();
	}
	
	private Candidate evaluateInsertions(Candidate cand, Solution<Integer> currentSol, Boolean first, double bestCost) {
		double deltaCost = 0;
		for (Integer candIn : CL) {
			deltaCost = ObjFunction.evaluateInsertionCost(candIn, currentSol);
			if (deltaCost < bestCost) {
				cand.setDeltaCost(deltaCost);
				cand.setIndex(candIn);
				bestCost = deltaCost;
				if (first) break;
			}
		}
		return cand;
	}
	
	private Candidate evaluateRemovals(Candidate cand, Solution<Integer> currentSol, Boolean first, double bestCost) {
		double deltaCost = 0;
		for (Integer candOut : currentSol) {
			deltaCost = ObjFunction.evaluateRemovalCost(candOut, currentSol);
			if (deltaCost < bestCost) {
				cand.setDeltaCost(deltaCost);
				cand.setIndex(candOut);
				bestCost = deltaCost;
				if (first) break;
			}
		}
		return cand;
	}
	
	private Candidate[] evaluateExchanges(Candidate bestCandIn, Candidate bestCandOut, Solution<Integer> currentSol, Boolean first, double bestCost) {
		double deltaCost = 0;
		for (Integer candIn : CL) {
			for (Integer candOut : currentSol) {
				deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, currentSol);
				if (deltaCost < bestCost) {
					bestCandIn.setIndex(candIn);
					bestCandOut.setIndex(candOut);
					bestCandIn.setDeltaCost(deltaCost);
					bestCandOut.setDeltaCost(deltaCost);
					bestCost = deltaCost;
					if (first) break;
				}
			}
		}
		return new Candidate[] {bestCandIn, bestCandOut};
	}
	
	private void updateCurrentSoluction(Candidate bestCandIn, Candidate bestCandOut) {
		Double minDeltaCost = Math.min(bestCandIn.getDeltaCost(), bestCandOut.getDeltaCost());
		if (minDeltaCost < -Double.MIN_VALUE) {
			if (bestCandOut.getIndex() != null) {
				currentSol.remove(bestCandOut.getIndex());
				CL.add(bestCandOut.getIndex());
			}
			if (bestCandIn.getIndex() != null) {
				currentSol.add(bestCandIn.getIndex());
				CL.remove(bestCandIn.getIndex());
			}
			
			ObjFunction.evaluate(currentSol);
		}
	}
	
	public Solution<Integer> firstImprovingLocalSearch(){
		Double minDeltaCost = -Double.MIN_VALUE;
		
		updateCL();
		Candidate bestCandIn = new Candidate(), bestCandOut = new Candidate();
			
		// Evaluate insertions
		bestCandIn = evaluateInsertions(bestCandIn, currentSol, true, minDeltaCost);
		if (bestCandIn.getDeltaCost() < minDeltaCost) {
			minDeltaCost = bestCandIn.getDeltaCost();
			bestCandOut.setIndex(null);
		}
		
		// Evaluate removals
		bestCandOut = evaluateRemovals(bestCandOut, currentSol, true, minDeltaCost);
		if (bestCandOut.getDeltaCost() < minDeltaCost) {
			minDeltaCost = bestCandIn.getDeltaCost();
			bestCandIn.setIndex(null);
		}
		
		// Evaluate exchanges
		Candidate[] inOut = evaluateExchanges(bestCandIn, bestCandOut, currentSol, true, minDeltaCost);
		if (inOut[0].getDeltaCost() < minDeltaCost) {
			bestCandIn = inOut[0];
			bestCandOut = inOut[1];
			minDeltaCost = bestCandIn.getDeltaCost();
		}
		
		updateCurrentSoluction(bestCandIn, bestCandOut);
		
		return null;
	}
	
	public Solution<Integer> bestImprovingLocalSearch(){
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
				bestCandOut.setIndex(null);
			}
			 
			// Evaluate removals
			bestCandOut = evaluateRemovals(bestCandOut, currentSol, false, minDeltaCost);
			if (bestCandOut.getDeltaCost() < minDeltaCost) {
				minDeltaCost = bestCandIn.getDeltaCost();
				bestCandIn.setIndex(null);
			}
			
			// Evaluate exchanges
			Candidate[] inOut = evaluateExchanges(bestCandIn, bestCandOut, currentSol, false, minDeltaCost);
			if (inOut[0].getDeltaCost() < minDeltaCost) {
				bestCandIn = inOut[0];
				bestCandOut = inOut[1];
				minDeltaCost = bestCandIn.getDeltaCost();
			}
			
			updateCurrentSoluction(bestCandIn, bestCandOut);
			
		} while (minDeltaCost < -Double.MIN_VALUE);

		return null;

	}

	@Override
	public Solution<Integer> constructiveHeuristic() {
		if(this.constructionType.equals(ConstructiveHeuristicType.RANDOM_PLUS))
			return this.randomPlusConstructionConstructiveHeuristic();
		return this.defaultConstructiveHeuristic();
	}
	
	private void chooseCandidateRandomly(List<Integer> candidates) {
		int rndIndex = rng.nextInt(candidates.size());
		Integer inCand = candidates.get(rndIndex);
		CL.remove(inCand);
		currentSol.add(inCand);
		ObjFunction.evaluate(currentSol);
	}
	
	public Solution<Integer> defaultConstructiveHeuristic() {
		CL = makeCL();
		RCL = makeRCL();
		currentSol = createEmptySol();
		currentCost = Double.POSITIVE_INFINITY;

		/* Main loop, which repeats until the stopping criteria is reached. */
		while (!constructiveStopCriteria()) {

			double maxCost = Double.NEGATIVE_INFINITY, minCost = Double.POSITIVE_INFINITY;
			currentCost = ObjFunction.evaluate(currentSol);
			updateCL();

			/*
			 * Explore all candidate elements to enter the solution, saving the
			 * highest and lowest cost variation achieved by the candidates.
			 */
			for (Integer c : CL) {
				Double deltaCost = ObjFunction.evaluateInsertionCost(c, currentSol);
				if (deltaCost < minCost)
					minCost = deltaCost;
				if (deltaCost > maxCost)
					maxCost = deltaCost;
			}

			/*
			 * Among all candidates, insert into the RCL those with the highest
			 * performance using parameter alpha as threshold.
			 */
			for (Integer c : CL) {
				Double deltaCost = ObjFunction.evaluateInsertionCost(c, currentSol);
				if (deltaCost <= minCost + alpha * (maxCost - minCost)) {
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
	public Solution<Integer> solve() {
		return super.solve();
	}
	
	public Solution<Integer> randomPlusConstructionConstructiveHeuristic() {
		CL = makeCL();
		RCL = makeRCL();
		currentSol = createEmptySol();
		currentCost = Double.POSITIVE_INFINITY;
		int p = (int) (CL.size() * perctRandomPlus);
		
		for (int i = 0; i < p; ++i) {
			chooseCandidateRandomly(CL);
		}

		/* Main loop, which repeats until the stopping criteria is reached. */
		while (!constructiveStopCriteria()) {

			double maxCost = Double.NEGATIVE_INFINITY, minCost = Double.POSITIVE_INFINITY;
			currentCost = ObjFunction.evaluate(currentSol);
			updateCL();

			/*
			 * Explore all candidate elements to enter the solution, saving the
			 * highest and lowest cost variation achieved by the candidates.
			 */
			for (Integer c : CL) {
				Double deltaCost = ObjFunction.evaluateInsertionCost(c, currentSol);
				if (deltaCost < minCost)
					minCost = deltaCost;
				if (deltaCost > maxCost)
					maxCost = deltaCost;
			}

			/*
			 * Among all candidates, insert into the RCL those with the highest
			 * performance using parameter alpha as threshold.
			 */
			for (Integer c : CL) {
				Double deltaCost = ObjFunction.evaluateInsertionCost(c, currentSol);
				if (deltaCost <= minCost + alpha * (maxCost - minCost)) {
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
	public Boolean constructiveStopCriteria() {
		return (currentCost > currentSol.cost) ? false : true;
	}
}
