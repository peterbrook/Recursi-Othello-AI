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
import java.util.List;
import java.util.Map;

public class MTDDecider2 implements Decider {

	// These are the different types of values which could be stored in our
	// transposition table
	public enum EntryType {
		EXACT_VALUE, LOWERBOUND, UPPERBOUND;
	}
	
	private class SearchNode {
		int f_minus, f_plus, depth;
		State gameState;
		boolean maxnode;
		Action bestAction;
		EntryType type;
		public List<ActionValuePair> actions;
		
		public SearchNode() {
			f_minus = LOSE;
			f_plus = WIN;
		}
	}
	
	private class SearchStatistics {
		int searchDepth;
		int timeSpent;
		int nodesEvaluated;
	}
	
	private static final int WIN = 100000;
	private static final int LOSE = -100000;
	private static final boolean DEBUG = false;
	private static final int TABLE_SIZE = 500000;
	/*
	 *  This threshold determines whether a move should look to the previous 
	 *  move or 2 moves back for an estimate of its bound. When iterative 
	 *  deepening is less than this it will look two back.
	 */
	private static final int EVEN_ODD_THRESHOLD = 100;
	private boolean maximizer;
	private int searchTime;
	private int maxdepth;
	private long startTimeMillis;
	
	private List<SearchStatistics> statsList;
	private int leafNodes;
	private int checkedNodes;
	
	// A transposition table for caching repeated states. Critical since
	// iterative deepening hits states over and over
	private Map<State, SearchNode> transpositionTable;
	private int loopsHit;
	private int actionsChecked;
	private int cacheHits;
	public boolean REFINE_ACTIONS = false;
	public boolean INACCURATE_PRUNING = false;
	public boolean INTERMOVE_TRANSPOSITIONS = false;
	public MTDDecider2(boolean maximizer, int timeinmsec, int maxdepth) {
		this.maximizer = maximizer;
		this.searchTime = timeinmsec;
		this.maxdepth = maxdepth;
		this.statsList = new ArrayList<SearchStatistics>();
	}
	
	public void initializeTranspositionTable() {
		transpositionTable = new TranspositionTable<State, SearchNode>(TABLE_SIZE);
	}
	
	@Override
	public Action decide(State state) {
		if (!INTERMOVE_TRANSPOSITIONS)
			transpositionTable = new HashMap<State, SearchNode>(TABLE_SIZE);
		leafNodes = 0; checkedNodes = 0; loopsHit =0; actionsChecked = 0; cacheHits = 0;
		startTimeMillis = System.currentTimeMillis();
		
		try {
			if (DEBUG) GraphVizPrinter.setState(state);
			Action a = iterative_deepening(state);
			if (DEBUG) GraphVizPrinter.setDecision(a.applyTo(state));
			if (DEBUG) GraphVizPrinter.printGraphToFile();
			return a;
		} catch (InvalidActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private Action iterative_deepening(State root) throws InvalidActionException {
		
		ActionValuePair evenGuess = new ActionValuePair(null, 0);
		ActionValuePair oddGuess = new ActionValuePair(null, 0);
		SearchNode rootNode = new SearchNode();
		rootNode.gameState = root;
		rootNode.maxnode = this.maximizer;
		int d; boolean early_exit = false;
		for (d = 1; d < this.maxdepth; d++) {
			rootNode.depth = d;
			try {
				if (d < EVEN_ODD_THRESHOLD) {
					if (d % 2 == 0)
						evenGuess = MTD(rootNode, evenGuess, d);
					else
						oddGuess = MTD(rootNode, oddGuess, d);
				} else {
					evenGuess = MTD(rootNode, evenGuess, d);
				}
			} catch (IllegalStateException ex) {
				early_exit = true;
				break;
			}
			
			
			System.out.printf("%2.2f",0.001*(System.currentTimeMillis() - startTimeMillis));
			System.out.print(": " + d + " " + (((d % 2 == 1) && d < EVEN_ODD_THRESHOLD) ? oddGuess: evenGuess));
			System.out.println();
			
			if (times_up()) {
				early_exit = true;
				break;
			}
		}
		String ps = saveSearchStatistics(d);
		System.out.print(ps);
		
		if (!early_exit) {
		} else {
			System.out.println("Exited early from search at depth "+d);
		}
		// If we didn't exit early, then the current value of d is one higher than our actual search depth
		// If we did exit early, then the search depth we want to use is the previous one
		d--;
		Action move;
		if (d % 2 == 1 && d < EVEN_ODD_THRESHOLD)
			move = oddGuess.action;
		else
			move = evenGuess.action;
		System.out.println("Moving to "+move);
		return move;
	}

	private String saveSearchStatistics(int d) {
		SearchStatistics s = new SearchStatistics();
		s.nodesEvaluated = leafNodes;
		s.timeSpent = (int) (System.currentTimeMillis() - startTimeMillis);
		s.searchDepth = d;
		statsList.add(s);
		
		double nodesPerSec = (1000.0*s.nodesEvaluated) / s.timeSpent;
		double EBF = Math.log(s.nodesEvaluated)/Math.log(s.searchDepth);
		double searchEfficiency = (1.0 * leafNodes) / checkedNodes;
		double avgActionschecked = (1.0 * actionsChecked) / loopsHit;
		
		return String.format("NPS:%.2f EBF:%.2f eff:%.2f aact:%.2f ch:%d\n", nodesPerSec, EBF, searchEfficiency, avgActionschecked, cacheHits);
	}
	
	private boolean times_up() {
		boolean timesUp = (System.currentTimeMillis() - startTimeMillis) > searchTime;
		return timesUp;
	}

	private ActionValuePair MTD(SearchNode n, ActionValuePair firstGuess, int depth) throws InvalidActionException {
		ActionValuePair g = firstGuess;
		int f_plus = WIN; int f_minus = LOSE;
		int iter = 0;
		do {
			int gamma;
			if (g.value == f_minus) {
				gamma = g.value + 1;
			} else {
				gamma = g.value;
			}
			g = MT(n,gamma, depth, iter);
			if (g.value < gamma) {
				f_plus = g.value;
			} else {
				f_minus = g.value;
			}
			iter++;
		} while (f_plus != f_minus);
		return g;
	}
	
	
	/**
	 * Performs a Memory-enhanced Test search from the given search state stored in the search node, going down the given depth 
	 * @param n The search node to start searching from
	 * @param gamma The null window upper bound
	 * @param depth The depth to search
	 * @param iter unused, except for debugging
	 * @return an ActionValuePair which stores the minimax value of this node as well as the best action to take from it
	 * @throws InvalidActionException
	 * @throws IllegalStateException If we run out of time during the search
	 */
	private ActionValuePair MT(SearchNode n, int gamma, int depth, int iter) throws InvalidActionException, IllegalStateException {
		checkedNodes++;
		if (DEBUG) GraphVizPrinter.setState(n.gameState);
		
		/**
		 * If we are not at a low depth (have at least more recursive calls
		 * below us) then we are called infrequently enough that we can afford
		 * to check if we are out of time
		 */
		if (depth > 4) {
			if (times_up())
				throw new IllegalStateException("Out of time");
		}
		
		populateFromTable(n);
		
		ActionValuePair bestAction = new ActionValuePair(null, 0);
		List<ActionValuePair> actions = null;
		// If we are at a leaf
		if (depth == 0 || n.gameState.getStatus() != Status.Ongoing) {
			leafNodes++;
			// and we don't have the node stored in the table
			if (n.f_minus == LOSE && n.f_plus == WIN) {
				bestAction.value = (int) n.gameState.heuristic();
			} else // If we stored the lower bound 
				if (n.f_plus == WIN) {
				bestAction.value = n.f_minus;
			} else {// We stored the upper bound 
				bestAction.value = n.f_plus;
			}
			if (DEBUG) GraphVizPrinter.setRelation(n.gameState, bestAction.value, n.gameState.getParentState(), iter);
		} else {// We are an interior node
			if (n.actions != null)
				actions = n.actions;
			else
				actions = buildAVPList(n.gameState.getActions(), n.bestAction);
			
			// Partial move ordering. Check value up to depth D-3 and order by that
			int[] depthsToSearch;
			if (false/*depth > 4*/) {
				depthsToSearch = new int[2];
				depthsToSearch[0] = depth - 2; // TODO: this should be easily adjustable
				depthsToSearch[1] = depth;
			} else {
				depthsToSearch = new int[1];
				depthsToSearch[0] = depth;
			}

			bestAction.value = n.maxnode ? LOSE : WIN;
			
			// Do our shorter depth search first to order moves on the longer search
			for (int i = 0; i < depthsToSearch.length; i++) {
				// Now find the best action to take from here
				loopsHit++;
				
				if (!INACCURATE_PRUNING) {
					// Start off with the worst value possible
					bestAction.value = n.maxnode ? LOSE : WIN;
				}
				for (ActionValuePair avp: actions) {
					actionsChecked++;
					// If our current best action is above our (beta) cutoff, stop
					if (n.maxnode) { if (bestAction.value >= gamma) break; }
					else { if (bestAction.value < gamma) break; }
					
					State childState = avp.action.applyTo(n.gameState);
					SearchNode c = new SearchNode();
					c.gameState = childState;
					c.maxnode = !n.maxnode;
					c.depth = depthsToSearch[i];
					populateFromTable(c);
					
					int currentBound = n.maxnode ? c.f_plus : c.f_minus; 
					
					ActionValuePair bestChildAction;
					// If we are above (or below) our current bound, look up a more accurate value.
					if ((n.maxnode && currentBound >= gamma) || (!n.maxnode && currentBound < gamma)) {
						bestChildAction = MT(c, gamma, depthsToSearch[i] - 1, iter);
					} else { // Just return our existing bound
						bestChildAction = new ActionValuePair(null, currentBound);
					}
					// And push the value back into the actions list for ordering
					avp.value = bestChildAction.value;
					// And update our best observed action
					if (n.maxnode)
						bestAction = maxAVP(bestAction, avp.action, bestChildAction);
					else
						bestAction = minAVP(bestAction, avp.action, bestChildAction);
				}
				/*
				if (depthsToSearch.length > 1 && i==0) {
				*/
					if (n.maxnode) {
						// Sort the actions to order moves on the deeper search
						Collections.sort(actions, Collections.reverseOrder());
					} else {
						// Sort the actions to order moves on the deeper search
						Collections.sort(actions);
					}
					if (REFINE_ACTIONS)
						refineActionsList(actions, n.maxnode);
				/*}*/
			}
			if (DEBUG) GraphVizPrinter.setRelation(n.gameState, bestAction.value, n.gameState.getParentState(), gamma-1, gamma, iter);
		}
		
		if (bestAction.value >= gamma) {
			n.f_minus = bestAction.value;
			n.type = EntryType.LOWERBOUND;
		} else {
			n.f_plus = bestAction.value;
			n.type = EntryType.UPPERBOUND;
		}
		n.bestAction = bestAction.action;
		n.depth = depth;
		n.actions = actions;
		saveToTable(n);

		return bestAction;
	}

	
	private void refineActionsList(List<ActionValuePair> actions, boolean maxnode) {
		// remove the worst 30% of the moves
		
		int numActions = actions.size();
		// this will truncate so that we don't remove an action if we only have one
		int numToRemove = (int) (numActions * 0.8);
		//System.out.println("NumToRemove: "+ numToRemove);
		for (; numToRemove > 0; numToRemove--) {
			actions.remove(actions.size()-1);
		}
	}

	private void populateFromTable(SearchNode n) {
		SearchNode node = transpositionTable.get(n.gameState);
		if (node == null) return;
		cacheHits++;
		if (node.depth >= n.depth) {
			
			switch (node.type) {
			case LOWERBOUND:
				n.f_minus = node.f_minus;
				break;
			case UPPERBOUND: 
				n.f_plus = node.f_plus;
				break;
			default:
				n.f_plus = WIN; n.f_minus = LOSE;
			}
		}
		if (n.maxnode == node.maxnode)
			n.actions = node.actions;
	}

	private void saveToTable(SearchNode n) {
		if (transpositionTable.size() <  TABLE_SIZE  && n.depth >= 2) {
			transpositionTable.put(n.gameState, n);
		}
	}
	
	private List<ActionValuePair> buildAVPList(List<Action> actions) {
		List<ActionValuePair> res = new ArrayList<ActionValuePair>();

		for (Action a : actions) {
			ActionValuePair p = new ActionValuePair(a, 0);
			res.add(p);
		}

		return res;
	}
	
	private List<ActionValuePair> buildAVPList(List<Action> actions, Action best) {
		List<ActionValuePair> res = new ArrayList<ActionValuePair>();
		
		if (best != null) {
			ActionValuePair p = new ActionValuePair(best, 0);
			res.add(p);
			for (Action a: actions) {
				if (!a.equals(best))
					res.add(new ActionValuePair(a, 0));
			}
		} else {
			for (Action a: actions) {
				res.add(new ActionValuePair(a, 0));
			}
		}
		return res;
	}
	
	private ActionValuePair maxAVP(ActionValuePair currentBestAction, Action transitionAction, ActionValuePair bestChildAction) {
		if (currentBestAction.action == null || currentBestAction.value < bestChildAction.value) {
			// update the current best action
			currentBestAction.principalVariation = bestChildAction;
			currentBestAction.value = bestChildAction.value;
			currentBestAction.action = transitionAction;
		}
		return currentBestAction;
	}
	
	private ActionValuePair minAVP(ActionValuePair currentBestAction, Action transitionAction, ActionValuePair bestChildAction) {
		if (currentBestAction.action == null || currentBestAction.value > bestChildAction.value) {
			// update the current best action
			currentBestAction.principalVariation = bestChildAction;
			currentBestAction.value = bestChildAction.value;
			currentBestAction.action = transitionAction;
		}
		return currentBestAction;
	}

	public void printSearchStatistics() {
		double avgNodesPerSec = 0; double avgEBF = 0;
		for (SearchStatistics s: statsList) {
			double nodesPerSec = (1000.0*s.nodesEvaluated) / s.timeSpent;
			avgNodesPerSec += nodesPerSec;
			double EBF = Math.log(s.nodesEvaluated)/Math.log(s.searchDepth);
			avgEBF += EBF;
		}
		
		avgNodesPerSec /= statsList.size();
		avgEBF /= statsList.size();
		
		System.out.printf("Average Nodes Per Second:%.2f\n", avgNodesPerSec);
		System.out.printf("Average EBF:%.2f\n", avgEBF);
	}
}
