package problems.pap;

import java.io.IOException;


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
	public Double evaluatePAP() {
		return -super.evaluatePAP();
	}
	
	/* (non-Javadoc)
	 * @see problems.pap.PAP#evaluateInsertion(int)
	 */
	@Override
	public Double evaluateInsertionPAP(int i) {	
		return -super.evaluateInsertionPAP(i);
	}
	
	/* (non-Javadoc)
	 * @see problems.pap.PAP#evaluateRemoval(int)
	 */
	@Override
	public Double evaluateRemovalPAP(int i) {
		return -super.evaluateRemovalPAP(i);
	}
	
	/* (non-Javadoc)
	 * @see problems.pap.PAP#evaluateExchange(int, int)
	 */
	@Override
	public Double evaluateExchangePAP(int in, int out) {
		return -super.evaluateExchangePAP(in,out);
	}

}
