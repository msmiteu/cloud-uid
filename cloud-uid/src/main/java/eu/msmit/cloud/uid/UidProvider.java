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
public interface UidProvider<T extends Uid> {

	/**
	 * @return the type this provider can handle
	 */
	UidVariant canProvide();

	/**
	 * Decode the buffer
	 * 
	 * @param buf
	 *            the buffer
	 * @return the type
	 */
	T decode(long[] buf);

	/**
	 * Encode the {@link Uid} into long array. The most significant four bits of
	 * the first long are formatted according to the {@link UidType}.
	 * 
	 * @param cid
	 * @param buf
	 * @return true when success
	 */
	boolean encode(T cid, long[] buf);
}
