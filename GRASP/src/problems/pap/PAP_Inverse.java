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
	public Double evaluateQBF() {
		return -super.evaluateQBF();
	}
	
	/* (non-Javadoc)
	 * @see problems.pap.PAP#evaluateInsertion(int)
	 */
	@Override
	public Double evaluateInsertionQBF(int i) {	
		return -super.evaluateInsertionQBF(i);
	}
	
	/* (non-Javadoc)
	 * @see problems.pap.PAP#evaluateRemoval(int)
	 */
	@Override
	public Double evaluateRemovalQBF(int i) {
		return -super.evaluateRemovalQBF(i);
	}
	
	/* (non-Javadoc)
	 * @see problems.pap.PAP#evaluateExchange(int, int)
	 */
	@Override
	public Double evaluateExchangeQBF(int in, int out) {
		return -super.evaluateExchangeQBF(in,out);
	}

}
