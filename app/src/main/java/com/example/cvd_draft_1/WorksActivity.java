package com.example.cvd_draft_1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class WorksActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    private Button btnToggleTheme;
    private boolean isThemeChanging = false;

    // Images, titles, and descriptions for each step
    private int[] images = {R.drawable.step1, R.drawable.step2, R.drawable.step3};
    private String[] stepTitles = {"Step 1", "Step 2", "Step 3"};
    private String[] stepDescriptions = {
            "Answer your chosen questions for an AI to generate a script that fits your needs",
            "Follow the editing steps to refine the script",
            "Record your video pitch with the generated script"
    };


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply the saved theme preference using ThemeUtils
        ThemeUtils.applyTheme(this);

        setContentView(R.layout.activity_works);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Setup ViewPager2 with adapter for image and text swiping
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        ImagePagerAdapter adapter = new ImagePagerAdapter(images, stepTitles, stepDescriptions);
        viewPager.setAdapter(adapter);

        // Initialize buttons
        Button btnJob = findViewById(R.id.btnJob);
        btnJob.setOnClickListener(view -> {
            Intent intent = new Intent(WorksActivity.this, LauncherActivity.class);
            startActivity(intent);
        });

        Button btnChangeLanguage = findViewById(R.id.btnChangeLanguage);
        btnChangeLanguage.setOnClickListener(view -> changeLanguage());

        // Setup Theme Toggle Button
        btnToggleTheme = findViewById(R.id.btnToggleTheme);
        updateThemeButtonText(); // Set initial button text based on the theme

        btnToggleTheme.setOnClickListener(view -> {
            if (!isThemeChanging) {
                toggleTheme();
            }
        });
    }

    private void toggleTheme() {
        // Use ThemeUtils to toggle the theme
        boolean isDarkModeEnabled = ThemeUtils.isDarkMode(this);
        ThemeUtils.saveThemeState(this, !isDarkModeEnabled); // Toggle and save the new theme state
        ThemeUtils.applyTheme(this); // Apply the new theme
        recreate(); // Recreate the activity to apply the new theme
    }

    private void updateThemeButtonText() {
        boolean isDarkMode = ThemeUtils.isDarkMode(this);
        if (isDarkMode) {
            btnToggleTheme.setText("Switch To Light Mode");
        } else {
            btnToggleTheme.setText("Switch To Dark Mode");
        }
    }

    private void changeLanguage() {
        final String[] languages = {"English", "français", "deutsch", "Italiano"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setTitle("Choose Language");
        mBuilder.setSingleChoiceItems(languages, -1, (dialogInterface, i) -> {
            if (i == 0) {
                setLocale("");
                recreate();
            } else if (i == 1) {
                setLocale("fr");
                recreate();
            } else if (i == 2) {
                setLocale("nl");
                recreate();
            } else if (i == 3) {
                setLocale("it");
                recreate();
            }
        });
        mBuilder.create();
        mBuilder.show();
    }

    private void setLocale(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("Language", language);
        editor.apply();
    }

    private class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ViewHolder> {
        private int[] images;
        private String[] titles;
        private String[] descriptions;

        public ImagePagerAdapter(int[] images, String[] titles, String[] descriptions) {
            this.images = images;
            this.titles = titles;
            this.descriptions = descriptions;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView titleTextView;
            TextView descriptionTextView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.stepImage);
                titleTextView = itemView.findViewById(R.id.stepTitle);
                descriptionTextView = itemView.findViewById(R.id.stepDescription);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_item_layout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.imageView.setImageResource(images[position]);
            holder.titleTextView.setText(titles[position]);
            holder.descriptionTextView.setText(descriptions[position]);

            // Theme-aware text color handling
            boolean isDarkMode = ThemeUtils.isDarkMode(holder.itemView.getContext());
            if (isDarkMode) {
                holder.titleTextView.setTextColor(Color.WHITE);
                holder.descriptionTextView.setTextColor(Color.LTGRAY);
            } else {
                holder.titleTextView.setTextColor(Color.BLACK);
                holder.descriptionTextView.setTextColor(Color.DKGRAY);
            }
        }

        @Override
        public int getItemCount() {
            return titles.length;
        }
    }
}




//package com.example.cvd_draft_1;
//
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.res.Configuration;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.viewpager2.widget.ViewPager2;
//import android.view.LayoutInflater;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.view.View;
//import java.util.Locale;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import androidx.appcompat.app.AppCompatDelegate;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//import android.provider.Settings;
//import android.widget.Button;
//
//public class WorksActivity extends AppCompatActivity {
//    FirebaseAuth mAuth;
//    private Button btnToggleTheme;
//
//    // Track if the theme is changing to prevent unnecessary recreate calls
//    private boolean isThemeChanging = false;
//    private View mainLayout; // Declare a reference for the main layout
//
//    // Images for each step
//    private int[] images = {
//            R.drawable.step1,
//            R.drawable.step2,
//            R.drawable.step3
//    };
//
//    // Titles for each step
//    private String[] stepTitles = {"Step 1", "Step 2", "Step 3"};
//
//    // Descriptions for each step
//    private String[] stepDescriptions = {
//            "Answer your chosen questions for an AI to generate a script that fits your needs",
//            "Follow the editing steps to refine the script",
//            "Record your video pitch with the generated script"
//    };
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // Apply the saved theme preference before setting the content view
//        applyThemePreference();
//
//        setContentView(R.layout.activity_works);
//
//        // Initialize FirebaseAuth instance
//        mAuth = FirebaseAuth.getInstance();
//
//        mainLayout = findViewById(R.id.main); // Reference the main layout
//        adjustThemeColors(); // Adjust the colors for dark/light theme
//
//        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
//        // Setup ViewPager2 with adapter for image and text swiping
//        ViewPager2 viewPager = findViewById(R.id.viewPager);
//        ImagePagerAdapter adapter = new ImagePagerAdapter(images, stepTitles, stepDescriptions);
//        viewPager.setAdapter(adapter);
//
//        // Initialize buttons
//        Button btnJob = findViewById(R.id.btnJob);
//        btnJob.setOnClickListener(view -> {
//            Intent intent = new Intent(WorksActivity.this, LauncherActivity.class);
//            startActivity(intent);
//        });
//
//        Button btnChangeLanguage = findViewById(R.id.btnChangeLanguage);
//        btnChangeLanguage.setOnClickListener(view -> changeLanguage());
//
//        // Setup Theme Toggle Button
//        btnToggleTheme = findViewById(R.id.btnToggleTheme);
//        updateThemeButtonText(); // Set initial button text based on the theme
//
//        btnToggleTheme.setOnClickListener(view -> {
//            if (!isThemeChanging) {
//                toggleTheme();
//            }
//        });
//    }
//
//    private void adjustThemeColors() {
//        // Determine the current theme
//        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
//        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
//            // Apply dark theme colors
//            mainLayout.setBackgroundColor(Color.BLACK);
//        } else {
//            // Apply light theme colors
//            mainLayout.setBackgroundColor(Color.WHITE);
//        }
//    }
//
//    private void changeLanguage() {
//        final String languages[] = {"English", "français", "deutsch", "Italiano"};
//        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
//        mBuilder.setTitle("Choose Language");
//        mBuilder.setSingleChoiceItems(languages, -1, (dialogInterface, i) -> {
//            if (i == 0) {
//                setLocale("");
//                recreate();
//            } else if (i == 1) {
//                setLocale("fr");
//                recreate();
//            } else if (i == 2) {
//                setLocale("nl");
//                recreate();
//            } else if (i == 3) {
//                setLocale("it");
//                recreate();
//            }
//        });
//        mBuilder.create();
//        mBuilder.show();
//    }
//
//    private void setLocale(String language) {
//        Locale locale = new Locale(language);
//        Locale.setDefault(locale);
//        Configuration configuration = new Configuration();
//        configuration.setLocale(locale);
//        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
//        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
//        editor.putString("Language", language);
//        editor.apply();
//    }
//
//    private void updateThemeButtonText() {
//        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
//        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
//            btnToggleTheme.setText("Switch To Light Mode");
//        } else {
//            btnToggleTheme.setText("Switch To Dark Mode");
//        }
//    }
//
//    private void toggleTheme() {
//        SharedPreferences.Editor editor = getSharedPreferences("ThemePrefs", MODE_PRIVATE).edit();
//        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
//
//        isThemeChanging = true; // Set flag to prevent repeated recreate calls
//        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//            editor.putBoolean("isDarkMode", false);
//        } else {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//            editor.putBoolean("isDarkMode", true);
//        }
//        editor.apply();
//        recreate();
//    }
//
//    private void applyThemePreference() {
//        SharedPreferences prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
//        boolean isDarkMode = prefs.getBoolean("isDarkMode", false);
//        if (isDarkMode) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        } else {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//        }
//    }
//    private class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ViewHolder> {
//        private int[] images;
//        private String[] titles;
//        private String[] descriptions;
//
//        public ImagePagerAdapter(int[] images, String[] titles, String[] descriptions) {
//            this.images = images;
//            this.titles = titles;
//            this.descriptions = descriptions;
//        }
//
//        public class ViewHolder extends RecyclerView.ViewHolder {
//            ImageView imageView;
//            TextView titleTextView;
//            TextView descriptionTextView;
//
//            public ViewHolder(@NonNull View itemView) {
//                super(itemView);
//                imageView = itemView.findViewById(R.id.stepImage);
//                titleTextView = itemView.findViewById(R.id.stepTitle);
//                descriptionTextView = itemView.findViewById(R.id.stepDescription);
//            }
//        }
//
//        @NonNull
//        @Override
//        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_item_layout, parent, false);
//            return new ViewHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//            holder.imageView.setImageResource(images[position]);
//            holder.titleTextView.setText(titles[position]);
//            holder.descriptionTextView.setText(descriptions[position]);
//
//            int nightModeFlags = holder.itemView.getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
//            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
//                holder.titleTextView.setTextColor(Color.WHITE);
//                holder.descriptionTextView.setTextColor(Color.LTGRAY);
//            } else {
//                holder.titleTextView.setTextColor(Color.BLACK);
//                holder.descriptionTextView.setTextColor(Color.DKGRAY);
//            }
//        }
//
//        @Override
//        public int getItemCount() {
//            return titles.length;
//        }
//    }
//}
