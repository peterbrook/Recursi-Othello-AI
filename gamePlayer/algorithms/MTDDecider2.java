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
	private static final int TABLE_SIZE = 100000;
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
	
	public MTDDecider2(boolean maximizer, int timeinmsec, int maxdepth) {
		this.maximizer = maximizer;
		this.searchTime = timeinmsec;
		this.maxdepth = maxdepth;
		this.statsList = new ArrayList<SearchStatistics>();
	}
	
	@Override
	public Action decide(State state) {
		leafNodes = 0; checkedNodes = 0; loopsHit =0; actionsChecked = 0; cacheHits = 0;
		startTimeMillis = System.currentTimeMillis();
		transpositionTable = new HashMap<State, SearchNode>(TABLE_SIZE);
		try {
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
		int d;
		for (d = 1; d < this.maxdepth; d++) {
			rootNode.depth = d;
			try {
				if (d % 2 == 0)
					evenGuess = MTD(rootNode, evenGuess, d);
				else
					oddGuess = MTD(rootNode, oddGuess, d);
			} catch (IllegalStateException ex) {
				break;
			}
			
			
			System.out.printf("%2.2f",0.001*(System.currentTimeMillis() - startTimeMillis));
			System.out.print(": " + d + " " + (d % 2 == 0 ? evenGuess : oddGuess));
			System.out.println();
			
			if (times_up()) break;
		}
		String ps = saveSearchStatistics(d);
		System.out.print(ps);
		if (d % 2 == 0)
			return evenGuess.action;
		else
			return oddGuess.action;
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
		return (System.currentTimeMillis() - startTimeMillis) > searchTime;
	}

	private ActionValuePair MTD(SearchNode n, ActionValuePair firstGuess, int depth) throws InvalidActionException {
		ActionValuePair g = firstGuess;
		int f_plus = WIN; int f_minus = LOSE;
		
		do {
			int gamma;
			if (g.value == f_minus) {
				gamma = g.value + 1;
			} else {
				gamma = g.value;
			}
			g = MT(n,gamma, depth);
			if (g.value < gamma) {
				f_plus = g.value;
			} else {
				f_minus = g.value;
			}
		} while (f_plus != f_minus);
		return g;
	}
	
	@SuppressWarnings("unchecked")
	private ActionValuePair MT(SearchNode n, int gamma, int depth) throws InvalidActionException, IllegalStateException {
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
			if (DEBUG) GraphVizPrinter.setRelation(n.gameState, bestAction.value, n.gameState.getParentState());
		} else {// We are an interior node
			// Start off with the worst value possible
			bestAction.value = n.maxnode ? LOSE : WIN;
			List<ActionValuePair> actions = buildAVPList(n.gameState.getActions());//, n.bestAction);
			
			// Partial move ordering. Check value up to depth D-3 and order by that
			int[] depthsToSearch;
			if (depth > 4) {
				depthsToSearch = new int[2];
				depthsToSearch[0] = depth - 2; // TODO: this should be easily adjustable
				depthsToSearch[1] = depth;
			} else {
				depthsToSearch = new int[1];
				depthsToSearch[0] = depth;
			}
			
			// Do our shorter depth search first to order moves on the longer search
			for (int i = 0; i < depthsToSearch.length; i++) {
				// Now find the best action to take from here
				loopsHit++;
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
						bestChildAction = MT(c, gamma, depthsToSearch[i] - 1);
					} else { // Just return our existing bound
						bestChildAction = new ActionValuePair(null, currentBound);
					}
					// Set the action to match the action we used to get the child
					bestChildAction.action = avp.action;
					// And push the value back into the actions list for ordering
					avp.value = bestChildAction.value;
					// And update our best observed action
					if (n.maxnode)
						bestAction = maxAVP(bestAction, bestChildAction);
					else
						bestAction = minAVP(bestAction, bestChildAction);
				}
				
				if (depthsToSearch.length > 1 && i==0) {
					if (n.maxnode) {
						// Sort the actions to order moves on the deeper search
						Collections.sort(actions, Collections.reverseOrder());
					} else {
						// Sort the actions to order moves on the deeper search
						Collections.sort(actions);
					}
				}
			}
			if (DEBUG) GraphVizPrinter.setRelation(n.gameState, bestAction.value, n.gameState.getParentState(), gamma-1, gamma);
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
		saveToTable(n);

		return bestAction;
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
	}

	private void saveToTable(SearchNode n) {
		if (transpositionTable.size() <  TABLE_SIZE)
			transpositionTable.put(n.gameState, n);
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
	
	private ActionValuePair maxAVP(ActionValuePair l, ActionValuePair r) {
		if (l.value > r.value)
			return l;
		if (l.value < r.value)
			return r;
		if (l.action == null) return r;
		return l;
	}
	
	private ActionValuePair minAVP(ActionValuePair l, ActionValuePair r) {
		if (l.value < r.value)
			return l;
		if (l.value > r.value)
			return r;
		if (l.action == null) return r;
		return l;
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
