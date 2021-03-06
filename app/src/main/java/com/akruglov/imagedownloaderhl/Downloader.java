package com.akruglov.imagedownloaderhl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by akruglov on 13.03.17.
 */

public class Downloader<T> {

    private static final int LOAD_PICTURE_MESSAGE_ID = 1;
    private static final int RESPONSE_LOAD_PICTURE_MESSAGE_ID = 2;

    private ResponseHandler mResponseHandler;
    private HandlerThread mDownloader;
    private RequestHandler<T> mRequestHandler;

    private ConcurrentHashMap<T, String> mRequestMap = new ConcurrentHashMap<>();

    public static byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public static class RequestHandler<T> extends Handler {

        private Handler mResponseHandler;
        private ConcurrentHashMap<T, String> mRequestMap;

        public RequestHandler(Looper looper, Handler responseHandler,
                              ConcurrentHashMap<T, String> requestMap) {
            super(looper);
            mResponseHandler = responseHandler;
            mRequestMap = requestMap;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == LOAD_PICTURE_MESSAGE_ID) {
                try {
                    String url = mRequestMap.get(msg.obj);
                    byte[] bitmapBytes = getUrlBytes(url);
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                    Log.i("PictureFragment", "Image downloaded");
                    Message message = mResponseHandler.obtainMessage(RESPONSE_LOAD_PICTURE_MESSAGE_ID, bitmap);
                    mResponseHandler.sendMessage(message);
                } catch (IOException e) {
                    Log.e("PictureFragment", "Error downloading image", e);
                }
            }
        }
    }

    public static class ResponseHandler extends Handler {

        private Bitmap mBitmap;
        private WeakReference<ImageView> mImage;

        public ResponseHandler() {
            super();
        }

        public void setImageView(ImageView imageView) {
            if (mBitmap != null) {
                Log.i("PictureFragment", "Already received");
                imageView.setImageDrawable(new BitmapDrawable(imageView.getResources(), mBitmap));
            } else {
                mImage = new WeakReference<ImageView>(imageView);
            }
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == RESPONSE_LOAD_PICTURE_MESSAGE_ID) {
                Log.i("PictureFragment", "Response received");
                mBitmap = (Bitmap) msg.obj;
                ImageView imageView = mImage.get();
                if (imageView != null) {
                    Log.i("PictureFragment", "Set in handle");
                    imageView.setImageDrawable(new BitmapDrawable(imageView.getResources(), mBitmap));
                }
            }
        }
    }

    public void start() {
        mResponseHandler = new ResponseHandler(); // attached to main looper

        mDownloader = new HandlerThread("downloader", Process.THREAD_PRIORITY_BACKGROUND);
        mDownloader.start();

        mRequestHandler = new RequestHandler(mDownloader.getLooper(), mResponseHandler, mRequestMap);
    }

    public void stop() {
        mRequestHandler.removeMessages(LOAD_PICTURE_MESSAGE_ID);
        mDownloader.quit();
    }

    public void setImageView(ImageView imageView) {
        mResponseHandler.setImageView(imageView);
    }

    public void downloadImageAsync(String url, T target) {
        mRequestMap.put(target, url);
        Message request = mRequestHandler.obtainMessage(LOAD_PICTURE_MESSAGE_ID, target);
        mRequestHandler.sendMessage(request);
    }
}
