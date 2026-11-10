package com.example.clockstf

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class WidgetConfigActivity : AppCompatActivity() {

    private lateinit var prefs: android.content.SharedPreferences
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private var currentClockColor: Int = WidgetUtils.DEFAULT_CLOCK_COLOR
    private var currentDateColor: Int = WidgetUtils.DEFAULT_DATE_COLOR
    private var currentFontSize: Int = WidgetUtils.DEFAULT_FONT_SIZE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        prefs = WidgetUtils.getPrefs(this)

        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        initUI()
        setupListeners()
    }

    private fun initUI() {
        findViewById<Switch>(R.id.switch_uppercase).isChecked =
            prefs.getBoolean(WidgetUtils.KEY_UPPERCASE, WidgetUtils.DEFAULT_UPPERCASE)
        findViewById<Switch>(R.id.switch_text_shadow).isChecked =
            prefs.getBoolean(WidgetUtils.KEY_TEXT_SHADOW, WidgetUtils.DEFAULT_TEXT_SHADOW)
        findViewById<Switch>(R.id.switch_background).isChecked =
            prefs.getBoolean(WidgetUtils.KEY_BACKGROUND, WidgetUtils.DEFAULT_BACKGROUND)
        findViewById<Switch>(R.id.switch_show_date).isChecked =
            prefs.getBoolean(WidgetUtils.KEY_SHOW_DATE, WidgetUtils.DEFAULT_SHOW_DATE)
        findViewById<Switch>(R.id.switch_date_above).isChecked =
            prefs.getBoolean(WidgetUtils.KEY_DATE_ABOVE, WidgetUtils.DEFAULT_DATE_ABOVE)

        findViewById<Switch>(R.id.switch_short_weekday).isChecked =
            prefs.getBoolean(WidgetUtils.KEY_SHORT_WEEKDAY, WidgetUtils.DEFAULT_SHORT_WEEKDAY)
        findViewById<Switch>(R.id.switch_full_weekday).isChecked =
            prefs.getBoolean(WidgetUtils.KEY_FULL_WEEKDAY, WidgetUtils.DEFAULT_FULL_WEEKDAY)
        findViewById<Switch>(R.id.switch_short_month).isChecked =
            prefs.getBoolean(WidgetUtils.KEY_SHORT_MONTH, WidgetUtils.DEFAULT_SHORT_MONTH)
        findViewById<Switch>(R.id.switch_full_month).isChecked =
            prefs.getBoolean(WidgetUtils.KEY_FULL_MONTH, WidgetUtils.DEFAULT_FULL_MONTH)

        val useDefaultClockColor = prefs.getBoolean(WidgetUtils.KEY_USE_DEFAULT_CLOCK_COLOR, WidgetUtils.DEFAULT_USE_DEFAULT_CLOCK_COLOR)
        val useDefaultDateColor = prefs.getBoolean(WidgetUtils.KEY_USE_DEFAULT_DATE_COLOR, WidgetUtils.DEFAULT_USE_DEFAULT_DATE_COLOR)

        findViewById<CheckBox>(R.id.check_default_clock_color).isChecked = useDefaultClockColor
        findViewById<CheckBox>(R.id.check_default_date_color).isChecked = useDefaultDateColor

        currentClockColor = if (useDefaultClockColor) WidgetUtils.DEFAULT_CLOCK_COLOR
            else prefs.getInt(WidgetUtils.KEY_CLOCK_COLOR, WidgetUtils.DEFAULT_CLOCK_COLOR)
        currentDateColor = if (useDefaultDateColor) WidgetUtils.DEFAULT_DATE_COLOR
            else prefs.getInt(WidgetUtils.KEY_DATE_COLOR, WidgetUtils.DEFAULT_DATE_COLOR)

        currentFontSize = prefs.getInt(WidgetUtils.KEY_FONT_SIZE, WidgetUtils.DEFAULT_FONT_SIZE)
        findViewById<SeekBar>(R.id.slider_font_size).progress = currentFontSize
        updateFontSizeDisplay()

        updateWeekdayOptions()
        updateMonthOptions()
    }

    private fun setupListeners() {
        setupSwitchListener(R.id.switch_uppercase, WidgetUtils.KEY_UPPERCASE)
        setupSwitchListener(R.id.switch_text_shadow, WidgetUtils.KEY_TEXT_SHADOW)
        setupSwitchListener(R.id.switch_background, WidgetUtils.KEY_BACKGROUND)
        setupSwitchListener(R.id.switch_show_date, WidgetUtils.KEY_SHOW_DATE)
        setupSwitchListener(R.id.switch_date_above, WidgetUtils.KEY_DATE_ABOVE)

        setupSwitchListener(R.id.switch_short_weekday, WidgetUtils.KEY_SHORT_WEEKDAY)
        setupSwitchListener(R.id.switch_full_weekday, WidgetUtils.KEY_FULL_WEEKDAY)
        setupSwitchListener(R.id.switch_short_month, WidgetUtils.KEY_SHORT_MONTH)
        setupSwitchListener(R.id.switch_full_month, WidgetUtils.KEY_FULL_MONTH)

        findViewById<Switch>(R.id.switch_short_weekday).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                findViewById<Switch>(R.id.switch_full_weekday).isChecked = false
            }
            updateWeekdayOptions()
        }

        findViewById<Switch>(R.id.switch_full_weekday).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                findViewById<Switch>(R.id.switch_short_weekday).isChecked = false
            }
            updateWeekdayOptions()
        }

        findViewById<Switch>(R.id.switch_short_month).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                findViewById<Switch>(R.id.switch_full_month).isChecked = false
            }
            updateMonthOptions()
        }

        findViewById<Switch>(R.id.switch_full_month).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                findViewById<Switch>(R.id.switch_short_month).isChecked = false
            }
            updateMonthOptions()
        }

        findViewById<CheckBox>(R.id.check_default_clock_color).setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(WidgetUtils.KEY_USE_DEFAULT_CLOCK_COLOR, isChecked).apply()
        }

        findViewById<CheckBox>(R.id.check_default_date_color).setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(WidgetUtils.KEY_USE_DEFAULT_DATE_COLOR, isChecked).apply()
        }

        findViewById<TextView>(R.id.btn_clock_color).setOnClickListener {
            showColorPicker(true)
        }

        findViewById<TextView>(R.id.btn_date_color).setOnClickListener {
            showColorPicker(false)
        }

        findViewById<SeekBar>(R.id.slider_font_size).setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    currentFontSize = progress
                    updateFontSizeDisplay()
                }
                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            }
        )

        findViewById<TextView>(R.id.btn_reset_font).setOnClickListener {
            findViewById<SeekBar>(R.id.slider_font_size).progress = WidgetUtils.DEFAULT_FONT_SIZE
            currentFontSize = WidgetUtils.DEFAULT_FONT_SIZE
            updateFontSizeDisplay()
        }

        findViewById<TextView>(R.id.btn_reset_all).setOnClickListener {
            WidgetUtils.resetAllPreferences(this)
            setResult(RESULT_OK)
            finish()
        }

        findViewById<TextView>(R.id.btn_cancel).setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        findViewById<TextView>(R.id.btn_validate).setOnClickListener {
            savePreferences()
            ClockStfWidget.updateAllWidgets(this)
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun setupSwitchListener(switchId: Int, prefKey: String) {
        findViewById<Switch>(switchId).setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(prefKey, isChecked).apply()
        }
    }

    private fun updateWeekdayOptions() {
        val shortChecked = findViewById<Switch>(R.id.switch_short_weekday).isChecked
        val fullChecked = findViewById<Switch>(R.id.switch_full_weekday).isChecked
        if (!shortChecked && !fullChecked) {
            findViewById<Switch>(R.id.switch_short_weekday).isChecked = true
        }
    }

    private fun updateMonthOptions() {
        val shortChecked = findViewById<Switch>(R.id.switch_short_month).isChecked
        val fullChecked = findViewById<Switch>(R.id.switch_full_month).isChecked
        if (!shortChecked && !fullChecked) {
            findViewById<Switch>(R.id.switch_short_month).isChecked = true
        }
    }

    private fun updateFontSizeDisplay() {
        val fontSizeValue = findViewById<TextView>(R.id.font_size_value)
        if (currentFontSize == WidgetUtils.DEFAULT_FONT_SIZE) {
            fontSizeValue.text = getString(R.string.font_size_default)
        } else {
            fontSizeValue.text = currentFontSize.toString()
        }
    }

    private fun showColorPicker(isClockColor: Boolean) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_color_picker, null)
        val colorPickerView = dialogView.findViewById<ColorPickerView>(R.id.color_picker_view)
        val alphaSlider = dialogView.findViewById<SeekBar>(R.id.alpha_slider)
        val alphaValue = dialogView.findViewById<TextView>(R.id.alpha_value)
        val hexValue = dialogView.findViewById<android.widget.EditText>(R.id.hex_value)

        val initialColor = if (isClockColor) currentClockColor else currentDateColor
        colorPickerView.setColor(initialColor)
        alphaSlider.progress = Color.alpha(initialColor)
        alphaValue.text = Color.alpha(initialColor).toString()
        hexValue.setText(WidgetUtils.colorToHex(initialColor))

        colorPickerView.setOnColorChangedListener { color ->
            val newColor = (Color.alpha(color) shl 24) or (color and 0x00FFFFFF)
            if (isClockColor) {
                currentClockColor = newColor
            } else {
                currentDateColor = newColor
            }
            hexValue.setText(WidgetUtils.colorToHex(newColor))
        }

        alphaSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                alphaValue.text = progress.toString()
                val baseColor = colorPickerView.getColor()
                val newColor = (progress shl 24) or (baseColor and 0x00FFFFFF)
                if (isClockColor) {
                    currentClockColor = newColor
                } else {
                    currentDateColor = newColor
                }
                hexValue.setText(WidgetUtils.colorToHex(newColor))
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        hexValue.setOnEditorActionListener { _, _, _ ->
            val hex = hexValue.text.toString()
            val newColor = WidgetUtils.parseColorFromHex(hex)
            colorPickerView.setColor(newColor)
            alphaSlider.progress = Color.alpha(newColor)
            alphaValue.text = Color.alpha(newColor).toString()
            if (isClockColor) {
                currentClockColor = newColor
            } else {
                currentDateColor = newColor
            }
            true
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.color_picker_title)
            .setView(dialogView)
            .setPositiveButton(R.string.btn_ok) { _, _ ->
                if (isClockColor) {
                    prefs.edit().putInt(WidgetUtils.KEY_CLOCK_COLOR, currentClockColor).apply()
                    prefs.edit().putBoolean(WidgetUtils.KEY_USE_DEFAULT_CLOCK_COLOR, false).apply()
                    findViewById<CheckBox>(R.id.check_default_clock_color).isChecked = false
                } else {
                    prefs.edit().putInt(WidgetUtils.KEY_DATE_COLOR, currentDateColor).apply()
                    prefs.edit().putBoolean(WidgetUtils.KEY_USE_DEFAULT_DATE_COLOR, false).apply()
                    findViewById<CheckBox>(R.id.check_default_date_color).isChecked = false
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .setNeutralButton(R.string.btn_default) { _, _ ->
                val defaultColor = if (isClockColor) WidgetUtils.DEFAULT_CLOCK_COLOR else WidgetUtils.DEFAULT_DATE_COLOR
                if (isClockColor) {
                    currentClockColor = defaultColor
                } else {
                    currentDateColor = defaultColor
                }
                colorPickerView.setColor(defaultColor)
                alphaSlider.progress = Color.alpha(defaultColor)
                alphaValue.text = Color.alpha(defaultColor).toString()
                hexValue.setText(WidgetUtils.colorToHex(defaultColor))
            }
            .create()

        dialog.show()
    }

    private fun savePreferences() {
        prefs.edit().apply {
            putBoolean(WidgetUtils.KEY_UPPERCASE, findViewById<Switch>(R.id.switch_uppercase).isChecked)
            putBoolean(WidgetUtils.KEY_TEXT_SHADOW, findViewById<Switch>(R.id.switch_text_shadow).isChecked)
            putBoolean(WidgetUtils.KEY_BACKGROUND, findViewById<Switch>(R.id.switch_background).isChecked)
            putBoolean(WidgetUtils.KEY_SHOW_DATE, findViewById<Switch>(R.id.switch_show_date).isChecked)
            putBoolean(WidgetUtils.KEY_DATE_ABOVE, findViewById<Switch>(R.id.switch_date_above).isChecked)
            putBoolean(WidgetUtils.KEY_SHORT_WEEKDAY, findViewById<Switch>(R.id.switch_short_weekday).isChecked)
            putBoolean(WidgetUtils.KEY_FULL_WEEKDAY, findViewById<Switch>(R.id.switch_full_weekday).isChecked)
            putBoolean(WidgetUtils.KEY_SHORT_MONTH, findViewById<Switch>(R.id.switch_short_month).isChecked)
            putBoolean(WidgetUtils.KEY_FULL_MONTH, findViewById<Switch>(R.id.switch_full_month).isChecked)
            putInt(WidgetUtils.KEY_FONT_SIZE, currentFontSize)
            apply()
        }

        val useDefaultClockColor = findViewById<CheckBox>(R.id.check_default_clock_color).isChecked
        val useDefaultDateColor = findViewById<CheckBox>(R.id.check_default_date_color).isChecked

        if (!useDefaultClockColor) {
            prefs.edit().putInt(WidgetUtils.KEY_CLOCK_COLOR, currentClockColor).apply()
        }
        if (!useDefaultDateColor) {
            prefs.edit().putInt(WidgetUtils.KEY_DATE_COLOR, currentDateColor).apply()
        }
    }
}