package com.example.ducks.screen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import static com.example.ducks.screen.MainActivity.room;
import static com.example.ducks.screen.Search.*;

public class Video extends Activity implements TextureView.SurfaceTextureListener {
    // Log тэг
    private static final String TAG = Video.class.getName();
    private float mVideoWidth;
    private float mVideoHeight;
    private float mDisplayWidth;
    private float mDisplayHeight;
    static float ax, ay, bx, by;
    private TextureView mTextureView;
    static String path;
    public static SimpleExoPlayer player;
    private static boolean second = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.texture_video_crop);
        calculateVideoSize();
        if (second) {
            ax *= mVideoWidth / (float) 100;
            ay *= mVideoHeight / (float) 100;
            bx *= mVideoWidth / (float) 100;
            by *= mVideoHeight / (float) 100;
        }
        //перевод координат из процентов
        initView();
    }

    //для полноэкранного режима
    public void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }


    //для неполноэкранного режима
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    //подготовка TextureView
    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        hideSystemUI();

        mTextureView = findViewById(R.id.textureView);
        mTextureView.setSurfaceTextureListener(this);
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        mDisplayWidth = size.x;
        mDisplayHeight = size.y;

        updateTextureViewSize();
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (mMediaPlayer != null) {
//            // Освобождаем ресурсы
//            mMediaPlayer.stop();
//            mMediaPlayer.release();
//            mMediaPlayer = null;
//        }
//    }

    //получение размеров видеофайла
    private void calculateVideoSize() {
        try {
            FileDescriptor fd = new FileInputStream(path).getFD();
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(fd);
            String height = metaRetriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String width = metaRetriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            mVideoHeight = Float.parseFloat(height);
            mVideoWidth = Float.parseFloat(width);

        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        } catch (NumberFormatException e) {
            Log.d(TAG, e.getMessage());
        }
    }


    //обрезка видео путем изменения размеров TextureView
    private void updateTextureViewSize() {
        float scaleX = mDisplayWidth / mVideoWidth, scaleY = mDisplayHeight / mVideoHeight;
        float scale = mDisplayHeight / Math.abs(by - ay);
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.setScale(scale / scaleX, scale / scaleY);
        matrix.postTranslate(-scale * ax, -scale * ay);
        mTextureView.setLayoutParams(new FrameLayout.LayoutParams((int) mDisplayWidth, (int) mDisplayHeight));
        mTextureView.setTransform(matrix);
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
        Surface surface = new Surface(surfaceTexture);

        try {
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            } //получение разрешений

            player = ExoPlayerFactory.newSimpleInstance(Video.this);
            player.setVideoSurface(surface);

            try {
                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(Video.this,
                        Util.getUserAgent(Video.this, "Exoplayer"));
                MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(path));
                player.prepare(videoSource);

                Thread.sleep(100);

                if (!second)
                    player.setPlayWhenReady(true);
                player.addListener(new Player.EventListener() {
                    @Override
                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                        if (playbackState == ExoPlayer.STATE_READY) {
                            l = System.currentTimeMillis() - l;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (second)
                                        new getPause().start();
                                    if (!second) {
                                        second = true;
                                        recreate();
                                    }
                                }
                            });
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (IllegalArgumentException e) {
            Log.d(TAG, e.getMessage());
        } catch (SecurityException e) {
            Log.d(TAG, e.getMessage());
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length != 2 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,
                        "Доступ к файлам запрещен!\nРазрешите приложению читать файлы.",
                        Toast.LENGTH_SHORT).show();
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    class getPause extends Thread {
        @Override
        public void run() {
            super.run();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(URL)
                    .client(getUnsafeOkHttpClient().build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            while (true) {
                Call<Boolean> call = retrofit.create(Service.class).getPause(Search.room);
                try {
                    Response<Boolean> response = call.execute();
                    Boolean pause = response.body();
                    //получение паузы

                    if (pause != null && pause == player.getPlayWhenReady()) {
                        if (pause) {
                            player.setPlayWhenReady(false);
                            player.seekTo((System.currentTimeMillis() + (int) Sync.deltaT) - timeStart);
                        }
                        else{
                           player.setPlayWhenReady(true);
                        }
                    }
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
