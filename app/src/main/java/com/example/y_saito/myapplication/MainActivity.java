package com.example.y_saito.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private SensorManager mSensorManager = null;
    private SensorEventListener mSensorEventListener = null;

    private float[] fMagnetic = new float[3];
    private float[] fAccell = new float[3];

    private ImageView compass;
    private RotateAnimation rotate;
    private float angle;
    private float from;
    private float to;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compass = (ImageView) findViewById(R.id.compass);
        from = 0;

        mSensorManager = (SensorManager) getSystemService( Context.SENSOR_SERVICE );

        mSensorEventListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                switch( event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        fAccell = event.values.clone();
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        fMagnetic = event.values.clone();
                        break;
                }
                // 回転行列を得る
                float[] inR = new float[9];
                SensorManager.getRotationMatrix(
                        inR,
                        null,
                        fAccell,
                        fMagnetic );
                // ワールド座標とデバイス座標のマッピングを変換する
                float[] outR = new float[9];
                SensorManager.remapCoordinateSystem(
                        inR,
                        SensorManager.AXIS_X, SensorManager.AXIS_Y,
                        outR );
                // 姿勢を得る
                float[] fAttitude = new float[3];
                SensorManager.getOrientation(
                        outR,
                        fAttitude );

                angle = rad2deg( fAttitude[0] );

//                String buf =
//                        String.format( "sensor:\t%f\n", fMagnetic[0] ) +
//                        String.format( "values:\t%f\n", fMagnetic[1] ) +
//                        String.format( "X:\t%f\n", fAccell[0] ) +
//                        String.format( "Y:\t%f\n", fAccell[1] ) +
//                        String.format( "Z:\t%f\n", fAccell[2] ) +
//                        String.format( "方位角:\t%f\n", rad2deg( fAttitude[0] )) +
//                        String.format( "前後の傾斜:\t%f\n", rad2deg( fAttitude[1] )) +
//                        String.format( "左右の傾斜:\t%f\n", rad2deg( fAttitude[2] ));
                String buf =String.format( "方位角:\t%f\n", angle);
                TextView t = (TextView) findViewById( R.id.textView );
                t.setText( buf );

                TextView t2 = (TextView) findViewById( R.id.direction );
                t2.setText( direction(angle) );

                to = -angle;
                startRotation(from, to);
                from = to;
            }

            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

    }

    private float rad2deg( float rad ) {
        return rad * (float) 180.0 / (float) Math.PI;
    }

    private void startRotation(float from, float to) {

        // RotateAnimation(float fromDegrees, float toDegrees, int pivotXType, float pivotXValue, int pivotYType,float pivotYValue)
        rotate = new RotateAnimation(from, to,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        // animation時間 msec
        rotate.setDuration(200);
        // 繰り返し回数
        rotate.setRepeatCount(1);
        // animationが終わったそのまま表示にする
        rotate.setFillAfter(true);

        //アニメーションの開始
        compass.startAnimation(rotate);

    }

    private String direction(float angle){
        String direction = "";
        if(angle >= -22 && angle <= 22){
            direction = "北";
        }else if(angle >= 23 && angle <= 67){
            direction = "北東";
        }else if(angle >= 68 && angle <= 112){
            direction = "東";
        }else if(angle >= 113 && angle <= 157){
            direction = "南東";
        }else if(angle >= 158 || angle <= -158){
            direction = "南";
        }else if(angle <= -113 && angle >= -157){
            direction = "南西";
        }else if(angle <=-68 && angle >= -112){
            direction = "西";
        }else if(angle <= -23 && angle >=-67){
            direction = "北西";
        }
        return direction;
    }



    protected void onStart() { // ⇔ onStop
        super.onStart();

        mSensorManager.registerListener(
                mSensorEventListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER ),
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(
                mSensorEventListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onStop() { // ⇔ onStart
        super.onStop();

        mSensorManager.unregisterListener( mSensorEventListener );
    }
}
