/**
 * Copyright 2015 Marijn Smit (info@msmit.eu)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.msmit.cloud.uid;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since 27 Nov 2015
 */
class CryptKernel {

	private static final int BLOCKSIZE = 8;

	class RawUid {
		long[] buf;
		UidVariant variant;
	}

	// 60 bits mask
	private static final long MASK = 0x0fffffffffffffffL;

	private static final String DESEDE = "DESede";
	private static final String DESEDE_TRANSFORM = DESEDE + "/CBC/NoPadding";
	private static final String HASHER = "SHA-256";

	private static final long IV_SALT = 2559135151345L;
	private static final long KEY_SALT = 1562556012354L;

	private final MessageDigest digest_;
	private final Cipher cipher_;
	private final Base64.Encoder encoder_;
	private final Base64.Decoder decoder_;
	private final UidFactory factory_;
	private SecretKeySpec secretKey_;
	private IvParameterSpec ivSpec_;

	CryptKernel(UidFactory factory) {
		try {
			digest_ = MessageDigest.getInstance(HASHER);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		try {
			cipher_ = Cipher.getInstance(DESEDE_TRANSFORM);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new RuntimeException(e);
		}

		encoder_ = Base64.getEncoder();
		decoder_ = Base64.getDecoder();

		initSharedKey(factory.getKey(), factory.getKernels().peek());
		factory_ = factory;
	}

	/**
	 * Initialize the shared key, taking the kernel provided
	 * 
	 * @param key
	 *            the key
	 * @param kernel
	 *            the base kernel
	 */
	protected void initSharedKey(byte[] key, CryptKernel kernel) {
		if (kernel != null && kernel.secretKey_ != null) {
			secretKey_ = kernel.secretKey_;
			ivSpec_ = kernel.ivSpec_;
		} else {
			kernel = this;

			byte[] secretKey = kernel.deriveKey(key, KEY_SALT, DESedeKeySpec.DES_EDE_KEY_LEN);
			byte[] iv = kernel.deriveKey(key, IV_SALT, cipher_.getBlockSize());

			secretKey_ = new SecretKeySpec(secretKey, DESEDE);
			ivSpec_ = new IvParameterSpec(iv);
		}
	}

	public String encrypt(UidVariant type, long[] buf) throws GeneralSecurityException {
		ByteBuffer bytes = ByteBuffer.allocate(type.getBlocks() * BLOCKSIZE);
		for (int l = 0; l < buf.length; l++) {
			long tmp = buf[l];
			if (l == 0) {
				long variantMask = (long) type.getMask() << 60;
				tmp = (tmp & MASK) | variantMask;
			}
			bytes.putLong(tmp);
		}

		byte[] result = null;

		cipher_.init(Cipher.ENCRYPT_MODE, secretKey_, ivSpec_);
		result = cipher_.doFinal(bytes.array());

		String base6x = base6xEncode(result);
		base6x = checksum(base6x);

		return base6x;
	}

	public RawUid decrypt(String input) throws GeneralSecurityException {
		String base6x = validate(input);
		byte[] decoded = base6xDecode(base6x);
		byte[] decrypt = null;

		cipher_.init(Cipher.DECRYPT_MODE, secretKey_, ivSpec_);
		decrypt = cipher_.doFinal(decoded);

		RawUid uid = new RawUid();
		uid.buf = new long[decrypt.length / BLOCKSIZE];
		uid.variant = null;

		ByteBuffer buf = ByteBuffer.wrap(decrypt);

		for (int l = 0; l < uid.buf.length; l++) {
			if (l == 0) {
				long tmp = buf.getLong();
				int variant = (int) (tmp >>> 60);

				uid.variant = factory_.getVariant(variant);
				uid.buf[l] = tmp & MASK;
			} else {
				uid.buf[l] = buf.getLong();
			}
		}

		return uid;
	}

	private String checksum(String base6x) {
		digest_.reset();
		byte[] buf = digest_.digest(base6x.getBytes());
		String checksum = Integer.toHexString(buf[0] & 0xff);
		if (checksum.length() == 1) {
			checksum = "0" + checksum;
		}
		return checksum.substring(0, 1) + base6x + checksum.substring(1, 2);
	}

	private String validate(String input) {
		int inputLen = input.length();

		if ((inputLen - 2) % 11 != 0) {
			throw new IllegalArgumentException();
		}

		String base6x = input.substring(1, inputLen - 1);
		String check = checksum(base6x);

		if (!input.equals(check)) {
			throw new IllegalArgumentException();
		}

		return base6x;
	}

	private String base6xEncode(byte[] result) {
		StringBuilder base64 = new StringBuilder(encoder_.encodeToString(result));
		for (int l = base64.length() - 1; l >= 0; l--) {
			char c = base64.charAt(l);
			if (c == '=') {
				base64.setLength(l);
			} else if (c == '+') {
				base64.setCharAt(l, '-');
			} else if (c == '/') {
				base64.setCharAt(l, '_');
			}
		}
		return base64.toString();
	}

	private byte[] base6xDecode(String val) {
		StringBuilder base6x = new StringBuilder(val);

		for (int l = base6x.length() - 1; l >= 0; l--) {
			char c = base6x.charAt(l);
			if (c == '-') {
				base6x.setCharAt(l, '+');
			} else if (c == '_') {
				base6x.setCharAt(l, '/');
			}
		}

		for (int p = (base6x.length() % 4); p > 0; p--) {
			base6x.append('=');
		}

		return decoder_.decode(base6x.toString());
	}

	/**
	 * Derive a key from the given input and salt
	 * 
	 * @param input
	 *            the input
	 * @param salt
	 *            the salt
	 * @param bytes
	 *            the amount of bytes to output
	 * @return the result
	 */
	byte[] deriveKey(byte[] input, long salt, int bytes) {

		ByteBuffer buf = ByteBuffer.allocate(8);
		buf.putLong(salt);

		digest_.reset();
		digest_.update(buf.array());
		digest_.update(input);

		byte[] digest = digest_.digest();
		byte[] result = new byte[bytes];
		System.arraycopy(digest, 0, result, 0, result.length);

		return result;
	}

}
