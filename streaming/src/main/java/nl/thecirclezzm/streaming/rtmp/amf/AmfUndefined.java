/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.thecirclezzm.streaming.rtmp.amf;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author leoma
 */
public class AmfUndefined implements AmfData {

    public static void writeUndefinedTo(OutputStream out) throws IOException {
        out.write(AmfType.UNDEFINED.getValue());
    }

    @Override
    public void writeTo(@NonNull OutputStream out) throws IOException {
        out.write(AmfType.UNDEFINED.getValue());
    }

    @Override
    public void readFrom(InputStream in) {
        // Skip data type byte (we assume it's already read)
    }

    @Override
    public int getSize() {
        return 1;
    }
}
