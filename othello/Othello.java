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
		for (int i=0; i < 20; i++) {
			OthelloState startState = new OthelloState();
			startState.setStandardStartState();/*
			startState.setValueOnBoards((byte)2, (byte)2, (byte)3);
			startState.setValueOnBoards((byte)2, (byte)3, (byte)3);
			startState.setValueOnBoards((byte)2, (byte)4, (byte)3);
			startState.setValueOnBoards((byte)3, (byte)2, (byte)2);
			startState.setValueOnBoards((byte)3, (byte)3, (byte)2);
			startState.setValueOnBoards((byte)3, (byte)4, (byte)3);
			startState.generateMoveBoards();*/
			//System.out.println("Start State:\n"+startState);
			Game g = new Game(new MTDDecider(true, 30, 64, false, false),
					 new MTDDecider(false, 30, 64, true, false),
					 startState);
			g.run();
			if (g.getStatus() == Status.PlayerOneWon) p1++;
			if (g.getStatus() == Status.PlayerTwoWon) p2++;
		}
		System.out.println("P1: "+p1+" P2: "+p2);
	}
		
	
}
