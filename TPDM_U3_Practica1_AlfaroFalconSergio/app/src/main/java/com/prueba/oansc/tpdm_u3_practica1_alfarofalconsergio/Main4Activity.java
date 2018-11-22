package com.prueba.oansc.tpdm_u3_practica1_alfarofalconsergio;

import android.app.AlertDialog;
import android.app.MediaRouteActionProvider;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main4Activity extends AppCompatActivity {

    EditText nombre, precio;
    Button agregar, actualizar, borrar;
    ListView listaPlatillo;
    DatabaseReference baseDeDatos;
    List<Map> platillosLocal;
    boolean actualizando;
    int platilloActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        nombre = findViewById(R.id.nombrePlatillo);
        precio = findViewById(R.id.precioPlatillo);
        agregar = findViewById(R.id.agregarPlatillo);
        actualizar = findViewById(R.id.actualizarPlatillo);
        borrar= findViewById(R.id.borrarPlatillo);
        listaPlatillo = findViewById(R.id.listaPlatillo);
        baseDeDatos = FirebaseDatabase.getInstance().getReference();
        actualizando = false;

        cambiarBotones(false);

        agregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actualizando) {
                    cambioEstado(false);
                    limpiarCampos();
                } else {
                    if (validado()) {
                        baseDeDatos.child("platillo").push().setValue(obtenerPlatillo());
                        Toast.makeText(Main4Activity.this, "Se agregó correctamente", Toast.LENGTH_LONG).show();
                        limpiarCampos();
                    }
                }
            }
        });

        actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mensaje = new AlertDialog.Builder(Main4Activity.this);
                mensaje.setTitle("Advertencia").setMessage("¿Seguro que deseas actualizar el platillo?").setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (validado()) {
                            baseDeDatos.child("platillo").child(platillosLocal.get(platilloActual).get("idP").toString()).setValue(obtenerPlatillo());
                            Toast.makeText(Main4Activity.this, "Se modificó el platillo", Toast.LENGTH_LONG).show();
                            cambioEstado(false);
                            limpiarCampos();
                            dialog.dismiss();
                        }
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        });

        borrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mensaje = new AlertDialog.Builder(Main4Activity.this);
                mensaje.setTitle("Advertencia").setMessage("¿Seguro que deseas borrar el platillo?").setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        baseDeDatos.child("platillo").child(platillosLocal.get(platilloActual).get("idP").toString()).removeValue();
                        Toast.makeText(Main4Activity.this, "Se eliminó el platillo", Toast.LENGTH_LONG).show();
                        cambioEstado(false);
                        limpiarCampos();
                        dialog.dismiss();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        });

        listaPlatillo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (platillosLocal.size() < 1) {
                    return;
                }
                platilloActual = position;
                cambioEstado(true);
                nombre.setText(platillosLocal.get(position).get("nombre").toString());
                precio.setText(platillosLocal.get(position).get("precio").toString());
            }
        });

        baseDeDatos.child("platillo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0){
                    Toast.makeText(Main4Activity.this, "No hay platillos", Toast.LENGTH_LONG).show();
                    return;
                }
                platillosLocal = new ArrayList<>();
                for (final DataSnapshot temporal : dataSnapshot.getChildren()) {
                    baseDeDatos.child("platillo").child(temporal.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Platillo platillo = dataSnapshot.getValue(Platillo.class);
                            if (platillo != null) {
                                Map<String, Object> elemento = new HashMap<>();
                                elemento.put("idP", temporal.getKey());
                                elemento.put("nombre", platillo.getNombre());
                                elemento.put("precio", platillo.getPrecio());
                                platillosLocal.add(elemento);
                                cargarLista();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private Map<String, Object> obtenerPlatillo() {
        Map<String, Object> nuevoPlatillo = new HashMap<>();

        nuevoPlatillo.put("nombre", nombre.getText().toString());
        nuevoPlatillo.put("precio", precio.getText().toString());

        return nuevoPlatillo;
    }

    private void cargarLista () {
        String[] lista = new String[platillosLocal.size()];
        for (int i = 0; i < lista.length; i++) {
            lista[i] = platillosLocal.get(i).get("nombre").toString();
        }
        ArrayAdapter<String> molde = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lista);
        listaPlatillo.setAdapter(molde);
    }

    private void cambiarBotones (boolean noSeleccionado) {
        actualizar.setEnabled(noSeleccionado);
        borrar.setEnabled(noSeleccionado);
    }

    private void limpiarCampos () {
        nombre.setText("");
        precio.setText("");
    }

    private void cambioEstado(boolean cambio) {
        cambiarBotones(cambio);
        actualizando = cambio;
        if (cambio) {
            agregar.setText("Cancelar");
        } else {
            agregar.setText("Agregar");
        }
    }

    private boolean validado () {
        if (nombre.getText().toString().equals("")) {
            Toast.makeText(Main4Activity.this, "Escribe un nombre para el platillo", Toast.LENGTH_LONG).show();
            return false;
        }
        if (precio.getText().toString().equals("")) {
            Toast.makeText(Main4Activity.this, "Escribe un precio para el platillo", Toast.LENGTH_LONG).show();
            return false;
        }
        try {
            int a = Integer.parseInt(precio.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(Main4Activity.this, "Solo puedes introducir números en el precio", Toast.LENGTH_LONG).show();
            precio.setText("");
            return false;
        }
        return true;
    }

}
