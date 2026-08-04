package br.com.lipe.automessages.proxy.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ProxyMessagePacket {

    public static final String CHANNEL = "automsg:proxy";

    private static final String MAGIC = "AutoMessages";
    private static final int PROTOCOL_VERSION = 1;
    private static final int MAX_LINES = 100;
    private static final int MAX_PAYLOAD_BYTES = 30000;

    private ProxyMessagePacket() {
    }

    public static byte[] encode(List<String> lines) {
        if (lines == null || lines.isEmpty() || lines.size() > MAX_LINES) {
            return new byte[0];
        }

        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(byteStream);
            output.writeUTF(MAGIC);
            output.writeInt(PROTOCOL_VERSION);
            output.writeInt(lines.size());
            for (String line : lines) {
                output.writeUTF(line);
            }
            output.flush();
            byte[] data = byteStream.toByteArray();
            return data.length <= MAX_PAYLOAD_BYTES ? data : new byte[0];
        } catch (IOException exception) {
            return new byte[0];
        }
    }

    public static List<String> decode(byte[] data) {
        if (data == null || data.length == 0 || data.length > MAX_PAYLOAD_BYTES) {
            return Collections.emptyList();
        }

        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(data));
            if (!MAGIC.equals(input.readUTF()) || input.readInt() != PROTOCOL_VERSION) {
                return Collections.emptyList();
            }

            int lineCount = input.readInt();
            if (lineCount <= 0 || lineCount > MAX_LINES) {
                return Collections.emptyList();
            }

            List<String> lines = new ArrayList<String>();
            for (int index = 0; index < lineCount; index++) {
                lines.add(input.readUTF());
            }
            return lines;
        } catch (IOException exception) {
            return Collections.emptyList();
        }
    }
}
