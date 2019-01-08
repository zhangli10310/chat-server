/*
* Tencent is pleased to support the open source community by making Mars available.
* Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
*
* Licensed under the MIT License (the "License"); you may not use this file except in 
* compliance with the License. You may obtain a copy of the License at
* http://opensource.org/licenses/MIT
*
* Unless required by applicable law or agreed to in writing, software distributed under the License is
* distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
* either express or implied. See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.zl.chat.msg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class MsgHeader {

    private static final Logger LOGGER = LoggerFactory.getLogger(MsgHeader.class);

    private static final int FIXED_HEADER_SKIP = 4 + 4 + 4 + 4 + 4;

    public static final int CLIENTVERSION = 200;

    public int headLength;
    public int clientVersion;
    public int cmdId;
    public int seq;

    public byte[] options;
    public byte[] body;



    public static class InvalidHeaderException extends Exception {

        public InvalidHeaderException(String message) {
            super(message);
        }
    }

    /**
     * Decode MsgHeader from InputStream
     *
     * @param is close input stream yourself
     * @return
     * @throws IOException
     */
    public boolean decode(final InputStream is) throws InvalidHeaderException {
        final DataInputStream dis = new DataInputStream(is);

        try {
            headLength = dis.readInt();
            clientVersion = dis.readInt();
            cmdId = dis.readInt();
            seq = dis.readInt();
            int bodyLen = dis.readInt();

            if (clientVersion != CLIENTVERSION) {
                throw new InvalidHeaderException("invalid client version in header, clientVersion: " + clientVersion + " packlen: " + (headLength + bodyLen));
            }

            LOGGER.debug(String.format("dump clientVersion=%d, cmdid=%d, seq=%d, packlen=%d", clientVersion, cmdId, seq, (headLength + bodyLen)));

            // read body?
            if (bodyLen > 0) {
                body = new byte[bodyLen];
                dis.readFully(body);

            } else {
                // no body?!
                switch (cmdId) {
                    case MsgConstant.CMDID_NOOPING:
                        break;

                    default:
                        throw new InvalidHeaderException("invalid header body, cmdid:" + cmdId);
                }
            }

            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public byte[] encode() throws InvalidHeaderException {
//        if (body == null && cmdId != MsgConstant.CMDID_NOOPING && cmdId != MsgConstant.CMDID_NOOPING_RESP) {
//            throw new InvalidHeaderException("invalid header body");
//        }

        final int headerLength = FIXED_HEADER_SKIP + (options == null ? 0 : options.length);
        final int bodyLength = (body == null ? 0 : body.length);
        final int packLength = headerLength + bodyLength;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(packLength);

        try {
            final DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(headerLength);
            dos.writeInt(CLIENTVERSION);
            dos.writeInt(cmdId);
            dos.writeInt(seq);
            dos.writeInt(bodyLength);

            if (options != null) {
                dos.write(options);
            }

            if (body != null) {
                dos.write(body);
            }

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                baos.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return baos.toByteArray();
    }
}
