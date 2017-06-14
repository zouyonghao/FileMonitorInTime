package cn.studyjams.s2.sj0209.filemonitorintime;

import android.os.FileObserver;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


/**
 * Created by zyh on 2017/6/13.
 * RecursiveFileObserver for watch file changes.
 */
@SuppressWarnings(value = {"rawtypes", "unchecked"})
class RecursiveFileObserver extends FileObserver {

    /**
     * Only modification events
     */
    public static int CHANGES_ONLY = CREATE | DELETE | CLOSE_WRITE
            | DELETE_SELF | MOVE_SELF | MOVED_FROM | MOVED_TO;

    private List mObservers;
    private String mPath;
    private int mMask;

    RecursiveFileObserver(String path) {
        this(path, ALL_EVENTS);
    }

    private RecursiveFileObserver(String path, int mask) {
        super(path, mask);
        mPath = path;
        mMask = mask;
    }

    private Thread watchThread;
    private boolean running;
    private List<String> files;
    private ArrayAdapter<String> adapter;

    @Override
    public void startWatching() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                running = true;
                if (mObservers != null)
                    return;

                mObservers = new ArrayList();
                Stack stack = new Stack();
                stack.push(mPath);

                while (!stack.isEmpty() && running) {
                    try {
                        String parent = (String) stack.pop();
                        SingleFileObserver observer = new SingleFileObserver(parent, mMask);
                        mObservers.add(observer);
                        observer.startWatching();
                        File path = new File(parent);
                        File[] files = path.listFiles();
                        if (null == files)
                            continue;
                        for (File f : files) {
                            if (f.isDirectory() && !f.getName().equals(".")
                                    && !f.getName().equals("..") && !f.getName().contains(".")) {
                                stack.push(f.getPath());
                            }
                        }
                    } catch (Exception e) {
                        Log.e("WatchERROR", "error", e);
                    }
                }
            }
        }).start();

    }

    @Override
    public void stopWatching() {
        running = false;
        if (mObservers == null)
            return;

        for (int i = 0; i < mObservers.size(); i++) {
            SingleFileObserver sfo = (SingleFileObserver) mObservers.get(i);
            sfo.stopWatching();
        }

        mObservers.clear();
        mObservers = null;
    }

    @Override
    public void onEvent(int event, String path) {
        path = path.substring(path.indexOf(mPath) + mPath.length());
        if (!path.contains("null") && !this.files.contains(path)) {
            this.files.add(path);
            this.adapter.notifyDataSetChanged();
        }

        switch (event) {
            case FileObserver.ACCESS:
                Log.i("RecursiveFileObserver", "ACCESS: " + path);
                break;
            case FileObserver.ATTRIB:
                Log.i("RecursiveFileObserver", "ATTRIB: " + path);
                break;
            case FileObserver.CLOSE_NOWRITE:
                Log.i("RecursiveFileObserver", "CLOSE_NOWRITE: " + path);
                break;
            case FileObserver.CLOSE_WRITE:
                Log.i("RecursiveFileObserver", "CLOSE_WRITE: " + path);
                break;
            case FileObserver.CREATE:
                Log.i("RecursiveFileObserver", "CREATE: " + path);
                break;
            case FileObserver.DELETE:
                Log.i("RecursiveFileObserver", "DELETE: " + path);
                break;
            case FileObserver.DELETE_SELF:
                Log.i("RecursiveFileObserver", "DELETE_SELF: " + path);
                break;
            case FileObserver.MODIFY:
                Log.i("RecursiveFileObserver", "MODIFY: " + path);
                break;
            case FileObserver.MOVE_SELF:
                Log.i("RecursiveFileObserver", "MOVE_SELF: " + path);
                break;
            case FileObserver.MOVED_FROM:
                Log.i("RecursiveFileObserver", "MOVED_FROM: " + path);
                break;
            case FileObserver.MOVED_TO:
                Log.i("RecursiveFileObserver", "MOVED_TO: " + path);
                break;
            case FileObserver.OPEN:
                Log.i("RecursiveFileObserver", "OPEN: " + path);
                break;
            default:
                Log.i("RecursiveFileObserver", "DEFAULT(" + event + " : " + path);
                break;
        }
    }

    void setAdapter(ArrayAdapter<String> adapter) {
        this.adapter = adapter;
    }

    /**
     * Monitor single directory and dispatch all events to its parent, with full
     * path.
     */
    private class SingleFileObserver extends FileObserver {
        String mPath;

        public SingleFileObserver(String path) {
            this(path, ALL_EVENTS);
            mPath = path;
        }

        SingleFileObserver(String path, int mask) {
            super(path, mask);
            mPath = path;
        }

        @Override
        public void onEvent(int event, String path) {
            String newPath = mPath + "/" + path;
            RecursiveFileObserver.this.onEvent(event, newPath);
        }
    }

    void setFiles(List<String> files) {
        this.files = files;
    }

}
