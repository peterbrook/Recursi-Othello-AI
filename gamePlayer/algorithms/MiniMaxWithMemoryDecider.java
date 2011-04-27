// NOTE: This is extra code, not a critical part of our Othello assignment
package gamePlayer.algorithms;

import gamePlayer.Action;
import gamePlayer.Decider;
import gamePlayer.GraphVizPrinter;
import gamePlayer.InvalidActionException;
import gamePlayer.State;
import gamePlayer.State.Status;
import gamePlayer.algorithms.ABWithMemoryDecider.SearchNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * This class represents an AI Decider that uses a MiniMax algorithm.
 * We use alpha-beta pruning, but besides that we're pretty vanilla.
 * @author Ashoat Tevosyan
 * @since Mon April 18 2011
 * @version CSE 473
 */
public class MiniMaxWithMemoryDecider implements Decider {
	
	private static final int MIN_VAL = -999;
	private static final int MAX_VAL = 999;
	
	private class SearchNode {
		float lowerbound = MIN_VAL, upperbound = MAX_VAL;
	}
	
	// Are we maximizing or minimizing?
	private boolean maximize;
	// The depth to which we should analyze the search space
	private int depth;
	// HashMap to avoid recalculating States
	private HashMap<State, SearchNode> computedStates;
	// Used to generate a graph of the search space for each turn in SVG format
	private static final boolean DEBUG = false;
	
	/**
	 * Initialize this MiniMaxDecider. 
	 * @param maximize Are we maximizing or minimizing on this turn? True if the former.
	 * @param depth    The depth to which we should analyze the search space.
	 */
	public MiniMaxWithMemoryDecider(boolean maximize, int depth) {
		this.maximize = maximize;
		this.depth = depth;
		computedStates = new HashMap<State, SearchNode>();
	}
	
	/**
	 * Decide which state to go into.
	 * We manually MiniMax the first layer so we can figure out which heuristic is from which Action.
	 * Also, we want to be able to choose randomly between equally good options.
	 * "I'm the decider, and I decide what is best." - George W. Bush
	 * @param state The start State for our search.
	 * @return The Action we are deciding to take.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Action decide(State state) {
		if (DEBUG) GraphVizPrinter.setState(state);
		// Choose randomly between equally good options
		float value = maximize ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
		List<Action> bestActions = new ArrayList<Action>();
		// Iterate!
		int flag = maximize ? 1 : -1;
		for (Action action : state.getActions()) {
			try {
				// Algorithm!
				State newState = action.applyTo(state);
				float newValue = this.miniMaxRecursor(newState,
						Float.NEGATIVE_INFINITY,
						Float.POSITIVE_INFINITY, 
						1,
						!this.maximize);
				if (DEBUG) GraphVizPrinter.setRelation(newState, newValue, state);
				// Better candidates?
				if (flag * newValue > flag * value) {
					value = newValue;
					bestActions.clear();
				}
				// Add it to the list of candidates?
				if (flag * newValue >= flag * value) bestActions.add(action);
			} catch (InvalidActionException e) {
				throw new RuntimeException("Invalid action!");
			}
		}
		// Pick one of the best randomly
		Collections.shuffle(bestActions);
		// Graph?
		try {
			GraphVizPrinter.setDecision(bestActions.get(0).applyTo(state));
		} catch (InvalidActionException e) {
			throw new RuntimeException("Invalid action!");
		}
		if (DEBUG) GraphVizPrinter.printGraphToFile();
		return bestActions.get(0);
	}
	
	/**
	 * The true implementation of the MiniMax algorithm!
	 * Thoroughly commented for your convenience.
	 * @param state    The State we are currently parsing.
	 * @param alpha    The alpha bound for alpha-beta pruning.
	 * @param beta     The beta bound for alpha-beta pruning.
	 * @param depth    The current depth we are at.
	 * @param maximize Are we maximizing? If not, we are minimizing.
	 * @return The best point count we can get on this branch of the state space to the specified depth.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public float miniMaxRecursor(State state, float alpha, float beta, int depth, boolean maximize) {
		// Specify us
		if (DEBUG) GraphVizPrinter.setState(state);
		// Has this state already been computed?
		if (computedStates.containsKey(state)) {
			if (DEBUG) GraphVizPrinter.setCached(state);
			SearchNode node = computedStates.get(state);

			if (node.lowerbound >= beta) return node.lowerbound;
			if (node.upperbound <= alpha) return node.upperbound;
			alpha = alpha > node.lowerbound ? alpha : node.lowerbound;
			beta = beta < node.upperbound ? beta : node.upperbound;
		}
		// Is this state done?
		if (state.getStatus() != Status.Ongoing) {
			float value = state.heuristic();
			// Store so we don't have to compute it again.
			SearchNode node = new SearchNode();
			if (value <= alpha) node.upperbound = value;
			if (value > alpha && value < beta) {
				node.lowerbound = node.upperbound = value;
			}
			if (value >= beta) node.lowerbound = value;
			computedStates.put(state, node);
			return value;
		}
		// Have we reached the end of the line?
		if (depth == this.depth) return state.heuristic();
		// If not, recurse further. Identify the best actions to take.
		float value = maximize ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
		int flag = maximize ? 1 : -1;
		List<Action> test = state.getActions();
		for (Action action : test) {
			// Check it. Is it better? If so, keep it.
			try {
				State childState = action.applyTo(state);
				float newValue = this.miniMaxRecursor(childState, alpha, beta, depth + 1, !maximize);
				if (DEBUG) GraphVizPrinter.setRelation(childState, newValue, state);
				if (flag * newValue > flag * value) value = newValue;
			} catch (InvalidActionException e) {
				throw new RuntimeException("Invalid action!");
			}
			// Pruning!
			float pruner = maximize ? beta : alpha;
			if (flag * value > flag * pruner) break;
			// Updating alpha/beta values.
			if (maximize && value > alpha) alpha = value;
			else if (!maximize && value < beta) beta = value;
		}
		// Store so we don't have to compute it again.
		SearchNode node = new SearchNode();
		if (value <= alpha) node.upperbound = value;
		if (value > alpha && value < beta) {
			node.lowerbound = node.upperbound = value;
		}
		if (value >= beta) node.lowerbound = value;
		
		computedStates.put(state, node);

		return value;
	}
	
}