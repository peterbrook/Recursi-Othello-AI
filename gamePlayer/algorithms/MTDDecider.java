package gamePlayer.algorithms;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import gamePlayer.Action;
import gamePlayer.Decider;
import gamePlayer.State;

public class MTDDecider implements Decider {
	
	private class SearchNode {
		float lowerbound;
		float upperbound;
	}
	
	private Map<State, SearchNode> transpositionTable;
	
	// Time we have left to compute this move
	//private 
	
	@Override
	public Action decide(State state) {
		transpositionTable = new HashMap<State, SearchNode>();
		
		return null;
	}
	
	private float iterative_deepening(State root) {
		float guess = 0;
		int maxdepth = 10;
		for (int d=0; d < maxdepth; d++) {
			guess = MTDF(root, guess, d);
			if (times_up()) break;
		}
		return guess;
	}
	
	private boolean times_up() {
		// TODO Auto-generated method stub
		return false;
	}

	private float MTDF(State root, float f, int depth) {
		float g = f;
		float beta;
		float upperbound = Float.POSITIVE_INFINITY;
		float lowerbound = Float.NEGATIVE_INFINITY;
		
		while (lowerbound < upperbound) {
			if (g == lowerbound) {
				beta = g+1;
			} else {
				beta = g;
			}
			g = AlphaBetaWithMemory(root, beta-1, beta, depth);
			if (g < beta) {
				upperbound = g;
			} else {
				lowerbound = g;
			}
		}
		
		return g;
	}

	private float AlphaBetaWithMemory(State root, float alpha, float beta, int depth) {
		// try to look up the state in the T-table
		return 0;
	}
}
