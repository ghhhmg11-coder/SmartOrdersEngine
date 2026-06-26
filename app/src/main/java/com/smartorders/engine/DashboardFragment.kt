package com.smartorders.engine

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.smartorders.engine.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: PrefsManager
    private var demoManager: DemoModeManager? = null
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PrefsManager(requireContext())
        notificationHelper = NotificationHelper(requireContext())

        setupObservers()
        setupClickListeners()
        updateServiceStatusUI()
    }

    private fun setupObservers() {
        AppRepository.serviceRunning.observe(viewLifecycleOwner) {
            updateServiceStatusUI()
        }

        AppRepository.sessionStats.observe(viewLifecycleOwner) { stats ->
            binding.tvDetectedCount.text = stats.detected.toString()
            binding.tvMatchedCount.text = stats.matched.toString()
            binding.tvRejectedCount.text = stats.rejected.toString()
            binding.tvTotalEarnings.text = "%.2f".format(stats.totalEarnings)
        }

        AppRepository.lastTrip.observe(viewLifecycleOwner) { trip ->
            if (trip != null) {
                binding.tvLastPrice.text = trip.priceFormatted
                binding.tvLastTime.text = trip.timeFormatted
                binding.tvLastDistance.text = trip.distanceFormatted
                binding.tvLastPackage.text = trip.packageName.ifEmpty { "--" }
                val matchText = if (trip.isMatched) "✓ مناسب" else "✗ غير مناسب"
                binding.tvLastMatchStatus.text = matchText
                binding.tvLastMatchStatus.setTextColor(
                    if (trip.isMatched)
                        requireContext().getColor(R.color.green_match)
                    else
                        requireContext().getColor(R.color.red_reject)
                )
                binding.cardLastTrip.visibility = View.VISIBLE
            }
        }

        AppRepository.lastAutoAcceptResult.observe(viewLifecycleOwner) { result ->
            if (result.isNotEmpty()) {
                binding.tvAutoAcceptResult.text = result
                binding.tvAutoAcceptResult.visibility = View.VISIBLE
                val isSuccess = result.startsWith("✓")
                binding.tvAutoAcceptResult.setTextColor(
                    if (isSuccess)
                        requireContext().getColor(R.color.green_match)
                    else
                        requireContext().getColor(R.color.yellow_warning)
                )
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnAccessibilitySettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        binding.switchService.setOnCheckedChangeListener { _, isChecked ->
            prefs.serviceEnabled = isChecked
            updateServiceStatusUI()
        }

        binding.switchAutoAccept.setOnCheckedChangeListener { _, isChecked ->
            prefs.autoAcceptEnabled = isChecked
            binding.tvAutoAcceptResult.visibility = View.GONE
            AppRepository.lastAutoAcceptResult.value = ""
            updateAutoAcceptUI()
        }

        binding.btnDemoMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.demoMode = isChecked
            if (isChecked) startDemoMode() else stopDemoMode()
        }

        binding.btnResetStats.setOnClickListener {
            AppRepository.resetStats()
        }
    }

    private fun updateServiceStatusUI() {
        val isRunning = AppRepository.serviceRunning.value == true
        val isEnabled = prefs.serviceEnabled

        binding.switchService.isChecked = isEnabled

        if (isRunning && isEnabled) {
            binding.tvServiceStatus.text = "نشط"
            binding.tvServiceStatus.setTextColor(requireContext().getColor(R.color.green_match))
            binding.ivServiceIndicator.setColorFilter(requireContext().getColor(R.color.green_match))
        } else if (isRunning && !isEnabled) {
            binding.tvServiceStatus.text = "متوقف مؤقتاً"
            binding.tvServiceStatus.setTextColor(requireContext().getColor(R.color.yellow_warning))
            binding.ivServiceIndicator.setColorFilter(requireContext().getColor(R.color.yellow_warning))
        } else {
            binding.tvServiceStatus.text = "غير مفعّل"
            binding.tvServiceStatus.setTextColor(requireContext().getColor(R.color.red_reject))
            binding.ivServiceIndicator.setColorFilter(requireContext().getColor(R.color.red_reject))
        }

        binding.btnDemoMode.isChecked = prefs.demoMode
        updateAutoAcceptUI()
    }

    private fun updateAutoAcceptUI() {
        val enabled = prefs.autoAcceptEnabled
        binding.switchAutoAccept.isChecked = enabled
        binding.tvAutoAcceptWarning.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    private fun startDemoMode() {
        demoManager?.stop()
        demoManager = DemoModeManager { trip ->
            val isMatch = prefs.tripMatchesSettings(trip)
            val finalTrip = trip.copy(isMatched = isMatch)
            AppRepository.rawScreenText.postValue(trip.rawText)
            AppRepository.recordTrip(finalTrip)
            if (isMatch) {
                if (prefs.soundEnabled) notificationHelper.playSound()
                if (prefs.vibrationEnabled) notificationHelper.vibrate()
                notificationHelper.sendTripMatchNotification(finalTrip)
            }
        }
        demoManager?.start()
    }

    private fun stopDemoMode() {
        demoManager?.stop()
        demoManager = null
    }

    override fun onDestroyView() {
        stopDemoMode()
        _binding = null
        super.onDestroyView()
    }
}
