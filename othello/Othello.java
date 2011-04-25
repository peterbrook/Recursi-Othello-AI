package othello;

import gamePlayer.Game;
import gamePlayer.MiniMaxDecider;
import gamePlayer.algorithms.NegaMaxDecider;

public class Othello {
	
	public static void main(String[] args) {
		OthelloState startState = new OthelloState();
		startState.setStandardStartState();
		new Game(new NegaMaxDecider(true, 10),
				 new NegaMaxDecider(false, 10),
				 startState).run();
	}
	
}
