// Name: Tan Wei Jie, Chester
// Course: CE4046: Intelligent Agents
// Assignment 1: Agent Decision Making

package ce4046.asgmt1;

public class State {
	
	// Instance Variables
	private double reward;
	private boolean wall;
	
	private double utility;	
	private String action;
	
	// Constructor
	public State () {
		this.reward = -0.04;
		this.wall = false;
		
		this.utility = 0.0;
		this.action = "up";
	}
	
	// Instance Methods
	// Get and Set Methods
	public double getReward () {
		return reward;
	}
	
	public void setReward (double reward) {
		this.reward = reward;
	}
	
	public boolean getWall () {
		return wall;
	}
	
	public void setWall (boolean wall) {
		this.wall = wall;
	}
	
	public double getUtility () {
		return utility;
	}
	
	public void setUtility (double utility) {
		this.utility = utility;
	}

	public String getAction () {
		return action;
	}
	
	public void setAction (String action) {
		this.action = action;
	}
}
