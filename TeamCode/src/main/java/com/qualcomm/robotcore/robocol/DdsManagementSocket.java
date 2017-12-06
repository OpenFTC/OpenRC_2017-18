package com.qualcomm.robotcore.robocol;

/*
 * Copyright (c) 2017 Noah Andrews, OpenFTC, Qualcomm Technologies Inc
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * (subject to the limitations in the disclaimer below) provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of OpenFTC nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS LICENSE. THIS
 * SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;

import com.qualcomm.robotcore.util.RobotLog;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class DdsManagementSocket {
    private static final String TAG = "DDS Management Socket";
    private static final boolean DEBUG = false;
    private static final boolean VERBOSE_DEBUG = false;

    private DatagramSocket socket;

    private int sendBufferSize;
    private int receiveBufferSize;


    private boolean sendErrorReported = false;
    private boolean recvErrorReported = false;

    // Locks
    private final Object recvLock = new Object(); // only one recv() at a time
    private final Object sendLock = new Object(); // only one send() at a time
    private final Object bindCloseLock = new Object(); // serializes bind() vs close()

    @SuppressLint("DefaultLocale") public void send(RobocolDatagram message) {
        // We're not certain whether socket.send() is thread-safe or not, so we are conservative. And,
        // besides, this makes sure that any logging isn't interlaced.
        synchronized (this.sendLock) {
            try {
                if (VERBOSE_DEBUG) RobotLog.vv(TAG, "calling socket.send()");
                socket.send(message.getPacket());
                if (DEBUG) RobotLog.vv(TAG, String.format("sent packet to=%s len=%d", message.getPacket().getAddress().toString(), message.getPayloadLength()));

            } catch (RuntimeException e) {
                RobotLog.logExceptionHeader(TAG, e, "exception sending datagram");

            } catch (IOException e) {
                // Always log the first IOException for sending. After that, only keep logging if DEBUG is true
                if (!sendErrorReported) {
                    sendErrorReported = !DEBUG;
                    RobotLog.logExceptionHeader(TAG, e, "exception sending datagram");
                }
            }
        }
    }

    /**
     * Receive a RobocolDatagram packet
     * @return packet; or null if error
     */
    @SuppressLint("DefaultLocale") public @Nullable
    RobocolDatagram recv() {
        // This locking may ultimately now be unnecessary. But it's harmless, so we'll keep it for now
        synchronized (this.recvLock) {
            RobocolDatagram result = RobocolDatagram.forReceive(receiveBufferSize);
            DatagramPacket packetRecv = result.getPacket();

            try {
                // We have seen rare situations where recv() is called before the socket is bound.
                // Thus guards against same.
                if (socket == null) return null;

                // Block until a packet is received or a timeout occurs
                if (VERBOSE_DEBUG) RobotLog.vv(TAG, "calling socket.receive()");
                socket.receive(packetRecv);
                if (DEBUG) RobotLog.vv(TAG, String.format("received packet from=%s len=%d", packetRecv.getAddress().toString(), result.getPayloadLength()));

            } catch (SocketException |SocketTimeoutException e) {
                // Always log the first exception for receiving. After that, only keep logging if DEBUG is true
                if (!recvErrorReported) {
                    recvErrorReported = !DEBUG;
                    RobotLog.logExceptionHeader(TAG, e, "no packet received");
                }
                return null;

            } catch (IOException|RuntimeException e) {
                RobotLog.logExceptionHeader(TAG, e, "no packet received");
                return null;
            }

            return result;
        }
    }

}
