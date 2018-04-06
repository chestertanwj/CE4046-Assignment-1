// Name: Tan Wei Jie, Chester
// Course: CE4046: Intelligent Agents
// Assignment 1: Agent Decision Making

// A. Value Iteration Algorithm
// 1. Initialize maze
// 2. Initialize states
// 3. Set state rewards and walls
// 4. Display maze
// 5. Perform value iteration until state utilities converged
// 5.1. At start of iteration, save current state utilities
// 5.2. For each iteration, for each state, consider all actions and calculate expected utility of taking each action
//		For action with highest expected utility (best action), calculate new state utilities
// 5.3. At end of iteration, compare new state utilities with saved state utilities
// 6. Display final state utilities and final state actions (optimal policy)

// B. Additional Note
// 1. Maze is expressed as a row-major array
// 1.1. States are expressed as (row, column)
// 2. User to specify error value for convergence criteria (assume 0.1)
// 3. Debugging supported
// 3.1. Higher level debugging for more detailed testing
// 4. Data exporting supported

package ce4046.asgmt1;

import java.io.*;

public class ValueIterationPart2 {

	public static void main (String[] args) {
		
		// For debugging purposes
		boolean debugL1 = false;
		boolean debugL2 = false;
		
		// For data exporting purposes
		boolean enWrite = false;
		String fileLocation = "C:\\Users\\Chester\\Desktop\\CE4046 Workspace\\";
		String fileName = "ValueIterationPart2.csv";
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

		System.out.println("Start of Value Iteration Program!");		
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
		
		System.out.println("Start of Value Iteration!");
		
		int iterationNum = 1;
		double discount = 0.99;
		
		boolean converged = false;
		double error = 0.1; // or 2 * Math.pow(10, -23)
		
		double forward = 0.8;
		double side = 0.1;
		
		String [] actions = new String [4];
		actions[0] = "up";
		actions[1] = "down";
		actions[2] = "left";
		actions[3] = "right";
		
		// Loop until state utilities have converged
		while (!converged) {
			if (debugL1)
				System.out.printf("Start of Iteration %d\n", iterationNum);
			
			// Save current state utilities
			double [][] currUtility = new double [rowNum][columnNum];
			for (int row=0; row<rowNum; row++)
				for (int column=0; column<columnNum; column++)
					currUtility[row][column] = maze[row][column].getUtility();
			
			// Calculate new state utilities
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
									upExpectedUtility += forward * currUtility[row-1][column];
								else // Failure
									upExpectedUtility += forward * currUtility[row][column];
								// Attempt to move leftward (westward)
								if (column != 0 && !maze[row][column-1].getWall()) // Success
									upExpectedUtility += side * currUtility[row][column-1];
								else // Failure
									upExpectedUtility += side * currUtility[row][column];
								// Attempt to move rightward (eastward)
								if (column != columnNum-1 && !maze[row][column+1].getWall()) // Success
									upExpectedUtility += side * currUtility[row][column+1];
								else // Failure
									upExpectedUtility += side * currUtility[row][column];
								// Assume up action is best action
								maxActionExpectedUtility = upExpectedUtility;
								maze[row][column].setAction("up");
							}
							
							// Consider down action
							if (action.equals("down")) {
								// Attempt to move forward (southward)
								if (row != rowNum-1 && !maze[row+1][column].getWall()) // Success
									downExpectedUtility += forward * currUtility[row+1][column];
								else // Failure
									downExpectedUtility += forward * currUtility[row][column];
								// Attempt to move leftward (eastward)
								if (column != columnNum-1 && !maze[row][column+1].getWall()) // Success
									downExpectedUtility += side * currUtility[row][column+1];
								else // Failure
									downExpectedUtility += side * currUtility[row][column];
								// Attempt to move rightward (westward)
								if (column != 0 && !maze[row][column-1].getWall()) // Success
									downExpectedUtility += side * currUtility[row][column-1];
								else // Failure
									downExpectedUtility += side * currUtility[row][column];
								// Check if down action is new best action
								if (downExpectedUtility > maxActionExpectedUtility) {
									maxActionExpectedUtility = downExpectedUtility;
									maze[row][column].setAction("down");
								}
							}
							
							// Consider left action
							if (action.equals("left")) {
								// Attempt to move forward (leftward)
								if (column != 0 && !maze[row][column-1].getWall()) // Success
									leftExpectedUtility += forward * currUtility[row][column-1];
								else // Failure
									leftExpectedUtility += forward * currUtility[row][column];
								// Attempt to move leftward (downward)
								if (row != rowNum-1 && !maze[row+1][column].getWall()) // Success
									leftExpectedUtility += side * currUtility[row+1][column];
								else // Failure
									leftExpectedUtility += side * currUtility[row][column];
								// Attempt to move rightward (northward)
								if (row != 0 && !maze[row-1][column].getWall()) // Success
									leftExpectedUtility += side * currUtility[row-1][column];
								else // Failure
									leftExpectedUtility += side * currUtility[row][column];
								// Check if left action is new best action
								if (leftExpectedUtility > maxActionExpectedUtility) {
									maxActionExpectedUtility = leftExpectedUtility;
									maze[row][column].setAction("left");
								}
							}
							
							// Consider right action
							if (action.equals("right")) {
								// Attempt to move forward (rightward)
								if (column != columnNum-1 && !maze[row][column+1].getWall()) // Success
									rightExpectedUtility += forward * currUtility[row][column+1];
								else // Failure
									rightExpectedUtility += forward * currUtility[row][column];
								// Attempt to move leftward (northward)
								if (row != 0 && !maze[row-1][column].getWall()) // Success
									rightExpectedUtility += side * currUtility[row-1][column];
								else // Failure
									rightExpectedUtility += side * currUtility[row][column];
								// Attempt to move rightward (southward)
								if (row != rowNum-1 && !maze[row+1][column].getWall()) // Success
									rightExpectedUtility += side * currUtility[row+1][column];
								else // Failure
									rightExpectedUtility += side * currUtility[row][column];
								// Check if right action is new best action
								if (rightExpectedUtility > maxActionExpectedUtility) {
									maxActionExpectedUtility = rightExpectedUtility;
									maze[row][column].setAction("right");
								}
							}
						}
						
						// Calculate and set new state utility
						maze[row][column].setUtility(maze[row][column].getReward() + discount * maxActionExpectedUtility);
						
						if (debugL2) {
							System.out.printf("Displaying Action Expected Utilities for State (Row: %d, Column: %d)...\n", row, column);
							System.out.printf("Up Expected Utility: %.4f\n", upExpectedUtility);
							System.out.printf("Down Expected Utility: %.4f\n", downExpectedUtility);
							System.out.printf("Left Expected Utility: %.4f\n", leftExpectedUtility);
							System.out.printf("Right Expected Utility: %.4f\n", rightExpectedUtility);
							System.out.printf("Max Action Expected Utility: %.4f\n", maxActionExpectedUtility);
							System.out.printf("Action Expected Utilities for State (Row: %d, Column: %d) Displayed!\n", row, column);
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
			
			// Assume state utilities converged and check for disagreement
			converged = true;
			for (int row=0; row<rowNum; row++)
				for (int column=0; column<columnNum; column++)
					if (!maze[row][column].getWall())
						if ((Math.abs(maze[row][column].getUtility()-currUtility[row][column])) > error)
							converged = false;
			if (converged)
				System.out.println("State Utilities have Converged!");
			
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
		
		System.out.println("End of Value Iteration!");
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
}
