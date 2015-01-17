package com.example.ccc.socialappclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";
    private UiLifecycleHelper uiHelper;

    Timer timer;
    TimerTask timerTask;

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);

        LoginButton loginButton = (LoginButton) view.findViewById(R.id.authButton);
        loginButton.setFragment(this);
        loginButton.setReadPermissions(Arrays.asList("user_likes", "user_status", "user_friends"));

        return view;
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            Log.i(TAG, "You have successfully logged to Facebook");
            if (timer != null) {
                timer.cancel();
            }

            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    Log.i(TAG, "A new request for Facebook Friends List will be done");

                    new Request(
                            Session.getActiveSession(),
                            "/me/friends",
                            null,
                            HttpMethod.GET,
                            new Request.Callback() {
                                public void onCompleted(Response response) {

                                    Log.i(TAG, "Let's hope we won't get any errors.");

                                    FacebookRequestError error = response.getError();

                                    if (error != null && response != null) {
                                        Log.e(TAG, error.toString());
                                    } else {
                                        Log.i(TAG, "Info Main Fragment");
                                        GraphObject graphObject = response.getGraphObject();
                                        JSONArray dataArray = (JSONArray) graphObject.getProperty("data");
                                        if (dataArray.length() > 0) {

                                            // Check if the user has at least one friend in the list
                                            for (int i = 0; i < dataArray.length(); i++) {

                                                JSONObject jsonObject = dataArray.optJSONObject(i);
                                                Log.i(TAG, jsonObject.toString());

                                            }
                                        }
                                    }
                                }
                            }
                    ).executeAndWait();
                }
            };


            timer.schedule(timerTask, 10000, 20000);
        } else if (state.isClosed()) {
            if (timer != null) {
                timer.cancel();
            }
            Log.i(TAG, "You have successfully logged out of Facebook.");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // For scenarios where the main activity is launched and user
        // session is not null, the session state change notification
        // may not be triggered. Trigger it if it's open/closed.
        Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed())) {
            onSessionStateChange(session, session.getState(), null);
        }

        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
        super.onPause();
    }

}

