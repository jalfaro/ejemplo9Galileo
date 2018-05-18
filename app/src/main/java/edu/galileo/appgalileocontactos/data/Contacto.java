package edu.galileo.appgalileocontactos.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class Contacto {
    private String nombre;
    private String numero;
    private String imagenStream;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getImagenStream() {
        return imagenStream;
    }

    public void setImagenStream(String imagenStream) {
        this.imagenStream = imagenStream;
    }

    public Bitmap getBitmap() {
        Bitmap decodedByte = null;
        if (!imagenStream.equals("")) {
            byte[] decodedString = Base64.decode(imagenStream, Base64.DEFAULT);
            decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }
        return decodedByte;
    }

    public static List<Contacto> getListFromJSON(JSONArray array) {
        List<Contacto> lista = null;
        if (array != null) {
            if (array.length() > 0) {
                lista = new ArrayList<Contacto>();
                Contacto objeto;
                for (int i= 0; i < array.length(); i ++) {
                    objeto = null;
                    try {
                        objeto = new Contacto();
                        objeto.setNombre(array.getJSONObject(i).getString("nombre"));
                        objeto.setNumero(array.getJSONObject(i).getString("telefono"));
                        objeto.setImagenStream(array.getJSONObject(i).getString("imagen"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (objeto != null) {
                        lista.add(objeto);
                    }
                }
            }
        }
        return lista;
    }
}
