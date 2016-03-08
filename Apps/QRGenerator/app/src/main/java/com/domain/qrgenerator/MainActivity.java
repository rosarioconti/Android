package com.domain.qrgenerator;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.Dimension;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Intents;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener {

    private static ImageView iv;
    private static Context context;
    private Spinner items,quantities;
    private int width = 320, height = 240;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        items = (Spinner) findViewById(R.id.items);
        quantities = (Spinner) findViewById(R.id.quantities);


        List<String> list = new ArrayList<String>();
        list.add("item 1");
        list.add("item 2");
        list.add("item 3");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        items.setAdapter(dataAdapter);
        items.setOnItemSelectedListener(this);
        iv = (ImageView) findViewById(R.id.image_view);
    }
    public static Bitmap toBitmap(BitMatrix matrix){
        int height = matrix.getHeight();
        int width = matrix.getWidth();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565); //1byte //faster way
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                bmp.setPixel(x, y, matrix.get(x,y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
    }

    public static Image createQRCodeImage(String toEncode, int w, int h) {

        QRCodeWriter qrWrite = new QRCodeWriter();
        try {
            BitMatrix matrix = qrWrite.encode(toEncode, BarcodeFormat.QR_CODE, w, h);
            Bitmap bmp = toBitmap(matrix);
            iv.setImageBitmap(bmp);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Toast.makeText(context,
                "OnItemSelectedListener : " + items.getItemAtPosition(i).toString(),
                Toast.LENGTH_SHORT).show();
        createQRCodeImage(items.getItemAtPosition(i).toString() + "01234567890123456789012345678901234567890123456789",width,height);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
