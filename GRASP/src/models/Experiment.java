package models;

public class Experiment {
	private LocalSearchType localSearchType;
	private ConstructiveHeuristicType constructiveHeuristicType;
	private String key;
	private double perctRandomPlus;
	
	public Experiment(LocalSearchType localSearchType, ConstructiveHeuristicType constructiveHeuristicType,
			String key) {
		super();
		this.localSearchType = localSearchType;
		this.constructiveHeuristicType = constructiveHeuristicType;
		this.key = key;
		this.perctRandomPlus = 0;
	}
	
	public Experiment(LocalSearchType localSearchType, ConstructiveHeuristicType constructiveHeuristicType,
			String key, double perctRandomPlus) {
		super();
		this.localSearchType = localSearchType;
		this.constructiveHeuristicType = constructiveHeuristicType;
		this.key = key;
		this.perctRandomPlus = perctRandomPlus;
	}
	
	public LocalSearchType getLocalSearchType() {
		return localSearchType;
	}
	public void setLocalSearchType(LocalSearchType localSearchType) {
		this.localSearchType = localSearchType;
	}
	public ConstructiveHeuristicType getConstructiveHeuristicType() {
		return constructiveHeuristicType;
	}
	public void setConstructiveHeuristicType(ConstructiveHeuristicType constructiveHeuristicType) {
		this.constructiveHeuristicType = constructiveHeuristicType;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}

	public double getPerctRandomPlus() {
		return perctRandomPlus;
	}

	public void setPerctRandomPlus(double perctRandomPlus) {
		this.perctRandomPlus = perctRandomPlus;
	}
	
}
