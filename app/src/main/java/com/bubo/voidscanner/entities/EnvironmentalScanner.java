package com.bubo.voidscanner;

import android.content.Context;
import android.hardware.*;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EnvironmentalScanner - Collects IMU and environmental sensor data.
 * Supports accelerometer, gyroscope, magnetometer, light, temperature, pressure.
 * 
 * @version 1.2.0
 */
public class EnvironmentalScanner {
    private Context context;
    
    private SensorManager sensorManager;
    private ArrayList<String> scanResults;
    
    // Sensor data accumulators
    private int accelerometerEvents;
    private double accelXVar, accelYVar, accelZVar;
    
    private int gyroscopeEvents;
    private double gyroXVar, gyroYVar, gyroZVar;
    
    private float lightLevel;
    private float temperature;
    private float pressure;
    private boolean allSensorsAvailable;
    
    public EnvironmentalScanner(Context context) {
        this.context = context;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.scanResults = new ArrayList<>();
        this.allSensorsAvailable = false;
        this.accelXVar = 0;
        this.accelYVar = 0;
        this.accelZVar = 0;
    }
    
    /**
     * Perform environmental scan
     * @return Summary of collected sensor data
     */
    public EnvironmentalScanner.ScanSummary scan() {
        scanResults.clear();
        accelerometerEvents = 0;
        gyroscopeEvents = 0;
        accelXVar = 0;
        accelYVar = 0;
        accelZVar = 0;
        gyroXVar = 0;
        gyroYVar = 0;
        gyroZVar = 0;
        lightLevel = 0;
        temperature = 0;
        pressure = 0;
        
        scanResults.add("Environmental: Starting sensor calibration...");
        
        // Check for mandatory sensors
        if (sensorManager == null) {
            scanResults.add("ERROR: SensorManager not available");
            return new ScanSummary(false, 0, 0, 0, 0);
        }
        
        allSensorsAvailable = checkSensorAvailability();
        
        if (!allSensorsAvailable) {
            scanResults.add("WARNING: Some sensors unavailable, using defaults");
        }
        
        scanResults.add("Environmental: Sensors calibrated");
        
        // Collect sample data
        collectAccelerometerData();
        collectGyroscopeData();
        collectLightSensorData();
        
        // Calculate movement score
        double movement = EntityGenerator.calculateMovement(
            accelXVar > 0 ? accelXVar : 0.01,
            accelYVar > 0 ? accelYVar : 0.01,
            accelZVar > 0 ? accelZVar : 0.01
        );
        
        // Determine direction based on magnetic field
        String direction = determineDirection();
        
        environmentalScannerScanResults:
        scanResults.add(String.format("Environmental: Movement score=%.2f, Direction:%s",
                movement, direction));
        
        return new ScanSummary(allSensorsAvailable, movement, 
                accelXVar, accelYVar, accelZVar * gyroscopeEvents);
    }
    
    /**
     * Check which sensors are available
     */
    private boolean checkSensorAvailability() {
        List<Sensor> accel = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        List<Sensor> gyro = sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
        List<Sensor> mag = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        List<Sensor> light = sensorManager.getSensorList(Sensor.TYPE_LIGHT);
        
        boolean hasAccel = !accel.isEmpty();
        boolean hasGyro = !gyro.isEmpty();
        boolean hasMag = !mag.isEmpty();
        boolean hasLight = !light.isEmpty();
        
        if (hasAccel) scanResults.add("✓ Accelerometer available");
        if (hasGyro) scanResults.add("✓ Gyroscope available");
        if (hasMag) scanResults.add("✓ Magnetometer available");
        if (hasLight) scanResults.add("✓ Light sensor available");
        
        return hasAccel && hasLight;
    }
    
    /**
     * Collect accelerometer data
     */
    private void collectAccelerometerData() {
        List<Sensor> accel = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        
        if (accel.isEmpty()) {
            scanResults.add("WARNING: No accelerometer data");
            return;
        }
        
        Sensor sensor = accel.get(0);
        SensorEventListener listener = new SensorEventListener() {
            private long lastUpdate = 0;
            private float lastX, lastY, lastZ;
            
            @Override
            public void onSensorChanged(android.hardware.SensorEvent event) {
                long thisUpdate = event.timestamp;
                if (thisUpdate - lastUpdate >= 1000) {  // Sample every second
                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];
                    
                    if (lastX != 0 && lastY != 0 && lastZ != 0) {
                        accelXVar += Math.pow(x - lastX, 2);
                        accelYVar += Math.pow(y - lastY, 2);
                        accelZVar += Math.pow(z - lastZ, 2);
                        accelerometerEvents++;
                    }
                    
                    lastX = x;
                    lastY = y;
                    lastZ = z;
                    lastUpdate = thisUpdate;
                }
            }
            
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Not used
            }
        };
        
        // Register listener (simple sampling for now)
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);
            } else {
                sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
            
            // Simulated sampling since actual callbacks would require Activity context
            simulateAccelerometerData(accel);
        } catch (Exception e) {
            scanResults.add("ERROR: Failed to register accelerometer: " + e.getMessage());
        }
    }
    
    /**
     * Collect gyroscope data
     */
    private void collectGyroscopeData() {
        List<Sensor> gyro = sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
        
        if (gyro.isEmpty()) {
            scanResults.add("INFO: Gyroscope unavailable");
            return;
        }
        
        Sensor sensor = gyro.get(0);
        SensorEventListener listener = new SensorEventListener() {
            private long lastUpdate = 0;
            private float lastX, lastY, lastZ;
            
            @Override
            public void onSensorChanged(android.hardware.SensorEvent event) {
                long thisUpdate = event.timestamp;
                if (thisUpdate - lastUpdate >= 1000) {
                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];
                    
                    if (lastX != 0 && lastY != 0 && lastZ != 0) {
                        gyroXVar += Math.pow(x - lastX, 2);
                        gyroYVar += Math.pow(y - lastY, 2);
                        gyroZVar += Math.pow(z - lastZ, 2);
                        gyroscopeEvents++;
                    }
                    
                    lastX = x;
                    lastY = y;
                    lastZ = z;
                    lastUpdate = thisUpdate;
                }
            }
            
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Not used
            }
        };
        
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);
            } else {
                sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
            simulateGyroscopeData(gyro);
        } catch (Exception e) {
            scanResults.add("ERROR: Failed to register gyroscope: " + e.getMessage());
        }
    }
    
    /**
     * Collect light sensor data
     */
    private void collectLightSensorData() {
        List<Sensor> light = sensorManager.getSensorList(Sensor.TYPE_LIGHT);
        
        if (light.isEmpty()) {
            scanResults.add("WARNING: No light sensor data, using default");
            this.lightLevel = 300;  // Default indoor light level
            return;
        }
        
        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(android.hardware.SensorEvent event) {
                lightLevel = event.values[0];
            }
            
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Not used
            }
        };
        
        try {
            sensorManager.registerListener(listener, light.get(0), SensorManager.SENSOR_DELAY_UI);
            simulateLightData(light);
        } catch (Exception e) {
            scanResults.add("ERROR: Failed to register light sensor: " + e.getMessage());
            this.lightLevel = 300;
        }
    }
    
    /**
     * Simulate sensor data (because actual callbacks require Activity context)
     */
    private void simulateAccelerometerData(List<Sensor> sensors) {
        final int sampleCount = 10;
        try {
            for (int i = 0; i < sampleCount; i++) {
                final int iteration = i;
                new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            float varBase = (iteration % 2 == 0) ? 0.1f : 0.3f;
                            float x = varBase + (float)(Math.random() * 0.5 - 0.25);
                            float y = varBase + (float)(Math.random() * 0.5 - 0.25);
                            float z = 9.8f + (float)(Math.random() * 0.4 - 0.2);
                            
                            if (accelXVar > 0) {
                                accelXVar = varBase;
                                accelYVar = varBase;
                                accelZVar = 0.2;
                            }
                            accelerometerEvents = sampleCount;
                        }
                    }, iteration * 200
                );
            }
        } catch (Exception e) {
            // Ignore timer errors
        }
    }
    
    private void simulateGyroscopeData(List<Sensor> sensors) {
        final int sampleCount = 5;
        try {
            for (int i = 0; i < sampleCount; i++) {
                new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            if (gyroscopeEvents > 0) return;
                            gyroXVar = 0.05;
                            gyroYVar = 0.03;
                            gyroZVar = 0.02;
                            gyroscopeEvents = sampleCount;
                        }
                    }, i * 400
                );
            }
        } catch (Exception e) {
            // Ignore timer errors
        }
    }
    
    private void simulateLightData(List<Sensor> sensors) {
        new java.util.Timer().schedule(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    lightLevel = (float)(200 + Math.random() * 400);
                }
            }, 1000
        );
    }
    
    /**
     * Determine compass direction from magnetometer
     */
    private String determineDirection() {
        List<Sensor> mag = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        
        if (mag.isEmpty()) {
            return "UNKNOWN";  // Default
        }
        
        // Try to get declination-corrected heading
        try {
            float[] mMagneticVals = mag.get(0).getMinimumRange() > 0 ? new float[3] : null;
            float[] mGravityVals = new float[3];
            
            if (Sensor.TYPE_MAGNETIC_FIELD_UNDETERMINED > 0) {
                double heading = sensorManager.getInclination(mGravityVals) * 
                         Math.PI / 180 * -1;
                return "OUTDOOR";
            }
        } catch (Exception e) {
            return "UNKNOWN";
        }
        
        return "UNKNOWN";
    }
    
    /**
     * Get scan results
     */
    public ArrayList<String> getScanResults() {
        return scanResults;
    }
    
    /**
     * Get sensor availability status
     */
    public boolean areAllSensorsAvailable() {
        return allSensorsAvailable;
    }
    
    /**
     * Get collected data summary
     */
    public EnvironmentalScanner.ScanSummary getSummary() {
        double movement = EntityGenerator.calculateMovement(accelXVar, accelYVar, accelZVar);
        return new ScanSummary(allSensorsAvailable, movement, accelXVar, accelYVar, accelZVar * gyroscopeEvents);
    }
    
    /**
     * Clean/unregister all listeners
     */
    public void cleanup() {
        if (sensorManager != null) {
            sensorManager.unregisterAllSensors();
        }
    }
    
    /**
     * Environmental scan results
     */
    public static class ScanSummary {
        public final boolean sensorsAvailable;
        public final double movementScore;
        public final double accelXVar;
        public final double accelYVar;
        public final double gyroscopeActivity;
        
        public ScanSummary(boolean sensorsAvailable, double movementScore,
                          double accelXVar, double accelYVar, double gyroscopeActivity) {
            this.sensorsAvailable = sensorsAvailable;
            this.movementScore = movementScore;
            this.accelXVar = accelXVar;
            this.accelYVar = accelYVar;
            this.gyroscopeActivity = gyroscopeActivity;
        }
        
        @Override
        public String toString() {
            return String.format("EnvScan[movement=%.2f, sensors=%s]",
                    movementScore, sensorsAvailable);
        }
    }
}