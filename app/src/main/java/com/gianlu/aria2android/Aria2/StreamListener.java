package com.gianlu.aria2android.Aria2;

import com.gianlu.aria2android.BuildConfig;
import com.gianlu.commonutils.Logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamListener extends Thread {
    private static final Pattern UNRECOGNIZED_OPTION_PATTERN = Pattern.compile("aria2c: unrecognized option `(.*?)'");
    private final InputStream in;
    private final InputStream err;
    private final IStreamListener listener;
    private volatile boolean _shouldStop;

    public StreamListener(InputStream in, InputStream err, IStreamListener listener) {
        this.in = in;
        this.err = err;
        this.listener = listener;
    }

    public void stopSafe() {
        _shouldStop = true;
    }

    @Override
    public void run() {
        BufferedReader in = new BufferedReader(new InputStreamReader(this.in));
        BufferedReader err = new BufferedReader(new InputStreamReader(this.err));

        while (!_shouldStop) {
            try {
                String inLine;
                if ((inLine = in.readLine()) != null) {
                    listener.onNewLogLine(new Logging.LogLine(Logging.LogLine.Type.INFO, inLine));
                    if (BuildConfig.DEBUG) System.out.println("aria2c: " + inLine);
                }

                String errLine;
                if ((errLine = err.readLine()) != null) {
                    if (errLine.startsWith("WARNING:")) {
                        listener.onNewLogLine(new Logging.LogLine(Logging.LogLine.Type.WARNING, errLine.replace("WARNING:", "")));
                    } else if (errLine.startsWith("ERROR:")) {
                        listener.onNewLogLine(new Logging.LogLine(Logging.LogLine.Type.ERROR, errLine.replace("ERROR:", "")));
                    } else {
                        if (errLine.contains("unrecognized option")) {
                            handleUnrecognizedOption(errLine);
                        } else {
                            listener.unknownLogLine(errLine);
                        }
                    }

                    if (BuildConfig.DEBUG) System.err.println("aria2c: " + errLine);
                }
            } catch (IOException ex) {
                if (BuildConfig.DEBUG)
                    ex.printStackTrace(); // FIXME: "read failed" when terminating the process
                listener.onNewLogLine(new Logging.LogLine(Logging.LogLine.Type.ERROR, "Failed parsing the log: " + ex.getMessage()));
            }
        }
    }

    private void handleUnrecognizedOption(String line) {
        Matcher matcher = UNRECOGNIZED_OPTION_PATTERN.matcher(line);
        if (matcher.find()) {
            String option = matcher.group(1);
            listener.onNewLogLine(new Logging.LogLine(Logging.LogLine.Type.ERROR, "Unrecognized option: " + option));
        }

        listener.onTerminated();
    }

    public interface IStreamListener {
        void onNewLogLine(Logging.LogLine line);

        void onTerminated();

        void unknownLogLine(String line);
    }
}