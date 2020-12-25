package problems.pap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import models.Key;
import models.Triple;
import problems.Evaluator;
import solutions.Solution;


/*
 * This class has 3 main goals
 * 	1) Load and storage problem instance;
 *  2) Compute objective value;
 *  3) Validate solution;
 * 
 */

public class PAP implements Evaluator<Triple> {

	/**
	 * Dimension of the domain.
	 */
	public final Integer size;

	/**
	 * The array of numbers representing the domain.
	 */
	public final Double[][][] variables;


	public static HashMap<String, Integer> values;
	public Integer[] hd;
	public Integer[][] apd;
	public Integer[][] rpt;


	/**
	 * The constructor for QuadracticBinaryFunction class. The filename of the
	 * input for setting matrix of coefficients A of the PAP. The dimension of
	 * the array of variables x is returned from the {@link #readInput} method.
	 * 
	 * @param filename
	 *            Name of the file containing the input for setting the PAP.
	 * @throws IOException
	 *             Necessary for I/O operations.
	 */
	public PAP(String filename) throws IOException {
		size = readInput(filename);
		variables = allocateVariables();
	}

	/**
	 * Evaluates the value of a solution by transforming it into a vector. This
	 * is required to perform the matrix multiplication which defines a PAP.
	 * 
	 * @param sol
	 *            the solution which will be evaluated.
	 */
	public void setVariables(Solution<Triple> sol) {
		resetVariables();
		if (!sol.isEmpty()) {
			for (Triple elem : sol) {
				variables[elem.getP()][elem.getD()][elem.getT()] = 1.0;
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see problems.Evaluator#getDomainSize()
	 */
	@Override
	public Integer getDomainSize() {
		return size;
	}
	
	public Integer getValue(String key) {
		return values.get(key);
	}
	
	private HashMap<Integer, Integer> sumOneToValue(HashMap<Integer, Integer> map, Integer key) {
		Integer value = map.containsKey(key)?  map.get(key) : 0;
		value += 1;
		map.put(key, value);
		return map;
	}
	
	public boolean validate(Solution<Triple> sol) {
		
		HashMap<Integer, Set<Integer>> allocations = new HashMap<Integer, Set<Integer>>();
		HashMap<Integer, Integer> periods = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> workloads = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> rooms = new HashMap<Integer, Integer>();
		HashMap<Key, Integer> restrictions = new HashMap<Key, Integer>();
		
		// Process solution and extract information
		for (Triple triple : sol) {
			int p = triple.getP(), d = triple.getD(), t = triple.getT();
			
			Set<Integer> set = allocations.containsKey(d)?  allocations.get(d) : new HashSet<Integer>();
			set.add(p);
			allocations.put(d, set);
			
			periods = sumOneToValue(periods, d);
			workloads = sumOneToValue(workloads, p);
			rooms = sumOneToValue(rooms, t);
			
			Key key = new Key(p, t);
			Integer countClasses = restrictions.containsKey(key)?  restrictions.get(key) : 0;
			restrictions.put(key, countClasses + 1);
		}

		// Problem constraint 1
		for(Map.Entry<Integer, Set<Integer>> entry : allocations.entrySet()) {
		    // Check if has more than one professor
			if (entry.getValue().size() > 1)
		    	return false;
		}

		// Problem constraint 3
		for(Map.Entry<Integer, Integer> entry : periods.entrySet()) {
		    // Check if has more than one professor
			if (entry.getValue() != hd[entry.getKey()])
		    	return false;
		}
		
		// Problem constraint 4
		for (Map.Entry<Integer, Integer> entry : rooms.entrySet())
			if (entry.getValue() > values.get("S"))
				return false;

		// Problem constraint 6
		for (Map.Entry<Integer, Integer> entry : workloads.entrySet())
			if (entry.getValue() > values.get("H"))
				return false;

	    // Problem constraint 5 e 7
		
		for (Map.Entry<Key, Integer> entry : restrictions.entrySet()) {
			Key key = entry.getKey();
			if (entry.getValue() > rpt[key.getX()][key.getY()])
				return false;
		}

	    return true;
	}

	/**
	 * {@inheritDoc} In the case of a PAP, the evaluation correspond to
	 * computing a matrix multiplication x'.A.x. A better way to evaluate this
	 * function when at most two variables are modified is given by methods
	 * {@link #evaluateInsertionPAP(int)}, {@link #evaluateRemovalPAP(int)} and
	 * {@link #evaluateExchangePAP(int,int)}.
	 * 
	 * @return The evaluation of the PAP.
	 */
	@Override
	public Double evaluate(Solution<Triple> sol) {
		setVariables(sol);
		if (!validate(sol))
			return Double.MAX_VALUE;
		return sol.cost = evaluatePAP(sol);

	}

	/**
	 * Evaluates a PAP by calculating the matrix multiplication that defines the
	 * PAP: f(x) = x'.A.x .
	 * 
	 * @return The value of the PAP.
	 */
	public Double evaluatePAP(Solution<Triple> sol) {
		Integer[] courses = new Integer[values.get("D")];
		Arrays.fill(courses, -1);
		for (Triple triple: sol) {
			courses[triple.getD()] = triple.getP();
		}
		
		Double cost = 0.0;
		for (int i = 0; i < values.get("D"); ++i) {
			if (courses[i] != -1)
				cost += apd[i][courses[i]];
			else
				cost -= 100;
		}
		return cost;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see problems.Evaluator#evaluateInsertionCost(java.lang.Object,
	 * solutions.Solution)
	 */
	@Override
	public Double evaluateInsertionCost(Triple elem, Solution<Triple> sol) {
		setVariables(sol);
		if (!validate(sol))
			return Double.MAX_VALUE;
		return evaluateInsertionPAP(elem);

	}

	/**
	 * Determines the contribution to the PAP objective function from the
	 * insertion of an element.
	 * 
	 * @param i
	 *            Index of the element being inserted into the solution.
	 * @return The variation of the objective function resulting from the
	 *         insertion.
	 */
	public Double evaluateInsertionPAP(Triple triple) {
		if (variables[triple.getP()][triple.getD()][triple.getT()] == 1)
			return 0.0;

		return evaluateContributionPAP(triple);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see problems.Evaluator#evaluateRemovalCost(java.lang.Object,
	 * solutions.Solution)
	 */
	@Override
	public Double evaluateRemovalCost(Triple elem, Solution<Triple> sol) {
		setVariables(sol);
		if (!validate(sol))
			return Double.MAX_VALUE;
		return evaluateRemovalPAP(elem);

	}

	/**
	 * Determines the contribution to the PAP objective function from the
	 * removal of an element.
	 * 
	 * @param i
	 *            Index of the element being removed from the solution.
	 * @return The variation of the objective function resulting from the
	 *         removal.
	 */
	public Double evaluateRemovalPAP(Triple triple) {
		if (variables[triple.getP()][triple.getD()][triple.getT()] == 0)
			return 0.0;

		return -evaluateContributionPAP(triple);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see problems.Evaluator#evaluateExchangeCost(java.lang.Object,
	 * java.lang.Object, solutions.Solution)
	 */
	@Override
	public Double evaluateExchangeCost(Triple elemIn, Triple elemOut, Solution<Triple> sol) {
		setVariables(sol);
		if (!validate(sol))
			return Double.MAX_VALUE;
		return evaluateExchangePAP(elemIn, elemOut);

	}

	/**
	 * Determines the contribution to the PAP objective function from the
	 * exchange of two elements one belonging to the solution and the other not.
	 * 
	 * @param in
	 *            The index of the element that is considered entering the
	 *            solution.
	 * @param out
	 *            The index of the element that is considered exiting the
	 *            solution.
	 * @return The variation of the objective function resulting from the
	 *         exchange.
	 */
	public Double evaluateExchangePAP(Triple in, Triple out) {
        Double sum = 0.0;

		//TODO: implement equal method on Triple?
		if (in == out)
			return 0.0;
		if (variables[in.getP()][in.getD()][in.getT()] == 1)
			return evaluateRemovalPAP(out);
		if (variables[out.getP()][out.getD()][out.getT()] == 0)
			return evaluateInsertionPAP(in);

		sum += evaluateContributionPAP(in);
		sum -= evaluateContributionPAP(out);

		return sum;
	}

	/**
	 * Determines the contribution to the PAP objective function from the
	 * insertion of an element. This method is faster than evaluating the whole
	 * solution, since it uses the fact that only one line and one column from
	 * matrix A needs to be evaluated when inserting a new element into the
	 * solution. This method is different from {@link #evaluateInsertionPAP(int)},
	 * since it disregards the fact that the element might already be in the
	 * solution.
	 * 
	 * @param i
	 *            index of the element being inserted into the solution.
	 * @return the variation of the objective function resulting from the
	 *         insertion.
	 */
	private Double evaluateContributionPAP(Triple triple) {
		double sum = 0.0;
		
		// Check if the course it's already allocated
		for (int t = 0; t < values.get("T"); ++t)
			sum += variables[triple.getP()][triple.getD()][t];
		
		if (sum == 0)
			return Double.valueOf(-apd[triple.getP()][triple.getD()]);
		
		return 0.0;
	}

	/**
	 * Responsible for setting the PAP function parameters by reading the
	 * necessary input from an external file. This method reads the domain's
	 * dimension and matrix {@link #apd}.
	 * 
	 * @param filename
	 *            Name of the file containing the input for setting the black
	 *            box function.
	 * @return The dimension of the domain.
	 * @throws IOException
	 *             Necessary for I/O operations.
	 */
	protected Integer readInput(String filename) throws IOException {

		Reader fileInst = new BufferedReader(new FileReader(filename));
		StreamTokenizer stok = new StreamTokenizer(fileInst);

		values = new HashMap<>();

		String[] keys = {"P", "D", "T", "S", "H"};
		stok.nextToken();
		for (String k:keys) {
			stok.nextToken();
			values.put(k, (int)stok.nval);
			stok.nextToken();
		}

		int p = values.get("P");
		int d = values.get("D");
		int t = values.get("T");

		//HD
		stok.nextToken();
		hd = new Integer[p];
		for(int i = 0; i < values.get("P"); i++){
			stok.nextToken();
			hd[i] = (int)stok.nval;
		}

		//APD
		stok.nextToken();
		apd = new Integer[p][d];
		for(int i = 0; i < p; i++){
			for(int j = 0; j < d; j++){
				stok.nextToken();
				apd[i][j] = (int)stok.nval;
			}
		}

		//RPT
		stok.nextToken();
		rpt = new Integer[p][t];
		for(int i = 0; i < p; i++){
			for(int j = 0; j < t; j++){
				stok.nextToken();
				rpt[i][j] = (int)stok.nval;
			}
		}

		return p*d*t;

	}

	/**
	 * Reserving the required memory for storing the values of the domain
	 * variables.
	 * 
	 * @return a pointer to the array of domain variables.
	 */
	protected Double[][][] allocateVariables() {
		Double[][][] _variables = new Double[values.get("P")][values.get("D")][values.get("T")];
		return _variables;
	}

	/**
	 * Reset the domain variables to their default values.
	 */
	public void resetVariables() {
		for (int p = 0; p < values.get("P"); ++p)
			for (int d = 0; d < values.get("D"); ++d)
				Arrays.fill(variables[p][d], 0.0);
	}


	@Override
	public Integer[][] getApd() {
		return apd;
	}
	
	@Override
	public Integer getHd(int d) {
		return hd[d];
	}
}
