package com.acatapps.videomaker.custom_view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Paint
import android.graphics.Typeface
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import com.acatapps.videomaker.R
import com.acatapps.videomaker.adapter.ColorListAdapter
import com.acatapps.videomaker.adapter.FontListAdapter
import com.acatapps.videomaker.data.TextStickerAttrData
import com.acatapps.videomaker.databinding.LayoutAddTextBinding
import com.acatapps.videomaker.databinding.LayoutEditTextColorBinding
import com.acatapps.videomaker.databinding.LayoutEditTextFontsBinding
import com.acatapps.videomaker.databinding.LayoutEditTextStyleBinding
import com.acatapps.videomaker.utils.DimenUtils
import com.acatapps.videomaker.utils.Logger
import com.acatapps.videomaker.utils.RawResourceReader

@SuppressLint("ViewConstructor")
class AddTextLayout(context: Context?,  editTextSticker: EditTextSticker? = null) :
    LinearLayout(context) {
    private var mEditState = false
    private var mMainTextSticker: EditTextSticker? = null
    private val mColorListAdapter = ColorListAdapter {
        mMainTextSticker?.changeColor(it)
    }
    private val mFontListAdapter = FontListAdapter {
        onChangeFont(it)
    }
    private var mEditMode = EditMode.TEXT

    private var mTextAttrData:TextStickerAttrData?=null

    private lateinit var binding: LayoutAddTextBinding

    init {
        editTextSticker?.let {
            mMainTextSticker = it
            mEditState = true
            mTextAttrData = it.getTextAttrData()
        }
        initAttrs()
        updateIcon()

    }

    private fun initAttrs() {
        layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        binding = LayoutAddTextBinding.inflate(LayoutInflater.from(context), this, true)
        mColorListAdapter.setItemList(RawResourceReader.readTextColorFile())
        initAction()
        initView()
    }

    private fun initAction() {
        binding.icKeyboard.setOnClickListener {
            mEditMode = EditMode.TEXT
            showKeyboard()
            updateIcon()
        }
        binding.icColor.setOnClickListener {
            if (mEditMode == EditMode.COLOR) return@setOnClickListener
            hideKeyboard()
            mEditMode = EditMode.COLOR
            showChangeColorLayout()
            updateIcon()
        }
        binding.icFonts.setOnClickListener {
            if (mEditMode == EditMode.FONTS) return@setOnClickListener
            mEditMode = EditMode.FONTS
            hideKeyboard()
            showChangeFontLayout()
            updateIcon()
        }
        binding.icStyle.setOnClickListener {
            if (mEditMode == EditMode.STYLE) return@setOnClickListener
            mEditMode = EditMode.STYLE
            hideKeyboard()
            showChangeStyleLayout()
            updateIcon()
        }

    }

    fun showKeyboard() {
        mEditMode = EditMode.TEXT
        binding.toolsDetails.removeAllViews()
        openKeyboard()
        mMainTextSticker?.requestFocus()
        //hideKeyboard()

    }

    private fun initView() {
        if (mMainTextSticker == null) {
            mMainTextSticker = EditTextSticker(context, null).apply {
                id = View.generateViewId()
            }
        }

        val screenW = DimenUtils.screenWidth(context)
        val videoPreviewScale = DimenUtils.videoPreviewScale()

        binding.textContainer.layoutParams.width = (screenW*videoPreviewScale).toInt()
        binding.textContainer.layoutParams.height = (screenW*videoPreviewScale).toInt()
        binding.textContainer.addView(mMainTextSticker)


    }
    private var autoShowKeyboard = false
    fun onResume() {
        Logger.e("add text layout on resume")
        object :CountDownTimer(500,500){
            override fun onFinish() {
                if(autoShowKeyboard)
                openKeyboard()
            }

            override fun onTick(millisUntilFinished: Long) {

            }

        }.start()

    }

    private fun showChangeFontLayout() {
        val viewBinding = LayoutEditTextFontsBinding.inflate(LayoutInflater.from(context))
        showToolsView(viewBinding.root)
        viewBinding.fontsListView.adapter = mFontListAdapter
        viewBinding.fontsListView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    private fun showChangeColorLayout() {
        val viewBinding = LayoutEditTextColorBinding.inflate(LayoutInflater.from(context))
        showToolsView(viewBinding.root)
        viewBinding.textColorListView.apply {
            adapter = mColorListAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun showChangeStyleLayout() {
        val viewBinding = LayoutEditTextStyleBinding.inflate(LayoutInflater.from(context))
        showToolsView(viewBinding.root)
        viewBinding.icTextAlignLeft.setOnClickListener {
            mMainTextSticker?.changeAlign(EditTextSticker.AlignMode.LEFT)
        }
        viewBinding.icTextAlignCenter.setOnClickListener {
            mMainTextSticker?.changeAlign(EditTextSticker.AlignMode.CENTER)
        }
        viewBinding.icTextAlignRight.setOnClickListener {
            mMainTextSticker?.changeAlign(EditTextSticker.AlignMode.RIGHT)
        }
        viewBinding.textStyleRegular.setOnClickListener {
            mMainTextSticker?.changeTextStyle(Typeface.NORMAL)
        }
        viewBinding.textStyleBold.setOnClickListener {
            mMainTextSticker?.changeTextStyle(Typeface.BOLD)
        }
        viewBinding.textStyleItalic.setOnClickListener {
            mMainTextSticker?.changeTextStyle(Typeface.ITALIC)
        }
        viewBinding.textStyleBoldItalic.setOnClickListener {
            mMainTextSticker?.changeTextStyle(Typeface.BOLD_ITALIC)
        }
        viewBinding.textStyleStrike.setOnClickListener {
            mMainTextSticker?.changeTextFlag(Paint.STRIKE_THRU_TEXT_FLAG)
        }
        viewBinding.textStyleUnderline.setOnClickListener {
            mMainTextSticker?.changeTextFlag(Paint.UNDERLINE_TEXT_FLAG)
        }
    }

    private fun showToolsView(view: View) {
        binding.toolsDetails.removeAllViews()
        binding.toolsDetails.addView(view)
        playTranslationYAnimation(view)
    }


    private fun openKeyboard() {
     autoShowKeyboard = true
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)

    }

     fun hideKeyboard() {

         autoShowKeyboard = false
        val imm: InputMethodManager =
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        imm.hideSoftInputFromWindow(windowToken, 0)
    }




    private fun playTranslationYAnimation(view: View) {
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            ObjectAnimator.ofFloat(view, "alpha", 0.5f, 1f),
            ObjectAnimator.ofFloat(view, "translationY", 64f, 0f)
        )
        animatorSet.duration = 250
        animatorSet.interpolator = FastOutLinearInInterpolator()
        animatorSet.start()
    }

    fun getEditTextView(): EditTextSticker? {
        if (mMainTextSticker?.getMainText()!!.isNotEmpty()) {
            binding.textContainer.removeView(mMainTextSticker)
            return mMainTextSticker
        } else {
            Toast.makeText(context, context.getString(R.string.type_your_text), Toast.LENGTH_LONG)
                .show()
            return null
        }
    }

    private fun onChangeFont(fontId: Int) {
        mMainTextSticker?.changeFonts(fontId)
    }

    enum class EditMode {
        NONE, TEXT, FONTS, COLOR, STYLE
    }



    fun onBackPress(): EditTextSticker? {
        return if (mEditState) {
            binding.textContainer.removeView(mMainTextSticker)
            mMainTextSticker
        } else {
            null
        }
    }

    fun onCancelEdit():EditTextSticker? {
        hideKeyboard()
        return if(mEditState) {
            if(mTextAttrData == null) return null
            mTextAttrData?.let {
                mMainTextSticker?.setAttr(it)
            }
            binding.textContainer.removeView(mMainTextSticker)
            mMainTextSticker?.clearFocus()
            mMainTextSticker
        } else {
            mMainTextSticker?.clearFocus()
            null
        }
    }

    fun editState():Boolean=mEditState

    private fun updateIcon() {

        binding.icKeyboard.setImageResource(R.drawable.ic_keyboard_default)
        binding.icFonts.setImageResource(R.drawable.ic_font_default)
        binding.icColor.setImageResource(R.drawable.ic_color_default)
        binding.icStyle.setImageResource(R.drawable.ic_font_style_default)
        when(mEditMode) {
            EditMode.TEXT -> {
                binding.icKeyboard.setImageResource(R.drawable.ic_keyboard_active)
            }
            EditMode.FONTS -> {
                binding.icFonts.setImageResource(R.drawable.ic_font_active)
            }
            EditMode.STYLE -> {
                binding.icStyle.setImageResource(R.drawable.ic_font_style_active)
            }
            EditMode.COLOR -> {
                binding.icColor.setImageResource(R.drawable.ic_color_active)
            }
            EditMode.NONE -> {}
        }
    }

}
