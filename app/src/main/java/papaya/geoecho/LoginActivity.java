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

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import model.client.LoginApp;
import model.client.Response;

import static papaya.geoecho.R.id.bLogin;
import static papaya.geoecho.R.id.tRecordar;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
        //Referencias UI

    private Button login;
    private EditText user,password;
    private TextView forgotPass;
    private LoginApp loginData;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    //Constantes
    public static final int CONNECTION_ERROR = -1;

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

    /*
    Función AsyncTask que en segundo plano connectarà con el servidor, validarà el usuario y lanzará
        la actividad principal, pasandole los datos del usuario y el sessionID
     */

    public class UserLoginTask extends AsyncTask<Void, Void, Response> {

        /*Clase tonta, no hace nada */

        ProgressDialog mDialog = new ProgressDialog(LoginActivity.this);

        UserLoginTask() {

        }

        @Override
        protected Response doInBackground(Void... params) {
            Response result = new Response();
            try {
                result= serverLogin(loginData);
            } catch (Exception e) {
                showAlert("Connection Error","Imposible to connect with server. Please try again");
            }

            return result;
        }

        @Override
        protected void onPostExecute(final Response result) {
            int status = result.getStatusQuery();

            //Gestionamos la respuesta del servidor
            if (status == result.LOGIN_FAILED){
                showAlert("Authentication", "User or password are incorrects");

            }else if (status == result.LOGIN_OK){
                loginData.setSessionID(result.getSessionID());
                Intent i = new Intent(LoginActivity.this, MainGeoActivity.class);
                saveUserData(loginData);
                startActivity(i);

            }else if (status == CONNECTION_ERROR){
                showAlert("Error", "Server connection failed. Please try again");

            }else
                showAlert("Unkown Error", "Contact with administrator");
            mDialog.dismiss();
        }
        @Override
        protected void onPreExecute() {
            //Autenticando login...
            mDialog.setMessage("Authenticating...");
            mDialog.show();
        }
    }

    /**
     * Función que mostrará un AlertDialog personalizado
     * @param: Titulo del error
     * @param: Mensaje del error
     */
    public void showAlert (String title, String msg){
        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this,R.style.CustomAlert).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }



    /**
     * Función para validar los campos user y password.
     * Los campos no pueden estar vacíos y han de contener más de 3 caracteres
     */
    public boolean checkDataLogin(){
        Boolean validated = true;

        password.setError(null);
        user.setError(null);

        String passTemp = password.getText().toString();
        String userTemp = user.getText().toString();

        if(TextUtils.isEmpty(passTemp)){
            password.setError("Field required");
            password.requestFocus();
            validated = false;
        }else if(!ValidMinChars(passTemp)){
            password.setError("At least 4 chars needed");
            password.requestFocus();
            validated = false;
        }
        if(TextUtils.isEmpty(userTemp)){
            user.setError("Field required");
            user.requestFocus();
            validated = false;
        }else if(!ValidMinChars(userTemp)){
            user.setError("At least 4 chars needed");
            user.requestFocus();
            validated = false;
        }

        return validated;

    }
    public boolean ValidMinChars(String field){
        return field.trim().length()>3;
    }


    /*
    Función para guardar la información del usuario de una actividad a otra
     */
    private void saveUserData(LoginApp data){
        editor.putString("user",data.getUser());
        editor.putInt("session",data.getSessionID());
        editor.commit();
    }

    /**
     * Función en segundo plano que connectarà con el servidor, validarà el usuario y lanzará
     * la actividad principal, pasandole los datos del usuario y el sessionID
     * @param: objeto LoginApp
     * @return: devolverá un objeto Response
     */
    public Response serverLogin (LoginApp data) throws Exception{
        String serverUrl = "http://ec2-52-31-205-76.eu-west-1.compute.amazonaws.com/geoechoserv";
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
            int responseCode = con.getResponseCode();
            if (responseCode == 200){
                ObjectInputStream objectInput = new ObjectInputStream(con.getInputStream());
                result = (Response)objectInput.readObject();
            }else
                throw new Exception();

        }catch (Exception e){
            result = new Response();
            result.setSessionID(CONNECTION_ERROR);
        }

        return result;
    }
}
