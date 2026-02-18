package com.deepdefender.nagarsewahackthon;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.deepdefender.nagarsewahackthon.UserFragments.UserExploreFragment;
import com.deepdefender.nagarsewahackthon.UserFragments.UserHistoryFragment;
import com.deepdefender.nagarsewahackthon.UserFragments.UserHomeFragment;
import com.deepdefender.nagarsewahackthon.UserFragments.UserProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Load default fragment
        loadFragment(new UserHomeFragment());

        bottomNavigation.setOnItemSelectedListener(item -> {

            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new UserHomeFragment();
            }
            else if (item.getItemId() == R.id.nav_explore) {
                selectedFragment = new UserExploreFragment();
            }
            else if (item.getItemId() == R.id.nav_history) {
                selectedFragment = new UserHistoryFragment();
            }
            else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new UserProfileFragment();
            }

            return loadFragment(selectedFragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
