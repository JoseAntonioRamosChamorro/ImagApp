package com.example.idioma;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    //modificar en caso de que cambie la ip del equipo
    String ip = "192.168.1.106";
    //cambiar entre documentos de php
    String prim = "idiomas.php";
    String segun = "palabras.php";
    //cambiar campos
    String id = "idIdiomas";
    String campo ="nombreIdioma";

    ListView listaIdiomas;
    ArrayList<String> idiomas;
    String idIdioma, nombreIdioma;

    ConsultaRemota acceso;
    AltaRemota alta;
    BajaRemota baja;
    JSONArray result;
    JSONObject jsonobject;
    int posicion;
    ArrayAdapter<String> adapter;
    FloatingActionButton fabIdioma;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listaIdiomas = findViewById(R.id.listaIdiomas);
        fabIdioma = findViewById(R.id.fabIdiomas);

        idiomas = new ArrayList<>();

        // Creamos el adaptador
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, idiomas);

        // Asignamos el adaptador a nuestro ListView
        listaIdiomas.setAdapter(adapter);

        acceso = new ConsultaRemota();
        acceso.execute();

        // Asignamos un listener a cada elemento de la lista
        listaIdiomas.setOnItemClickListener((parent, view, position, id) -> {
            Intent visorDetalles = new Intent(view.getContext(), Palabras.class);
            visorDetalles.putExtra("Idiomas", idiomas.get(position));
            startActivity(visorDetalles);
        });

        // Realizamos el alta de un idioma desde el botón fabidiomas
        fabIdioma.setOnClickListener(view -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder( com.example.idioma.MainActivity.this);
            alertDialog.setTitle("Idioma Nuevo");
            alertDialog.setMessage("Introduce el idioma");
            final EditText nombreIdioma = new EditText( com.example.idioma.MainActivity.this);
            nombreIdioma.setHint("Idioma");
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            nombreIdioma.setLayoutParams(layoutParams);
            alertDialog.setView(nombreIdioma);
            alertDialog.setPositiveButton("Confirmar", (dialog, which) -> {
                alta = new AltaRemota(nombreIdioma.getText().toString());
                alta.execute();
                acceso = new ConsultaRemota();
                acceso.execute();
            });
            alertDialog.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
            alertDialog.show();
        });

        // Realizamos la baja de un idioma con una pulsación larga en el idioma
        listaIdiomas.setOnItemLongClickListener((arg0, v, index, arg3) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder( com.example.idioma.MainActivity.this);
            builder.setMessage("Confirma si quieres eliminar el idioma")
                    .setCancelable(false)
                    .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Identifico el id del idioma seleccionado
                            String[] cadena = listaIdiomas.getItemAtPosition(index).toString().split("  -  ");
                            baja = new BajaRemota(cadena[0]);
                            baja.execute();
                            acceso = new ConsultaRemota();
                            acceso.execute();
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
        });
    }

    // Consulta idioma
    private class ConsultaRemota extends AsyncTask<Void, Void, String> {
        // Constructor
        public ConsultaRemota() {
        }

        // Inspectores
        protected void onPreExecute() {
            Toast.makeText( com.example.idioma.MainActivity.this, "Obteniendo datos...", Toast.LENGTH_SHORT).show();
        }

        protected String doInBackground(Void... argumentos) {
            try {
                // Crear la URL de conexión al API
                URL url = new URL("http://" + ip + "/ApiRest/" + prim);
                // Crear la conexión HTTP
                HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();
                // Establecer método de comunicación. Por defecto GET.
                myConnection.setRequestMethod("GET");
                if (myConnection.getResponseCode() == 200) {
                    // Conexión exitosa
                    // Creamos Stream para la lectura de datos desde el servidor
                    InputStream responseBody = myConnection.getInputStream();
                    InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                    // Creamos Buffer de lectura
                    BufferedReader bR = new BufferedReader(responseBodyReader);
                    String line = "";
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
                    idIdioma = jsonobject.getString(id);
                    nombreIdioma = jsonobject.getString(campo);
                    responseBody.close();
                    responseBodyReader.close();
                    myConnection.disconnect();
                } else {
                    // Error en la conexión
                    Log.println(Log.ERROR, "Error", "¡Conexión fallida!");
                }
            } catch (Exception e) {
                Log.println(Log.ERROR, "Error", "¡Conexión fallida");
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
                        idiomas.add(jsonobject.getString(id) + "  -  " + jsonobject.getString(campo));
                        adapter.notifyDataSetChanged();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // Alta idiomas
    private class AltaRemota extends AsyncTask<Void, Void, String> {
        // Atributos
        String nombreIdioma;

        // Constructor
        public AltaRemota(String nombre) {
            this.nombreIdioma = nombre;
        }

        // Inspectores
        protected void onPreExecute() {
        }

        protected String doInBackground(Void... argumentos) {
            try {
                // Crear la URL de conexión al API
                URL url = new URL("http://" + ip + "/ApiRest/" + prim);
                // Crear la conexión HTTP
                HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();
                // Establecer método de comunicación
                myConnection.setRequestMethod("POST");
                // Conexión exitosa
                HashMap<String, String> postDataParams = new HashMap<>();
                postDataParams.put(campo, this.nombreIdioma);
                myConnection.setDoInput(true);
                myConnection.setDoOutput(true);
                OutputStream os = myConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
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

    private class BajaRemota extends AsyncTask<Void, Void, String> {
        // Atributos
        String idIdioma;

        // Constructor
        public BajaRemota(String id) {
            this.idIdioma = id;
        }

        // Inspectores
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... voids) {
            // Comprobamos que el elemento idioma está vacío
            try {
                // Creamos la URL de conexión al API
                Uri uri = new Uri.Builder().scheme("http").authority(ip).path("/ApiRest/" + segun).appendQueryParameter(id, this.idIdioma).build();
                URL url = new URL(uri.toString());
                // Create connection
                HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();
                // Establecemos método. Por defecto GET.
                myConnection.setRequestMethod("GET");
                if (myConnection.getResponseCode() == 200) {
                    InputStream responseBody = myConnection.getInputStream();
                    InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                    BufferedReader bR = new BufferedReader(responseBodyReader);
                    String line;
                    StringBuilder responseStrBuilder = new StringBuilder();
                    // Leemos el flujo de entrada
                    while ((line = bR.readLine()) != null) {
                        responseStrBuilder.append(line);
                    }
                    // Parseamos respuesta en formato JSON
                    result = new JSONArray(responseStrBuilder.toString());
                    responseBody.close();
                    responseBodyReader.close();
                    myConnection.disconnect();
                } else {
                    // Error en la conexión
                    Log.println(Log.ERROR, "Error", "¡Conexión fallida!");
                }
            } catch (Exception e) {
                Log.println(Log.ERROR, "Error", "¡Conexión fallida!");
            } finally {
                // Borramos el idioma si está vacío o mostramos un mensaje de error si el idioma no está vacío
                if (result.length() == 0) {
                    try {
                        // Crear la URL de conexión al API
                        URI baseUri = new URI("http://" + ip + "/ApiRest/" + prim);
                        String[] parametros = {"id", this.idIdioma};
                        URI uri = applyParameters(baseUri, parametros);
                        // Create connection
                        HttpURLConnection myConnection = (HttpURLConnection) uri.toURL().openConnection();
                        // Establecer método. Por defecto GET
                        myConnection.setRequestMethod("DELETE");
                        if (myConnection.getResponseCode() == 200) {
                            // Success
                            Log.println(Log.ASSERT, "Resultado", "Idioma eliminado");
                            myConnection.disconnect();
                        } else {
                            // Error handling code goes here
                            Log.println(Log.ASSERT, "Error", "Error");
                        }
                    } catch (Exception e) {
                        Log.println(Log.ASSERT, "Excepción", e.getMessage());
                    }
                } else {
                    // Mostramos un mensaje indicando que NO se puede eliminar un idioma con registros
                    runOnUiThread(new Runnable() {
                        public void run() {
                            AlertDialog alertDialog = new AlertDialog.Builder(com.example.idioma.MainActivity.this).create();
                            alertDialog.setTitle("Alerta");
                            alertDialog.setMessage("No puedes eliminar un idioma con registros, elimine primero las palabras");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Aceptar", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            alertDialog.show();
                        }
                    });
                }
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
                    // As URLEncoder are always correct, this exception should never be thrown.
                    throw new RuntimeException(ex);
                }
            }
            try {
                return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), query.toString(), null);
            } catch (Exception ex) {
                // As URLEncoder are always correct, this exception should never be thrown.
                throw new RuntimeException(ex);
            }
        }
    }
}