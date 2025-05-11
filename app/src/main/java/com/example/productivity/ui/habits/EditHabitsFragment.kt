package com.example.productivity.ui.habits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.productivity.AppDatabase
import com.example.productivity.R
import com.example.productivity.data.habits.HabitsEntity
import com.example.productivity.habits.HabitsRepositoryImpl
import com.example.productivity.habits.HabitsViewModel
import com.example.productivity.habits.HabitsViewModelFactory
import com.example.productivity.ui.habits.edit.EditHabitsAdapter
import kotlinx.coroutines.launch

/**
 * Диалог для просмотра/удаления привычек.
 * Все операции (загрузка, удаление) через HabitsViewModel.
 */
class EditHabitsFragment : DialogFragment() {

    private val viewModel: HabitsViewModel by viewModels {
        HabitsViewModelFactory(
            HabitsRepositoryImpl(
                AppDatabase.getDatabase(requireContext()).habitsDao(),
                AppDatabase.getDatabase(requireContext()).habitCompletionDao()
            )
        )
    }

    private lateinit var adapter: EditHabitsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_edit_habits, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageButton>(R.id.buttonBack).setOnClickListener {
            findNavController().navigateUp()
        }

        recyclerView = view.findViewById(R.id.rv_edit_habits)
        emptyTextView = view.findViewById(R.id.tv_empty_habits)
        adapter = EditHabitsAdapter(
            habits = emptyList(),
            onEditHabit   = { habit -> navigateToEditHabit(habit) },
            onDeleteHabit = { habit -> viewModel.deleteHabit(habit) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.habits.collect { list ->
                adapter.updateList(list)
                emptyTextView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                recyclerView.visibility  = if (list.isEmpty()) View.GONE  else View.VISIBLE
            }
        }

        viewModel.loadHabits()
    }

    private fun navigateToEditHabit(habit: HabitsEntity) {
        val bundle = Bundle().apply {
            putInt("habitId", habit.id)
            putString("habitTitle", habit.title)
            putInt("iconResId", habit.iconResId)
            putInt("habitColor", habit.color)
            putParcelable("repeatType", habit.repeatType)
            putIntegerArrayList("repeatDays", habit.repeatDays?.let { ArrayList(it) })
            putString("startDate", habit.startDate)
        }
        findNavController().navigate(R.id.editOneHabitFragment, bundle)
    }
}
