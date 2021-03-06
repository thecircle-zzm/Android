package nl.thecirclezzm.streaming.rtmp.amf;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author francois
 */
public class AmfBoolean implements AmfData {

    private boolean value;

    public AmfBoolean(boolean value) {
        this.value = value;
    }

    public AmfBoolean() {
    }

    public static boolean readBooleanFrom(InputStream in) throws IOException {
        // Skip data type byte (we assume it's already read)
        return in.read() == 0x01;
    }

    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public void writeTo(@NonNull OutputStream out) throws IOException {
        out.write(AmfType.BOOLEAN.getValue());
        out.write(value ? 0x01 : 0x00);
    }

    @Override
    public void readFrom(@NonNull InputStream in) throws IOException {
        value = in.read() == 0x01;
    }

    @Override
    public int getSize() {
        return 2;
    }
}
