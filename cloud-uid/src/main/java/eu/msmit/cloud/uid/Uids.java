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
 * @since 30 Nov 2015
 */
public class Uids {

	private static final UidFactory FACTORY;

	static {
		FACTORY = new UidFactory("232015612951561435".getBytes());
	}

	/**
	 * @param uid
	 */
	public static <T extends Uid> T decode(String uid) {
		return FACTORY.resolve(uid);
	}

	/**
	 * @param uid
	 * @return
	 */
	public static String encode(Uid uid) {
		return FACTORY.encode(uid);
	}
}
