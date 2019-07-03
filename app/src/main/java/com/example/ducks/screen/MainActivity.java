package com.example.ducks.screen;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button main, wall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        main = findViewById(R.id.main);
        wall = findViewById(R.id.wall);
        final int px = convertDpToPixel(90, MainActivity.this);

        main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.checked));
                main.setPadding(px, 0, 0,0);
                startActivity(new Intent(MainActivity.this, Main.class));
            }
        });

        wall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wall.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.checked));
                wall.setPadding( px, 0, 0,0);
                startActivity(new Intent(MainActivity.this, Search.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        main.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.rectangle));
        main.setPadding(0,0,0,0);
        wall.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.rectangle));
        wall.setPadding(0,0,0,0);
    }

    public static int convertDpToPixel(int dp, Context context){
        return dp * (context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
