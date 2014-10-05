import java.util.*;
import java.io.*;

class clause {
	ArrayList<Character> primise = new ArrayList<Character>();
	char head;
	clause(){}
	clause(clause c) {
		this.primise.addAll(c.primise);
		this.head = c.head;
	}
	public String toString() {
		String ret = Character.toString(this.head);
		if(!this.primise.isEmpty()) {
			ret += " :- "+this.primise.get(0).toString();
			for(int k=1; k<this.primise.size(); k++) {
				ret += ","+ this.primise.get(k).toString();
			}
		}
		return ret;
	}
	@Override
	public boolean equals(Object c) {
		if(c.getClass()==this.getClass()) {
			clause comp = (clause) c;
			return (this.head==comp.head && this.primise.containsAll(comp.primise) && comp.primise.containsAll(this.primise));
		} else {
			return false;
		}
	}
}
class FClog {
	ArrayList<Character> facts = new ArrayList<Character>();
	clause rule = new clause();
	char entail;
	public FClog copy() {
		FClog fc = new FClog();
		fc.facts.addAll(this.facts);
		fc.rule = new clause(this.rule);
		fc.entail = this.entail;
		return fc;
	}
}
class BClog {
	ArrayList<Character> newGoals = new ArrayList<Character>();
	clause rule = new clause();
	char goal;
}
class Resclause {
	String cnf = "";
	Resclause(){}
	Resclause(String emp) {
		this.cnf = emp;
	}
	Resclause(clause c) {
		if(c.head!='\u0000') {
			this.cnf = c.head+"";
			for(int i=0; i<c.primise.size(); i++) {
				cnf += " OR -"+c.primise.get(i);
			}
		} else {
			if(!c.primise.isEmpty()) {
				this.cnf = "-"+c.primise.get(0);
			}
			for(int i=1; i<c.primise.size(); i++) {
				this.cnf +=" OR -"+c.primise.get(i);
			}
		}
	}
}
class RESlog {
	Resclause rc1 = new Resclause();
	Resclause rc2 = new Resclause();
	Resclause addrc = new Resclause();
	String sp = "";
	RESlog(){}
	RESlog(Resclause rc1, Resclause rc2) {
		this.rc1 = rc1;
		this.rc2 = rc2;
	}
	RESlog(Resclause rc1, Resclause rc2, Resclause addrc) {
		this.rc1 = rc1;
		this.rc2 = rc2;
		this.addrc = addrc;
	}
	RESlog(String sp) {
		this.sp = sp;
	}
	public String toString() {
		String ret;
		if(this.sp.equals("")) {
			ret = rc1.cnf+" # "+rc2.cnf+" # "+addrc.cnf;
		} else {
			ret = sp;
		}
		return ret;
	}
}
public class pl {
	public static ArrayList<clause> KB = new ArrayList<clause>();
	public static ArrayList<Character> query = new ArrayList<Character>();
	public static ArrayList<String> result = new ArrayList<String>();
	public static ArrayList<FClog> FClogresult = new ArrayList<FClog>();
	public static ArrayList<ArrayList<BClog>> BClogresult = new ArrayList< ArrayList<BClog>>();
	public static ArrayList<BClog> bclog;
	public static ArrayList<RESlog> Reslogresult = new ArrayList<RESlog>();
	public static int iter = 0;
	public static String oefile;
	public static String ilfile;
	public static int task;
	public static Comparator<Character> reOrder = new Comparator<Character>(){ 
		public int compare(Character c1, Character c2) {
			if(c1.charValue() > c2.charValue())
			{ return 1; }
			else if(c1.charValue() == c2.charValue())
			{ return 0; }
			else { return -1; }
		}
	};
	public static void main(String[] args) {
		parseInput(args);
		
		switch(task) {
			case 1: PL_FC_ENTAILS(); break;
			case 2: bcEntails(); break;
			case 3: {
				resEntails(); 
				break;
			}
		}
	}
	public static void PL_FC_ENTAILS() {
		int[] count = new int[KB.size()];
		boolean[] infer = new boolean[52];
		Queue<Character> agenda = new LinkedList<Character>();
		FClog fcRecord = new FClog(); // first record
		//---------- init local variables ----------
		for(int i=0; i<KB.size();i++) {
			count[i] = KB.get(i).primise.size();
			if(KB.get(i).primise.size()==0) {
				agenda.offer(KB.get(i).head);
			}
		}
		//------------------------------------------
		while(!agenda.isEmpty()) {
			char p = agenda.poll();
			boolean hasInferred = true;
			if(p<'a') {
				hasInferred = infer[p-'A'];
				infer[p-'A'] = true;
			} else {
				hasInferred = infer[p-'a'+26];
				infer[p-'a'+26] = true;
			}
			if(!hasInferred) {
				fcRecord.facts.add(new Character(p));
				Collections.sort(fcRecord.facts, reOrder);
				for(int i=0; i<KB.size();i++) {
					if(KB.get(i).primise.contains(new Character(p))) {
						count[i]--;
						if(count[i]==0) {
							agenda.offer(new Character(KB.get(i).head));
							fcRecord.entail = KB.get(i).head;
							fcRecord.rule = new clause(KB.get(i));
							FClogresult.add(fcRecord.copy());
							fcRecord.rule = null;
						}
					}
				}
			}
		}
		//------------print out result--------------
		try{
			File f = new File(ilfile);
			if(f.exists()) {
				f.delete();
			}
			f.createNewFile();
			PrintWriter pw = new PrintWriter(new FileWriter(f));
			pw.write("<Known/Deducted facts>#Rules Fires#NewlyEntailedFacts\r\n");
			for(int i = 0; i<query.size(); i++) {
				int j = 0;
				char find = '*';
				while(j<FClogresult.size() && find!=query.get(i)) {
					int k = 0;
					String s = ""+FClogresult.get(j).facts.get(k++);
					while(k<FClogresult.get(j).facts.size()) {
						s += ", "+FClogresult.get(j).facts.get(k++);
					}
					k=0;
					s += "#"+FClogresult.get(j).rule.head+" :- "+FClogresult.get(j).rule.primise.get(k++);
					while(k<FClogresult.get(j).rule.primise.size()) {
						s += ","+FClogresult.get(j).rule.primise.get(k++);
					}
					s += " # "+FClogresult.get(j).entail;
					pw.write(s+"\r\n");
					find = FClogresult.get(j).entail;
					j++;
				}
				pw.write("-------------------------------------------------------------\r\n");
			}
			pw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		try{
			File f = new File(oefile);
			if(f.exists()) {
				f.delete();
			}
			f.createNewFile();
			PrintWriter pw = new PrintWriter(new FileWriter(f));
			for(int i = 0; i<query.size(); i++) {
				boolean get;
				if(query.get(i)<'a') {
					get = infer[query.get(i)-'A'];
				} else {
					get = infer[query.get(i)-'a'+26];
				}
				if(get) {
					pw.write("YES\r\n");
				} else {
					pw.write("NO\r\n");
				}
			}
			pw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		//------------------------------------------
	}
	public static boolean PL_BC_ENTAILS(Stack<Character> goals, boolean[] infer) {
		char g = goals.pop();
		boolean hasInferred = true;
		if(g<'a') {
			hasInferred = infer[g-'A'];
			infer[g-'A'] = true;
		} else {
			hasInferred = infer[g-'a'+26];
			infer[g-'a'+26] = true;
		}
		if(!hasInferred) {
			ArrayList<clause> Clauses = new ArrayList<clause>();
			for(int i=0; i<KB.size(); i++) {
				if(KB.get(i).head==g) {
					if(KB.get(i).primise.isEmpty()) {
						if(g<'a') {
							infer[g-'A'] = false;
						} else {
							infer[g-'a'+26] = false;
						}
						Clauses.clear(); //---------------------
						Clauses.add(KB.get(i)); //---------------------
						break; //---------------------
					} else {
						Clauses.add(KB.get(i));
					}
				}
			}
			if(Clauses.size()==0) {
				BClog log = new BClog();
				log.goal = g;
				log.newGoals.add(g);
				bclog.add(log);
			}
			for(int i=0; i<Clauses.size();i++) {
				BClog log = new BClog();
				log.goal = g;
				log.rule = Clauses.get(i);
				log.newGoals.addAll(Clauses.get(i).primise);
				bclog.add(log);
				
				Stack<Character> copygoals = new Stack<Character>();
				copygoals.addAll(goals);
				for(int j=Clauses.get(i).primise.size(); j>0; j--) {
					goals.push(Clauses.get(i).primise.get(j-1)); // interesting
				}
//				goals.addAll(Clauses.get(i).primise); // interesting
				if(goals.isEmpty()) {
					return true;
				}
				if (PL_BC_ENTAILS(goals, infer)) {
					return true;
				}
				goals.clear();
				goals.addAll(copygoals);
			}
		} else {
			BClog log = new BClog();
			log.goal = g;
			bclog.add(log);
		}
		return false;
	}
	public static void bcEntails() {
		for(int i=0; i<query.size();i++) {
//			System.out.println("query: "+query.get(i));
			bclog = new ArrayList<BClog>();
			boolean[] infer = new boolean[52];
			Stack<Character> goals = new Stack<Character>();
			goals.push(query.get(i));
			boolean find = PL_BC_ENTAILS(goals, infer);
			if(find) {
				result.add("YES");
			} else {
				result.add("NO");
			}
			BClogresult.add(bclog);
		}
		//------------print out result--------------
		try{
			File f = new File(ilfile);
			if(f.exists()) {
				f.delete();
			}
			f.createNewFile();
			PrintWriter pw = new PrintWriter(new FileWriter(f));
			pw.write("<Queue of Goals>#Relevant Rules/Fact#New Goal Introduced\r\n");
			for(int i=0; i<query.size(); i++) {
				ArrayList<BClog> curlog = BClogresult.get(i);
				for(int j=0; j<curlog.size();j++) {
					if(curlog.get(j).rule.head=='\u0000') {
						if(!curlog.get(j).newGoals.isEmpty()) {
							pw.write(curlog.get(j).goal+" # N/A # N/A\r\n");
						} else {
							pw.write(curlog.get(j).goal+" # CYCLE DETECTED # N/A\r\n");
						}
					} else {
						pw.write(curlog.get(j).goal+" # "+curlog.get(j).rule+" # ");
						if(curlog.get(j).newGoals.isEmpty()) {
							pw.write("N/A\r\n");
						} else {
							pw.write(curlog.get(j).newGoals.get(0));
							for(int l=1; l<curlog.get(j).newGoals.size();l++) {
								pw.write(", " + curlog.get(j).newGoals.get(l));
							}
							pw.write("\r\n");
						}
					}
				}
				pw.write("-------------------------------------------------------------\r\n");
			}
			pw.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		try{
			File f = new File(oefile);
			if(f.exists()) {
				f.delete();
			}
			f.createNewFile();
			PrintWriter pw = new PrintWriter(new FileWriter(f));
			for(int i = 0; i<query.size(); i++) {
				pw.write(result.get(i)+"\r\n");
			}
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void resEntails() {
		for(int i=0; i<KB.size(); i++) {
			Collections.sort(KB.get(i).primise, reOrder);
		}
		ArrayList<clause> KBtmp = new ArrayList<clause>();
		KBtmp.addAll(KB);
		for(int i=0; i<query.size(); i++){
			iter = 0;
			clause c = new clause();
			c.primise.add(query.get(i));
			KB.add(c);
			boolean plres = PL_RESOLUTION();
			if(plres) {
				result.add("YES");
			} else {
				result.add("NO");
			}
			KB = new ArrayList<clause>();
			KB.addAll(KBtmp);
			Reslogresult.add(new RESlog("-------------------------------------------------------------"));
		}
		try{
			File f = new File(ilfile);
			if(f.exists()) {
				f.delete();
			}
			f.createNewFile();
			PrintWriter pw = new PrintWriter(new FileWriter(f));
			pw.write("Resolving clause 1#Resolving clause 2#Added clause\r\n");
			for(int i=0; i<Reslogresult.size(); i++) {
				if(Reslogresult.get(i).sp.equals("")) {
					pw.write(Reslogresult.get(i)+"\r\n");
				} else {
					pw.write(Reslogresult.get(i).sp+"\r\n");
				}
			}
			pw.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		try{
			File f = new File(oefile);
			if(f.exists()) {
				f.delete();
			}
			f.createNewFile();
			PrintWriter pw = new PrintWriter(new FileWriter(f));
			for(int i = 0; i<query.size(); i++) {
				pw.write(result.get(i)+"\r\n");
			}
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static clause PL_RESOLVE(clause C1, clause C2) {
		clause C = new clause();
		C.head = C2.head;
		C.primise.addAll(C1.primise);
		for(int i=0; i<C2.primise.size(); i++) {
			if(!C.primise.contains(C2.primise.get(i)) && C2.primise.get(i)!=C1.head) {
				C.primise.add(C2.primise.get(i));
			}
		}
		Collections.sort(C.primise, reOrder);
//		if(C.head!='\u0000') {
//			System.out.print(C.head+" ");
//			for(int x=0; x<C.primise.size(); x++) {
//				System.out.print("OR -"+C.primise.get(x)+" ");
//			}
//		} else {
//			if(!C.primise.isEmpty()) {
//				System.out.print("-"+C.primise.get(0)+" ");
//			}
//			for(int x=1; x<C.primise.size(); x++) {
//				System.out.print("OR -"+C.primise.get(x)+" ");
//			}
//		}
//		System.out.print("\n");
		return C;
	}
	public static boolean PL_RESOLUTION() {
		iter++;
		Reslogresult.add(new RESlog("ITERATION = "+iter));
		ArrayList<clause> newclause = new ArrayList<clause>();
		for(int i=0; i<KB.size()-1; i++) {
			for(int j=i+1; j<KB.size(); j++) {
				clause c = new clause();
				RESlog resclause = new RESlog();
				boolean a = KB.get(j).head!='\u0000' && !KB.get(i).primise.isEmpty() && KB.get(i).primise.contains(KB.get(j).head);
				boolean b = KB.get(i).head!='\u0000' && !KB.get(j).primise.isEmpty() && KB.get(j).primise.contains(KB.get(i).head);
				if(a && !b) {
					Resclause rc1 = new Resclause(KB.get(i));
					Resclause rc2 = new Resclause(KB.get(j));
					resclause = new RESlog(rc1, rc2);
					//-----------------------------
//					if(KB.get(i).head!='\u0000') {
//						System.out.print(KB.get(i).head+" ");
//						for(int x=0; x<KB.get(i).primise.size(); x++) {
//							System.out.print("OR -"+KB.get(i).primise.get(x)+" ");
//						}
//					} else {
//						if(!KB.get(i).primise.isEmpty()) {
//							System.out.print("-"+KB.get(i).primise.get(0)+" ");
//						}
//						for(int x=1; x<KB.get(i).primise.size(); x++) {
//							System.out.print("OR -"+KB.get(i).primise.get(x)+" ");
//						}
//					}
//					System.out.print("# ");
//					
//					System.out.print(KB.get(j).head+" ");
//					for(int x=0; x<KB.get(j).primise.size(); x++) {
//						System.out.print("OR -"+KB.get(j).primise.get(x)+" ");
//					}
//					System.out.print("# ");
					//-----------------------------
					c = PL_RESOLVE(KB.get(j), KB.get(i));
					if(c.head=='\u0000' && c.primise.isEmpty()) {
						Resclause addrc = new Resclause("Empty");
						resclause.addrc = addrc;
						Reslogresult.add(resclause);
						return true;
					} else {
						Resclause addrc = new Resclause(c);
						resclause.addrc = addrc;
						Reslogresult.add(resclause);
					}
				} else if (!a && b) {
					//-----------------------------
					Resclause rc1 = new Resclause(KB.get(i));
					Resclause rc2 = new Resclause(KB.get(j));
					resclause = new RESlog(rc1, rc2);
					//-----------------------------
//					System.out.print(KB.get(i).head+" ");
//					for(int x=0; x<KB.get(i).primise.size(); x++) {
//						System.out.print("OR -"+KB.get(i).primise.get(x)+" ");
//					}
//					System.out.print("# ");
//					
//					if(KB.get(j).head!='\u0000') {
//						System.out.print(KB.get(j).head+" ");
//						for(int x=0; x<KB.get(j).primise.size(); x++) {
//							System.out.print("OR -"+KB.get(j).primise.get(x)+" ");
//						}
//					} else {
//						if(!KB.get(j).primise.isEmpty()) {
//							System.out.print("-"+KB.get(j).primise.get(0)+" ");
//						}
//						for(int x=1; x<KB.get(j).primise.size(); x++) {
//							System.out.print("OR -"+KB.get(j).primise.get(x)+" ");
//						}
//					}
//					System.out.print("# ");
					//-----------------------------
					c = PL_RESOLVE(KB.get(i), KB.get(j));
					if(c.head=='\u0000' && c.primise.isEmpty()) {
						Resclause addrc = new Resclause("Empty");
						resclause.addrc = addrc;
						Reslogresult.add(resclause);
						return true;
					} else {
						Resclause addrc = new Resclause(c);
						resclause.addrc = addrc;
						Reslogresult.add(resclause);
					}
				}
				if(!KB.contains(c) && !newclause.contains(c) && !(c.head=='\u0000' && c.primise.isEmpty())) {
					newclause.add(c);
				}
			} // end of for
		}
		if(newclause.isEmpty()) {
			return false;
		} else {
			KB.addAll(newclause);
			newclause.clear();
			return PL_RESOLUTION();
		}
	}
	public static void parseInput(String[] args) {
		for(int i=0; i<args.length; i++) {
			if(args[i].equals("-t")) {
				i++;
				task = args[i].toCharArray()[0]-'0';
			} else if(args[i].equals("-kb")) {
				String kb_file = args[++i];
				try{
					File kbFile = new File(kb_file);
					InputStreamReader kbReader = new InputStreamReader(new FileInputStream(kbFile));
					BufferedReader kbBuffer = new BufferedReader(kbReader);
					String kbClause = kbBuffer.readLine();
					while(kbClause!=null) {
						clause c = new clause();
						char[] kbliteral = kbClause.toCharArray();
						c.head = kbliteral[0];
						if(kbClause.length()>1) {
							for(int j=1; j<kbliteral.length;j++) {
								if(('A'<kbliteral[j]&&kbliteral[j]<'Z') || ('a'<kbliteral[j]&&kbliteral[j]<'z')) {
									c.primise.add(kbliteral[j]);
								}
							}
						}
						KB.add(c);
						kbClause = kbBuffer.readLine();
					}
					kbBuffer.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			} else if(args[i].equals("-q")) {
				String q_file = args[++i];
				try{
					File kbFile = new File(q_file);
					InputStreamReader qReader = new InputStreamReader(new FileInputStream(kbFile));
					BufferedReader qBuffer = new BufferedReader(qReader);
					String qLiteral = qBuffer.readLine();
					while(qLiteral!=null) {
						query.add(qLiteral.toCharArray()[0]);
						qLiteral = qBuffer.readLine();
					}
					qBuffer.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			} else if(args[i].equals("-oe")) {
				oefile = args[++i];
			} else if(args[i].equals("-ol")) {
				ilfile = args[++i];
			}
		}
	}
}