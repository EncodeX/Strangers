package com.neu.strangers.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import com.neu.strangers.R;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with Android Studio.
 * Author: Enex Tapper
 * Date: 15/5/28
 * Project: Strangers
 * Package: com.neu.strangers.tools
 */
public class ImageCache {
	private LruCache<String, Bitmap> mMemoryCache;
	private Set<ASyncDownloadImage> mTasks;
	private ListView mListView;

	public ImageCache(ListView listView) {
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int cacheSize = maxMemory / 10;
		this.mListView = listView;
		this.mTasks = new HashSet<>();

		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getByteCount();
			}
		};
	}

	public void showImage(String url, ImageView imageView) {
		Bitmap bitmap = getBitmapFromMemoryCaches(url);
		if (bitmap == null) {
			imageView.setImageResource(R.mipmap.ic_launcher);
		} else {
			imageView.setImageBitmap(bitmap);
		}
	}

	public void loadImages(int start, int end) {
		// Todo 此处需要重写
//		for (int i = start; i < end; i++) {
//			String url = " ";
//			Bitmap bitmap = getBitmapFromMemoryCaches(url);
//			if (bitmap == null) {
//				ASyncDownloadImage task = new ASyncDownloadImage(url);
//				mTasks.add(task);
//				task.execute(url);
//			} else {
//				ImageView imageView = (ImageView) mListView.findViewWithTag(url);
//				imageView.setImageBitmap(bitmap);
//			}
//		}
	}

	// 从LruCache获取中获取缓存对象
	public Bitmap getBitmapFromMemoryCaches(String url) {
		return mMemoryCache.get(url);
	}

	// 增加缓存对象到LruCache
	public void addBitmapToMemoryCaches(String url, Bitmap bitmap) {
		if (getBitmapFromMemoryCaches(url) == null) {
			mMemoryCache.put(url, bitmap);
		}
	}

	private static Bitmap getBitmapFromUrl(String urlString) {
		Bitmap bitmap;
		InputStream is = null;
		try {
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			is = new BufferedInputStream(conn.getInputStream());
			bitmap = BitmapFactory.decodeStream(is);
			conn.disconnect();
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
			}
		}
		return null;
	}

	public void cancelAllTasks() {
		if (mTasks != null) {
			for (ASyncDownloadImage task : mTasks) {
				task.cancel(false);
			}
		}
	}

	class ASyncDownloadImage extends AsyncTask<String, Void, Bitmap> {

		private String url;

		public ASyncDownloadImage(String url) {
			this.url = url;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			url = params[0];
			Bitmap bitmap = getBitmapFromUrl(url);
			if (bitmap != null) {
				addBitmapToMemoryCaches(url, bitmap);
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			super.onPostExecute(bitmap);
			ImageView imageView = (ImageView) mListView.findViewWithTag(url);
			if (imageView != null && bitmap != null) {
				imageView.setImageBitmap(bitmap);
			}
			mTasks.remove(this);
		}
	}
}
