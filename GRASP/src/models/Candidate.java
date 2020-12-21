package models;

public class Candidate {
	
	private Integer index;
	private Double deltaCost;
	
	public Candidate(Integer index, Double deltaCost) {
		super();
		this.index = index;
		this.deltaCost = deltaCost;
	}
	
	public Candidate() {
		super();
		this.index = null;
		this.deltaCost = Double.POSITIVE_INFINITY;
	}
	
	public void setIndex(Integer index) {
		this.index = index;
	}
	
	public void setDeltaCost(Double deltaCost) {
		this.deltaCost = deltaCost;
	}

	public Integer getIndex() {
		return index;
	}

	public Double getDeltaCost() {
		return deltaCost;
	}
}
