package papaya.geoecho;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import model.client.Logout;
import model.client.Message;
import model.client.QueryApp;
import model.client.Response;
import model.client.ResponseQueryApp;

public class MainGeoActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    //Localizacion
    private GoogleMap mMap;
    private Location location;
    private LocationManager gestorLoc;
    private double longitud = 0.0;
    private double latitud = 0.0;
    SupportMapFragment mapFragment;
    MarkerOptions startMark;
    LatLng myPosition;
    ArrayList<Marker> markerList;

    //Constantes
    public static final int CONNECTION_ERROR = -1;

    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_geo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle("GeoEcho");

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (location != null) {
                    editor.putFloat("Lat",(float)location.getLatitude());
                    editor.putFloat("Long",(float)location.getLongitude());
                    editor.commit();
                    Intent i = new Intent(getApplicationContext(), newMessage.class);
                    startActivityForResult(i,0);
                }
            }
        });

        //Gestion GPS
        gestorLoc = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        sharedPref = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        editor = sharedPref.edit();


        // Obtenim el fragment del mapa i omplim el nostre "MapView"
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Inicializamos la lista de markers
        markerList = new ArrayList<Marker>();

        //Por si no encontramos una posicion, mostraremos esto
        startMark = (new MarkerOptions()
                .position(new LatLng(0,0))
                .title("Papaya TEAM!")
                .snippet("Searching your position!"));

        //Iniciamos la búsqueda de localizacion
        getLocation(gestorLoc);






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

        if (item.isChecked()){
            item.setChecked(false);
        }else
            item.setChecked(true);

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_logout:
                new UserLogoutTask().execute();
                break;
            case R.id.action_map:
                break;
            case R.id.action_profile:
                break;
            case R.id.item_all:
                break;
            case R.id.item_publics:
                break;
        }

        return super.onOptionsItemSelected(item);
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


    public void showAlert (String title, String msg){
        AlertDialog alertDialog = new AlertDialog.Builder(MainGeoActivity.this,R.style.CustomAlert).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Marker temp;
        if (requestCode == 0 && resultCode == RESULT_OK) {
            new serverLocationUpdate().execute();
        }
    }





    //TODO ===================================   MAPS   =========================================//


    @Override
    public boolean onMarkerClick(Marker m){
        if (distanceMarkerToLocation(m,location)<=20){
            //ToDo start activity para leer el mensaje
            Intent i = new Intent (this, MessageView.class);
            i.putExtra("text",m.getTitle());
            if (m.getSnippet()!=null)
                i.putExtra("photo64",m.getSnippet());
            else
                i.putExtra("photo64","EMPTY");
            startActivity(i);
        }
        return true;
    }

    public void getLocation(LocationManager gestor) {
        //Comprobará si tenim connexió GPS (precisa)
        boolean isGPSEnabled = gestor
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        try {
            if (isGPSEnabled) { //Si tenim connexió GPS, actualitcem la localització

                if (location == null) {
                    gestor.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            1000,
                            1, this);
                    Log.d("GPS", "GPS Enabled");
                    if (gestor != null) {
                        location = gestor
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }
            }else
                disableButton();
                Toast.makeText(this,"Please connect GPS to get location",Toast.LENGTH_SHORT).show();

        }catch (SecurityException ex){}
    }
    //Genera el mapa dentro del fragmento de la actividad
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        //Codigo para activar la localización del dispositivo con el punto azul
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        if (location == null) {
            mMap.addMarker(startMark);
        }else{
            latitud = location.getLatitude();
            longitud = location.getLongitude();
        }
        myPosition = new LatLng(latitud, longitud);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition,16));
        mMap.setOnMarkerClickListener(this);
    }

    //Que hacer cada vez que se modifica la localización
    @Override
    public void onLocationChanged(Location location) {
        //Cada vez que cambie la localización, actualizará la posicion actual y la enviará al servidor
        if (location !=null){
            this.location = location;
            new serverLocationUpdate().execute();
            //updateMyPosition(location);
        }
    }


    /**
     * Función que aplica transparencia según la cercania al mensaje
     * @param: localización
     */
    public void updateMarkers(Location location){
        for (Marker marker: markerList){
            if (distanceMarkerToLocation(marker,location)<=20){
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_geoactived));
            }else
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_geodesactived));
        }

    }
    /**
     * Función que calcula la distancia entre un Marker (mensaje) y una localización
     * @param: Marker, localización
     * @return: distancia en decimal
     */
    public float distanceMarkerToLocation(Marker mark, Location location){
        Location temp = new Location("Temp");
        temp.setLatitude(mark.getPosition().latitude);
        temp.setLongitude(mark.getPosition().longitude);
        return location.distanceTo(temp);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        String missatge = "";
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                disableButton();
                missatge = "GPS out of service";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                disableButton();
                missatge = "GPS temporally not available";
                break;
            case LocationProvider.AVAILABLE:
                enableButton();
                missatge = "GPS active";
                break;
        }

        //Toast.makeText(getApplicationContext(),missatge,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        //Si el GPS està actiu.. .
     //   Toast.makeText(this,"GPS active. Looking for location...",Toast.LENGTH_SHORT).show();
        enableButton();
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Si desactivem el GPS manualment
       // Toast.makeText(this,"GPS desactivated",Toast.LENGTH_SHORT).show();
        disableButton();
        location = null;
    }

    public void disableButton(){
        fab.setEnabled(false);
        fab.setAlpha(0.3f);
    }
    public void enableButton(){
        fab.setEnabled(true);
        fab.setAlpha(1f);
    }


    //TODO ===================================   Server Connections   =========================================//
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


    public class serverLocationUpdate extends AsyncTask<Void, Void, List<Message>> {

        @Override
        protected List<Message> doInBackground(Void... params) {
            List<Message> result = new ArrayList<Message>();
            try {
                //TODO conexión con el servidor
                result=  updateServerMessageList(location);

            } catch (Exception e) {
                showAlert("Connection Error","Imposible to connect with server. Please try again");
            }

            return result;
        }

        @Override
        protected void onPostExecute(final List<Message> result) {
            //TODO Gestionamos la respuesta del servidor
            if (result !=null) {
                if (result.size() > 0) {
                    markerList.clear();
                    mMap.clear();
                    for (Message temp : result) {
                        createMarkerFromMessage(temp);
                    }
                }
                updateMarkers(location);
            }
        }

        @Override
        protected void onPreExecute() {
            //Algo ?
        }
    }
    public void createMarkerFromMessage (Message message){
        Marker temp;
        int code = message.hashCode();
        String tt = message.getText();
        String texto = tt.toString();
        temp= mMap.addMarker(new MarkerOptions()
                .position(new LatLng(message.getCoordY(),message.getCoordX())).snippet(message.getPhotoBase64())
                //.title(message.getText()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                .title(message.getText()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_geodesactived)));
        markerList.add(temp);
    }

    /**
     * Función en segundo plano enviará petición al servidor para que elimine la sessionID
     * @param: objeto Logout
     * @return: objeto Response para verificar que el logout es correcto
     */
    public List<Message> updateServerMessageList (Location location) throws Exception{
        String serverUrl = "http://ec2-52-31-205-76.eu-west-1.compute.amazonaws.com/geoechoserv";
        ResponseQueryApp response = null;
        List<Message> lista = new ArrayList<Message>();

        QueryApp data = new QueryApp();
        data.setCoordX((float)location.getLongitude());
        data.setCoordY((float)location.getLatitude());
        data.setSessionID(sharedPref.getInt("session",0));
        //data.setSessionID(1123259813);

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
            if (responseCode == 200) {
                ObjectInputStream objectInput = new ObjectInputStream(con.getInputStream());
                response = (ResponseQueryApp) objectInput.readObject();
                lista = response.getMessageList();
                //lista = (List<Message>) objectInput.readObject();
            }
        }catch (Exception e){
        }

        return lista;

    }

}
