package com.prueba.oansc.tpdm_u3_practica1_alfarofalconsergio;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

public class Main5Activity extends AppCompatActivity {

    EditText nombre, precio;
    Button agregar, actualizar, borrar;
    ListView listaBebida;
    DatabaseReference baseDeDatos;
    List<Map> bebidasLocal;
    boolean actualizando;
    int bebidaActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);

        nombre = findViewById(R.id.nombreBebida);
        precio = findViewById(R.id.precioBebida);
        agregar = findViewById(R.id.agregarBebida);
        actualizar = findViewById(R.id.actualizarBebida);
        borrar= findViewById(R.id.borrarBebida);
        listaBebida = findViewById(R.id.listaBebida);
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
                        baseDeDatos.child("bebida").push().setValue(obtenerBebida());
                        Toast.makeText(Main5Activity.this, "Se agregó correctamente", Toast.LENGTH_LONG).show();
                        limpiarCampos();
                    }
                }
            }
        });

        actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mensaje = new AlertDialog.Builder(Main5Activity.this);
                mensaje.setTitle("Advertencia").setMessage("¿Seguro que deseas actualizar la bebida?").setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (validado()) {
                            baseDeDatos.child("bebida").child(bebidasLocal.get(bebidaActual).get("idB").toString()).setValue(obtenerBebida());
                            Toast.makeText(Main5Activity.this, "Se modificó la bebida", Toast.LENGTH_LONG).show();
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
                AlertDialog.Builder mensaje = new AlertDialog.Builder(Main5Activity.this);
                mensaje.setTitle("Advertencia").setMessage("¿Seguro que deseas borrar la bebida?").setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        baseDeDatos.child("bebida").child(bebidasLocal.get(bebidaActual).get("idB").toString()).removeValue();
                        Toast.makeText(Main5Activity.this, "Se eliminó la bebida", Toast.LENGTH_LONG).show();
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

        listaBebida.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (bebidasLocal.size() < 1) {
                    return;
                }
                bebidaActual = position;
                cambioEstado(true);
                nombre.setText(bebidasLocal.get(position).get("nombre").toString());
                precio.setText(bebidasLocal.get(position).get("precio").toString());
            }
        });

        baseDeDatos.child("bebida").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0){
                    Toast.makeText(Main5Activity.this, "No hay bebidas", Toast.LENGTH_LONG).show();
                    return;
                }
                bebidasLocal = new ArrayList<>();
                for (final DataSnapshot temporal : dataSnapshot.getChildren()) {
                    baseDeDatos.child("bebida").child(temporal.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Bebida bebida = dataSnapshot.getValue(Bebida.class);
                            if (bebida != null) {
                                Map<String, Object> elemento = new HashMap<>();
                                elemento.put("idB", temporal.getKey());
                                elemento.put("nombre", bebida.getNombre());
                                elemento.put("precio", bebida.getPrecio());
                                bebidasLocal.add(elemento);
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

    private void cambiarBotones (boolean noSeleccionado) {
        actualizar.setEnabled(noSeleccionado);
        borrar.setEnabled(noSeleccionado);
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

    private Map<String, Object> obtenerBebida() {
        Map<String, Object> nuevaBebida = new HashMap<>();

        nuevaBebida.put("nombre", nombre.getText().toString());
        nuevaBebida.put("precio", precio.getText().toString());

        return nuevaBebida;
    }

    private void limpiarCampos () {
        nombre.setText("");
        precio.setText("");
    }

    private void cargarLista () {
        String[] lista = new String[bebidasLocal.size()];
        for (int i = 0; i < lista.length; i++) {
            lista[i] = bebidasLocal.get(i).get("nombre").toString();
        }
        ArrayAdapter<String> molde = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lista);
        listaBebida.setAdapter(molde);
    }

    private boolean validado () {
        if (nombre.getText().toString().equals("")) {
            Toast.makeText(Main5Activity.this, "Escribe un nombre para la bebida", Toast.LENGTH_LONG).show();
            return false;
        }
        if (precio.getText().toString().equals("")) {
            Toast.makeText(Main5Activity.this, "Escribe un precio para la bebida", Toast.LENGTH_LONG).show();
            return false;
        }
        try {
            int a = Integer.parseInt(precio.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(Main5Activity.this, "Solo puedes introducir números en el precio", Toast.LENGTH_LONG).show();
            precio.setText("");
            return false;
        }
        return true;
    }

}
