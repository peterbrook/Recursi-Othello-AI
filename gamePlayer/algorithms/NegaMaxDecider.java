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
	private static final boolean DEBUG_PRINT = false;

	/**
	 * Initialize this MiniMaxDecider.
	 * 
	 * @param maximize
	 *            Are we maximizing or minimizing on this turn? True if the
	 *            former.
	 * @param depth
	 *            The depth to which we should analyze the search space.
	 */
	public NegaMaxDecider(boolean maximize, int depth) {
		this.maximize = maximize;
		this.depth = depth;
	}

	public Action decide(State state) {
		if (DEBUG)
			GraphVizPrinter.setState(state);
		// Choose randomly between equally good options
		float value = maximize ? Float.NEGATIVE_INFINITY
				: Float.POSITIVE_INFINITY;
		List<Action> bestActions = new ArrayList<Action>();
		// Iterate!
		int flag = maximize ? 1 : -1;
		float alpha = Float.NEGATIVE_INFINITY;
		float beta = Float.POSITIVE_INFINITY;
		for (Action action : state.getActions()) {
			try {
				// Algorithm!
				State newState = action.applyTo(state);
				indentedPrint(0, "Root: Before calling NegaMax alpha:"+alpha+" beta:"+beta);
				float newValue = -NegaMax(newState, 1, -beta, -alpha, -flag);
				indentedPrint(0, "Root: got value:"+newValue);
				if (DEBUG)
					GraphVizPrinter.setRelation(newState, newValue, state);

				alpha = Math.max(alpha, newValue);
				indentedPrint(0, "Root: new alpha:"+alpha);
				// Better candidates?
				if (flag * newValue > flag * value) {
					value = newValue;
					bestActions.clear();
				}
				// Add it to the list of candidates?
				if (flag * newValue >= flag * value)
					bestActions.add(action);
			} catch (InvalidActionException e) {
				throw new RuntimeException("Invalid action!");
			}
		}
		// Pick one of the best randomly
		// Collections.shuffle(bestActions);
		// Graph?
		try {
			GraphVizPrinter.setDecision(bestActions.get(0).applyTo(state));
		} catch (InvalidActionException e) {
			throw new RuntimeException("Invalid action!");
		}
		if (DEBUG)
			GraphVizPrinter.printGraphToFile();
		return bestActions.get(0);
	}

	private void indentedPrint(int depth, String s) {
		if (DEBUG_PRINT) {
			for (int i=0; i < depth; i++) {
				System.out.print("\t");
			}
			System.out.println(s);
		}
	}
	private float NegaMax(State s, int depth, float alpha, float beta, int color)
			throws InvalidActionException {
		if (DEBUG)
			GraphVizPrinter.setState(s);
		if (s.getStatus() != Status.Ongoing || depth == this.depth) {
			indentedPrint(depth, "Fast returning at leaf. H:"+s.heuristic()+" c:"+color+" ret:"+color*s.heuristic());
			return color * s.heuristic();
		}
		indentedPrint(depth, "Starting child node examination. alpha: "+alpha+" beta:"+beta);
		for (Action a : s.getActions()) {
			State childState = a.applyTo(s);
			indentedPrint(depth, "Examining child from action:"+a);
			float nmValue = -NegaMax(childState, depth + 1, -beta, -alpha,
					-color);
			indentedPrint(depth, "Got value:"+nmValue);
			if (DEBUG)
				GraphVizPrinter.setRelation(childState, nmValue, s);
			indentedPrint(depth, "Old alpha:"+alpha);
			alpha = Math.max(alpha, nmValue);
			indentedPrint(depth, "New alpha:"+alpha);
			if (alpha >= beta) {
				indentedPrint(depth, "A-B Pruned. Alpha:"+alpha+" Beta:"+beta);
				break;
			}
		}
		return alpha;
	}

}
