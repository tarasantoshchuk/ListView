package com.example.tarasantoshchuk.listview;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends Activity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private HashMap<ContactViewHolder, ContactAdapter.BitmapDownloadTask> mHolderMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<Contact> contactList = initContactData();
        ListView listView = (ListView) findViewById(R.id.listView);
        ContactAdapter adapter = new ContactAdapter(this, contactList, mHolderMap);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        for(ContactAdapter.BitmapDownloadTask task: mHolderMap.values()) {
            if(task.getStatus() != AsyncTask.Status.FINISHED) {
                task.cancel(false);
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
        list.add(new Contact("https://goo.gl/k8S14T", "name1", "surname1"));
        list.add(new Contact("https://goo.gl/EKnMxl", "name2", "surname2"));
        list.add(new Contact("https://goo.gl/vbRLx8", "name3", "surname3"));
        list.add(new Contact("https://goo.gl/zGfYzI", "name4", "surname4"));
        list.add(new Contact("https://goo.gl/k8S14T", "name1", "surname1"));
        list.add(new Contact("https://goo.gl/EKnMxl", "name2", "surname2"));
        list.add(new Contact("https://goo.gl/vbRLx8", "name3", "surname3"));
        return list;
    }
}
