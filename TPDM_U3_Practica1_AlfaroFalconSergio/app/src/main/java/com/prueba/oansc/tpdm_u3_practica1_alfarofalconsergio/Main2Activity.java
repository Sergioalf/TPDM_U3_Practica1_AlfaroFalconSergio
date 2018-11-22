package com.prueba.oansc.tpdm_u3_practica1_alfarofalconsergio;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main2Activity extends AppCompatActivity {

    EditText mesa, cantidadP, cantidadB;
    Spinner listaPlatillos, listaBebidas;
    Button agregarP, agregarB, guardar;
    ListView resumen;
    DatabaseReference baseDeDatos;
    List<Map> platillos;
    List<Map> bebidas;
    List<String> comanda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        mesa = findViewById(R.id.noMesaLevantar);
        cantidadP = findViewById(R.id.cantidadPlatillo);
        cantidadB = findViewById(R.id.cantidadBebida);
        listaPlatillos = findViewById(R.id.listaPlatillos);
        listaBebidas = findViewById(R.id.listaBebidas);
        agregarP = findViewById(R.id.agregarPlatillos);
        agregarB = findViewById(R.id.agregarBebidas);
        guardar = findViewById(R.id.guardarComanda);
        resumen = findViewById(R.id.listaComanda);
        baseDeDatos = FirebaseDatabase.getInstance().getReference();
        comanda = new ArrayList<>();
        bebidas = new ArrayList<>();

        cargarSpinners();

        agregarP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validoPlatillo()) {
                    comanda.add("p-" + cantidadP.getText().toString() + "-" + platillos.get(listaPlatillos.getSelectedItemPosition()).get("nombre") + "-" + (Integer.parseInt(platillos.get(listaPlatillos.getSelectedItemPosition()).get("precio").toString()) * Integer.parseInt(cantidadP.getText().toString())));
                    establecerPedidos();
                    cantidadP.setText("");
                }
            }
        });

        agregarB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validoBebida()) {
                    comanda.add("b-" + cantidadB.getText().toString() + "-" + bebidas.get(listaBebidas.getSelectedItemPosition()).get("nombre") + "-" + (Integer.parseInt(bebidas.get(listaBebidas.getSelectedItemPosition()).get("precio").toString()) * Integer.parseInt(cantidadB.getText().toString())));
                    establecerPedidos();
                    cantidadB.setText("");
                }
            }
        });

        resumen.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder mensaje = new AlertDialog.Builder(Main2Activity.this);
                mensaje.setTitle("Advertencia").setMessage("¿Deseas eliminar este pedido?").setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        comanda.remove(position);
                        establecerPedidos();
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

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (valido()) {
                    Map<String, Object> nuevaComanda = new HashMap<>();

                    nuevaComanda.put("fecha", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                    nuevaComanda.put("estatus", "por pagar");
                    nuevaComanda.put("platillos", obtenerProductos("p"));
                    nuevaComanda.put("bebidas", obtenerProductos("b"));
                    nuevaComanda.put("total", obtenerTotal() + "");
                    nuevaComanda.put("nomesa", mesa.getText().toString());

                    baseDeDatos.child("comanda").push().setValue(nuevaComanda);
                    Toast.makeText(Main2Activity.this, "Se agregó correctamente", Toast.LENGTH_LONG).show();
                    limpiarCampos();
                }
            }
        });
    }

    private void limpiarCampos () {
        mesa.setText("");
        cantidadP.setText("");
        cantidadB.setText("");
        listaBebidas.setSelection(0);
        listaPlatillos.setSelection(0);
        comanda.clear();
        establecerPedidos();
    }

    private String obtenerProductos (String tipo) {
        String res = "";
        for (int i = 0, n = 0; i < comanda.size(); i++) {
            String ar[] = comanda.get(i).toString().split("-");
            if (ar[0].equals(tipo)) {
                if (n == 0) {
                    res += ar[1] + "-" + ar[2] + "-" + ar[3];
                } else {
                    res += "&" + ar[1] + "-" + ar[2] + "-" + ar[3];
                }
                n++;
            }
        }
        return res;
    }

    private int obtenerTotal () {
        int res = 0;
        for (int i = 0; i < comanda.size(); i++) {
            String[] ar = comanda.get(i).toString().split("-");
            res += Integer.parseInt(ar[3]);
        }
        return res;
    }

    private void establecerPedidos () {
        String[] cad = new String[comanda.size()];
        for (int i = 0; i < cad.length; i++) {
            String[] ar = comanda.get(i).toString().split("-");
            cad[i] = ar[1] + " - " + ar[2];
        }
        ArrayAdapter<String> molde = new ArrayAdapter(Main2Activity.this, android.R.layout.simple_list_item_1, cad);
        resumen.setAdapter(molde);
    }

    private void cargarSpinners () {
        baseDeDatos.child("platillo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() != 0){
                    platillos = new ArrayList<>();
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
                                    platillos.add(elemento);
                                    cargarLista();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        baseDeDatos.child("bebida").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() != 0){
                    bebidas = new ArrayList<>();
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
                                    bebidas.add(elemento);
                                    cargarLista();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void cargarLista () {
        String[] lista = new String[platillos.size()];
        for (int i = 0; i < lista.length; i++) {
            lista[i] = platillos.get(i).get("nombre").toString();
        }
        ArrayAdapter<String> molde = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lista);
        listaPlatillos.setAdapter(molde);
        lista = new String[bebidas.size()];
        for (int i = 0; i < lista.length; i++) {
            lista[i] = bebidas.get(i).get("nombre").toString();
        }
        molde = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lista);
        listaBebidas.setAdapter(molde);
    }

    private boolean valido () {
        if (mesa.getText().toString().equals("")) {
            Toast.makeText(Main2Activity.this, "Escribe el número de mesa", Toast.LENGTH_LONG).show();
            return false;
        }
        try {
            int a = Integer.parseInt(mesa.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(Main2Activity.this, "Solo número para la mesa", Toast.LENGTH_LONG).show();
            mesa.setText("");
            return false;
        }
        if (comanda.size() < 1) {
            Toast.makeText(Main2Activity.this, "Introduce al menos una bebida o platillo", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private boolean validoPlatillo () {
        if (cantidadP.getText().toString().equals("")) {
            Toast.makeText(Main2Activity.this, "Escribe una cantidad de platillos", Toast.LENGTH_LONG).show();
            return false;
        }
        try {
            int a = Integer.parseInt(cantidadP.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(Main2Activity.this, "Solo número para la cantidad de platillos", Toast.LENGTH_LONG).show();
            cantidadP.setText("");
            return false;
        }
        return true;
    }

    private boolean validoBebida () {
        if (cantidadB.getText().toString().equals("")) {
            Toast.makeText(Main2Activity.this, "Escribe una cantidad de bebidas", Toast.LENGTH_LONG).show();
            return false;
        }
        try {
            int a = Integer.parseInt(cantidadB.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(Main2Activity.this, "Solo número para la cantidad de bebidas", Toast.LENGTH_LONG).show();
            cantidadB.setText("");
            return false;
        }
        return true;
    }

}
