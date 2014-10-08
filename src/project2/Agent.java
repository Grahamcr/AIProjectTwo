package project2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import project2.SemanticNetworkObj;
import project2.RavensFigure;

/******************************************************************************
 * The Agent class is responsible for attempting to solve the Raven's Progressive
 * Matrices Problem.  This Agent uses two approaches to score the what it
 * believes to be the correct answer and the combines those scores to give it's
 * best guess.  The first approach uses Semantic Networks to represent the 
 * transitions between Raven's figures and the Generate and Test method to find
 * which answer has a similar Semantic Network for the transitions it undergoes.
 * The second approach uses Frames to represent the Raven's Figures and then
 * determines the transitions required to go from the starting figure, A, to 
 * the answer figure, D, using Mean Ends Analysis. The agent then calculates
 * which figure is most similar to the frame it calculated to be the answer
 * based on it's analysis using a weighted score. 
 * 
 *@author Craig Graham 
 ******************************************************************************/
public class Agent {

	/*** Utility class used to complete matching calculations*/
	private MatchUtil matchUtil;
	
	/*** Utility class used to complete matching calculations*/
	private SemanticNetworkUtil smUtil;
	
	/*** Utility class used to complete matching calculations*/
	private FrameUtil frameUtil;

	/******************************************************************************
	 * Constructor to instantiate the RavenUtil that will be used to complete
	 * the calculations. 
	 ******************************************************************************/
	public Agent() {
		matchUtil = new MatchUtil();
		smUtil = new SemanticNetworkUtil();
		frameUtil = new FrameUtil();
	}

	/******************************************************************************
    * Method to solve either a 2x1 Matrix or a 2x2 Matrix
	* @param problem - Raven's Problem to solve
	* @return String - the agent's best answer for the problem
    ******************************************************************************/
	public String Solve(RavensProblem problem) {
		String bestAnswer = "";
		if (problem.getProblemType().equals("2x1")) {
			bestAnswer = solve2x1(problem);
		} else {
			bestAnswer = solve2x2(problem);
		}
		System.out.println(" - Finished!");
		return bestAnswer;
	}

	/******************************************************************************
    * Method to solve a 2x1 Matrix 
	* @param problem - Raven's Problem to solve
	* @return String - the agent's best answer for the problem
    ******************************************************************************/
	public String solve2x1(RavensProblem problem) {

			//No logic required here....just be random
			//This project does not need to solve 2x1.
			//TODO:Add some sort of learning here!!!!!
    		Random random = new Random();
    		return String.valueOf(random.nextInt((6)));


 
	}

	/******************************************************************************
	 * Method to solve a 2x1 Matrix 
	 * @param problem - Raven's Problem to solve
	 * @return String - the agent's best answer for the problem
	 ******************************************************************************/
	public String solve2x2(RavensProblem problem) {

		System.out.print("Solving Question: " + problem.getName());
		// For cases when multiple "best" answers exist
		List<String> tiedAnswers = new ArrayList<String>();
		boolean tieExists = false;

		String bestAnswer = "";
		int bestScore = -999;

		// Get each of the figures in the questions
		HashMap<String, RavensFigure> questionSet = problem.getFigures();

		// Get the example figures (A&B) and the prompt figure (C)
		RavensFigure figureA = questionSet.get("A");
		RavensFigure figureB = questionSet.get("B");
		RavensFigure figureC = questionSet.get("C");

		// Break the object and it's attributes down into an iterable object
		// where the key is object name, and the value is a map of the object's attribute
		// names and the corresponding attribute values
		HashMap<String, HashMap<String, String>> figureAValues = matchUtil
				.getValuesMap(figureA);
		HashMap<String, HashMap<String, String>> figureBValues = matchUtil
				.getValuesMap(figureB);
		HashMap<String, HashMap<String, String>> figureCValues = matchUtil
				.getValuesMap(figureC);

		//match the objects using Analogical Reasoning
		matchUtil.matchObjects(figureAValues, figureBValues);
		matchUtil.matchObjects(figureBValues, figureCValues);
				
		// Calculate the transitions from A -> B and A -> C
		TransitionList transitions = new TransitionList();
		transitions.addAll(frameUtil.calcTransitions(figureAValues, figureBValues));
		transitions.addAll(frameUtil.calcTransitions(figureAValues, figureCValues));

		//The Frames Approach
		Frame frameA = frameUtil.convertToFrame(figureAValues);
		Frame calculatedFrame = new Frame("calculated");
		frameUtil.applyTransformation(frameA, transitions, calculatedFrame);

		HashMap<String, HashMap<String, String>> calculatedFrameValues = matchUtil
				.getValuesMap(calculatedFrame);
		
		//The Semantic Network approach
		//Determine the differences which exist between Figure A and Figure B
    	List<SemanticNetworkObj> baselineDiffAB = smUtil.calculateDifference(figureAValues, figureBValues);
		
    	//Determine the differences which exist between Figure A and Figure C
    	List<SemanticNetworkObj> baselineDiffAC = smUtil.calculateDifference(figureAValues, figureCValues);
    	
		// Step through each of the possible answers and make Semantic Network and Frame Comparisons
		for (int i = 1; i <= 6; i++) {
			
			// Get the next possible answer
			RavensFigure nextAnswer = questionSet.get(String.valueOf(i));
			
			HashMap<String, HashMap<String, String>> nextValues = matchUtil
					.getValuesMap(nextAnswer);

			//Use Analogical Reasoning to match objects
			matchUtil.matchObjects(figureBValues, nextValues);
			matchUtil.matchObjects(figureBValues, figureCValues);
			
			
			//Frames approach
			int score = frameUtil.compareFrames(nextValues, calculatedFrameValues);

			//Semantic Network Approach - 
    		//Determine the differences which exist between Figure B and the possible answer "i"
    		List<SemanticNetworkObj> testDiffBD = smUtil.calculateDifference(figureBValues, nextValues);
    		
    		//Determine the differences which exist between Figure C and the possible answer "i"
    		List<SemanticNetworkObj> testDiffCD = smUtil.calculateDifference(figureCValues, nextValues);
    		
    		//Compare the differences between A&B to those between C&Answer "i"
    		score += smUtil.compareDifference(baselineDiffAB, testDiffCD);
    		
    		//Compare the differences between A&C to those between B&Answer "i"
    		score += smUtil.compareDifference(baselineDiffAC, testDiffBD);
    		
			// Capture if this score is better than the previous best
			if (score > bestScore) {
				bestScore = score;
				bestAnswer = String.valueOf(i);
				tieExists = false;
			}

			// If the scores are the same, prepare to guess...
			else if (score == bestScore) {
				tieExists = true;
				tiedAnswers.add(String.valueOf(i));
				if (!tiedAnswers.contains(bestAnswer)) {
					tiedAnswers.add(bestAnswer);
				}
			}
		}

		// If there is a tie, just make a random guess
		if (tieExists) {
			Random random = new Random();
			bestAnswer = String.valueOf(tiedAnswers.get(random
					.nextInt((tiedAnswers.size()))));
		}

		return bestAnswer;
	}

	
}
