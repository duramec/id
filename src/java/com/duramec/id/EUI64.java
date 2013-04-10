package com.duramec.id;

import java.text.ParseException;
import java.util.regex.Pattern;

public class EUI64 implements Comparable<EUI64> {

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
			.compile("([0-9a-f]){16}");

	/**
	 * Minimum sortable value for a EUI64.
	 */
	public final static EUI64 min = new EUI64("0000.0000.0000.0000");

	/**
	 * Maximum sortable value for a EUI64
	 */
	public final static EUI64 max = new EUI64("ffff.ffff.ffff.ffff");

	/**
	 * "Nil" node value.
	 */
	public final static EUI64 nil = min;

	/**
	 * Takes a 16-byte hex string and inserts "." where appropriate
	 */
	public static String normalize(String stripped) {
		StringBuffer buf = new StringBuffer(19);
		buf.append(stripped, 0, 3);
		buf.append(".");
		buf.append(stripped, 4, 7);
		buf.append(".");
		buf.append(stripped, 8, 11);
		buf.append(".");
		buf.append(stripped, 12, 15);
		return buf.toString();
	}

	/**
	 * EUI64 address parser. It is lenient on capitalization as well as the
	 * placement of ':' and '.', but any other deviations from the standard will
	 * throw a ParseException.
	 * 
	 * @param addr
	 * @throws ParseException
	 */
	public static EUI64 parse(String addr) throws ParseException {
		String stripped = addr.toLowerCase().replaceAll("[\\:\\.]", "");
		if (!strippedPattern.matcher(stripped).matches()) {
			throw new ParseException("EUI64 address \"" + addr
					+ "\" does not have the correct format", 0);
		}
		return new EUI64(normalize(stripped));
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
	private EUI64(String address) {
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
	public int compareTo(EUI64 o) {
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
		return address.replaceAll(".", "");
	}

}