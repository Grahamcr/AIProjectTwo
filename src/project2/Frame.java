package project2;

import java.util.ArrayList;
import java.util.List;

public class Frame {

	private List<FrameFiller> fillers;
	private String name;
	
	
	@Override
	public String toString() {
		return "Frame [" + fillers + ", name=" + name + "]";
	}

	public Frame(String name) {
		fillers = new ArrayList<FrameFiller>();
		this.name = name;
	}

	public Frame() {
		fillers = new ArrayList<FrameFiller>();
		this.name = name;
	}
	
	public void addFillers(List<FrameFiller> toAdd) {
		fillers.addAll(toAdd);		
	}
	
	public FrameFiller getFillerByName(String objectName) {
		FrameFiller toReturn = null;
		for(FrameFiller filler : fillers) {
			if(filler.getName().equals(objectName)) {
				toReturn = filler;
			}
		}
		return toReturn;
	}
	
	public List<FrameFiller> getFillers() {
		return fillers;
	}

	public void setFillers(List<FrameFiller> filler) {
		this.fillers = filler;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void remove(FrameFiller filler) {
		fillers.remove(filler);
	}


	
}
