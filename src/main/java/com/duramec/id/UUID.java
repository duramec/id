/*
 * UUID.java
 *
 * Created 07.02.2003
 *
 * eaio: UUID - an implementation of the UUID specification
 * Copyright (c) 2003-2009 Johann Burkard (jb@eaio.com) http://eaio.com.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 *
 * Modified by Aaron Bernard (aaron.bernard@duramec.com)
 * Copyright (c) 2010-2012 Duramec LLC.
 *
 */
package com.duramec.id;

import com.duramec.time.T60Instant;
import com.duramec.time.T72Instant;

import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class UUID implements Comparable<UUID>, Serializable, Cloneable {

	/**
	 * The time field of the UUID.
	 * 
	 * @serial
	 */
	private long timeBits;

	/**
	 * The clock sequence and node field of the UUID.
	 * 
	 * @serial
	 */
	private long clockSeqAndNodeBits;

	/**
	 * Version 1 (Mac-address based)
	 */
	private static long versionBits = 0x1000L;

	/**
	 * Variant 2 (Leach-Salz)
	 */
	private static long variantBits = 0x8000L;

	/**
	 * Maximum long value that can be held in the payload.
	 */
	public static long maxPayload = 0x00FFFFFFFFL;

	/**
	 * Two longs constructor.
	 * 
	 * @param timeBits
	 * @param clockSeqAndNodeBits
	 */
	public UUID(long timeBits, long clockSeqAndNodeBits) {
		this.timeBits = timeBits;
		this.clockSeqAndNodeBits = clockSeqAndNodeBits;
	}

	/**
	 * Bytes constructor
	 * 
	 * @param bytes
	 */
	public UUID(byte[] bytes) {
		this(ByteBuffer.wrap(bytes, 0, 8).getLong(), ByteBuffer.wrap(bytes, 8,
				8).getLong());
	}

	/**
	 * Takes a tick count and turns it into convoluted bit fields
	 * 
	 * @param tick
	 * @return
	 */
	private final static long convolute(long tick) {
		return ((tick & 0xFFFFFFFF) << 32) // lo
				| ((tick & 0xFFFF00000000L) >>> 16) // mid
				| (versionBits | ((tick >>> 48) & 0x0FFFL)); // hi and version
	}

	/**
	 * Takes a convoluted bit field and turns it into a tick count
	 * 
	 * @param bits
	 * @return
	 */
	private final static long deconvolute(long bits) {
		return ((bits & 0xFFFFFFFF00000000L) >>> 32) // lo
				| ((bits & 0xFFFF0000L) << 16) // mid
				| ((bits & 0x0FFFL) << 48); // hi, exclude version
	}

	/**
	 * Constructor for the UUID class. Takes a node name in the form of a String
	 * mac address, a 100ns since the epoch tick count, and a 14-bit payload as
	 * a long. This payload is specified to be the clock sequence in RFC4122,
	 * however it need not be.
	 * 
	 * 
	 * @param node
	 * @param tick
	 * @param payload
	 */
	public UUID(EUI48 node, T60Instant inst, long payloadBits) {
		long tick = inst.asLong();
		this.timeBits = convolute(tick);
		this.clockSeqAndNodeBits = (0x0000FFFFFFFFFFFFL & node.asLong())
				| (((0x3FFFL & payloadBits) | variantBits) << 48);
	}
	
	public UUID(EUI48 node, T72Instant inst) {
	}
	
	public UUID(EUI64 node, T60Instant inst) {
		// create version 6
	}

	/**
	 * Copy constructor for UUID. Values of the given UUID are copied.
	 * 
	 * @param u
	 *            the UUID, may not be <code>null</code>
	 */
	public UUID(UUID u) {
		this(u.timeBits, u.clockSeqAndNodeBits);
	}

	/**
	 * Parses a textual representation of a UUID.
	 * <p>
	 * No validation is performed. If the {@link CharSequence} is shorter than
	 * 36 characters, {@link ArrayIndexOutOfBoundsException}s will be thrown.
	 * 
	 * @param s
	 *            the {@link CharSequence}, may not be <code>null</code>
	 */
	public UUID(CharSequence s) {
		this(Hex.parseLong(s.subSequence(0, 18)), Hex.parseLong(s.subSequence(
				19, 36)));
	}

	/**
	 * Compares this UUID to another Object. Throws a {@link ClassCastException}
	 * if the other Object is not an instance of the UUID class. Returns a value
	 * smaller than zero if the other UUID is "larger" than this UUID and a
	 * value larger than zero if the other UUID is "smaller" than this UUID.
	 * 
	 * @param t
	 *            the other UUID, may not be <code>null</code>
	 * @return a value &lt; 0, 0 or a value &gt; 0
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * @throws ClassCastException
	 */
	public int compareTo(UUID t) {
		if (this == t) {
			return 0;
		}
		if (timeBits > t.timeBits) {
			return 1;
		}
		if (timeBits < t.timeBits) {
			return -1;
		}
		if (clockSeqAndNodeBits > t.clockSeqAndNodeBits) {
			return 1;
		}
		if (clockSeqAndNodeBits < t.clockSeqAndNodeBits) {
			return -1;
		}
		return 0;
	}

	/**
	 * Get most significant bits in the UUID.
	 * 
	 * @return
	 */
	public final long getMostSignificantBits() {
		return timeBits;
	}

	/**
	 * Get least significant bits in the UUID.
	 * 
	 * @return
	 */
	public final long getLeastSignificantBits() {
		return clockSeqAndNodeBits;
	}

	/**
	 * Returns the node address as a <code>long</code> value.
	 * 
	 * @return
	 */
	public final long getNode() {
		return clockSeqAndNodeBits & 0x0000FFFFFFFFFFFFL;
	}

	/**
	 * Return the payload as a <code>long</code> value.
	 * 
	 * @return
	 */
	public final long getPayload() {
		// todo: if this is version 6, just return 0L
		return (clockSeqAndNodeBits & 0x3FFF000000000000L) >>> 48;
	}

	/**
	 * Return the <code>Instant</code> encoded inside this UUID.
	 */
	public final T60Instant getT60Instant() {
		long tick = deconvolute(timeBits);
		return new T60Instant(tick);
	}

	/**
	 * Version of the UUID.
	 * 
	 * @return
	 */
	public final int version() {
		return (int) ((timeBits & 0xF000L) >>> 12);
	}

	/**
	 * Variant of the UUID.
	 * 
	 * @return
	 */
	public final int variant() {
		return (int) ((clockSeqAndNodeBits & 0xC000000000000000L) >>> 62);
	}

	/**
	 * f Tweaked Serialization routine.
	 * 
	 * @param out
	 *            the ObjectOutputStream
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeLong(timeBits);
		out.writeLong(clockSeqAndNodeBits);
	}

	/**
	 * Tweaked Serialization routine.
	 * 
	 * @param in
	 *            the ObjectInputStream
	 * @throws IOException
	 */
	private void readObject(ObjectInputStream in) throws IOException {
		timeBits = in.readLong();
		clockSeqAndNodeBits = in.readLong();
	}

	/**
	 * Returns this UUID as a String.
	 * 
	 * @return a String, never <code>null</code>
	 * @see java.lang.Object#toString()
	 * @see #toAppendable(Appendable)
	 */
	@Override
	public final String toString() {
		return toAppendable(null).toString();
	}

	/**
	 * Appends a String representation of this to the given {@link StringBuffer}
	 * or creates a new one if none is given.
	 * 
	 * @param in
	 *            the StringBuffer to append to, may be <code>null</code>
	 * @return a StringBuffer, never <code>null</code>
	 * @see #toAppendable(Appendable)
	 */
	public StringBuffer toStringBuffer(StringBuffer in) {
		StringBuffer out = in;
		if (out == null) {
			out = new StringBuffer(36);
		} else {
			out.ensureCapacity(out.length() + 36);
		}
		return (StringBuffer) toAppendable(out);
	}

	/**
	 * Appends a String representation of this object to the given
	 * {@link Appendable} object.
	 * <p>
	 * For reasons I'll probably never understand, Sun has decided to have a
	 * number of I/O classes implement Appendable which forced them to destroy
	 * an otherwise nice and simple interface with {@link IOException}s.
	 * <p>
	 * I decided to ignore any possible IOExceptions in this method.
	 * 
	 * @param a
	 *            the Appendable object, may be <code>null</code>
	 * @return an Appendable object, defaults to a {@link StringBuilder} if
	 *         <code>a</code> is <code>null</code>
	 */
	public Appendable toAppendable(Appendable a) {
		Appendable out = a;
		if (out == null) {
			out = new StringBuilder(36);
		}
		try {
			Hex.append(out, (int) (timeBits >> 32)).append('-');
			Hex.append(out, (short) (timeBits >> 16)).append('-');
			Hex.append(out, (short) timeBits).append('-');
			Hex.append(out, (short) (clockSeqAndNodeBits >> 48)).append('-');
			Hex.append(out, clockSeqAndNodeBits, 12);
		} catch (IOException ex) {
			// What were they thinking?
		}
		return out;
	}

	/**
	 * Returns a hash code of this UUID. The hash code is calculated by XOR'ing
	 * the upper 32 bits of the time and clockSeqAndNode fields and the lower 32
	 * bits of the time and clockSeqAndNode fields.
	 * 
	 * @return an <code>int</code> representing the hash code
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) ((timeBits >> 32) ^ timeBits ^ (clockSeqAndNodeBits >> 32) ^ clockSeqAndNodeBits);
	}

	/**
	 * Clones this UUID.
	 * 
	 * @return a new UUID with identical values, never <code>null</code>
	 */
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException ex) {
			// One of Sun's most epic fails.
			return null;
		}
	}

	/**
	 * Compares two Objects for equality.
	 * 
	 * @see java.lang.Object#equals(Object)
	 * @param obj
	 *            the Object to compare this UUID with, may be <code>null</code>
	 * @return <code>true</code> if the other Object is equal to this UUID,
	 *         <code>false</code> if not
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof UUID)) {
			return false;
		}
		return compareTo((UUID) obj) == 0;
	}

	/**
	 * Returns the nil UUID (a UUID whose values are both set to zero).
	 * <p>
	 * Starting with version 2.0, this method does return a new UUID instance
	 * every time it is called. Earlier versions returned one instance. This has
	 * now been changed because this UUID has public, non-final instance fields.
	 * Returning a new instance is therefore more safe.
	 * 
	 * @return a nil UUID, never <code>null</code>
	 */
	public static UUID nilUUID() {
		return new UUID(0, 0);
	}

}