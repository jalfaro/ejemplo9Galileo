package edu.galileo.appgalileocontactos;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText txtNombre, txtTelefono;
    private ImageView imgFoto;
    private Button btnGrabar, btnConsultar;
    private Uri imageUri;
    private Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        //Estas lineas son importantes para evitar que apartir de telefonos con la version 24 de algun problema por que el URI expone el path completo de la foto, caso contrario hay que usar un FileProvider
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        txtNombre = findViewById(R.id.txtNombre);
        txtTelefono = findViewById(R.id.txtTelefono);
        imgFoto = findViewById(R.id.imgFoto);
        btnGrabar = findViewById(R.id.btnGuardar);
        btnConsultar = findViewById(R.id.btnConsultar);
        imgFoto.setOnClickListener(this);
        btnGrabar.setOnClickListener(this);
        btnConsultar.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0]== PackageManager.PERMISSION_GRANTED && grantResults[1]== PackageManager.PERMISSION_GRANTED && requestCode == 1) {
            takePicture();
        } else {
            Toast.makeText(this, "No se otorgo permiso para tomar fotos", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            image = BitmapFactory.decodeFile(imageUri.getPath());
            imgFoto.setImageBitmap(image);
        }

    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnGuardar) {
            StringRequest  request = new StringRequest(Request.Method.POST, "http://186.151.140.61/galileo/contacto", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    JSONObject json = null;
                    try {
                        json = new JSONObject(response);
                        Toast.makeText(MainActivity.this, json.getString("mensaje"), Toast.LENGTH_LONG).show();
                        txtNombre.setText("");
                        txtTelefono.setText("");
                        imgFoto.setImageResource(android.R.drawable.ic_media_play);
                    } catch (JSONException e) {
                        e.printStackTrace();

                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    String error1 = error.getMessage();
                }
            }) {
                @Override
                public byte[] getBody() throws AuthFailureError {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("nombre", txtNombre.getText().toString());
                        json.put("telefono", txtTelefono.getText().toString());
                        json.put("imagen", encodeFileImage());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String httpPostBody = json.toString();

                    // usually you'd have a field with some values you'd want to escape, you need to do it yourself if overriding getBody. here's how you do it
                    return httpPostBody.getBytes();
                }
            };
            ((ContactoApp) getApplication()).getQueue().add(request);

        } else if (v.getId() == R.id.imgFoto) {
            isStoragePermissionGranted();
        } else if (v.getId() == R.id.btnConsultar) {
            Intent intent = new Intent(this, ConsultaActivity.class);
            startActivity(intent);
        }
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if ((checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) && (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
                takePicture();
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("Test","Permission is granted");
            takePicture();
            return true;
        }
    }

    public void takePicture () {
        imageUri = getOutputMediaFileUri();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 101);
        }
    }

    private Uri getOutputMediaFileUri()
    {
        //Valida si tenemos posibilidad de escribir en la storage
        if(isExternalStorageAvaiable())
        {
            String mediaStorageDir = Environment.getExternalStorageDirectory() + File.separator + "galileo_contactos";
            File mediaStorageDirFile = new File(mediaStorageDir);
            if (!mediaStorageDirFile.exists()) {
                mediaStorageDirFile.mkdir();
            }

            String fileName = "";
            String fileType = "";
            String timeStamp = Calendar.getInstance().getTimeInMillis() + "";

            fileName = "IMG_"+timeStamp;
            fileType = ".jpg";

            File mediaFile;
            try
            {
                mediaFile = File.createTempFile(fileName,fileType,mediaStorageDirFile);
                Log.i("st","File: "+Uri.fromFile(mediaFile));
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Log.i("St","Error creating file: " + mediaStorageDir +fileName +fileType);
                return null;
            }
            return Uri.fromFile(mediaFile);
        }
        return null;

    }

    private boolean isExternalStorageAvaiable()
    {
        String state = Environment.getExternalStorageState();

        if(Environment.MEDIA_MOUNTED.equals(state))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //Codificar archivo en base64 para almacenar en la foto
    private String encodeFileImage() {//String filePath){
        Bitmap bm = null;//BitmapFactory.decodeFile(filePath);
        bm = getResizedBitmap(image, 200);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        // Recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bm,  (int)(newHeight * ((float)width/height)), newHeight, false);
        return resizedBitmap;
    }
}
