package papaya.geoecho;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import static papaya.geoecho.R.id.rLogin;
import static papaya.geoecho.R.id.rRegister;

public class LogReg extends AppCompatActivity implements View.OnClickListener {


    //Permisos que pediremos al usuario
    private final int ALL_PERMISSION = 123;
    private String[] permisosFitxer = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA};

    //UI components
    Button Login, Sign;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_reg);

        Login = (Button) findViewById(R.id.rLogin);
        Login.setOnClickListener(this);
        Sign = (Button) findViewById(R.id.rRegister);
        Sign.setOnClickListener(this);


        //Demanem permisos
        ActivityCompat.requestPermissions(LogReg.this,permisosFitxer,ALL_PERMISSION);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSION: {
                //Si no se acepta, grantResults estará vacío
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[3] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[4] == PackageManager.PERMISSION_GRANTED) {
                    //permisos aceptados
                    try{

                    }catch (SecurityException ex){}

                } else {
                    // permisos no aceptados, se finaliza
                    Toast.makeText(this,"Permissions must be accepted",Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case rLogin:
                Intent i = new Intent(this, LoginActivity.class);
                startActivity(i);
                break;
            case rRegister:
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
        }
    }
}
