package guru.furu.malbum_android;

import android.app.ProgressDialog;
import android.content.Context;
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

import java.io.IOException;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class LoginFragment extends Fragment {

    private TextView serverHostnameView;
    private TextView usernameView;
    private TextView passwordView;

    private Button loginButton;

    private ProgressDialog mProgress;

    // this is modified in another thread
    private volatile boolean loginSuccess;

    private static final String SERVER_HOSTNAME = "hostname";
    private static final String SERVER_USERNAME = "username";
    private static final String SERVER_PWD = "password";

    private static final String PREF_FILE_NAME = "malbum_login_data";

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

        loginSuccess = false;

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

    private void postLogin() {

        if(loginSuccess) {
            //login succeeded
            // modify shared prefs value
            /*SharedPreferences.Editor editor =
                    getActivity().getSharedPreferences(PREF_FILE_NAME,
                            Context.MODE_PRIVATE).edit();

            editor.putString(SERVER_HOSTNAME, host);
            editor.putString(SERVER_USERNAME, username);
            editor.putString(SERVER_PWD, password);
            editor.apply();*/

        } else {
            // login failed
            Toast.makeText(getActivity(),
                    "Login failed. Please check your information and try again.",
                    Toast.LENGTH_LONG).show();
        }

    }


    private class LoginTask extends AsyncTask<String, Void, Boolean> {


        @Override
        protected Boolean doInBackground(String... params) {

            String serverHostname = params[0];
            String username = params[1];
            String password = params[2];

            Log.d(DEBUG, serverHostname + ":" + username + ":" + password);

            try {
                Thread.sleep(2000); // DEBUG
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return Boolean.FALSE; // DEBUG
        }

        @Override
        protected void onProgressUpdate(Void... progress) {

        }


        @Override
        protected void onPostExecute(Boolean result) {

            if(mProgress != null) {
                mProgress.dismiss();
            }

            loginSuccess = result;
            postLogin();
        }
    }
}
