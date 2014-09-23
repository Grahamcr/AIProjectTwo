package project2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import project2.SemanticNetworkAttribute;
import project2.SemanticNetworkObj;
import project2.RavensAttribute;
import project2.RavensObject;
import project2.RavensFigure;
import project2.FillerValue;
import project2.FrameFiller;


/*******************************************************************************
 * RavenUtil is a Service class which is responsible for doing most of the 
 * calculations required when solving a Raven's Problem.  The Utility is capable
 * of completing calculations using Semantic Networks, Frames, Means-End 
 * Reduction and Generate & Test.
 * 
 * @author Craig Graham
 *******************************************************************************/
public class RavenUtil {

	/*** Constant to mark when an object in a frame is deleted */
	private static final String DELETEDOBJECT = "DELETED";

	/*******************************************************************************
	 * Default empty constructor. 
	 * 
	 *******************************************************************************/
	public RavenUtil() {
		
	}
	
	/******************************************************************************
	 * Using Frames to represent a Raven's Figure, add the transformations or
	 * attribute changes the Figure underwent horizontally and when 
	 * applicable, vertically. 
	 * This method counts on pass-by-reference to make changes to the object
	 * calcFrame. 
	 * 
	 * @param frame - The starting point frame (ie. Raven's Figure A)
	 * @param transitions - The transformations the starting frame underwent 
	 * 						in the problem set.
	 * @param calcFrame - The Raven's Frame which will be the "calculated"
	 * 					  answer for the problem. 
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
							if(filler.getName().equals(Change.DELETED)) {
								calcFrame.remove(filler);
							}
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
    /**********************************************************************************************
     * 
     * @param values
     * @return
     ***********************************************************************************************/
    public List<SemanticNetworkObj> convertMapToNetwork(HashMap<String, HashMap<String, String>> values) {
    	List<SemanticNetworkObj> toReturn = new ArrayList<SemanticNetworkObj>();
    	for(String objectName : values.keySet()) {
    		HashMap<String, String> network = values.get(objectName);
    		SemanticNetworkObj obj = new SemanticNetworkObj();
    		obj.setName(objectName);
    		List<SemanticNetworkAttribute> attributes = new ArrayList<SemanticNetworkAttribute>();
    		for(String attributeVal : network.keySet()) {
    			SemanticNetworkAttribute attr = new SemanticNetworkAttribute();
    			attr.setName(attributeVal);
    			attr.setNewVal( network.get(attributeVal));
    			attributes.add(attr);
    		}
    		obj.setAttributes(attributes);
    		toReturn.add(obj);
    	}
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
    public HashMap<String, HashMap<String, String>> getValuesMap(List<SemanticNetworkObj> figure) {
    	HashMap<String, HashMap<String, String>> toReturn = new HashMap<String, HashMap<String, String>>();
    	for(SemanticNetworkObj obj : figure)  {
    		String name = obj.getName();
    		HashMap<String, String> valueMap = new HashMap<String, String>();
    		for(SemanticNetworkAttribute attr : obj.getAttributes()) {
    			valueMap.put(attr.getName(), attr.getNewVal());
    		}
    		toReturn.put(name, valueMap);  		
    	}
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
//		if(first.size() != second.size()) {
//			score = -100;
//			foundMisMatch = true;
//		}
//		
//		for(String fillerInFirst : first.keySet()) {
//
//			HashMap<String, String> firstFillers = first.get(fillerInFirst);
//    		
//    		for(String fillerInSecond : second.keySet()) {
//    			
//    			HashMap<String, String> secondFillers = second.get(fillerInSecond);
//    			
//    			if((secondFillers == null && firstFillers != null) ||  (secondFillers != null && firstFillers == null) ||  secondFillers.size() != firstFillers.size())  {
//    				score = -100;
//    				foundMisMatch = true;
//    			}
//    		}
//		}
		
		if(!foundMisMatch) {
			//Step through each object in the first figure and compare it's attributes to
	    	//those in the second figure
	    	for(String fillerInFirst : first.keySet()) {
	    		try {
					HashMap<String, String> firstFillers = first.get(fillerInFirst);
		    		
		    		for(String fillerInSecond : second.keySet()) {
		    			try {
			    			HashMap<String, String> secondFillers = second.get(fillerInSecond);
			    			
			    			for(String firstValueName : firstFillers.keySet()) {
			    				try {
				    				String firstValue = firstFillers.get(firstValueName);
				    				
				    				for(String secondValueName : secondFillers.keySet()) {
				    					try {
					    					String secondValue = secondFillers.get(secondValueName);
					    					
					    					if(secondValueName.equals(firstValueName)) {
					    						if(secondValue.equals(firstValue)) {
					    							switch (firstValue) {
					    								case "shape":
					    									score += 5;
					    								break;
					    								case "overlaps":
					    									score += 4;
					    								break;
					    								case "fill":
					    									score += 2;
					    								break;
					    								case "inside":
					    								case "outside":
					    								case "above":
					    								case "below":
					    									score += 3;
					    								break;
					    								case "angle":
					    									score += 2;
					    								break;
					    								default:
					    									score += 1;
					    								break;
					    							}
					    							
					    						}
					    					}
				    					}catch(NullPointerException e) {
				    		    			
				    		    		}
				    				}
			    				}catch(NullPointerException e) {
			    	    			
			    	    		}
			    			}
		    			}catch(NullPointerException e) {
			    			
			    		}
		    		}
	    		}catch(NullPointerException e) {
	    			
	    		}
	    	}
		}
				
		return score * 4;
	}
	
	/*******************************************************************************************
	 * 
	 * @param first
	 * @param second
	 *******************************************************************************************/
	public void matchObjects(List<SemanticNetworkObj> first, List<SemanticNetworkObj> second) {
		Map<String, HashMap<String, Integer>> objectMatchMap = new HashMap<String, HashMap<String, Integer>>();
		
		int firstFillerCount = first.size();
		int secondFillerCount = second.size();
		
		if(firstFillerCount == 1 && secondFillerCount == 1)  {
			second.get(0).setName(first.get(0).getName());
		}
		else {
			int deleted = firstFillerCount - secondFillerCount > 0 ? firstFillerCount - secondFillerCount : 0;
			int added = secondFillerCount - firstFillerCount > 0 ? secondFillerCount - firstFillerCount : 0;
			
			//Step through each object in the first figure and compare it's attributes to
	    	//those in the second figure
	    	for(SemanticNetworkObj firstObj : first) {
	
	    		List<SemanticNetworkAttribute> firstAttrs = firstObj.getAttributes();
	    		
	    		HashMap<String, Integer> objectSimularityMap = new HashMap<String, Integer>();
	    		
	    		for(SemanticNetworkObj secondObj : second) {
	    			
	    			List<SemanticNetworkAttribute> secondAttrs = secondObj.getAttributes();
	    			
	    			int simularityScore = 0;
	    			
	    			//Test if they were added/deleted the same
	    			if(secondObj.getExistsNew() == firstObj.getExistsNew()) {
	    				simularityScore += 10;
	    			}
	    			if(secondObj.getExistsOld() == firstObj.getExistsOld()) {
	    				simularityScore += 10;
	    			}
	    			
	    			for(SemanticNetworkAttribute firstAttr : firstAttrs) {
	    				
	    				String firstValueNew = firstAttr.getNewVal();
	    				String firstValueOld = firstAttr.getOldVal();
	    				
	    				for(SemanticNetworkAttribute secondAttr : secondAttrs) {
	    					
	    					String secondValueNew = secondAttr.getNewVal();
	    					String secondValueOld = secondAttr.getOldVal();
	    					
	    					if(firstAttr.getName().equals(secondAttr.getName())) {
	    						if(secondValueOld.equals(firstValueOld) || secondValueNew.equals(firstValueNew)) {
	    							switch (firstAttr.getName()) {
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
	    			objectSimularityMap.put(secondObj.getName(), Integer.valueOf(simularityScore));
	    			
	    			objectMatchMap.put(firstObj.getName(), objectSimularityMap);
	    		}
	    	}
			
	    	HashMap<String, String> bestMatchesMap = findBestMatches(objectMatchMap, added, deleted);
	    	
	    	//BestMatchesMap - {E=B, C=A}  First  - [[name=C, attributes=[], existsNew=false, existsOld=true], 
	    	//										[name=E, attributes=[], existsNew=true, existsOld=false]]
	    	for(String matchName : bestMatchesMap.keySet()) {
	    		for(SemanticNetworkObj obj : first) {
	    			if(obj.getName().equals(matchName)) {
	    				obj.setName(bestMatchesMap.get(matchName));
	    			}
	    		}
	    		replaceFillerValues(first, bestMatchesMap.get(matchName), matchName);
	    	}
//	    	renameMatching(second, objectMatchMap);
		}
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
			replaceFillerValues(second, nameInSecond, nameInFirst);
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
			
	    	HashMap<String, String> bestMatchesMap = findBestMatches(objectMatchMap, added, deleted);
	    	
	    	for(String matchName : bestMatchesMap.keySet()) {
	    		String value = bestMatchesMap.get(matchName);
	    		HashMap<String, String> deletedMap = new HashMap<String, String>();
	    		deletedMap.put(DELETEDOBJECT, DELETEDOBJECT);
	    		HashMap<String, String> temp = value.equals(DELETEDOBJECT) ? deletedMap : second.get(bestMatchesMap.get(matchName));
				second.remove(bestMatchesMap.get(matchName));
				second.put(matchName, temp);
	    		replaceFillerValues(second, bestMatchesMap.get(matchName), matchName);
	    	}
//	    	renameMatching(second, objectMatchMap);
		}
	}
	
	
	/**********************************************************************************
	 * This method depends on pass-by-reference
	 * @param filler
	 * @param oldName
	 * @param newName
	 ***********************************************************************************/
	private void replaceFillerValues(
			HashMap<String, HashMap<String, String>> filler, String oldName, String newName) {
		for(String fillerName : filler.keySet()) {
			HashMap<String, String> temp = filler.get(fillerName);
			List<String> toReplace = new ArrayList<String>();
			if(temp != null) {
				for(String valueName : temp.keySet()) {
					String value = temp.get(valueName);
					if(value.equals(oldName)) {
						toReplace.add(valueName);
					}
				}
				for(String replace : toReplace) {
					temp.remove(replace);
					temp.put(replace, newName);
				}
			}
		}
	}
	
	/**********************************************************************************
	 * This method depends on pass-by-reference 
	 * @param filler
	 * @param oldName
	 * @param newName
	 ***********************************************************************************/
	private void replaceFillerValues(List<SemanticNetworkObj> network, String oldName, String newName) {
		
		for(SemanticNetworkObj obj : network) {
			for(SemanticNetworkAttribute attr : obj.getAttributes()) {
				String value = attr.getOldVal();
				String newValue = attr.getNewVal();
				if(value.equals(oldName)) {
					attr.setNewVal(newName);
				}
				if(newValue.equals(oldName)) {
					attr.setOldVal(newName);
				}
			}
		}
	}

	/***************************************************************************************************
	 * 
	 * @param objectMatchMap
	 * @return
	 ***************************************************************************************************/
	private HashMap<String, String> findBestMatches(Map<String, HashMap<String, Integer>> objectMatchMap, int added, int deleted) {
		HashMap<String, String> finalMatches = new HashMap<String, String>();
		HashMap<String, HashMap<String, Integer>> prevMatches = new HashMap<String, HashMap<String, Integer>>();
		
    	for(String objectName : objectMatchMap.keySet()) {
    		
    		String best = findNextBestMatch(objectName, objectMatchMap, prevMatches);
    		
    		//Capture that match
    		finalMatches.put(objectName, best);
    		HashMap<String, Integer> temp = new HashMap<String, Integer>();
    		temp.put(best, objectMatchMap.get(objectName).get(best));
    		prevMatches.put(objectName, temp);
    	}
    	Map<String, ArrayList<String>> duplicates = findDuplicates(finalMatches);
    	accountForAddDelete(duplicates, objectMatchMap, finalMatches, added, deleted);
    	
    	return finalMatches;
	}
	
	
	/*************************************************************************
	 * 
	 * @param duplicates
	 * @param objectMatchMap
	 * @param finalMatches
	 * @param added
	 * @param deleted
	 *************************************************************************/
	private void accountForAddDelete(Map<String, ArrayList<String>> duplicates,
			Map<String, HashMap<String, Integer>> objectMatchMap,
			HashMap<String, String> finalMatches, int added, int deleted) {
		
		//Store the similarity score info for each duplicate set
		//Key is the object which was mapped to multiple times, value is one of the mapped values
		//and it's similarity score
		Map<String, HashMap<String, Integer>> simMap = new HashMap<String, HashMap<String, Integer>>();
		
		//Get all of the values which have been mapped TO by multiple different objects
		for(String multiple : duplicates.keySet()) {
			
			//Get each of the objects which have mapped to "multiple"
			List<String> competition = duplicates.get(multiple);
			
			//Get the similarity score for each mapped object to "multiple"
			for(String comp : competition) {
				
				//Get the similarity score from the objectMatchMap
				int simScore = getSimularityScore(comp, multiple, objectMatchMap);
				
				//Store the competition and it's similarity score value
				HashMap<String, Integer> compMap = simMap.containsKey(multiple) ? simMap.get(multiple) : new HashMap<String, Integer>();
				compMap.put(comp, Integer.valueOf(simScore));
				simMap.put(multiple, compMap);
			}
			
		}
		
		adjustDuplicateMappings(simMap, finalMatches, added, deleted);
		
	}

	// TODO:klj
	private void adjustDuplicateMappings(
			Map<String, HashMap<String, Integer>> simMap,
			HashMap<String, String> finalMatches, int added, int deleted) {
		if(deleted > 0) {
			for(String multiple : simMap.keySet()) {
				HashMap<String, Integer> compMap = simMap.get(multiple);
				String worstMatch = "";
				int worstScore = Integer.MAX_VALUE;
				for(String compName : compMap.keySet()) {
					int currentScore = compMap.get(compName).intValue();
					if(currentScore < worstScore) {
						worstScore = currentScore;
						worstMatch = compName;
					}
				}
				finalMatches.remove(worstMatch);
				finalMatches.put(worstMatch, DELETEDOBJECT);
			}
		}
		
	}

	private int getSimularityScore(String comp, String multiple,
			Map<String, HashMap<String, Integer>> objectMatchMap) {
		int toReturn = 0;
		HashMap<String, Integer> scores = objectMatchMap.get(comp);
		toReturn = scores.get(multiple).intValue();
		return toReturn;	
	}

	private Map<String, ArrayList<String>> findDuplicates(HashMap<String, String> finalMatches) {
		Map<String, ArrayList<String>> duplicates = new HashMap<String, ArrayList<String>>();
		for(String match : finalMatches.keySet()) {
			String matchValue = finalMatches.get(match);
			if(getCount(finalMatches.values(), matchValue) > 1) {
					
				//matchValue is the object which has been mapped to multiple times
				ArrayList<String> tmp = duplicates.containsKey(matchValue) ? duplicates.get(matchValue) : new ArrayList<String>();
				
				//match is one of the multiple different objects which have been mapped to matchValue
				tmp.add(match);
				
				duplicates.put(matchValue, tmp);
			}
		}
		return duplicates;
	}
	
	private int getCount(Collection<String> array, String element) {
		int toReturn = 0;
		for(String str : array) {
			if(str.equals(element)) {
				toReturn++;
			}
		}
		return toReturn;
	}
	private String findNextBestMatch(String objectName, Map<String, HashMap<String, Integer>> objectMatchMap, HashMap<String, HashMap<String, Integer>> prevMatches) {
		
		int iter = 0;
		boolean bestFound = false;
		String best = "";
		int bestScore = 0;
		while(!bestFound) {
    		HashMap<String, Integer> possibleMatches = objectMatchMap.get(objectName);
    		
    		//Step through each object and find out what it's best match is
    		best = findXBestMatch(possibleMatches, iter);
    		    		
    		//Test to see if another object has already taken it with a better score...
    		String comparison = trueBest(best, Integer.valueOf(bestScore), possibleMatches.get(best), prevMatches);
    		String comparison2 = findTheTrue(iter, best, bestScore, objectMatchMap);
    		
    		if(comparison != null) {
    			if(iter > possibleMatches.size()) {
    				comparison2 = objectName;
    			}
    			if(comparison.equals(best) && comparison2.equals(objectName)) {
    				bestFound = true;
    			}
    		}else {
    			if(comparison2.equals(objectName)) {
    				bestFound = true;
    			}
    		}
    		iter += 1;
		}
		HashMap<String, Integer> tmp = new HashMap<String, Integer>();
		tmp.put(best, bestScore);
		prevMatches.put(objectName, tmp);
		return best;
	}
	
	
	private String findTheTrue(int iter, String best, int bestScore,
			Map<String, HashMap<String, Integer>> objectMatchMap) {
		List<HashMap<String, Integer>> rankedMatches = new ArrayList<HashMap<String, Integer>>();
		int calcBest = 0;
		String calcBestName = "";
		for(String objectName : objectMatchMap.keySet()) {
			HashMap<String, Integer> matches = objectMatchMap.get(objectName);
			for(String matchName : matches.keySet()) {
				Integer score = matches.get(matchName);
				if(matchName.endsWith(best) ) {
					//if(calcBest < score.intValue()) {
						calcBest = score.intValue();
						calcBestName = objectName;
						addToRankedArray(calcBest, calcBestName, rankedMatches);
					//}
				}
			}
		}
		String toReturn = "";
		try {
			toReturn = (String) rankedMatches.get(iter).keySet().toArray()[0];
		} catch(IndexOutOfBoundsException e) {
			int size = rankedMatches.size();
			Random rand = new Random();
			int last = rand.nextInt(size);
			toReturn = (String) rankedMatches.get(last).keySet().toArray()[0];
		}
		return toReturn;
	}

	private void addToRankedArray(int calcBest, String calcBestName,
			List<HashMap<String, Integer>> rankedMatches) {
		if(rankedMatches.size() > 1) {
			for(int i = 0; i < rankedMatches.size() -1; i++) {
				HashMap<String, Integer> rankedCurrent = rankedMatches.get(i);
				HashMap<String, Integer> rankedNext = rankedMatches.get(i+1);
				int current = 0;
				int next = 0;
				for(String rankedName : rankedCurrent.keySet()) {
					current = rankedCurrent.get(rankedName).intValue();
				}
				for(String rankedName : rankedNext.keySet()) {
					next = rankedNext.get(rankedName).intValue();
				}
				if(calcBest > current && calcBest <= next) {
					HashMap<String, Integer> tmp = new HashMap<String, Integer>();
					tmp.put(calcBestName, Integer.valueOf(calcBest));
					rankedMatches.add(i, tmp);
				}
			}
		}else if(rankedMatches.size() == 1) {
			HashMap<String, Integer> tmp = new HashMap<String, Integer>();
			tmp.put(calcBestName, Integer.valueOf(calcBest));
			HashMap<String, Integer> added = rankedMatches.get(0);
			int addedValue = 0;
			for(String addedName : added.keySet()) {
				addedValue = added.get(addedName).intValue();
			}
			if(addedValue >= calcBest) {
				rankedMatches.add(1, tmp);
			}else {
				rankedMatches.add(0, tmp);
			}
		}
		else {
			HashMap<String, Integer> tmp = new HashMap<String, Integer>();
			tmp.put(calcBestName, Integer.valueOf(calcBest));
			rankedMatches.add(tmp);
		}
		
	}

	private String trueBest(String best, Integer bestScore,
			Integer score, HashMap<String, HashMap<String, Integer>> prevMatches) {
		
		for(String objName : prevMatches.keySet()) {
			HashMap<String, Integer> currentMatch = prevMatches.get(objName);
			for(String currentBest : currentMatch.keySet()) {
				if(currentBest.equals(best) && score.intValue() <= bestScore.intValue()) {
					return currentBest;
				}else if(currentBest.equals(best) && score.intValue() > bestScore.intValue()) {
					return best;
				}
			}
		}
		
		return null;
	}

	
	private String findXBestMatch(HashMap<String, Integer> possibleMatches, int x) {
		List<HashMap<String, Integer>> rankedMatches = new ArrayList<HashMap<String, Integer>>();
		for(String possible : possibleMatches.keySet()) {
			Integer score = possibleMatches.get(possible);
			if(rankedMatches.size() > 1) {
				for(int i = 0; i < rankedMatches.size() -1; i++) {
					HashMap<String, Integer> rankedCurrent = rankedMatches.get(i);
					HashMap<String, Integer> rankedNext = rankedMatches.get(i+1);
					int current = 0;
					int next = 0;
					for(String rankedName : rankedCurrent.keySet()) {
						current = rankedCurrent.get(rankedName).intValue();
					}
					for(String rankedName : rankedNext.keySet()) {
						next = rankedNext.get(rankedName).intValue();
					}
					if(score > current && score <= next) {
						HashMap<String, Integer> tmp = new HashMap<String, Integer>();
						tmp.put(possible, Integer.valueOf(score));
						rankedMatches.add(i, tmp);
					}
				}
			}else if(rankedMatches.size() == 1) {
				HashMap<String, Integer> tmp = new HashMap<String, Integer>();
				tmp.put(possible, Integer.valueOf(score));
				HashMap<String, Integer> added = rankedMatches.get(0);
				int addedValue = 0;
				for(String addedName : added.keySet()) {
					addedValue = added.get(addedName).intValue();
				}
				if(addedValue >= score) {
					rankedMatches.add(1, tmp);
				}else {
					rankedMatches.add(0, tmp);
				}
			}
			else {
				HashMap<String, Integer> tmp = new HashMap<String, Integer>();
				tmp.put(possible, Integer.valueOf(score));
				rankedMatches.add(tmp);
			}
		}
		String toReturn = "";
		try {
			toReturn = (String) rankedMatches.get(x).keySet().toArray()[0];
		} catch(IndexOutOfBoundsException e) {
			int size = rankedMatches.size();
			Random rand = new Random();
			int last = rand.nextInt(size);
			toReturn = (String) rankedMatches.get(last).keySet().toArray()[0];
		}
		return toReturn;
		
	}
		
	 /****************************************************************
     * Helper method used to see if the differences (Semantic Network)
     * between a set of two figures is similar/the same as the differences 
     * (Semantic Network) that exist between a second set of two figures.
     * For example the Semantic Network of figures A & B is the same
     * as the Semantic Network for figures C & 1.
     * 
     * @param baseline - The Semantic Network between the 
     * 					 objects in Figure A and Figure B
     * @param test - The Semantic Network between the objects in
     * 				 objects in Figure C and possible answer "i"
     * @return - integer: The score given to the possible answer where
     * 			          the higher the score the closer the Semantic Networks
     * 					  between A&B/C&i are considered to be 
     ******************************************************************/
    public int compareDifference(List<SemanticNetworkObj> baseline, List<SemanticNetworkObj> test)  {
		
		//TODO: Map the objects to one another    	
    	matchObjects(test, baseline);
		
    	int toReturn = 0;
    	List<String> alreadyTested = new ArrayList<String>();
    	//Start off with the simple test if the amount of changes is the same
    	if(baseline.size() == test.size())  {
    		
    		//Next start comparing each of the objects and seeing if the differences
    		//between the starting figure and the resulting figure are similar for both sets 
    		//of figures
    		for(int i = 0; i < baseline.size(); i++) {
    			SemanticNetworkObj baseObj = baseline.get(i);
    			SemanticNetworkObj testObj = getObjectByName(baseObj.getName(), test);
    			alreadyTested.add(baseObj.getName());
    			if(testObj != null) {
    				toReturn += scoreDifference(baseObj, testObj);
    			}else {
    				//The object doesn't exist in the test figure
    				toReturn -= 1;
    			}
    		}
    		
    		for(int i = 0; i < test.size(); i++) {
    			SemanticNetworkObj testObj = test.get(i);
    			if(!alreadyTested.contains(testObj.getName())) {
    				alreadyTested.add(testObj.getName());
	    			SemanticNetworkObj baseObj = getObjectByName(testObj.getName(), baseline);
	    			if(baseObj != null) {
	    				toReturn += scoreDifference(baseObj, testObj);
	    			}else {
	    				//The object doesn't exist in the baseline figure
	    				toReturn -= 1;
	    			}
    			}
    		}
    	}else {
    		toReturn = -1;
    	}
    	
    	return toReturn;
    }
    /**********************************************************
     * Compare the Semantic Networks of the two Figure Sets
     * and determine how similar the differences between them
     * are.
     * 
     * @param baseObj - Semantic Network for the question prompt
     * @param testObj - Semantic Network for the possible answer
     * 
     * @return int - the weighted score for how similar the
     * 				  two argument networks are.
     **********************************************************/
    private int scoreDifference(SemanticNetworkObj baseObj, SemanticNetworkObj testObj) {
    	int toReturn = 0;
    	//Test the object consistency between figures for both sets
		toReturn += baseObj.getExistsNew() == testObj.getExistsNew() ? 1 : -1;
		toReturn += baseObj.getExistsOld() == testObj.getExistsOld() ? 1 : -1;
	
		//Now start comparing attributes, by first acquiring the list of them
		List<SemanticNetworkAttribute> baseAttrs = baseObj.getList();
		List<SemanticNetworkAttribute> testAttrs = testObj.getList();
		
		//Now the fun part....Start comparing the changes between each different attribute
		//Start by keeping track of the attributes tested so far
		List<String> testedAttr = new ArrayList<String>();
		
		//Next see if there are the same amount attribute changes for each set
		if(baseAttrs.size() == testAttrs.size()) {
			toReturn += 1;
		}else {
			toReturn -= 1;
		}
		
		//Step through each of the attributes in the figures A&B.
		toReturn += scoreObjAttributes(baseAttrs, testAttrs, testedAttr);
		
		//Step through each of the attributes in the figures C&i.
		toReturn += scoreObjAttributes(testAttrs, baseAttrs, testedAttr);
			
		// Test object fill similarities
		toReturn += scoreObjectFill(testAttrs, baseAttrs);
		
    	return toReturn;
    }
    
    /**************************************************************************
     * A helper method to see how similar attributes with multiple values 
     * are to one another.  This method tests how many of the values the
     * two attribute value lists share and returns the score. 
     * 
     * @param base - SemanticNetworkAttribute from the Question Example 
     * 				 Semantic Network
     * @param test - SemanticNetworkAttribute from the Question Prompt 
     * 				 Semantic Network
     * 
     * @return int - similarity score for the two attributes 
     ***************************************************************************/
    private int scoreMultipleValueAttr(SemanticNetworkAttribute base, SemanticNetworkAttribute test) {
    	int toReturn = 0;
    	//If the attribute has multiple values, split and count
		String[] baseNew = base.getNewVal().split(",");
		String[] baseOld = base.getOldVal().split(",");
		String[] testNew = test.getNewVal().split(",");
		String[] testOld = test.getOldVal().split(",");
		
		//Determine which list is longer for the new and old value of the attribute
    	String[] longerNew = baseNew.length > testNew.length ? baseNew : testNew;
    	String[] longerOld = baseOld.length > testOld.length ? baseOld : testOld;
    	ArrayList<String> shorterNew = baseNew.length > testNew.length ?  new ArrayList<String>(Arrays.asList(testNew)) :  new ArrayList<String>(Arrays.asList(baseNew));
    	ArrayList<String> shorterOld = baseOld.length > testOld.length ? new ArrayList<String>(Arrays.asList(testOld)) : new ArrayList<String>(Arrays.asList(baseOld));
    	
    	
    	//Step through each value in the longer list and see if 
    	//it exists in the shorter list
    	for(String next : longerNew) {
    		if(shorterNew.contains(next)) {
    			toReturn += 5;
    		}
    	}
    	for(String next : longerOld) {
    		if(shorterOld.contains(next)) {
    			toReturn += 5;
    		}
    	}
    	
    	return toReturn;
    	
    }
    /**********************************************************************************
     * More of a "Custom" helper method, this function is responsible for the specific
     * task of testing it the objects are filled according to the same pattern.
     * For example if the first Semantic Network increased it's fill by 50%, so 
     * should the answer network.
     * 
     * @param testAttrs - List of SemanticNetowrkAttributes from the object in the 
     * 					  possible answer Semantic Network
     * @param testAttrs - List of SemanticNetowrkAttributes from the object in the 
     * 					  question example Semantic Network
     * @return score - weight int score given to the simularity that exists between
     * 					the two objects
     ************************************************************************************/
    private int scoreObjectFill(List<SemanticNetworkAttribute> testAttrs, List<SemanticNetworkAttribute> baseAttrs) {
    	
    	//Local Storage Variables
    	int toReturn = 0;
    	double baseChange = 0.0;
    	double testChange = 0.0;
    	
    	//Get the fill attributes for both of the objects
    	SemanticNetworkAttribute baseFill = getAttrByName(baseAttrs, "fill");
    	SemanticNetworkAttribute testFill = getAttrByName(testAttrs, "fill");
    	
    	//If the object doesn't have fill, the result will be null so we need a "fake" attribute
    	testFill = testFill == null ? new SemanticNetworkAttribute("fill", "no", "no", false, 0) : testFill;
    	baseFill = baseFill == null ?  new SemanticNetworkAttribute("fill", "no", "no", false, 0) : baseFill;
    	
    	//If the attribute has multiple values, split and count
		String[] baseNewSplit = baseFill.getNewVal().split(",");
		String[] baseOldSplit = baseFill.getOldVal().split(",");
		int baseOldChange = getFillCount(baseOldSplit);
		int baseNewChange = getFillCount(baseNewSplit);
		
		//Check to see how the example semantic network object's fill attribute changed
		if(baseNewChange == 0)  {
			baseChange = baseOldChange;
		}else if(baseOldChange == 0) {
			baseChange = baseNewChange;
		}else {
			baseChange = ((double) (baseNewChange / baseOldChange));
			if(baseChange > 1) {
				baseChange -= 1;
			}
			baseChange = baseChange * 100;
		}
		
		//If the attribute has multiple values, split and count
		String[] testNewSplit = testFill.getNewVal().split(",");
		String[] testOldSplit = testFill.getOldVal().split(",");
		int testOldChange = getFillCount(testOldSplit);
		int testNewChange = getFillCount(testNewSplit);
		
		//Check to see how the possible answer semantic network object's fill attribute changed
		if(testNewChange == 0)  {
			testChange = testOldChange;
		}else if(testOldChange == 0) {
			testChange = testNewChange;
		}else {
			testChange = ((double) testNewChange / testOldChange);
			if(testChange > 1) {
				testChange -= 1;
			}
			testChange = testChange * 100;
		}
		
		
		//Compare the changes between the fill attribute of the objects 
		//within the two semantic networks
		if(baseChange == testChange) {
			toReturn += 5;
		}
		if(baseOldChange == testOldChange) {
			toReturn += 1;
		}
		if(baseNewChange == testNewChange) {
			toReturn += 1;
		}
    	    	
    	return toReturn;
    	
    }
    
    /*************************************************************************
     * Based on the string values given, determine how much of the object
     * is filled in.
     * 
     * @param values - String[]: value of the "fill" attribute
     * 
     * @return int: about in percent
     *************************************************************************/
    private int getFillCount(String[] values)  {
    	int toReturn = 0;
    	for(String value : values) {
    		if(value.contains("half")) {
    			toReturn += 50;
    		}else if(value.equals("no")) {
    			toReturn = 0;
    			break;
    		}else if(value.equals("yes")) {
    			toReturn = 5;
    			break;
    		}else if(value.contains("right") || value.contains("left")){
    			toReturn += 2;
    		}
    	}
    	return toReturn;
    }
    /***********************************************************
     * This method serves as the main workhorse of the 
     * comparing function.  It is responsible for checking if the
     * calculated difference between attributes is the same for the
     * example network as it is for the possible answer network.
     * 
     * @param baseAttrs - List<SemanticNetworkAttribute>: the attribute
     * 					  differences found in the example network
     * 					  of figures.
     * @param testAttrs - List<SemanticNetworkAttribute>: the attribute
     * 					  differences found in the prompt/answer network
     * 					  of figures.
     * 
     * @param testedAttr - Attributes previously tested for the 
     * 					   current object
     * 
     * @return int - The "likeness" score given to the two networks
     * 				 of attributes.
     ***************************************************************/
    private int scoreObjAttributes(List<SemanticNetworkAttribute> baseAttrs, List<SemanticNetworkAttribute> testAttrs, List<String> testedAttr) {
    			
    	int toReturn = 0;
    	//Step through each of the attributes in the figures C&i.
		for(int q = 0; q < testAttrs.size(); q++) {
			try {
				
				//Get the next attribute from the possible answer difference set
				SemanticNetworkAttribute testAttrFT = testAttrs.get(q);
				
				//Only test this attribute if we haven't already done so to this point
				if(!testedAttr.contains(testAttrFT.getName())) { 
					
					//Get the same attribute in the difference set for Figures A&B
					SemanticNetworkAttribute baseAttrFT = getAttrByName(baseAttrs, testAttrFT.getName());
					
					//Add this to the list of tested attributes to avoid double testing
					testedAttr.add(testAttrFT.getName());
					
					//If the attribute doesn't exist for both objects in each difference
					//set, dock points
					if(testAttrFT == null || baseAttrFT == null) {
						toReturn -= 1;
					}
					//Check to see if the change between attributes is the same for
					//each set of two figures
					else {
						
						//Test if both Semantic Networks have the attribute changing value
						if(testAttrFT.getChange() == baseAttrFT.getChange()) {
							toReturn += 5;
						}
						

						
						//Test the similarities between attributes with multiple values
						toReturn += scoreMultipleValueAttr(baseAttrFT, testAttrFT);
					}
				}
				
			//If the objects have a different amount of attributes, dock points
			}catch(IndexOutOfBoundsException ex2) {
				toReturn -=1;
			}
		}
		
		return toReturn;
    }
    /*********************************************************************
     * Helper method to get a SemanticNetwork based on its name 
     * from a List of Objects
     * 
     * @param name - String: Name of the Object
     * @param objs - List<SemanticNetwork>: 	List of objects to search
     * 
     * @return- SemanticNetwork: value is null if the network is not found.
     ***********************************************************************/
    private SemanticNetworkObj getObjectByName(String name, List<SemanticNetworkObj> objs) {
    	SemanticNetworkObj toReturn = null;
    	for(SemanticNetworkObj obj : objs) {
    		if(obj.getName().equals(name)) {
    			toReturn = obj;
    			break;
    		}
    	}
    	return toReturn;
    	
    }
    /**********************************************************
     * Helper method used to get the attribute with the
     *  name provided in the provided list of attributes.
     *  
     * @param attrs - List of attributes to search
     * @param name - The name of the attribute to find
     * @return - SemanticNetworkAttribute: null if not found
     **********************************************************/
    private SemanticNetworkAttribute getAttrByName(List<SemanticNetworkAttribute> attrs, String name) {
    	
    	//Object to return after search completes 
    	SemanticNetworkAttribute toReturn = null;
    	
    	//Step through the provided list until the attribute object requested is found
    	for(SemanticNetworkAttribute rra : attrs) {
    		if(rra.getName() != null && rra.getName().equals(name))  {
    			toReturn = rra;
    			break;
    		}
    	}
    	return toReturn;
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
    public List<SemanticNetworkObj> calculateDifference(RavensFigure figureA, RavensFigure figureB) {
    	
    	//List of differences to return
    	List<SemanticNetworkObj> toReturn = new ArrayList<SemanticNetworkObj>();
    	
    	//Break the object and it's attributes down into an iterable object where the
    	//key is object name, and the value is a map of the object's attribute names and
    	//the corresponding attribute values
    	HashMap<String, HashMap<String, String>> figureAValues = getValuesMap(figureA);
    	HashMap<String, HashMap<String, String>> figureBValues = getValuesMap(figureB);
    	
    	//Step through each object in the first figure and compare it's attributes to
    	//those in the second figure
    	for(String objectName : figureAValues.keySet()) {
    		
    		//Get the attributes and their values for this object in each figure
    		HashMap<String, String> firstFigObjs = figureAValues.get(objectName);
    		HashMap<String, String> secondFigObjs = figureBValues.get(objectName);
    		
    		//See if the second figure has the object found to exist in the first figure
    		if(secondFigObjs != null) {
    			
    			//Create a Result object to hold the difference between the two objects
	    		SemanticNetworkObj resultObj = new SemanticNetworkObj();
	    		
	    		//Set what we know so far, that it exists in both and it's name
	    		resultObj.setExistsNew(true);
	    		resultObj.setExistsOld(true);
	    		resultObj.setName(objectName);
	    		
	    		//Step through each attribute the object in the first figure has and
	    		//compare the attribute values to the same object in the second figure
	    		for(String attrName : firstFigObjs.keySet()) {
	    			
	    			//Create a Result Attribute object to hold the differences found
	    			SemanticNetworkAttribute rra = new SemanticNetworkAttribute();
	    			
	    			//Set the name of the attribute
	    			rra.setName(attrName);
	    			
	    			//Capture the value this attribute has in the first figure
	    			rra.setOldVal(firstFigObjs.get(attrName));
	    			
	    			//Set the initial value of the change between the two figures
	    			boolean change = false;
	    			
	    			//Check to see if the object in the second figure has the same attribute
	    			if(secondFigObjs.get(attrName) != null) {
	    				
	    				//Capture the value the attribute has in the second figure
		    			rra.setNewVal(secondFigObjs.get(attrName));
		    			
		    			//Calculate the change in the attribute from the first figure to the second
		    			change = rra.getOldVal().equals(rra.getNewVal());
		    			
		    			//Safely Calculate the percentage the attribute change (If applicable)
		    			try {
		    				if(Integer.valueOf(rra.getNewVal()) != 0 && Integer.valueOf(rra.getOldVal()) !=0 ) {
		    					rra.setPercent(Integer.valueOf(rra.getOldVal())/Integer.valueOf(rra.getNewVal()));
		    				}
		    			}catch(NumberFormatException e) {
		    				//nothing to see here... just playing it safe
		    			}catch(ArithmeticException e) {
		    				//Ok, it is strange if we get here... try it the other way? (This is dangerous) 
		    				rra.setPercent(Integer.valueOf(rra.getNewVal())/Integer.valueOf(rra.getOldVal()));
		    			}
	    			}else {
	    				
	    				//Attribute does not exist in the new figure
	    				rra.setNewVal("DNE");
	    			}
	    			rra.setChange(change);
	    			resultObj.addAttr(rra);
	    		}
	    		
	    		//Step through the attributes for this object in the second
	    		//figure to see if it has attributes the object in the first
	    		//figure does not have
	    		for(String oldAttrName : secondFigObjs.keySet()) {
	    			
	    			//Check to see if the attribute has already been captured
	    			if(resultObj.checkAttrExists(oldAttrName) == null) {
	    				
	    				//Create a "dummy"entry to keep track of this new attribute
	    				SemanticNetworkAttribute oldrra = new SemanticNetworkAttribute();
	    				oldrra.setName(oldAttrName);
	    				oldrra.setOldVal("DNE");
	    				oldrra.setNewVal(secondFigObjs.get(oldAttrName));
	    				oldrra.setChange(false);
	    				resultObj.addAttr(oldrra);
	    			}
	    		}
	    		
	    		//Add the dummy object to the list
	    		toReturn.add(resultObj);
	    		
	    	//If the object doesn't exist, then create a dummy object
	    	//to represent an object which is in the first figure, but
	    	//does not exist in the second
	    	}else {
	    		SemanticNetworkObj obj = new SemanticNetworkObj();
	    			obj.setName(objectName);
	    			obj.setExistsNew(false);
	    			obj.setExistsOld(true);
	    			toReturn.add(obj);
	    	}
    	}
    	
    	//Step through the objects in the second figure to see if
		//any of those objects do not appear in the first figure
    	for(String objectNameOld : figureBValues.keySet()) {
    		
    		//See if the object in the second figure is also in the first
    		HashMap<String, String> figureAObjVals = figureAValues.get(objectNameOld);
    		
    		//If the object in the second is not in the first, create a dummy object
    		if(figureAObjVals == null) {
    			
    			//Create the dummy object and it to the list
	    		SemanticNetworkObj resultObj = new SemanticNetworkObj();
	    		resultObj.setExistsNew(true);
	    		resultObj.setExistsOld(false);
	    		resultObj.setName(objectNameOld);
	    		toReturn.add(resultObj);
    		}
    	}
    	
    	return toReturn;
	}

   
}
