package project2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import project2.RavensAttribute;
import project2.RavensObject;
import project2.RavensFigure;
import project2.FillerValue;
import project2.FrameFiller;


/*******************************************************************************
 * 
 * 
 *******************************************************************************/
public class RavenUtil {

	/*******************************************************************************
	 * 
	 * 
	 *******************************************************************************/
	public RavenUtil() {
		
	}
	
	/******************************************************************************
	 * This method counts on pass-by-reference! 
	 * 
	 * @param frame
	 * @param transitions
	 *****************************************************************************/
	public void applyTransformation(Frame frame, TransitionList transitions, Frame calcFrame) {
		
		addFillers(frame.getFillers(), calcFrame);
		
		for(Transition t : transitions.getAll()) {
			String objectName = t.getObjectId();
			FrameFiller filler = calcFrame.getFillerByName(objectName);
			for(String fillName : t.getChanges().keySet()) {
				Change change = t.getChanges().get(fillName);
				
				switch (change.getChange()) {
					case Change.OBJDELETED:
						calcFrame.remove(filler);
					break;
					case Change.CHANGE:
						//The object this attribute belongs to could have already been deleted
						if(filler != null) {
							filler.changeValue(fillName, change.getNewValue());
						}
					break;
					case Change.NOCHANGE:
			
					break;
					case Change.DELETED:
						//The object this attribute belongs to could have already been deleted
						if(filler != null) {
							filler.remove(change.getName());
						}
					break;
				}
			}
		}
	}
	
	/*****************************************************************************
	 * This method counts on pass-by-reference!
	 * 
	 * @param toAdd
	 *****************************************************************************/
	private void addFillers(List<FrameFiller> fillers, Frame calcFrame) {
		
		List<FrameFiller> toAdd = new ArrayList<FrameFiller>();
		if(calcFrame.getFillers() != null && calcFrame.getFillers().size() > 0)  {
		for(FrameFiller newFF : fillers) {
			for(FrameFiller existing : calcFrame.getFillers()) {
				if(!existing.getName().equals(newFF.getName())) {
					toAdd.add(newFF);
				}
			}
		}
		}else {
			toAdd.addAll(fillers);
		}
		
		calcFrame.addFillers(toAdd);
		
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
    public Frame convertToFrame(RavensFigure figureA) {
    	
    	Frame toReturn = new Frame(figureA.getName());
    	
    	//List of differences to return
    	List<FrameFiller> fillers = new ArrayList<FrameFiller>();
    	
    	//Break the object and it's attributes down into an iterable object where the
    	//key is object name, and the value is a map of the object's attribute names and
    	//the corresponding attribute values
    	HashMap<String, HashMap<String, String>> figureAValues = getValuesMap(figureA);
    	
    	//Step through each object in the first figure and compare it's attributes to
    	//those in the second figure
    	for(String objectName : figureAValues.keySet()) {
    		
    		//Get the attributes and their values for this object in each figure
    		HashMap<String, String> firstFigObjs = figureAValues.get(objectName);
    		
    			
    			//Create a Result object to hold the difference between the two objects
	    		FrameFiller filler = new FrameFiller();
	    		
	    		//Set what we know so far, that it exists in both and it's name
	    		filler.setName(objectName);
	    		
	    		//Step through each attribute the object in the first figure has and
	    		//compare the attribute values to the same object in the second figure
	    		for(String attrName : firstFigObjs.keySet()) {
	    			
	    			//Create a Result Attribute object to hold the differences found
	    			FillerValue value = new FillerValue();
	    			
	    			//Set the name of the attribute
	    			value.setName(attrName);
	    			
	    			//Capture the value this attribute has in the first figure
	    			value.setVal(firstFigObjs.get(attrName));
		    		
	    			//Add the filler value 
	    			filler.addAttr(value);
	    		}
	    			    		
	    		//Add the filler object to the list
	    		fillers.add(filler);	
    	}
    	
    	toReturn.setFillers(fillers);
    	
    	return toReturn;
	} 
    /**************************************************************************************************
     * Helper method to parse out all of the objects and it's 
     * attributes into a HashMap for easier comparison.
     * 
     * @param figure - The Frame object to parse out
     * @return HashMap: Where the Key is the String Name of the objects in the Frame and the values
     * 					is another HashMap where the String key	is the name of a Attribute and the value
     * 					is the value of the HashMap.
     ***********************************************************************************************/
    public HashMap<String, HashMap<String, String>> getValuesMap(Frame figure) {
    	HashMap<String, HashMap<String, String>> toReturn = new HashMap<String, HashMap<String, String>>();
    	for(FrameFiller obj : figure.getFillers())  {
    		String name = obj.getName();
    		HashMap<String, String> valueMap = new HashMap<String, String>();
    		for(FillerValue attr : obj.getAttributes()) {
    			valueMap.put(attr.getName(), attr.getVal());
    		}
    		toReturn.put(name, valueMap);  		
    	}
    	return toReturn;
    }
    
    /**************************************************************************************************
     * Helper method to parse out all of the objects and it's 
     * attributes into a HashMap for easier comparison.
     * 
     * @param figure - The RavenFigure object to parse out
     * @return HashMap: Where the Key is the String Name of the objects in the Figure and the values
     * 					is another HashMap where the String key	is the name of a Attribute and the value
     * 					is the value of the HashMap.
     ***********************************************************************************************/
    private HashMap<String, HashMap<String, String>> getValuesMap(RavensFigure figure) {
    	HashMap<String, HashMap<String, String>> toReturn = new HashMap<String, HashMap<String, String>>();
    	for(RavensObject obj : figure.getObjects())  {
    		String name = obj.getName();
    		HashMap<String, String> valueMap = new HashMap<String, String>();
    		for(RavensAttribute attr : obj.getAttributes()) {
    			valueMap.put(attr.getName(), attr.getValue());
    		}
    		toReturn.put(name, valueMap);  		
    	}
    	return toReturn;
    }

    /**************************************************************************************************
     * 
     * @param nextAnswer
     * @param calculatedFrame
     * @return
     **************************************************************************************************/
	public int compareFrames(RavensFigure nextAnswer, Frame calculatedFrame) {
				
		HashMap<String, HashMap<String, String>> first = getValuesMap(nextAnswer);
    	HashMap<String, HashMap<String, String>> second = getValuesMap(calculatedFrame);
    	
    	matchObjects(second, first);
    	
		int score = 0;
		
		boolean foundMisMatch = false;
		if(first.size() != second.size()) {
			score = -100;
			foundMisMatch = true;
		}
		
		for(String fillerInFirst : first.keySet()) {

			HashMap<String, String> firstFillers = first.get(fillerInFirst);
    		
    		for(String fillerInSecond : second.keySet()) {
    			
    			HashMap<String, String> secondFillers = second.get(fillerInSecond);
    			
    			if((secondFillers == null && firstFillers != null) ||  (secondFillers != null && firstFillers == null) ||  secondFillers.size() != firstFillers.size())  {
    				score = -100;
    				foundMisMatch = true;
    			}
    		}
		}
		
		if(!foundMisMatch) {
			//Step through each object in the first figure and compare it's attributes to
	    	//those in the second figure
	    	for(String fillerInFirst : first.keySet()) {
	
				HashMap<String, String> firstFillers = first.get(fillerInFirst);
	    		
	    		for(String fillerInSecond : second.keySet()) {
	    			
	    			HashMap<String, String> secondFillers = second.get(fillerInSecond);
	    			
	    			for(String firstValueName : firstFillers.keySet()) {
	    				
	    				String firstValue = firstFillers.get(firstValueName);
	    				
	    				for(String secondValueName : secondFillers.keySet()) {
	    					
	    					String secondValue = secondFillers.get(secondValueName);
	    					
	    					if(secondValueName.equals(firstValueName)) {
	    						if(secondValue.equals(firstValue)) {
	    							score += 1;
	    						}
	    					}
	    				}
	    			}
	    		}
	    	}
		}
				
		return score;
	}
	
	/****************************************************************************************************
	 * This method counts on pass by reference!
	 * 
	 * @param first
	 * @param second
	 *****************************************************************************************************/
	public void matchObjects(HashMap<String, HashMap<String, String>> first, HashMap<String, HashMap<String, String>> second) {
		
		Map<String, HashMap<String, Integer>> objectMatchMap = new HashMap<String, HashMap<String, Integer>>();
		
		
		
		int firstFillerCount = first.size();
		int secondFillerCount = second.size();
		
		if(firstFillerCount == 1 && secondFillerCount == 1)  {
			String nameInFirst = "";
			String nameInSecond = "";
			for(String objNameInFirst : first.keySet()) {
				nameInFirst = objNameInFirst;
			}
			for(String objNameInSecond : second.keySet()) {
				nameInSecond = objNameInSecond;
			}
			HashMap<String, String> temp = second.get(nameInSecond);
			second.remove(nameInSecond);
			second.put(nameInFirst, temp);
		}
		else {
			int deleted = firstFillerCount - secondFillerCount > 0 ? firstFillerCount - secondFillerCount : 0;
			int added = secondFillerCount - firstFillerCount > 0 ? secondFillerCount - firstFillerCount : 0;
			
			
			//Step through each object in the first figure and compare it's attributes to
	    	//those in the second figure
	    	for(String fillerInFirst : first.keySet()) {
	
				HashMap<String, String> firstFillers = first.get(fillerInFirst);
	    		
	    		HashMap<String, Integer> objectSimularityMap = new HashMap<String, Integer>();
	    		
	    		for(String fillerInSecond : second.keySet()) {
	    			
	    			HashMap<String, String> secondFillers = second.get(fillerInSecond);
	    			
	    			int simularityScore = 0;
	    			
	    			for(String firstValueName : firstFillers.keySet()) {
	    				
	    				String firstValue = firstFillers.get(firstValueName);
	    				
	    				for(String secondValueName : secondFillers.keySet()) {
	    					
	    					String secondValue = secondFillers.get(secondValueName);
	    					
	    					if(secondValueName.equals(firstValueName)) {
	    						if(secondValue.equals(firstValue)) {
	    							switch (firstValueName) {
	    								case "shape":
	    									simularityScore += 5;
	    								break;
	    								case "fill" :
	    									simularityScore += 3;
	    								break;
	    								default:
	    									simularityScore += 1;
	    								break;
	    								
	    							}
	    						}
	    					}
	    				}
	    			}
	    			objectSimularityMap.put(fillerInSecond, Integer.valueOf(simularityScore));
	    			
	    			objectMatchMap.put(fillerInFirst, objectSimularityMap);
	    		}
	    	}
			
	    	HashMap<String, HashMap<String, Integer>> bestMatchesMap = findBestMatches(objectMatchMap);
	    	//findDuplicateMatches(objectMatchMap, bestMatchesMap, deleted, added);
	    	renameMatching(second, objectMatchMap);
	    	int x = 3;
	    	int y = x;
		}
	}
	
	/***************************************************************************************************
	 * 
	 * @param second
	 * @param objectMatchMap
	 ***************************************************************************************************/
	private void renameMatching(HashMap<String, HashMap<String, String>> second,
			Map<String, HashMap<String, Integer>> objectMatchMap) {
		for(String ObjNameInFirst : objectMatchMap.keySet()) {
			String highestMatch = "";
			int highestMatchScore = 0;
			for(String matchName : objectMatchMap.get(ObjNameInFirst).keySet()) {
				if(objectMatchMap.get(ObjNameInFirst).get(matchName) > highestMatchScore) {
					highestMatchScore = objectMatchMap.get(ObjNameInFirst).get(matchName);
					highestMatch = matchName;
				}
			}
			HashMap<String, String> temp = second.get(highestMatch);
			second.remove(highestMatch);
			second.put(ObjNameInFirst, temp);
		}
		
	}

	/**************************************************************************************************
	 * 
	 * @param objectMatchMap
	 * @param bestMatchesMap
	 * @param deleted
	 * @param added
	 ***************************************************************************************************/
	private void findDuplicateMatches(Map<String, HashMap<String, Integer>> objectMatchMap, HashMap<String, HashMap<String, Integer>> bestMatchesMap,
			int deleted, int added) {
		
		for(String objectName : bestMatchesMap.keySet()) {
			HashMap<String, Integer> matchesMap = bestMatchesMap.get(objectName);
			for(String matchName : matchesMap.keySet()) {
				HashMap<String, Integer> duplicates = null;
				if((duplicates = findMatches(matchName, bestMatchesMap)) != null) {
					handleDupliates(objectMatchMap, duplicates, deleted, added);
				}
			}
		}
		
		
	}

	/***************************************************************************************************
	 * 
	 * @param objectMatchMap
	 * @param duplicates
	 * @param deleted
	 * @param added
	 ***************************************************************************************************/
	private void handleDupliates(Map<String, HashMap<String, Integer>> objectMatchMap,
			HashMap<String, Integer> duplicates, int deleted, int added) {
		
		
	}

	/**************************************************************************************************
	 * 
	 * @param matchName
	 * @param bestMatchesMap
	 * @return
	 ***************************************************************************************************/
	private HashMap<String, Integer> findMatches(String matchName,
			HashMap<String, HashMap<String, Integer>> bestMatchesMap) {
		
		boolean dupFound = false;
		HashMap<String, Integer> duplicates = new HashMap<String, Integer>();
		for(String objectName : bestMatchesMap.keySet()) {
			HashMap<String, Integer> matchesMap = bestMatchesMap.get(objectName);
			for(String match : matchesMap.keySet()) {
				if(match.equals(matchName)) {
					duplicates.put(match, Integer.valueOf(matchesMap.get(match)));
					dupFound = true;
				}
			}
		}
		return dupFound ? duplicates : null;
	}

	/***************************************************************************************************
	 * 
	 * @param objectMatchMap
	 * @return
	 ***************************************************************************************************/
	private HashMap<String, HashMap<String, Integer>> findBestMatches(Map<String, HashMap<String, Integer>> objectMatchMap) {
		HashMap<String, HashMap<String, Integer>> bestMatchesMap = new HashMap<String, HashMap<String, Integer>>();
    	
    	for(String objectName : objectMatchMap.keySet()) {
    		
    		HashMap<String, Integer> possibleMatches = objectMatchMap.get(objectName);
    		int bestMatchScore = 0;
    		String bestMatchName = "";
    		HashMap<String, Integer> tiedMatches = new HashMap<String, Integer>();
    		boolean tieExists = false; 
    		
    		for(String matchName : possibleMatches.keySet()) {
    			int matchScore = possibleMatches.get(matchName);
    			if(matchScore > bestMatchScore) {
    				bestMatchScore = matchScore;
    				bestMatchName = matchName;
    				tieExists = false;
    				tiedMatches = new HashMap<String, Integer>();
    			}else if(bestMatchScore == matchScore) {
    				tieExists = true;
    				tiedMatches.put(matchName, Integer.valueOf(matchScore));
    				tiedMatches.put(bestMatchName, Integer.valueOf(bestMatchScore));
    			}
    		}
    		
    		if(!tieExists) {
    			HashMap<String, Integer> possibleMatchesMap = new HashMap<String, Integer>();
    			possibleMatchesMap.put(bestMatchName, Integer.valueOf(bestMatchScore));
    			bestMatchesMap.put(objectName, possibleMatchesMap);
    		}else {
    			bestMatchesMap.put(objectName, tiedMatches);
    		}
    	}
    	
    	return bestMatchesMap;
	}
}
