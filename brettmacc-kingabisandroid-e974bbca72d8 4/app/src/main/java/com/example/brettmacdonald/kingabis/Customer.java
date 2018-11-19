package com.example.brettmacdonald.kingabis;

import org.json.JSONObject;

public class Customer
{
    public String access_token, id, username, role, orderId;

    public Customer(JSONObject res)
    {
        try
        {
            this.access_token = "?access_token=" + res.get("id").toString();
            this.id = res.get("userId").toString();
            this.role = res.get("role").toString();
            this.username = res.get("username").toString();
            this.orderId = res.get("currentOrder").toString();
        }
        catch (Throwable t)
        {
            System.out.println(t);
        }
    }
}
