package com.prueba.oansc.tpdm_u3_practica1_alfarofalconsergio;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ListView menuPrincipal;
    DatabaseReference baseDeDatos;
    List<Map> platillosLocal;
    List<Map> bebidasLocal;
    List<Map> comandas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        menuPrincipal = findViewById(R.id.menuPrincipal);
        String[] items = {"Levantar pedido", "Cobrar", "Exportar a CSV", "Salir"};
        ArrayAdapter<String> contenido = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, items);
        menuPrincipal.setAdapter(contenido);
        baseDeDatos = FirebaseDatabase.getInstance().getReference();

        baseDeDatos.child("platillo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0){
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

        baseDeDatos.child("bebida").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0){
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

        baseDeDatos.child("comanda").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() == 0) {
                    return;
                }
                comandas = new ArrayList<>();
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

        menuPrincipal.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    siguienteVentana(Main2Activity.class);
                }
                if (position == 1) {
                    siguienteVentana(Main3Activity.class);
                }
                if (position == 2) {
                    formato();
                }
                if (position == 3) {
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.platillos) {
            siguienteVentana(Main4Activity.class);
        }
        if (id == R.id.bebidas) {
            siguienteVentana(Main5Activity.class);
        }

        return super.onOptionsItemSelected(item);
    }

    private void siguienteVentana(Class siguienteActivity) {
        Intent siguiente = new Intent(MainActivity.this, siguienteActivity);
        startActivity(siguiente);
    }

    private void formato () {
        String platillos = "";
        String bebidas = "";
        String comanda = "";
        for (int i = 0; i < platillosLocal.size(); i++) {
            Map<String, Object> e = platillosLocal.get(i);
            platillos += e.get("idP") + "," + e.get("nombre") + "," + e.get("precio") + "\n";
        }
        for (int i = 0; i < bebidasLocal.size(); i++) {
            Map<String, Object> e = bebidasLocal.get(i);
            bebidas += e.get("idB") + "," + e.get("nombre") + "," + e.get("precio") + "\n";
        }
        for (int i = 0; i < comandas.size(); i++) {
            Map<String, Object> e = comandas.get(i);
            comanda += e.get("idComanda") + "," + e.get("fecha") + "," + e.get("platillos") + "," + e.get("bebidas") + "," + e.get("estatus") + "," + e.get("total") + "," + e.get("nomesa" + "\n");
        }
        try {
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput("platillos.csv",MODE_PRIVATE));
            archivo.write(platillos);
            archivo.close();
            OutputStreamWriter archivo2 = new OutputStreamWriter(openFileOutput("bebidas.csv",MODE_PRIVATE));
            archivo2.write(bebidas);
            archivo2.close();
            OutputStreamWriter archivo3 = new OutputStreamWriter(openFileOutput("comanda.csv",MODE_PRIVATE));
            archivo3.write(comanda);
            archivo3.close();
            Toast.makeText(MainActivity.this, "Se guardó correctamente", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Error al guardar", Toast.LENGTH_LONG).show();

        }
    }

}