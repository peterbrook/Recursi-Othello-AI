package gamePlayer.algorithms;

import gamePlayer.Action;


public class ActionValuePair implements Comparable<ActionValuePair> {

	Action action;
	ActionValuePair principalVariation;
	int value, previousValue;
	
	public ActionValuePair(Action a, int v) {
		this.action = a;
		this.value = v;
		this.previousValue = 0;
		this.principalVariation = null;
	}

	@Override
	public int compareTo(ActionValuePair other) {
		return Float.compare(this.value, other.value);
	}
	
	@Override
	public String toString() {
		return "(Action : " + action + " Value; " + value + ")";
	}
	
}
