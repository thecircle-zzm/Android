package nl.thecirclezzm.streaming.rtmp.amf;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nl.thecirclezzm.streaming.rtmp.Util;

/**
 * AMF0 Number data type
 *
 * @author francois
 */
public class AmfNumber implements AmfData {

    /**
     * Size of an AMF number, in bytes (including type bit)
     */
    public static final int SIZE = 9;
    private double value;

    public AmfNumber(double value) {
        this.value = value;
    }

    public AmfNumber() {
    }

    public static double readNumberFrom(InputStream in) throws IOException {
        // Skip data type byte
        in.read();
        return Util.readDouble(in);
    }

    public static void writeNumberTo(OutputStream out, double number) throws IOException {
        out.write(AmfType.NUMBER.getValue());
        Util.writeDouble(out, number);
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public void writeTo(@NonNull OutputStream out) throws IOException {
        out.write(AmfType.NUMBER.getValue());
        Util.writeDouble(out, value);
    }

    @Override
    public void readFrom(@NonNull InputStream in) throws IOException {
        // Skip data type byte (we assume it's already read)
        value = Util.readDouble(in);
    }

    @Override
    public int getSize() {
        return SIZE;
    }
}
