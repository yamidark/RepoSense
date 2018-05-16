package reposense.system;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class StreamGobbler extends Thread {

    private InputStream is;
    private String value;
    private byte[] buffer = new byte[1 << 13];

    public StreamGobbler(InputStream is) {
        this.is = is;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void run() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            value = baos.toString();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
