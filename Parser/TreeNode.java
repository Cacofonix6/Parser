/////////////////
// Angus Walsh //
// 3268157     //
// COMP3290    //
/////////////////

import scanner.ReferenceTable;

import java.io.*;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Iterator;

/**
 * TreeNode class - Builds and prints syntax tree nodes
 */
public class TreeNode {

	////////////////////// Globals //////////////////////
	private static int count = 0, section = 0;

	////////////////////// Variables //////////////////////
	
	// Actual node values
	private int nodeValue;
	private TreeNode left,middle,right;
	private StRec symbol, type;

	// Values for tree generation
	private int depth, level, leftOffset, middleOffset, rightOffset, offset;
	
	////////////////////// Constructors //////////////////////
	public TreeNode (int value) {
		nodeValue = value;
		left = null;
		middle = null;
		right = null;
		symbol = null;
		type = null;
	}
	public TreeNode (int value, StRec st) {
		this(value);
		symbol = st;
	}
	public TreeNode (int value, TreeNode l, TreeNode r) {
		this(value);
		left = l;
		right = r;
	}
	public TreeNode (int value, TreeNode l, TreeNode m, TreeNode r) {
		this(value,l,r);
		middle = m;
	}

	////////////////////// Accessors //////////////////////
	/// Actual node values
	public int getValue() { return nodeValue; }
	public TreeNode getLeft() { return left; }
	public TreeNode getMiddle() { return middle; }
	public TreeNode getRight() { return right; }
	public StRec getSymbol() { return symbol; }
	public StRec getType() { return type; }

	/// Values for tree generation
	public int getLevel() { return level; }
	public int getLeftOffset() { return leftOffset; }
	public int getMiddleOffset() { return middleOffset; }
	public int getRightOffset() { return rightOffset; }
	public int getOffset() { return offset; }

	////////////////////// Mutators //////////////////////
	/// Actual node values
	public void setValue(int value) { nodeValue = value; }
	public void setLeft(TreeNode l) { left = l; }
	public void setMiddle(TreeNode m) { middle = m; }
	public void setRight(TreeNode r) { right = r; }
	public void setSymbol(StRec st) { symbol = st; }
	public void setType(StRec st) { type = st; }

	/// Values for tree generation
	public void setOffset(int _val) { offset = _val; }
	public void setLeftOffset(int _val) { leftOffset = _val; }
	public void setMiddleOffset(int _val) { middleOffset = _val; }
	public void setRightOffset(int _val) { rightOffset = _val; }

	////////////////////// Functions //////////////////////
		
	/**
	 * Modified version of provided preorder print that prints in 10 columns
	 * and pads variable names to 7 characters.
	 * @param  tr - current node
	 * @return    - resulting string
	 */
	public static String printTree(TreeNode tr) {
		String out = "";
		out += ReferenceTable.PRINTNODE[tr.getValue()]+" ";
		count++;

		if(count >= 10) {
			out += "\n";
			count = 0;
		}

		if(tr.symbol != null){
			String name = tr.symbol.getName() + " ";
			int size = (int)Math.ceil((double)name.length()/7);
			out += name;
			int spaceLeft = (size * 7) - name.length();
			for (int i = 0; i < spaceLeft; i++){
				out += " ";
			}
			count += size;
			if(count >= 10) {
				out += "\n";
				count = 0;
			}
		}
		
		if (tr.left   != null) { out += printTree(tr.left);   }
		if (tr.middle != null) { out += printTree(tr.middle); }
		if (tr.right  != null) { out += printTree(tr.right);  }

		return out;
	}

	////////////////////// Tree Generation //////////////////////
	/// The tree generation is quite messy and by no means perfect. 
	/// It was created as a debugging tool so the descriptions may not be great
	/// but it does the job. 

	/**
	 * drawTree does a breadth first search of all the children and adds them to a queue.
	 * It then creates an output using pre-computed offsets of all the children (see setOffsets()).
	 * It creates each line in sections of "nodewidth" and then prints out to the printwriter.
	 * @param _tr  	- Root node of the tree
	 * @return 		- String of entire tree
	 */
	public static String drawTree( TreeNode _tr){
		
		// Set levels and offsets of all the nodes
		setLevels(_tr, 0);
		setOffsets(_tr, _tr.getOffset());

		int depth = depth(_tr);
		int nodeWidth = 7;
		int currentLevel = _tr.getLevel();
		int sec = 0;
		int connectionSec = 0;

		String out = "";
		String connections = "";
		String nodeInfo = "";

		// breadth first queue of all the nodes
		Queue<TreeNode> bf = new LinkedList<>();
		bf.add(_tr);

		while(bf.size() > 0){

			TreeNode current = bf.poll();

			// If the current node has a different level to the previous one 
			// then start a new line.
			if(currentLevel != current.getLevel()){
				// add the previous line to the output
				out += "\n" + nodeInfo + "\n" + connections + "\n";

				// reset variables for the new line
				currentLevel = current.getLevel();
				connections = "";
				nodeInfo = "";
				sec = 0;
				connectionSec = 0;
			}

			// each line is separated into sections of "nodewidth".
			// draw up to and including the current node
			while (sec != current.getOffset()+1){
				out += drawSection(current, sec, nodeWidth);
				nodeInfo += drawNodeInfo(current, sec, nodeWidth);
				sec++;
			}

			// draw the sections up to and including all the children
			if(current.hasChildren()){				
				while (connectionSec != current.getRightMostChild(current).getOffset()+1){
					connections += drawConnection(current, connectionSec, nodeWidth);
					connectionSec++;
				}
			}
			
			// add the children of the current node to the queue
			if(current.getLeft() != null) 	{ bf.add(current.getLeft());	} 
			if(current.getMiddle() != null) { bf.add(current.getMiddle());	}
			if(current.getRight() != null) 	{ bf.add(current.getRight());	}
		}
		
		return out + "\n" + nodeInfo ;
	}

	/**
	 * Checks if the node has children.
	 * @return 	- True if any of the children are not null
	 */
	public boolean hasChildren(){		
		return left != null || middle != null || right != null;
	}

	/**
	 * Checks if there is a right child otherwise returns the parent. 
	 * Used in the drawing of connections to children
	 * @param  _n - 
	 * @return    [description]
	 */
	public static TreeNode getRightMostChild(TreeNode _n){
		if(_n.getRight() != null){
			return _n.getRight();
		}
		return _n;
	}

	/**
	 * creates a string based on the current section of the line
	 * @param  _n         - current node
	 * @param  _section   - current section
	 * @param  _nodeWidth - width of each section
	 * @return            - section string of size "_nodeWidth"
	 */
	public static String drawSection(TreeNode _n, int _section,  int _nodeWidth){
		String out = "";

		// If the current section is a node then write the node name
		if(_n.getOffset() == _section){
			out += ReferenceTable.PRINTNODE[_n.getValue()] + " " ;			
		} else { // other wise create a blank section
			for(int i = 0; i < _nodeWidth ; i++){
				out += " ";
			}
		}		
		return out;
	}	

	/**
	 * Draws the value and type information under the node name. currently draws
	 * the symbol table code
	 * @param  _n         - current node
	 * @param  _section   - current section
	 * @param  _nodeWidth - width of each section
	 * @return            - section string of size "_nodeWidth"
	 */
	public static String drawNodeInfo(TreeNode _n, int _section,  int _nodeWidth){
		String out = "";

		if(_n.getOffset() == _section){

			if(_n.getSymbol() != null) {
				int count = 3;
				String symbol = _n.getSymbol().getCode();
				for (int i = 0; i < count; i++){					
					if(i < symbol.length()) out += symbol.charAt(i);
					else out += " ";
				}
			} else {
				out += "[i]";
			}

			out += " ";
			
			if(_n.getType() != null) {
				int count = 3;
				String type = _n.getType().getCode();
				for (int i = 0; i < count; i++){					
					if(i < type.length()) out += type.charAt(i);
					else out += " ";
				}
			} else {
				out += "[t]";
			}

		} else {		

			for(int i = 0; i < _nodeWidth ; i++){
				out += " ";
			}

		}		
		return out;
	}

	/**
	 * Draws the connection lines between parent and children 
	 * @param  _n         - current node
	 * @param  _section   - current section
	 * @param  _nodeWidth - width of each section
	 * @return            - section string of size "_nodeWidth"
	 */
	public static String drawConnection(TreeNode _n, int _section, int _nodeWidth){
		
		if(_n.getLeft() != null &&_n.getLeft().getOffset() == _section){
			return "      /";	
		} 

		if(_n.getOffset() == _section){
			if(_n.getMiddle() != null)
				if(_n.getLeft() == null)
					return "   |---";
				else
					return "---|---";
			else if(_n.getLeft() != null && _n.getRight() == null)
				return "/      ";
			else if(_n.getLeft() == null && _n.getRight() != null)
				return "      \\";
			else if(_n.getLeft() != null && _n.getRight() != null)
				return "---^---";
			else
				return "       ";
			
		} 

		if(_n.getRight() != null && _n.getRight().getOffset() == _section){
			return "\\      ";
		}
		if(_n.getLeft() != null &&_section > _n.getLeft().getOffset() && _section <_n.getOffset()){
			return "-------";
		}
		if(_n.getRight() != null && _section < _n.getRight().getOffset() && _section > _n.getOffset()){
			return "-------";
		}
		return "       ";
	}	

	/**
	 * Recursively sets the depth of each node
	 * @param  _n - tree node
	 * @return    - depth value
	 */
	public static int depth(TreeNode _n){
		if (_n == null) return 0;

		_n.depth = 1 + Math.max(depth(_n.left), Math.max(depth(_n.middle), depth(_n.right)));
		return _n.depth;
	}

	/**
	 * Recursively sets the level of each node
	 * @param _n     - tree node
	 * @param _level - current level
	 */
	public static void setLevels(TreeNode _n, int _level){
		if (_n == null) return;

		_n.level = _level;
		setLevels(_n.left, _level+1);
		setLevels(_n.middle, _level+1);
		setLevels(_n.right, _level+1);

	}

	/**
	 * Recursively sets the offset of each child. Each nodes offset is based on the offset
	 * of its left child and the left child of its middle child. It also adds the offset
	 * of right children and the right child of its middle child to a total offset to pass
	 * to the parent as the parent will need to take right children into account.
	 * @param  _n             - current node
	 * @param  _currentOffset - cumulative offset for parents
	 * @return                - offset value
	 */
	private static int setOffsets(TreeNode _n, int _currentOffset){

		if(_n == null) return 0;	

		int totOff = 0; 				// total offset of all children
		int offset = _currentOffset;	// current nodes offset

		// set and add the offset of left child to this nodes offset and total offset
		if(_n.getLeft() != null) 	{ 
			_n.setLeftOffset(1 + setOffsets(_n.getLeft(), offset));
			offset += _n.getLeftOffset();
			totOff += _n.getLeftOffset();
		} 

		int rightOffset = 0;	// offset of right children

		// set and add the offset of the middle child to this nodes offset and total offset
		if(_n.getMiddle() != null) { 	
			totOff += setOffsets(_n.getMiddle(), offset);;
			offset += _n.getMiddle().getLeftOffset();
			rightOffset += _n.getMiddle().getRightOffset();
		}

		// set and add the offset of the right child to the total offset only
		if(_n.getRight() != null) 	{ 
			_n.setRightOffset(1 + setOffsets(_n.getRight(), rightOffset + offset + 1));
			totOff += _n.getRightOffset();
		}

		// set the current nodes offset
		_n.setOffset(offset);

		// return the total offset to the parent
		return totOff;
	}
}