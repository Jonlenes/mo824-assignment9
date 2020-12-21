package models;

public class AlphaProbability implements Comparable<AlphaProbability> {
	public double alpha;
    public double probability;
    
    public AlphaProbability(double alpha, double probability) {
        this.alpha = alpha;
        this.probability = probability;
    }

	@Override
	public int compareTo(AlphaProbability o) {
		return Double.compare(probability, o.probability);
	}

	@Override
	public String toString() {
		return "AlphaProbability [alpha=" + alpha + ", probability=" + probability + "]";
	}
}
