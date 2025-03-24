package com.example.productivity.habits.today

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.productivity.AppDatabase
import com.example.productivity.R
import com.example.productivity.habits.HabitCompletionDao
import com.example.productivity.habits.HabitCompletionEntity
import com.example.productivity.habits.HabitsDao
import com.example.productivity.habits.RepeatType
import com.example.productivity.home.HomeFragment
import com.example.productivity.home.UserRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TodayFragment : Fragment() {
    private lateinit var adapter: TodayAdapter
    private var habits = mutableListOf<HabitItem>()
    private lateinit var db: AppDatabase
    private lateinit var habitDao: HabitsDao
    private lateinit var habitCompletionDao: HabitCompletionDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_today, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.getDatabase(requireContext())
        habitDao = db.habitsDao()
        habitCompletionDao = db.habitCompletionDao()

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_today)
        adapter = TodayAdapter(habits) { habitId, isChecked, todayDate ->
            lifecycleScope.launch {
                if (isChecked) {
                    habitCompletionDao.insertCompletion(HabitCompletionEntity(habitId, todayDate, true))
                } else {
                    habitCompletionDao.deleteCompletion(habitId, todayDate)
                }

                updateHabitCompletion(habitId, isChecked, todayDate) // ✅ Передаём правильные аргументы
                loadHabits()
            }
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        loadHabits()
    }




    private var habitProcessing = mutableSetOf<Int>() // ✅ Отслеживаем обработку привычек


    private fun updateHabitCompletion(habitId: Int, isCompleted: Boolean, date: String) {
        if (habitProcessing.contains(habitId)) return
        habitProcessing.add(habitId)

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val habitCompletionDao = db.habitCompletionDao()
            val userDao = db.userDao()
            val userRepository = UserRepository(userDao)

            val wasCompleted = habitCompletionDao.isHabitCompleted(habitId, date) ?: false

            Log.d("TodayFragment", "habitId=$habitId был выполнен? $wasCompleted")

            val coinChange = 2
            val xpChange = 1

            if (!isCompleted) {
                // ✅ Удаляем выполнение привычки **только за сегодня**
                Log.d("TodayFragment", "❌ Удаляем выполнение habitId=$habitId за дату $date")
                userRepository.addCoinsAndXP(-coinChange, -xpChange)
                habitCompletionDao.deleteCompletion(habitId, date)
            } else {
                // ✅ Если привычка выполняется впервые сегодня
                Log.d("TodayFragment", "✅ Начисляем монеты и опыт за habitId=$habitId за дату $date")
                userRepository.addCoinsAndXP(coinChange, xpChange)
                habitCompletionDao.insertCompletion(HabitCompletionEntity(habitId, date, true))
            }

            requireActivity().runOnUiThread {
                loadHabits()
                val homeFragment = parentFragmentManager.findFragmentByTag("HomeFragment") as? HomeFragment
                homeFragment?.loadUserData() // Обновляем баланс на главном экране
            }

            habitProcessing.remove(habitId)
        }
    }



    private fun loadHabits() {
        lifecycleScope.launch {
            val habitsFromDb = habitDao.getAllHabits()

            val today = Calendar.getInstance()
            var dayOfWeek = today.get(Calendar.DAY_OF_WEEK) - 1
            if (dayOfWeek == -1) dayOfWeek = 6
            val dayOfMonth = today.get(Calendar.DAY_OF_MONTH)
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today.time)

            val sortedHabits = mutableListOf<HabitItem>()
            sortedHabits.add(HabitItem.Header("Нужно сделать:"))

            val habitsToShow = habitsFromDb.filter { habit ->
                val habitStartDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(habit.startDate)
                if (habitStartDate != null && habitStartDate.after(today.time)) {
                    return@filter false
                }

                val isHabitDay = when (habit.repeatType) {
                    RepeatType.DAILY -> true
                    RepeatType.WEEKLY -> habit.repeatDays?.contains(dayOfWeek) == true
                }

                isHabitDay
            }.map { habit ->
                val isCompleted = habitCompletionDao.isHabitCompleted(habit.id, todayDate) ?: false
                HabitItem.Habit(habit.id, habit.title, isCompleted, habit.iconResId, habit.color)
            }

            sortedHabits.addAll(habitsToShow.filter { !it.isCompleted })
            sortedHabits.add(HabitItem.Header("Выполненные:"))
            sortedHabits.addAll(habitsToShow.filter { it.isCompleted })

            requireActivity().runOnUiThread {
                adapter.updateList(sortedHabits) // ✅ Гарантируем обновление UI после обновления данных
            }
        }
    }


}
