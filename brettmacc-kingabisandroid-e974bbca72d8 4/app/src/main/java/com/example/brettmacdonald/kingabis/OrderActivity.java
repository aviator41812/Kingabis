package com.example.brettmacdonald.kingabis;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.loopj.android.http.AsyncHttpResponseHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import cz.msebera.android.httpclient.Header;

public class OrderActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        getOrderItems();
    }

    /** Logout Button Response */
    void logoutButton(View view)
    {
        /** send the logout request and switch back to the login screen */
        client.post(url + "Customers/logout" + user.access_token, new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response)
            {
                // called when response HTTP status is "200 OK"
                try
                {
                    System.out.println("Successfully logged out");
                    /** switch to main menu */
                    Intent intent = new Intent(OrderActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                catch (Throwable t)
                {
                    System.out.println("Throw error");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e)
            {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                requestFailedAlert(statusCode);
                System.out.println(new String(errorResponse));
            }
        });
    }

    /** View Menu Screen Button Response for customers */
    void viewMenuButton(View view)
    {
        Intent intent = new Intent(OrderActivity.this, MenuActivity.class);
        startActivity(intent);
    }

    /** Get all items for the current order from the server and populate order */
    void getOrderItems()
    {
        /** Send the order request to the server */
        client.get(url + "Orders/" + user.orderId + "/items" + user.access_token, new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response)
            {
                // called when response HTTP status is "200 OK"
                try
                {
                    JSONArray res = new JSONArray(new String(response));
                    LinearLayout itemList = findViewById(R.id.orderList);
                    /** iterate through the order items and add them to the layout */
                    for(int i = 0; i < res.length(); i++)
                    {
                        TextView txt = newItem(res.get(i).toString());
                        itemList.addView(txt);
                    }
                }
                catch (Throwable t)
                {
                    System.out.println("Throw error");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e)
            {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                requestFailedAlert(statusCode);
                System.out.println(new String(errorResponse));
            }
        });
    }

    /** when adding all the items to the order view, add deletion functionality to each item */
    TextView newItem(String text)
    {
        /** create the item */
        final TextView textView = new TextView(OrderActivity.this);
        textView.setText(text);
        /** add the delete functionality */
        textView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(OrderActivity.this);
                builder.setTitle("Remove Item From Order?");
                /** add an alert to confirm or cancel the order delete request */
                builder.setPositiveButton("Remove", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        // User clicked OK button
                        deleteItem(textView);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        return textView;
    }

    /** sends the request to the server to remove an item from the current order */
    void deleteItem(final TextView textView)
    {
        String itemId = "";
        try
        {
            JSONObject json = new JSONObject(textView.getText().toString());
            itemId = json.get("id").toString();
        }
        catch (Throwable t)
        {
            System.out.println("Throw error");
        }

        client.delete(url + "Orders/" + user.orderId + "/items/rel/" + itemId + user.access_token, new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response)
            {
                // called when response HTTP status is "200 OK"
                try
                {
                    System.out.println("Successfully deleted item to database");
                    /** if item is removed from order, update the order ui right away */
                    LinearLayout itemList = findViewById(R.id.orderList);
                    itemList.removeView(textView);
                }
                catch (Throwable t)
                {
                    System.out.println("Throw error");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e)
            {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                requestFailedAlert(statusCode);
                System.out.println(new String(errorResponse));
            }
        });
    }

    /** Create a popup dialog box to show the user the request failed */
    void requestFailedAlert(int statusCode)
    {
        String errTitle = "Request Failed";
        String errMsg = "Error code: " + statusCode;
        if (statusCode == 404)
        {
            errMsg = "Cannot connect to server. Please try again.";
        }
        else if (statusCode == 401)
        {
            errMsg = "Unauthorized request. You do not have the permissions to do this.";
        }

        /** Show a pop up dialog telling the username/password is wrong */
        AlertDialog.Builder builder = new AlertDialog.Builder(OrderActivity.this);
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
