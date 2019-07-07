package com.example.ducks.screen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
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

import static com.example.ducks.screen.Search.*;

public class Video extends Activity implements TextureView.SurfaceTextureListener {
    // Log тэг
    private static final String TAG = Video.class.getName();
    private double mVideoWidth;
    private double mVideoHeight;
    private double mDisplayWidth;
    private double mDisplayHeight;
    static double ax, ay, bx, by;
    private TextureView mTextureView;
    static String path;
    public static SimpleExoPlayer player;
    boolean paused = false;
    private static boolean second = false;
    private ProgressDialog progress;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.texture_video_crop);
        progress = new ProgressDialog(Video.this);
        progress.setMessage("Синхронизирую видео");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.setProgress(0);
        progress.show();
        calculateVideoSize();
        if (!second) {
            ax *= mVideoWidth / (double) 100;
            ay *= mVideoHeight / (double) 100;
            bx *= mVideoWidth / (double) 100;
            by *= mVideoHeight / (double) 100;
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
        double scaleX = mDisplayWidth / mVideoWidth, scaleY = mDisplayHeight / mVideoHeight;
        double scale = mDisplayHeight / Math.abs(by - ay);
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.setScale((float) (scale / scaleX), (float) (scale / scaleY));
        matrix.postTranslate((float) (-scale * ax), (float) (-scale * ay));
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
                player.setVolume(0);

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
                                    if (second) {
                                        new getPause().start();
                                    }
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
        if(progress != null)
            progress.dismiss();
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

        boolean synchronised = false;

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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (pause != null) {
                                if (pause == player.getPlayWhenReady() && synchronised) {
                                    if (pause) {
                                        player.setPlayWhenReady(false);
                                        player.seekTo((System.currentTimeMillis() + (int) Sync.deltaT) - timeStart);
                                        paused = true;
                                    } else {
                                        player.setPlayWhenReady(true);
                                    }
                                } else if (!pause && !paused) {
                                    if (Math.abs(((System.currentTimeMillis() + (int) Sync.deltaT) - timeStart) - player.getCurrentPosition()) > 300 && !synchronised) {
                                        long delta = ((System.currentTimeMillis() + (int) Sync.deltaT) - timeStart) - player.getCurrentPosition();
                                        Log.e("TIME", "" + delta + " ");
                                        player.seekTo((System.currentTimeMillis() + (int) Sync.deltaT) - timeStart + (delta < 0 ? -500 : 300));
                                        if((delta < 350 && delta > 0) || (delta > -350 && delta < 0))
                                            synchronised = true;
                                    }
                                }

                                if(synchronised && progress != null) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progress.dismiss();
                                            progress = null;
                                            player.setVolume(100);
                                        }
                                    });
                                }
                            }
                        }
                    });
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
