package br.com.lipe.automessages.proxy.message;

import br.com.lipe.automessages.message.BroadcastMessage;
import br.com.lipe.automessages.message.BroadcastType;
import br.com.lipe.automessages.message.MessageFormat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ProxyMessagePacket {

    public static final String CHANNEL = "automsg:proxy";

    private static final String MAGIC = "AutoMessages";
    private static final int PROTOCOL_VERSION = 2;
    private static final int MAX_LINES = 100;
    private static final int MAX_PAYLOAD_BYTES = 30000;

    private ProxyMessagePacket() {
    }

    public static byte[] encode(BroadcastMessage message) {
        if (message == null || message.getText().size() > MAX_LINES) {
            return new byte[0];
        }

        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(byteStream);
            output.writeUTF(MAGIC);
            output.writeInt(PROTOCOL_VERSION);
            output.writeUTF(message.getId());
            output.writeUTF(message.getType().name());
            output.writeUTF(message.getFormat().name());
            output.writeInt(message.getText().size());
            for (String line : message.getText()) {
                output.writeUTF(line);
            }
            output.writeUTF(message.getTitle());
            output.writeUTF(message.getSubtitle());
            output.writeInt(message.getFadeIn());
            output.writeInt(message.getStay());
            output.writeInt(message.getFadeOut());
            output.writeUTF(message.getBossBarColor());
            output.writeUTF(message.getBossBarStyle());
            output.writeFloat(message.getProgress());
            output.writeLong(message.getDurationSeconds());
            output.writeLong(message.getIntervalSeconds());
            output.flush();
            byte[] data = byteStream.toByteArray();
            return data.length <= MAX_PAYLOAD_BYTES ? data : new byte[0];
        } catch (IOException exception) {
            return new byte[0];
        }
    }

    public static BroadcastMessage decode(byte[] data) {
        if (data == null || data.length == 0 || data.length > MAX_PAYLOAD_BYTES) {
            return null;
        }

        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(data));
            if (!MAGIC.equals(input.readUTF()) || input.readInt() != PROTOCOL_VERSION) {
                return null;
            }

            String id = input.readUTF();
            BroadcastType type = BroadcastType.from(input.readUTF());
            MessageFormat format = MessageFormat.from(input.readUTF());
            int lineCount = input.readInt();
            if (lineCount < 0 || lineCount > MAX_LINES) {
                return null;
            }
            List<String> lines = new ArrayList<String>();
            for (int index = 0; index < lineCount; index++) {
                lines.add(input.readUTF());
            }
            return BroadcastMessage.builder(id)
                    .type(type)
                    .format(format)
                    .text(lines)
                    .title(input.readUTF())
                    .subtitle(input.readUTF())
                    .fadeIn(input.readInt())
                    .stay(input.readInt())
                    .fadeOut(input.readInt())
                    .bossBarColor(input.readUTF())
                    .bossBarStyle(input.readUTF())
                    .progress(input.readFloat())
                    .durationSeconds(input.readLong())
                    .intervalSeconds(input.readLong())
                    .build();
        } catch (IOException exception) {
            return null;
        }
    }
}
