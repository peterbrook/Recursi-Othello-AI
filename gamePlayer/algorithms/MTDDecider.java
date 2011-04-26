package gamePlayer.algorithms;

import gamePlayer.Action;
import gamePlayer.Decider;
import gamePlayer.GraphVizPrinter;
import gamePlayer.InvalidActionException;
import gamePlayer.State;
import gamePlayer.State.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MTDDecider implements Decider {
	public enum EntryType {
		EXACT_VALUE, LOWERBOUND, UPPERBOUND;
	}
	private class SearchNode {	
		EntryType type;
		int value;
		int depth;
		//int color;
		
	}

	private static final int LOSE = -100000;
	private static final int WIN = 100000;
	
	private int maxdepth;

	private static final boolean DEBUG = false;
	
	// Time we have to compute a move in seconds
	private int searchTime; 
	
	// Time we have left to search
	private long startTimeMillis;

	private boolean maximizer;
	
	private Random rnd = new Random(101);

	private Map<State, SearchNode> transpositionTable;

	private int checkedNodes;

	public MTDDecider(boolean maximizer, int searchTimeSec, int maxdepth) {
		searchTime = searchTimeSec;
		this.maximizer = maximizer;
		this.maxdepth = maxdepth;
	}
	
	@Override
	public Action decide(State state) {
		startTimeMillis = System.currentTimeMillis();
		transpositionTable = new HashMap<State, SearchNode>(1000);
		return iterative_deepening(state);
	}
	
	private Action iterative_deepening(State root) {
		if (DEBUG) GraphVizPrinter.setState(root);
		List<ActionValuePair> actions = buildAVPList(root.getActions());
		checkedNodes = 0;
		int d;
		for (d=1; d < maxdepth; d++) {
			for (ActionValuePair a: actions) {
				State n;
				try {
					n = a.action.applyTo(root);
					if (DEBUG) GraphVizPrinter.setState(n);
					a.value = MTDF(n, (int) a.value, d);
					if (DEBUG) GraphVizPrinter.setRelation(n, a.value, root);
				} catch (InvalidActionException e) {
					e.printStackTrace();
				}
			}
			
			//TODO: do we need to toggle reverseOrder depending on whether we maximize?
			//if (maximizer)
				Collections.sort(actions, Collections.reverseOrder());
			/*else
				Collections.sort(actions);
				*/
			if (times_up()) {
				break;
			}
		}
		if (DEBUG) GraphVizPrinter.printGraphToFile();
		System.out.println("MTD got to depth "+d+" and checked "+checkedNodes+" nodes");
		return getRandomBestAction(actions);
	}
	
	private boolean times_up() {
		return (System.currentTimeMillis() - startTimeMillis) > 1000*searchTime;
	}

	private float MTDF(State root, int firstGuess, int depth) {
		int g = firstGuess;
		int beta;
		int upperbound = WIN;
		int lowerbound = LOSE;
		
		int flag = maximizer ? 1 : -1;
		
		while (lowerbound < upperbound) {
			if (g == lowerbound) {
				beta = g+1;
			} else {
				beta = g;
			}
			g = -AlphaBetaWithMemory(root, beta-1, beta, depth, -flag);
			if (g < beta) {
				upperbound = g;
			} else {
				lowerbound = g;
			}
		}
		
		return g;
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
	private int AlphaBetaWithMemory(State state, int alpha, int beta, int depth, int color) {
		// Note that we checked a new node
		checkedNodes++;
		// Specify us
		if (DEBUG) GraphVizPrinter.setState(state);
		// Has this state already been computed?
		SearchNode node = transpositionTable.get(state);
		// TODO: shoot myself. This code wasn't working because I had node.depth >= depth rather than >
		if (node != null && node.depth > depth) {
			if (DEBUG) GraphVizPrinter.setCached(state);
			/* this seems not needed if (node.color != color) node.value = -node.value; */
			switch (node.type) {
			case EXACT_VALUE:
				return node.value;
			case UPPERBOUND:
				if (node.value > alpha) alpha = node.value;
				break;
			case LOWERBOUND:
				if (node.value < beta) beta = node.value;
				break;
			}
		}
		// Is this state/our search done?
		if (depth == 0 || state.getStatus() != Status.Ongoing) {
			int value = color*Math.max(Math.min((int)state.heuristic(),WIN),LOSE);
			return saveAndReturnState(state, alpha, beta, depth, value, color);
		}
		
		int bestValue = LOSE;
		
		List<Action> test = state.getActions();
		for (Action action : test) {
			// Check it. Is it better? If so, keep it.
			int newValue;
			try {
				State childState = action.applyTo(state);
				newValue = -AlphaBetaWithMemory(childState, -beta, -alpha, depth - 1, -color);
				if (DEBUG) GraphVizPrinter.setRelation(childState, newValue, state);
			} catch (InvalidActionException e) {
				throw new RuntimeException("Invalid action!");
			}
			if (newValue > bestValue) bestValue = newValue;
			if (bestValue > alpha) alpha = bestValue;
			if (bestValue >= beta) break;
		}
		return saveAndReturnState(state, alpha, beta, depth, bestValue, color);
	}

	private int saveAndReturnState(State state, int alpha, int beta,
			int depth, int value, int color) {
		// Store so we don't have to compute it again.
		SearchNode saveNode = new SearchNode();
		if (value <= alpha) { 
			saveNode.type = EntryType.LOWERBOUND;
		} else if (value >= beta) {
			saveNode.type = EntryType.UPPERBOUND;
		} else {
			saveNode.type = EntryType.EXACT_VALUE;
		}
		
		saveNode.depth = depth;
		saveNode.value = value;
		//saveNode.color = -color;
		transpositionTable.put(state, saveNode);
		
		return value;
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
	
	/**
	 * Returns a random action from among the best actions in the given list
	 * NOTE: this assumes the list is already sorted with the best move first,
	 * and that the list is nonempty!
	 * @param actions The actions to examine
	 * @return The selected action
	 */
	private Action getRandomBestAction(List<ActionValuePair> actions) {
		List<Action> bestActions = new LinkedList<Action>();
		
		int bestV = (int) actions.get(0).value;
		for (ActionValuePair avp: actions) {
			if (avp.value != bestV) break;
			
			bestActions.add(avp.action);
		}
		
		Collections.shuffle(bestActions);
		if (bestV == LOSE) {
			System.out.println("I LOST :(");
		}
		return bestActions.get(0);
	}
}
