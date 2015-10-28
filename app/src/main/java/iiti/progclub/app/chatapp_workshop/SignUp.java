package iiti.progclub.app.chatapp_workshop;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;

public class SignUp extends AppCompatActivity {

    EditText etEmail,etPass,etName,etUsername,etRePass;
    Button btnSignUp;
    CheckBox cbTerms;
    ProgressDialog pd;

    String username, password, repass, email, name;
    boolean isCbChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setTitle("SignUp");

        etEmail    = (EditText) findViewById(R.id.etEmailSignUp);
        etPass     = (EditText) findViewById(R.id.etPasswordSignUp);
        etName     = (EditText) findViewById(R.id.etFullNameSignUp);
        etUsername = (EditText) findViewById(R.id.etUsernameSignUp);
        etRePass   = (EditText) findViewById(R.id.etConfirmPassSignUp);
        btnSignUp  = (Button)   findViewById(R.id.btnSignUp);
        cbTerms    = (CheckBox) findViewById(R.id.cbTerms);

        btnSignUp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                username = etUsername.getText().toString();
                password = etPass.getText().toString();
                repass = etRePass.getText().toString();
                email = etEmail.getText().toString();
                name = etName.getText().toString();
                isCbChecked = cbTerms.isChecked();

                new SignUpTask().execute();
            }
        });
    }


    private enum SignUpTaskState
    {
        SUCCESS,
        NO_INTERNET,
        EMPTY_SIGNUP,
        PASS_NOMATCH,
        TERMS_UNAGREED,
        EXCEPTION_THROWN
    }

    String errMsg;
    class SignUpTask extends AsyncTask<Void,Boolean, SignUpTaskState> {

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(SignUp.this);
            pd.setMessage("Please wait while signing up...");
            pd.show();
        }

        @Override
        protected SignUpTaskState doInBackground(Void... params) {
            publishProgress(false);
            pd.setMessage("Signing up...");

            if (!Utilities.isNetworkAvailable(SignUp.this)) {
                return SignUpTaskState.NO_INTERNET;
            }

            username = username.trim();
            password = password.trim();
            repass = repass.trim();
            email = email.trim();
            name = name.trim();

            if (name.isEmpty() || username.isEmpty() || password.isEmpty() || email.isEmpty())
            {
                return SignUpTaskState.EMPTY_SIGNUP;
            }
            else if (!password.equals(repass))
            {
                return SignUpTaskState.PASS_NOMATCH;
            }
            else if (!isCbChecked)
            {
                return SignUpTaskState.TERMS_UNAGREED;
            }
            else {
                try {
                    ParseUser newUser = new ParseUser();
                    newUser.setUsername(username);
                    newUser.setPassword(password);
                    newUser.setEmail(email);
                    newUser.put("Name", name);
                    newUser.signUp();
                } catch (ParseException e) {
                    e.printStackTrace();
                    errMsg = e.getMessage();
                    return SignUpTaskState.EXCEPTION_THROWN;
                }
            }
            return SignUpTaskState.SUCCESS;
        }

        @Override
        protected void onProgressUpdate(Boolean... isEnabled) {
            btnSignUp.setEnabled(isEnabled[0]);
            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }


        @Override
        protected void onPostExecute(SignUpTaskState state) {
            pd.setMessage("");
            pd.dismiss();

            // refresh UI
            if (state == SignUpTaskState.SUCCESS) {
                // Success!
                Toast.makeText(SignUp.this, "Sign Up Successful!\nPlease verify your email before logging in.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignUp.this, Login.class);
                startActivity(intent);
            }
            else if (state == SignUpTaskState.NO_INTERNET) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SignUp.this);
                builder.setMessage(R.string.no_internet_msg)
                        .setTitle(R.string.no_internet_title)
                        .setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else if (state == SignUpTaskState.EMPTY_SIGNUP) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SignUp.this);
                builder.setMessage(R.string.signup_error_message)
                        .setTitle(R.string.signup_error_title)
                        .setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else if(state == SignUpTaskState.PASS_NOMATCH)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(SignUp.this);
                builder.setMessage(R.string.pass_no_match_error_msg)
                        .setTitle(R.string.signup_error_title)
                        .setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else if (state == SignUpTaskState.TERMS_UNAGREED) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SignUp.this);
                builder.setMessage("Please read and agree to our terms and conditions before signing up.")
                        .setTitle("Agree to terms...")
                        .setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else if (state == SignUpTaskState.EXCEPTION_THROWN) {
                // Fail
                String errorMsg = errMsg.replace("parameters", "Email ID or password");
                AlertDialog.Builder builder = new AlertDialog.Builder(SignUp.this);
                builder.setMessage(errorMsg)
                        .setTitle(R.string.signup_error_title)
                        .setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            btnSignUp.setEnabled(true);
        }
    }
}
