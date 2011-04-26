package gamePlayer.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import gamePlayer.Action;
import gamePlayer.Decider;
import gamePlayer.GraphVizPrinter;
import gamePlayer.InvalidActionException;
import gamePlayer.State;
import gamePlayer.State.Status;

public class NegaMaxDecider implements Decider {

	private class SearchNode {
		short depth;
		float h;
		float alpha,beta;
	}
	
	// Are we maximizing or minimizing?
	private boolean maximize;
	// The depth to which we should analyze the search space
	private int depth;
	private int maxdepth;
	private long leafsHit;
	private Map<State,SearchNode> stateCache;
	private int cacheHits;
	private int cacheMisses;
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

	/**
	 * Returns a random action from among the best actions in the given list
	 * @param actions The actions to examine
	 * @return The selected action
	 */
	private Action getRandomBestAction(List<ActionValuePair> actions) {
		List<Action> bestActions = new LinkedList<Action>();
		
		float bestV = actions.get(0).value;
		for (ActionValuePair avp: actions) {
			if (avp.value != bestV) break;
			
			bestActions.add(avp.action);
		}
		
		Collections.shuffle(bestActions);
		
		return bestActions.get(0);
	}
	
	/**
	 * Helper to create a list of ActionValuePairs with value of 0 from a list of actions 
	 * @param actions The actions to convert
	 * @return A list of actionvaluepairs
	 */
	private List<ActionValuePair> buildAVPList(List<Action> actions) {
		List<ActionValuePair> res = new ArrayList<ActionValuePair>();
		
		for (Action a: actions) {
			ActionValuePair p = new ActionValuePair(a, 0);
			res.add(p);
		}
		
		return res;
	}
	
	public Action decide(State state) {
		leafsHit = 0; cacheHits = 0; cacheMisses=0;
		stateCache = new HashMap<State, SearchNode>(1000000);
		if (DEBUG)
			GraphVizPrinter.setState(state);
		// Choose randomly between equally good options
		float value = maximize ? Float.NEGATIVE_INFINITY
				: Float.POSITIVE_INFINITY;
		// Iterate!
		int flag = maximize ? 1 : -1;
		float alpha = Float.NEGATIVE_INFINITY;
		float beta = Float.POSITIVE_INFINITY;
		List<ActionValuePair> actions = buildAVPList(state.getActions());
		for (ActionValuePair a : actions) {
			try {
				// Algorithm!
				State newState = a.action.applyTo(state);
				float newValue = -NegaMax(newState, 1, -beta, -alpha, -flag);
				if (DEBUG)
					GraphVizPrinter.setRelation(newState, newValue, state);
				a.value = newValue;
				if (maximize)
					alpha = Math.max(alpha, newValue);
				else
					beta = Math.min(beta, newValue);
			} catch (InvalidActionException e) {
				throw new RuntimeException("Invalid action!");
			}
		}
		
		Collections.sort(actions, Collections.reverseOrder());
		
		// Graph?
		try {
			GraphVizPrinter.setDecision(actions.get(0).action.applyTo(state));
		} catch (InvalidActionException e) {
			throw new RuntimeException("Invalid action!");
		}
		if (DEBUG)
			GraphVizPrinter.printGraphToFile();
		System.out.println("Hit "+leafsHit+" leaves. C-Misses:"+cacheMisses+" C-hits:"+cacheHits);
		return getRandomBestAction(actions);
	}

	private void indentedPrint(int depth, String s) {
		/*for (int i=0; i < depth; i++) {
			System.out.print("\t");
		}
		System.out.println(s);*/
	}
	private float NegaMax(State s, int depth, float alpha, float beta, int color)
			throws InvalidActionException {
		if (stateCache.containsKey(s)) {
			SearchNode n = stateCache.get(s);
			cacheHits++;
			if (n.depth >= depth) {
				return color * n.h;
			}
		}
		cacheMisses++;
		if (DEBUG)
			GraphVizPrinter.setState(s);
		if (s.getStatus() != Status.Ongoing || depth == this.depth) {
			indentedPrint(depth, "Fast returning at leaf. H:"+s.heuristic()+" c:"+color+" ret:"+color*s.heuristic());
			leafsHit++;
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
		SearchNode sn = new SearchNode();
		sn.h = alpha;
		sn.alpha = alpha;
		sn.beta = beta;
		sn.depth = (short)depth;
		//stateCache.put(s, sn);
		return alpha;
	}

}
