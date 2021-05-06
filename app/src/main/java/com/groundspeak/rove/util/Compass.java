package com.groundspeak.rove.util;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.view.Surface;
import android.view.WindowManager;

public class Compass {
    // Heading = direction device is pointing, relative to North (clockwise)
    // Bearing = direction to destination, relative to North (clockwise)
    // Relative bearing = angle between Heading and Bearing (clockwise) (i.e. Bearing minus Heading)

    public interface CompassListener {
        /**
         * @param heading Heading in degrees, clockwise from North
         */
        void onHeadingUpdate(float heading);
    }

    private static final float LOW_PASS_ALPHA = 0.15f;

    private final Context context;
    private final CompassListener listener;

    private float[]	mGravityVector = new float[3];
    private float[]	mMagneticFieldVector = new float[3];
    private float[]	mOrientation = new float[3];
    private float[]	mRotationMatrix = new float[9];

    private SensorEventListener mSensorListener = new SensorEventListener() {
        @SuppressWarnings("SuspiciousNameCombination")
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    mGravityVector = lowPass(event.values.clone(), mGravityVector);
                    break;

                case Sensor.TYPE_MAGNETIC_FIELD:
                    mMagneticFieldVector = lowPass(event.values.clone(), mMagneticFieldVector);
                    break;
            }

            if (SensorManager.getRotationMatrix(mRotationMatrix, null, mGravityVector, mMagneticFieldVector)) {
                int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
                int x = SensorManager.AXIS_X;
                int y = SensorManager.AXIS_Y;
                if (rotation == Surface.ROTATION_90) {
                    x = SensorManager.AXIS_Y;
                    y = SensorManager.AXIS_MINUS_X;
                } else if (rotation == Surface.ROTATION_180) {
                    x = SensorManager.AXIS_MINUS_X;
                    y = SensorManager.AXIS_MINUS_Y;
                } else if (rotation == Surface.ROTATION_270) {
                    x = SensorManager.AXIS_MINUS_Y;
                    y = SensorManager.AXIS_X;
                }
                float[] fixedRotationMatrix = new float[9];
                SensorManager.remapCoordinateSystem(mRotationMatrix, x, y, fixedRotationMatrix);
                SensorManager.getOrientation(fixedRotationMatrix, mOrientation);

                listener.onHeadingUpdate((float)(180 * mOrientation[0] / Math.PI));
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    /**
     * Adjusts heading to account for variance between magnetic and polar/"True" north
     */
    public static float adjustHeadingForDeclination(float heading, Location location) {
        float declination = 0;
        if (location != null) {
            GeomagneticField field = new GeomagneticField((float)location.getLatitude(), (float)location.getLongitude(), (float)location.getAltitude(), System.currentTimeMillis());
            declination = field.getDeclination();
        }

        return heading + declination;
    }

    private static float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            output[i] = output[i] + LOW_PASS_ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    public Compass(Context context, CompassListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void start() {
        SensorManager sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(mSensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(mSensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
    }

    public void stop() {
        SensorManager sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(mSensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensorManager.unregisterListener(mSensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
    }
}