package project2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import project2.RavensFigure;

/******************************************************************************
 *
 ******************************************************************************/
public class Agent {
	
	private RavenUtil ravenUtil;
	
	/******************************************************************************
	 *
	 ******************************************************************************/
    public Agent() {
    	ravenUtil = new RavenUtil();
    }
    
    /******************************************************************************
    *
    ******************************************************************************/
    public String Solve(RavensProblem problem) {
    	String bestAnswer = "";
    	if(problem.getProblemType().equals("2x1")) {
    		bestAnswer = solve2x1(problem);
    	}else {
    		bestAnswer = solve2x2(problem);
    	}
    	
    	return bestAnswer;
    }
    
    /******************************************************************************
    *
    ******************************************************************************/
    public String solve2x1(RavensProblem problem) {
    	
    	//Get each of the figures in the questions
    	HashMap<String, RavensFigure> questionSet = problem.getFigures();
    	
    	//Get the example figures (A&B) and the prompt figure (C)
    	RavensFigure figureA = questionSet.get("A");
    	RavensFigure figureB = questionSet.get("B");
    	RavensFigure figureC = questionSet.get("C");  	
    	
    	return "1";
    }
    
    /******************************************************************************
    * Transition 
    ******************************************************************************/
    public String solve2x2(RavensProblem problem) {
    	
    	//For cases when multiple "best" answers exist
    	List<String> tiedAnswers = new ArrayList<String>();
    	boolean tieExists = false;
    	
    	String bestAnswer ="";
    	int bestScore = -999;
    	
    	//Get each of the figures in the questions
    	HashMap<String, RavensFigure> questionSet = problem.getFigures();
    	
    	//Get the example figures (A&B) and the prompt figure (C)
    	RavensFigure figureA = questionSet.get("A");
    	RavensFigure figureB = questionSet.get("B");
    	RavensFigure figureC = questionSet.get("C");  
    	
    	Frame frameA = ravenUtil.convertToFrame(figureA);
    	Frame frameB = ravenUtil.convertToFrame(figureB);
    	Frame frameC = ravenUtil.convertToFrame(figureC);
    	
    	
    	//Calculate the transitions from A -> B and A -> C
    	TransitionList transitions = new TransitionList();
    	transitions.addAll(calcTransitions(frameA, frameB));
    	transitions.addAll(calcTransitions(frameA, frameC));
    	
    	
    	Frame calculatedFrame = new Frame("calculated");
    	ravenUtil.applyTransformation(frameA, transitions, calculatedFrame);
    	
    	//Step through each of the possible answers and compare them to C
    	for(int i = 1; i <= 6; i++) {
    		
    		
    		//Get the next possible answer
    		RavensFigure nextAnswer = questionSet.get(String.valueOf(i));
    		
    		int score = ravenUtil.compareFrames(nextAnswer, calculatedFrame);
    		
    		//Capture if this score is better than the previous best
    		if(score > bestScore) {
    			bestScore = score;
    			bestAnswer = String.valueOf(i);
    			tieExists = false;
    		}
    		
    		//If the scores are the same, prepare to guess...
    		else if(score == bestScore) {
    			tieExists = true;
    			tiedAnswers.add(String.valueOf(i));
    			if(!tiedAnswers.contains(bestAnswer)) {
    				tiedAnswers.add(bestAnswer);
    			}
    		}
    	}
    	
    	//If there is a tie, just make a random guess
    	if(tieExists) {
    		Random random = new Random();
    		bestAnswer = String.valueOf(tiedAnswers.get(random.nextInt((tiedAnswers.size()))));
    	}

    	
    	return bestAnswer;
    }
       
    
    /****************************************************************************
     * Given two RavenFigure objects determine the differences that exist between
     * the objects in each figure.  The result is a list of "Result" objects 
     * where each object represents the differences that object occurred as it
     * transitioned from figureA to figureB. 
     * 
     * @param figureA - First Raven's Figure object to compare
     * @param figureB - Second Raven's Figure object to compare
     * 
     * @return - List<SemanticNetwork>: List of the differences each object has
     * 									 between FigureA & FigureB
     *****************************************************************************/
    private TransitionList calcTransitions(Frame figureA, Frame figureB) {
    	
    	TransitionList toReturn = new TransitionList();
    	
    	//Break the object and it's attributes down into an iterable object where the
    	//key is object name, and the value is a map of the object's attribute names and
    	//the corresponding attribute values
    	HashMap<String, HashMap<String, String>> figureAValues = ravenUtil.getValuesMap(figureA);
    	HashMap<String, HashMap<String, String>> figureBValues = ravenUtil.getValuesMap(figureB);
    	
    	ravenUtil.matchObjects(figureAValues, figureBValues);
    	
    	//Step through each object in the first figure and compare it's attributes to
    	//those in the second figure
    	for(String objectName : figureAValues.keySet()) {
    		
    		Transition transition = new Transition(objectName);
    		
    		//Get the attributes and their values for this object in each figure
    		HashMap<String, String> firstFigObjs = figureAValues.get(objectName);
    		HashMap<String, String> secondFigObjs = figureBValues.get(objectName);
    		
    		//See if the second figure has the object found to exist in the first figure
    		if(secondFigObjs != null) {
    				    			    		
	    		//Step through each attribute the object in the first figure has and
	    		//compare the attribute values to the same object in the second figure
	    		for(String attrName : firstFigObjs.keySet()) {
	    			
	    			//Check to see if the object in the second figure has the same attribute
	    			if(secondFigObjs.get(attrName) != null) {
	    				
	    				//Capture the value the attribute has in the second figure
		    			String newValue = secondFigObjs.get(attrName);
		    			String oldValue = firstFigObjs.get(attrName);

		    			if(newValue.equals(oldValue)) {
		    				transition.setChange(attrName, Change.NOCHANGE, null, null);
		    			}else {
		    				transition.setChange(attrName, Change.CHANGE, oldValue, newValue);
		    			}
		    			
		    			
	    			}else {
	    				transition.setChange(attrName, Change.DELETED, null, null);
	    			}

	    		}
	    		
	    		//Step through the attributes for this object in the second
	    		//figure to see if it has attributes the object in the first
	    		//figure does not have
	    		for(String oldAttrName : secondFigObjs.keySet()) {
	    			
	    			//Check to see if the attribute has already been captured
	    			if(!transition.checkAttrExists(oldAttrName)) {
	    				
	    				transition.setChange(oldAttrName, Change.DELETED, null, null);
	    				
	    			}
	    		}
	    		
	    		
	    	//If the object doesn't exist, then create a dummy object
	    	//to represent an object which is in the first figure, but
	    	//does not exist in the second
	    	}else {
	    		transition.setChange(objectName, Change.OBJDELETED, null, null);
	    	}
    		toReturn.add(transition);
    	}
    	
    	//Step through the objects in the second figure to see if
		//any of those objects do not appear in the first figure
    	for(String objectNameOld : figureBValues.keySet()) {
    		   		
    		//See if the object in the second figure is also in the first
    		HashMap<String, String> figureAObjVals = figureAValues.get(objectNameOld);
    		
    		//If the object in the second is not in the first, create a dummy object
    		if(figureAObjVals == null) {
    			
    			Transition transition = new Transition(objectNameOld);
    			
    			transition.setChange(objectNameOld, Change.OBJDELETED, null, null);
	    		
	    		toReturn.add(transition);
    		}
    	}
    	
    	return toReturn;
	}
   

}
