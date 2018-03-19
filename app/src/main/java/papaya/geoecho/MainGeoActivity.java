package papaya.geoecho;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import model.client.Logout;
import model.client.Response;

public class MainGeoActivity extends AppCompatActivity implements LocationListener {

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    //Localizacion
    private Location location;
    private LocationManager gestorLoc;
    private double longitud = 0.0;
    private double latitud = 0.0;

    //Constantes
    public static final int CONNECTION_ERROR = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_geo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle("GeoEcho");

        //Gestion GPS
        gestorLoc = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        sharedPref = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googleMap) {
                LatLng sydney = new LatLng(41.15, 1.1167);
                googleMap.addMarker(new MarkerOptions()
                        .position(sydney)
                        .title("Papaya TEAM!")
                        .snippet("Creating the BEST APP"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,16));
            }

        });
    }

    /*
Función para enviar petición al servidor de eliminar la sessionId asignada a este usuario
 */
    public class UserLogoutTask extends AsyncTask<Void, Void, Response> {


        ProgressDialog mDialog = new ProgressDialog(MainGeoActivity.this);

        @Override
        protected Response doInBackground(Void... params) {

            Response result = new Response();
            Logout logoutData = new Logout();
            logoutData.setSessionID(sharedPref.getInt("session",0));

            try {
                result =serverLogout(logoutData);
            } catch (Exception e) {

            }
            return result;
        }

        @Override
        protected void onPostExecute(final Response result) {
            int status = result.getStatusQuery();

            //Gestionamos la respuesta del servidor
            if (status == result.LOGOUT_FAILED || status == CONNECTION_ERROR){
                showAlert("Error", "Error finishing this session. Please try again");

            }else if (status == result.LOGOUT_OK) {
                Intent intent = new Intent(MainGeoActivity.this, LogReg.class);
                // se eliminan todas las actividades y se inicia desde la pantalla de inicio/registro
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                editor.clear();
                startActivity(intent);
            }
            mDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            //Autenticando login...
            mDialog.setMessage("Disconnecting...");
            mDialog.show();
        }
    }

    /*
    Función que mostrará un mensaje de si/no preguntando si se quiere cerrar la sesión
     */
    public void logout(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        new UserLogoutTask().execute();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder ab = new AlertDialog.Builder(MainGeoActivity.this,R.style.CustomAlert);
        ab.setMessage("You will finalize your session. Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).setTitle("LoginActivity out");
        AlertDialog alertDialog = ab.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
    }

    //Hacemos que el botón de ir hacia atras, llame a la función logout
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {


        switch(keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
                logout();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Función en segundo plano enviará petición al servidor para que elimine la sessionID
     * @param: objeto Logout
     * @return: objeto Response para verificar que el logout es correcto
     */
    public Response serverLogout ( Logout data) throws Exception{

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
                //recibiremos un statusQuery
                ObjectInputStream objectInput = new ObjectInputStream(con.getInputStream());
                result = (Response)objectInput.readObject();
            }else
                throw new Exception();

        }catch (Exception e){
            result.setStatusQuery(CONNECTION_ERROR);
        }

        return result;
    }

    public void showAlert (String title, String msg){
        AlertDialog alertDialog = new AlertDialog.Builder(MainGeoActivity.this,R.style.CustomAlert).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.geo_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_logout:
                new UserLogoutTask().execute();
                break;
            case R.id.action_map:
                break;
            case R.id.action_profile:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        //Com que la localització triga uns segons després d'activar el GPS, ens avisarà quan la trobi
        if (location !=null){
            this.location = location;
        }
    }
    //TODO - Gestion de localización, aun por implementar
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        String missatge = "";
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                missatge = "GPS fora de servei";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                missatge = "GPS temporalment no disponible";
                break;
            case LocationProvider.AVAILABLE:
                missatge = "GPS actiu";
                break;
        }

        Toast.makeText(getApplicationContext(),
                missatge,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        //Si el GPS està actiu no activarem els botons encara. Esperarem a tenir la localització
        Toast.makeText(this,"GPS actiu. Buscant localitzacio...",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Si desactivem el GPS manualment
        Toast.makeText(this,"GPS desactivat",Toast.LENGTH_SHORT).show();
        location = null;
    }

}