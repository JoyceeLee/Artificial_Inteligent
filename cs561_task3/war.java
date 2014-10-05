import java.lang.*;
import java.util.*;
import java.io.*;

class nodeState extends Object {
	public String node;
	public float value;
	public int occupant;
	nodeState(){ };
	nodeState(String node, float value, int occupant) {
		this.node = node;
		this.value = value;
		this.occupant = occupant;
	}
	public boolean equals(Object object){
		if(object instanceof nodeState) {
			nodeState ns = (nodeState) object;
			if(this.node.equals(ns.node)) {
				return true;
			}
		}
		return false;
	}
	public nodeState Clone() {
		return new nodeState(this.node, this.value, this.occupant);
	}
}
class operation {
	public nodeState nodeSt;
	public int oper;
}
class turn {
	public String Player;
	public String Action;
	public String Dest;
	public ArrayList<String> Union = new ArrayList<String>();
	public float unionValue = 0;
	public ArrayList<String> Confed = new ArrayList<String>();
	public float confedValue = 0;
	public turn Clone() {
		turn tmp = new turn();
		tmp.Player = this.Player;
		tmp.Action = this.Action;
		tmp.Dest = this.Dest;
		tmp.Union.addAll(this.Union);
		tmp.unionValue = this.unionValue;
		tmp.Confed.addAll(this.Confed);
		tmp.confedValue = this.confedValue;
		return tmp;
	}
}
class record {
	public String Player;
	public String Action;
	public String Dest;
	public int Depth;
	public float value;
	public float alpha;
	public float beta;
	public boolean cutoff;
	public record Clone() {
		record tmp = new record();
		tmp.Player = this.Player;
		tmp.Action = this.Action;
		tmp.Dest = this.Dest;
		tmp.Depth = this.Depth;
		tmp.value = this.value;
		tmp.alpha = this.alpha;
		tmp.beta = this.beta;
		tmp.cutoff = this.cutoff;
		return tmp;
	}
	
}
public class war {
	static int task;
	static boolean log = true;
	static int cutoffDepth;
	static String outputPath;
	static String outputLog;
	static BufferedReader mapBuffer = null;
	static final int Force_March = 1;
	static final int Paratroop_Drop = 2;
	static final int Union = 1;
	static final int Confederacy = -1;
	static ArrayList<nodeState> nState = new ArrayList<nodeState>();
	static ArrayList<operation> path = new ArrayList<operation>();
	static ArrayList<turn> outputTurn = new ArrayList<turn>();
	static ArrayList<record> outputRec = new ArrayList<record>();
	static int[][] graph;
	public static void main(String[] args) {
		parseInput(args);
		if(task==1) {
			cutoffDepth = 1;
		}
		MAXMIN();
	}
	public static void MAXMIN() {
		int count = 0;
		int player = Union;
		turn step = new turn();
		step.Player = "N/A";
		step.Action = "N/A";
		step.Dest = "N/A";
		for(int i=0; i<nState.size(); i++) {
			if(nState.get(i).occupant==Union) {
				step.Union.add(nState.get(i).node);
				step.unionValue += nState.get(i).value;
			}
			else if(nState.get(i).occupant==Confederacy) {
				step.Confed.add(nState.get(i).node);
				step.confedValue += nState.get(i).value;
			}
			else if(nState.get(i).occupant==0) { // count by the way
				count++;
			}
		}
		outputTurn.add(step.Clone());
		while(count != 0) {
			if(player==Union) {
				cutoffDepth = cutoffDepth > count ? count : cutoffDepth;
				MAX(0, cutoffDepth, nState, "", 0, -Float.MAX_VALUE, Float.MAX_VALUE);
				int index = nState.indexOf(path.get(path.size()-1).nodeSt);
				nState.get(index).occupant = path.get(path.size()-1).nodeSt.occupant;
				if(path.get(path.size()-1).oper==Force_March) {
					int j = nState.indexOf(path.get(path.size()-1).nodeSt);
					for(int i=0; i<nState.size(); i++) {
						if(graph[i][j]==1 && nState.get(i).occupant==-1) {
							nState.get(i).occupant = 1;
						}
					}
				}
				//--------implement this turn--------
				step = new turn();
				step.Player = "Union";
				step.Action = path.get(path.size()-1).oper==Force_March ? "Force March" : "Paratroop Drop";
				step.Dest = path.get(path.size()-1).nodeSt.node;
				for(int i=0; i<nState.size(); i++) {
					if(nState.get(i).occupant==Union) {
						step.Union.add(nState.get(i).node);
						step.unionValue += nState.get(i).value;
					}
					else if(nState.get(i).occupant==Confederacy) {
						step.Confed.add(nState.get(i).node);
						step.confedValue += nState.get(i).value;
					}
				}
				outputTurn.add(step.Clone());
				//-----------------------------------
				player = -player;
				count--;
			} else {
				log = false;
				MIN(0, 1, nState, "", 0, -Float.MAX_VALUE, Float.MAX_VALUE);
				int index = nState.indexOf(path.get(path.size()-1).nodeSt);
				nState.get(index).occupant = path.get(path.size()-1).nodeSt.occupant;
				if(path.get(path.size()-1).oper==Force_March) {
					int j = nState.indexOf(path.get(path.size()-1).nodeSt);
					for(int i=0; i<nState.size(); i++) {
						if(graph[i][j]==1 && nState.get(i).occupant==1) {
							nState.get(i).occupant = -1;
						}
					}
				}
				//--------implement this turn--------
				step = new turn();
				step.Player = "Confederacy";
				step.Action = path.get(path.size()-1).oper==Force_March ? "Force March" : "Paratroop Drop";
				step.Dest = path.get(path.size()-1).nodeSt.node;
				for(int i=0; i<nState.size(); i++) {
					if(nState.get(i).occupant==Union) {
						step.Union.add(nState.get(i).node);
						step.unionValue += nState.get(i).value;
					}
					else if(nState.get(i).occupant==Confederacy) {
						step.Confed.add(nState.get(i).node);
						step.confedValue += nState.get(i).value;
					}
				}
				outputTurn.add(step.Clone());
				//-----------------------------------		
				player = -player;
				count--;
			}
		}
//		for(int i=0; i<outputTurn.size(); i++) {
//			System.out.println(outputTurn.get(i).Player);
//			System.out.println(outputTurn.get(i).Action);
//			System.out.println(outputTurn.get(i).Dest);
//			System.out.print("Union: {");
//			for(int j=0; j<outputTurn.get(i).Union.size(); j++) {
//				System.out.print(outputTurn.get(i).Union.get(j));
//			}
//			System.out.print("}\n");
//			System.out.println(outputTurn.get(i).unionValue);
//			System.out.print("Confed: {");
//			for(int j=0; j<outputTurn.get(i).Confed.size(); j++) {
//				System.out.print(outputTurn.get(i).Confed.get(j));
//			}
//			System.out.print("}\n");
//			System.out.println(outputTurn.get(i).confedValue);
//			System.out.println("-------------------------------------\n");
//		}
//		for(int i=0; i<Math.min(100,outputRec.size()) ; i++) {
//			System.out.print(outputRec.get(i).Player+",");
//			System.out.print(outputRec.get(i).Action+",");
//			System.out.print(outputRec.get(i).Dest+",");
//			System.out.print(outputRec.get(i).Depth+",");
//			if(task!=3) {
//				if(outputRec.get(i).value == Float.MAX_VALUE) {
//					System.out.print("Infinity\n");
//				} else if(outputRec.get(i).value == -Float.MAX_VALUE) {
//					System.out.print("-Infinity\n");
//				} else {
//					System.out.print(outputRec.get(i).value+"\n");
//				}
//			} else {
//				if(outputRec.get(i).value == Float.MAX_VALUE) {
//					System.out.print("Infinity, ");
//				} else if(outputRec.get(i).value == -Float.MAX_VALUE) {
//					System.out.print("-Infinity, ");
//				} else {
//					System.out.print(outputRec.get(i).value+", ");
//				}
//				//---- for alpha----
//				if(outputRec.get(i).alpha == -Float.MAX_VALUE) {
//					System.out.print("-Infinity, ");
//				} else {
//					System.out.print(outputRec.get(i).alpha+", ");
//				}
//				//------------------
//				//---- for beta ----
//				if(outputRec.get(i).beta == Float.MAX_VALUE) {
//					System.out.print("Infinity, ");
//				} else {
//					System.out.print(outputRec.get(i).beta+", ");
//				}
//				//------------------
//				if(outputRec.get(i).cutoff)
//					System.out.print("CUT-OFF");
//				System.out.print("\n");
//			}
//		}
		try {
			File f = new File(outputPath);
			if(f.exists()) {
				f.delete();
			}
			f.createNewFile();
			PrintWriter pwPath = new PrintWriter(new FileWriter(f));
			for(int i=0; i<outputTurn.size(); i++) {
				pwPath.write("TURN = "+i+"\n");
				pwPath.write("Player = "+outputTurn.get(i).Player+"\n");
				pwPath.write("Action = "+outputTurn.get(i).Action+"\n");
				pwPath.write("Destination = "+outputTurn.get(i).Dest+"\n");
				pwPath.write("Union,{");
				if(outputTurn.get(i).Union.size()>0) {
					pwPath.write(outputTurn.get(i).Union.get(0));
				}
				for(int j=1; j<outputTurn.get(i).Union.size(); j++) {
					pwPath.write(","+outputTurn.get(i).Union.get(j));
				}
				pwPath.write("},"+outputTurn.get(i).unionValue+"\n");
				pwPath.write("Confederacy,{");
				if(outputTurn.get(i).Confed.size()>0) {
					pwPath.write(outputTurn.get(i).Confed.get(0));
				}
				for(int j=1; j<outputTurn.get(i).Confed.size(); j++) {
					pwPath.write(","+outputTurn.get(i).Confed.get(j));
				}
				pwPath.write("},"+outputTurn.get(i).confedValue+"\n");
				pwPath.write("----------------------------------------------\n");
			}
			pwPath.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			File f = new File(outputLog);
			if(f.exists()) {
				f.delete();
			}
			f.createNewFile();
			PrintWriter pwLog = new PrintWriter(new FileWriter(f));
			if(task==3) {
				pwLog.write("Player,Action,Destination,Depth,Value,Alpha,Beta,CUT-OFF?\n");
			} else {
				pwLog.write("Player,Action,Destination,Depth,Value\n");
			}
			for(int i=0; i<outputRec.size(); i++) {
				pwLog.write(outputRec.get(i).Player+",");
				pwLog.write(outputRec.get(i).Action+",");
				pwLog.write(outputRec.get(i).Dest+",");
				pwLog.write(outputRec.get(i).Depth+",");
				if(task!=3) {
					if(outputRec.get(i).value == Float.MAX_VALUE) {
						pwLog.write("Infinity\n");
					} else if(outputRec.get(i).value == -Float.MAX_VALUE) {
						pwLog.write("-Infinity\n");
					} else {
						pwLog.write(outputRec.get(i).value+"\n");
					}
				} else {
					if(outputRec.get(i).value == Float.MAX_VALUE) {
						pwLog.write("Infinity,");
					} else if(outputRec.get(i).value == -Float.MAX_VALUE) {
						pwLog.write("-Infinity,");
					} else {
						pwLog.write(outputRec.get(i).value+",");
					}
					//---- for alpha----
					if(outputRec.get(i).alpha == -Float.MAX_VALUE) {
						pwLog.write("-Infinity,");
					} else {
						pwLog.write(outputRec.get(i).alpha+",");
					}
					//------------------
					//---- for beta ----
					if(outputRec.get(i).beta == Float.MAX_VALUE) {
						pwLog.write("Infinity");
					} else {
						pwLog.write(outputRec.get(i).beta+"");
					}
					//------------------
					if(outputRec.get(i).cutoff)
						pwLog.write(",CUT-OFF");
					pwLog.write("\n");
				}
			}
			pwLog.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public static float MAX(int curDepth, int maxDepth, ArrayList<nodeState> curState, String lastNode, int lastAct, float alpha, float beta) {
		float value;
		// judge if this is the terminal
		if(curDepth >= maxDepth) {
			value = 0;
			for(int i=0; i<curState.size();i++) {
				value += curState.get(i).value*curState.get(i).occupant;
			}
			if(log) {
				record rec = new record();
				rec.Player = "Confederacy";
				rec.Action = lastAct==Force_March ? "Force March" : "Paratroop Drop";
				rec.Dest = lastNode;
				rec.Depth = curDepth;
				rec.value = value;
				rec.alpha = alpha;
				rec.beta = beta;
				rec.cutoff = false;
				outputRec.add(rec.Clone());
			}
		}
		else{
			// implement operation queue of current state
			Queue<nodeState> FM = new LinkedList<nodeState>();
			Queue<nodeState> PD = new LinkedList<nodeState>();
			for(int i=0; i<curState.size(); i++) {
				if(curState.get(i).occupant==0) {	
					for(int j=0; j<curState.size(); j++) {
						if(graph[i][j]==1 && curState.get(j).occupant==1) {
							FM.add(curState.get(i));
							break;
						}
					}
					PD.add(curState.get(i));
				}
			}
			value = -Float.MAX_VALUE;
			int oper = Force_March;
			String maxnode;
			operation nextOper = new operation();
			//********** new record ***********
			 record rec = new record();
			 if(log) {
				 if(curDepth==0) {
						rec.Player = "N/A";
						rec.Action = "N/A";
						rec.Dest = "N/A";
						rec.Depth = curDepth;
						rec.value = value;
						rec.alpha = alpha;
						rec.beta = beta;
						rec.cutoff = false;
					} else {
						rec.Player = "Confederacy";
						rec.Action = lastAct==Force_March ? "Force March" : "Paratroop Drop";
						rec.Dest = lastNode;
						rec.Depth = curDepth;
						rec.value = value; 
						rec.alpha = alpha;
						rec.beta = beta;
						rec.cutoff = false;
					}
					if(task!=1 || curDepth!=0) {
						outputRec.add(rec.Clone()); 
					}
			 }
			//---------------------------------
			while(!FM.isEmpty()) {
				nodeState curNode = FM.remove();
				curNode.occupant=1;
				int j = curState.indexOf(curNode);
				ArrayList<Integer> conquer = new ArrayList<Integer>();
				for(int i=0; i<curState.size(); i++) {
					if(graph[i][j]==1 && curState.get(i).occupant==-1) {
						curState.get(i).occupant = 1;
						conquer.add(i);
					}
				}
				float tmp = MIN(curDepth+1, maxDepth, curState, curNode.node, Force_March, alpha, beta);
				if(log) {
					if(task!=1 || curDepth!=0) {
						rec.value = rec.value > tmp ? rec.value : tmp;
						rec.alpha = rec.alpha > tmp ? rec.alpha : tmp;
						if(rec.beta<=rec.alpha) rec.cutoff=true;
						outputRec.add(rec.Clone());
					}
				}
				if(tmp>value) {
					value = tmp;
					maxnode = curNode.node;
					if(curDepth==0) {
						nextOper.nodeSt = curNode.Clone();
						nextOper.oper = Force_March;
					}
				}
				curNode.occupant=0;
				for(int i=0; i<conquer.size(); i++) {
					curState.get(conquer.get(i)).occupant = -1;
				}
				conquer.clear();
				//-------- task 3 ---------
				if(task==3) {
					alpha = alpha > tmp ? alpha : tmp;
					if(beta<=alpha) {
						return value;
					}
				}
				//-------------------------
			}
			while(!PD.isEmpty()) {
				nodeState curNode = PD.remove();
				curNode.occupant=1;
				float tmp = MIN(curDepth+1, maxDepth, curState, curNode.node, Paratroop_Drop, alpha, beta);
				if(log) {
					if(task!=1 || curDepth!=0) {
						rec.value = rec.value > tmp ? rec.value : tmp;
						rec.alpha = rec.alpha > tmp ? rec.alpha : tmp;
						if(rec.beta<=rec.alpha) rec.cutoff=true;
						outputRec.add(rec.Clone());
					}
				}
				if(tmp>value) {
					value = tmp;
					maxnode = curNode.node;
					oper = Paratroop_Drop;
					if(curDepth==0) {
						nextOper.nodeSt = curNode.Clone();
						nextOper.oper = Paratroop_Drop;
					}
				}
				curNode.occupant=0;
				//-------- task 3 ---------
				if(task==3) {
					alpha = alpha > tmp ? alpha : tmp;
					if(beta<=alpha) {
						return value;
					}
				}
				//-------------------------
			}
			if(curDepth==0) {
				path.add(nextOper);
			}
		}
		return value;
	}
	public static float MIN(int curDepth, int maxDepth, ArrayList<nodeState> curState, String lastNode, int lastAct, float alpha, float beta) {
		float value;
		// judge if this is the terminal
		if(curDepth >= maxDepth) {
			value = 0;
			for(int i=0; i<curState.size();i++) {
				value += curState.get(i).value*curState.get(i).occupant;
			}
			if(log) {
				record rec = new record();
				rec.Player = "Union";
				rec.Action = lastAct==Force_March ? "Force March" : "Paratroop Drop";
				rec.Dest = lastNode;
				rec.Depth = curDepth;
				rec.value = value;
				rec.alpha = alpha;
				rec.beta = beta;
				rec.cutoff = false;
				outputRec.add(rec.Clone());
			}
		}
		else {
			// implement operation queue of current state
			Queue<nodeState> FM = new LinkedList<nodeState>();
			Queue<nodeState> PD = new LinkedList<nodeState>();
			for(int i=0; i<curState.size(); i++) {
				if(curState.get(i).occupant==0) {
					for(int j=0; j<curState.size(); j++) {
						if(graph[i][j]==1 && curState.get(j).occupant==-1) {
							FM.add(curState.get(i));
							break;
						}
					}
					PD.add(curState.get(i));
				}
			}
			value = Float.MAX_VALUE;
			int oper = Force_March;
			String minnode;
			operation nextOper = new operation();
			//********** new record ***********
			 record rec = new record(); 
			 if(log) {
				 if(curDepth==0) {
						rec.Player = "N/A";
						rec.Action = "N/A";
						rec.Dest = "N/A";
						rec.Depth = curDepth;
						rec.value = value;
						rec.alpha = alpha;
						rec.beta = beta;
						rec.cutoff = false;
					} else {
						rec.Player = "Union";
						rec.Action = lastAct==Force_March ? "Force March" : "Paratroop Drop";
						rec.Dest = lastNode;
						rec.Depth = curDepth;
						rec.value = value;
						rec.alpha = alpha;
						rec.beta = beta;
						rec.cutoff = false;
					}
					if(task!=1 || curDepth!=0) {
						outputRec.add(rec.Clone());
					}
			 }		
			//---------------------------------
			while(!FM.isEmpty()) {
				nodeState curNode = FM.remove();
				curNode.occupant = -1;
				int j = curState.indexOf(curNode);
				ArrayList<Integer> conquer = new ArrayList<Integer>();
				for(int i=0; i<curState.size(); i++) {
					if(graph[i][j]==1 && curState.get(i).occupant==1) {
						curState.get(i).occupant = -1;
						conquer.add(i);
					}
				}
				float tmp = MAX(curDepth+1, maxDepth, curState, curNode.node, Force_March, alpha, beta);
				if(log) {
					if(task!=1 || curDepth!=0) {
						rec.value = rec.value < tmp ? rec.value : tmp;
						rec.beta = rec.beta < tmp ? rec.beta : tmp;
						if(rec.beta<=rec.alpha) rec.cutoff=true;
						outputRec.add(rec.Clone());
					}
				}
				if(tmp<value) {
					value = tmp;
					minnode = curNode.node;
					if(curDepth==0) {
						nextOper.nodeSt = curNode.Clone();
						nextOper.oper = Force_March;
					}
				}
				curNode.occupant=0;
				for(int i=0; i<conquer.size(); i++) {
					curState.get(conquer.get(i)).occupant = 1;
				}
				conquer.clear();
				//-------- task 3 ---------
				if(task==3) {
					beta = beta < tmp ? beta : tmp;
					if(beta<=alpha) {
						return value;
					}
				}
				//-------------------------
			}
			while(!PD.isEmpty()) {
				nodeState curNode = PD.remove();
				curNode.occupant = -1;
				float tmp = MAX(curDepth+1, maxDepth, curState, curNode.node, Paratroop_Drop, alpha, beta);
				if(log) {
					if(task!=1 || curDepth!=0) {
						rec.value = rec.value < tmp ? rec.value : tmp;
						rec.beta = rec.beta < tmp ? rec.beta : tmp;
						if(rec.beta<=rec.alpha) rec.cutoff=true;
						outputRec.add(rec.Clone());
					}
				}
				if(tmp<value) {
					value = tmp;
					minnode = curNode.node;
					oper = Paratroop_Drop;
					if(curDepth==0) {
						nextOper.nodeSt = curNode.Clone();
						nextOper.oper = Paratroop_Drop;
					}
				}
				curNode.occupant=0;
				//-------- task 3 ---------
				if(task==3) {
					beta = beta < tmp ? beta : tmp;
					if(beta<=alpha) {
						return value;
					}
				}
				//-------------------------
			}
			if(curDepth==0) {
				path.add(nextOper);
			}
		}
		return value;
	}
	public static void parseInput(String[] inputStr) {
		int len = inputStr.length;
		for(int i=0; i<len; i++) {
			if(inputStr[i].equals("-t")){
				task = inputStr[++i].toCharArray()[0]-'0';
			} else if(inputStr[i].equals("-d")) {
				cutoffDepth = inputStr[++i].toCharArray()[0]-'0';
			} else if(inputStr[i].equals("-m")) {
				File map = new File(inputStr[++i]);
				try {
					InputStreamReader mapReader = new InputStreamReader(new FileInputStream(map));
					mapBuffer = new BufferedReader(mapReader);
				} catch(Exception e) {
					e.printStackTrace();
				}
			} else if(inputStr[i].equals("-i")) {
				File init = new File(inputStr[++i]);
				try {
					InputStreamReader initReader = new InputStreamReader(new FileInputStream(init));
					BufferedReader initBuffer = new BufferedReader(initReader);
					String initNode = initBuffer.readLine();
					while(initNode!=null) {
						String[] initInfo = initNode.split(",");
						String thisnode = initInfo[0];
						float thisvalue = Integer.parseInt(initInfo[1]);
						int thisoccup = Integer.parseInt(initInfo[2]);
						nState.add(new nodeState(thisnode, thisvalue, thisoccup));
						initNode = initBuffer.readLine();
					}
					initBuffer.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			} else if(inputStr[i].equals("-op")) {
				outputPath = inputStr[++i];
			} else if(inputStr[i].equals("-ol")) {
				outputLog = inputStr[++i];
			}
		}
		graph= new int[nState.size()][nState.size()];
		try {
			String initEdge = mapBuffer.readLine();
			while(initEdge!=null) {
				String[] initends = initEdge.split(",");
				int i = nState.indexOf(new nodeState(initends[0],0,0));
				int j = nState.indexOf(new nodeState(initends[1],0,0));
				graph[i][j] = 1;
				graph[j][i] = 1;
				initEdge = mapBuffer.readLine();
			}
			// mapBuffer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}