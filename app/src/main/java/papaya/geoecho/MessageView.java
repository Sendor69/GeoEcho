package papaya.geoecho;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import model.client.Message;

public class MessageView extends AppCompatActivity {

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    Message message;
    Bitmap imagen;
    ImageView imageView;
    TextView textMessage, dataMessage, userMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_view);


        sharedPref = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        Intent i = getIntent();
        message = (Message) getIntent().getSerializableExtra("Message");


        imageView = (ImageView) findViewById(R.id.imgMessage);
        textMessage = (TextView) findViewById(R.id.textMessage);
        dataMessage = (TextView) findViewById(R.id.dataMessage);
        userMessage = (TextView) findViewById(R.id.userMessage);

        //Comprobamos si tenemos imagen para este mensaje
        if (message.getPhotoBase64() == null){
            //La imageView pasara a gone
            imageView.setVisibility(View.GONE);
        }else{
            //Si la tenemos, la transformamos en bitmap y la asignamos al imageView
            imagen = base64ToBitmap(message.getPhotoBase64());
            imageView.setImageBitmap(imagen);
        }
        textMessage.setText(message.getText());
        userMessage.setText("User: " + message.getUserSender());
        dataMessage.setText("Data: " + message.getDate().toString());

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
