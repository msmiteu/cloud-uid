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

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since 23 Nov 2015
 */
public class UidFactory {

	private final LinkedBlockingQueue<CryptKernel> kernels_;
	private final Map<UidVariant, UidProvider<Uid>> providers_;
	private final Map<Integer, UidVariant> variants_;
	private byte[] key_;

	/**
	 * @param key
	 */
	public UidFactory(byte[] key) {
		this(key, 4);
	}

	/**
	 * @param key
	 * @param concurrency
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public UidFactory(byte[] key, int concurrency) {
		key_ = key;

		kernels_ = new LinkedBlockingQueue<>(concurrency);
		while (kernels_.remainingCapacity() > 0) {
			kernels_.add(new CryptKernel(this));
		}

		providers_ = new HashMap<>();
		variants_ = new HashMap<>();
		for (UidProvider provider : ServiceLoader.load(UidProvider.class)) {
			UidVariant variant = provider.canProvide();
			int mask = variant.getMask();

			if (mask > 0xf) {
				throw new IllegalArgumentException("Illegal mask, must be between 0x0 and 0xf");
			}

			providers_.put(variant, provider);

			if (variants_.put(mask, variant) != null) {
				throw new IllegalArgumentException("Duplicate variant, variant=" + variant + ", provider=" + provider);
			}
		}
	}

	/**
	 * @param uid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Uid> T resolve(String uid) {
		if (uid == null) {
			return null;
		}

		CryptKernel.RawUid raw = null;

		try {
			CryptKernel kernel = kernels_.poll(1, TimeUnit.DAYS);
			try {
				raw = kernel.decrypt(uid);
			} finally {
				kernels_.offer(kernel);
			}
		} catch (GeneralSecurityException | InterruptedException | IllegalArgumentException e) {
			return null;
		}

		if (raw == null) {
			return null;
		}

		UidProvider<Uid> provider = providers_.get(raw.variant);

		if (provider == null) {
			return null;
		}

		return (T) provider.decode(raw.buf);
	}

	/**
	 * @param uid
	 * @return
	 */
	public String encode(Uid uid) {
		if (uid == null) {
			return null;
		}

		UidVariant variant = uid.getVariant();
		UidProvider<Uid> provider = providers_.get(variant);

		if (provider == null) {
			return null;
		}

		long[] buf = new long[variant.getBlocks()];

		if (!provider.encode(uid, buf)) {
			return null;
		}

		try

		{
			CryptKernel kernel = kernels_.poll(1, TimeUnit.DAYS);
			try {
				return kernel.encrypt(variant, buf);
			} finally {
				kernels_.offer(kernel);
			}
		} catch (GeneralSecurityException | InterruptedException | IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * @return the kernels
	 */
	protected LinkedBlockingQueue<CryptKernel> getKernels() {
		return kernels_;
	}

	/**
	 * @return the key
	 */
	protected byte[] getKey() {
		return key_;
	}

	/**
	 * @param variant
	 * @return
	 */
	protected UidVariant getVariant(int variant) {
		return variants_.get(variant);
	}
}
