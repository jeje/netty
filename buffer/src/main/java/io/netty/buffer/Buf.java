/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.buffer;

/**
 * A buffer to operate on
 */
public interface Buf extends Freeable {
    /**
     * The BufType which will be handled by the Buf implementation
     */
    BufType type();

    /**
     * Return the maximal number of elements it can hold
     */
    int maxCapacity();

    /**
     * Return {@code true} if there is enough room to add num elements
     */
    boolean ensureIsWritable(int num);
}
