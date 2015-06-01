package com.neu.strangers.tools;

/**
 * Created with Android Studio.
 * Author: Enex Tapper
 * Date: 15/5/28
 * Project: Strangers
 * Package: com.neu.strangers.tools
 */
public interface Constants {
	class Application{
		public final static String PREFERENCE_NAME = "neu_strangers_preference";

		public final static String IS_LOGGED_IN = "is_logged_in";
		public final static String LOGGED_IN_USER_ID = "logged_in_user_id";

		public final static String PROFILE_USER_ID = "profile_user_id";
	}

	class Action{
		public final static int TAKE_PICTURE = 100;
		public final static int SELECT_PICTURE = 101;
		public final static int CROP_PICTURE = 102;
	}
}
