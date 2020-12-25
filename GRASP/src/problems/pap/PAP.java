package problems.pap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Arrays;
import java.util.HashMap;

import problems.Evaluator;
import solutions.Solution;


/*
 * This class has 3 main goals
 * 	1) Load and storage problem instance;
 *  2) Compute objective value;
 *  3) Validate solution;
 * 
 */

public class PAP implements Evaluator<Integer> {

	/**
	 * Dimension of the domain.
	 */
	public final Integer size;

	/**
	 * The array of numbers representing the domain.
	 */
	public final Double[] variables;


	public HashMap<String, Integer> values;
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
	public void setVariables(Solution<Integer> sol) {

		resetVariables();
		if (!sol.isEmpty()) {
			for (Integer elem : sol) {
				variables[elem] = 1.0;
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
	
	private Integer solIndex(int p, int d, int t) {
		return p + values.get("D") * (d + values.get("T") * t);
	}
	
	public Double getSolValue(Solution<Integer> sol, int p, int d, int t) {
		return variables[solIndex(p, d, t)];
	}
	
	
	public boolean validate(Solution<Integer> sol) {
		// Problem constraint 1 and 3
		Integer[] periods = new Integer[values.get("P")];
		for (int d = 0; d < values.get("D"); d++) {
	    	int countProf = 0;
			for (int p = 0; p < values.get("P"); p++) {
				int sum = 0;
	    		for (int t = 0; t < values.get("T"); t++) {
	    			sum += getSolValue(sol, p, d, t);
				}
	    		if (sum > 0) {
	    			countProf += 1;
	    			periods[p] += sum;
	    		}
	    		if (sum != 0 && sum != hd[d])
	    			return false;
	    	}
			if (countProf > 1)
				return false;
	    }
		
		// Problem constraint 6
		for (Integer period : periods)
			if (period > values.get("H"))
				return false;
		
		// Problem constraint 4
		for (int t = 0; t < values.get("T"); t++) {
			long sumRooms = 0;
			for (int d = 0; d < values.get("D"); d++) {
				for (int p = 0; p < values.get("P"); p++) {
					sumRooms += getSolValue(sol, p, d, t);
				}
			}
			if (sumRooms > values.get("S"))
				return false;
		}
	    
	    // Problem constraint 5 e 7
		for (int p = 0; p < values.get("P"); p++) {
			for (int t = 0; t < values.get("T"); t++) {
				long sum = 0;
				for (int d = 0; d < values.get("D"); d++) {
					sum += getSolValue(sol, p, d, t);
				}
				if (sum > rpt[p][t])
					return false;
			}
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
	public Double evaluate(Solution<Integer> sol) {

		setVariables(sol);
		return sol.cost = evaluatePAP();

	}

	/**
	 * Evaluates a PAP by calculating the matrix multiplication that defines the
	 * PAP: f(x) = x'.A.x .
	 * 
	 * @return The value of the PAP.
	 */
	public Double evaluatePAP() {

		Double aux = (double) 0, sum = (double) 0;
		Double vecAux[] = new Double[size];

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				aux += variables[j] * apd[i][j];
			}
			vecAux[i] = aux;
			sum += aux * variables[i];
			aux = (double) 0;
		}

		return sum;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see problems.Evaluator#evaluateInsertionCost(java.lang.Object,
	 * solutions.Solution)
	 */
	@Override
	public Double evaluateInsertionCost(Integer elem, Solution<Integer> sol) {

		setVariables(sol);
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
	public Double evaluateInsertionPAP(int i) {

		if (variables[i] == 1)
			return 0.0;

		return evaluateContributionPAP(i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see problems.Evaluator#evaluateRemovalCost(java.lang.Object,
	 * solutions.Solution)
	 */
	@Override
	public Double evaluateRemovalCost(Integer elem, Solution<Integer> sol) {

		setVariables(sol);
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
	public Double evaluateRemovalPAP(int i) {

		if (variables[i] == 0)
			return 0.0;

		return -evaluateContributionPAP(i);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see problems.Evaluator#evaluateExchangeCost(java.lang.Object,
	 * java.lang.Object, solutions.Solution)
	 */
	@Override
	public Double evaluateExchangeCost(Integer elemIn, Integer elemOut, Solution<Integer> sol) {

		setVariables(sol);
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
	public Double evaluateExchangePAP(int in, int out) {

		Double sum = 0.0;

		if (in == out)
			return 0.0;
		if (variables[in] == 1)
			return evaluateRemovalPAP(out);
		if (variables[out] == 0)
			return evaluateInsertionPAP(in);

		sum += evaluateContributionPAP(in);
		sum -= evaluateContributionPAP(out);
		sum -= (apd[in][out] + apd[out][in]);

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
	private Double evaluateContributionPAP(int i) {

		Double sum = 0.0;

		for (int j = 0; j < size; j++) {
			if (i != j)
				sum += variables[j] * (apd[i][j] + apd[j][i]);
		}
		sum += apd[i][i];

		return sum;
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
		apd = new Integer[p][p];
		for(int i = 0; i < p; i++){
			for(int j = 0; j < p; j++){
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

		return p;

	}

	/**
	 * Reserving the required memory for storing the values of the domain
	 * variables.
	 * 
	 * @return a pointer to the array of domain variables.
	 */
	protected Double[] allocateVariables() {
		Double[] _variables = new Double[size];
		return _variables;
	}

	/**
	 * Reset the domain variables to their default values.
	 */
	public void resetVariables() {
		Arrays.fill(variables, 0.0);
	}

	/**
	 * Prints matrix {@link #apd}.
	 */
	public void printMatrix() {

		for (int i = 0; i < size; i++) {
			for (int j = i; j < size; j++) {
				System.out.print(apd[i][j] + " ");
			}
			System.out.println();
		}

	}

	@Override
	public Boolean validateInsertion(Integer elem, Solution<Integer> sol) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Boolean validateRemoval(Integer elem, Solution<Integer> sol) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Boolean validateExchange(Integer elemIn, Integer elemOut, Solution<Integer> sol) {
		// TODO Auto-generated method stub
		return true;
	}

}
