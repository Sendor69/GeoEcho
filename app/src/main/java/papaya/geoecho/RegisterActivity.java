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

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import model.client.RegisterApp;
import model.client.Response;

import static papaya.geoecho.R.id.regRegister;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText user, password, mail;
    private Button register;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private RegisterApp registerData;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registre);

        user = (EditText) findViewById(R.id.regUser);
        password = (EditText) findViewById(R.id.regPasword);
        mail = (EditText) findViewById(R.id.regEmail);
        register = (Button)findViewById(R.id.regRegister);
        register.setOnClickListener(this);
        registerData = new RegisterApp();
        
        /*Shared Preferences con los datos del usuario */
        sharedPref = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    @Override
    public void onClick (View view) {
        switch (view.getId()){
            case regRegister:
                if (checkDataRegister()){
                    registerData.setUser(user.getText().toString().trim());
                    registerData.setPass(password.getText().toString().trim());
                    registerData.setMail(mail.getText().toString().trim());
                    new UserCreateTask().execute();
                }
                break;
        }

    }

    /*
    Función que en segundo plano enviará los datos del usuario nuevo al servidor para que los valide
    y si son correctos, los guardará en la base de datos y enviará una respuesta positiva.
     */
    public class UserCreateTask extends AsyncTask<Void, Void, Response> {

        ProgressDialog mDialog = new ProgressDialog(RegisterActivity.this);

        @Override
        protected Response doInBackground(Void... params) {
            // TODO: intenta conectarte al server y logear.
            Response result = new Response();
            try {
                result = validateRegister(registerData);
                Thread.sleep(2000);
            } catch (Exception e) {
                showAlert("Connection Error","Imposible to connect with server. Please try again");
            }

            return result;
        }

        @Override
        protected void onPostExecute(final Response result) {
            int session = result.getSessionID();
                switch (session){
                    case -1:
                        showAlert("Error", "Server connection failed. Please try again");
                        break;
                    case 1:
                        user.setError("User already exists");
                        user.requestFocus();
                        break;
                    case 2:
                        mail.setError("Email already exists");
                        user.requestFocus();
                        break;
                    default:
                        registerData.setSessionID(session);
                        Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                        saveUserData(registerData);
                        startActivity(i);
                        break;
                }
            mDialog.dismiss();
        }
        @Override
        protected void onPreExecute() {
            //Autenticando login...
            mDialog.setMessage("Sign in...");
            mDialog.show();
        }
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /*
    Función para controlar los EditText, longitud, email correcto, etc
     */
    public boolean checkDataRegister(){
        Boolean validated = true;

        password.setError(null);
        user.setError(null);
        mail.setError(null);

        String passTemp = password.getText().toString();
        String userTemp = user.getText().toString();
        String mailTemp = mail.getText().toString();

        if (TextUtils.isEmpty(mailTemp)){
            mail.setError("Field required");
            mail.requestFocus();
            validated = false;
        }else if(!isEmailValid(mailTemp)){
            mail.setError("Enter a valid @mail");
            mail.requestFocus();
            validated = false;
        }

        if(TextUtils.isEmpty(passTemp)){
            password.setError("Field required");
            password.requestFocus();
            validated = false;
        }else if(!validMinChars(passTemp)) {
            password.setError("At least 4 chars needed");
            password.requestFocus();
            validated = false;
        }

        if(TextUtils.isEmpty(userTemp)){
            user.setError("Field required");
            user.requestFocus();
            validated = false;
        }else if(!validMinChars(userTemp)){
            user.setError("At least 4 chars needed");
            user.requestFocus();
            validated = false;
        }

        return validated;

    }
    public boolean validMinChars(String user){
        return user.trim().length()>3;
    }

    public void showAlert (String title, String msg){
        AlertDialog alertDialog = new AlertDialog.Builder(RegisterActivity.this,R.style.CustomAlert).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    /*
    Guarda los datos del usuario en un sharedPreferences para intercambiarla por las actividades
     */
    private void saveUserData(RegisterApp data){
        editor.putString("user",data.getUser());
        editor.putString("mail",data.getMail());
        editor.putInt("session",data.getSessionID());
        editor.commit();
    }

    /*
    Función que se ejecutará dentro del AsyncTask para validar el registro con el servidor. Devuelve
    un objeto tipo Response de donde extraeremos la respuesta en forma de sessionID
     */
    public Response validateRegister(RegisterApp data) throws Exception{

        String serverUrl = "http://geoechoserv.machadocode.com/geoechoserv";
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
            result.setSessionID(-1);
        }

        return result;
    }
}
