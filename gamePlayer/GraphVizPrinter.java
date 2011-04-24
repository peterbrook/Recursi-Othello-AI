package gamePlayer;

import java.io.File;

/**
 * We use this class for debugging purposes.
 * It draws and maintains a graph of a search space.
 * @author Ashoat Tevosyan
 * @since Mon Apr 18 2011
 * @version CSE 473
 */
public class GraphVizPrinter {
	
	// The GraphViz object.
	private static GraphViz gv;
	// Which turn is this? Used so we don't overwrite the same file each turn.
	private static int turn = 0;
	// What type should we save our files in?
	public static String type = "svg";
	
	/**
	 * A static constructor to initialize GraphViz.
	 */
	static {
		gv = new GraphViz();
		gv.addln(gv.start_graph());
	}
	
	/**
	 * Add a State to the graph.
	 * @param state The State to add. 
	 */
	public static void setState(State state) {
		gv.addln(state.hashCode() + " [label=\"" + state.toString().replaceAll("\n", "\\\\n") + "\", shape=box, fontname=Courier];");
	}
	
	/**
	 * Set a relationship between two States with the given weight.
	 * @param state The child State.
	 * @param value The weight (heuristic) value of this relationship.
	 * @param daddy The parent State.
	 */
	public static void setRelation(State state, float value, State daddy) {
		if (daddy != null) gv.addln(daddy.hashCode() + " -> " + state.hashCode() + " [label=\"" + value + "\"];");
	}
	
	/**
	 * Highlight the final decision as red for easy viewing.
	 * @param state The final decision we settled on.
	 */
	public static void setDecision(State state) {
		gv.addln(state.hashCode() + " [color=red];");
	}
	
	/**
	 * Finally print the current graph to a file.
	 */
	public static void printGraphToFile() {
		gv.addln(gv.end_graph());
		gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), new File("turn" + turn++ + "." + type) );
		gv = new GraphViz();
		gv.addln(gv.start_graph());
	}
	
}