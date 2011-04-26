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

public class ABWithMemoryDecider implements Decider {

	private static final int MIN_VAL = -999;
	private static final int MAX_VAL = 999;
	
	class SearchNode {
		float lowerbound = MIN_VAL, upperbound = MAX_VAL;
	}

	private static final boolean ITERATIVE_DEEPENING = false;

	private static final boolean DEBUG = true;
	
	private HashMap<State, SearchNode> transpositionTable;
	private boolean maximizer;
	
	// Time we have to compute a move in seconds
	private int searchTime; 
	
	// Time we have left to search
	private long startTimeMillis;
	
	private int maxSearchDepth;
	
	public ABWithMemoryDecider(boolean maximizer, int searchTimeSec, int maxSearchDepth) {
		this.maximizer = maximizer;
		searchTime = searchTimeSec;
		this.maxSearchDepth = maxSearchDepth;
	}
	
	@Override
	public Action decide(State state) {
		startTimeMillis = System.currentTimeMillis();
		transpositionTable = new HashMap<State, SearchNode>();
		
		
		List<ActionValuePair> actions = buildAVPList(state.getActions());
		
		
		int search_depth = ITERATIVE_DEEPENING ? maxSearchDepth : 1;
		
		for (int d=1; d <= search_depth; d++) {
			
			// Easier to see than min and max int
			float alpha = MIN_VAL;
			float beta = MAX_VAL;
			
			if (DEBUG) GraphVizPrinter.setState(state);
			for (ActionValuePair a: actions) {
				State n;
				try {
					n = a.action.applyTo(state);
					int depth = ITERATIVE_DEEPENING ?  d : maxSearchDepth;
					float mmv = AlphaBetaWithMemory(n, alpha, beta, depth-1, !maximizer);
					if (DEBUG) GraphVizPrinter.setRelation(n, mmv, state);
					a.value = mmv;
					
					/*
					// Update A-B bounds
					if (maximizer)
						alpha = Math.max(alpha, mmv);
					else
						beta = Math.min(beta, mmv);
						*/
					
				} catch (InvalidActionException e) {
					e.printStackTrace();
				}
			}
			
			Collections.sort(actions, Collections.reverseOrder());
			System.out.println("Best Action: "+actions.get(0));
			if (DEBUG) {
				if (ITERATIVE_DEEPENING) GraphVizPrinter.printGraphToFileWDeepening(d);
				else GraphVizPrinter.printGraphToFile();
			}
			if (times_up()) {
				System.out.println("ABWM got to depth "+d);
				break;
			}
		}

		return getRandomBestAction(actions);
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
	 * Checks to see if the maximum search time for this move has elapsed
	 * @return true if we need to stop searching, false otherwise
	 */
	private boolean times_up() {
		return (System.currentTimeMillis() - startTimeMillis) > 1000*searchTime;
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

	private float AlphaBetaWithMemory(State n, float alpha, float beta, int d, boolean maximizer) throws InvalidActionException {
		float g;
		GraphVizPrinter.setState(n);
		
		if (transpositionTable.containsKey(n)) {
			if (DEBUG) GraphVizPrinter.setCached(n);
			SearchNode node = transpositionTable.get(n);
			// commented out a-b pruning for testing
			//if (node.lowerbound >= beta) return node.lowerbound;
			//if (node.upperbound <= alpha) return node.upperbound;
			alpha = alpha > node.lowerbound ? alpha : node.lowerbound;
			beta = beta < node.upperbound ? beta : node.upperbound;
		}
		
		if (d==0 || n.getStatus() != Status.Ongoing) {
			g = n.heuristic();
		} else {
			List<Action> actions = n.getActions();

			if (maximizer) {
				g = MIN_VAL;
				float a = alpha;
	
				for (int i=0; i < actions.size()/* && g < beta*/; i++) {
					State child = actions.get(i).applyTo(n);
					float abwm = AlphaBetaWithMemory(child, a, beta, d-1, !maximizer);
					if (DEBUG) GraphVizPrinter.setRelation(child, abwm, n);
					g = g > abwm ? g : abwm;
					a = g > a ? g : a;
				}
			} else {
				g = MAX_VAL; float b = beta;
				
				for (int i=0; i < actions.size()/* && g > alpha */; i++) {
					State child = actions.get(i).applyTo(n);
					float abwm = AlphaBetaWithMemory(child, alpha, b, d-1, !maximizer);
					if (DEBUG) GraphVizPrinter.setRelation(child, abwm, n);
					
					g = g < abwm ? g : abwm;
					b = g < b ? g : b;
				}
			}
		}
		SearchNode node = new SearchNode();
		if (g <= alpha) node.upperbound = g;
		if (g > alpha && g < beta) {
			node.lowerbound = node.upperbound = g;
		}
		if (g >= beta) node.lowerbound = g;
		
		//transpositionTable.put(n, node);
		
		return g;
	}
	
}
