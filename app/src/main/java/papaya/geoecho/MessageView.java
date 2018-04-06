package papaya.geoecho;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageView extends AppCompatActivity {

    String text, photo64;
    Bitmap imagen;
    ImageView imageView;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_view);

        Bundle extras = getIntent().getExtras();
        text = extras.getString("text");
        photo64 = extras.getString("photo64");

        imageView = (ImageView) findViewById(R.id.imgMessage);
        textView = (TextView) findViewById(R.id.textMessage);

        if (photo64.equals("EMPTY")){
            //La imageView pasara a gone
            imageView.setVisibility(View.GONE);
        }else{
            imagen = base64ToBitmap(photo64);
            imageView.setImageBitmap(imagen);
        }
        textView.setText(text);

    }











    public static Bitmap base64ToBitmap(String input)
    {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }
}
