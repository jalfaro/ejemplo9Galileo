package edu.galileo.appgalileocontactos;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

import edu.galileo.appgalileocontactos.adapter.ContactoAdapter;
import edu.galileo.appgalileocontactos.data.Contacto;

public class ConsultaActivity extends AppCompatActivity {
    private List<Contacto> lista;
    private ListView contactoList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consulta_layout);
        contactoList = findViewById(R.id.lstContactos);
        StringRequest request = new StringRequest(Request.Method.GET, "http://186.151.140.61/galileo/contacto",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray json = null;
                        try {
                            json = new JSONArray(response);
                            lista = Contacto.getListFromJSON(json);
                            contactoList.setAdapter(new ContactoAdapter(ConsultaActivity.this, lista));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        ((ContactoApp)getApplication()).getQueue().add(request);
    }

}
