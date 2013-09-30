package nodjo.fcr.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import android.util.Log;

public class StreamUtils {

    private final static String TAG = StreamUtils.class.getSimpleName();

    public static String convertStreamToString(InputStream is, int totalLength) throws IOException {
        /*
         * To convert the InputStream to String we use the
         * Reader.read(char[] buffer) method. We iterate until the
         * Reader return -1 which means there's no more data to
         * read. We use the StringWriter class to produce the string.
         */
        final StringWriter writer = new StringWriter(totalLength); // TODO optim don't instantiate
                                                                   // one each time
        final char[] buffer = new char[4096];
        final BufferedReader mReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        try {
            int n;
            while ((n = mReader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
                if (Thread.interrupted()) { // TODO is this useful?
                    Log.w(TAG, "interrupted!!");
                    return null;
                }
            }
        } finally {
            mReader.close();
        }
        return writer.toString();
    }
}