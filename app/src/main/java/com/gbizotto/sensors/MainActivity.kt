package com.gbizotto.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), SensorEventListener {

    private var oldAccuracy = 0

    private var previousX: Float = 0f
    private var biggestXDifference: Float = 0f

    private var previousY: Float = 0f
    private var biggestYDifference: Float = 0f

    private var accelerometerSensor: Sensor? = null

    private lateinit var mSensorManager: SensorManager

    private var mAccel: Float = 0.toFloat()
    private var mAccelCurrent: Float = 0.toFloat()
    private var mAccelLast: Float = 0.toFloat()

    private var totalJumps = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val deviceSensors: List<Sensor> = mSensorManager.getSensorList(Sensor.TYPE_ALL)

        Log.v(MainActivity::class.java.simpleName, "----- sensores = $deviceSensors")


        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            updateNotification("Yay accelerometer :D")
            accelerometerSensor = it
        } ?: run {
            updateNotification("No accelerometer :(")
        }

        mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)?.let {
            doLog("Yay gravity :D")
            accelerometerSensor = it
        } ?: run {
            doLog("No gravity :(")
        }

        btn_main.setOnClickListener {
            txt_result.text = "Biggest X = $biggestXDifference\nBiggest y = $biggestYDifference"
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        doLog("Accuracy has changed, new value is $accuracy, old value was $oldAccuracy")
        oldAccuracy = accuracy
    }

    override fun onSensorChanged(event: SensorEvent) {
        //            updateNotification("Event values size= ${it.values.size}\n event[0] = ${it.values[0]}\nevent[1] = ${it.values[1]}\nevent[2] = ${it.values[2]}\n")

//        val x = event.values[0]
//        val diffX = calculateDifference(x, previousX)
//        if (diffX > biggestXDifference) {
//            biggestXDifference = diffX
//        }
//
//        val y = event.values[1]
//        val diffY = calculateDifference(y, previousY)
//        if (diffY > biggestYDifference) {
//            biggestYDifference = diffY
//        }
//
//        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
//            val gravity = event.values.clone()
//            // Shake detection
//            val x = gravity[0].toDouble()
//            val y = gravity[1].toDouble()
//            val z = gravity[2].toDouble()
//
//            mAccelLast = mAccelCurrent
//            mAccelCurrent = sqrt(x * x + y * y + z * z).toFloat()
//
//            val delta = mAccelCurrent - mAccelLast
//            mAccel = mAccel * 0.9f + delta
//            // Make this higher or lower according to how much
//            // motion you want to detect
////                Log.v(MainActivity::class.java.simpleName, "Aceleração = $mAccel")
////                if (mAccel > 3) {
////                    // do something
////                    Log.v(
////                        MainActivity::class.java.simpleName,
////                        "Aceleração maior que 3, Aceleração = $mAccel"
////                    )
////                }
//
//            if (mAccel >= 10) {
//                doLog("Pulo identificado, aceleração = $mAccel")
//                totalJumps++
//                updateNotification("Pulos = $totalJumps")
//            }
//        }


        // In this example, alpha is calculated as t / (t + dT),
        // where t is the low-pass filter's time-constant and
        // dT is the event delivery rate.

        val alpha = 0.8f
        val gravity = FloatArray(3)
        val linearAcceleration = FloatArray(3)

        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

        // Remove the gravity contribution with the high-pass filter.
        linearAcceleration[0] = event.values[0] - gravity[0]
        linearAcceleration[1] = event.values[1] - gravity[1]
        linearAcceleration[2] = event.values[2] - gravity[2]
        /*
        If you push the device on the left side (so it moves to the right), the x acceleration value is positive.
        If you push the device on the bottom (so it moves away from you), the y acceleration value is positive.
        If you push the device toward the sky with an acceleration of A m/s2, the z acceleration value is equal to A + 9.81,
            which corresponds to the acceleration of the device (+A m/s2) minus the force of gravity (-9.81 m/s2).
        The stationary device will have an acceleration value of +9.81, which corresponds to the acceleration of the device (0 m/s2 minus the force of gravity, which is -9.81 m/s2).
         */

        if (linearAcceleration[2] <= 1) {
            doLog("Pulo identificado, aceleração = $mAccel")
            totalJumps++
            updateNotification("Pulos = $totalJumps")

            doLog("x = ${linearAcceleration[0]}, y = ${linearAcceleration[1]}, z] = ${linearAcceleration[2]}")
        }


        /*
        linear_acceleration[0] = 1.2854373,     linear_acceleration[1] = 0.5287828,     linear_acceleration[2] = 7.7212105
        linear_acceleration[0] = -0.43420783,   linear_acceleration[1] = 0.9051079,     linear_acceleration[2] = 7.780829
        linear_acceleration[0] = -0.8115301,    linear_acceleration[1] = 4.609175,      linear_acceleration[2] = 6.296506
        linear_acceleration[0] = 0.73152184,    linear_acceleration[1] = 7.8107643,     linear_acceleration[2] = -0.076763466
        linear_acceleration[0] = 1.7236865,     linear_acceleration[1] = -2.2780902,    linear_acceleration[2] = 7.306727

        linear_acceleration[0] = 1.3591031, linear_acceleration[1] = 0.28904012, linear_acceleration[2] = 7.7212925
         */
    }

    private fun calculateDifference(newValue: Float, oldValue: Float): Float {
        return if (newValue < oldValue) {
            oldValue.minus(newValue)
        } else {
            newValue.minus(oldValue)
        }
    }

    override fun onResume() {
        super.onResume()

        accelerometerSensor?.also { light ->
            mSensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)
    }

    private fun updateNotification(txt: String) {
        txt_notifications.text = txt
    }


    private fun doLog(txt: String) {
        Log.v(MainActivity::class.java.simpleName, txt)
    }
}
