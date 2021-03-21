package com.example.idioma;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Test extends AppCompatActivity {
    Button btnVolverT, btnComenzar, btnTerminar;
    //guarda el castellano
    TextView quest1, quest2, quest3, quest4, quest5;
    //guardar las respuestas
    EditText respues1, respues2, respues3, respues4, respues5;

    //idioma seleccionado, ejemplo ingles
    String idiomaselect;
    //id del idioma seleccionado, ejemplo ingles = 1
    String IDselect;

    BuscarPalabra acceso;

    int posicion;
    ArrayAdapter<String> adapter;
    //modificar en caso de que cambie la ip del equipo
    String ip = "192.168.1.106";
    //cambiar entre documentos de php
    String segun = "palabras.php";
    //campos de la bd
    String idtabla1 = "idIdiomas";
    String palabracaste = "palabraCastellano";
    String palabraoriginal = "palabraOriginal";

    String res1, res2, res3, res4, res5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        TextView idioma = findViewById(R.id.txtIdiomaSeleccionadoT);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        idiomaselect = bundle.getString("idiomaelegido");
        IDselect = bundle.getString("numidioma");
        // Separamos la cadena, el id y el nombre
        idioma.setText("Idioma " + "-" + idiomaselect + "-");

        //guardar el castellano
        quest1 = findViewById(R.id.quest1);
        quest2 = findViewById(R.id.quest2);
        quest3 = findViewById(R.id.quest3);
        quest4 = findViewById(R.id.quest4);
        quest5 = findViewById(R.id.quest5);
        //guardar el idioma nuevo
        respues1 = findViewById(R.id.respues1);
        respues2 = findViewById(R.id.respues2);
        respues3 = findViewById(R.id.respues3);
        respues4 = findViewById(R.id.respues4);
        respues5 = findViewById(R.id.respues5);
        //botones
        btnVolverT = findViewById(R.id.btnVolverT);
        btnVolverT.setOnClickListener(v -> onBackPressed());
        btnComenzar = findViewById(R.id.btncomenzar);
        btnTerminar = findViewById(R.id.btnTerminar);
        //POner las palabras
        btnComenzar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceso = new BuscarPalabra(IDselect);
                acceso.execute();


            }
        });

        //Comprobar la solucion
        btnTerminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int nota = 0;
                String respu1 = res1.toString();
                String respu2 = res2.toString();
                String respu3 = res3.toString();
                String respu4 = res4.toString();
                String respu5 = res5.toString();
                //sumas
                if (respu1.equals(respues1.getText().toString())) {
                    nota = nota + 1;
                }
                if (respu2.equals(respues2.getText().toString())) {
                    nota = nota + 1;
                }
                if (respu3.equals(respues3.getText().toString())) {
                    nota = nota + 1;
                }
                if (respu4.equals(respues4.getText().toString())) {
                    nota = nota + 1;
                }
                if (respu5.equals(respues5.getText().toString())) {
                    nota = nota + 1;
                }

                Toast.makeText(Test.this, "La nota final es: " + nota+"/5",
                        Toast.LENGTH_SHORT).show();

            }
        });

    }

    /* Toast.makeText(Test.this, "La palabra del primer campo es " + quest5.getText()
               + " y su solucion es " + res5 + " y usted puso> " + respues5.getText(),
       Toast.LENGTH_SHORT).show();
      */
    // Consulta idiomas
    class BuscarPalabra extends AsyncTask<Void, Void, String> {
        // Atributos
        String diIdiomaFK;
        //guardar palabra español
        String palabraESP1 = "";
        String palabraESP2 = "";
        String palabraESP3 = "";
        String palabraESP4 = "";
        String palabraESP5 = "";
        JSONArray result;
        JSONObject jsonobject;

        // Constructor
        public BuscarPalabra(String id) {
            this.diIdiomaFK = id;

        }

        // Inspectores
        protected void onPreExecute() {

        }

        protected String doInBackground(Void... argumentos) {
            try {
                // Crear la URL de conexión al API
                Uri uri = new Uri.Builder().scheme("http").authority(ip).path("/ApiRest/" + segun).appendQueryParameter(idtabla1, this.diIdiomaFK).build();
                // Create connection
                URL url = new URL(uri.toString());
                // Crear la conexión HTTP
                HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();
                // Establecer método de comunicación. Por defecto GET.
                myConnection.setRequestMethod("GET");
                if (myConnection.getResponseCode() == 200) {
                    // Conexión exitosa
                    // Creamos Stream para la lectura de datos desde el servidor
                    InputStream responseBody = myConnection.getInputStream();
                    InputStreamReader responseBodyReader = new InputStreamReader(responseBody, StandardCharsets.UTF_8);
                    // Creamos Buffer de lectura
                    BufferedReader bR = new BufferedReader(responseBodyReader);
                    String line;
                    StringBuilder responseStrBuilder = new StringBuilder();
                    // Leemos el flujo de entrada
                    while ((line = bR.readLine()) != null) {
                        responseStrBuilder.append(line);
                    }
                    // Parseamos respuesta en formato JSON
                    result = new JSONArray(responseStrBuilder.toString());
                    // Nos quedamos solamente con la primera

                    //general el random
                    int longitud = result.length();
                    int numero1 = (int) (Math.random() * longitud) + 1;
                    int numero2 = (int) (Math.random() * longitud) + 1;
                    int numero3 = (int) (Math.random() * longitud) + 1;
                    int numero4 = (int) (Math.random() * longitud) + 1;
                    int numero5 = (int) (Math.random() * longitud) + 1;
                    int num = 0;
                    //sacar los datos
                    for (int i = 0; i < numero1; i++) {
                        num = i;
                        jsonobject = result.getJSONObject(num);
                        palabraESP1 = jsonobject.getString(palabracaste);
                        res1 = jsonobject.getString(palabraoriginal);
                    }
                    for (int i = 0; i < numero2; i++) {
                        num = i;
                        jsonobject = result.getJSONObject(num);
                        palabraESP2 = jsonobject.getString(palabracaste);
                        res2 = jsonobject.getString(palabraoriginal);
                    }
                    for (int i = 0; i < numero3; i++) {
                        num = i;
                        jsonobject = result.getJSONObject(num);
                        palabraESP3 = jsonobject.getString(palabracaste);
                        res3 = jsonobject.getString(palabraoriginal);
                    }
                    for (int i = 0; i < numero4; i++) {
                        num = i;
                        jsonobject = result.getJSONObject(num);
                        palabraESP4 = jsonobject.getString(palabracaste);
                        res4 = jsonobject.getString(palabraoriginal);
                    }
                    for (int i = 0; i < numero5; i++) {
                        num = i;
                        jsonobject = result.getJSONObject(num);
                        palabraESP5 = jsonobject.getString(palabracaste);
                        res5 = jsonobject.getString(palabraoriginal);
                    }
                    //mostrar la palabra en español
                    quest1.setText(palabraESP1);
                    quest2.setText(palabraESP2);
                    quest3.setText(palabraESP3);
                    quest4.setText(palabraESP4);
                    quest5.setText(palabraESP5);


                    responseBody.close();
                    responseBodyReader.close();
                    myConnection.disconnect();
                } else {
                    // Error en la conexión
                    Log.println(Log.ERROR, "Error", "¡Conexión fallida!");
                }
            } catch (Exception e) {
                Log.println(Log.ERROR, "Error", "¡Conexión fallida!");
            }
            return (null);
        }


    }
}