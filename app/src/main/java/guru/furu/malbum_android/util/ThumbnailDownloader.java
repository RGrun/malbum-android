package guru.furu.malbum_android.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by richard on 10/4/15.
 *
 * This class defines a background thread that lives to download
 * images. It uses an interface to communicate with the UI Thread
 * in order to set the downloaded images in the RecyclerView.
 */
public class ThumbnailDownloader<T> extends HandlerThread {

    private LruCache<String, Bitmap> cache;
    private int cacheSize = 4 * 1024 * 1024; // 4mib cache


    private static final String TAG = "ThumbnailDownloader";

    private static final int MESSAGE_DOWNLOAD = 0;

    // this Handler handles incoming download requests
    private Handler requestHandler;
    private ConcurrentMap<T, String> requestMap = new ConcurrentHashMap<>();

    // this Handler is a reference to the UI thread's Handler
    private Handler responseHandler;
    private ThumbnailDownloadListener<T> thumbnailDownloadListener;

    // interface used to call methods on UI thread from background HandlerThread
    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        thumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        this.responseHandler = responseHandler;

        cache = new LruCache<>(cacheSize);

    }

    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "Got a URL: " + url);


        if(url == null) {
            requestMap.remove(target);
        } else {
            // add message to download queue
            requestMap.put(target, url);

            // create new message for handler to handle
            requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
                    .sendToTarget();
        }
    }


    @Override
    protected void onLooperPrepared() {
        requestHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                if(msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: " + requestMap.get(target));
                    handleRequest(target);
                }
            }

        };
    }

    private void handleRequest(final T target) {
        try {
            final String url = requestMap.get(target);
            final Bitmap bitmap;

            if(url == null) {
                return;
            }

            // have we already downloaded this image (is it in the cache?)
            if(cache.get(url) != null) {
                // Yes: use the cached bitmap
                bitmap = cache.get(url);
                Log.i(TAG, "Got item from cache: " + url);

            } else {
                // No: download the bitmap and stick it in the cache for later
                byte[] bitmapBytes = new ServerConnect().getUrlBytes(url);

                bitmap = BitmapFactory
                        .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);

                cache.put(url, bitmap);
                Log.i(TAG, "Put item in cache: " + url);
            }



            Log.i(TAG, "Bitmap created.");

            responseHandler.post(new Runnable() {

                // this code is run in the UI thread
                @Override
                public void run() {
                    if(requestMap.get(target) != url) {
                        return;
                    }

                    // update ImageView in main thread
                    requestMap.remove(target);
                    thumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                }
            });

        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }
    }

    public void clearQueue() {
        requestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }
}
