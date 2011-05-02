package othello.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import gamePlayer.Action;
import gamePlayer.Decider;
import gamePlayer.InvalidActionException;
import gamePlayer.State;
import gamePlayer.algorithms.MTDDecider;

import java.awt.Point;
import java.util.HashMap;

import org.junit.Test;

import othello.OthelloAction;
import othello.OthelloState;

public class OthelloStateTests {
	
	@Test
	public void testMoveIsValid() {
		// 2 is player 1 = O = true, 3 is player 2 = X = false
		OthelloState state = new OthelloState();
		state.setValueOnBoards((byte)3, (byte)3, (byte)2);
		state.setValueOnBoards((byte)4, (byte)4, (byte)2);
		state.setValueOnBoards((byte)3, (byte)4, (byte)3);
		state.setValueOnBoards((byte)4, (byte)3, (byte)3);
		state.generateMoveBoards();
		System.out.println(state.toString());
		
		assertTrue(state.moveIsValid((byte)4, (byte)2, true));
		assertTrue(state.moveIsValid((byte)5, (byte)3, true));
		assertTrue(state.moveIsValid((byte)2, (byte)4, true));
		assertTrue(state.moveIsValid((byte)3, (byte)5, true));
		
		OthelloState s2 = new OthelloState();
		/*for (int i=0; i < 8; i++) {
			for (int j=0; j < 8; j++) {
				boolean val = state.moveIsValid((byte)i, (byte)j, true);
				s2.setValueOnBoards((byte)i, (byte)j, val ? (byte) 2 : (byte)3);
				
			}
		}*/
		assertTrue(state.moveIsValid((byte)2, (byte)3, false));
		assertTrue(state.moveIsValid((byte)3, (byte)2, false));
		assertTrue(state.moveIsValid((byte)4, (byte)5, false));
		assertTrue(state.moveIsValid((byte)5, (byte)4, false));
		
		/*for (int i=0; i < 8; i++) {
			for (int j=0; j < 8; j++) {
				boolean val = state.moveIsValid((byte)i, (byte)j, false);
				s2.setValueOnBoards((byte)i, (byte)j, val ? (byte) 2 : (byte)3);
			}
		}
		
		System.out.println(s2);*/
	}
	
	@Test
	public void testMoveIsValid2() {
		
		// NOTE: x is vertical, y is horizontal
		OthelloState state = new OthelloState();
		state.setValueOnBoards((byte)3, (byte)3, (byte)3);
		state.setValueOnBoards((byte)4, (byte)4, (byte)2);
		state.setValueOnBoards((byte)3, (byte)4, (byte)3);
		state.setValueOnBoards((byte)4, (byte)3, (byte)3);
		state.setValueOnBoards((byte)2, (byte)3, (byte)3);
		state.generateMoveBoards();

		assertTrue(state.moveIsValid((byte)2, (byte)2, true));
		assertTrue(state.moveIsValid((byte)4, (byte)2, true));
		assertTrue(state.moveIsValid((byte)2, (byte)4, true));
		assertFalse(state.moveIsValid((byte)1, (byte)3, true));
		System.out.println(state);
	}
	
	@Test
	public void testCapture() {
		OthelloState state = new OthelloState();
		state.setValueOnBoards((byte)3, (byte)3, (byte)3);
		state.setValueOnBoards((byte)4, (byte)4, (byte)2);
		state.setValueOnBoards((byte)3, (byte)4, (byte)3);
		state.setValueOnBoards((byte)4, (byte)3, (byte)3);
		state.setValueOnBoards((byte)2, (byte)3, (byte)3);
		state.generateMoveBoards();
		OthelloState res = state.childOnMove((byte)2, (byte)3);
		
		for (int i=0; i < 2; i++) {
			for (int j= 0; j < 8; j++) {
				assertFalse(res.moveIsValid((byte)i, (byte)j, true));
			}
		}
		
		for (int i=5; i < 8; i++) {
			for (int j= 0; j < 8; j++) {
				assertFalse(res.moveIsValid((byte)i, (byte)j, true));
			}
		}
		
		
		//System.out.println("res"+res);
		OthelloState res2 = res.childOnMove((byte)2, (byte)2);
		//System.out.println("res2"+res2);
		OthelloState res3 = res2.childOnMove((byte)3, (byte)2);
		//System.out.println("res3\n"+res3);
		OthelloState res4 = res3.childOnMove((byte)4, (byte)2);
		System.out.println("res4\n"+res4);
		
		OthelloState printState = new OthelloState();
		for (byte i=0; i < 8; i++) {
			for (byte j=0; j < 8; j++) {
				boolean val = res4.moveIsValid((byte)i, (byte)j, false);
				printState.setValueOnBoards(i, j, val ? (byte)3 : (byte)2);
			}
		}
		
		System.out.println(printState);
	}

	@Test
	public void testPlan() {
		OthelloState state = new OthelloState();
		state.setStandardStartState();
		String[] moves = "f5 f6 e6 f4 e3 c5 c6 d6 e7 f3 g4 g5 h6 g3 b5 h4 g6 f7 h5 h7 h3 h2 f8 d7 e8 c4 c7".split(" ");
		
		for (String s: moves) {
			Point p = sToM(s);
			state = state.childOnMove((byte)p.x, (byte)p.y);
		}
		
		Decider d = new MTDDecider(false, 12000, 64);
		OthelloAction a = (OthelloAction) d.decide(state);
		
		System.out.println("Move: "+a);
		try {
			state = a.applyTo(state);
			System.out.println("New State:\n"+state);
			assertTrue(state.getSpotAsChar((byte)6, (byte)6) == ' ');
		} catch (InvalidActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void testPlan2() {
		OthelloState state = new OthelloState();
		state.setStandardStartState();
		String[] moves = "d3 c5 e6 f5 f6 e3 d6 f7 b6 d7 d8 c6".split(" "); // g7 b5 
		
		for (String s: moves) {
			Point p = sToM(s);
			state = state.childOnMove((byte)p.x, (byte)p.y);
		}
		
		Decider d = new MTDDecider(true, 50000, 64);
		OthelloAction a = (OthelloAction) d.decide(state);
		
		System.out.println("Move: "+a);
		try {
			state = a.applyTo(state);
			System.out.println("New State:\n"+state);
			assertTrue(state.getSpotAsChar((byte)6, (byte)6) == ' ');
		} catch (InvalidActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testPlan3() {
		OthelloState state = new OthelloState();
		state.setStandardStartState();
		String[] moves = "e6 f4 d3 d6 e3 f6 e7 f8 g5 h4 h6 c3 c5 d2 c4 f5 f3 e2 g4 g6 f2 h3 e1 h5 h2 g3 c1 d1 f1 c2 e8 d7 c8 c6 b3 a3 h7 f7 b6 d8 g8 c7 b8 a6".split(" "); // b4 a4 b5 b7 
		
		for (String s: moves) {
			Point p = sToM(s);
			state = state.childOnMove((byte)p.x, (byte)p.y);
		}
		
		Decider d = new MTDDecider(true, 50000, 64);
		OthelloAction a = (OthelloAction) d.decide(state);
		
		System.out.println("Move: "+a);
		try {
			state = a.applyTo(state);
			System.out.println("New State:\n"+state);
			assertTrue(state.getSpotAsChar((byte)6, (byte)6) == ' ');
		} catch (InvalidActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Point sToM(String move) {
		if (move.length() != 2) {
			throw new IllegalArgumentException();
		}
		HashMap<Character,Integer> charMap = new HashMap<Character, Integer>();
		charMap.put('a', 0);
		charMap.put('b', 1);
		charMap.put('c', 2);
		charMap.put('d', 3);
		charMap.put('e', 4);
		charMap.put('f', 5);
		charMap.put('g', 6);
		charMap.put('h', 7);
		
		char c1 = move.charAt(0);
		char c2 = move.charAt(1);
		
		Point p = new Point();
		p.x = Character.getNumericValue(c2) - 1;
		p.y = charMap.get(c1);
		return p;
		
	}
	
	@Test
	public void testHeuristic() {
		OthelloState state = new OthelloState();
		state.setValueOnBoards((byte)1, (byte)7, (byte)2);
		state.setValueOnBoards((byte)2, (byte)5, (byte)3);
		state.setValueOnBoards((byte)2, (byte)6, (byte)2);
		
		state.setValueOnBoards((byte)3, (byte)3, (byte)3);
		state.setValueOnBoards((byte)3, (byte)4, (byte)3);
		state.setValueOnBoards((byte)3, (byte)5, (byte)3);
		
		state.setValueOnBoards((byte)4, (byte)2, (byte)3);
		state.setValueOnBoards((byte)4, (byte)3, (byte)3);
		state.setValueOnBoards((byte)4, (byte)4, (byte)2);
		state.setValueOnBoards((byte)4, (byte)5, (byte)3);
		
		state.setValueOnBoards((byte)5, (byte)1, (byte)3);
		state.setValueOnBoards((byte)5, (byte)2, (byte)3);
		state.setValueOnBoards((byte)5, (byte)3, (byte)3);
		state.setValueOnBoards((byte)5, (byte)4, (byte)2);
		state.setValueOnBoards((byte)5, (byte)5, (byte)3);
		
		state.setValueOnBoards((byte)6, (byte)2, (byte)2);
		state.setValueOnBoards((byte)6, (byte)4, (byte)2);
		
		state.setValueOnBoards((byte)7, (byte)1, (byte)2);
		state.setValueOnBoards((byte)7, (byte)2, (byte)2);
		state.setValueOnBoards((byte)7, (byte)3, (byte)2);
		
		state.generateMoveBoards();
		
		MTDDecider d = new MTDDecider(true, 500000, 64);
		Action a = d.decide(state);
		try {
			State newstate = a.applyTo(state);
			
			System.out.println(newstate);
		} catch (InvalidActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testHash() {
		OthelloState state = new OthelloState();
		state.setStandardStartState();
		state.generateMoveBoards();
		
		state.setValueOnBoards((byte)3, (byte)3, (byte)2);
		state.setValueOnBoards((byte)3, (byte)4, (byte)3);
		
		int hc1 = state.hashCode();
		state.setValueOnBoards((byte)2, (byte)3, (byte)2);
		state.setValueOnBoards((byte)2, (byte)4, (byte)3);
		int hc2 = state.hashCode();
		
		assertFalse(hc1==hc2);
		
	}
}
