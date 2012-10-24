package edu.nyu.ComputerGame.ConquerThePath;

import java.util.ArrayList;
import java.util.Random;

public class ConquerThePathGame {

	private Territory board[][];
	ArrayList<Territory> path;

	public ConquerThePathGame(){}

	// Set the no of territories
	public ConquerThePathGame(int dimension){
		board = new Territory[dimension][dimension];

	}
	//Query for a particular territory
	public Territory getTerritory(int row,int column){
		return board[row][column];
	}
	// Set the territory owners
	public void setTerritoryOwners(){

		for(int i=0;i<board.length;i++){
			for(int j=0;j<board[0].length;j++){
				int r= new Random().nextInt(100)%2;
				if(r==0)
					board[i][j].setOwner(Player.ComputerPlayer);
				else
					board[i][j].setOwner(Player.OtherPlayer);

			}
		}
		board[0][0].setOwner(Player.GamePlayer);	// The first cell is of GamePlayer
	}

	// Set the no of dies for each territory. Can be 1,2,3 or 4
	public void setTerritoryDies(){
		for(int i=0;i<board.length;i++){
			for(int j=0;j<board[0].length;j++){
				int r= new Random().nextInt(100)%3+1;
				board[i][j].setNoOfDies(r);		
			}

		}
		board[0][0].setNoOfDies(4); // Set the no of dies for the GamePlayer territory to 4 to let it start as strong
	}
	//Randomly selects a path from (0,0) to (dimension,dimension)
	public void formPath(int x, int y){
		if(x==0 || y==0){
			path.add(board[x][y]);
			board[x][y].setPath(true);
			return;
		}

		int r= new Random().nextInt(100)%2;
		if(r==0){
			path.add(board[x-1][y]);
			board[x-1][y].setPath(true);
			formPath(x-1,y);

		}
		else{
			path.add(board[x][y-1]);
			board[x][y-1].setPath(true);
			formPath(x,y-1);
		}	
	}
	//Set the game
	public void initialize(){
		path = new ArrayList<Territory>();
		for(int i=0;i<board.length;i++){
			for(int j=0;j<board.length;j++){
				board[i][j] = new Territory();
			}
		}
		this.formPath(board.length-1,board.length-1);
		path.add(board[board.length-1][board.length-1]);
		board[board.length-1][board.length-1].setPath(true);
		this.setTerritoryDies();
		this.setTerritoryOwners();

	}
	// Checks if all the territories along the path have been conquered
	public boolean isWon(){
		for(Territory t:path){
			if(t.getOwner()!=Player.GamePlayer){
				return false;
			}
		}
		return true;
	}
	// Validates a attack that it can be carried only on adjacent 4 cells i.e 4 connectivity
	public boolean validateAttack(int row1,int col1,int row2,int col2){
		double d = Math.sqrt(Math.pow(row1-row2,2)+Math.pow(col1-col2, 2));
		if(d>1){
			return false;
		}
		else{
			return true;
		}
	}
	// Conquer the territory
	public void attack(int row1,int col1,int row2,int col2,ArrayList<Integer> diesRollResult1,ArrayList<Integer> diesRollResult2)
	{
		int sum1=0,sum2 = 0;
		for(Integer i: diesRollResult1)
			sum1 = sum1+i;
		for(Integer i: diesRollResult2)
			sum2 = sum2+i;
		
		if (sum1>sum2)
			board[row2][col2].setOwner(board[row1][col1].getOwner());
		else if(sum2>sum1)
			board[row1][col1].setOwner(board[row2][col2].getOwner());
		else //tie
		{
			int k = new Random().nextInt(100)%2;
			if (k==0)
				board[row2][col2].setOwner(board[row1][col1].getOwner());
			else
				board[row1][col1].setOwner(board[row2][col2].getOwner());
				
		}
		//Redistribution of dies
		board[row1][col1].setNoOfDies((board[row1][col1].getNoOfDies()+board[row2][col2].getNoOfDies())/2);
		board[row2][col2].setNoOfDies((board[row1][col1].getNoOfDies()+board[row2][col2].getNoOfDies())/2);
	}
	
	// Returns a list of die roll output
	public ArrayList<Integer> rollDies(int x,int y){
		ArrayList<Integer> dieRollOutput = new ArrayList<Integer>();
		int n =  board[x][y].getNoOfDies();
		for(int i= 0;i<n;i++){
			dieRollOutput.add(new Random().nextInt(100)%6+1);
			
		}
		
		return dieRollOutput;
	}



}