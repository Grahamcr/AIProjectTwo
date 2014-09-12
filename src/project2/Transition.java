package project2;

import java.util.HashMap;
import java.util.Map;

public class Transition {

	Map<String, Change> changeMap;
	String objectId;
	
	public Transition(String objectId) {
		
		changeMap = new HashMap<String, Change>();
		this.objectId = objectId;
	}
	
	public void setChange(String name, String type, String oldValue, String newValue) {
		changeMap.put(name, new Change(name, type, oldValue, newValue));
	}
	
	
	public boolean checkAttrExists(String attrName) {
		boolean toReturn = false;
		for(String name : changeMap.keySet()) {
			if(attrName.equals(name)) {
				toReturn = true;
				break;
			}
		}
		return toReturn;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	
	public boolean isObjectDeleted(String objectId) {
		boolean toReturn = false;
		Change change = null;
		if((change = changeMap.get(objectId)) != null) {
			if(change.getChange().equals(Change.OBJDELETED)) {
				toReturn = true;
			}
		}
		return toReturn;
	}

	public Change getValue(String name) {
		return changeMap.get(name);
	}
	
	public Map<String, Change> getChanges() {
		return this.changeMap;
	}
}
