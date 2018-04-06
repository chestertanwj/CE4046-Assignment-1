// Name: Tan Wei Jie, Chester
// Course: CE4046: Intelligent Agents
// Assignment 1: Agent Decision Making

// A. Policy Iteration Algorithm
// 1. Initialize maze
// 2. Initialize states
// 3. Set state rewards and walls
// 4. Display maze
// 5. Perform policy iteration until state actions (policy) do not change
// 5.1. At start of iteration, save current state actions (policy)
// 5.2. For each iteration, for policy evaluation, create a system of linear equations with the current state action (policy)
//		System of linear equations is created using 2 matrices and solved with JAMA (Java Matrix Package)
//		The LHS matrix is created by considering state itself and current state action (current policy)
//		The RHS matrix is created by considering the state reward
//		By solving the system of linear equations, the state utilities are calculated
//		For policy improvement, for each state, consider all actions and calculate expected utility of taking each action
//		State action (policy) is chosen based on the action with the highest expected utility (best action)
// 5.3. At end of iteration, compare new state actions (new policy) with saved state actions (old policy)
// 6. Display final state utilities and final state actions (optimal policy)

// B. Additional Note
// 1. Maze is expressed as a row-major array
// 1.1 States are expressed as (row, column)
// 2. Debugging supported
// 2.1 Higher level debugging for more detailed testing
// 3. Data exporting supported

package ce4046.asgmt1;

import Jama.Matrix; // Refer to: https://math.nist.gov/javanumerics/jama/
import java.io.*;

public class PolicyIterationPart2 {

	public static void main (String[] args) {
		
		// For debugging purposes
		boolean debugL1 = false;
		boolean debugL2 = false;
		
		// For data exporting purposes
		boolean enWrite = false;
		String fileLocation = "C:\\Users\\Chester\\Desktop\\CE4046 Workspace\\";
		String fileName = "PolicyIterationPart2.csv";
		String filePath = fileLocation + fileName;

		File file = new File (filePath);
		FileWriter fw = null;
		try {
			fw = new FileWriter (file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedWriter bw = new BufferedWriter (fw);
		PrintWriter pw = new PrintWriter (bw);
		
		System.out.println("Start of Policy Iteration Program!");		
		System.out.println("Initializing Maze...");
		
		int columnNum = 10;
		int rowNum = 10;
		
		State [][] maze = new State [rowNum][columnNum];
		
		System.out.println("Maze Initialized!");
		System.out.println("Initializing States...");
		
		for (int row=0; row<rowNum; row++)
			for (int column=0; column<columnNum; column++)
				maze[row][column] = new State();
		
		System.out.println("States Initialized!");
		System.out.println("Setting State Rewards and Walls...");
		
		maze[0][1].setReward(1);
		maze[0][2].setReward(2);
		maze[0][3].setReward(-2);
		maze[0][5].setReward(1);
		maze[0][6].setReward(1);
		maze[0][7].setWall(true);
		maze[0][9].setReward(-2);
		
		maze[1][0].setWall(true);
		maze[1][1].setWall(true);
		maze[1][2].setWall(true);
		maze[1][3].setReward(-1);
		maze[1][8].setReward(1);
		
		maze[2][1].setReward(2);
		maze[2][2].setWall(true);
		maze[2][3].setReward(-1);
		maze[2][5].setWall(true);
		maze[2][7].setReward(1);
		
		maze[3][0].setReward(2);
		maze[3][2].setWall(true);
		maze[3][5].setWall(true);
		maze[3][6].setReward(1);
		maze[3][9].setReward(-1);
		
		maze[4][1].setReward(-2);
		maze[4][3].setReward(1);
		maze[4][4].setReward(2);
		maze[4][5].setWall(true);
		maze[4][6].setWall(true);
		maze[4][8].setWall(true);
		maze[4][9].setReward(-1);
		
		maze[5][0].setReward(-2);
		maze[5][1].setWall(true);
		maze[5][3].setReward(1);
		maze[5][4].setReward(2);
		maze[5][5].setWall(true);
		
		maze[6][8].setWall(true);
		
		maze[7][1].setWall(true);
		maze[7][2].setReward(-1);
		maze[7][3].setWall(true);
		maze[7][6].setReward(-1);
		
		maze[8][1].setWall(true);
		maze[8][3].setWall(true);
		maze[8][5].setReward(-1);
		maze[8][6].setWall(true);
		maze[8][7].setWall(true);
		maze[8][8].setReward(-2);
		maze[8][9].setReward(1);
		
		maze[9][1].setReward(-1);
		maze[9][3].setWall(true);
		maze[9][4].setReward(-1);
		maze[9][6].setWall(true);
		maze[9][7].setWall(true);
		maze[9][8].setReward(-2);
		
		System.out.println("State Rewards and Walls Set!");
		System.out.println("Displaying Maze...");
		
		for (int row=0; row<rowNum; row++) {
			for (int column=0; column<columnNum; column++) {
				if (maze[row][column].getWall())
					System.out.printf("%6s ", "wall");
				else
					System.out.printf("%6.4f ", maze[row][column].getReward());
			}
			System.out.println();
		}
		
		System.out.println("Maze Displayed!");
		
		if (enWrite) {
			pw.print("\"Iteration/Row,Column:\",");
			for (int row=0; row<rowNum; row++) {
				for (int column=0; column<columnNum; column++) {
					if (!maze[row][column].getWall())
						pw.printf("\"%s,%s\",", row, column);
				}
			}
			pw.println();
		}
		
		System.out.println("Start of Policy Iteration!");
		
		int iterationNum = 1;
		double discount = 0.99;
		
		boolean unchanged = false;
		
		double forward = 0.8;
		double side = 0.1;
		
		String [] actions = new String [4];
		actions[0] = "up";
		actions[1] = "down";
		actions[2] = "left";
		actions[3] = "right";
		
		// Calculate number of states
		int stateNum = rowNum * columnNum;
		
		// Calculate number of non-walls
		int nonWallNum = stateNum;
		for (int row=0; row<rowNum; row++)
			for (int column=0; column<columnNum; column++)
				if (maze[row][column].getWall())
					nonWallNum -= 1;
		
		// Create an array for mapping state number to non-wall number
		// Index is state number and element is non-wall number
		// Calculate state utilities by system of linear equations that only consider non-walls
		int [] stateToNonWall = new int [stateNum];
		int i = 0;
		for (int row=0; row<rowNum; row++)
			for (int column=0; column<columnNum; column++) {
				if (maze[row][column].getWall())
					stateToNonWall[getNumber(row, column, columnNum)] = 0;
				else {
					stateToNonWall[getNumber(row, column, columnNum)] = i;
					i++;
				}
			}			
		
		// Loop until state actions (policy) do not change
		while (!unchanged) {
			if (debugL1)
				System.out.printf("Start of Iteration %d\n", iterationNum);
			
			// Save current state actions (current policy)
			String currAction [][] = new String [rowNum][columnNum];
			for (int row=0; row<rowNum; row++)
				for (int column=0; column<columnNum; column++)
					currAction[row][column] = maze[row][column].getAction();
			
			// Policy Evaluation
			
			// Initialize LHS matrix
			double [][] LHSM = new double [nonWallNum][nonWallNum];
			for (int mRow=0; mRow<nonWallNum; mRow++)
				for (int mColumn=0; mColumn<nonWallNum; mColumn++)
					LHSM[mRow][mColumn] = 0.0;
			
			// Initialize RHS matrix
			double [] RHSM = new double [nonWallNum];
			for (int mRow=0; mRow<nonWallNum; mRow++)
				RHSM[mRow] = 0.0;
			
			// Populate LHS and RHS matrices
			for (int row=0; row<rowNum; row++) {
				for (int column=0; column<columnNum; column++) {
					if (!maze[row][column].getWall()) {
						String action = maze[row][column].getAction();
						
						// Populate LHS matrix by considering state itself and current state action (current policy)
						
						// Consider state itself
						LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row, column, columnNum)]] += 1.0;
						
						// Consider up action
						if (action.equals("up")) {
							// Attempt to move forward (northward)
							if (row != 0 && !maze[row-1][column].getWall()) // Success
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row-1, column, columnNum)]] -= discount * forward;
							else // Failure
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row, column, columnNum)]] -= discount * forward;
							// Attempt to move leftward (westward)
							if (column != 0 && !maze[row][column-1].getWall()) // Success
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row, column-1, columnNum)]] -= discount * side;
							else // Failure
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row, column, columnNum)]] -= discount * side;
							// Attempt to move right side (eastward)
							if (column != columnNum-1 && !maze[row][column+1].getWall()) // Success
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row, column+1, columnNum)]] -= discount * side;
							else // Failure
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row, column, columnNum)]] -= discount * side;
						}
						
						// Consider down action
						if (action.equals("down")) {
							// Attempt to move forward (southward)
							if (row != rowNum-1 && !maze[row+1][column].getWall()) // Success
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row+1, column, columnNum)]] -= discount * forward;
							else // Failure
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row, column, columnNum)]] -= discount * forward;
							// Attempt to move leftward (eastward)
							if (column != columnNum-1 && !maze[row][column+1].getWall()) // Success
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row, column+1, columnNum)]] -= discount * side;
							else // Failure
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row, column, columnNum)]] -= discount * side;
							// Attempt to move rightward (westward)
							if (column != 0 && !maze[row][column-1].getWall()) // Success
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row, column-1, columnNum)]] -= discount * side;
							else // Failure
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row, column, columnNum)]] -= discount * side;
						}
						
						// Consider left action
						if (action.equals("left")) {
							// Attempt to move forward (westward)
							if (column != 0 && !maze[row][column-1].getWall()) // Success
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row, column-1, columnNum)]] -= discount * forward;
							else // Failure
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row, column, columnNum)]] -= discount * forward;
							// Attempt to move leftward (southward)
							if (row != rowNum-1 && !maze[row+1][column].getWall()) // Success
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row+1, column, columnNum)]] -= discount * side;
							else // Failure
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row, column, columnNum)]] -= discount * side;
							// Attempt to move rightward (northward)
							if (row != 0 && !maze[row-1][column].getWall()) // Success
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row-1, column, columnNum)]] -= discount * side;
							else // Failure
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row, column, columnNum)]] -= discount * side;
						}
						
						// Consider right action
						if (action.equals("right")) {
							// Attempt to move forward (eastward)
							if (column != columnNum-1 && !maze[row][column+1].getWall()) // Success
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row, column+1, columnNum)]] -= discount * forward;
							else // Failure
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row, column, columnNum)]] -= discount * forward;
							// Attempt to move leftward (northward)
							if (row != 0 && !maze[row-1][column].getWall()) // Success
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row-1, column, columnNum)]] -= discount * side;
							else // Failure
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row, column, columnNum)]] -= discount * side;
							// Attempt to move rightward (southward)
							if (row != rowNum-1 && !maze[row+1][column].getWall()) // Success
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row+1, column, columnNum)]] -= discount * side;
							else // Failure
								LHSM[stateToNonWall[getNumber(row, column, columnNum)]][stateToNonWall[getNumber(row, column, columnNum)]] -= discount * side;
						}
						
						// Populate RHS matrix with state reward
						RHSM[stateToNonWall[getNumber(row, column, columnNum)]] += maze[row][column].getReward();
					}
				}
			}
			
			if (debugL2) {
				System.out.printf("Displaying LHS Matrix for Iteration %d...\n", iterationNum);
				
				for (int mRow=0; mRow<nonWallNum; mRow++) {
					for (int mColumn=0; mColumn<nonWallNum; mColumn++) {
						System.out.printf("%6.3f,", LHSM[mRow][mColumn]);
					}
					System.out.println();
				}
				
				System.out.printf("LHS Matrix for Iteration %d Displayed!\n", iterationNum);
				System.out.printf("Displaying RHS Matrix for Iteration %d...\n", iterationNum);
				
				for (int mRow=0; mRow<nonWallNum; mRow++) {
					System.out.printf("%6.3f\n", RHSM[mRow]);
				}
				
				System.out.printf("RHS Matrix for Iteration %d Displayed!\n", iterationNum);
			}
			
			// Calculate state utilities by solving system of linear equations
			Matrix LHS = new Matrix(LHSM);
			Matrix RHS = new Matrix(RHSM, nonWallNum);
			Matrix ANS = LHS.solve(RHS);
			
			// Set state utilities
			for (int row=0; row<rowNum; row++)
				for (int column=0; column<columnNum; column++)
					if (!maze[row][column].getWall())
						maze[row][column].setUtility(ANS.get(stateToNonWall[getNumber(row, column, columnNum)], 0));
			
			// Policy Improvement (similar to Value Iteration)
			
			for (int row=0; row<rowNum; row++) {
				for (int column=0; column<columnNum; column++) {
					if (!maze[row][column].getWall()) {
						
						double upExpectedUtility = 0.0;
						double downExpectedUtility = 0.0;
						double leftExpectedUtility = 0.0;
						double rightExpectedUtility = 0.0;
						double maxActionExpectedUtility = 0.0;
						
						// Consider all actions
						for (int a=0; a<actions.length; a++) {
							String action = actions[a];
							
							// Consider up action
							if (action.equals("up")) {
								// Attempt to move forward (northward)
								if (row != 0 && !maze[row-1][column].getWall()) // Success
									upExpectedUtility += forward * maze[row-1][column].getUtility();
								else // Failure
									upExpectedUtility += forward * maze[row][column].getUtility();
								// Attempt to move leftward (westward)
								if (column != 0 && !maze[row][column-1].getWall()) // Success
									upExpectedUtility += side * maze[row][column-1].getUtility();
								else // Failure
									upExpectedUtility += side * maze[row][column].getUtility();
								// Attempt to move rightward (eastward)
								if (column != columnNum-1 && !maze[row][column+1].getWall()) // Success
									upExpectedUtility += side * maze[row][column+1].getUtility();
								else // Failure
									upExpectedUtility += side * maze[row][column].getUtility();
								// Assume up action is best action
								maxActionExpectedUtility = upExpectedUtility;
								maze[row][column].setAction("up");
							}
							
							// Consider down action
							if (action.equals("down")) {
								// Attempt to move forward (southward)
								if (row != rowNum-1 && !maze[row+1][column].getWall()) // Success
									downExpectedUtility += forward * maze[row+1][column].getUtility();
								else // Failure
									downExpectedUtility += forward * maze[row][column].getUtility();
								// Attempt to move leftward (eastward)
								if (column != columnNum-1 && !maze[row][column+1].getWall()) // Success
									downExpectedUtility += side * maze[row][column+1].getUtility();
								else // Failure
									downExpectedUtility += side * maze[row][column].getUtility();
								// Attempt to move rightward (westward)
								if (column != 0 && !maze[row][column-1].getWall()) // Success
									downExpectedUtility += side * maze[row][column-1].getUtility();
								else // Failure
									downExpectedUtility += side * maze[row][column].getUtility();
								// Check if down action is new best action
								if (downExpectedUtility > maxActionExpectedUtility) {
									maxActionExpectedUtility = downExpectedUtility;
									maze[row][column].setAction("down");
								}
							}
							
							// Consider left action
							if (action.equals("left")) {
								// Attempt to move forward (westward)
								if (column != 0 && !maze[row][column-1].getWall()) // Success
									leftExpectedUtility += forward * maze[row][column-1].getUtility();
								else // Failure
									leftExpectedUtility += forward * maze[row][column].getUtility();
								// Attempt to move leftward (southward)
								if (row != rowNum-1 && !maze[row+1][column].getWall()) // Success
									leftExpectedUtility += side * maze[row+1][column].getUtility();
								else // Failure
									leftExpectedUtility += side * maze[row][column].getUtility();
								// Attempt to move rightward (northward)
								if (row != 0 && !maze[row-1][column].getWall()) // Success
									leftExpectedUtility += side * maze[row-1][column].getUtility();
								else // Failure
									leftExpectedUtility += side * maze[row][column].getUtility();
								// Check if left action is new best action
								if (leftExpectedUtility > maxActionExpectedUtility) {
									maxActionExpectedUtility = leftExpectedUtility;
									maze[row][column].setAction("left");
								}
							}
							
							// Consider right action
							if (action.equals("right")) {
								// Attempt to move forward (eastward)
								if (column != columnNum-1 && !maze[row][column+1].getWall()) // Success
									rightExpectedUtility += forward * maze[row][column+1].getUtility();
								else // Failure
									rightExpectedUtility += forward * maze[row][column].getUtility();
								// Attempt to move leftward (northward)
								if (row != 0 && !maze[row-1][column].getWall()) // Success
									rightExpectedUtility += side * maze[row-1][column].getUtility();
								else // Failure
									rightExpectedUtility += side * maze[row][column].getUtility();
								// Attempt to move rightward (southward)
								if (row != rowNum-1 && !maze[row+1][column].getWall()) // Success
									rightExpectedUtility += side * maze[row+1][column].getUtility();
								else // Failure
									rightExpectedUtility += side * maze[row][column].getUtility();
								// Check if right action is new best action
								if (rightExpectedUtility > maxActionExpectedUtility) {
									maxActionExpectedUtility = rightExpectedUtility;
									maze[row][column].setAction("right");
								}
							}
						}
					}
				}
			}
			
			if (debugL1) {
				System.out.printf("Displaying Intermediate State Utilities for Iteration %d...\n", iterationNum);
				
				for (int row=0; row<rowNum; row++) {
					for (int column=0; column<columnNum; column++) {
						if (maze[row][column].getWall())
							System.out.printf("%8s ", "wall");
						else
							System.out.printf("%8.4f ", maze[row][column].getUtility());
					}
					System.out.println();
				}
				
				System.out.printf("Intermediate State Utilities for Iteration %d Displayed!\n", iterationNum);
				System.out.printf("Displaying Intermediate State Actions (Non-optimal Policy) for Iteration %d...\n", iterationNum);
				
				for (int row=0; row<rowNum; row++) {
					for (int column=0; column<columnNum; column++) {
						if (maze[row][column].getWall())
							System.out.printf("%6s ", "wall");
						else
							System.out.printf("%6s ", maze[row][column].getAction());
					}
					System.out.println();
				}
				
				System.out.printf("Intermediate State Actions (Non-optimal Policy) for Iteration %d Displayed!\n", iterationNum);
			}
			
			// Assume state actions (policy) have not changed and check for disagreement
			unchanged = true;
			for (int row=0; row<rowNum; row++)
				for (int column=0; column<columnNum; column++)
					if (!maze[row][column].getWall())
						if (!maze[row][column].getAction().equals(currAction[row][column]))
							unchanged = false;
			if (unchanged)
				System.out.println("State Actions (Policy) have not Changed!");
			
			if (enWrite) {
				pw.printf("%d,", iterationNum);
				for (int row=0; row<rowNum; row++)
					for (int column=0; column<columnNum; column++)
						if (!maze[row][column].getWall())
							pw.printf("%.4f,", maze[row][column].getUtility());
				pw.println();
			}
			
			if (debugL1)
				System.out.printf("End of Iteration %d\n", iterationNum);
			iterationNum++;
		}
		
		System.out.println("End of Policy Iteration!");
		System.out.printf("Iterations taken: %d\n", iterationNum-1);
		
		System.out.println("Displaying Final State Utilities...");
		
		for (int row=0; row<rowNum; row++) {
			for (int column=0; column<columnNum; column++) {
				if (maze[row][column].getWall())
					System.out.printf("%8s ", "wall");
				else
					System.out.printf("%8.4f ", maze[row][column].getUtility());
			}
			System.out.println();
		}
		
		System.out.println("Final State Utilities Displayed!");
		System.out.println("Displaying Final State Actions (Optimal Policy)...");
		
		for (int row=0; row<rowNum; row++) {
			for (int column=0; column<columnNum; column++) {
				if (maze[row][column].getWall())
					System.out.printf("%6s ", "wall");
				else
					System.out.printf("%6s ", maze[row][column].getAction());
			}
			System.out.println();
		}
		
		System.out.println("Final State Actions (Optimal Policy) Displayed!");
		System.out.println("End of Value Iteration Program!");
		
		pw.close();
	}

	public static int getNumber (int row, int column, int columnNum) {
		int number = row * columnNum + column;
		return number;
	}
	
	public static int getRow (int number, int columnNum) {
		int row = number / columnNum;
		return row;
	}
	
	public static int getColumn (int number, int rowNum) {
		int column = number % rowNum;
		return column;
	}
}
