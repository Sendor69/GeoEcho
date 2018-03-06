package papaya.geoecho;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static papaya.geoecho.R.id.regRegister;

public class Registre extends AppCompatActivity implements View.OnClickListener{

    EditText user, pw, mail;
    Button register;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registre);

        user = (EditText) findViewById(R.id.regUser);
        pw = (EditText) findViewById(R.id.regPasword);
        mail = (EditText) findViewById(R.id.regEmail);
        register = (Button)findViewById(R.id.regRegister);
        register.setOnClickListener(this);
    }

    @Override
    public void onClick (View view) {
        switch (view.getId()){
            case regRegister:
                new UserCreateTask().execute();
                break;
        }

    }

    public class UserCreateTask extends AsyncTask<Void, Void, Boolean> {

        /*Clase tonta, no hace nada */

        ProgressDialog mDialog = new ProgressDialog(Registre.this);

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
            mDialog.setMessage("Sign in...");
            mDialog.show();
        }
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
