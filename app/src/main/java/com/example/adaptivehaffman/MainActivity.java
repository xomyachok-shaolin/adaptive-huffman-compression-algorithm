package com.example.adaptivehaffman;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("SourceLockedOrientationActivity")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        final EditText editInput = findViewById(R.id.textViewInput);
        final EditText editOutput = findViewById(R.id.textViewOutput);

        Button btnEncode = findViewById(R.id.buttonEncode);
        Button btnDecode = findViewById(R.id.buttonDecode);

        // Конвертируем Drawable в Bitmap
        final Bitmap bitmapBad = BitmapFactory.decodeResource(getResources(), R.drawable.bad);
        final Bitmap bitmapGood = BitmapFactory.decodeResource(getResources(), R.drawable.good);
        final Bitmap bitmapPoker = BitmapFactory.decodeResource(getResources(), R.drawable.poker);

        btnEncode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textInput = editInput.getText().toString();
                String temp = "";

                try {
                    temp = Tree.getEncodeString(textInput);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                editOutput.setText(temp);

                AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this);
                ImageView imageView = new ImageView(MainActivity.this);

                if (temp.length() == 0) {
                    imageView.setImageBitmap(bitmapPoker);
                } else {
                    double koefficient = textInput.length() * 8.0 / temp.length();
                    koefficient = new BigDecimal(koefficient).setScale(2, RoundingMode.HALF_UP).doubleValue();

                    if (koefficient > 1.0)
                        imageView.setImageBitmap(bitmapGood);
                    else
                        imageView.setImageBitmap(bitmapBad);

                    builder.setMessage("Коэффициент сжатия составил: " + koefficient);
                }

                builder.setView(imageView);
                builder.create();
                builder.show();
            }
        });

        btnDecode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textInput = editOutput.getText().toString();

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Ошибка ввода при декодировании!");
                builder.setMessage("Будьте любезны, необходимо иметь корректное 8-битное двоичное представление с учетом кодировки Windows-1251.");
                builder.setCancelable(true);
                builder.setPositiveButton("Хорошо!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = builder.create();

                if (textInput.length() < 8) {
                    alertDialog.show();
                } else {
                    try {
                        String temp = Tree.getDecodeString(textInput);
                        if (temp.equals(editInput.getText().toString())) {
                            builder.setTitle("Успешно!");
                            builder.setMessage("Строки совпали при декодировании.");
                            alertDialog = builder.create();
                            alertDialog.show();
                        } else
                            editInput.setText(temp);

                    } catch (Exception e) {
                        alertDialog.show();
                    }
                }
            }
        });
    }
}
