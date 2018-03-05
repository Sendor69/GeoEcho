package papaya.geoecho;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import papaya.geoecho.Model.LoginApp;
import papaya.geoecho.Model.Response;

import static papaya.geoecho.R.id.bLogin;
import static papaya.geoecho.R.id.tRecordar;


public class Login extends AppCompatActivity implements View.OnClickListener {

    //Referencias UI

    Button login;
    EditText user,password;
    TextView forgotPass;
    LoginApp loginData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        login = (Button) findViewById(bLogin);
        login.setOnClickListener(this);
        user = (EditText)findViewById(R.id.eUsername);
        password = (EditText) findViewById(R.id.ePasword);
        forgotPass = (TextView) findViewById(tRecordar);
        forgotPass.setOnClickListener(this);
        loginData = new LoginApp();

    }

    @Override
    public void onClick (View view) {
        switch (view.getId()){
            case bLogin:
                if (checkData()){
                    loginData.setUser(user.getText().toString().trim());
                    loginData.setPass(password.getText().toString().trim());
                    new UserLoginTask().execute();
                }else
                    showAlert("Error","Fields can't be empty");
                break;
            case tRecordar:
                //ToDO por implementar
                break;
        }

    }



    public class UserLoginTask extends AsyncTask<Void, Void, Response> {

        /*Clase tonta, no hace nada */

        ProgressDialog mDialog = new ProgressDialog(Login.this);

        UserLoginTask() {

        }

        @Override
        protected Response doInBackground(Void... params) {
            // TODO: intenta conectarte al server y logear.
            Response result = new Response();
            try {
                // Simulate network access.
                Thread.sleep(2000);
                result= validateLogin(loginData);
            } catch (InterruptedException e) {

            }

            return result;
        }

        @Override
        protected void onPostExecute(final Response result) {
            String session = result.getSessionID();
            if (session != null){
                mDialog.dismiss();
                Intent i = new Intent(Login.this, MainActivity.class);
                i.putExtra("User",loginData.getUser());
                startActivity(i);
            }else
                showAlert("", "Authentication failed");
            mDialog.dismiss();
        }
        @Override
        protected void onPreExecute() {
            //Autenticando login...
            mDialog.setMessage("Authenticating...");
            mDialog.show();
        }
    }

    public boolean checkData(){
        return user.getText().toString().trim().length()>0 && password.getText().toString().trim().length()>0;

    }

    public void showAlert (String title, String msg){
        AlertDialog alertDialog = new AlertDialog.Builder(Login.this,R.style.CustomAlert).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public Response validateLogin(LoginApp data){
        //Validate login with server

        Response result = new Response();
        if (data.getUser().equals("Admin") && data.getPass().equals("admin")) {
            result.setSessionID(data.getUser());
        }else
            result.setSessionID(null);
        return result;
    }
}
