package com.app.lockcomposeChild.x

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.app.lockcomposeChild.R

class RecentAppsAccessibilityService : AccessibilityService() {
    private var overlayView: View? = null
    private lateinit var windowManager: WindowManager

    companion object {
        private const val TAG = "RecentAppsService"
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.d("MyAccessibilityService", "AccessibilityEvent")
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED) {
            val contentDescription = event.contentDescription
            Log.d("MyAccessibilityService", "Long press detected on 1: $contentDescription")
            contentDescription?.let {
                Log.d("MyAccessibilityService", "Long press detected on 2: $contentDescription")
                showPartialOverlay()
            }
        }
    }



    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    private fun showPartialOverlay() {
        if (overlayView == null) {
            val layoutInflater = LayoutInflater.from(this)
            val overlayLayout = layoutInflater.inflate(R.layout.activity_lock_screen, null)
            val askPermissionBtn = overlayLayout.findViewById<Button>(R.id.askPermission)
            val cancelPermission = overlayLayout.findViewById<Button>(R.id.cancelPermission)
            val lockUi = overlayLayout.findViewById<LinearLayout>(R.id.lockUi)

            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSPARENT
            )

            overlayView = overlayLayout
            windowManager.addView(overlayView, layoutParams)

            askPermissionBtn.setOnClickListener {
                if (lockUi.visibility == View.GONE) {
                    lockUi.visibility = View.VISIBLE
                    askPermissionBtn.visibility = View.GONE
                    cancelPermission.visibility = View.VISIBLE
                    showPassCodeUi(overlayLayout, packageName)
                }
            }

            cancelPermission.setOnClickListener {
                askPermissionBtn.visibility = View.VISIBLE
                cancelPermission.visibility = View.GONE
                lockUi.visibility = View.GONE
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showPassCodeUi(view: View, packageName: String) {
        val btn0 = view.findViewById<TextView>(R.id.btn0)
        val btn1 = view.findViewById<TextView>(R.id.btn1)
        val btn2 = view.findViewById<TextView>(R.id.btn2)
        val btn3 = view.findViewById<TextView>(R.id.btn3)
        val btn4 = view.findViewById<TextView>(R.id.btn4)
        val btn5 = view.findViewById<TextView>(R.id.btn5)
        val btn6 = view.findViewById<TextView>(R.id.btn6)
        val btn7 = view.findViewById<TextView>(R.id.btn7)
        val btn8 = view.findViewById<TextView>(R.id.btn8)
        val btn9 = view.findViewById<TextView>(R.id.btn9)
        val tick = view.findViewById<ImageView>(R.id.tick)

        val edit = view.findViewById<EditText>(R.id.passCodeEdit)

        val passcodeBuilder = StringBuilder()
        val numberButtons = listOf(btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9)

        tick.setOnClickListener {
            val enteredPasscode = passcodeBuilder.toString()
            if (enteredPasscode == "1234") { // Dummy check for illustration
                edit.text.clear()
                removeOverlay()
            } else {
                Toast.makeText(this, "Passcode is incorrect", Toast.LENGTH_LONG).show()
            }
        }

        numberButtons.forEach { button ->
            button.setOnClickListener {
                passcodeBuilder.append(button.text)
                edit.setText(passcodeBuilder.toString())
            }
        }

        addRemoveIcon(edit)
        edit.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = edit.compoundDrawablesRelative[2]
                if (drawableEnd != null && event.rawX >= edit.right - drawableEnd.bounds.width()) {
                    if (passcodeBuilder.isNotEmpty()) {
                        passcodeBuilder.deleteCharAt(passcodeBuilder.length - 1)
                        edit.setText(passcodeBuilder.toString())
                    }
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun removeOverlay() {
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
    }

    private fun addRemoveIcon(edit: EditText) {
        val drawableEnd = edit.compoundDrawablesRelative[2]
        if (drawableEnd != null) {
            val greenColor = ContextCompat.getColor(this, R.color.greenColor)
            drawableEnd.colorFilter = PorterDuffColorFilter(greenColor, PorterDuff.Mode.SRC_IN)
            edit.invalidate()
        }
    }


}
