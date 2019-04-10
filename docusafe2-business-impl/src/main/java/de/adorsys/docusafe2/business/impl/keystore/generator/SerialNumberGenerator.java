package de.adorsys.docusafe2.business.impl.keystore.generator;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.UUID;

public class SerialNumberGenerator {

	/**
	 * Generates a unique 40-character serial number.
	 * @return A unique serial number {@link BigInteger}
	 */
	public static BigInteger uniqueSerial() {
		DecimalFormat decimalFormat = new DecimalFormat("00000000000000000000");
		UUID random = UUID.randomUUID();
		BigInteger msb = BigInteger.valueOf(random.getMostSignificantBits());
		BigInteger lsb = BigInteger.valueOf(random.getLeastSignificantBits());
		final BigInteger bit64 = BigInteger.ONE.shiftLeft(64);
		if (msb.signum() < 0) msb = msb.add(bit64);
		if (lsb.signum() < 0)lsb = lsb.add(bit64);
		String mostSignificantNumbers = decimalFormat.format(msb);
		String leastSignificantNumbers = decimalFormat.format(lsb);
		return new BigInteger(mostSignificantNumbers + leastSignificantNumbers);
	}
}
