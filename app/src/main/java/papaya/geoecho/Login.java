package papaya.geoecho;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import papaya.geoecho.Model.LoginApp;
import papaya.geoecho.Model.Response;

import static papaya.geoecho.R.id.bLogin;
import static papaya.geoecho.R.id.tRecordar;


public class Login extends AppCompatActivity implements View.OnClickListener {

    //Referencias UI

    private Button login;
    private EditText user,password;
    private TextView forgotPass;
    private LoginApp loginData;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        login = (Button) findViewById(bLogin);
        login.setOnClickListener(this);
        user = (EditText)findViewById(R.id.eUsername);
        password = (EditText) findViewById(R.id.ePasword);
        loginData = new LoginApp();

        sharedPref = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        /*//TODO Para Implementar en los siguientes TEA
        forgotPass = (TextView) findViewById(tRecordar);
        forgotPass.setOnClickListener(this);
        */

    }

    @Override
    public void onClick (View view) {
        switch (view.getId()){
            case bLogin:
                if (checkDataLogin()){
                    loginData.setUser(user.getText().toString().trim());
                    loginData.setPass(password.getText().toString().trim());
                    new UserLoginTask().execute();
                }
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
                showAlert("Connection Error","Imposible to connect with server. Please try again");
            }

            return result;
        }

        @Override
        protected void onPostExecute(final Response result) {
            String session = result.getSessionID();
            if (session != null){
                loginData.setSessionID(session);
                Intent i = new Intent(Login.this, MainActivity.class);
                saveUserData(loginData);
                startActivity(i);
            }else
                showAlert("", "Incorrect log in");
            mDialog.dismiss();
        }
        @Override
        protected void onPreExecute() {
            //Autenticando login...
            mDialog.setMessage("Authenticating...");
            mDialog.show();
        }
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

    /*
    Función para validar los campos user y password.
    Los campos no pueden estar vacíos y han de contener más de 3 caracteres
     */
    public boolean checkDataLogin(){
        Boolean validated = true;

        password.setError(null);
        user.setError(null);

        String passTemp = password.getText().toString();
        String userTemp = user.getText().toString();

        if(TextUtils.isEmpty(passTemp)){
            password.setError("Field required");
            validated = false;
        }else if(!validUser(passTemp)){
            password.setError("At least 4 chars needed");
            validated = false;
        }
        if(TextUtils.isEmpty(userTemp)){
            user.setError("Field required");
            validated = false;
        }else if(!validUser(userTemp)){
            user.setError("At least 4 chars needed");
            validated = false;
        }

        return validated;

    }
    public boolean validUser(String user){
        return user.trim().length()>3;
    }
    public boolean validPass(String pass){
        return pass.trim().length()>3;
    }
    /*
    Función para guardar la información del usuario de una actividad a otra
     */
    private void saveUserData(LoginApp data){
        editor.putString("user",data.getUser());
        editor.putString("session",data.getSessionID());
        editor.commit();
    }

    public Response serverLogin (LoginApp data) throws Exception{

        String serverUrl = "";
        Response result = new Response();
        URL url = new URL(serverUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setDoOutput(true); // para poder excribir
        con.setDoInput(true); // para poder leer

        try {
            ObjectOutputStream objectOut = new ObjectOutputStream(con.getOutputStream());
            objectOut.writeObject(data);

        }catch (Exception e){

        }
        int responseCode = con.getResponseCode();

        System.out.println("\nSending 'POST' request to URL : " + url);

        //System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        String inputLine;
        StringBuilder response;

        try{
            //Como recibir la información ?
            ObjectInputStream objectInput = new ObjectInputStream(con.getInputStream());

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            result.setSessionID(response.toString());

        }catch (Exception e){

        }

        return result;
    }
}
