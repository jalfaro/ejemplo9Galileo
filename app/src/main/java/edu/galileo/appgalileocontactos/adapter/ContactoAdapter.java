package edu.galileo.appgalileocontactos.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import edu.galileo.appgalileocontactos.R;
import edu.galileo.appgalileocontactos.data.Contacto;

public class ContactoAdapter extends ArrayAdapter<Contacto> {
    private Context context;
    private List<Contacto> list;
    public ContactoAdapter(@NonNull Context context, @NonNull List<Contacto> objects) {
        super(context, R.layout.row_contactos_layout, objects);
        this.context = context;
        this.list = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater lf =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = lf.inflate(R.layout.row_contactos_layout, null);
        }
        if (list.get(position) != null) {
            TextView nombre = v.findViewById(R.id.nombre_contacto);
            TextView telefono = v.findViewById(R.id.telefono_contacto);
            ImageView imagen = v.findViewById(R.id.imagen_contacto);
            nombre.setText(list.get(position).getNombre());
            telefono.setText(list.get(position).getNumero());
            imagen.setImageBitmap(list.get(position).getBitmap());
        }
        return v;
    }
}
