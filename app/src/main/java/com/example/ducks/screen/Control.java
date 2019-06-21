package com.example.ducks.screen;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.ducks.screen.MainActivity.room;
import static com.example.ducks.screen.Search.URL;
import static com.example.ducks.screen.Search.getUnsafeOkHttpClient;

public class Control extends AppCompatActivity {

    private static boolean isPaused = true;
    private static boolean isStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        ImageButton imageButton = findViewById(R.id.play);
        if (!isPaused)
            imageButton.setImageResource(R.drawable.pause);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPaused) {
                    imageButton.setImageResource(R.drawable.pause);
                    isPaused = false;
                    if (!isStarted) {
                        new SendTime().start();
                        isStarted = true;
                        for(int i = 0; i < 2; i++) {
                            java.util.Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    isPaused = true;
                                    new SetPause().start();
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    isPaused = false;
                                    new SetPause().start();
                                }
                            }, 1000);
                        }
                    }
                    else{
                        new SetPause().start();
                    }
                }
                else {
                    imageButton.setImageResource(R.drawable.play);
                    isPaused = true;
                    new SetPause().start();
                }

            }
        });
    }

    class SendTime extends Thread {
        @Override
        public void run() {
            super.run();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(URL)
                    .client(getUnsafeOkHttpClient().build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            long time = System.currentTimeMillis() + (int) Sync.deltaT + 15000;
            Call<Void> call = retrofit.create(Service.class).putStartVideo(room, time);
            try {
                call.execute();
                //отправка времени
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class SetPause extends Thread {
        @Override
        public void run() {
            super.run();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(URL)
                    .client(getUnsafeOkHttpClient().build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            Call<Void> call = retrofit.create(Service.class).setPause(room, isPaused);
            try {
                call.execute();
                //отправка паузы
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
