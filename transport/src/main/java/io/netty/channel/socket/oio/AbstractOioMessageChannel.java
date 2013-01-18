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
package io.netty.channel.socket.oio;

import io.netty.buffer.MessageBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Abstract base class for OIO which reads and writes objects from/to a Socket
 */
public abstract class AbstractOioMessageChannel extends AbstractOioChannel {

    /**
     * @see AbstractOioChannel#AbstractOioChannel(Channel, Integer)
     */
    protected AbstractOioMessageChannel(Channel parent, Integer id) {
        super(parent, id);
    }

    @Override
    public InetSocketAddress localAddress() {
        return (InetSocketAddress) super.localAddress();
    }

    @Override
    public InetSocketAddress remoteAddress() {
        return (InetSocketAddress) super.remoteAddress();
    }

    @Override
    protected void doRead() {
        final ChannelPipeline pipeline = pipeline();
        final MessageBuf<Object> msgBuf = pipeline.inboundMessageBuffer();
        boolean closed = false;
        boolean read = false;
        boolean firedInboundBufferSuspended = false;
        try {
            // TODO: Replace with some sane number.. Maybe configurable ?
            if (!msgBuf.ensureIsWritable(1)) {
                return;
            }
            int localReadAmount = doReadMessages(msgBuf);
            if (localReadAmount > 0) {
                read = true;
            } else if (localReadAmount < 0) {
                closed = true;
            }
        } catch (Throwable t) {
            if (read) {
                read = false;
                pipeline.fireInboundBufferUpdated();
            }
            firedInboundBufferSuspended = true;
            pipeline.fireInboundBufferSuspended();
            pipeline.fireExceptionCaught(t);
            if (t instanceof IOException) {
                unsafe().close(unsafe().voidFuture());
            }
        } finally {
            if (read) {
                pipeline.fireInboundBufferUpdated();
            }
            if (!firedInboundBufferSuspended) {
                pipeline.fireInboundBufferSuspended();
            }
            if (closed && isOpen()) {
                unsafe().close(unsafe().voidFuture());
            }
        }
    }

    @Override
    protected void doFlushMessageBuffer(MessageBuf<Object> buf) throws Exception {
        while (!buf.isEmpty()) {
            doWriteMessages(buf);
        }
    }

    /**
     * Read Objects from the underlying Socket.
     *
     * @param buf           the {@link MessageBuf} into which the read objects will be written
     * @return amount       the number of objects read. This may return a negative amount if the underlying
     *                      Socket was closed
     * @throws Exception    is thrown if an error accoured
     */
    protected abstract int doReadMessages(MessageBuf<Object> buf) throws Exception;

    /**
     * Write the Objects which is hold by the {@link MessageBuf} to the underlying Socket.
     *
     * @param buf           the {@link MessageBuf} which holds the data to transfer
     * @throws Exception    is thrown if an error accoured
     */
    protected abstract void doWriteMessages(MessageBuf<Object> buf) throws Exception;
}
