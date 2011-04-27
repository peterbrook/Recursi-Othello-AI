package othello;

import gamePlayer.Game;
import gamePlayer.State.Status;
import gamePlayer.algorithms.ABWithMemoryDecider;
import gamePlayer.algorithms.MTDDecider;
import gamePlayer.algorithms.MiniMaxDecider;
import gamePlayer.algorithms.MiniMaxWithMemoryDecider;
import gamePlayer.algorithms.NegaMaxDecider;

public class Othello {
	
	public static void main(String[] args) {
		int p1 = 0, p2=0;
		for (int i=0; i < 30; i++) {
			OthelloState startState = new OthelloState();
			startState.setStandardStartState();
			Game g = new Game(new MTDDecider(true, 30, 30, false),
					 new MTDDecider(false, 30, 20, true),
					 startState);
			g.run();
			if (g.getStatus() == Status.PlayerOneWon) p1++;
			if (g.getStatus() == Status.PlayerTwoWon) p2++;
		}
		System.out.println("P1: "+p1+" P2: "+p2);
	}
		
	
}
