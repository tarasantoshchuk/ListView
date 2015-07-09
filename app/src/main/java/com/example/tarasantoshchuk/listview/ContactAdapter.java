package com.example.tarasantoshchuk.listview;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactAdapter extends BaseAdapter{
    private List<Contact> mContactList;
    private HashMap<ContactViewHolder, BitmapDownloadTask> mHolderMap;
    private LayoutInflater mInflater;
    private Context mContext;

    public ContactAdapter(Context context, List<Contact> contactList,
                          HashMap<ContactViewHolder, BitmapDownloadTask> mTasks) {
        mInflater = LayoutInflater.from(context);
        this.mContactList = new ArrayList<Contact>(contactList);
        this.mHolderMap = mTasks;
        this.mContext = context;
    }

    private Contact getContact(int position) {
        return mContactList.get(position);
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public int getCount() {
        return mContactList.size();
    }

    @Override
    public Object getItem(int position) {
        return getContact(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ContactViewHolder holder;
        Contact contact = getContact(position);

        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, null);
            holder = new ContactViewHolder();

            holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            holder.txtName = (TextView) convertView.findViewById(R.id.txtName);
            holder.txtSurname = (TextView) convertView.findViewById(R.id.txtSurname);

            mHolderMap.put(holder, null);

            convertView.setTag(holder);
        } else {
            holder = (ContactViewHolder)convertView.getTag();
        }
        BitmapDownloadTask task = mHolderMap.get(holder);

        if( task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
            task.cancel(false);
        }

        mHolderMap.put(holder, (BitmapDownloadTask)
                new BitmapDownloadTask(mContext, contact, holder.imageView).execute());

        holder.txtName.setText(contact.getName());
        holder.txtSurname.setText(contact.getSurname());

        return convertView;
    }

    public class BitmapDownloadTask extends AsyncTask<Void, Void, Bitmap> {
        /**
         * initial value of inSampleSize that used in getSampleSize()
         */
        private static final int IN_SAMPLE_INIT_VALUE = 1;

        /**
         * multiplier that is used in getSampleSize()
         */
        private static final int IN_SAMPLE_MULT = 2;

        private static final int BUFFER_SIZE = 1024;

        private static final int END_OF_STREAM = -1;

        private Context mContext;
        private Contact mContact;
        private ImageView mImageView;

        public BitmapDownloadTask(Context context, Contact contact, ImageView imageView) {
            mContext = context;
            mContact = contact;
            mImageView = imageView;
            mImageView.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                File image = new File(mContext.getFilesDir() + "/" +
                        Integer.toString(mContact.getPhotoUrl().hashCode()));

                if(!image.exists()) {
                    image.createNewFile();

                    URL url = new URL(mContact.getPhotoUrl());
                    url.openConnection().connect();

                    InputStream input = new BufferedInputStream(url.openStream(), BUFFER_SIZE);
                    OutputStream output = new FileOutputStream(image);

                    byte data[] = new byte[BUFFER_SIZE];
                    int count;

                    while (!isCancelled() && (count = input.read(data)) != END_OF_STREAM) {
                        output.write(data, 0, count);
                    }

                    output.flush();

                    output.close();
                    input.close();

                    if (isCancelled()) {
                        image.delete();
                        return null;
                    }
                }

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                BitmapFactory.decodeFile(image.getPath(), options);

                options.inSampleSize = getInSampleSize(mImageView, options);
                options.inJustDecodeBounds = false;

                return BitmapFactory.decodeFile(image.getPath(), options);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private int getInSampleSize(ImageView mImageView, BitmapFactory.Options options) {
            int inSampleSize = IN_SAMPLE_INIT_VALUE;

            final int halfHeight = options.outHeight / IN_SAMPLE_MULT;
            final int halfWidth = options.outWidth / IN_SAMPLE_MULT;

            final int viewHeight = mImageView.getHeight();
            final int viewWidth = mImageView.getWidth();

            while ((halfHeight / inSampleSize) > viewHeight
                    && (halfWidth / inSampleSize) > viewWidth) {
                inSampleSize *= IN_SAMPLE_MULT;
            }

            return inSampleSize;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap == null) {
                Toast.makeText(mContext, "Bad url format", Toast.LENGTH_SHORT).show();
            } else {
                mImageView.setImageBitmap(bitmap);
                mImageView.setVisibility(View.VISIBLE);
            }
        }
    }
}
