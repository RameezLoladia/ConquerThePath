package edu.nyu.ComputerGame.ConquerThePath;

/**
 * This class represents single territory/cell
 * @author rameez
 *
 */

public class Territory {
	
	private boolean isPath;
	public Territory(){};
	public Territory(boolean isPath, Player owner, int noOfDies) {
		this.isPath = isPath;
		this.owner = owner;
		this.noOfDies = noOfDies;
	}
	private Player owner;
	private int noOfDies;
	public boolean isPath() {
		return isPath;
	}
	public void setPath(boolean isPath) {
		this.isPath = isPath;
	}
	public Player getOwner() {
		return owner;
	}
	public void setOwner(Player owner) {
		this.owner = owner;
	}
	public int getNoOfDies() {
		return noOfDies;
	}
	public void setNoOfDies(int noOfDies) {
		this.noOfDies = noOfDies;
	}
	
	

}
