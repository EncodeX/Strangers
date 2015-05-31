package com.neu.strangers.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import com.jakewharton.disklrucache.DiskLruCache;
import com.neu.strangers.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
	private DiskLruCache mDiskCache;

	public ImageCache(Context context, ListView listView) {
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

		File cacheDir = getFileCache(context, "images");
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		try {
			mDiskCache = DiskLruCache.open(cacheDir, 1, 1, 10 * 1024 * 1024);
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	private File getFileCache(Context context, String cacheFileName) {
		String cachePath;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				|| !Environment.isExternalStorageRemovable()) {
			cachePath = context.getExternalCacheDir().getPath();
		} else {
			cachePath = context.getCacheDir().getPath();
		}
		return new File(cachePath + File.separator + cacheFileName);
	}

	private static boolean getBitmapUrlToStream(String urlString, OutputStream outputStream) {
		HttpURLConnection urlConnection = null;
		BufferedOutputStream out = null;
		BufferedInputStream in = null;
		try {
			final URL url = new URL(urlString);
			urlConnection = (HttpURLConnection) url.openConnection();
			in = new BufferedInputStream(urlConnection.getInputStream(), 8 * 1024);
			out = new BufferedOutputStream(outputStream, 8 * 1024);
			int b;
			while ((b = in.read()) != -1) {
				out.write(b);
			}
			return true;
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public String toMD5String(String key) {
		String cacheKey;
		try {
			final MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(key.getBytes());
			cacheKey = bytesToHexString(digest.digest());
		} catch (NoSuchAlgorithmException e) {
			cacheKey = String.valueOf(key.hashCode());
		}
		return cacheKey;
	}

	private String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	public void flushCache() {
		if (mDiskCache != null) {
			try {
				mDiskCache.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
