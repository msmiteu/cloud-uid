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

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since 23 Nov 2015
 */
public enum UidType implements UidVariant {

	// Time based UUID
	UUID_V1("0001", 2),

	// Random UUID
	UUID_V4("0100", 2),

	// Persistable
	PERSISTABLE("1001", 2),

	// Mongo UID
	MONGO("1010", 2);

	private final int variant_;
	private final int blocks_;

	private UidType(String variant, int blocks) {
		if (variant.length() != 4) {
			throw new IllegalArgumentException();
		}

		variant_ = Integer.parseInt(variant);
		blocks_ = blocks;
	}

	/**
	 * @return the 4 bits variant
	 */
	public int getMask() {
		return variant_;
	}

	/**
	 * @return the amount of 64 bit blocks this {@link UidType} requires
	 */
	public int getBlocks() {
		return blocks_;
	}

	/**
	 * @param variant
	 * @return
	 */
	public static UidType valueOf(int variant) {
		for (UidType type : UidType.values()) {
			if (type.variant_ == variant) {
				return type;
			}
		}
		throw new IllegalArgumentException();
	}
}
