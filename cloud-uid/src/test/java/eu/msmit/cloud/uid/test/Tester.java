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
package eu.msmit.cloud.uid.test;

import org.junit.Test;

import eu.msmit.cloud.uid.Uid;
import eu.msmit.cloud.uid.Uids;

public class Tester {

	@Test
	public void basicTest() throws Exception {
		Uid uid = new TestUid();

		System.out.println(uid);
		System.out.println(Uids.encode(uid));
		System.out.println(Uids.decode(Uids.encode(uid)));
	}
}
