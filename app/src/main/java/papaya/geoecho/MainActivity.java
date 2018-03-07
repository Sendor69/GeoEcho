package papaya.geoecho;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import model.client.Logout;
import model.client.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    TextView user;
    View headerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        headerView = navigationView.getHeaderView(0);
        user = (TextView)headerView.findViewById(R.id.tUserNav);
        user.setText(sharedPref.getString("user",""));

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_logOff) {
            new UserLogoutTask().execute();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    /*
    Función para enviar petición al servidor de eliminar la sessionId asignada a este usuario
     */
    public class UserLogoutTask extends AsyncTask<Void, Void, Response> {


        ProgressDialog mDialog = new ProgressDialog(MainActivity.this);

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
            mDialog.dismiss();
            Intent intent = new Intent (MainActivity.this,LogReg.class);
            // se eliminan todas las actividades y se inicia desde la pantalla de inicio/registro
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        @Override
        protected void onPreExecute() {
            //Autenticando login...
            mDialog.setMessage("Disconnecting...");
            mDialog.show();
        }
    }

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

        AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this,R.style.CustomAlert);
        ab.setMessage("You will finalize your session. Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).setTitle("LoginActivity out");
        AlertDialog alertDialog = ab.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
    }

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

    public Response serverLogout ( Logout data) throws Exception{

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
                System.out.println("+++++++++++++++ Logout correcto ++++++++++++++");
            }else
                throw new Exception();

        }catch (Exception e){
            System.out.println("************** Ha fallado el logout ***************");
        }

        return result;
    }






}
