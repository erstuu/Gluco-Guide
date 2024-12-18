package com.health.glucoguide.ui.fragment.inputdata

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.snackbar.Snackbar
import com.health.glucoguide.R
import com.health.glucoguide.databinding.FragmentInputDataBinding
import com.health.glucoguide.data.remote.request.UserData
import com.health.glucoguide.ui.activity.main.MainActivity

class InputDataFragment : Fragment() {

    private var _binding: FragmentInputDataBinding? = null
    private val binding get() = _binding!!
    private lateinit var userData: UserData
    private var isNetworkConnected: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInputDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = activity as? MainActivity
        mainActivity?.networkUtils?.observe(viewLifecycleOwner) { isConnected ->
            isNetworkConnected = isConnected
        }

        setupShapeGreenBackground()
        setupShapeDoubleCheckCard()
        setupToolbar()
        setupInputData()
    }

    private fun setupToolbar() {
        val toolbar = binding.toolbar
        toolbar.isTitleCentered = true
        toolbar.setNavigationIcon(R.drawable.ic_arrow_30)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupInputData() {
        userData = UserData()

        val genderArray = resources.getStringArray(R.array.gender_array)
        val ageArray = resources.getStringArray(R.array.age)
        val smokeHistoryArray = resources.getStringArray(R.array.smoking_history)
        val heartDiseaseArray = resources.getStringArray(R.array.heart_disease)
        val hypertensionArray = resources.getStringArray(R.array.hypertension)

        setupAutoCompleteTextView(binding.tiGender, genderArray) {
            userData.gender_encoded = it
        }
        setupAutoCompleteTextView(binding.tiAge, ageArray) {
            userData.age = it.toInt()
        }
        setupAutoCompleteTextView(binding.tiSmokingHistory, smokeHistoryArray) {
            userData.smoking_history_encoded = it
        }
        setupAutoCompleteTextView(binding.tiHeartDisease, heartDiseaseArray) {
            userData.heart_disease = it
        }
        setupAutoCompleteTextView(binding.tiHypertension, hypertensionArray) {
            userData.hypertension = it
        }

        binding.btnNext.setOnClickListener {
            if (!isNetworkConnected) {
                showSnackbar(getString(R.string.network_connection_error))
            } else {
                if (validateInputData(userData)) {
                    val toInputDataAdvance =
                        InputDataFragmentDirections.actionInputDataFragmentToInputDataAdvancedFragment(
                            userData
                        )
                    findNavController().navigate(toInputDataAdvance)

                    clearInputData()
                }
            }
        }
    }

    private fun validateInputData(userData: UserData): Boolean {
        val gender = userData.gender_encoded?.trim()
        val age = userData.age
        val smokingHistory = userData.smoking_history_encoded?.trim()
        val heartDisease = userData.heart_disease?.trim()
        val hypertension = userData.hypertension?.trim()

        return if (
            gender.isNullOrEmpty() ||
            age == 0 ||
            smokingHistory.isNullOrEmpty() ||
            heartDisease.isNullOrEmpty() ||
            hypertension.isNullOrEmpty()
            )  {

            showSnackbar(getString(R.string.error_empty_field))
            false
        } else {
            true
        }
    }

    private fun showSnackbar(errorMessage: String) {
        Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_SHORT).apply {
            setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.red))
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            setActionTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            anchorView = requireActivity().findViewById(R.id.bottom_navigation)
            setAction(getString(R.string.ok)) {
                dismiss()
            }
        }.show()
    }

    private fun clearInputData() {
        binding.tiGender.text?.clear()
        binding.tiAge.text?.clear()
        binding.tiSmokingHistory.text?.clear()
        binding.tiHeartDisease.text?.clear()
        binding.tiHypertension.text?.clear()
    }

    private fun setupAutoCompleteTextView(autoCompleteTextView: AutoCompleteTextView, dataArray: Array<String>, onItemSelected: (String) -> Unit) {
        ArrayAdapter(
            requireContext(),
            R.layout.dropdownitem,
            R.id.dropdownTextView,
            dataArray
        ).also { adapter ->
            autoCompleteTextView.setAdapter(adapter)
            autoCompleteTextView.threshold = 1
            autoCompleteTextView.post {
                autoCompleteTextView.dropDownWidth = autoCompleteTextView.width
            }
        }

        autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            onItemSelected(selectedItem)
        }
    }

    private fun setupShape(cardView: MaterialCardView, cornerSizeResId: Int, colorResId: Int) {
        val cornerSize = resources.getDimension(cornerSizeResId)
        val shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setBottomLeftCorner(RoundedCornerTreatment())
            .setBottomLeftCornerSize(cornerSize)
            .setBottomRightCorner(RoundedCornerTreatment())
            .setBottomRightCornerSize(cornerSize)
            .build()

        cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), colorResId))
        cardView.shapeAppearanceModel = shapeAppearanceModel
        cardView.strokeWidth = 0
    }

    private fun setupShapeGreenBackground() {
        setupShape(binding.materialCardView, R.dimen.corner_radius_green_background, R.color.light_army)
    }

    private fun setupShapeDoubleCheckCard() {
        setupShape(binding.doubleCheckCardView, R.dimen.corner_radius_grey_background, R.color.grey)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}