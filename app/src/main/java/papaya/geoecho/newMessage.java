package papaya.geoecho;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import model.client.Message;
import model.client.Response;

import static papaya.geoecho.R.id.add_photo;
import static papaya.geoecho.R.id.send_message;

public class newMessage extends AppCompatActivity implements View.OnClickListener {

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    //UI elements
    private Button bPhoto, bSend;
    private TextView photoAdded;
    private EditText messageText;
    private String photoBase64;
    private File photo;
    private Message mensaje;

    //Test
    private ImageView imagen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        bPhoto = (Button)findViewById(add_photo);
        bPhoto.setOnClickListener(this);
        bSend = (Button)findViewById(R.id.send_message);
        bSend.setOnClickListener(this);

        photoAdded = (TextView)findViewById(R.id.image_added);
        messageText = (EditText)findViewById(R.id.text_message);
        imagen = (ImageView) findViewById(R.id.photo_added);

        //Con este código se evita un error de Uri.fromFile en algunas versiones
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        sharedPref = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    @Override
    public void onClick (View view){
        switch (view.getId()){
            case add_photo:
                addPhoto();
                break;
            case send_message:
                mensaje = generateMessage();
                new placeMessage().execute();
                break;
        }

    }

    public Message generateMessage(){
        Float latitud = sharedPref.getFloat("Lat",0);
        Float longitud = sharedPref.getFloat("Long",0);
        String text = messageText.getText().toString();
        String imageBase64 = photoBase64 ;
        String userSender = sharedPref.getString("user","");
        String userReceiver = null;
        Date date = Calendar.getInstance().getTime();
        // ToDo revisar si he de enviarlo yo
        int life = 10;
        boolean msgPublic = true;
        boolean msgVisible = false;
        boolean msgReaded = false;

        return new Message(longitud,latitud,text,imageBase64,userSender, userReceiver, date, life, msgPublic, msgVisible, msgReaded);
    }

    public void addPhoto() {
        // Intent para la cámara
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        // Se crea un nuevo fichero para guardar la foto temporalmente
        photo = null;
        try {
            photo = createImageFile();

        }catch (IOException e){}
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));

        //Activity cámara
        startActivityForResult(intent, 0);
    }

    private File createImageFile() throws IOException {
        //Creamos un nombre único para el fichero
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp;
        File storageDir = getExternalFilesDir("TEMP");
        if (!storageDir.exists()){
            storageDir.mkdir();
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* sufix */
                storageDir      /* directori */
        );
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Primer cridem al mètode d'Activity per que faci la seva tasca
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0: //Cámara
                if (resultCode == Activity.RESULT_OK) {
                    Uri photoUri = Uri.fromFile(photo);
                        // Amb el contentResolver accedim al contingut de l'activitat (imatge)
                        ContentResolver contRes = getContentResolver();
                        // Indiquem que el fitxer ha canviat
                        contRes.notifyChange(photoUri, null);
                    Bitmap bitmap;
                    try {
                        bitmap = android.provider.MediaStore.Images.Media
                                .getBitmap(contRes, photoUri);
                        photoBase64 = bitmapToBase64(bitmap);

                        bitmap = base64ToBitmap(photoBase64);
                        imagen.setImageBitmap(bitmap);
                        photoAdded.setVisibility(View.VISIBLE);

                    } catch (Exception e) {
                        Toast.makeText(this, "Error loading photo" +
                                        photoUri.toString(),
                                Toast.LENGTH_SHORT).show();
                        deleteFile();
                    }
                }else {
                    deleteFile();
                    Toast.makeText(this,"Error adding photo",Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

        /*
    Función AsyncTask que en segundo plano connectarà con el servidor, validarà el usuario y lanzará
        la actividad principal, pasandole los datos del usuario y el sessionID
     */

    public class placeMessage extends AsyncTask<Void, Void, Response> {

        ProgressDialog mDialog = new ProgressDialog(newMessage.this);


        @Override
        protected Response doInBackground(Void... params) {
            Response result = new Response();
            try {
                //TODO conexión con el servidor
                //result=  messageToServer(mensaje);
                Thread.sleep(2000);
            } catch (Exception e) {
                showAlert("Connection Error","Imposible to connect with server. Please try again");
            }

            return result;
        }

        @Override
        protected void onPostExecute(final Response result) {
            int status = result.getStatusQuery();

            //TODO Gestionamos la respuesta del servidor

            /*
            if (status == result.LOGIN_FAILED){
                showAlert("Authentication", "User or password are incorrects");

            }else if (status == result.LOGIN_OK){
                loginData.setSessionID(result.getSessionID());
                Intent i = new Intent(LoginActivity.this, MainGeoActivity.class);
                startActivity(i);

            }else if (status == CONNECTION_ERROR){
                showAlert("Error", "Server connection failed. Please try again");

            }else
                showAlert("Unkown Error", "Contact with administrator");
                */
            mDialog.dismiss();
            //Si ha ido bien
            setResult(RESULT_OK);
            if (photo != null)
                deleteFile();
            finish();
        }
        @Override
        protected void onPreExecute() {
            //Autenticando login...
            mDialog.setMessage("Placing message...");
            mDialog.show();
        }
    }

    /**
     * Función en segundo plano que connectarà con el servidor y enviará el mensaje
     * @param: objeto Message
     * @return: devolverá un objeto Response para saber si se ha podido enviar todo bien
     */
    public Response messageToServer (Message data) throws Exception{
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
                ObjectInputStream objectInput = new ObjectInputStream(con.getInputStream());
                result = (Response)objectInput.readObject();
            }else
                throw new Exception();

        }catch (Exception e){
            result = new Response();
        }

        return result;
    }


    public void deleteFile(){
        Uri uri = Uri.fromFile(photo);
        File fdelete = new File(uri.getPath());
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                //No haremos nada
            } else {
                Toast.makeText(this,"Error deleting file: " + fdelete.getName(),Toast.LENGTH_SHORT).show();
            }
        }

    }

    //Nos aseguramos que al destruir la actividad o darle al botón de retroceder, se eliminará la imagen
    @Override
    public void onBackPressed(){
        if (photo != null)
            deleteFile();
        super.onBackPressed();
    }

    /**
     * Función que convierte un bitmap en un string Base64
     * @param: objeto bitmap
     * @return: devolverá un string Base64
     */
    public static String bitmapToBase64(Bitmap image)
    {
        Bitmap immagex=image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b,Base64.DEFAULT);
        return imageEncoded;
    }
    /**
     * Función que convierte un string Base64 en bitmap
     * @param: string Base64
     * @return: devolverá objeto bitmap
     */
    public static Bitmap base64ToBitmap(String input)
    {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    /**
     * Función que mostrará un AlertDialog personalizado
     * @param: Titulo del error
     * @param: Mensaje del error
     */
    public void showAlert (String title, String msg){
        AlertDialog alertDialog = new AlertDialog.Builder(newMessage.this,R.style.CustomAlert).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }


}
