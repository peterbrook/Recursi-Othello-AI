package gamePlayer.algorithms;

import gamePlayer.Action;


public class ActionValuePair implements Comparable<ActionValuePair> {

	Action action;
	float value;
	
	public ActionValuePair(Action a, int v) {
		this.action = a;
		this.value = v;
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
