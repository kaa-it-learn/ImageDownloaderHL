package com.akruglov.imagedownloaderhl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Handler;
import android.os.Process;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by akruglov on 13.03.17.
 */

public class PictureFragment extends Fragment {

    private static final String PICTURE_URL = "http://kingofwallpapers.com/picture/picture-008.jpg";

    private ImageView mPicture;
//    private ResponseHandler mResponseHandler;
//    private HandlerThread mDownloader;
//    private Handler mRequestHandler;

    private Downloader mDownloader;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

//        mResponseHandler = new ResponseHandler(); // attached to main looper
//
//        mDownloader = new HandlerThread("downloader", Process.THREAD_PRIORITY_BACKGROUND);
//        mDownloader.start();
//
//        mRequestHandler = new RequestHandler(mDownloader.getLooper(), mResponseHandler);
//
//        Message request = mRequestHandler.obtainMessage(LOAD_PICTURE_MESSAGE_ID, PICTURE_URL);
//        mRequestHandler.sendMessage(request);

        mDownloader = new Downloader();
        mDownloader.start();
        mDownloader.downloadImageAsync(PICTURE_URL);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.picture_fragment, container, false);

        mPicture = (ImageView) view.findViewById(R.id.picture);

        mDownloader.setImageView(mPicture);

        Log.i("Picture fragment", "onCreateView");

        return view;
    }
}
