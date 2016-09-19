package com.gianlu.aria2android.Logging;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gianlu.aria2android.R;
import com.gianlu.aria2android.Utils;

import java.util.List;

public class LoglineAdapter extends BaseAdapter {
    private Activity context;
    private List<LoglineItem> objs;

    public LoglineAdapter(Activity context, List<LoglineItem> objs) {
        this.context = context;
        this.objs = objs;
    }

    @Override
    public int getCount() {
        return objs.size();
    }

    @Override
    public LoglineItem getItem(int i) {
        return objs.get(i);
    }

    public void clear() {
        objs.clear();
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public void addLine(LoglineItem line) {
        Utils.LogMe(context, line.getMessage(), line.getType() == LoglineItem.TYPE.ERROR);
        objs.add(line);
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }
    public void addLine(LoglineItem.TYPE type, String message) {
        addLine(new LoglineItem(type, message));
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setPadding(12, 12, 12, 12);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        final LoglineItem item = getItem(position);

        TextView type = new TextView(context);
        type.setTypeface(Typeface.DEFAULT_BOLD);
        switch (item.getType()) {
            case INFO:
                type.setText(R.string.infoTag);
                type.setTextColor(Color.BLACK);
                break;
            case WARNING:
                type.setText(R.string.warningTag);
                type.setTextColor(Color.YELLOW);
                break;
            case ERROR:
                type.setText(R.string.errorTag);
                type.setTextColor(Color.RED);
                break;
        }
        linearLayout.addView(type);
        linearLayout.addView(Utils.fastTextView(context, item.getMessage()));
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("error log", item.getMessage()));
                Utils.UIToast(context, Utils.TOAST_MESSAGES.COPIED_TO_CLIPBOARD);
            }
        });


        return linearLayout;
    }
}