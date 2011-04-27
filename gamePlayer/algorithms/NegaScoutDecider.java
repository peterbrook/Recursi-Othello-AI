// NOTE: This is extra code, not a critical part of our Othello assignment
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

public class NegaScoutDecider implements Decider {

	// Are we maximizing or minimizing?
	private boolean maximize;
	// The depth to which we should analyze the search space
	private int depth;
	// HashMap to avoid recalculating States
	private Map<State, Float> computedStates;
	private int maxdepth;
	// Used to generate a graph of the search space for each turn in SVG format
	private static final boolean DEBUG = true;

	/**
	 * Initialize this MiniMaxDecider.
	 * 
	 * @param maximize
	 *            Are we maximizing or minimizing on this turn? True if the
	 *            former.
	 * @param depth
	 *            The depth to which we should analyze the search space.
	 */
	public NegaScoutDecider(boolean maximize, int depth) {
		this.maximize = maximize;
		this.depth = depth;
		computedStates = new HashMap<State, Float>();
	}

	@Override
	public Action decide(State state) {
		if (DEBUG)
			GraphVizPrinter.setState(state);
		// Choose randomly between equally good options
		float value = maximize ? Float.NEGATIVE_INFINITY
				: Float.POSITIVE_INFINITY;
		List<Action> bestActions = new ArrayList<Action>();
		// Iterate!
		int flag = maximize ? 1 : -1;
		for (Action action : state.getActions()) {
			try {
				// Algorithm!
				State newState = action.applyTo(state);
				float newValue = -NegaScout(newState, 1,
						Float.NEGATIVE_INFINITY,
						Float.POSITIVE_INFINITY);
				if (DEBUG)
					GraphVizPrinter.setRelation(newState, newValue, state);
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
		//Collections.shuffle(bestActions);
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

	private float NegaScout(State s, int depth, float alpha, float beta)
			throws InvalidActionException {
		// Specify us
		if (DEBUG) GraphVizPrinter.setState(s);

		if (s.getStatus() != Status.Ongoing || depth == this.depth) {
			return s.heuristic();
		}
		float a, b, t;
		int i;

		List<Action> actions = s.getActions();
		int w = actions.size();

		//a = alpha;
		b = beta;
		for (i = 0; i < w; i++) {
			State successor = actions.get(i).applyTo(s);
			a = -NegaScout(successor, depth + 1, -b, -alpha);
			if (alpha < a && a < beta && i > 0) {
				a = -NegaScout(successor, depth + 1, -beta, -alpha); /* re-search */
			}
			alpha = Math.max(alpha, a);
			
			if (DEBUG) GraphVizPrinter.setRelation(successor, a, s);
			
			if (alpha >= beta) {
				return alpha;
			}
			b = alpha + 1;
		}
		return alpha;
	}

}
