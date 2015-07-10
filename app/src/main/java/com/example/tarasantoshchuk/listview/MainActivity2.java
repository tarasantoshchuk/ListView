package com.example.tarasantoshchuk.listview;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

public class MainActivity2 extends Activity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private HashMap<ContactViewHolder, Future> mHolderMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<Contact> contactList = initContactData();
        ListView listView = (ListView) findViewById(R.id.listView);
        ContactAdapter2 adapter = new ContactAdapter2(this, contactList, mHolderMap);
        listView.setAdapter(adapter);

    }

    @Override
    protected void onStop() {
        super.onStop();
        for(Future future: mHolderMap.values()) {
            if(!future.isDone()) {
                future.cancel(false);
            }
        }

    }

    private List<Contact> initContactData() {
        List<Contact> list = new ArrayList<Contact>();
        list.add(new Contact("https://goo.gl/k8S14T", "name1", "surname1"));
        list.add(new Contact("https://goo.gl/EKnMxl", "name2", "surname2"));
        list.add(new Contact("https://goo.gl/vbRLx8", "name3", "surname3"));
        list.add(new Contact("https://goo.gl/zGfYzI", "name4", "surname4"));
        list.add(new Contact("https://goo.gl/k8S14T", "name1", "surname1"));
        list.add(new Contact("https://goo.gl/EKnMxl", "name2", "surname2"));
        list.add(new Contact("https://goo.gl/vbRLx8", "name3", "surname3"));
        list.add(new Contact("https://goo.gl/zGfYzI", "name4", "surname4"));
        return list;
    }
}
