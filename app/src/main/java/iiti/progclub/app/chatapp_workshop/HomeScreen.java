package iiti.progclub.app.chatapp_workshop;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class HomeScreen extends AppCompatActivity {

    ListView lvAllChats;
    ArrayList<String> allChatsUsernames;
    ArrayList<CustomListItem> allChats;
    CustomListAdapter adapter;

    EditText etNewContactUsername;
    TextView tvInfo;
    Button btnAddContact;

    String usernameToAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        lvAllChats = (ListView) findViewById(R.id.lvAllContacts);
        allChatsUsernames = new ArrayList<>();
        adapter = new CustomListAdapter(this, allChats);
        lvAllChats.setAdapter(adapter);

        lvAllChats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Utilities.setCurChatUsername(allChatsUsernames.get(position));
                Intent i = new Intent(HomeScreen.this, ChatScreen.class);
                startActivity(i);
            }
        });

        etNewContactUsername = (EditText) findViewById(R.id.etNewContactUsername);
        tvInfo = (TextView) findViewById(R.id.tvInfoHomeScreen);
        btnAddContact = (Button) findViewById(R.id.btnAddContactButton);
        btnAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameToAdd = etNewContactUsername.getText().toString();
                new AddUserTask().execute();
            }
        });

        new LoadAllTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_common, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_Logout) {
            Utilities.contextLogout = HomeScreen.this;
            new Utilities.LogoutTask().execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private enum LoadAllTaskState
    {
        SUCCESS,
        NO_INTERNET,
        EXCEPTION_THROWN
    }
    private class LoadAllTask extends AsyncTask<Void,Void, LoadAllTaskState> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(HomeScreen.this);
            pd.setMessage("Loading all chat data...\nPlease wait...");
            pd.show();
        }

        @Override
        protected LoadAllTaskState doInBackground(Void... params) {
            if(!Utilities.isNetworkAvailable(HomeScreen.this))
                return LoadAllTaskState.NO_INTERNET;

            allChatsUsernames.clear();
            allChatsUsernames.addAll(Utilities.getAllChatFilesNames());
            allChats.clear();
            for(String str : allChatsUsernames)
                allChats.add(new CustomListItem(str, false));

            ParseQuery<ParseObject> query = ParseQuery.getQuery(Utilities.MessageQueueClassName);
            query.whereEqualTo(Utilities.toUsername_COLUMN, Utilities.getUsername());
            query.addAscendingOrder(Utilities.fromUsername_COLUMN);
            try
            {
                List<ParseObject> postList = query.find();
                String uname = "";
                List<String> chatWrite = new ArrayList<>();
                for(ParseObject obj : postList)
                {
                    String curUname = obj.getString(Utilities.fromUsername_COLUMN);
                    if(!uname.equalsIgnoreCase(curUname))
                    {
                        if(chatWrite.size() > 0)
                            Utilities.writeToFile(uname, chatWrite);
                        uname = curUname;
                        chatWrite = new ArrayList<>();
                        if(!allChatsUsernames.contains(uname))
                        {
                            allChatsUsernames.add(uname);
                            allChats.add(new CustomListItem(uname, true));
                        }
                        else
                        {
                            allChats.set(allChatsUsernames.indexOf(uname), new CustomListItem(uname, true));
                        }
                    }
                    chatWrite.add("<b>" + uname + "</b><br>" + obj.getString(Utilities.message_COLUMN));
                }
                if(chatWrite.size() > 0)
                    Utilities.writeToFile(uname, chatWrite);
            } catch (ParseException e) {
                //publishProgress("An error occurred. Please try refresh. If questions still don't load then please logout and login again!");
                Log.d(getClass().getSimpleName(), "Error: " + e.getMessage());
                e.printStackTrace();
                return LoadAllTaskState.EXCEPTION_THROWN;
            }

            return LoadAllTaskState.SUCCESS;
        }

        @Override
        protected void onPostExecute(LoadAllTaskState state) {
            pd.setMessage("");
            pd.dismiss();
            if (state == LoadAllTaskState.SUCCESS) {
                adapter.notifyDataSetChanged();
                if(allChats.isEmpty())
                    tvInfo.setText("You have no saved or new messages.");
                else
                    tvInfo.setText("All contacts loaded:");
                Toast.makeText(HomeScreen.this, "All data loaded!", Toast.LENGTH_SHORT).show();
            } else if (state == LoadAllTaskState.NO_INTERNET) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreen.this);
                builder.setMessage(R.string.no_internet_msg)
                        .setTitle(R.string.no_internet_title)
                        .setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            } else if (state == LoadAllTaskState.EXCEPTION_THROWN) {
                Toast.makeText(HomeScreen.this, "Some Error occurred!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private enum AddUserTaskState
    {
        SUCCESS,
        NO_INTERNET,
        USER_NOT_FOUND,
        USER_ALREADY_IN_CONTACT,
        EXCEPTION_THROWN
    }
    private class AddUserTask extends AsyncTask<Void,Void, AddUserTaskState> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(HomeScreen.this);
            pd.setMessage("Adding Contact...\nPlease wait...");
            pd.show();
        }

        @Override
        protected AddUserTaskState doInBackground(Void... params) {
            if(!Utilities.isNetworkAvailable(HomeScreen.this))
                return AddUserTaskState.NO_INTERNET;

            if(allChatsUsernames.contains(usernameToAdd))
                return AddUserTaskState.USER_ALREADY_IN_CONTACT;

            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo(Utilities.username_COLUMN, usernameToAdd);
            try
            {
                List<ParseUser> postList = query.find();
                if(postList.isEmpty())
                    return AddUserTaskState.USER_NOT_FOUND;
                Utilities.writeToFile(usernameToAdd, new ArrayList<String>());
                allChatsUsernames.add(usernameToAdd);
                allChats.add(new CustomListItem(usernameToAdd, false));
            } catch (ParseException e) {
                //publishProgress("An error occurred. Please try refresh. If questions still don't load then please logout and login again!");
                Log.d(getClass().getSimpleName(), "Error: " + e.getMessage());
                e.printStackTrace();
                return AddUserTaskState.EXCEPTION_THROWN;
            }
            return AddUserTaskState.SUCCESS;
        }

        @Override
        protected void onPostExecute(AddUserTaskState state) {
            pd.setMessage("");
            pd.dismiss();
            if (state == AddUserTaskState.SUCCESS) {
                adapter.notifyDataSetChanged();
                Toast.makeText(HomeScreen.this, "Contact added!", Toast.LENGTH_SHORT).show();
            } else if (state == AddUserTaskState.USER_ALREADY_IN_CONTACT) {
                Toast.makeText(HomeScreen.this, "User is already in contacts!", Toast.LENGTH_SHORT).show();
            } else if (state == AddUserTaskState.USER_NOT_FOUND) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreen.this);
                builder.setMessage("No user found with the username " + usernameToAdd + "!\nPlease verify username!")
                        .setTitle("NOT FOUND!")
                        .setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            } else if (state == AddUserTaskState.NO_INTERNET) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreen.this);
                builder.setMessage(R.string.no_internet_msg)
                        .setTitle(R.string.no_internet_title)
                        .setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            } else if (state == AddUserTaskState.EXCEPTION_THROWN) {
                Toast.makeText(HomeScreen.this, "Some Error occurred!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
