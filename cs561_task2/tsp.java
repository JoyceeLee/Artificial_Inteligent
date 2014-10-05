/**
 * @author Yang Li
 */

import java.lang.*;
import java.io.*;
import java.util.*;
// node in map
class Node {
	char val;
	int x;
	int y;
	float f;
	float h;
	float g;
	Node preNode;
	Node(char val, int x, int y){
		this.val = val;
		this.x = x;
		this.y = y;
	}
	Node(Node newNode){
		this.val = newNode.val;
		this.x = newNode.x;
		this.y = newNode.y;
		this.f = newNode.f;
		this.h = newNode.h;
		this.g = newNode.g;
		this.preNode = newNode.preNode;
	}
}
// the comparator to sort node in openList
class NodeFCamp implements Comparator<Node>{
	public int compare(Node arg0, Node arg1) {
		if(((int)arg0.f-(int)arg1.f)!=0){
			return ((int)arg0.f-(int)arg1.f);
		}
		else return (arg0.y*tsp.n+arg0.x)-(arg1.y*tsp.n+arg1.x);
	}
}
//the comparator to sort State in openlState
class StateFCamp implements Comparator<State>{
	public int compare(State arg0, State arg1){
		return (int)(arg0.g+arg0.h) - (int)(arg1.g+arg1.h);
	}
}
class disCamp implements Comparator<pathDis>{
	public int compare(pathDis arg0, pathDis arg1){
		return ((int)arg0.dis- (int)arg1.dis);
	}
}
// the shortest distance from node s to node g 
class pathDis{
	char s;
	char g;
	float dis;
	pathDis(char s, char g, float dis){
		this.s = s;
		this.g = g;
		this.dis = dis;
	}
	pathDis(pathDis pd){
		this.s = pd.s;
		this.g = pd.g;
		this.dis = pd.dis;
	}
}

// record path from node start to goal, this is for print
class Path {
	 char start;
	 char goal;
	 ArrayList<Node> trace = new ArrayList<Node>();;
	 Path(char start, char goal){
		 this.start = start;
		 this.goal = goal;
	 }
}
// state when solve the TSP
class State {
	char cur;
	ArrayList<Character> visited = new ArrayList<Character>();
	float g;
	float h;
	State(char cur, float g, float h){
		this.cur = cur;
		this.g = g;
		this.h = h;
	}
}
public class tsp {
	public static int task;
	public static File inputFile;
	public static String outputFile;
	public static String outputLog;
	public static char[][] map;
	public static int m = 0;
	public static int n = 0;
	public static List<Node> openList = new ArrayList<Node>();
	public static List<Node> closeList = new ArrayList<Node>();
	public final static int step = 1;
	public static ArrayList<Path> paths = new ArrayList<Path>();
	public static ArrayList<pathDis> graph = new ArrayList<pathDis>();
	public static float[][] disTable;
	public static List<State> openlState = new ArrayList<State>();
	public static List<State> closelState = new ArrayList<State>();
	public static Node[] nOrder;
	public static TreeSet<Node> nodeOrder = new TreeSet<Node>(new Comparator<Object>(){
		public int compare(Object n1, Object n2){
			Node no1 = (Node) n1;
			Node no2 = (Node) n2;
			if(no1.val>no2.val) return -1;
			else if(no1.val==no2.val) return 0;
			else return 1;
		}
	});
	
	public static void main(String[] args) {
		parseInput(args);
		searchPair();
		if(task==1){
			printLog(paths);
			printPath(graph);
		}
		if(task==2){
//			System.out.println("this is the new task2");
			tspPath(graph);
			printTSP(closelState);
			printTSPLog(closelState);
		}
	}
	
	public static void searchPair(){
		int len = nodeOrder.size();
		nOrder = new Node[len];
		for(int i = 0; i<len; i++){
			// nOrder[i] = nodeOrder.pollLast();
			nOrder[i] = new Node(nodeOrder.last());
			nodeOrder.remove(nodeOrder.last());
			// System.out.println(nOrder[i].val);
			//=====================================
		}
		for(int i = 0; i<(len-1); i++)
			for(int j = i+1; j< len; j++){
				openList.clear();
				closeList.clear();
				AStar(nOrder[i], nOrder[j]);
			}
	}
	
	// main search algorithm
	public static void AStar(Node s, Node g){
		Node cur = null;
		Path p = new Path(s.val, g.val);
		Node st = new Node(s.val,s.x,s.y);
		st.g = 0;
		st.h = Math.abs(g.x-s.x) + Math.abs(g.y-s.y);
		st.f = st.g + st.h;
		openList.add(st);
		while(!openList.isEmpty()){
			cur = openList.get(0);
			p.trace.add(cur);
			if(cur.val==g.val){
				paths.add(p);
				graph.add(new pathDis(s.val, g.val, cur.f));
				return;
			}
			if(cur.y-1>=0){
				checkPath(cur.x, cur.y-1, cur, g);
			}
			if(cur.y+1<m){
				checkPath(cur.x, cur.y+1, cur, g);
			}
			if(cur.x-1>=0){
				checkPath(cur.x-1, cur.y, cur, g);
			}
			if(cur.x+1<n){
				checkPath(cur.x+1, cur.y, cur, g);
			}
			closeList.add(openList.remove(0));
			Collections.sort(openList, new NodeFCamp());
		}
	}
	
	public static void checkPath(int x, int y, Node pre, Node g){
		Node nex = new Node(map[y][x],x,y);
		nex.preNode = pre;
		// check if the node is reachable
		if(map[y][x]=='*'){
			closeList.add(nex);
			return;
		}
		// check if the node has already been visited
		if(isContains(closeList, nex.x, nex.y)!=-1){
			return;
		}
		int index = isContains(openList, nex.x, nex.y);
		if(index != -1){
			// check if have to update the g, f, h
			if(pre.g+step<openList.get(index).g){
				nex.g = pre.g+step;
				nex.h = Math.abs(nex.x-g.x)+Math.abs(nex.y-g.y);
				nex.f = nex.h + nex.g;
				openList.set(index, nex);
			}
		} else {
			nex.h = Math.abs(nex.x-g.x)+Math.abs(nex.y-g.y);
			nex.g = nex.preNode.g+step;
			nex.f = nex.g+ nex.h;
			openList.add(nex);
		}
	}
	
	public static int isContains(List<Node> list, int x, int y){
		for(int ii = 0; ii<list.size();ii++){
			if(list.get(ii).x==x && list.get(ii).y==y)
				return ii;
		}
		return -1;
	}
	
	public static void parseInput(String[] inputs) {
		int len = inputs.length;
		for(int i=0; i<len; i++) {
			if(inputs[i].equals("-t")) {
				task = (inputs[++i].toCharArray()[0]-'1')+1;
			}
			else if(inputs[i].equals("-i")) {
				String input_file = inputs[++i];
				inputFile = new File(input_file);
				try {
					InputStreamReader inputReader = new InputStreamReader(new FileInputStream(inputFile));
					BufferedReader inputBr = new BufferedReader(inputReader);
					inputBr.mark(1024);
					while(inputBr.readLine()!=null){
						m++;
					}
					inputBr.reset();
					String mapLine = inputBr.readLine();
					n = mapLine.length();
					map = new char[m][n];
					for(int ii=0; ii<m; ii++){
						map[ii]=mapLine.toCharArray();
						mapLine = inputBr.readLine();
					}
//					//-----------------------------
//					for(int ii =0; ii<m; ii++){
//						for(int jj=0; jj<n; jj++){
//							System.out.print(map[ii][jj]);
//						}
//						System.out.print('\r');
//						System.out.print('\n');
//					}
//					//-----------------------------
					for(int ii=0; ii<m;ii++)
						for(int jj=0; jj<n; jj++)
							if(map[ii][jj]>='A'){
								nodeOrder.add(new Node(map[ii][jj],jj,ii));
							}
					
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			else if(inputs[i].equals("-op")) {
				outputFile = inputs[++i];
			}
			else if(inputs[i].equals("-ol")) {
				outputLog = inputs[++i];
			}
			else {
				System.out.println("wrong input:"+inputs[i]);
			}
		}
	}
	
	public static void printPath(ArrayList<pathDis> graph){
		try{
			File f = new File(outputFile);
			if(f.exists()) {
				// System.out.println("delete the old file");
				f.delete();
			}
			f.createNewFile();
			PrintWriter pw = new PrintWriter(new FileWriter(f));
			int len = graph.size();
			for(int i=0; i<len; i++){
				pw.write(graph.get(i).s + "," + graph.get(i).g + "," + graph.get(i).dis+"\r\n");
			}
			pw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void printLog(ArrayList<Path> paths) {
		try{
			File f = new File(outputLog);
			if(f.exists()) {
				// System.out.println("delete the old file");
				f.delete();
			}
			f.createNewFile();
			PrintWriter pwl = new PrintWriter(new FileWriter(f));
			int len = paths.size();
			for(int i=0; i<len; i++){
				Path p = paths.remove(0);
				pwl.write("from '" + p.start + "' to '" + p.goal + "'\r\n");
				pwl.write("-----------------------------------------------\r\n");
				pwl.write("x,y,g,h,f\r\n");
				int plen = p.trace.size();
				for(int j=0; j< plen; j++){
					Node no = p.trace.get(j);
					pwl.write(no.x+","+no.y+","+no.g+","+no.h+","+no.f+"\r\n");
				}
				pwl.write("-----------------------------------------------\r\n");
			}
			pwl.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}


/***********************************************************************/
/*----------------------- functions for task 2 --------------------------*/
/***********************************************************************/
	
	public static void tspPath(ArrayList<pathDis> graph) {
		// table to record distance between each pair of node
		disTable = new float[nOrder.length][nOrder.length];
		for(int i=0; i<graph.size();i++){
			int rr = graph.get(i).s-'A';
			int cc = graph.get(i).g-'A';
			disTable[rr][cc] = graph.get(i).dis;
			disTable[cc][rr] = graph.get(i).dis;
		}
		ArrayList<Character> subV = new ArrayList<Character>();
		for(int i=0; i<nOrder.length;i++){
			subV.add(nOrder[i].val);
		}
		ArrayList<Character> previsited = new ArrayList<Character>();
		mst(subV, 'A', 0, previsited);
		//!openlState.isEmpty()
		while(true){
			State curSt = openlState.remove(0);
			closelState.add(curSt);
		//	System.out.println(curSt.cur);
			if(curSt.visited.size()==(nOrder.length-1)){
				int ss = curSt.cur-'A';
				int gg = 0;
				float fingoal = curSt.g + disTable[ss][gg];
				float finh = 0;
				State finSt = new State('A', fingoal, finh);
				finSt.visited.addAll(curSt.visited);
				finSt.visited.add(curSt.cur);
				closelState.add(finSt);
				return;
			}
			for(int i=0; i<nOrder.length;i++){
				if(!curSt.visited.contains(nOrder[i].val) && nOrder[i].val != curSt.cur){
					subV = new ArrayList<Character>();
					previsited = new ArrayList<Character>(); // use reference as arg here, do not clear !
					char nexCh = nOrder[i].val;
					int ss = curSt.cur-'A';
					int gg = nexCh-'A';
					float nexg = curSt.g + disTable[ss][gg];
					previsited.addAll(curSt.visited);
					previsited.add(curSt.cur);
					for(int ii=0; ii<nOrder.length;ii++){
						if(!previsited.contains(nOrder[ii].val)){
							subV.add(nOrder[ii].val);
						}
					}
					subV.add(0, 'A');
					mst(subV, nexCh, nexg, previsited);
				}
			}
			//sort State in the openlState
			Collections.sort(openlState, new StateFCamp());
		}
	}
	public static void mst(ArrayList<Character> subV, char cur, float g, ArrayList<Character> previsited) {
		// to sort edges in the sub graph
//		TreeSet<pathDis> disSort = new TreeSet<pathDis>(new Comparator<Object>(){
//			public int compare(Object arg1, Object arg2){
//				pathDis p1 = (pathDis) arg1;
//				pathDis p2 = (pathDis) arg2;
//				if((p1.s!=p2.s || p1.g!=p2.g) && p1.dis>=p2.dis ) return 1; // regard equal if return 0
//				else if(p1.s==p2.s && p1.g==p2.g) return 0;
//				else return -1;
//			}
//		});
		List<pathDis> disSort = new ArrayList<pathDis>();
		float h = 0;
		int edgeNum = 0;
		char[] subVert = new char[subV.size()];
		char[] parent = new char[subV.size()];
		ArrayList<pathDis> mst = new ArrayList<pathDis>();
		for(int i=0; i<subV.size(); i++){
			subVert[i] = subV.get(i);
			parent[i] = subV.get(i);
		}
		for(int i=0; i<(subVert.length-1);i++)
			for(int j=i+1; j<subVert.length;j++){
				int rr = subVert[i]-'A';
				int cc = subVert[j]-'A';
				disSort.add(new pathDis(subVert[i], subVert[j], disTable[rr][cc]));
			}
		Collections.sort(disSort, new disCamp());
		// main operation of Kruskal's Algorithm
		while(edgeNum < (subVert.length-1)) {
//			pathDis tmp = disSort.pollFirst();
			pathDis tmp = new pathDis(disSort.get(0));
			disSort.remove(0);
		//	System.out.println(disSort.size());
			//============================================
			int v1 = subV.indexOf(tmp.s);
			int v2 = subV.indexOf(tmp.g);
			if(parent[v1] != parent[v2]){
				mst.add(tmp); // add the edge to mst
				edgeNum++;
				h += tmp.dis;
				char combGroup = parent[v2];
				for(int i=0; i<parent.length;i++) // combine 2 components
					if(parent[i]==combGroup)
						parent[i]=parent[v1];
			}
		}
		State tmpSt = new State(cur, g, h);
		tmpSt.visited = previsited;
		openlState.add(tmpSt);
	}
	public static void printTSPLog(List<State> closelState){
		try{
			File f = new File(outputLog);
			if(f.exists()) {
				// System.out.println("delete the old file");
				f.delete();
			}
			f.createNewFile();
			PrintWriter pwl = new PrintWriter(new FileWriter(f));
			int lenTSP = closelState.size();
			for(int i=0; i<lenTSP; i++){
				int visNum = closelState.get(i).visited.size();
				for(int j=0; j<visNum; j++){
					pwl.write(closelState.get(i).visited.get(j));
				}
				float tmpf = closelState.get(i).h+closelState.get(i).g;
			    pwl.write(closelState.get(i).cur+","+closelState.get(i).g+","+closelState.get(i).h+","+tmpf+"\r\n");
			}
			pwl.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	public static void printTSP(List<State> closelState){
		try{
			File f = new File(outputFile);
			if(f.exists()) {
				// System.out.println("delete the old file");
				f.delete();
			}
			f.createNewFile();
			PrintWriter pw = new PrintWriter(new FileWriter(f));
			int lastSt = closelState.size()-1;
			int visNum = closelState.get(lastSt).visited.size();
			for(int j=0; j<visNum; j++){
				pw.write(closelState.get(lastSt).visited.get(j)+"\r\n");
			}
			float lastf = closelState.get(lastSt).h+closelState.get(lastSt).g;
			pw.write(closelState.get(lastSt).cur+"\r\n"+"Total Tour Cost: "+lastf+"\r\n");
			pw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}