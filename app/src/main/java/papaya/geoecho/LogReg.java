package papaya.geoecho;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import static papaya.geoecho.R.id.rLogin;
import static papaya.geoecho.R.id.rRegister;

public class LogReg extends AppCompatActivity implements View.OnClickListener {

    //UI components
    Button Login, Sign;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_reg);

        Login = (Button) findViewById(rLogin);
        Login.setOnClickListener(this);
        Sign = (Button) findViewById(R.id.rRegister);
        Sign.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case rLogin:
                Intent i = new Intent(this, Login.class);
                startActivity(i);
                break;
            case rRegister:
                Intent intent = new Intent(this, Registre.class);
                startActivity(intent);
        }
    }
}
