package com.example.Sparikin2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import com.example.Sparikin2.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    SurfaceView sur;
    TextView tvTime, tvMatrixSize;
    Spinner spn;
    ArrayAdapter spnAdp;
    SeekBar sbMatrixSize;
    double[][] matrix;
    int MatrixSize;
    Bitmap res;
    Bitmap bmp;
    Thread thread;
    Thread[] threads;
    Worker[] workers;
    boolean stopper = false;
    boolean fullStopper = false;
    Thread sleepper;
    RadioButton rbBox;
    RadioButton rbGaus;
    double sum;
    double s;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvMatrixSize = findViewById(R.id.tvMatrixSize);
        sur = findViewById(R.id.sfvImage);
        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.disk);
        Drawable draw = new BitmapDrawable(getResources(), image);
        Context cts = this;
        sbMatrixSize = findViewById(R.id.sbMatrixSize);
        spnAdp = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        spnAdp.add(getResources().getStringArray(R.array.threadsCount));
        sur.setForeground(draw);
        spn = findViewById(R.id.spnThreadsCount);

        MatrixSize = sbMatrixSize.getProgress();
        MatrixSize = 3;
        tvMatrixSize.setText("Matrix size: " + String.valueOf(MatrixSize));
        rbBox = findViewById(R.id.rbBoxBlur);
        rbGaus = findViewById(R.id.rbGausBlur);
        tvTime=findViewById(R.id.textView);
        tvTime.setText("");
        sbMatrixSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {//event when seekbar value is changer
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                MatrixSize = sbMatrixSize.getProgress();
                tvMatrixSize.setText("Matrix size: " + String.valueOf(MatrixSize));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    double sumM(double[][] matrix)
    {
        double sum = 0;
        for (int i = 0; i < matrix.length; i++)
        {
            for (int j = 0; j < matrix.length; j++)
            {
                sum += matrix[i][j];
            }
        }
        return sum;
    }
    double[][] fillMatrix()
    {
        double[][] m = new double[MatrixSize][MatrixSize];
        double e;
        double g1;
        double x, y;
        int median = MatrixSize / 2;
        for (int i = 0; i < MatrixSize; i++)
        {
            for (int j = 0; j < MatrixSize; j++)
            {

                g1 = 1.0D / (2 * Math.PI * (s*s));
                x = Math.pow((i-median), 2);
                y = Math.pow((j-median), 2);
                e = Math.pow(Math.E, ((x + y) / (2 * (s*s))) / -1.0D);
                m[i][j] = g1 * e;
            }
        }
        return m;
    }
    public class Worker implements Runnable {

        public int x;
        public int y0;
        public int y1;
        public int width;
        public int height;
        public int matrixSize;
        public Bitmap bmp;
        public Bitmap res;
        public double sum;
        public double[][] matrix;
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss:SSS z");
        Date date1 = new Date(System.currentTimeMillis());

        @Override
        public void run() {//run procedure - methode for executable code in task

            for (int y = y0; y < y1; y++) {//move on height of image
                for (int x = 0; x < width; x++) {//move on width of image
                    int red = 0;
                    int green = 0;
                    int blue = 0;
                    double dRed = 0;
                    double dGreen = 0;
                    double dBlue = 0;

                    for (int v = 0; v < matrixSize; v++) {//move on height matrix box (5x5)
                        for (int u = 0; u < matrixSize; u++) {//move on width matrix box (5x5)
                            if (Thread.currentThread().isInterrupted()) {
                                return;
                            }
                            int px = u + x - matrixSize / 2;//the value, which need add with width coordinate changeable pixel
                            int py = v + y - matrixSize / 2;//the value, which need add with height coordinate changeable pixel

                            if (px < 0) {
                                px = 0;
                            }
                            if (py < 0) {
                                py = 0;
                            }
                            if (px >= width) {
                                px = width - 1;
                            }
                            if (py >= height) {
                                py = height - 1;
                            }
                            int c = bmp.getPixel(px, py);//get new pixel from changeable pixel
                            if (rbBox.isChecked()) {//if box blur is selected
                                red += Color.red(c);
                                green += Color.green(c);
                                blue += Color.blue(c);
                            } else {
                                dRed += Double.valueOf(Color.red(c) * matrix[u][v]);
                                dGreen += Double.valueOf(Color.green(c) * matrix[u][v]);
                                dBlue += Double.valueOf(Color.blue(c) * matrix[u][v]);
                            }
                            if (fullStopper) {
                                if (rbBox.isChecked()) {
                                    red /= (matrixSize * matrixSize);
                                    green /= (matrixSize * matrixSize);
                                    blue /= (matrixSize * matrixSize);
                                } else {

                                    dRed /= sum;
                                    dGreen /= sum;
                                    dBlue /= sum;
                                    red = (int)dRed;
                                    green = (int)dGreen;
                                    blue = (int)dBlue;
                                    Log.e("test",String.valueOf(sum)+" "+String.valueOf(red));
                                }
                                res.setPixel(x, y, Color.rgb((int)red, (int)green, (int)blue));
                                return;
                            }
                        }
                    }
                    if (rbBox.isChecked()) {
                        red /= (matrixSize * matrixSize);
                        green /= (matrixSize * matrixSize);
                        blue /= (matrixSize * matrixSize);
                    } else {

                        dRed /= sum;
                        dGreen /= sum;
                        dBlue /= sum;
                        red = (int)dRed;
                        green = (int)dGreen;
                        blue = (int)dBlue;
                        Log.e("test",String.valueOf(sum)+" "+String.valueOf(red));
                    }
                    res.setPixel(x, y, Color.rgb((int)red, (int)green, (int)blue));
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Drawable draw = new BitmapDrawable(getResources(), res);
                            sur.setForeground(draw);
                            sur.invalidate();
                        }
                    });
                }
            }
            Date date2 = new Date(System.currentTimeMillis());
            Long milliseconds = date2.getTime() - date1.getTime();
            Double seconds = (double) (milliseconds / 1000);
            DecimalFormat format = new DecimalFormat("#.00");
            tvTime.setText(String.valueOf(format.format(seconds)));
        }
    }

    public void buttonExecuteOnClick(View v) {
        bmp=null;
        res=null;
        sum=0;
        matrix=null;
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.disk);
        matrix = fillMatrix();
        sum = sumM(matrix);
        fullStopper = false;
        MatrixSize = sbMatrixSize.getProgress();
        s=MatrixSize;
        Runnable sleeper = new Runnable() {

            @Override
            public void run() {


                int threadCount = Integer.parseInt(spn.getSelectedItem().toString());
                bmp = BitmapFactory.decodeResource(getResources(), R.drawable.disk);
                int w = bmp.getWidth();
                int h = bmp.getHeight();
                int pieceSize = h / threadCount;//Size of piece of image that can be changed some thread
                res = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Thread[] threads = new Thread[threadCount];
                Worker[] workers = new Worker[threadCount];
                for (int i = 0; i < threadCount; i++) {
                    workers[i] = new Worker();
                    workers[i].bmp = bmp;
                    workers[i].res = res;
                    workers[i].matrixSize = MatrixSize;
                    workers[i].width = w;
                    workers[i].height = h;
                    workers[i].sum=sum;
                    workers[i].matrix=matrix;
                    workers[i].y0 = pieceSize * i;
                    workers[i].y1 = workers[i].y0 + pieceSize;
                    threads[i] = new Thread(workers[i]);
                    threads[i].start();
                }
                //Code for stop the threads in array
                if (fullStopper) {
                    for (int i = 0; i < threadCount; i++) {
                        threads[i].interrupt();
                    }
                }
                //Code for waiting when threads in array are running and changing piece of image
                stopper = false;
                while (!stopper) {
                    if (fullStopper) {
                        break;
                    }
                    stopper = true;
                    for (int i = 0; i < threadCount; i++) {
                        if (threads[i].isAlive()) {
                            stopper = false;
                        }
                    }
                }
            }
        };
        sleepper = new Thread(sleeper);
        sleepper.start();
        sur.invalidate();
    }

    public void ButtonStopOnClick(View v) {

        fullStopper = true;
        sleepper.interrupt();
    }
}
