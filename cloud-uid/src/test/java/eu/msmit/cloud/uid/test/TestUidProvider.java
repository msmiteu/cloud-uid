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

import eu.msmit.cloud.uid.UidProvider;
import eu.msmit.cloud.uid.UidVariant;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since 2 Dec 2015
 */
public class TestUidProvider implements UidProvider<TestUid> {

	@Override
	public UidVariant canProvide() {
		return TestUid.VARIANT;
	}

	@Override
	public TestUid decode(long[] buf) {
		return new TestUid();
	}

	@Override
	public boolean encode(TestUid cid, long[] buf) {
		return true;
	}
}
