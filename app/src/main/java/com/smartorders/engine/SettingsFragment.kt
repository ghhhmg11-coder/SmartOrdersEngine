package com.smartorders.engine

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.smartorders.engine.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: PrefsManager
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PrefsManager(requireContext())
        loadSettings()
        setupListeners()
    }

    private fun loadSettings() {
        isLoading = true
        binding.switchHelper.isChecked = prefs.serviceEnabled
        binding.etMinPrice.setText(prefs.minPrice.toInt().toString())
        binding.etMaxPrice.setText(prefs.maxPrice.toInt().toString())
        binding.etMinTime.setText(prefs.minPickupTime.toString())
        binding.etMaxTime.setText(prefs.maxPickupTime.toString())
        binding.etMinDistance.setText(prefs.minDistance.toInt().toString())
        binding.etMaxDistance.setText(prefs.maxDistance.toInt().toString())
        binding.switchSound.isChecked = prefs.soundEnabled
        binding.switchVibration.isChecked = prefs.vibrationEnabled
        isLoading = false
    }

    private fun setupListeners() {
        binding.switchHelper.setOnCheckedChangeListener { _, isChecked ->
            if (!isLoading) prefs.serviceEnabled = isChecked
        }
        binding.switchSound.setOnCheckedChangeListener { _, isChecked ->
            if (!isLoading) prefs.soundEnabled = isChecked
        }
        binding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            if (!isLoading) prefs.vibrationEnabled = isChecked
        }

        addTextWatcher(binding.etMinPrice) { text ->
            text.toFloatOrNull()?.let { prefs.minPrice = it }
        }
        addTextWatcher(binding.etMaxPrice) { text ->
            text.toFloatOrNull()?.let { prefs.maxPrice = it }
        }
        addTextWatcher(binding.etMinTime) { text ->
            text.toIntOrNull()?.let { prefs.minPickupTime = it }
        }
        addTextWatcher(binding.etMaxTime) { text ->
            text.toIntOrNull()?.let { prefs.maxPickupTime = it }
        }
        addTextWatcher(binding.etMinDistance) { text ->
            text.toFloatOrNull()?.let { prefs.minDistance = it }
        }
        addTextWatcher(binding.etMaxDistance) { text ->
            text.toFloatOrNull()?.let { prefs.maxDistance = it }
        }

        binding.btnSaveSettings.setOnClickListener {
            saveSettings()
            binding.tvSaveConfirm.visibility = View.VISIBLE
            binding.tvSaveConfirm.postDelayed({
                binding.tvSaveConfirm.visibility = View.GONE
            }, 2000)
        }
    }

    private fun addTextWatcher(editText: android.widget.EditText, onChanged: (String) -> Unit) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!isLoading) onChanged(s?.toString() ?: "")
            }
        })
    }

    private fun saveSettings() {
        binding.etMinPrice.text.toString().toFloatOrNull()?.let { prefs.minPrice = it }
        binding.etMaxPrice.text.toString().toFloatOrNull()?.let { prefs.maxPrice = it }
        binding.etMinTime.text.toString().toIntOrNull()?.let { prefs.minPickupTime = it }
        binding.etMaxTime.text.toString().toIntOrNull()?.let { prefs.maxPickupTime = it }
        binding.etMinDistance.text.toString().toFloatOrNull()?.let { prefs.minDistance = it }
        binding.etMaxDistance.text.toString().toFloatOrNull()?.let { prefs.maxDistance = it }
        prefs.serviceEnabled = binding.switchHelper.isChecked
        prefs.soundEnabled = binding.switchSound.isChecked
        prefs.vibrationEnabled = binding.switchVibration.isChecked
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
