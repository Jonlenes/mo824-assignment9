package models;

public class Candidate {
	
	private Triple triple;
	private Double deltaCost;
	
	public Candidate(Triple triple, Double deltaCost) {
		super();
		this.triple = triple;
		this.deltaCost = deltaCost;
	}
	
	public Candidate() {
		super();
		this.triple = null;
		this.deltaCost = Double.POSITIVE_INFINITY;
	}
	
	public void setTriple(Triple triple) {
		this.triple = triple;
	}
	
	public void setDeltaCost(Double deltaCost) {
		this.deltaCost = deltaCost;
	}

	public Triple getTriple() {
		return triple;
	}

	public Double getDeltaCost() {
		return deltaCost;
	}
}
