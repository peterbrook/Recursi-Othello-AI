package othello;

import gamePlayer.Game;
import gamePlayer.MiniMaxDecider;

public class Othello {
	
	public static void main(String[] args) {
		OthelloState startState = new OthelloState();
		startState.setStandardStartState();
		new Game(new MiniMaxDecider(true, 2),
				 new MiniMaxDecider(false, 2),
				 startState).run();
	}
	
}