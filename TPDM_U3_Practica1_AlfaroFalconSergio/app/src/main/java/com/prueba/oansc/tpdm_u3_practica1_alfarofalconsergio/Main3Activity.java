package com.prueba.oansc.tpdm_u3_practica1_alfarofalconsergio;

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

import com.google.android.gms.common.SignInButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main3Activity extends AppCompatActivity {

    EditText mesa, total;
    Button buscar, pagado;
    ListView listaComandas;
    DatabaseReference baseDeDatos;
    List<Map> comandas;
    int comandaActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        mesa = findViewById(R.id.noMesaCobrar);
        total = findViewById(R.id.total);
        buscar = findViewById(R.id.buscar);
        pagado = findViewById(R.id.pagado);
        listaComandas = findViewById(R.id.listaComandaDos);
        baseDeDatos = FirebaseDatabase.getInstance().getReference();
        comandas = new ArrayList<>();

        pagado.setEnabled(false);

        pagado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> e = new HashMap<>();
                e.put("estatus", "pagado");
                baseDeDatos.child("comanda").child(comandas.get(comandaActual).get("idComanda").toString()).updateChildren(e);
                mesa.setText("");
                total.setText("");
                String[] a = new String[0];
                ArrayAdapter<String> x = new ArrayAdapter<>(Main3Activity.this, android.R.layout.simple_list_item_1, a);
                listaComandas.setAdapter(x);
                pagado.setEnabled(false);
                Toast.makeText(Main3Activity.this, "Se ha pagado correctamente", Toast.LENGTH_LONG).show();
            }
        });

        buscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (valido()) {
                    int no = Integer.parseInt(mesa.getText().toString());
                    for (int i = 0; i < comandas.size(); i++) {
                        Map<String, Object> act = comandas.get(i);
                        if (act.get("estatus").toString().equals("por pagar") && Integer.parseInt(act.get("nomesa").toString()) == no) {
                            comandaActual = i;
                            // listaComandas.setEnabled(false);
                            llenarComanda();
                            total.setText("$" + comandas.get(i).get("total"));
                            pagado.setEnabled(true);
                            return;
                        }
                    }
                }
                Toast.makeText(Main3Activity.this, "No se encontraron comandas por pagar de esa mesa", Toast.LENGTH_LONG).show();
            }
        });

        listaComandas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });

        baseDeDatos.child("comanda").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() == 0) {
                    Toast.makeText(Main3Activity.this, "No hay comandas", Toast.LENGTH_LONG).show();
                    return;
                }
                for (final DataSnapshot temporal : dataSnapshot.getChildren()) {
                    baseDeDatos.child("comanda").child(temporal.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Comanda comanda = dataSnapshot.getValue(Comanda.class);
                            if (comanda != null) {
                                Map<String, Object> elemento = new HashMap<>();
                                elemento.put("idComanda", temporal.getKey());
                                elemento.put("fecha", comanda.getFecha());
                                elemento.put("platillos", comanda.getPlatillos());
                                elemento.put("bebidas", comanda.getBebidas());
                                elemento.put("estatus", comanda.getEstatus());
                                elemento.put("total", comanda.getTotal());
                                elemento.put("nomesa", comanda.getNomesa());
                                comandas.add(elemento);
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

    private void llenarComanda() {
        Map<String, Object> comanda = comandas.get(comandaActual);
        String[] platillos = comanda.get("platillos").toString().split("&");
        String[] bebidas = comanda.get("bebidas").toString().split("&");
        List<String> estructura = new ArrayList<>();
        estructura.add("Fecha: " + comanda.get("fecha"));
        estructura.add("No. Mesa: " + comanda.get("nomesa"));
        estructura.add("Estatus: " + comanda.get("estatus"));
        estructura.add("Platillos:");
        int i =0;
        for (i = 0; i < platillos.length; i++) {
            String[] ar = platillos[i].split("-");
            estructura.add("   " + ar[0] + " - " + ar[1] + "  $" + ar[2]);
        }
        estructura.add("Bebidas:");
        for (int j = 0; j < bebidas.length; j++) {
            String[] ar = bebidas[j].split("-");
            estructura.add("   " + ar[0] + " - " + ar[1] + "  $" + ar[2]);
        }
        ArrayAdapter<String> molde = new ArrayAdapter(Main3Activity.this, android.R.layout.simple_list_item_1, estructura);
        listaComandas.setAdapter(molde);
    }

    private boolean valido () {
        if (mesa.getText().toString().equals("")){
            Toast.makeText(Main3Activity.this, "Introduce el número de mesa", Toast.LENGTH_LONG).show();
            return false;
        }
        try {
            int a = Integer.parseInt(mesa.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(Main3Activity.this, "Solo números para la mesa", Toast.LENGTH_LONG).show();
            mesa.setText("");
            return false;
        }
        return true;
    }

}
