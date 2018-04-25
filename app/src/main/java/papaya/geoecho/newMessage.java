package papaya.geoecho;

import android.app.ProgressDialog;
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
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

import model.client.Message;
import model.client.Response;

import static papaya.geoecho.R.id.add_photo;
import static papaya.geoecho.R.id.radio_private;
import static papaya.geoecho.R.id.radio_public;
import static papaya.geoecho.R.id.send_message;

public class newMessage extends AppCompatActivity implements View.OnClickListener {

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    //UI elements
    private Button bPhoto, bSend;
    private RadioButton rbPublic, rbPrivate;
    private TextView photoAdded;
    private EditText messageText, messagePrivateUser;
    private String photoBase64;
    private File photo;
    private Message mensaje;
    private ConstraintLayout l_privateUser;


    //Test
    private ImageView imagen;

    //Path imagen
    private String dir;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        sharedPref = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        bPhoto = (Button)findViewById(add_photo);
        bPhoto.setOnClickListener(this);
        bSend = (Button)findViewById(R.id.send_message);
        bSend.setOnClickListener(this);
        rbPublic = (RadioButton) findViewById(R.id.radio_public);
        rbPublic.setOnClickListener(this);
        rbPrivate = (RadioButton) findViewById(R.id.radio_private);
        rbPrivate.setOnClickListener(this);
        l_privateUser = (ConstraintLayout)findViewById(R.id.layout_privateUser);

        photoAdded = (TextView)findViewById(R.id.image_added);
        messageText = (EditText)findViewById(R.id.text_message);
        messagePrivateUser = (EditText)findViewById(R.id.text_sendTo);
        imagen = (ImageView) findViewById(R.id.photo_added);

        //Limitamos el texto a 255 characteres para que entre en la base de datos
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(255);
        messageText.setFilters(FilterArray);

        //Con este código se evita un error de Uri.fromFile en algunas versiones
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        //Apuntamos a la memoria externa
        dir = Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    @Override
    public void onClick (View view){
        switch (view.getId()){
            case add_photo:
                addPhoto();
                break;
            case send_message:
                if (checkDataMessage()) {
                    mensaje = generateMessage();
                    new placeMessage().execute();
                }
                break;
            case radio_private:
                rbPublic.setChecked(false);
                l_privateUser.setVisibility(View.VISIBLE);
                break;
            case radio_public:
                rbPrivate.setChecked(false);
                messagePrivateUser.setText("");
                Boolean test = messagePrivateUser.getText().toString().trim().equals("") ? true : false ;
                l_privateUser.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * Función para validar los campos user y password.
     * Los campos no pueden estar vacíos y han de contener más de 3 caracteres
     */
    public boolean checkDataMessage(){
        Boolean validated = true;

        if (rbPrivate.isChecked()){
            if (TextUtils.isEmpty(messagePrivateUser.getText())){
                messagePrivateUser.setError("User required");
                messagePrivateUser.requestFocus();
                validated = false;
            }
        }
        if (TextUtils.isEmpty(messageText.getText())) {
            messageText.setError("Message can't be empty");
            messageText.requestFocus();
            validated = false;
        }
        return validated;
    }


    /**
     * Función que genera un objeto Message con los datos necesarios, obtenidos del usuario
     * @param: introdución del user
     * @return: objeto Message
     */
    public Message generateMessage(){
        Float latitud = sharedPref.getFloat("Lat",0);
        Float longitud = sharedPref.getFloat("Long",0);
        String text = messageText.getText().toString();
        String imageBase64 = photoBase64 ;
        String userSender = sharedPref.getString("user","");
        String userReceiver = messagePrivateUser.getText().toString();
        Date date = Calendar.getInstance().getTime();
        int life = 10;
        boolean msgPublic = messagePrivateUser.getText().toString().equals("") ? true : false ;
        boolean msgVisible = true;
        boolean msgReaded = false;

        return new Message(longitud,latitud,text,imageBase64,userSender, userReceiver, date, life, msgPublic, msgVisible, msgReaded);
    }
    /**
     * Función que genera un fichero y una vez creado correctamente inicializará la cámara enviandole
     * como parametro el Uri del fichero.
     */
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
    /**
     * Función que crea el fichero con un nombre único basado en unos parámetros predefinidos
     * @return: objeto File
     */
    private File createImageFile() throws IOException {
        //Creamos un nombre único para el fichero
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp;
        //File storageDir = getExternalFilesDir("TEMP");
        File storageDir = new File(dir + "/" + "geoecho");
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

    /**
     * Activity que gestionará el resultado de la cámara de fotos
     * @param: requestCode, resultCode, data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0: //Cámara
                if (resultCode == RESULT_OK) {
                    Uri photoUri = Uri.fromFile(photo);
                    Bitmap bitmap;
                    try {
                        //Vamos a buscar el fichero creado anteriormente para transformarlo en bitmap
                        //El fichero se comprimirá.
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        bitmap = BitmapFactory.decodeFile(photo.getPath(),bmOptions);
                        int alt = (int) (bitmap.getHeight() * 500 / bitmap.getWidth());
                        Bitmap mini = Bitmap.createScaledBitmap(bitmap, 500, alt, true);

                        photoBase64 = bitmapToBase64(mini);
                        photoAdded.setVisibility(View.VISIBLE);
                        bPhoto.setEnabled(false);
                        bPhoto.setAlpha(0.5f);
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

    /**
     * Función en segundo plano que enviará el mensaje generado al servidor para hacerlo público
     */

    public class placeMessage extends AsyncTask<Void, Void, Response> {

        ProgressDialog mDialog = new ProgressDialog(newMessage.this);
        Response result = null;

        @Override
        protected Response doInBackground(Void... params) {
            Response result = new Response();
            try {
                result=  messageToServer(mensaje);
            } catch (Exception e) {
                showAlert("Connection Error","Imposible to connect with server. Please try again");
            }
            return result;
        }

        @Override
        protected void onPostExecute(final Response result) {
            int status = result.getStatusQuery();
            mDialog.dismiss();

            /**
             * Si el servidor devuelve 11 quiere decir que el mensaje se ha guardado correctamente.
             * Una vez finalizado, se eliminará el fichero de la imagen
             */
            if (status == 11) {
                //Si ha ido bien
                setResult(RESULT_OK);
                if (photo != null)
                    deleteFile();
                finish();
            }
            else
                showAlert("Connection Error","Error placing the message. Please try again");
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
     * @return: devolverá un objeto Response para saber si se ha podido enviar correctamente
     */
    public Response messageToServer (Message data) throws Exception{
        //String serverUrl = "http://ec2-52-31-205-76.eu-west-1.compute.amazonaws.com/geoechoserv";
        String serverUrl = "https://ec2-52-31-205-76.eu-west-1.compute.amazonaws.com:8443/geoechoserv";
        Response result = new Response();
        URL url = new URL(serverUrl);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setSSLSocketFactory(CustomSSLSocketFactory.getSSLSocketFactory(newMessage.this));
        con.setHostnameVerifier(new AllowAllHostnameVerifier());
        data.setSessionID(sharedPref.getInt("session",0));

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
            result.setStatusQuery(Response.MESSAGE_FAILED);
        }

        return result;
    }

    /**
     * Función para eliminar un fichero
     * @param: Uri del archivo photo
     * @return: si se elimina correctamente no muestra nada
     */
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
