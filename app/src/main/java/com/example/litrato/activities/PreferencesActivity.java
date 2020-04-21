package com.example.litrato.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.litrato.R;
import com.example.litrato.activities.tools.Preference;
import com.example.litrato.activities.tools.PreferenceManager;
import com.example.litrato.activities.ui.ColorTheme;

/**
 * This class is where the user can view and change app's preferences.
 *
 * @author Thomas Barillot, Rodin Duhayon, Alex Fournier, Marion de Oliveira
 * @version 1.0
 * @since   2020-31-01
 */
public class PreferencesActivity extends AppCompatActivity {

    private TextView settingsTitle;

    private TextView darkModeTitle;
    private Switch darkModeSwitch;

    private TextView importedBmpTitle;
    private TextView importedBmpDesc;
    private Spinner importedBmpSpinner;

    private TextView miniatureTitle;
    private TextView miniatureDesc;
    private Spinner miniatureSpinner;

    private TextView saveOriginalResolutionTitle;
    private TextView saveOriginalResolutionDesc;
    private Switch saveOriginalResolutionSwitch;

    private TextView openHistogramByDefaultTitle;
    private TextView openHistogramByDefaultDesc;
    private Switch openHistogramByDefaultSwitch;

    private ImageButton returnButton;

    private final String[] importedBmpArray = new String[] {
            "200", "350", "500", "750", "1000", "1200", "1500", "2000"
    };

    private final String[] miniatureArray = new String[] {
            "30", "50", "75", "100", "120", "150", "200", "250"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        settingsTitle = findViewById(R.id.exifTitle);
        returnButton =  findViewById(R.id.returnButton);

        darkModeTitle = findViewById(R.id.cameraModel);
        darkModeSwitch = findViewById(R.id.darkModeSwitch);

        importedBmpTitle = findViewById(R.id.importedBmpTitle);
        importedBmpDesc = findViewById(R.id.importedBmpDesc);
        importedBmpSpinner = findViewById(R.id.importedBmpSpinner);

        miniatureTitle = findViewById(R.id.miniatureTitle);
        miniatureDesc = findViewById(R.id.miniatureDesc);
        miniatureSpinner = findViewById(R.id.miniatureSpinner);

        saveOriginalResolutionTitle = findViewById(R.id.saveOriginalResolutionTitle);
        saveOriginalResolutionDesc = findViewById(R.id.saveOriginalResolutionDesc);
        saveOriginalResolutionSwitch = findViewById(R.id.saveOriginalResolutionSwitch);

        openHistogramByDefaultTitle = findViewById(R.id.openHistogramByDefaultTitle);
        openHistogramByDefaultDesc = findViewById(R.id.openHistogramByDefaultDesc);
        openHistogramByDefaultSwitch = findViewById(R.id.openHistogramByDefaultSwitch);


        applyColorTheme();
        initializeListener();

        darkModeSwitch.setChecked(PreferenceManager.getBoolean(getApplicationContext(), Preference.DARK_MODE));

        /*SPINNER*/
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_item, importedBmpArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        importedBmpSpinner.setAdapter(adapter);

        adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_item, miniatureArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        miniatureSpinner.setAdapter(adapter);

        int tmp = PreferenceManager.getInt(getApplicationContext(), Preference.IMPORTED_BMP_SIZE);
        int index = 0;
        for (String str:importedBmpArray) {
            if (str.equals(Integer.toString(tmp))) importedBmpSpinner.setSelection(index);
            index++;
        }

        tmp = PreferenceManager.getInt(getApplicationContext(), Preference.MINIATURE_BMP_SIZE);
        index = 0;
        for (String str:miniatureArray) {
            if (str.equals(Integer.toString(tmp))) miniatureSpinner.setSelection(index);
            index++;
        }

        saveOriginalResolutionSwitch.setChecked(PreferenceManager.getBoolean(getApplicationContext(), Preference.SAVE_ORIGINAL_RESOLUTION));
        openHistogramByDefaultSwitch.setChecked(PreferenceManager.getBoolean(getApplicationContext(), Preference.OPEN_HISTOGRAM_BY_DEFAULT));


    }


    private void applyColorTheme() {
        ColorTheme.setColorTheme(getApplicationContext());
        ColorTheme.window(getApplicationContext(), getWindow());

        ColorTheme.textView(settingsTitle);
        ColorTheme.textView(darkModeTitle);
        ColorTheme.textView(importedBmpTitle);
        ColorTheme.textView(importedBmpDesc);
        ColorTheme.textView(miniatureTitle);
        ColorTheme.textView(miniatureDesc);
        ColorTheme.textView(saveOriginalResolutionTitle);
        ColorTheme.textView(saveOriginalResolutionDesc);
        ColorTheme.textView(openHistogramByDefaultTitle);
        ColorTheme.textView(openHistogramByDefaultDesc);

        ColorTheme.switchL(darkModeSwitch);
        ColorTheme.switchL(saveOriginalResolutionSwitch);
        ColorTheme.switchL(openHistogramByDefaultSwitch);

        ColorTheme.spinner(miniatureSpinner);
        ColorTheme.spinner(importedBmpSpinner);

        ColorTheme.icon(getApplicationContext(), returnButton, R.drawable.icon_goback);
    }

    private void initializeListener() {

        // Adds listener for the first switch
        darkModeSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferenceManager.setBoolean(getApplicationContext(), Preference.DARK_MODE, darkModeSwitch.isChecked());
                applyColorTheme();
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PreferenceManager.setBoolean(getApplicationContext(), Preference.DARK_MODE, darkModeSwitch.isChecked());
                PreferenceManager.setInt(getApplicationContext(), Preference.IMPORTED_BMP_SIZE, Integer.parseInt(importedBmpSpinner.getSelectedItem().toString()));
                PreferenceManager.setInt(getApplicationContext(), Preference.MINIATURE_BMP_SIZE, Integer.parseInt(miniatureSpinner.getSelectedItem().toString()));
                PreferenceManager.setBoolean(getApplicationContext(), Preference.SAVE_ORIGINAL_RESOLUTION, saveOriginalResolutionSwitch.isChecked());
                PreferenceManager.setBoolean(getApplicationContext(), Preference.OPEN_HISTOGRAM_BY_DEFAULT, openHistogramByDefaultSwitch.isChecked());
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        returnButton.performClick();
    }
}
