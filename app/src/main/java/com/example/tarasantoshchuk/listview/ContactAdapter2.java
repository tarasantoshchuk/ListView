package com.example.tarasantoshchuk.listview;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ContactAdapter2 extends BaseAdapter {
    private List<Contact> mContactList;
    private LayoutInflater mInflater;

    private HashMap<ContactViewHolder, Future> mHolderMap;
    private HashMap<ContactViewHolder, Runnable> mHolderTasks =
            new HashMap<ContactViewHolder, Runnable>();

    private Activity mActivity;
    private ExecutorService mExecutor;

    public ContactAdapter2(Activity activity, List<Contact> contactList,
                           HashMap<ContactViewHolder, Future> mHolderMap) {
        mInflater = LayoutInflater.from(activity);
        this.mContactList = new ArrayList<Contact>(contactList);
        this.mActivity = activity;
        this.mHolderMap = mHolderMap;
        mExecutor = Executors.newCachedThreadPool();
    }

    private Contact getContact(int position) {
        return mContactList.get(position);
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
            mHolderTasks.put(holder, null);

            convertView.setTag(holder);
        } else {
            holder = (ContactViewHolder)convertView.getTag();
        }
        Future future = mHolderMap.get(holder);
        if (future != null && !future.isDone()) {
            future.cancel(false);
        }
        Runnable task = new BitmapDownloadTask(mActivity, contact, holder, mHolderTasks);
        mHolderTasks.put(holder, task);
        mHolderMap.put(holder, mExecutor.submit(task));

        holder.txtName.setText(contact.getName());
        holder.txtSurname.setText(contact.getSurname());

        return convertView;
    }

    public static class BitmapDownloadTask implements Runnable {
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

        private Activity mActivity;
        private Contact mContact;
        private ImageView mImageView;
        private ContactViewHolder mHolder;
        private HashMap<ContactViewHolder, Runnable> mHoldersTasks;

        private static WeakHashMap<String, Object> sLocks = new WeakHashMap<String, Object>();

        public BitmapDownloadTask(Activity activity, Contact contact, ContactViewHolder holder,
                                  HashMap<ContactViewHolder, Runnable> holdersTasks) {
            mActivity = activity;
            mContact = contact;
            mHolder = holder;
            mHoldersTasks = holdersTasks;
            mImageView = holder.imageView;
            mImageView.setVisibility(View.INVISIBLE);
        }

        @Override
        public void run() {
            try {
                File image = new File(mActivity.getFilesDir() + "/" +
                        Integer.toString(mContact.getPhotoUrl().hashCode()));

                synchronized(ContactAdapter.BitmapDownloadTask.class) {
                    if(sLocks.get(mContact.getPhotoUrl()) == null) {
                        sLocks.put(mContact.getPhotoUrl(), new Object());
                    }
                }
                synchronized(sLocks.get(mContact.getPhotoUrl())) {
                    if (!image.exists()) {
                        loadFile(image);
                    }
                }
                if(Thread.currentThread().isInterrupted()) {
                    return;
                }

                setBitmap(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void setBitmap(File image) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeFile(image.getPath(), options);

            options.inSampleSize = getInSampleSize(mImageView, options);
            options.inJustDecodeBounds = false;

            final Bitmap bitmap = BitmapFactory.decodeFile(image.getPath(), options);
            if (!Thread.currentThread().isInterrupted()) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mHoldersTasks.get(mHolder) == BitmapDownloadTask.this) {
                            if (bitmap == null) {
                                Toast.makeText(mActivity, "Bad url format", Toast.LENGTH_SHORT).show();
                            } else {
                                mImageView.setImageBitmap(bitmap);
                                mImageView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
            }
        }

        private void loadFile(File image) throws IOException {
            image.createNewFile();

            URL url = new URL(mContact.getPhotoUrl());
            url.openConnection().connect();

            InputStream input = new BufferedInputStream(url.openStream(), 8 * BUFFER_SIZE);

            OutputStream output = new FileOutputStream(image);

            byte data[] = new byte[BUFFER_SIZE];
            int count;

            while (!Thread.currentThread().isInterrupted() && (count = input.read(data)) != END_OF_STREAM) {
                output.write(data, 0, count);
            }

            output.flush();

            output.close();
            input.close();

            if (Thread.currentThread().isInterrupted()) {
                image.delete();
            }
        }

        private int getInSampleSize(ImageView mImageView, BitmapFactory.Options options) {
            int inSampleSize = IN_SAMPLE_INIT_VALUE;

            final int halfHeight = options.outHeight / IN_SAMPLE_MULT;
            final int halfWidth = options.outWidth / IN_SAMPLE_MULT;

            final int viewHeight = mImageView.getHeight();
            final int viewWidth = mImageView.getWidth();

            if(viewHeight * viewWidth == 0) { return inSampleSize; }
            while ((halfHeight / inSampleSize) > viewHeight
                    && (halfWidth / inSampleSize) > viewWidth) {
                inSampleSize *= IN_SAMPLE_MULT;
            }

            return inSampleSize;
        }
    }
}
