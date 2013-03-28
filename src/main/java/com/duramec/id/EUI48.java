package com.duramec.id;

import java.text.ParseException;
import java.util.regex.Pattern;

public final class EUI48 implements Comparable<EUI48> {
	/**
	 * Mac Address of node
	 */
	private final String address;

	/**
	 * Long representation of node.
	 */
	private final long node;

	/**
	 * Regular expression pattern without punctuation.
	 */
	private static final Pattern strippedPattern = Pattern
			.compile("([0-9a-f]){12}");

	/**
	 * Minimum sortable value for a EUI48.
	 */
	public final static EUI48 min = new EUI48("00:00:00:00:00:00");

	/**
	 * Maximum sortable value for a EUI48
	 */
	public final static EUI48 max = new EUI48("ff:ff:ff:ff:ff:ff");

	/**
	 * "Nil" node value.
	 */
	public final static EUI48 nil = min;

	/**
	 * Takes a 12-byte hex string and inserts ":" where appropriate
	 * 
	 * @param stripped
	 * @return
	 */
	private static String normalize(String stripped) {
		StringBuffer buf = new StringBuffer(17);
		buf.append(stripped, 0, 1);
		buf.append(":");
		buf.append(stripped, 2, 3);
		buf.append(":");
		buf.append(stripped, 4, 5);
		buf.append(":");
		buf.append(stripped, 6, 7);
		buf.append(":");
		buf.append(stripped, 8, 9);
		buf.append(":");
		buf.append(stripped, 10, 11);
		return buf.toString();
	}

	/**
	 * Mac address parser. It is lenient on capitalization as well as the
	 * placement of ':' and '.', but any other deviations from the standard will
	 * throw a ParseException.
	 * 
	 * @param addr
	 * @throws ParseException
	 */
	public static EUI48 parse(String addr) throws ParseException {
		String stripped = addr.toLowerCase().replaceAll("[\\:\\.]", "");
		if (!strippedPattern.matcher(stripped).matches()) {
			throw new ParseException("EUI48 address \"" + addr
					+ "\" does not have the correct format", 0);
		}
		return new EUI48(normalize(stripped));
	}

	/**
	 * Private constructor, used by parse(), which forces callers to use parse
	 * if they want an EUI48. This lends itself to good practice where
	 * 
	 * - ParseExceptions are handled
	 * 
	 * - the natural String representation of nodes is used
	 * 
	 * Using raw longs is thorny and should be avoided.
	 * 
	 * @param address
	 */
	private EUI48(String address) {
		this.address = address;
		this.node = Hex.parseLong(address);
	}

	/**
	 * Get long data representation.
	 * 
	 * @return
	 */
	public long asLong() {
		return node;
	}

	@Override
	public int compareTo(EUI48 o) {
		if (node < o.node)
			return -1;
		if (node > o.node)
			return 1;
		return 0;
	}

	/**
	 * String representation
	 */
	@Override
	public String toString() {
		return address;
	}

	/**
	 * String representation without ':'
	 * 
	 * @return
	 */
	public String toStringNoPunctuation() {
		return address.replaceAll(":", "");
	}

}