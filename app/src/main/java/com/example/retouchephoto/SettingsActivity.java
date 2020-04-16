package com.example.retouchephoto;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    TextView settingsTitle;

    TextView darkModeTitle;
    Switch darkModeSwitch;

    TextView importedBmpTitle;
    TextView importedBmpDesc;
    Spinner importedBmpSpinner;

    TextView miniatureTitle;
    TextView miniatureDesc;
    Spinner miniatureSpinner;

    ImageButton returnButton;

    String[] importedBmpArray = new String[] {
            "200", "350", "500", "750", "1000", "1200", "1500", "2000"
    };

    String[] miniatureArray = new String[] {
            "30", "50", "75", "100", "120", "150", "200", "250"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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



        applyColorTheme();
        initializeListener();

        darkModeSwitch.setChecked(MainActivity.preferences.getBoolean(Settings.PREFERENCE_DARK_MODE, Settings.DEFAULT_DARK_MODE));

        /*SPINNER*/
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_item, importedBmpArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        importedBmpSpinner.setAdapter(adapter);

        adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_item, miniatureArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        miniatureSpinner.setAdapter(adapter);

        int tmp = MainActivity.preferences.getInt(Settings.PREFERENCE_IMPORTED_BMP_SIZE, Settings.DEFAULT_IMPORTED_BMP_SIZE);
        int index = 0;
        for (String str:importedBmpArray) {
            if (str.equals(Integer.toString(tmp))) importedBmpSpinner.setSelection(index);
            index++;
        }

        tmp = MainActivity.preferences.getInt(Settings.PREFERENCE_MINIATURE_BMP_SIZE, Settings.DEFAULT_MINIATURE_BMP_SIZE);
        index = 0;
        for (String str:miniatureArray) {
            if (str.equals(Integer.toString(tmp))) miniatureSpinner.setSelection(index);
            index++;
        }


    }


    private void applyColorTheme() {

        Settings.setColorTheme(MainActivity.preferences.getBoolean(Settings.PREFERENCE_DARK_MODE, Settings.DEFAULT_DARK_MODE));

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Settings.COLOR_BACKGROUND);
        window.getDecorView().setBackgroundColor(Settings.COLOR_BACKGROUND);

        settingsTitle.setTextColor(Settings.COLOR_TEXT);

        darkModeTitle.setTextColor(Settings.COLOR_TEXT);
        darkModeSwitch.setTextColor(Settings.COLOR_TEXT);

        importedBmpTitle.setTextColor(Settings.COLOR_TEXT);
        importedBmpDesc.setTextColor(Settings.COLOR_TEXT);

        miniatureTitle.setTextColor(Settings.COLOR_TEXT);
        miniatureDesc.setTextColor(Settings.COLOR_TEXT);

        ColorStateList thumbStates = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_enabled},
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        Settings.COLOR_TEXT,
                        Settings.COLOR_TEXT,
                        Settings.COLOR_TEXT
                }
        );
        darkModeSwitch.setThumbTintList(thumbStates);

        importedBmpSpinner.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ((TextView) importedBmpSpinner.getSelectedView()).setTextColor(Settings.COLOR_TEXT);
            }
        });

        miniatureSpinner.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ((TextView) miniatureSpinner.getSelectedView()).setTextColor(Settings.COLOR_TEXT);
            }
        });

        returnButton.setImageDrawable(ImageTools.getThemedIcon(getApplicationContext(), R.drawable.goback));

    }

    private void initializeListener() {

        // Adds listener for the first switch
        darkModeSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = MainActivity.preferences.edit();
                editor.putBoolean(Settings.PREFERENCE_DARK_MODE, darkModeSwitch.isChecked());
                editor.apply();
                applyColorTheme();
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = MainActivity.preferences.edit();
                editor.putBoolean(Settings.PREFERENCE_DARK_MODE, darkModeSwitch.isChecked());
                editor.putInt(Settings.PREFERENCE_IMPORTED_BMP_SIZE, Integer.parseInt(importedBmpSpinner.getSelectedItem().toString()));
                editor.putInt(Settings.PREFERENCE_MINIATURE_BMP_SIZE, Integer.parseInt(miniatureSpinner.getSelectedItem().toString()));
                editor.apply();
                finish();
            }
        });



    }

    @Override
    public void onBackPressed() {
        returnButton.performClick();
    }


}
