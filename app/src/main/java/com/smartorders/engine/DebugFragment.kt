package com.smartorders.engine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.smartorders.engine.databinding.FragmentDebugBinding

class DebugFragment : Fragment() {

    private var _binding: FragmentDebugBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDebugBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()

        binding.btnClearDebug.setOnClickListener {
            AppRepository.rawScreenText.postValue("")
            binding.tvRawText.text = getString(R.string.debug_no_data)
        }
    }

    private fun setupObservers() {
        AppRepository.rawScreenText.observe(viewLifecycleOwner) { text ->
            binding.tvRawText.text = text.ifEmpty { getString(R.string.debug_no_data) }
        }

        AppRepository.lastTrip.observe(viewLifecycleOwner) { trip ->
            if (trip != null) {
                binding.tvParsedPrice.text = trip.priceFormatted
                binding.tvParsedTime.text = trip.timeFormatted
                binding.tvParsedDistance.text = trip.distanceFormatted
                binding.tvPackageName.text = trip.packageName.ifEmpty { "--" }
                binding.tvActionLabels.text = trip.actionLabels.joinToString(", ").ifEmpty { "--" }
                val matchText = if (trip.isMatched) "✓ مناسب" else "✗ غير مناسب"
                binding.tvMatchStatus.text = matchText
                binding.tvMatchStatus.setTextColor(
                    if (trip.isMatched)
                        requireContext().getColor(R.color.green_match)
                    else
                        requireContext().getColor(R.color.red_reject)
                )
                binding.tvTimestamp.text = trip.formattedTimestamp
            } else {
                clearDebugFields()
            }
        }
    }

    private fun clearDebugFields() {
        binding.tvParsedPrice.text = "--"
        binding.tvParsedTime.text = "--"
        binding.tvParsedDistance.text = "--"
        binding.tvPackageName.text = "--"
        binding.tvActionLabels.text = "--"
        binding.tvMatchStatus.text = "--"
        binding.tvTimestamp.text = "--"
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
