package cmsc420_s22;

import java.util.ArrayList;

public class HBkdTree<LPoint extends LabeledPoint2D> {
	private int maxDiff;
	private Rectangle2D box;
	int size;
	Node root;
	
	class Node {
		Airport airport;
		Rectangle2D cell;
		Node left;
		Node right;
		Node parent;
		int cutting;
		int height;
	
		public Node(int cut, Airport pt, Rectangle2D cell) {
			cutting = cut;
			airport = pt;
			this.cell = cell;
			this.height = 0;
		}
		
		public int compareTo(Node z, int dim) {
		    int res = 0;
		    if (dim == 0) {
		    	if (airport.getX() < z.airport.getX()) {res = -1;}
		    	if (airport.getX() > z.airport.getX()) {res = 1;}
		    } else {
		    	if (airport.getY() < z.airport.getY()) {res = -1;}
		    	if (airport.getY() > z.airport.getY()) {res = 1;}
		    }
		    return res;
		}
		
		public String toString() {
			return "[" + height + " " + cutting + " " + airport.getString("full") + "]";
		}
	}
	
	public HBkdTree(int maxHeightDifference, Rectangle2D bbox) {
		maxDiff = maxHeightDifference;
		box = bbox;
		root = null;
		size = 0;
	}
	
	@SuppressWarnings("unchecked")
	public LPoint find(Point2D pt) {
		Node ret = findAux(pt, root);
		if (ret == null) {
			return null;
		}
		return (LPoint) ret.airport;	
	}
	
	private Node findAux(Point2D pt, Node root) {
		if (root == null || pt.equals(root.airport.getPoint2D())) {
			return root;
		}  
		Node left = findAux(pt, root.left);
		Node right = findAux(pt, root.right);
		if (pt.get(root.cutting) < root.airport.get(root.cutting)) {
			return left;
		} else if (pt.get(root.cutting) > root.airport.get(root.cutting)) {
			return right;
		} else if (right == null) {
			return left;
		} else {
			return right;
		}
	} 
	
	public void insert(LPoint pt) throws Exception {
		if (find(pt.getPoint2D()) != null) {
			throw new Exception("Attempt to insert a duplicate point");
		} else if (!box.contains(pt.getPoint2D())) {
			throw new Exception("Attempt to insert a point outside bounding box");
		} else {
			Node newNode = insertAux(pt, root);
			traverseUp(newNode);
			size++;
		}
	}
	
	private int getHeight(Node node) {
		if (node.left == null && node.right == null) {
			return 0;
		} else if (node.left == null) {
			return 1 + getHeight(node.right);
		} else if (node.right == null) {
			return 1 + getHeight(node.left);
		} else {
			return 1 + Math.max(getHeight(node.left), getHeight(node.right));
		}
	}
	
	private void traverseUp(Node node) {
		if (node != null) {
			node.height = getHeight(node);
			if (node.height == 0 || node.height <= maxDiff) {
				if (node != root) {
					traverseUp(node.parent);
				}
			} else if ((node.left == null && node.right.height >= maxDiff) ||
					(node.right == null && node.left.height >= maxDiff) ||
					(Math.abs(node.right.height - node.left.height) > maxDiff)) {
				Node temp = rebuild(node);
				traverseUp(temp);
			} else {
				if (node != root) {
					traverseUp(node.parent);
				}
			}
		}
	}
	
	public void preOrderTrav(Node node, ArrayList<Node> sjd) {
		if (node != null) {
			sjd.add(node);
			preOrderTrav(node.left, sjd);
			preOrderTrav(node.right, sjd);
		}
	}
	
	private void quicksrt(ArrayList<Node> list, int dim) {
	    Node temp;
	    if (list.size() > 1) {
	        for (int x = 0; x < list.size(); x++) {
	            for (int i = 0; i < list.size() - x - 1; i++) {
	                if (list.get(i).compareTo(list.get(i + 1), dim) > 0) {
	                    temp = list.get(i);
	                    list.set(i, list.get(i + 1));
	                    list.set(i + 1, temp);
	                } else if (list.get(i).compareTo(list.get(i + 1), dim) == 0) {
	                	if (list.get(i).compareTo(list.get(i + 1), (dim + 1) % 2) > 0) {
		                    temp = list.get(i);
		                    list.set(i, list.get(i + 1));
		                    list.set(i + 1, temp);
	                	}
	                }
	            }
	        }
	    }
	}
	
	private Node formTree(ArrayList<Node> arr, Rectangle2D box) {
		int i = arr.size() / 2;
		Node temp = null; 
		if (box.getWidth(0) >= box.getWidth(1)) {
			quicksrt(arr, 0);
			temp = arr.get(i);
			temp.cutting = 0;
		} else {
			quicksrt(arr, 1);
			temp = arr.get(i);
			temp.cutting = 1;
		}
		temp.cell = box;
		if (i == 0) {  // when arr.size() == 1
			temp.left = null;
			temp.right = null;
		} else if (i == arr.size() - 1) {  // when arr.size() == 2
			temp.left = formTree(new ArrayList<Node>(arr.subList(0, 1)), 
					box.leftPart(temp.cutting, temp.airport.get(temp.cutting)));
			temp.right = null;
			temp.left.parent = temp;
		} else {  // when arr.size() > 2
			temp.left = formTree(new ArrayList<Node>(arr.subList(0, i)), 
					box.leftPart(temp.cutting, temp.airport.get(temp.cutting)));
			temp.right = formTree(new ArrayList<Node>(arr.subList(i + 1, arr.size())), 
					box.rightPart(temp.cutting, temp.airport.get(temp.cutting)));
			temp.left.parent = temp;
			temp.right.parent = temp;
		}
		temp.height = getHeight(temp);
		return temp;
	}
	
	private Node rebuild(Node node) {
		ArrayList<Node> arr = new ArrayList<Node>();
		preOrderTrav(node, arr);
		Node oldParent = node.parent;
		boolean left = false;
		if (oldParent != null) {
			if (node == oldParent.left) {
				left = true;
			}
		}
		Node newRoot = formTree(arr, node.cell);
		newRoot.parent = oldParent;
		if (oldParent != null) {
			if (left) {
				oldParent.left = newRoot;
			} else {
				oldParent.right = newRoot;
			}
		} else {
			root = newRoot;
		}
		return newRoot;
	}
	
	private Node insertAux(LPoint pt, Node root) {
		if (root == null) {
			if (box.getWidth(0) >= box.getWidth(1)) {
				root = new Node(0, (Airport) pt, box);
			} else {
				root = new Node(1, (Airport) pt, box);
			}
			this.root = root;
			return root;
		} 
		Rectangle2D right = root.cell.rightPart(root.cutting, root.airport.get(root.cutting));
		Rectangle2D left = root.cell.leftPart(root.cutting, root.airport.get(root.cutting));
		if (right.contains(pt.getPoint2D())) {
			if (root.right == null) {
				if (right.getWidth(0) >= right.getWidth(1)) {
					root.right = new Node(0, (Airport) pt, right);
				} else {
					root.right = new Node(1, (Airport) pt, right);
				}
				root.right.parent = root;
				return root.right;
			} else {
				return insertAux(pt, root.right);
			}
		} else {
			if (root.left == null) {
				if (left.getWidth(0) >= left.getWidth(1)) {
					root.left = new Node(0, (Airport) pt, left);
				} else {
					root.left = new Node(1, (Airport) pt, left);
				}
				root.left.parent = root;
				return root.left;
			} else {
				return insertAux(pt, root.left);
			}
		}
	}
	
	private Node findMin(Node p, int dim) { 
		if (p == null) { return null; }  // fell out of tree?
		if (p.cutting == dim) {  // cutting dimension matches i?
			if (p.left == null) {
				return p;  // no left child?
			} else {
				return findMin(p.left, dim); // get min from left subtree
			}
		} else { // it may be in either side
			Node leftMin = findMin(p.left, dim);
			Node rightMin = findMin(p.right, dim);
			if (leftMin == null && rightMin == null) {
				return p;
			} else if (leftMin == null) {
				if (rightMin.airport.get(dim) < p.airport.get(dim)) {
					return rightMin;
				}
				return p;
			} else if (rightMin == null) {
				if (leftMin.airport.get(dim) < p.airport.get(dim)) {
					return leftMin;
				}
				return p;
			} else {
				ArrayList<Node> trie = new ArrayList<Node>();
				trie.add(leftMin);
				trie.add(rightMin);
				trie.add(p);
				quicksrt(trie, dim);
				return trie.get(0);
			}
		}
	}
	
	private Node deleteAux(Node tar) {
		Node replacement = findMin(tar.right, tar.cutting);
		boolean swish = false;
		if (replacement == null) {
			swish = true;
			replacement = findMin(tar.left, tar.cutting);
		}
		if (replacement != null) { 
			Airport newport = replacement.airport;
			tar.airport = new Airport(newport.getCode(), newport.getCity(), 
					newport.getX(), newport.getY());
			if (swish) {
				tar.right = tar.left;
				tar.left = null;
			}
			return deleteAux(replacement);
		} else if (root != tar) {   //delete leaf node
			if (tar == tar.parent.left) {
				tar.parent.left = null;
			} else {
				tar.parent.right = null;
			}
			return tar;
		} else {
			root = null;
			return root;
		}
	}
	
	private void newCells(Node root, Rectangle2D box) {
		if (root != null) {
			root.cell = box;
			int cut = root.cutting;
			newCells(root.left, root.cell.leftPart(cut, root.airport.get(cut)));
			newCells(root.right, root.cell.rightPart(cut, root.airport.get(cut)));
		}
	}
	
	public void delete(Point2D pt) throws Exception {
		Node tar = findAux(pt, root);
		if (tar == null) {
			throw new Exception("Attempt to delete a nonexistent point");
		}
		Node leaf = deleteAux(tar);
		newCells(root, box);
		traverseUp(leaf);
		size--;
	}
	
	private void preOrderBrute(Node node, ArrayList<String> arr) {
		if (node != null) {
			if (node.cutting == 0) {
				arr.add("(x=" + node.airport.get(0) + " ht=" + node.height + ") " + 
						node.airport.toString());
			} else {
				arr.add("(y=" + node.airport.get(1) + " ht=" + node.height + ") " + 
						node.airport.toString());
			}
			preOrderBrute(node.left, arr);
			preOrderBrute(node.right, arr);
		} else {
			arr.add("[]");
		}
	}
	
	public ArrayList<String> getPreorderList() {
		ArrayList<String> result = new ArrayList<String>();
		preOrderBrute(root, result);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<LPoint> orthogRangeReport(Rectangle2D query) {
		ArrayList<LPoint> result = new ArrayList<LPoint>();
		ArrayList<Node> nodes = new ArrayList<Node>();
		preOrderTrav(root, nodes);
		for (Node i : nodes) {
			if (query.contains(i.airport.getPoint2D())) {
				result.add((LPoint) i.airport);
			}
		}
		return result;
	}
	
	public int size() { 
		return size; 
	}
	
	public void clear() {
		size = 0;
		root = null;
	}
	
	public static void main(String[] args) throws Exception {
		HBkdTree<Airport> ohey = 
				new HBkdTree<Airport>(1, new Rectangle2D(new Point2D(0,0), new Point2D(10,10)));
		ohey.insert(new Airport("ATL", "seattel", 1, 5));
		Airport ap = new Airport("SFO", "seattel", 1, 9);
		ohey.insert(ap);
//		ohey.insert(new Airport("IAD", "seattel", 3, 4));
//		ohey.insert(new Airport("DFW", "seattel", 3, 8));
//		ohey.insert(new Airport("LAX", "seattel", 4, 2));
//		ohey.insert(new Airport("JFK", "seattel", 9, 3));
//		ohey.insert(new Airport("ORD", "seattel", 2, 6));
//		ohey.insert(new Airport("SFO", "seattel", 1, 9));
//		ohey.insert(new Airport("BWI", "seattel", 8, 8));
		ohey.insert(new Airport("ORD", "seattel", 2, 6));
		ohey.delete(new Point2D(1, 5));
		ohey.delete(new Point2D(2, 6));
		ohey.delete(new Point2D(1, 9));
		//ohey.insert(new Airport("FUK", "seattel", 1, 1));
//		ArrayList<HBkdTree<Airport>.Node> sjd = new ArrayList<HBkdTree<Airport>.Node>();
//		ohey.preOrderTrav(ohey.root, sjd);
		System.out.println(ohey.getPreorderList());
	}
}
