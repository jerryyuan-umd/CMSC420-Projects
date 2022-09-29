package cmsc420_s22;

import java.util.ArrayList;
import java.util.LinkedList;

public class QuakeHeap<Key extends Comparable<Key>, Value> {

	class Node {
		Key key;
		Value value;
		Node left, right, parent;
		
		public Node(Key key, Value val) {
			this.key = key;
			this.value = val;
		}
	}
	ArrayList<LinkedList<Node>> roots;
	private double ratio = 0.75;
	int[] nodeCt;
	int levels;
	
	public class Locator { 
		private Node u; // the node
		private Locator(Node u) { this.u = u; } // constructor
		Node get() { return u; }
	}

	public QuakeHeap(int nLevels) {
		levels = nLevels;
		nodeCt = new int[nLevels];
		roots = new ArrayList<LinkedList<Node>>(nLevels);
		for (int i = 0; i < nLevels; i++) {
			roots.add(new LinkedList<Node>());
		}
	}
	
	public void clear() {
		for (int i = 0; i < levels; i++) {
			nodeCt[i] = 0;
			roots.get(i).clear();
		}
	}
	
	public Locator insert(Key x, Value v) {
		roots.get(0).add(0, new Node(x, v));
		nodeCt[0]++;
		return new Locator(roots.get(0).get(0)); 
	}
	
	public Key getMinKey() throws Exception {
		boolean empty = true;
		Key min = null;
		for (int i = 0; i < levels; i++) {
			if (roots.get(i).peekFirst() != null) {
				empty = false;
				min = roots.get(i).peekFirst().key;
			}
		}
		if (empty) throw new Exception("Empty heap");
		for (int i = 0; i < levels - 1; i++) {
			sort(roots.get(i));
			if (roots.get(i).size() > 0 && 
					roots.get(i).get(0).key.compareTo(min) < 0) {
				min = roots.get(i).get(0).key;
			}
			while (roots.get(i).size() >= 2) {
				Node u = roots.get(i).pop();
				Node v = roots.get(i).pop();
				Node w = link(u, v);
				roots.get(i + 1).addFirst(w);
			}
		}
		if (levels > 0) {
			sort(roots.get(levels - 1));
			if (roots.get(levels - 1).size() > 0 && 
					roots.get(levels - 1).get(0).key.compareTo(min) < 0) {
				min = roots.get(levels - 1).get(0).key;
			}
		}
		return min; 
	}
	
	/*bubble sort the linked list*/
	public void sort(LinkedList<Node> lst) {
	    if (lst.size() > 1) {
	        for (int i = 0; i < lst.size(); i++) {
	            Node curr = lst.getFirst();
	            //System.out.println(curr.key);
	            Node next = lst.get(1);
	            for (int j = 0; j < lst.size() - 1; j++) {
	                if (curr.key.compareTo(next.key) > 0) {
	                    int temp = lst.indexOf(next);
	                	lst.remove(temp);
	                	lst.add(lst.indexOf(curr), next);
	                	next = curr;
	                	curr = lst.get(temp - 1);
	                } 
	                curr = next;
	                if (next != lst.peekLast()) {
	                	next = lst.get(lst.indexOf(next) + 1);
	                }
	            } 
	        }
	    }
	}
	
	public int getMaxLevel(Locator r) {
		Node p = r.get();
		int count = 0;
		while (p.parent != null && p.parent.key.compareTo(p.key) == 0) {
			count++;
			p = p.parent;
		}
		return count; 
	}
	
	public void traverse(Node nd, ArrayList<String> lst) {
		if (nd == null) {
			lst.add("[null]");
		} else if (nd.left == null && nd.right == null) {
			lst.add("[" + nd.key + " " + nd.value + "]");
		} else {
			lst.add("(" + nd.key + ")");
			traverse(nd.left, lst);
			traverse(nd.right, lst);
		}
	}
	
	public ArrayList<String> listHeap() {
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < levels; i++) {
			if (nodeCt[i] != 0) {
				sort(roots.get(i));
				String header = "{lev: " + String.valueOf(i);
				header += " nodeCt: " + String.valueOf(nodeCt[i]) + "}";
				result.add(header);
				for (int j = 0; j < roots.get(i).size(); j++) {
					traverse(roots.get(i).get(j), result);
				}
			}
		}
		return result; 
	}
	// New functions

	public void decreaseKey(Locator r, Key newKey) throws Exception { 
		Node u = r.get(); // leaf node to be changed
		Node uChild = null;
		if (u.key.compareTo(newKey) < 0) {
			throw new Exception("Invalid key for decrease-key");
		}
		do {
			u.key = newKey;
			uChild = u; 
			u = u.parent; // move up a level
		} while (u != null && uChild == u.left);
		if(u != null) {
			cut(u);
		}
	}
	
	private int getLevel(Node u) {
		int i = 0;
		while (u.left != null) {
			u = u.left;
			i++;
		}
		return i;
	}
	
	private void cut(Node u) {
		Node uChild = u.right;
		if(uChild != null) {
			uChild.parent = null;
			u.right = null;
			roots.get(getLevel(uChild)).add(uChild);
		}
	}
	
	public Value extractMin() throws Exception {
		boolean empty = true;
		Node u = null;
		Key min = null;
		if (levels != 0) {
			for (int i = 0; i < levels; i++) {
				if (roots.get(i).peekFirst() != null) {
					empty = false;
					u = roots.get(i).peekFirst();
					min = u.key;
				}
			}
		}
		if (empty) throw new Exception("Empty heap");
		for (int i = 0; i <= levels - 1; i++) {
			sort(roots.get(i));
			if (roots.get(i).size() > 0 && 
					roots.get(i).get(0).key.compareTo(min) < 0) {
				u = roots.get(i).get(0);
				min = u.key;
			}
		}
		Value result = deleteLeftPath(u);
		mergeTrees();
		quake();
		return result; 
	}
	
	private Value deleteLeftPath(Node u) {
		Value result = null; 
		roots.get(getLevel(u)).remove(u);
		while (u != null) { // repeat all the way down
			cut(u);
			nodeCt[getLevel(u)] -= 1; // one less node on this level
			result = u.value;
			u = u.left;
		}
		return result;
	}
	
	private void mergeTrees() { // merge trees bottom-up in pairs
		for (int lev = 0; lev < levels - 1; lev++) { // process levels bottom-up
			while (roots.get(lev).size() >= 2) { // at least two trees?
				sort(roots.get(lev));
				Node u = roots.get(lev).pop();
				Node v = roots.get(lev).pop();
				Node w = link(u, v);
				roots.get(lev + 1).add(w);
			}
		}
	}
	
	private Node link(Node u, Node v) { 
		Node w = new Node(u.key, null); 
		w.right = v;
		w.left = u;
		u.parent = w;
		v.parent = w;
		nodeCt[getLevel(u) + 1]++;
		return w;
	}
	
	private void quake() {
		int lev = 0;
		while (lev < levels - 1 && nodeCt[lev + 1] <= ratio * nodeCt[lev]) {
			lev++; 
		}
		earfquake(lev);
	}
	
	//This method destroys every node above level lev
	private void earfquake(int lev) {
		for (int i = lev + 1; i < levels; i++) { //iterate thro all levels from lev to top
			nodeCt[i] = 0;
			for (int j = 0; j < roots.get(i).size(); j++) {
				LinkedList<Node> sh = obliterate(roots.get(i).get(j), lev, 
						new LinkedList<Node>());
				roots.get(lev).addAll(sh);
			}
			roots.get(i).clear();
		}
	}
	
	//This method destroys every node above level lev in the tree with root u
	private LinkedList<Node> obliterate(Node u, int lev, LinkedList<Node> newb) {
		if (u == null) {
			return newb;
		} else if (getLevel(u) == lev) {
			if (u == u.parent.left) {
				u.parent.left = null;
			} else {
				u.parent.right = null;
			}
			u.parent = null;
			newb.add(u);
			return newb;
		} else {
			return obliterate(u.left, lev, obliterate(u.right, lev, newb));
		}
	}
	
	public int size() {
		return nodeCt[0]; 
	}
	
	public void setQuakeRatio(double newRatio) throws Exception {
		if (newRatio <= 1 && newRatio >= 0.5) {
			ratio = newRatio;
		} else {
			throw new Exception("Quake ratio is outside valid bounds");
		}
	}
	
	public void setNLevels(int nl) throws Exception {
		if (nl < 1) {
			throw new Exception("Attempt to set an invalid number of levels");
		}
		int[] copiedArray = new int[nl];
		if (nl < levels) {
			earfquake(nl - 1);
			for (int i = levels - 1; i >= nl; i--) {
				roots.remove(i);
			}
			System.arraycopy(nodeCt, 0, copiedArray, 0, nl);
		} else {
			for (int i = 0; i < nl - levels; i++) {
				roots.add(new LinkedList<Node>());
			}
			System.arraycopy(nodeCt, 0, copiedArray, 0, levels);
		}
		levels = nl;
	}
}
