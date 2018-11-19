package com.example.brettmacdonald.kingabis;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.loopj.android.http.*;
import org.json.JSONObject;
import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity 
{
    public static AsyncHttpClient client = new AsyncHttpClient();
    public static Customer user;
    public static String url = "https://kingabis-217007.appspot.com/api/";
    //public static String url = "http://10.0.2.2:3000/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button guestBut =  (Button) findViewById(R.id.guestLoginButton);
        final Button loginBut =  (Button) findViewById(R.id.loginButton);

        //overrides below are use to make buttons unclickable once they been pressed preventing
        //multiple login requests
        guestBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guestBut.setClickable(false);
                guestLoginButton(view);

            }
        });

        loginBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginBut.setClickable(false);
                loginButton(view);
            }
        });
    }



    /** Login Button Response */
    void loginButton(View view)
    {
        /** Grab the username/password text entrys in the view */
        TextView username = findViewById(R.id.usernameEntry);
        TextView password = findViewById(R.id.passwordEntry);
        RequestParams params = new RequestParams();
        params.put("username", username.getText());
        params.put("password", password.getText());

        /** Send the login request to the server */
        login(params);
    }

    /**
     *  Guest Login Button Response
     *
     *  When user continues as guest, automatically create a new guest account for them
     */
    void guestLoginButton(View view)
    {

        /** There is code in the backend that recognizes this guest login and creates a new guest */
        RequestParams params = new RequestParams();
        params.put("username", "guest");
        params.put("password", "guest");

        /** Send the login request to the server */
        login(params);
    }

    /** Sends the login request to the server */
    void login(RequestParams params)
    {
        /** Send the login request to the server */
        client.post(url + "Customers/login", params, new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response)
            {
                // called when response HTTP status is "200 OK"
                try
                {
                    /** Parse the login data and create customer object containing id, accesstoken, etc */
                    JSONObject res = new JSONObject(new String(response));
                    user = new Customer(res);
                    /** Automatically switch to the menu screen after successful login */
                    Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                    startActivity(intent);
                }
                catch (Throwable t)
                {
                    System.out.println(t);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e)
            {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                loginFailedAlert(statusCode);
            }
        });
    }

    /** Create a popup dialog box to show the user the login failed */
    void loginFailedAlert(int statusCode)
    {
        String errTitle = "Login Failed";
        String errMsg = "Error code: " + statusCode;
        if (statusCode == 404)
        {
            errMsg = "Cannot connect to server. Please try again.";
        }
        else if (statusCode == 401)
        {
            errMsg = "Incorrect username/password.";
        }

        /** Show a pop up dialog telling the username/password is wrong */
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(errMsg)
                .setTitle(errTitle);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
