package problems.pap;

import java.io.IOException;

import models.Triple;
import solutions.Solution;


public class PAP_Inverse extends PAP {

	/**
	 * Constructor for the PAP_Inverse class.
	 * 
	 * @param filename
	 *            Name of the file for which the objective function parameters
	 *            should be read.
	 * @throws IOException
	 *             Necessary for I/O operations.
	 */
	public PAP_Inverse(String filename) throws IOException {
		super(filename);
	}


	/* (non-Javadoc)
	 * @see problems.pap.PAP#evaluate()
	 */
	@Override
	public Double evaluatePAP(Solution<Triple> sol) {
		return -super.evaluatePAP(sol);
	}
	
	/* (non-Javadoc)
	 * @see problems.pap.PAP#evaluateInsertion(int)
	 */
	@Override
	public Double evaluateInsertionPAP(Triple triple) {	
		return -super.evaluateInsertionPAP(triple);
	}
	
	/* (non-Javadoc)
	 * @see problems.pap.PAP#evaluateRemoval(int)
	 */
	@Override
	public Double evaluateRemovalPAP(Triple triple) {
		return -super.evaluateRemovalPAP(triple);
	}
	
	/* (non-Javadoc)
	 * @see problems.pap.PAP#evaluateExchange(int, int)
	 */
	@Override
	public Double evaluateExchangePAP(Triple in, Triple out) {
		return -super.evaluateExchangePAP(in,out);
	}

}
