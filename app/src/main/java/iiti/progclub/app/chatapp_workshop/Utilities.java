package iiti.progclub.app.chatapp_workshop;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.parse.ParseUser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Many utility functions to aid our cause
 *
 * Created by Abhinav Tripathi on 22-Oct-15.
 */
public class Utilities {
    private static final String MSG_TERMINATE = "<<TERMINATE_MESSAGE>>";
    private static final String APP_DIR_NAME = "ChatAPP";
    public static final String MessageQueueClassName = "All_Messages";
    public static final String toUsername_COLUMN = "toUser";
    public static final String fromUsername_COLUMN = "fromUser";
    public static final String message_COLUMN = "message";
    public static final String username_COLUMN = "username";

    //This Function Checks if an active internet connection is available
    public static boolean isNetworkAvailable(Context ctx)
    {
        ConnectivityManager ctvMngr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo aNetInfo = ctvMngr.getActiveNetworkInfo();
        return aNetInfo != null && aNetInfo.isAvailable();
    }

    static ParseUser curUser = null;
    static public boolean checkLoggedInUser()
    {
        curUser = ParseUser.getCurrentUser();
        return curUser != null;
    }
    static public void setCurrentUser(ParseUser usr)
    {
        curUser = usr;
    }
    static public String getUsername()
    {
        return curUser.getUsername();
    }

    public static Context contextLogout;
    public static class LogoutTask extends AsyncTask<Void,Void, Void> {
        ProgressDialog pd;
        Intent i;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(contextLogout);
            pd.setMessage("Logging out...\nPlease wait...");
            pd.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            ParseUser.logOut();
            curUser = null;
            i = new Intent(contextLogout, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            return null;
        }

        @Override
        protected void onPostExecute(Void state) {
            pd.setMessage("");
            pd.dismiss();
            contextLogout.startActivity(i);
        }
    }

    public static void writeToFile(String fileName, List<String> data) {
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + File.separator +  APP_DIR_NAME);
            if(!dir.exists())
                dir.mkdirs();

            File file = new File(dir, fileName);
            if(!file.exists())
                file.createNewFile();
            FileOutputStream f = new FileOutputStream(file, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(f);
            for(String str : data)
                outputStreamWriter.append(String.format("%s\n%s\n", str, MSG_TERMINATE));
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static List<String> readFromFile(String fileName, Context ctx) {
        List<String> read_data = new ArrayList<>();
        try
        {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + File.separator +  APP_DIR_NAME);
            if(!dir.exists())
                dir.mkdirs();
            File file = new File(dir, fileName);
            if(!file.exists())
                return read_data;
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String receiveString;
            StringBuilder stringBuilder = new StringBuilder();
            while ( (receiveString = bufferedReader.readLine()) != null ) {
                if(receiveString.equalsIgnoreCase(MSG_TERMINATE))
                {
                    read_data.add(stringBuilder.toString());
                    stringBuilder = new StringBuilder();
                }
                else
                    stringBuilder.append(receiveString + "\n");
            }
            bufferedReader.close();
        }
        catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return read_data;
    }

    public static List<String> getAllChatFilesNames()
    {
        List<String> allFiles = new ArrayList<>();
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + File.separator +  APP_DIR_NAME);
        if(!dir.exists())
        {
            dir.mkdirs();
            return allFiles;
        }
        File[] files = dir.listFiles();
        for(File f : files)
        {
            allFiles.add(f.getName());
        }
        return allFiles;
    }

    private static String chatUsername = "";
    public static void setCurChatUsername(String usrname)
    {
        chatUsername = usrname;
    }
    public static String getCurChatUsername()
    {
        return chatUsername;
    }
}
