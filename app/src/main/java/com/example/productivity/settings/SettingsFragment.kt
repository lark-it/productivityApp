package com.example.productivity.settings

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.app.TimePickerDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.productivity.R
import com.example.productivity.notifications.NotificationReceiver
import java.util.*

class SettingsFragment : Fragment() {

    private lateinit var switchNotifications: Switch
    private lateinit var btnPickTime: Button
    private lateinit var timeText: TextView
    private lateinit var checkboxHabits: CheckBox

    private var savedHour = 8
    private var savedMinute = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPrefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)

        switchNotifications = view.findViewById(R.id.switchNotifications)
        btnPickTime = view.findViewById(R.id.btnPickTime)
        timeText = view.findViewById(R.id.notificationTimeText)
        checkboxHabits = view.findViewById(R.id.checkboxHabits)

        savedHour = sharedPrefs.getInt("hour", 8)
        savedMinute = sharedPrefs.getInt("minute", 0)
        timeText.text = String.format("%02d:%02d", savedHour, savedMinute)
        switchNotifications.isChecked = sharedPrefs.getBoolean("notifications_enabled", false)
        checkboxHabits.isChecked = sharedPrefs.getBoolean("notify_habits", true)

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(
                        requireContext(),
                        "Пожалуйста, включите уведомления в настройках приложения",
                        Toast.LENGTH_LONG
                    ).show()
                    switchNotifications.isChecked = false
                    return@setOnCheckedChangeListener
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = requireContext().getSystemService(AlarmManager::class.java)
                    if (!alarmManager.canScheduleExactAlarms()) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        startActivity(intent)
                        Toast.makeText(
                            requireContext(),
                            "Разрешите точные будильники в настройках",
                            Toast.LENGTH_LONG
                        ).show()
                        switchNotifications.isChecked = false
                        return@setOnCheckedChangeListener
                    }
                }

                sharedPrefs.edit().putBoolean("notifications_enabled", true).apply()
                setDailyReminder(savedHour, savedMinute)
                Toast.makeText(requireContext(), "Уведомления включены", Toast.LENGTH_SHORT).show()
            } else {
                sharedPrefs.edit().putBoolean("notifications_enabled", false).apply()
                cancelReminder()
                Toast.makeText(requireContext(), "Уведомления отключены", Toast.LENGTH_SHORT).show()
            }
        }

        btnPickTime.setOnClickListener {
            val listener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                savedHour = hour
                savedMinute = minute
                timeText.text = String.format("%02d:%02d", hour, minute)
                sharedPrefs.edit().putInt("hour", hour).putInt("minute", minute).apply()
                if (switchNotifications.isChecked) setDailyReminder(hour, minute)
            }
            TimePickerDialog(requireContext(), listener, savedHour, savedMinute, true).show()
        }

        checkboxHabits.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("notify_habits", isChecked).apply()
        }

        val btnTestNotification = view.findViewById<Button>(R.id.btnTestNotification)
        btnTestNotification.setOnClickListener {
            val intent = Intent(requireContext(), NotificationReceiver::class.java)
            requireContext().sendBroadcast(intent)
            Toast.makeText(requireContext(), "Тестовое уведомление отправлено", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setDailyReminder(hour: Int, minute: Int) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    private fun cancelReminder() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }
}