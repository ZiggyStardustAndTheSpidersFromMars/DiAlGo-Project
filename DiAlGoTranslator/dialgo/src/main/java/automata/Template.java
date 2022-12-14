package automata;

import java.util.LinkedList;
import java.util.List;
/**
 * automaton template object for uppaal automata network.
 * @author Torben Friedrich Goerner
 */
public class Template {

	/**
	 * name of the template
	 */
	private String name;
	/**
	 * local declaration
	 */
	private String declaration;
	/**
	 * parameters of the template
	 */
	private String parametes;
	private List<Location> locations;
	private List<Transition> transitions;
	
	
	public Template() {
		this.name = "";
		this.declaration = "";
		this.locations = new LinkedList<>();
		this.transitions = new LinkedList<>();
	}
	
	@Override
	public String toString() {
		return "\r\n	<template>\r\n" +
				"		<name x=\"5\" y=\"5\">" + this.name + "</name>\r\n" +
				"		<parameter>" + this.parametes + "</parameter>\r\n" +
				"		<declaration>" + 
				this.declaration.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
				.replaceAll("\"", "&quot;").replaceAll("'", "&apos;") + 
				"	</declaration>\r\n" +
				"		" + this.locations.stream().map(e -> e.toString()).reduce("", String::concat) + "		\r\n" +
				"		<init ref=\"" + getInit().getId() + "\"/>\r\n" +
				"		" + this.transitions.stream().map(e -> e.toString()).reduce("", String::concat) + "		\r\n" +
				"	</template>\r\n";
	}
	
	public void addLocation(Location location) {
		this.locations.add(location);
	}
	
	public void addTransition(Transition transition) {
		this.transitions.add(transition);
	}
	
	public Location getInit() {
		for(Location location : locations) {
			if(location.isInitial()) {
				return location;
			}
		}
		return null; //ERROR CASE
	}
	
	public Location getLocationById(String id) {
		for(Location location : locations) {
			if(location.getId().equals(id)) {
				return location;
			}
		}
		return null; //ERROR CASE
	}
	
	public String getHighestLocationId() {
		Location lTemp = null;
		int hTemp = 0;
		for(Location location : locations) {
			if(Integer.parseInt(location.getId().replaceAll("[\\D]", "")) >= hTemp) {
				lTemp = location;
				hTemp = Integer.parseInt(location.getId().replaceAll("[\\D]", ""));
			}
		}
		return lTemp.getId();
	}
	
	//-----------Getter and Setter----------
	
	public String getName() {return this.name;}
	public void setName(String name) {this.name = name;}
	
	public String getDeclaration() {return this.declaration;}
	public void setDeclaration(String declaration) {this.declaration = declaration;}
	
	public List<Location> getLocations() {return this.locations;}
	public void setLocations(List<Location> locations) {this.locations = locations;}
	
	public List<Transition> getTransitions() {return this.transitions;}
	public void setTransitions(List<Transition> transitions) {this.transitions = transitions;}

	public String getParametes() {return parametes;}
	public void setParametes(String parametes) {this.parametes = parametes;}

}
