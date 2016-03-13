package guru.furu.malbum_android.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Map;

import guru.furu.malbum_android.R;
import guru.furu.malbum_android.fragments.AlbumFragment;
import guru.furu.malbum_android.fragments.LatestFragment;
import guru.furu.malbum_android.fragments.LoginFragment;
import guru.furu.malbum_android.fragments.UploadFragment;
import guru.furu.malbum_android.model.MalbumUser;

/**
 * Created by richard on 1/10/16.
 *
 * This class represents the Activity behind a tabbed interface.
 *
 * Tab 1: various user galleries.
 * Tab 2: take and upload a new photo.
 */
public class TabbedGalleryActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private MalbumUser malbumUser;

    public static final int LATEST_TAB = 0;
    public static final int ALBUM_TAB = 1;
    public static final int UPLOAD_TAB = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed_pager);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Latest Images");
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Latest"));
        tabLayout.addTab(tabLayout.newTab().setText("Albums"));
        tabLayout.addTab(tabLayout.newTab().setText("Upload"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager) findViewById(R.id.pager);

        FragmentManager fragmentManager = getSupportFragmentManager();
        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {

                switch(position) {
                    case 0:
                        return LatestFragment.newInstance();

                    case 1:
                        return AlbumFragment.newInstance();

                    case 2:
                        return UploadFragment.newInstance();

                }

                // if we get here, something went very wrong
                return null;

            }

            @Override
            public int getCount() {
                return 3;
            }
        });

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                int pos = tab.getPosition();

                switch (pos) {
                    case 0:
                        toolbar.setTitle("Latest Images");
                        //setSupportActionBar(toolbar);
                        break;
                    case 1:
                        toolbar.setTitle("Recent Images By User");
                        //setSupportActionBar(toolbar);
                        break;
                    case 2:
                        toolbar.setTitle("Upload New Image");
                        //setSupportActionBar(toolbar);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        this.malbumUser = rebuildUser();

    }

    private MalbumUser rebuildUser() {

        Map<String, ?> userMap = getSharedPreferences(LoginFragment.PREF_FILE_NAME,
                Context.MODE_PRIVATE).getAll();

        String uname = (String) userMap.get("uname");
        String uname_lower = (String) userMap.get("uname_lower");
        String fname = (String) userMap.get("fname");
        String lname = (String) userMap.get("lname");
        String api_key = (String) userMap.get("api_key");
        String user_id = (String) userMap.get("user_id");
        String hostname = (String) userMap.get("hostname");

        return new MalbumUser(uname, user_id, uname_lower, fname, lname, api_key, hostname);

    }

    public MalbumUser getMalbumUser() {
        return malbumUser;
    }


    public void setCurrentTab(int tabIndex) {
        viewPager.setCurrentItem(tabIndex);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tabbed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {

            // remove stored prefs data

            SharedPreferences.Editor editor =
                    getSharedPreferences(LoginFragment.PREF_FILE_NAME,
                            Context.MODE_PRIVATE).edit();

            editor.clear();

            editor.commit();

            // return to loginActivity
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
