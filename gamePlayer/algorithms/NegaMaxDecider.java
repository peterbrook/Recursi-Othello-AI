package gamePlayer.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gamePlayer.Action;
import gamePlayer.Decider;
import gamePlayer.GraphVizPrinter;
import gamePlayer.InvalidActionException;
import gamePlayer.State;
import gamePlayer.State.Status;

public class NegaMaxDecider implements Decider {

	// Are we maximizing or minimizing?
	private boolean maximize;
	// The depth to which we should analyze the search space
	private int depth;
	private int maxdepth;
	// Used to generate a graph of the search space for each turn in SVG format
	private static final boolean DEBUG = false;
	
	/**
	 * Initialize this MiniMaxDecider. 
	 * @param maximize Are we maximizing or minimizing on this turn? True if the former.
	 * @param depth    The depth to which we should analyze the search space.
	 */
	public NegaMaxDecider(boolean maximize, int depth) {
		this.maximize = maximize;
		this.depth = depth;
	}
	
	@Override
	public Action decide(State state) {
		if (DEBUG) GraphVizPrinter.setState(state);
		// Choose randomly between equally good options
		
		float value = maximize ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
		List<Action> bestActions = new ArrayList<Action>();
		Action chosenAction = null;
		// Iterate!
		int flag = maximize ? 1 : -1;
		
		for (Action action : state.getActions()) {
			try {
				// Algorithm!
				State newState = action.applyTo(state);
				
				float newValue = -NegaMax(newState, 1,
						Float.NEGATIVE_INFINITY,
						Float.POSITIVE_INFINITY, 
						flag);
				
				if (DEBUG) GraphVizPrinter.setRelation(newState, newValue, state);
				
				if ((flag == 1 && newValue >= value) || (flag == -1 && newValue <= value)) {
					value = newValue;
					chosenAction = action;
				}
				/*
				// Better candidates?
				if (flag * newValue > flag * value) {
					value = newValue;
					bestActions.clear();
				}
				// Add it to the list of candidates?
				if (flag * newValue >= flag * value) bestActions.add(action);
				*/
				
			} catch (InvalidActionException e) {
				throw new RuntimeException("Invalid action!");
			}
		}
		// Pick one of the best randomly
		//Collections.shuffle(bestActions);
		// Graph?
		try {
			//GraphVizPrinter.setDecision(bestActions.get(0).applyTo(state));
			GraphVizPrinter.setDecision(chosenAction.applyTo(state));
		} catch (InvalidActionException e) {
			throw new RuntimeException("Invalid action!");
		}
		if (DEBUG) GraphVizPrinter.printGraphToFile();
		//return bestActions.get(0);
		return chosenAction;
	}
	
	private float NegaMax(State s, int depth, float alpha, float beta, int color) throws InvalidActionException {
		if (DEBUG) GraphVizPrinter.setState(s);
		if (s.getStatus() != Status.Ongoing || 
				depth == this.depth) {
			return -color*s.heuristic();
		}
		
		for (Action a: s.getActions()) {
			State childState = a.applyTo(s);
			float nmValue = -NegaMax(childState, depth+1, -beta, -alpha, -color);
			if (DEBUG) GraphVizPrinter.setRelation(childState, nmValue, s);
			alpha = Math.max(alpha, nmValue);
			
			if (alpha >= beta) {
				break;
			}
		}
		return alpha;
	}

}
