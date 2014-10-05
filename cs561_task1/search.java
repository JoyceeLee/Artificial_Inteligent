/**
 * @author: Yang Li
 * 
 * */
import java.text.DecimalFormat;
import java.util.*;
import java.io.*;

class Edge{
	private int adjvex;// end of the edge
	private float weight;// the next edge with the same start
	public Edge next;
	public void setAdj(int adjvex){
		this.adjvex = adjvex;
	}
	public int getAdj(){
		return this.adjvex;
	}
	public void setWeight(float weight){
		this.weight = weight;
	}
	public float getWeight(){
		return this.weight;
	}
	public Edge(int adjvex, float weight){
		this.adjvex = adjvex;
		this.weight = weight;
		next = null;
	}
}

class Node{
	private int node;
	private int pre;
	public Edge link;
	public int depth;
	public float cost;
	public Node(int node){
		this.node = node;
		link = null;
		this.pre = -1;
		this.depth = -1;
		this.cost = Float.MAX_VALUE;
	}
	public void setnode(int node){
		this.node = node;
	}
	public int getnode(){
		return this.node;
	}
	public void setpre(int pre){
		this.pre = pre;
	}
	public int getpre(){
		return this.pre;
	}
}

class Graph{
	private int nodeNum;
	public Node[] V;
	private boolean[] visited;
	public ArrayList<Integer> pathArray = new ArrayList<Integer>();
	public Node[] logArray;
	public static int count = 0;
	public Graph(int nodeNum, Node[] V){
		this.nodeNum = nodeNum;
		this.V = V;
		this.visited = new boolean[nodeNum];
		this.logArray = new Node[nodeNum];
	}
	public void BFS(int sNode, int gNode){
		Queue<Integer> que = new LinkedList<Integer>();
		que.add(sNode);
		visited[sNode] = true;
		V[sNode].depth = 0;
		V[sNode].cost = 0;
		while(!que.isEmpty()) {
			Integer v = que.poll();
			logArray[count++] = V[v];
			Edge e = V[v].link;
			while(e != null && !visited[gNode]) {
				if(!visited[e.getAdj()])
				{
					que.add(e.getAdj());
					visited[e.getAdj()] = true;
					V[e.getAdj()].setpre(v);
					V[e.getAdj()].depth = V[v].depth +1;
					DecimalFormat df = new DecimalFormat("0.00");
					double d = V[v].cost + e.getWeight();
					String db = df.format(d);
					V[e.getAdj()].cost = Float.parseFloat(db); // too many decimal digits
				}
				e = e.next;
			}
		}
		pathArray.add(gNode);
		int pathNode = V[gNode].getpre();
		while(pathNode != -1){
			pathArray.add(pathNode);
			pathNode = V[pathNode].getpre();
		}
	}
	public void DFS(int sNode, int gNode){
		visited[sNode] = true;
		if(count == 0){
			V[sNode].depth = 0;
			V[sNode].cost = 0;
			logArray[count++] = V[sNode];
		}
		if(sNode == gNode){
			pathArray.add(gNode);
			int pathNode = V[gNode].getpre();
			while(pathNode != -1){
				pathArray.add(pathNode);
				pathNode = V[pathNode].getpre();
			}
			return;
		}
		Edge e = V[sNode].link;
		while(e != null) {
			if(!visited[e.getAdj()]) {
				V[e.getAdj()].setpre(sNode);
 				V[e.getAdj()].depth = V[sNode].depth +1;
 				DecimalFormat df = new DecimalFormat("0.00");
 				double d = V[sNode].cost + e.getWeight();
 				String db = df.format(d);
 				V[e.getAdj()].cost = Float.parseFloat(db); // too many decimal digits
 				logArray[count++] = V[e.getAdj()];
				DFS(e.getAdj(),gNode);
			}
			if(visited[gNode]) return;
			else {
				e = e.next;
			}
		}
	}
	public void UniCost(int sNode, int gNode){
		PriorityQueue<Node> queue = new PriorityQueue<Node>(nodeNum, new Comparator<Object>(){
			public int compare(Object n1, Object n2) {
				Node node1 = (Node) n1;
				Node node2 = (Node) n2;
				if (node1.cost > node2.cost){
					return 1;
				}
				else if(node1.cost < node2.cost){
					return -1;
				}
				else return 0;
			}
		});
		V[sNode].cost = 0;
		V[sNode].depth = 0;
		queue.add(V[sNode]);
		while(queue.peek() != null) {
			Node exp = queue.poll();
			// System.out.println(exp.getnode() + ", " + exp.cost);	
			visited[exp.getnode()] = true;
			logArray[count++] = V[exp.getnode()];
			Edge e = V[exp.getnode()].link;
			while(e != null) {
				if(V[e.getAdj()].cost > (V[exp.getnode()].cost + e.getWeight())) {
					// System.out.println(V[e.getAdj()].getnode());
					if(queue.contains(V[e.getAdj()])) {
						queue.remove(V[e.getAdj()]);
					}
					V[e.getAdj()].depth = V[exp.getnode()].depth +1;
					DecimalFormat df = new DecimalFormat("0.00");
					double d = V[exp.getnode()].cost + e.getWeight();
					String db = df.format(d);
					V[e.getAdj()].cost = Float.parseFloat(db); // too many decimal digits
					V[e.getAdj()].setpre(exp.getnode());
					queue.add(V[e.getAdj()]);
				}
				e = e.next;
			}
		}
		pathArray.add(gNode);
		int pathNode = V[gNode].getpre();
		while(pathNode != -1){
			pathArray.add(pathNode);
			pathNode = V[pathNode].getpre();
		}
	}
	public void ccBFS(String outputFile, String outputLog){
		ArrayList<Integer> groupQue = new ArrayList<Integer>();
		Queue<Integer> que = new LinkedList<Integer>();
		int group = -1;
		for(int i = 0; i<nodeNum; i++){
			if(!visited[i]){		
				que.add(i);
				visited[i] = true;
				V[i].depth = 0;
				V[i].cost = 0;
				while(!que.isEmpty()) {
					Integer v = que.poll();
					groupQue.add(v);
					Edge e = V[v].link;
					while(e != null) {
						if(!visited[e.getAdj()])
						{
							que.add(e.getAdj());
							visited[e.getAdj()] = true;
							V[e.getAdj()].setpre(v);
							V[e.getAdj()].depth = V[v].depth +1;
						}
						e = e.next;
					}
				}
				groupQue.add(group--);
			}
		}
		try{
			File f = new File(outputFile);
			if(f.exists()) {
				//System.out.println("delete the old file");
				f.delete();
			}
			f.createNewFile();
			PrintWriter pw = new PrintWriter(new FileWriter(f));
			PriorityQueue<Integer> ccQue = new PriorityQueue<Integer>();
			for(int i = 0; i<groupQue.size(); i++) {
				if(groupQue.get(i)>=0){
					ccQue.add(groupQue.get(i));
				}
				else{
					int len = ccQue.size();
					String s = search.nodes.get(ccQue.poll());
					for(int j = 1; j<len;j++){
						s += "," + search.nodes.get(ccQue.poll());
					}
					pw.write(s+"\r\n");
					ccQue.clear();
				}
			}
			pw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		try{
			File f = new File(outputLog);
			if(f.exists()) {
				//System.out.println("delete the old file");
				f.delete();
			}
			f.createNewFile();
			PrintWriter pwl = new PrintWriter(new FileWriter(f));
			pwl.write("name, depth, group"+"\r\n");
			group = 1;
			for(int i = 0; i<groupQue.size(); i++) {
				if(groupQue.get(i)>=0){
					String s = search.nodes.get(groupQue.get(i))+","+V[groupQue.get(i)].depth+","+group;
					pwl.write(s+"\r\n");
				}
				else{
					String s ="-------------------------";
					pwl.write(s+"\r\n");
					group++;
				}
			}
			pwl.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
public class search {
	public static ArrayList<String> nodes = new ArrayList<String>();
	public static int task;
	public static File tiebreakFile;
	public static File inputFile;
	public static String sNode;
	public static String gNode;
	public static String outputPath;
	public static String outputLog;
	public static Node[] Vertex;
	public static Graph graph;
	public static BufferedReader tiebreakBr = null;
	public static BufferedReader inputBr = null;
	public static void main(String[] args){
		parseInputs(args);	
		constructGraph();
		switch(task) {
			case 1:
				graph.BFS(nodes.indexOf(sNode), nodes.indexOf(gNode));
				break;
			case 2:
				graph.DFS(nodes.indexOf(sNode), nodes.indexOf(gNode));
				break;
			case 3:
				graph.UniCost(nodes.indexOf(sNode), nodes.indexOf(gNode));
			case 4:
				graph.ccBFS(outputPath,outputLog);
		}
		if(outputPath != null && task!=4){
			printPath(graph.pathArray, outputPath);
		}
		if(outputLog != null && task!=4){
			printLog(graph.logArray, outputLog);
		}
	}
	public static void parseInputs(String[] inputs){
		int len = inputs.length;
		for(int i = 0; i < len; i++)
		{
			if (inputs[i].equals("-t"))
			{
				i++;
				if(inputs[i].toCharArray().length == 1 && (inputs[i].toCharArray()[0]-'1') < 4){
					task = (inputs[i].toCharArray()[0]-'1')+1;
				}
				else{
					String tiebreak = inputs[i];
					tiebreakFile = new File(tiebreak);
					try{
						InputStreamReader tiebreakReader = new InputStreamReader(new FileInputStream(tiebreakFile));
						tiebreakBr = new BufferedReader(tiebreakReader);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
			else if(inputs[i].equals("-s"))
			{
				i++;
				sNode = inputs[i];
			}
			else if(inputs[i].equals("-g"))
			{
				i++;
				gNode = inputs[i];
			}
			else if(inputs[i].equals("-i"))
			{
				i++;
				String input = inputs[i];
				inputFile = new File(input);
				try{
					InputStreamReader inputReader = new InputStreamReader(new FileInputStream(inputFile));
					inputBr = new BufferedReader(inputReader);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			else if(inputs[i].equals("-op")){
				i++;
				outputPath = inputs[i];
			}
			else if(inputs[i].equals("-ol")){
				i++;
				outputLog = inputs[i];
			}
			else{
				System.out.println("wrong input:"+inputs[i]);
			}
		}
	}
	public static void constructGraph(){
		try {
			String tbLine = tiebreakBr.readLine();
			int nodeNum = 0;
			while(tbLine != null){
				if(! tbLine.contains(" ")){
					nodes.add(tbLine);
					nodeNum++;
					tbLine = tiebreakBr.readLine();
				}
				else{
					System.out.println("wrong in tiebreaking file");
				}
			}
		// construct nodes of the graph
		Vertex = new Node[nodeNum];
		for(int i = 0; i < nodeNum; i++){
			Node n = new Node(i);
			Vertex[i] = n;
		}
		graph = new Graph(nodeNum, Vertex);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// construct edges of the graph
		try {
			String inputLine = inputBr.readLine();
			while(inputLine != null) {
				String[] inputEle = inputLine.split(",");
				//-----------------------------------
				Edge edge1 = new Edge(nodes.indexOf(inputEle[1]),Float.parseFloat(inputEle[2]));
				edge1.next = graph.V[nodes.indexOf(inputEle[0])].link;
				if(edge1.next != null && edge1.next.getAdj() < nodes.indexOf(inputEle[1])) {
					Edge tmp = new Edge(-1, 0);
					while(edge1.next != null && edge1.next.getAdj() < nodes.indexOf(inputEle[1])) {
						tmp.next = edge1.next;
						edge1.next = edge1.next.next;
					}
	                tmp.next.next = edge1;
				}
				else {
					graph.V[nodes.indexOf(inputEle[0])].link = edge1;
				}
				//-----------------------------------
				Edge edge2 = new Edge(nodes.indexOf(inputEle[0]),Float.parseFloat(inputEle[2]));
				edge2.next = graph.V[nodes.indexOf(inputEle[1])].link;
				if(edge2.next != null && edge2.next.getAdj() < nodes.indexOf(inputEle[0])) {
					Edge tmp = new Edge(-1, 0);
					while(edge2.next != null && edge2.next.getAdj() < nodes.indexOf(inputEle[0])) {
						tmp.next = edge2.next;
						edge2.next = edge2.next.next;
					}
	                tmp.next.next = edge2;
				}
				else {
					graph.V[nodes.indexOf(inputEle[1])].link = edge2;
				}
				//----------------------------------
				inputLine = inputBr.readLine();
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	@SuppressWarnings("resource")
	public static void printPath(ArrayList<Integer> pathArray, String outputFile){
		int len = pathArray.size();
		//System.out.println(outputFile);
		try{
			File f = new File(outputFile);
			if(f.exists()) {
				// System.out.println("delete the old file");
				f.delete();
			}
			f.createNewFile();
			PrintWriter pw = new PrintWriter(new FileWriter(f));
			for(int i = len-1; i>-1; i--) {
				String s = nodes.get(pathArray.get(i));
				// System.out.println(s);
				pw.write(s+"\r\n");
			}
			pw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}	
	public static void printLog(Node[] logArray, String logFile){
		try{
			File f = new File(logFile);
			if(f.exists()) {
				// System.out.println("delete the old file");
				f.delete();
			}
			f.createNewFile();
			PrintWriter pwl = new PrintWriter(new FileWriter(f));
			pwl.write("name depth cost"+"\r\n");
			for(int i = 0; logArray[i]!=null; i++) {
				String s = nodes.get(logArray[i].getnode())+","+logArray[i].depth+","+logArray[i].cost;
				// System.out.println(s);
				pwl.write(s+"\r\n");
			}
			pwl.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
