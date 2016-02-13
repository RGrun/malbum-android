package guru.furu.malbum_android.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.Map;

import guru.furu.malbum_android.R;
import guru.furu.malbum_android.fragments.LoginFragment;
import guru.furu.malbum_android.fragments.SingleUserAlbumFragment;
import guru.furu.malbum_android.model.MalbumUser;

/**
 * Created by richard on 1/18/16.
 *
 * The Activity that holds a SingleUserAlbumFragment.
 */
public class SingleUserAlbumActivity extends AppCompatActivity {

    private MalbumUser malbumUser;
    private Toolbar toolbar;

    private Fragment createFragment() {
        return SingleUserAlbumFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fragment);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }

        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
        String usernameOfAlbumOwner = getIntent().getStringExtra("uname");

        toolbar.setTitle(usernameOfAlbumOwner + "'s Photos");
        setSupportActionBar(toolbar);


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

    public void setToolbarText(String newTitle) {
        toolbar.setTitle(newTitle);
    }

    public static Intent newIntent(Context context, String uname) {

        Intent i = new Intent(context, SingleUserAlbumActivity.class);

        i.putExtra("uname", uname);

        return i;


    }
}
