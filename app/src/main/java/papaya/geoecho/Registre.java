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

import java.util.Random;

import papaya.geoecho.Model.RegisterApp;
import papaya.geoecho.Model.Response;

import static papaya.geoecho.R.id.regRegister;

public class Registre extends AppCompatActivity implements View.OnClickListener{

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

    public class UserCreateTask extends AsyncTask<Void, Void, Response> {

        /*Clase tonta, no hace nada */

        ProgressDialog mDialog = new ProgressDialog(Registre.this);

        @Override
        protected Response doInBackground(Void... params) {
            // TODO: intenta conectarte al server y logear.
            Response result = new Response();
            try {
                // Simulate network access.
                result = validateRegister(registerData);
                Thread.sleep(2000);
            } catch (Exception e) {
                showAlert("Connection Error","Imposible to connect with server. Please try again");
            }

            return result;
        }

        @Override
        protected void onPostExecute(final Response result) {
            String session = result.getSessionID();
            if (session != null){
                switch (session){
                    case "USER_DUPLICATED":
                        user.setError("User already exists");
                        user.requestFocus();
                        break;
                    case "MAIL_DUPLICATED":
                        mail.setError("Email already exists");
                        user.requestFocus();
                        break;
                    default:
                        registerData.setSessionID(session);
                        Intent i = new Intent(Registre.this, MainActivity.class);
                        saveUserData(registerData);
                        startActivity(i);
                        break;
                }
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

    public boolean checkDataRegister(){
        Boolean validated = true;

        password.setError(null);
        user.setError(null);
        mail.setError(null);

        String passTemp = password.getText().toString();
        String userTemp = user.getText().toString();
        String mailTemp = mail.getText().toString();

        if(TextUtils.isEmpty(passTemp)){
            password.setError("Field required");
            validated = false;
        }else if(!validUser(passTemp)) {
            password.setError("At least 4 chars needed");
            validated = false;

        }if(TextUtils.isEmpty(userTemp)){
            user.setError("Field required");
            validated = false;
        }else if(!validUser(userTemp)){
            user.setError("At least 4 chars needed");
            validated = false;
        }

        if (TextUtils.isEmpty(mailTemp)){
            mail.setError("Field required");
            validated = false;
        }else if(!isEmailValid(mailTemp)){
            mail.setError("Enter a valid @mail");
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

    public void showAlert (String title, String msg){
        AlertDialog alertDialog = new AlertDialog.Builder(Registre.this,R.style.CustomAlert).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void saveUserData(RegisterApp data){
        editor.putString("user",data.getUser());
        editor.putString("mail",data.getMail());
        editor.putString("session",data.getSessionID());
        editor.commit();
    }

    public Response validateRegister(RegisterApp data) throws Exception{

        Response result = new Response();
        Random rdm = new Random();

        switch (rdm.nextInt(3)){
            case 1:
                result.setSessionID("USER_DUPLICATED");
                break;
            case 2:
                result.setSessionID("MAIL_DUPLICATED");
                break;
            default:
                result.setSessionID(data.getUser()+123);
                break;
        }
        return result;

        /* TODO por implementar con el servidor

        String serverUrl = "";


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

        */
    }
}
