package com.neu.strangers.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

/**
 * An EditText, which notifies when something was cut/copied/pasted inside it.
 * 
 * @author Lukas Knuth
 * @version 1.0
 */
@SuppressLint("NewApi") public class ChatEditText extends EditText implements
		MenuItem.OnMenuItemClickListener {

    
	private final Context mContext;

	/*
	 * Just the constructors to create a new EditText...
	 */
	public ChatEditText(Context context) {
		super(context);
		this.mContext = context;
	}

	public ChatEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
	}

	public ChatEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
	}

	@Override
	protected void onCreateContextMenu(ContextMenu menu) {

		super.onCreateContextMenu(menu);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// TODO Auto-generated method stub
		return onTextContextMenuItem(item.getItemId());
	}

	@Override
	public boolean onTextContextMenuItem(int id) {
		// Do your thing:
		boolean consumed = super.onTextContextMenuItem(id);
		// React:
		switch (id) {
		case android.R.id.cut:
			onTextCut();
			break;
		case android.R.id.paste:
			onTextPaste();
			break;
		case android.R.id.copy:
			onTextCopy();
		}
		return consumed;
	}


	public void onTextCut() {
	//	Toast.makeText(mContext, "Cut!", Toast.LENGTH_SHORT).show();
	}


	public void onTextCopy() {
		//Toast.makeText(mContext, "Copy!", Toast.LENGTH_SHORT).show();
	}


	public void onTextPaste() {
		//Toast.makeText(mContext, "Paste!", Toast.LENGTH_SHORT).show();
	}
}
