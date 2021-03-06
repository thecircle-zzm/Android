package nl.thecirclezzm.streaming.rtmp.amf;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import nl.thecirclezzm.streaming.rtmp.Util;

/**
 * @author francois
 */
public class AmfString implements AmfData {

    private static final String TAG = "AmfString";

    private String value;
    private boolean key;
    private int size = -1;

    public AmfString() {
    }

    public AmfString(String value, boolean isKey) {
        this.value = value;
        this.key = isKey;
    }

    public AmfString(String value) {
        this(value, false);
    }

    public AmfString(boolean isKey) {
        this.key = isKey;
    }

    public static String readStringFrom(@NonNull InputStream in, boolean isKey) throws IOException {
        if (!isKey) {
            // Read past the data type byte
            in.read();
        }
        int length = Util.readUnsignedInt16(in);
        // Read string value
        byte[] byteValue = new byte[length];
        Util.readBytesUntilFull(in, byteValue);
        return new String(byteValue, StandardCharsets.US_ASCII);
    }

    public static void writeStringTo(@NonNull OutputStream out, String string, boolean isKey)
            throws IOException {
        // Strings are ASCII encoded
        byte[] byteValue = string.getBytes(StandardCharsets.US_ASCII);
        // Write the STRING data type definition (except if this String is used as a key)
        if (!isKey) {
            out.write(AmfType.STRING.getValue());
        }
        // Write 2 bytes indicating string length
        Util.writeUnsignedInt16(out, byteValue.length);
        // Write string
        out.write(byteValue);
    }

    /**
     * @return the byte size of the resulting AMF string of the specified value
     */
    public static int sizeOf(String string, boolean isKey) {
        return (isKey ? 0 : 1) + 2 + string.getBytes(StandardCharsets.US_ASCII).length;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isKey() {
        return key;
    }

    public void setKey(boolean key) {
        this.key = key;
    }

    @Override
    public void writeTo(@NonNull OutputStream out) throws IOException {
        // Strings are ASCII encoded
        byte[] byteValue = this.value.getBytes(StandardCharsets.US_ASCII);
        // Write the STRING data type definition (except if this String is used as a key)
        if (!key) {
            out.write(AmfType.STRING.getValue());
        }
        // Write 2 bytes indicating string length
        Util.writeUnsignedInt16(out, byteValue.length);
        // Write string
        out.write(byteValue);
    }

    @Override
    public void readFrom(@NonNull InputStream in) throws IOException {
        // Skip data type byte (we assume it's already read)
        int length = Util.readUnsignedInt16(in);
        size = 3 + length; // 1 + 2 + length
        // Read string value
        byte[] byteValue = new byte[length];
        Util.readBytesUntilFull(in, byteValue);
        value = new String(byteValue, StandardCharsets.US_ASCII);
    }

    @Override
    public int getSize() {
        if (size == -1) {
            size = (isKey() ? 0 : 1) + 2 + value.getBytes(StandardCharsets.US_ASCII).length;
        }
        return size;
    }
}
