package guru.furu.malbum_android;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * Login Activity's fragment.
 */
public class LoginFragment extends Fragment {

    private TextView serverHostnameView;
    private TextView usernameView;
    private TextView passwordView;

    private Button loginButton;

    private ProgressDialog mProgress;

    public static final String PREF_FILE_NAME = "malbum_login_data";

    private static final String DEBUG = "LoginFragment";

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    public LoginFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.fragment_login, container, false);

        serverHostnameView = (TextView) v.findViewById(R.id.server_hostname);
        usernameView = (TextView) v.findViewById(R.id.login_username);
        passwordView = (TextView) v.findViewById(R.id.login_password);

        loginButton = (Button) v.findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String serverHostname = serverHostnameView.getText().toString();
                String username = usernameView.getText().toString();
                String password = passwordView.getText().toString();

                attemptLogin(serverHostname, username, password);
            }
        });
        return v;
    }


    private void attemptLogin(String host, String username, String password) {

        // attempt contact of server here
        mProgress = ProgressDialog.show(getActivity(), "Contacting Server",
                "Attempting login...");

        new LoginTask().execute(host, username, password);

    }

    @SuppressLint("CommitPrefEdits")
    private void postLogin(MalbumUser user) {

        // null result signals connection failure

        if(user != null) {
            //login succeeded
            // modify shared prefs value
            SharedPreferences.Editor editor =
                    getActivity().getSharedPreferences(PREF_FILE_NAME,
                            Context.MODE_PRIVATE).edit();

            /*editor.putString("hostname", user.getHostname());
            editor.putString("uname", user.getUname());
            editor.putString("uname_lower", user.getUname_lower());
            editor.putString("fname", user.getFname());
            editor.putString("lname", user.getLname());
            editor.putString("api_key", user.getApi_key());
            editor.putString("user_id", user.getUser_id());
            editor.commit();*/

            Toast.makeText(getActivity(),
                    "Login Success!",
                    Toast.LENGTH_SHORT).show();

            // launch tabbed activity
            Intent i = new Intent(getActivity(), TabbedGalleryActivity.class);
            startActivity(i);

        } else {
            // login failed
            Toast.makeText(getActivity(),
                    "Login failed. Please check your information and try again.",
                    Toast.LENGTH_LONG).show();
        }

    }


    // Executes a login attempt in another thread.
    // Sets the value of loginStatus upon completion.
    private class LoginTask extends AsyncTask<String, Void, MalbumUser> {


        @Override
        protected MalbumUser doInBackground(String... params) {

            String serverHostname = params[0];
            String username = params[1];
            String password = params[2];

            Log.d(DEBUG, serverHostname + ":" + username + ":" + password);

            try {

                MalbumUser userObject = new ServerConnect(serverHostname, username, password)
                        .attemptLogin();

                debugUserObject(userObject);

                return userObject;


            } catch (JSONException joe) {
                joe.printStackTrace();
                Log.e(DEBUG, "JSON processing error");
            } catch (IOException ioe) {
                ioe.printStackTrace();
                Log.e(DEBUG, "Problem connecting to hostname");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... progress) {
            // not used
        }


        @Override
        protected void onPostExecute(MalbumUser result) {

            if(mProgress != null) {
                mProgress.dismiss();
            }

            // null result signals connection failure
            postLogin(result);


        }
    }

    private void debugUserObject(MalbumUser user) {

        Log.d(DEBUG, "Username: " + user.getUname());
        Log.d(DEBUG, "fname: " + user.getFname());
        Log.d(DEBUG, "user_id: " + user.getUser_id());
        Log.d(DEBUG, "api_key: " + user.getApi_key());

    }
}
