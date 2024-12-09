package com.health.glucoguide.ui.activity.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.health.glucoguide.R
import com.health.glucoguide.databinding.ActivityFirstOnBoardingBinding
import com.health.glucoguide.ui.activity.login.LoginActivity

class OnBoardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFirstOnBoardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstOnBoardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupShapeBackground()
        setupAction()
    }

    private fun setupAction() {
        binding.btnNextOnboarding.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupShapeBackground() {
        val cornerSizeBackground = resources.getDimension(R.dimen.corner_radius_grey_background)
        val shapeAppearanceModelBackground = ShapeAppearanceModel.builder()
            .setTopLeftCorner(RoundedCornerTreatment())
            .setTopLeftCornerSize(cornerSizeBackground)
            .setTopRightCorner(RoundedCornerTreatment())
            .setTopRightCornerSize(cornerSizeBackground)
            .build()

        val shapeDrawableBackground = MaterialShapeDrawable(shapeAppearanceModelBackground)
        shapeDrawableBackground.fillColor = ContextCompat.getColorStateList(this, R.color.low_grey)
        binding.cardMaterial.apply {
            background = shapeDrawableBackground
            strokeWidth = 0
        }
    }
}