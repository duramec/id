package com.duramec.id;


public final class Node
{
	/**
	 * Mac Address of node
	 */
	private final String address;
	// todo: change this to eui48 and eui64 support
	// zigbee uses this
	
	/**
	 * Long representation of node.
	 */
	private final long node;
	
	/**
	 * Minimum sortable value for a Node.
	 */
	public final static Node min = new Node("00:00:00:00:00:00");
	
	/**
	 * Maximum sortable value for a Node.
	 */
	public final static Node max = new Node("ff:ff:ff:ff:ff:ff");
	
	/**
	 * "Nil" node value.
	 */
	public final static Node nil = min;
		
	/**
	 * Mac address constructor.
	 * 
	 * @param addr
	 */
	public Node(String addr) {
		this.address = addr;
		this.node = Hex.parseLong(addr);
	}
	
	/**
	 * Get long data representation.
	 * 
	 * @return
	 */
	public long asLong() {
		return node;
	}
	
	/**
	 * String representation
	 */
	public String toString() {
		return address;
	}
	
}