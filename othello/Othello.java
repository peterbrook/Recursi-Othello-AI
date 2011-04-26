package othello;

import gamePlayer.Game;
import gamePlayer.MiniMaxDecider;
import gamePlayer.algorithms.ABWithMemoryDecider;
import gamePlayer.algorithms.MTDDecider;
import gamePlayer.algorithms.MiniMaxWithMemoryDecider;
import gamePlayer.algorithms.NegaMaxDecider;

public class Othello {
	
	public static void main(String[] args) {
		OthelloState startState = new OthelloState();
		startState.setStandardStartState();
		new Game(new MTDDecider(true, 10, 20),
				 new NegaMaxDecider(false, 6),
				 startState).run();
	}
	
}
