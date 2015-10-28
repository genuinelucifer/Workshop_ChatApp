package iiti.progclub.app.chatapp_workshop;

import android.app.Application;

import com.parse.Parse;

/**
 * This Class takes care of initialising Parse API for our app
 *
 * Created by Abhinav Tripathi on 22-Oct-15.
 */
public class ChatAppApplication extends Application {
    //TODO: Fill following keys from your parse console
    String APP_ID = "";
    String CLIENT_KEY = "";

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, APP_ID, CLIENT_KEY);
    }
}
