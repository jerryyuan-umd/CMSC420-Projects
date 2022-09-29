package cmsc420_s22; // Don't delete this line or your file won't pass the autograder

import java.util.ArrayList;

/**
 * DualList (skeleton)
 *
 * MODIFY THE FOLLOWING CLASS.
 *
 * You are free to make whatever changes you like or to create additional
 * classes and files.
 */

public class DualList<Key1 extends Comparable<Key1>, Key2 extends Comparable<Key2>> {

	// -----------------------------------------------------------------
	// Public members - You should not modify the function signatures
	// -----------------------------------------------------------------

	private ArrayList<Key1> first1, first2;
	private ArrayList<Key2> second1, second2;
	
	public DualList() {
		first1 = new ArrayList<Key1>();
		first2 = new ArrayList<Key1>();
		second1 = new ArrayList<Key2>();
		second2 = new ArrayList<Key2>();
	} // constructor
	
	public void insert(Key1 x1, Key2 x2) {
		boolean inserted = false;
		for (int i = 0; i < size(); i++) {
			if (first1.get(i).compareTo(x1) > 0) {
				first1.add(i, x1);
				second1.add(i, x2);
				inserted = true;
				break;
			} else if (first1.get(i).compareTo(x1) == 0) {
				if (second1.get(i).compareTo(x2) > 0) {
					first1.add(i, x1);
					second1.add(i, x2);
					inserted = true;
					break;
				}
			}
		}
		if (!inserted) {
			first1.add(x1);
			second1.add(x2);
		}
		inserted = false;
		for (int i = 0; i < second2.size(); i++) {
			if (second2.get(i).compareTo(x2) > 0) {
				first2.add(i, x1);
				second2.add(i, x2);
				inserted = true;
				break;
			} else if (second2.get(i).compareTo(x2) == 0) {
				if (first2.get(i).compareTo(x1) > 0) {
					first2.add(i, x1);
					second2.add(i, x2);
					inserted = true;
					break;
				}
			}
		}
		if (!inserted) {
			first2.add(x1);
			second2.add(x2);
		}
	} // insert a new pair
	
	public int size() {
		return first1.size(); 
	} // return the number of pairs
	
	public Key2 extractMinKey1() throws Exception {
		if (size() == 0) {
			throw new Exception("Attempt to extract " + 
					"from an empty list");
		}
		first1.remove(0);
		Key2 temp = second1.remove(0);
		int index = second2.indexOf(temp);
		first2.remove(index);
		second2.remove(index);
		return temp; 
	} // remove smallest by Key1 and return its Key2 value
	
	public Key1 extractMinKey2() throws Exception {
		if (size() == 0) {
			throw new Exception("Attempt to extract " + 
					"from an empty list");
		}
		second2.remove(0);
		Key1 temp = first2.remove(0);
		int index = first1.indexOf(temp);
		second1.remove(index);
		first1.remove(index);
		return temp;  
	} // remove smallest by Key2 and return its Key1 value
	
	public ArrayList<String> listByKey1() {
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < size(); i++) {
			result.add("(" + first1.get(i) + ", " + second1.get(i) + ")");
		}
		return result; 
	} // return a list sorted by Key1
	
	public ArrayList<String> listByKey2() { 
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < size(); i++) {
			result.add("(" + first2.get(i) + ", " + second2.get(i) + ")");
		}
		return result; 
	} // return a list sorted by Key2
}
