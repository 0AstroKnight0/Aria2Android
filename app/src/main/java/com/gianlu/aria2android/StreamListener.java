package com.gianlu.aria2android;

import com.gianlu.aria2android.Logging.LoglineAdapter;
import com.gianlu.aria2android.Logging.LoglineItem;
import com.gianlu.commonutils.CommonUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class StreamListener implements Runnable {
    private static boolean _shouldStop;
    private final LoglineAdapter adapter;
    private final InputStream in;
    private final InputStream err;

    StreamListener(LoglineAdapter adapter, InputStream in, InputStream err) {
        this.adapter = adapter;
        this.in = in;
        this.err = err;
    }

    static void stop() {
        _shouldStop = true;
    }

    @Override
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        BufferedReader ereader = new BufferedReader(new InputStreamReader(err));

        while (!_shouldStop) {
            try {
                String line;
                if ((line = reader.readLine()) != null) {
                    adapter.addLine(LoglineItem.TYPE.INFO, line);
                }

                String linee;
                if ((linee = ereader.readLine()) != null) {
                    if (linee.startsWith("WARNING:")) {
                        adapter.addLine(LoglineItem.TYPE.WARNING, linee.replace("WARNING:", ""));
                    } else {
                        adapter.addLine(LoglineItem.TYPE.ERROR, linee.replace("ERROR:", ""));
                    }
                }
            } catch (IOException ex) {
                CommonUtils.logMe(adapter.getContext(), ex);
            }
        }
    }
}