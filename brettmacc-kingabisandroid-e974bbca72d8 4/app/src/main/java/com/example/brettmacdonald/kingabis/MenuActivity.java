package com.example.brettmacdonald.kingabis;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MenuActivity extends MainActivity
{
    ListView list_View;
    ArrayList<Item> list = new ArrayList<>();
    MenuListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        list_View = (ListView) findViewById(R.id.list_view);

        /** hide add item button if the user role is a customer */
        if (user.role.equals("customer"))
        {
            Button addItem = findViewById(R.id.addItemButton);
            addItem.setVisibility(View.INVISIBLE);
        }
        /** hide view order button if the user role is employee */
        else
        {
            Button orderButton = findViewById(R.id.viewOrderButton);
            orderButton.setVisibility(View.INVISIBLE);
        }

        /** get all items and populate the menu screen */
        System.out.println("start adapter");
        adapter = new MenuListAdapter(MenuActivity.this, R.layout.adapter_view_layout, list);
        list_View.setAdapter(adapter);
        System.out.println("done adapter");
        getAllItems();
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
                    Intent intent = new Intent(MenuActivity.this, MainActivity.class);
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

    /** View Order Screen Button Response for customers */
    void viewOrderButton(View view)
    {
        Intent intent = new Intent(MenuActivity.this, OrderActivity.class);
        startActivity(intent);
    }

    /** Add Item Button Response for employees */
    void addItemButton(View view)
    {
        String addItemTitle = "Add Item";
        /** make a text entry slot so employee can enter item info */
        final EditText addItemName = new EditText(this);

        /** Show a pop up dialog telling the username/password is wrong */
        AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
        builder.setTitle(addItemTitle)
                .setView(addItemName);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                // User clicked OK button
                /** create a new item based on the text input */
                String item = String.valueOf(addItemName.getText());
                RequestParams params = new RequestParams();
                params.put("name", item);
                params.put("type", item);
                params.put("price", 6);
                postItem(params);

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

    /** employee makes a request to the server to create an item */
    void postItem(RequestParams params)
    {
        /** params include item name, type and price */
        client.post(url + "Items" + user.access_token, params, new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response)
            {
                // called when response HTTP status is "200 OK"
                try
                {
                    System.out.println("Successfully added item to database");
                    /** add the new item and update the menu right away */
                    JSONObject res = new JSONObject(new String(response));
                    String[] info = parseName(res.toString());
                    Item item = new Item(info[0], info[1], info[2], info[3]);
                    list.add(item);
                    adapter.notifyDataSetChanged();
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

    /** employee makes a request to the server to delete an item */
    void deleteItem(final ListView listView)
    {
        String itemId = "";
        try
        {
            /** get the itemId from the textView object */
            JSONObject json = new JSONObject(listView.toString());
            itemId = json.get("id").toString(); //need to add id to item parser so it can be accessed here!!!
        }
        catch (Throwable t)
        {
            System.out.println("Throw error");
        }

        /** send delete request */
        client.delete(url + "Items/" + itemId + user.access_token, new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response)
            {
                // called when response HTTP status is "200 OK"
                try
                {
                    System.out.println("Successfully deleted item to database");
                    /** if the item is successfully deleted, remove it from the menu right away */
                    ListView itemList = findViewById(R.id.list_view);
                    //itemList.removeView(textView);
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



    /** customer can add an item from the menu to their order */
    void addItemToOrder(final ListView list_View)
    {
        String itemId = "";
        try
        {
            JSONObject json = new JSONObject(list_View.toString());
            itemId = json.get("id").toString();
        }
        catch (Throwable t)
        {
            System.out.println("Throw error");
        }

        client.put(url + "Orders/" + user.orderId + "/items/rel/" + itemId + user.access_token, new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response)
            {
                // called when response HTTP status is "200 OK"
                try
                {
                    System.out.println("Successfully added item to order");
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
                requestFailedAlert(statusCode);
                System.out.println(new String(errorResponse));
            }
        });
    }

    /** when adding all the items to the menu view, add order/deletion functionality to each item based on user role */
    void newItem(Item item)
    {
        adapter.notifyDataSetChanged();

        /** employees can delete items from the menu by clicking on an item and following the pop up */
        if (user.role.equals("employee"))
        {
            list_View.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(MenuActivity.this);
                    adb.setTitle("Delete Item?");
                    adb.setMessage("Are you sure you want to delete " + pos);
                    final int positionToRemove = pos;
                    adb.setNegativeButton("Cancel", null);
                    adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            list.remove(positionToRemove);
                            deleteItem(list_View);
                            adapter.notifyDataSetChanged();
                        }});
                    adb.show();
                }
            });
        }
        /** customers can add items to their order by clicking items in the menu */
        else if (user.role.equals("customer"))
        {
            list_View.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(MenuActivity.this);
                    adb.setTitle("Add To Order?");
                    final int positionToRemove = pos;
                    adb.setNegativeButton("Cancel", null);
                    adb.setPositiveButton("Add to Order", new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            addItemToOrder(list_View);
                        }});
                    adb.show();
                }
            });
        }
        //return list_View;
    }

    /** Get all items from the server and populate menu */
    void getAllItems()
    {
        /** Send the login request to the server */
        client.get(url + "Items" + user.access_token, new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response)
            {
                // called when response HTTP status is "200 OK"
                try
                {
                    JSONArray res = new JSONArray(new String(response));
                    //ListView itemList = findViewById(R.id.list_view);
                    /** iterate through all of the items in the server response, add them to the layout one by one */
                    for(int i = 0; i < res.length(); i++)
                    {
                        String[] info = parseName(res.get(i).toString());
                        Item item = new Item(info[0], info[1], info[2], info[3]);
                        list.add(item);
                        newItem(item);
                        System.out.println("getting items!!!!!!!!!!!!!!!!");
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

    String[] parseName(String str)
    {
        //pulls all of the information in the string pulled from the server out and returns
        //the important parts in an array (parsing)
        String[] spl = str.split(":");
        String[] p1 = spl[1].split(",");
        String[] p2 = spl[2].split(",");
        String[] p3 = spl[3].split(",");
        String[] p4 = spl[4].split(",");
        String name = p1[0];
        String type = p2[0];
        String price = p3[0];
        String count = p4[0];

        name = name.replace("\"", "");
        type = type.replace("\"", "");

        String[] ret = {name, type, price, count};
        return ret;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
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
