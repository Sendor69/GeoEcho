package papaya.geoecho;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static papaya.geoecho.R.id.bLogin;
import static papaya.geoecho.R.id.tRecordar;


public class Login extends AppCompatActivity implements View.OnClickListener {

    //Referencias UI

    Button login;
    EditText user,password;
    TextView record;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        login = (Button) findViewById(bLogin);
        login.setOnClickListener(this);
        user = (EditText)findViewById(R.id.eUsername);
        password = (EditText) findViewById(R.id.ePasword);
        record = (TextView) findViewById(tRecordar);
        record.setOnClickListener(this);

    }

    @Override
    public void onClick (View view) {
        switch (view.getId()){
            case bLogin:
                new UserLoginTask().execute();
                Intent i = new Intent (this,MainActivity.class);
                startActivity(i);
                break;
            case tRecordar:
                //ToDO por implementar
                break;
        }

    }



    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        /*Clase tonta, no hace nada */

        ProgressDialog mDialog = new ProgressDialog(Login.this);

        UserLoginTask() {

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: intenta conectarte al server y logear.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mDialog.dismiss();

        }
        @Override
        protected void onPreExecute() {
            //Autenticando login...
            mDialog.setMessage("Authenticating...");
            mDialog.show();
        }
    }
}
