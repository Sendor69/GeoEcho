package papaya.geoecho;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageView extends AppCompatActivity {

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    String text, photo64;
    Bitmap imagen;
    ImageView imageView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_view);


        sharedPref = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        editor = sharedPref.edit();


        text = sharedPref.getString("textMessage","");
        photo64 = sharedPref.getString("photo64","");
        editor.remove("photo64");
        editor.commit();

        imageView = (ImageView) findViewById(R.id.imgMessage);
        textView = (TextView) findViewById(R.id.textMessage);

        //Comprobamos si tenemos imagen para este mensaje
        if (photo64.equals("EMPTY")){
            //La imageView pasara a gone
            imageView.setVisibility(View.GONE);
        }else{
            //Si la tenemos, la transformamos en bitmap y la asignamos al imageView
            imagen = base64ToBitmap(photo64);
            imageView.setImageBitmap(imagen);
        }
        textView.setText(text);

    }

    /**
     * Función para transformar un String base64 en un bitmap
     * @param: String base64
     * @return: bitmap object
     */
    public static Bitmap base64ToBitmap(String input)
    {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }
}
