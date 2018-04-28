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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import model.client.Logout;
import model.client.Message;
import model.client.QueryApp;
import model.client.Response;
import model.client.ResponseQueryApp;

public class MainGeoActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    //Gestor sharedPreferences
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    //Localizacion
    private GoogleMap mMap;
    private Location location;
    private LocationManager gestorLoc;
    private double longitud = 0.0;
    private double latitud = 0.0;
    SupportMapFragment mapFragment;
    LatLng myPosition;
    ArrayList<Marker> markerList;

    //Variable para mostrar públicos
    private String filter = FILTER_ALL;

    //Lista de mensajes global
    private List<Message> messageList;

    //Constantes
    public static final int CONNECTION_ERROR = -1;
    public static final String FILTER_ALL = "ALL";
    public static final String FILTER_PUBLIC = "PUBLIC";
    public static final String FILTER_PRIVATE = "PRIVATE";
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
                //Iniciaremos la actividad de nuevo mensaje, enviandole la localización actual
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

        //Inicializamos la lista de markers y messages
        markerList = new ArrayList<Marker>();
        messageList = new ArrayList<Message>();

        //Iniciamos la búsqueda de localización
        getLocation(gestorLoc);

    }

    /**
     * Añade los items al menú de la action bar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.geo_menu, menu);
        return true;
    }

    /**
     * Función para gestionar que ha de hacer cada botón del menú de la action bar
     */
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
                //nada
                break;
            case R.id.action_refresh:
                new serverLocationUpdate().execute();
                break;
            case R.id.item_all:
                filter = FILTER_ALL;
                markerFilter(filter);
                break;
            case R.id.item_publics:
                filter = FILTER_PUBLIC;
                markerFilter(filter);
                break;
            case R.id.item_privates:
                filter = FILTER_PRIVATE;
                markerFilter(filter);
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
        //Generamos el texto de aviso con un alertDialog
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
     * Función para mostrar un mensaje de alerta personalizado
     * @param: string
     * @return: objeto AlertDialog
     */
    public void showAlert (String title, String msg){
        AlertDialog alertDialog = new AlertDialog.Builder(MainGeoActivity.this,R.style.CustomAlert).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            //si el resultado es correcto, actualizamos el mapa
            new serverLocationUpdate().execute();
        }
    }

    /**
     * Función para filtrar los mensajes según el usuario elija
     * @param: string
     */
    public void markerFilter (String filter){
        switch (filter){
            case FILTER_ALL:
                for (Marker marker: markerList) {
                    marker.setVisible(true);
                    /*
                    //TODO Temporal hasta que el servidor solo nos envíe nuestros mensajes privados
                    if (marker.isDraggable()) {
                        marker.setVisible(true);

                    } else if (messageList.get(Integer.valueOf(marker.getTitle())).getUserReceiver().equals(sharedPref.getString("user", ""))){
                        marker.setVisible(true);
                    }else
                        marker.setVisible(false);
                        */
                }
                break;
            case FILTER_PUBLIC:
                for (Marker marker: markerList){
                    if (marker.isDraggable()){
                        marker.setVisible(true);
                    }else
                        marker.setVisible(false);
                }
                break;
            case FILTER_PRIVATE:
                for (Marker marker: markerList){
                    if (marker.isDraggable()){
                        marker.setVisible(false);
                    }else
                        //TODO Temporal hasta que el servidor solo nos envíe nuestros mensajes privados
                    //if (messageList.get(Integer.valueOf(marker.getTitle())).getUserReceiver().equals(sharedPref.getString("user","")))
                        marker.setVisible(true);
                }
                break;

        }
    }





    //===================================   MAPS   =========================================//

    /*
    *Función onClick de los markers modificada, controlaremos que la distancia sea la necesaria para
    * poder mostrar el mensaje al usuario. Si lo es, cargaremos el mensaje en otra actividad para verlo
   */
    @Override
    public boolean onMarkerClick(Marker m){
        if (distanceMarkerToLocation(m,location)<=20){
            //ToDo start activity para leer el mensaje
            Message temp = messageList.get(Integer.parseInt(m.getTitle()));
            Intent i = new Intent (this, MessageView.class);
            i.putExtra("Message",temp);
            startActivity(i);
        }
        return true;
    }
      /*
      * Función automática de android para coger la localización mediante el GPS
       */
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
                //Toast.makeText(this,"Please connect GPS to get location",Toast.LENGTH_SHORT).show();

        }catch (SecurityException ex){}
    }

    /*
    *Genera el mapa dentro del fragmento de la actividad
   */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        //Codigo para activar la localización del dispositivo con el punto azul, el tracker
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        if (location == null) {
            //No haremos nada hasta que se encuentre la localización
        }else{
            latitud = location.getLatitude();
            longitud = location.getLongitude();
        }
        myPosition = new LatLng(latitud, longitud);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition,16));
        mMap.setOnMarkerClickListener(this);
    }

    /**
     * Usaremos esta función para recargar el mapa cada vez que la localización sea diferente
     */
    @Override
    public void onLocationChanged(Location location) {
        //Cada vez que cambie la localización, actualizará la posicion actual y la enviará al servidor
        if (location !=null){
            this.location = location;
            new serverLocationUpdate().execute();
        }
    }


    /**
     * Función que modifica el icono de los mensajes según distancia
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

    /**
     * Funciones sobreescritas de google Maps para controlar el estado de la conexión GPS
     * Las usaremos para habilitar o deshabilitar el botón del nuevo mensaje
     */
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

    /**
     * Funciones que habilitan o deshabilitan el botón para crear nuevos mensajes
     */
    public void disableButton(){
        fab.setEnabled(false);
        fab.setAlpha(0.3f);
    }
    public void enableButton(){
        fab.setEnabled(true);
        fab.setAlpha(1f);
    }


    //===================================   Server Connections   =========================================//
    /**
     * Función en un hilo a parte  para enviar petición al servidor de eliminar la sessionId asignada
     * a este usuario.
     *  @param: Logout class
     *  @return: Response class
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

        String serverUrl = "https://ec2-52-31-205-76.eu-west-1.compute.amazonaws.com:8443/geoechoserv";
        Response result = new Response();
        URL url = new URL(serverUrl);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setSSLSocketFactory(CustomSSLSocketFactory.getSSLSocketFactory(MainGeoActivity.this));
        con.setHostnameVerifier(new AllowAllHostnameVerifier());

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

    /**
     * Función en un hilo a parte  para enviar la localización actual al servidor, y que este nos
     * devuelva una lista con todos los mensajes cercanos
     * @param: location
     * @return: List<Message>
     */
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
                messageList = result;
                markerList.clear();
                mMap.clear();
                if (result.size() > 0) {
                    for (Message temp : result) {
                        createMarkerFromMessage(temp, result.indexOf(temp));
                    }
                }
                updateMarkers(location);
                markerFilter(filter);
            }
        }

        @Override
        protected void onPreExecute() {
            //Algo ?
        }
    }

    /**
     * Función que transformará los mensajes enviados por el servidor en markers clickables en el mapa
     * @param: message
     * @return: creará los markers del mapa
     */
    public void createMarkerFromMessage (Message message, int posicion){

        Marker temp = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(message.getCoordY(), message.getCoordX()))
                .draggable(message.isMsgPublic())
                .title(String.valueOf(posicion))
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_geodesactived)));
                //.snippet(message.getPhotoBase64())
                //.title(formatMessage(message))
                //.title(message.getText()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        markerList.add(temp);
    }

    /**
     * Formateo del mensaje para mostrarlo correctamente al usuario
     * @param: message
     * @return: String con el texto ya formateado
     */
    public String formatMessage(Message message){
        return "Data: "+message.getDate().toString() + "\n"
                + "User: " +message.getUserSender().toString() + "\n\n"
                + "Message: " + message.getText();

    }
    /**
     * Función en segundo plano enviará petición al servidor para que elimine la sessionID
     * @param: objeto Logout
     * @return: objeto Response para verificar que el logout es correcto
     */
    public List<Message> updateServerMessageList (Location location) throws Exception{
        String serverUrl = "https://ec2-52-31-205-76.eu-west-1.compute.amazonaws.com:8443/geoechoserv";
        ResponseQueryApp response = null;
        List<Message> lista = new ArrayList<Message>();

        QueryApp data = new QueryApp();
        data.setCoordX((float)location.getLongitude());
        data.setCoordY((float)location.getLatitude());
        data.setSessionID(sharedPref.getInt("session",0));
        //data.setSessionID(1123259813);

        URL url = new URL(serverUrl);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setSSLSocketFactory(CustomSSLSocketFactory.getSSLSocketFactory(MainGeoActivity.this));
        con.setHostnameVerifier(new AllowAllHostnameVerifier());

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
            System.out.println(e);
        }

        return lista;

    }

}
