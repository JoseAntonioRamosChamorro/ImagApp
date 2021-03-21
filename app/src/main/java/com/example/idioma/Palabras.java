package com.example.idioma;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class Palabras extends AppCompatActivity {
    //modificar en caso de que cambie la ip del equipo
    String ip = "192.168.1.106";
    //cambiar entre documentos de php
    String segun = "palabras.php";
    //campos de la bd
    String idtabla1 = "idIdiomas";
    String idBD = "idpalabras";
    String palabracaste = "palabraCastellano";
    String palabraoriginal = "palabraOriginal";
    String idBDFK = "idiomaFK";


    ListView listaidiomas;
    ArrayList<String> idiomas;
    String idiomaselecionado;

    ConsultaRemota acceso;
    AltaRemota alta;
    BajaRemota baja;
    ModificacionRemota modifica;

    JSONArray result;
    JSONObject jsonobject;
    int posicion;
    ArrayAdapter<String> adapter;

    Button btnVolver, btntest;
    FloatingActionButton fabidioma;
    String[] cadena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idioma);

        TextView idioma = findViewById(R.id.txtIdiomaSeleccionado);
        // Recibimos el idioma seleccionado
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        idiomaselecionado = bundle.getString("Idiomas");

        // Separamos la cadena, el id y el nombre
        cadena = idiomaselecionado.split("  -  ");
        idioma.setText("Idioma " + "-" + cadena[1] + "-");

        listaidiomas = findViewById(R.id.listaidioma);
        btnVolver = findViewById(R.id.btnVolver);
        fabidioma = findViewById(R.id.fabidioma);

        idiomas = new ArrayList<>();

        // Creamos el adaptador
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, idiomas);

        // Asignamos el adaptador a nuestro ListView
        listaidiomas.setAdapter(adapter);

        // Alta de un idioma
        fabidioma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Palabras.this);
                alertDialog.setTitle("NUEVA PALABRA");
                alertDialog.setMessage("Introduzca las palabras");
                EditText palabraEsp = new EditText(Palabras.this);
                palabraEsp.setHint("Palabra en Español");
                EditText palabra2 = new EditText(Palabras.this);
                palabra2.setHint("Palabra en idioma seleccionado");
                LinearLayout layout = new LinearLayout(Palabras.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(palabraEsp);
                layout.addView(palabra2);
                alertDialog.setView(layout);
                // Botón Confirmar nuevo idioma
                alertDialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        alta = new AltaRemota(palabraEsp.getText().toString(), palabra2.getText().toString(), cadena[0]);
                        alta.execute();
                        acceso = new ConsultaRemota(cadena[0]);
                        acceso.execute();
                    }
                });
                alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();
            }
        });

        // Botón Modificar
        listaidiomas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] IdiomaSeleccionado = listaidiomas.getItemAtPosition(position).toString().split("-");
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Palabras.this);
                alertDialog.setTitle("MODIFICAR PALABRA");
                alertDialog.setMessage("Modificar palabra");
                //crear
                EditText espword = new EditText(Palabras.this);
                EditText noespword = new EditText(Palabras.this);
                //agregar texto
                espword.setText(IdiomaSeleccionado[1]);
                noespword.setText(IdiomaSeleccionado[2]);

                LinearLayout layout = new LinearLayout(Palabras.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                //añadir al layeout
                layout.addView(espword);
                layout.addView(noespword);
                alertDialog.setView(layout);
                // Botón Confirmar
                alertDialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        modifica = new ModificacionRemota(IdiomaSeleccionado[0], espword.getText().toString(), noespword.getText().toString());
                        modifica.execute();
                        acceso = new ConsultaRemota(cadena[0]);
                        acceso.execute();
                    }
                });
                // Botón Cancelar
                alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();
            }
        });
        acceso = new ConsultaRemota(cadena[0]);
        acceso.execute();

        // Baja de un idioma
        listaidiomas.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View v, int index, long arg3) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Palabras.this);
                builder.setMessage("Confirma si quieres eliminar la palabra")
                        .setCancelable(false)
                        .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Consigo el ID del cuaderno seleccionado de la lista
                                String[] idiomaSelecionado = listaidiomas.getItemAtPosition(index).toString().split("  -  ");
                                baja = new BajaRemota(idiomaSelecionado[0]);
                                baja.execute();
                                acceso = new ConsultaRemota(cadena[0]);
                                acceso.execute();
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }
        });
        //vuelve al activity anterior idiomas
        btnVolver.setOnClickListener(v -> onBackPressed());

        btntest = findViewById(R.id.btntest);
        btntest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //datos a pasar
                Intent intent1 = new Intent(Palabras.this, Test.class);
                intent1.putExtra("idiomaelegido", cadena[1]);
                Intent intent2 = new Intent(Palabras.this, Test.class);
                intent1.putExtra("numidioma", cadena[0]);
                startActivity(intent1);
                startActivity(intent2);

            }
        });
    }

    // Consulta idiomas
    class ConsultaRemota extends AsyncTask<Void, Void, String> {
        // Atributos
        String diIdiomaFK;

        // Constructor
        public ConsultaRemota(String id) {
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
                    posicion = 0;
                    jsonobject = result.getJSONObject(posicion);
                    // Sacamos dato a dato obtenido
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

        protected void onPostExecute(String mensaje) {
            // Añado los idiomas obtenidos a la lista
            try {
                idiomas.clear();
                if (result != null) {
                    int longitud = result.length();
                    for (int i = 0; i < longitud; i++) {
                        jsonobject = result.getJSONObject(i);
                        idiomas.add(jsonobject.getString(idBD) + "  -  "
                                + jsonobject.getString(palabracaste) + "  -  "
                                + jsonobject.getString(palabraoriginal));
                        adapter.notifyDataSetChanged();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter.notifyDataSetChanged();
        }
    }

    // Alta Idioma
    private class AltaRemota extends AsyncTask<Void, Void, String> {
        // Atributos
        String castepalabra, originalpalabra, ididiomaFK;

        // Constructor
        public AltaRemota(String castellanopalabra, String palabraselect, String idIdiomaFK) {
            this.castepalabra = castellanopalabra;
            this.originalpalabra = palabraselect;
            this.ididiomaFK = idIdiomaFK;
        }

        // Inspectoras
        protected void onPreExecute() {
        }

        protected String doInBackground(Void... argumentos) {
            try {
                // Crear la URL de conexión al API
                URL url = new URL("http://" + ip + "/ApiRest/" + segun);
                // Crear la conexión HTTP
                HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();
                // Establecer método de comunicación
                myConnection.setRequestMethod("POST");
                // Conexión exitosa
                HashMap<String, String> postDataParams = new HashMap<>();
                postDataParams.put(palabracaste, this.castepalabra);
                postDataParams.put(palabraoriginal, this.originalpalabra);
                postDataParams.put(idBDFK, this.ididiomaFK);
                myConnection.setDoInput(true);
                myConnection.setDoOutput(true);
                OutputStream os = myConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                writer.write(getPostDataString(postDataParams));
                writer.flush();
                writer.close();
                os.close();
                myConnection.getResponseCode();
                if (myConnection.getResponseCode() == 200) {
                    // Success
                    myConnection.disconnect();
                } else {
                    // Error handling code goes here
                    Log.println(Log.ASSERT, "Error", "Error");
                }
            } catch (Exception e) {
                Log.println(Log.ASSERT, "Excepción", e.getMessage());
            }
            return (null);
        }

        protected void onPostExecute(String mensaje) {
        }

        private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    result.append("&");
                }
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
            return result.toString();
        }
    }

    // Baja apuntes
    private class BajaRemota extends AsyncTask<Void, Void, String> {
        // Atributos
        String idAtt;

        // Constructor
        public BajaRemota(String id) {
            this.idAtt = id;
        }

        // Inspectores
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // Crear la URL de conexión al API
                URI baseUri = new URI("http://" + ip + "/ApiRest/" + segun);
                String[] parametros = {"id", this.idAtt};
                URI uri = applyParameters(baseUri, parametros);
                // Create connection
                HttpURLConnection myConnection = (HttpURLConnection) uri.toURL().openConnection();
                // Establecer método. Por defecto GET
                myConnection.setRequestMethod("DELETE");
                if (myConnection.getResponseCode() == 200) {
                    // Success
                    Log.println(Log.ASSERT, "Resultado", "Palabra eliminada");
                    myConnection.disconnect();
                } else {
                    // Error handling code goes here
                    Log.println(Log.ASSERT, "Error", "Error");
                }
            } catch (Exception e) {
                Log.println(Log.ASSERT, "Excepción", e.getMessage());
            }
            return null;
        }

        protected void onPostExecute(String mensaje) {
        }

        URI applyParameters(URI uri, String[] urlParameters) {
            StringBuilder query = new StringBuilder();
            boolean first = true;
            for (int i = 0; i < urlParameters.length; i += 2) {
                if (first) {
                    first = false;
                } else {
                    query.append("&");
                }
                try {
                    query.append(urlParameters[i]).append("=").append(URLEncoder.encode(urlParameters[i + 1], "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException(ex);
                }
            }
            try {
                return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), query.toString(), null);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    // Modificar apuntes
    private class ModificacionRemota extends AsyncTask<Void, Void, String> {
        // Atributos
        String campoID;
        String campo1;
        String campo2;

        // Constructor
        public ModificacionRemota(String id, String palabracastellano, String textoApunte) {
            this.campoID = id;
            this.campo1 = palabracastellano;
            this.campo2 = textoApunte;
        }

        // Inspectores
        protected void onPreExecute() {
        }

        protected String doInBackground(Void... voids) {
            try {
                String response = "";
                Uri uri = new Uri.Builder().scheme("http").authority(ip).path("/ApiRest/" + segun)
                        .appendQueryParameter(idBD, this.campoID)
                        .appendQueryParameter(palabracaste, this.campo1)
                        .appendQueryParameter(palabraoriginal, this.campo2)
                        .build();
                // Create connection
                URL url = new URL(uri.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(15000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("PUT");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                } else {
                    response = "";
                }
                connection.getResponseCode();
                if (connection.getResponseCode() == 200) {
                    // Success
                    Log.println(Log.ASSERT, "Resultado", "Palabra eliminado:" + response);
                    connection.disconnect();
                } else {
                    // Error handling code goes here
                    Log.println(Log.ASSERT, "Error", "Error");
                }
            } catch (Exception e) {
                Log.println(Log.ASSERT, "Excepción", e.getMessage());
            }
            return null;
        }

        protected void onPostExecute(String mensaje) {
        }
    }
}