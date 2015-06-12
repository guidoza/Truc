/* Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.game.guillermo.truc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.github.alexkolpa.fabtoolbar.FabToolbar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MainActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener, RealTimeMessageReceivedListener,
        RoomStatusUpdateListener, RoomUpdateListener, OnInvitationReceivedListener {

    /*
     * API INTEGRATION SECTION. This section contains the code that integrates
     * the game with the Google Play game services API.
     */

    final static String TAG = "ButtonClicker2000";

    // Request codes for the UIs that we show with startActivityForResult:
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_INVITATION_INBOX = 10001;
    final static int RC_WAITING_ROOM = 10002;

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    // Client used to interact with Google APIs.
    private GoogleApiClient mGoogleApiClient;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Set to true to automatically start the sign in flow when the Activity starts.
    // Set to false to require the user to click the button in order to sign in.
    private boolean mAutoStartSignInFlow = true;

    // ID de la sala activa, null si no estamos jugando
    String mRoomId = null;

    //Estamos en multiplayer?
    boolean mMultiplayer = false;

    //Lista de participantes en el juego activo
    ArrayList<Participant> mParticipants = null;

    //Id de mi participante
    String mMyId = null;

    //Id de la invitacion que recibimos
    String mIncomingInvitationId = null;

    //Para enviar mensajes
    byte[] mMsgBuf = new byte[2];

    String idJugador1 = null;
    String idJugador2 = null;
    String turno = null;
    ImageView tvJugador1;
    ImageView tvJugador2;
    ImageView tvJugador3;
    ImageView tvCartaMesa1;
    ImageView tvCartaMesa2;
    ImageView tvCartaMesa3;
    ImageView tvMesaRival1;
    ImageView tvMesaRival2;
    ImageView tvMesaRival3;
    TextView marcador;
    TextView marcador2;
    TextView textoAccion1;
    TextView textoAccion2;
    private List<Carta> baraja = new ArrayList<>();
    private List<Carta> manoJugador = new ArrayList<>();
    Carta carta1;
    Carta carta2;
    Carta carta3;
    int miValor = 0, valor1 = 0, valor2 = 0, valor3 = 0, valorEmpate = 0;
    int ronda = 1;
    int misRondasGanadas = 0;
    AlertDialog.Builder dialogoNuevaPartida;
    String remoteId;
    String mano;
    boolean hayEmpate = false;
    final static int NO_QUIERO_TRUC = 1;
    final static int TRUC = 2;
    final static int RETRUC = 3;
    final static int CUATRE_VAL = 4;
    final static int NO_QUIERO_ENVID = 1;
    final static int ENVID = 2;
    final static int TORNE = 4;
    int puntosTruc = 0;
    int puntosEnvid = 0;
    int miEnvid = 0;
    boolean hayEnvid = false;
    int puntosTotalesMios = 0;
    int puntosTotalesJugador2 = 0;
    String ganadorRonda1 = "";
    String sCartasJ2 = "";
    int[] list = new int[3];
    int[] list2 = new int[3];
    int[] numCarta = new int[3];
    Carta aux;
    int envidOtro = 0;
    String ganadorEnvid = null;
    boolean todosMismoPalo = false;
    int maximo = 0;
    boolean hayTruc = false;
    boolean hayRetruc = false;
    boolean hayCuatreVal = false;
    boolean hayJocFora = false;
    boolean hayVuelvo = false;
    boolean faltaDirecta = false;

    MaterialDialog.Builder materialDialog;
    MaterialDialog repartiendo;

    ImageView imgPerfilRival;
    ImageView imgPerfil;
    ProgressBar progressBar1;
    ProgressBar progressBar2;
    CountDownTimer mCountDownTimerJ1;
    CountDownTimer mCountDownTimerJ2;
    int segundos = 40;

    private float xDelta;
    private float yDelta;
    PointF inicio1;
    PointF inicio2;
    PointF inicio3;
    PointF inicio1Rival;
    PointF inicio2Rival;
    PointF inicio3Rival;
    int posTvJugador1 = 0, posTvJugador2 = 0, posTvJugador3 = 0;
    boolean hayAnimaciones = false;
    boolean hayAnimacionRival1 = false;
    boolean hayAnimacionRival2 = false;
    boolean hayAnimacionRival3 = false;
    ImageButton truc;
    ImageButton envid;
    ImageButton meVoy;
    ImageButton retruque;
    ImageButton quatreVal;
    ImageButton jocFora;
    ImageButton salir;
    ImageButton laFalta;
    FabToolbar actionButton;
    View.OnClickListener menuListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        menuListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = "";

                switch (v.getId()) {
                    case R.id.truco:
                        truco();
                        actionButton.hide();
                        truc.setVisibility(View.GONE);
                        break;
                    case R.id.retruco:
                        retruco();
                        actionButton.hide();
                        retruque.setVisibility(View.GONE);
                        break;
                    case R.id.quatre:
                        quatreVal();
                        actionButton.hide();
                        quatreVal.setVisibility(View.GONE);
                        break;
                    case R.id.joc_fora:
                        jocFora();
                        actionButton.hide();
                        jocFora.setVisibility(View.GONE);
                        break;
                    case R.id.envido:
                        envido();
                        actionButton.hide();
                        envid.setVisibility(View.GONE);
                        laFalta.setVisibility(View.GONE);
                        break;
                    case R.id.la_falta:
                        envidoLaFalta();
                        actionButton.hide();
                        envid.setVisibility(View.GONE);
                        laFalta.setVisibility(View.GONE);
                        break;
                    case R.id.me_voy:
                        //A implementar
                        break;
                    case R.id.salir:
                        actionButton.hide();
                        break;

                }

                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        };


        tvJugador1 = (ImageView) findViewById(R.id.carta1Jugador);
        tvJugador2 = (ImageView) findViewById(R.id.carta2Jugador);
        tvJugador3 = (ImageView) findViewById(R.id.carta3Jugador);

        inicio1 = new PointF(tvJugador1.getX(), tvJugador1.getY());
        inicio2 = new PointF(tvJugador2.getX(), tvJugador2.getY());
        inicio3 = new PointF(tvJugador3.getX(), tvJugador3.getY());

        tvCartaMesa1 = (ImageView) findViewById(R.id.carta1Mesa);
        tvCartaMesa2 = (ImageView) findViewById(R.id.carta2Mesa);
        tvCartaMesa3 = (ImageView) findViewById(R.id.carta3Mesa);

        tvMesaRival1 = (ImageView) findViewById(R.id.carta1MesaRival);
        tvMesaRival2 = (ImageView) findViewById(R.id.carta2MesaRival);
        tvMesaRival3 = (ImageView) findViewById(R.id.carta3MesaRival);
        tvMesaRival1.animate().rotationXBy(30).rotationYBy(-5).rotation(3).setDuration(0);
        tvMesaRival2.animate().rotationXBy(30).setDuration(0);
        tvMesaRival3.animate().rotationXBy(30).rotationYBy(5).rotation(-3).setDuration(0);



        tvMesaRival1.setVisibility(View.INVISIBLE);
        tvMesaRival2.setVisibility(View.INVISIBLE);
        tvMesaRival3.setVisibility(View.INVISIBLE);
        inicio1Rival = new PointF(tvJugador1.getX(), tvJugador1.getY());
        inicio2Rival = new PointF(tvJugador2.getX(), tvJugador2.getY());
        inicio3Rival = new PointF(tvJugador3.getX(), tvJugador3.getY());

        textoAccion1 = (TextView) findViewById(R.id.textoJugador1);
        textoAccion2 = (TextView) findViewById(R.id.textoJugador2);
        marcador = (TextView) findViewById(R.id.marcador);
        marcador2 = (TextView) findViewById(R.id.marcador2);
        imgPerfilRival = (ImageView) findViewById(R.id.imgPerfilRival);
        imgPerfil = (ImageView) findViewById(R.id.imgPerfil);

        actionButton = (FabToolbar) findViewById(R.id.fab_toolbar);
        truc = (ImageButton) findViewById(R.id.truco);
        envid = (ImageButton) findViewById(R.id.envido);
        meVoy = (ImageButton) findViewById(R.id.me_voy);
        retruque = (ImageButton) findViewById(R.id.retruco);
        quatreVal = (ImageButton) findViewById(R.id.quatre);
        jocFora = (ImageButton) findViewById(R.id.joc_fora);
        salir = (ImageButton) findViewById(R.id.salir);
        laFalta = (ImageButton) findViewById(R.id.la_falta);

        progressBar1 = (ProgressBar) findViewById(R.id.progres_segundos_1);
        progressBar2 = (ProgressBar) findViewById(R.id.progres_segundos_2);

        // Creamos el nuevo cliente de Google con acceso a Plus y Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        //Listener para todos los elementos
        for (int id : CLICKABLES) {
            findViewById(id).setOnClickListener(this);
        }

    }


    private void showSingleChoiceAlertEnvid(String title, int array) {
        new MaterialDialog.Builder(this)
                .title(title)
                .titleColorRes(R.color.menuItems)
                .items(array)
                .itemColorRes(R.color.menuItems)
                .itemsCallbackSingleChoice(2, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        switch (which) {
                            //Quiero
                            case 0:
                                miEnvid = comprobarEnvid();
                                hayEnvid = true;
                                comprobarGanadorEnvid();
                                puntosEnvid = ENVID;
                                enviarMensajeHayEnvidAndGanador(ganadorEnvid, 1);
                                cambiarBarraProgreso();
                                break;
                            //Vuelvo
                            case 1:
                                miEnvid = comprobarEnvid();
                                enviarMensajeVuelvoAEnvidar();
                                cambiarBarraProgreso();
                                hayVuelvo = true;
                                break;
                            //Falta
                            case 2:
                                miEnvid = comprobarEnvid();
                                enviarMensajeLaFalta(1);
                                cambiarBarraProgreso();
                                break;
                            //No quiero
                            case 3:
                                enviarMensajeNoQuiero(1);
                                cambiarBarraProgreso();
                                break;
                        }
                        return true;
                    }
                })
                .positiveText("Elegir")
                .cancelable(false)
                .show().getWindow().setBackgroundDrawable(new ColorDrawable(0x30000000));
    }

    private void showSingleChoiceAlertVuelvo(String title, int array) {
        new MaterialDialog.Builder(this)
                .title(title)
                .titleColorRes(R.color.menuItems)
                .items(array)
                .itemColorRes(R.color.menuItems)
                .itemsCallbackSingleChoice(2, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        switch (which) {
                            //Quiero
                            case 0:
                                hayEnvid = true;
                                comprobarGanadorEnvid();
                                enviarMensajeHayEnvidAndGanador(ganadorEnvid, 2);
                                puntosEnvid = TORNE;
                                desbloquearCartas();
                                if(!turno.equals(mMyId)){
                                    cambiarBarraProgreso();
                                }else reiniciarBarraProgreso();
                                break;
                            //Falta
                            case 1:
                                enviarMensajeLaFalta(1);
                                cambiarBarraProgreso();
                                break;
                            //No quiero
                            case 2:
                                desbloquearCartas();
                                enviarMensajeNoQuiero(2);
                                if(!turno.equals(mMyId)){
                                    cambiarBarraProgreso();
                                }else reiniciarBarraProgreso();
                                break;
                        }
                        return true;
                    }
                })
                .positiveText("Elegir")
                .cancelable(false)
                .show().getWindow().setBackgroundDrawable(new ColorDrawable(0x30000000));
    }

    private void showSingleChoiceAlertFalta(String title, int array) {
        new MaterialDialog.Builder(this)
                .title(title)
                .titleColorRes(R.color.menuItems)
                .items(array)
                .itemColorRes(R.color.menuItems)
                .itemsCallbackSingleChoice(2, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        switch (which) {
                            //Quiero
                            case 0:
                                hayEnvid = true;
                                if (mMyId.equals(turno)) desbloquearCartas();
                                comprobarGanadorEnvid();
                                //puntos a sumar por la falta
                                if(ganadorEnvid.equals(mMyId)){
                                    if(puntosTotalesJugador2<=12){
                                        puntosEnvid = 24;
                                    } else if(puntosTotalesJugador2>12){
                                        puntosEnvid = 24 - puntosTotalesJugador2;
                                    }
                                }
                                enviarMensajeHayEnvidAndGanador(ganadorEnvid, 3);
                                if(!turno.equals(mMyId)){
                                    cambiarBarraProgreso();
                                }else reiniciarBarraProgreso();
                                break;
                            case 1:
                                if (mMyId.equals(turno)) desbloquearCartas();
                                if(faltaDirecta){
                                    enviarMensajeNoQuiero(4);
                                }else enviarMensajeNoQuiero(3);
                                if(!turno.equals(mMyId)){
                                    cambiarBarraProgreso();
                                }else reiniciarBarraProgreso();
                                break;
                        }
                        return true;
                    }
                })
                .positiveText("Elegir")
                .cancelable(false)
                .show().getWindow().setBackgroundDrawable(new ColorDrawable(0x30000000));
    }

    private void showSingleChoiceAlertTruco(String title, int array) {
        new MaterialDialog.Builder(this)
                .title(title)
                .titleColorRes(R.color.menuItems)
                .items(array)
                .itemColorRes(R.color.menuItems)
                .itemsCallbackSingleChoice(2, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        switch (which) {
                            //Quiero
                            case 0:
                                hayTruc = true;
                                enviarMensajeQuieroTruc();
                                cambiarBarraProgreso();
                                retruque.setVisibility(View.VISIBLE);
                                break;
                            //Retruque
                            case 1:
                                enviarMensajeRetruc();
                                cambiarBarraProgreso();
                                break;
                            //No quiero
                            case 2:
                                enviarMensajeNoQuieroTruc(1);
                                //mostrarResultadosPerdedorMano("PRIMERO");
                                break;
                        }
                        return true;
                    }
                })
                .positiveText("Elegir")
                .cancelable(false)
                .show().getWindow().setBackgroundDrawable(new ColorDrawable(0x30000000));
    }

    private void showSingleChoiceAlertRetruc(String title, int array) {
        new MaterialDialog.Builder(this)
                .title(title)
                .titleColorRes(R.color.menuItems)
                .items(array)
                .itemColorRes(R.color.menuItems)
                .itemsCallbackSingleChoice(2, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        switch (which) {
                            //Quiero
                            case 0:
                                hayRetruc = true;
                                enviarMensajeQuieroRetruc();
                                if(!turno.equals(mMyId)){
                                    cambiarBarraProgreso();
                                }else{
                                    reiniciarBarraProgreso();
                                    desbloquearCartas();
                                }
                                quatreVal.setVisibility(View.VISIBLE);
                                break;
                            //Cuatre val
                            case 1:
                                enviarMensajeCuatreVal();
                                cambiarBarraProgreso();
                                break;
                            //No quiero
                            case 2:
                                enviarMensajeNoQuieroTruc(2);
                               // mostrarResultadosPerdedorMano("PRIMERO");
                                break;
                        }
                        return true;
                    }
                })
                .positiveText("Elegir")
                .cancelable(false)
                .show().getWindow().setBackgroundDrawable(new ColorDrawable(0x30000000));
    }

    private void showSingleChoiceAlertCuatreVal(String title, int array) {
        new MaterialDialog.Builder(this)
                .title(title)
                .titleColorRes(R.color.menuItems)
                .items(array)
                .itemColorRes(R.color.menuItems)
                .itemsCallbackSingleChoice(2, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        switch (which) {
                            //Quiero
                            case 0:
                                hayCuatreVal = true;
                                enviarMensajeQuieroCuatreVal();
                                if(!turno.equals(mMyId)){
                                    cambiarBarraProgreso();
                                }else{
                                    reiniciarBarraProgreso();
                                    desbloquearCartas();
                                }
                                jocFora.setVisibility(View.VISIBLE);
                                break;
                            //Joc fora
                            case 1:
                                enviarMensajeJocFora();
                                cambiarBarraProgreso();
                                break;
                            //No quiero
                            case 2:
                                enviarMensajeNoQuieroTruc(3);
                               // mostrarResultadosPerdedorMano("PRIMERO");
                                break;
                        }
                        return true;
                    }
                })
                .positiveText("Elegir")
                .cancelable(false)
                .show().getWindow().setBackgroundDrawable(new ColorDrawable(0x30000000));
    }

    private void showSingleChoiceAlertJocFora(String title, int array) {
        new MaterialDialog.Builder(this)
                .title(title)
                .titleColorRes(R.color.menuItems)
                .items(array)
                .itemColorRes(R.color.menuItems)
                .itemsCallbackSingleChoice(2, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        switch (which) {
                            //Quiero
                            case 0:
                                hayJocFora = true;
                                enviarMensajeQuieroJocFora();
                                desbloquearCartas();
                                if(!turno.equals(mMyId)){
                                    cambiarBarraProgreso();
                                }else{
                                    reiniciarBarraProgreso();
                                    desbloquearCartas();
                                }
                                break;
                            //No quiero
                            case 1:
                                enviarMensajeNoQuieroTruc(4);
                               // mostrarResultadosPerdedorMano("PRIMERO");
                                break;

                        }
                        return true;
                    }
                })
                .positiveText("Elegir")
                .cancelable(false)
                .show().getWindow().setBackgroundDrawable(new ColorDrawable(0x30000000));
    }

    private void comprobarGanadorEnvid() {
        if (miEnvid > envidOtro && mMyId.equals(idJugador1)) {
            ganadorEnvid = idJugador1;
        } else if (miEnvid > envidOtro && mMyId.equals(idJugador2)) {
            ganadorEnvid = idJugador2;
        } else if (miEnvid < envidOtro && mMyId.equals(idJugador1)) {
            ganadorEnvid = idJugador2;
        } else if (miEnvid < envidOtro && mMyId.equals(idJugador2)) {
            ganadorEnvid = idJugador1;
        } else if (miEnvid == envidOtro && mMyId.equals(idJugador1) && mMyId.equals(mano)) {
            ganadorEnvid = idJugador1;
        } else if (miEnvid == envidOtro && mMyId.equals(idJugador1) && !mMyId.equals(mano)) {
            ganadorEnvid = idJugador2;
        } else if (miEnvid == envidOtro && mMyId.equals(idJugador2) && mMyId.equals(mano)) {
            ganadorEnvid = idJugador2;
        } else if (miEnvid == envidOtro && mMyId.equals(idJugador2) && !mMyId.equals(mano)) {
            ganadorEnvid = idJugador1;
        }
    }

    private void showBasicAlert(String title, String message) {
        materialDialog = new MaterialDialog.Builder(this)
                .title(title)
                .content(message)
                .positiveText("Aceptar")
                .cancelable(false);
        materialDialog.show();
    }
    /*
    private void showBasicAlertWinLost(String title, String message) {
        materialDialog = new MaterialDialog.Builder(this)
                .title(title)
                .content(message)
                .positiveText("Aceptar")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        leaveRoom();
                    }
                })
                .cancelable(false);
        materialDialog.show();
    }*/

    private void showProgressDialog(String content) {
        materialDialog = new MaterialDialog.Builder(this)
                .title("Repartiendo...")
                .content(content)
                .progress(true, 0)
                .cancelable(false);
        repartiendo = materialDialog.show();
    }

    private void showProgressCustomDialog(View content) {
        materialDialog = new MaterialDialog.Builder(this)
                .title("Repartiendo...")
                .titleGravity(GravityEnum.CENTER)
                .customView(content, false)
                .cancelable(false)
                .theme(Theme.DARK);
        repartiendo = materialDialog.show();
    }

    private void envido() {
        Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
        bloquearCartas();
        miEnvid = comprobarEnvid();
        enviarMensajeEnvid(miEnvid);
        cambiarBarraProgreso();
    }
    private void envidoLaFalta() {
        Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
        bloquearCartas();
        miEnvid = comprobarEnvid();
        enviarMensajeLaFalta(2);
        cambiarBarraProgreso();
    }

    private int comprobarEnvid() {
        String palo1 = carta1.getPalo();
        String palo2 = carta2.getPalo();
        String palo3 = carta3.getPalo();


        if (palo1.equals(palo2) && palo1.equals(palo3)) todosMismoPalo = true;

        if (todosMismoPalo) {
            if (carta1.getNumero().compareTo(carta2.getNumero()) > 0) {
                if (carta2.getNumero().compareTo(carta3.getNumero()) > 0) {
                    return Integer.parseInt(carta1.getNumero()) + Integer.parseInt(carta2.getNumero()) + 20;
                } else {
                    return Integer.parseInt(carta1.getNumero()) + Integer.parseInt(carta3.getNumero()) + 20;
                }
            } else {
                if (carta1.getNumero().compareTo(carta3.getNumero()) > 0) {
                    return Integer.parseInt(carta1.getNumero()) + Integer.parseInt(carta2.getNumero()) + 20;
                } else
                    return Integer.parseInt(carta3.getNumero()) + Integer.parseInt(carta2.getNumero()) + 20;

            }

        } else {
            if (palo1.equals(palo2))
                return Integer.parseInt(carta1.getNumero()) + Integer.parseInt(carta2.getNumero()) + 20;
            else if (palo1.equals(palo3))
                return Integer.parseInt(carta1.getNumero()) + Integer.parseInt(carta3.getNumero()) + 20;
            else if (palo2.equals(palo3))
                return Integer.parseInt(carta3.getNumero()) + Integer.parseInt(carta2.getNumero()) + 20;
        }

        for (int i = 0; i < 3; i++) {
            if (maximo < Integer.parseInt(manoJugador.get(i).getNumero()))
                maximo = Integer.parseInt(manoJugador.get(i).getNumero());
        }
        return maximo;

    }

    public void truco() {
        Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
        bloquearCartas();
        enviarMensajeTruc();
        cambiarBarraProgreso();
    }

    public void retruco() {
        Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
        bloquearCartas();
        enviarMensajeRetruc();
        cambiarBarraProgreso();
    }

    public void quatreVal() {
        Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
        bloquearCartas();
        enviarMensajeCuatreVal();
        cambiarBarraProgreso();
    }

    public void jocFora() {
        Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
        bloquearCartas();
        enviarMensajeJocFora();
        cambiarBarraProgreso();
    }


    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {
            case R.id.button_single_player_2:



            case R.id.button_sign_in:
                // user wants to sign in
                // Check to see the developer who's running this sample code read the instructions :-)
                // NOTE: this check is here only because this is a sample! Don't include this
                // check in your actual production app.
                if (!BaseGameUtils.verifySampleSetup(this, R.string.app_id)) {
                    Log.w(TAG, "*** Warning: setup problems detected. Sign in may not work!");
                }

                // start the sign-in flow
                Log.d(TAG, "Sign-in button clicked");
                mSignInClicked = true;
                mGoogleApiClient.connect();
                break;
            case R.id.button_sign_out:
                // user wants to sign out
                // sign out.
                Log.d(TAG, "Sign-out button clicked");
                mSignInClicked = false;
                Games.signOut(mGoogleApiClient);
                mGoogleApiClient.disconnect();
                switchToScreen(R.id.screen_sign_in);
                break;
            case R.id.button_invite_players:
                // show list of invitable players
                intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, 1, 3);
                switchToScreen(R.id.screen_wait);
                startActivityForResult(intent, RC_SELECT_PLAYERS);
                break;
            case R.id.button_see_invitations:
                // show list of pending invitations
                intent = Games.Invitations.getInvitationInboxIntent(mGoogleApiClient);
                switchToScreen(R.id.screen_wait);
                startActivityForResult(intent, RC_INVITATION_INBOX);
                break;
            case R.id.button_accept_popup_invitation:
                // user wants to accept the invitation shown on the invitation popup
                // (the one we got through the OnInvitationReceivedListener).
                acceptInviteToRoom(mIncomingInvitationId);
                mIncomingInvitationId = null;
                break;
            case R.id.button_quick_game:
                // user wants to play against a random opponent right now
                startQuickGame();
                break;
        }
    }

    void startQuickGame() {
        // quick-start a game with 1 randomly selected opponent
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        switchToScreen(R.id.screen_wait);
        keepScreenOn();
        desbloquearCartas();
        resetAll();
        resetTurno();
        Games.RealTimeMultiplayer.create(mGoogleApiClient, rtmConfigBuilder.build());
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode,
                                 Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);

        switch (requestCode) {
            case RC_SELECT_PLAYERS:
                // we got the result from the "select players" UI -- ready to create the room
                handleSelectPlayersResult(responseCode, intent);
                break;
            case RC_INVITATION_INBOX:
                // we got the result from the "select invitation" UI (invitation inbox). We're
                // ready to accept the selected invitation:
                handleInvitationInboxResult(responseCode, intent);
                break;
            case RC_WAITING_ROOM:
                // we got the result from the "waiting room" UI.
                if (responseCode == Activity.RESULT_OK) {
                    // ready to start playing
                    Log.d(TAG, "Starting game (waiting room returned OK).");
                    resetPuntos();
                    showProgressDialog("�Empezamos!");
                    resetAnimaciones();
                    inicializarMano();
                } else if (responseCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                    // player indicated that they want to leave the room
                    leaveRoom();
                } else if (responseCode == Activity.RESULT_CANCELED) {
                    // Dialog was cancelled (user pressed back key, for instance). In our game,
                    // this means leaving the room too. In more elaborate games, this could mean
                    // something else (like minimizing the waiting room UI).
                    leaveRoom();
                }
                break;
            case RC_SIGN_IN:
                Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
                        + responseCode + ", intent=" + intent);
                mSignInClicked = false;
                mResolvingConnectionFailure = false;
                if (responseCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                } else {
                    BaseGameUtils.showActivityResultError(this, requestCode, responseCode, R.string.signin_other_error);
                }
                break;
        }
        super.onActivityResult(requestCode, responseCode, intent);
    }

    // Handle the result of the "Select players UI" we launched when the user clicked the
    // "Invite friends" button. We react by creating a room with those players.
    private void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        Log.d(TAG, "Select players UI succeeded.");

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        Log.d(TAG, "Invitee count: " + invitees.size());

        // get the automatch criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
        }

        // create the room
        Log.d(TAG, "Creating room...");
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.addPlayersToInvite(invitees);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        if (autoMatchCriteria != null) {
            rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        }
        switchToScreen(R.id.screen_wait);
        keepScreenOn();
        desbloquearCartas();
        Games.RealTimeMultiplayer.create(mGoogleApiClient, rtmConfigBuilder.build());
        Log.d(TAG, "Room created, waiting for it to be ready...");
    }

    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");
        Invitation inv = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
        acceptInviteToRoom(inv.getInvitationId());
    }

    // Accept the given invitation.
    void acceptInviteToRoom(String invId) {
        // accept the invitation
        Log.d(TAG, "Accepting invitation: " + invId);
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
        roomConfigBuilder.setInvitationIdToAccept(invId)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
        switchToScreen(R.id.screen_wait);
        keepScreenOn();
        desbloquearCartas();
        Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfigBuilder.build());
    }

    // Activity is going to the background. We have to leave the current room.
    @Override
    public void onStop() {
        Log.d(TAG, "**** got onStop");

        // if we're in a room, leave it.
        leaveRoom();

        // stop trying to keep the screen on
        stopKeepingScreenOn();

        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            switchToScreen(R.id.screen_sign_in);
        } else {
            switchToScreen(R.id.screen_wait);
        }
        super.onStop();
    }

    // Activity just got to the foreground. We switch to the wait screen because we will now
    // go through the sign-in flow (remember that, yes, every time the Activity comes back to the
    // foreground we go through the sign-in flow -- but if the user is already authenticated,
    // this flow simply succeeds and is imperceptible).
    @Override
    public void onStart() {
        switchToScreen(R.id.screen_wait);
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.w(TAG,
                    "GameHelper: client was already connected on onStart()");
            switchToScreen(R.id.screen_main);
        } else {
            Log.d(TAG, "Connecting client.");
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    // Handle back key to make sure we cleanly leave a game if we are in the middle of one
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mCurScreen == R.id.screen_game) {
            leaveRoom();
            return true;
        }
        return super.onKeyDown(keyCode, e);
    }

    // Leave the room.
    void leaveRoom() {
        Log.d(TAG, "Leaving room.");
        mSecondsLeft = 0;
        resetPuntos();
        stopKeepingScreenOn();
        if (mRoomId != null) {
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
            mRoomId = null;
            switchToScreen(R.id.screen_wait);
        } else {
            switchToMainScreen();
        }
    }

    // Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.
    void showWaitingRoom(Room room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, MIN_PLAYERS);

        // show waiting room UI
        startActivityForResult(i, RC_WAITING_ROOM);
    }

    // Called when we get an invitation to play a game. We react by showing that to the user.
    @Override
    public void onInvitationReceived(Invitation invitation) {
        // We got an invitation to play a game! So, store it in
        // mIncomingInvitationId
        // and show the popup on the screen.
        mIncomingInvitationId = invitation.getInvitationId();
        ((TextView) findViewById(R.id.incoming_invitation_text)).setText(
                invitation.getInviter().getDisplayName() + " " +
                        getString(R.string.is_inviting_you));
        switchToScreen(mCurScreen); // This will show the invitation popup
    }

    @Override
    public void onInvitationRemoved(String invitationId) {
        if (mIncomingInvitationId.equals(invitationId)) {
            mIncomingInvitationId = null;
            switchToScreen(mCurScreen); // This will hide the invitation popup
        }
    }

    /*
     * CALLBACKS SECTION. This section shows how we implement the several games
     * API callbacks.
     */

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected() called. Sign in successful!");

        Log.d(TAG, "Sign-in succeeded.");

        // register listener so we are notified if we receive an invitation to play
        // while we are in the game
        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);

        if (connectionHint != null) {
            Log.d(TAG, "onConnected: connection hint provided. Checking for invite.");
            Invitation inv = connectionHint
                    .getParcelable(Multiplayer.EXTRA_INVITATION);
            if (inv != null && inv.getInvitationId() != null) {
                // retrieve and cache the invitation ID
                Log.d(TAG, "onConnected: connection hint has a room invite!");
                acceptInviteToRoom(inv.getInvitationId());
                return;
            }
        }
        switchToMainScreen();

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called. Trying to reconnect.");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);

        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient,
                    connectionResult, RC_SIGN_IN, getString(R.string.signin_other_error));
        }

        switchToScreen(R.id.screen_sign_in);
    }

    // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
    // is connected yet).
    @Override
    public void onConnectedToRoom(Room room) {
        Log.d(TAG, "onConnectedToRoom.");

        // get room ID, participants and my ID:
        mRoomId = room.getRoomId();
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));
        remoteId = null;
        resetPuntos();

        ArrayList<String> ids = room.getParticipantIds();
        for (int i = 0; i < ids.size(); i++) {
            String aux = ids.get(i);
            if (!aux.equals(mMyId)) {
                remoteId = aux;
                break;
            }
        }
        if (mMyId.compareTo(remoteId) > 0) {
            idJugador1 = mMyId;
            idJugador2 = remoteId;
        } else {
            idJugador1 = remoteId;
            idJugador2 = mMyId;
        }

        turno = idJugador1;
        mano = idJugador1;

        // print out the list of participants (for debug purposes)
        Log.d(TAG, "Room ID: " + mRoomId);
        Log.d(TAG, "My ID " + mMyId);
        Log.d(TAG, "<< CONNECTED TO ROOM>>");


        if (mRoomId != null) {
            for (Participant p : mParticipants) {
                String pid = p.getParticipantId();

                if (pid.equals(mMyId)) {
                    new LoadProfileImage(imgPerfil).execute(p.getIconImageUrl());
                }else {
                    new LoadProfileImage(imgPerfilRival).execute(p.getIconImageUrl());
                }
            }
        }

        //getProfileInformation();
    }

    // Called when we've successfully left the room (this happens a result of voluntarily leaving
    // via a call to leaveRoom(). If we get disconnected, we get onDisconnectedFromRoom()).
    @Override
    public void onLeftRoom(int statusCode, String roomId) {
        // we have left the room; return to main screen.
        Log.d(TAG, "onLeftRoom, code " + statusCode);
        switchToMainScreen();
    }

    // Called when we get disconnected from the room. We return to the main screen.
    @Override
    public void onDisconnectedFromRoom(Room room) {
        mRoomId = null;
        showGameError();
    }

    // Show error message about game being cancelled and return to main screen.
    void showGameError() {
        BaseGameUtils.makeSimpleDialog(this, getString(R.string.game_problem));
        switchToMainScreen();
    }

    // Called when room has been created
    @Override
    public void onRoomCreated(int statusCode, Room room) {
        Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
            showGameError();
            return;
        }

        // show the waiting room UI
        showWaitingRoom(room);
    }

    // Called when room is fully connected.
    @Override
    public void onRoomConnected(int statusCode, Room room) {
        Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }
        updateRoom(room);
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }

        // show the waiting room UI
        showWaitingRoom(room);
    }

    // We treat most of the room update callbacks in the same way: we update our list of
    // participants and update the display. In a real game we would also have to check if that
    // change requires some action like removing the corresponding player avatar from the screen,
    // etc.
    @Override
    public void onPeerDeclined(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onP2PDisconnected(String participant) {
    }

    @Override
    public void onP2PConnected(String participant) {
    }

    @Override
    public void onPeerJoined(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerLeft(Room room, List<String> peersWhoLeft) {
        updateRoom(room);
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        updateRoom(room);
    }

    @Override
    public void onRoomConnecting(Room room) {
        updateRoom(room);
    }

    @Override
    public void onPeersConnected(Room room, List<String> peers) {
        updateRoom(room);
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> peers) {
        updateRoom(room);
    }

    void updateRoom(Room room) {
        if (room != null) {
            mParticipants = room.getParticipants();
        }
        if (mParticipants != null) {
            updatePeerScoresDisplay();
        }
    }

    /*
     * GAME LOGIC SECTION. Methods that implement the game's rules.
     */

    // Current state of the game:
    int mSecondsLeft = -1; // how long until the game ends (seconds)
    final static int GAME_DURATION = 20; // game duration, seconds.
    int mScore = 0; // user's current score

    void resetAll() {
        miValor = 0;
        valor1 = 0;
        valor2 = 0;
        valor3 = 0;
        valorEmpate = 0;
        ronda = 1;
        misRondasGanadas = 0;
        tvJugador1.setVisibility(View.VISIBLE);
        tvJugador2.setVisibility(View.VISIBLE);
        tvJugador3.setVisibility(View.VISIBLE);
        tvCartaMesa1.setVisibility(View.INVISIBLE);
        tvCartaMesa2.setVisibility(View.INVISIBLE);
        tvCartaMesa3.setVisibility(View.INVISIBLE);
        tvMesaRival1.setVisibility(View.INVISIBLE);
        tvMesaRival2.setVisibility(View.INVISIBLE);
        tvMesaRival3.setVisibility(View.INVISIBLE);
        hayEmpate = false;
        ganadorRonda1 = "";
        miEnvid = 0;
        hayEnvid = false;
        ganadorEnvid = null;
        envidOtro = 0;
        todosMismoPalo = false;
        maximo = 0;
        puntosEnvid = 0;
        puntosTruc = 0;
        hayTruc = false;
        hayRetruc = false;
        hayCuatreVal = false;
        hayJocFora = false;
        hayVuelvo = false;
        faltaDirecta = false;

        textoAccion1.setVisibility(View.INVISIBLE);
        textoAccion2.setVisibility(View.INVISIBLE);
        segundos = 40;

        progressBar1.setVisibility(View.INVISIBLE);
        progressBar2.setVisibility(View.INVISIBLE);

        truc.setVisibility(View.VISIBLE);
        envid.setVisibility(View.VISIBLE);
        meVoy.setVisibility(View.VISIBLE);
        salir.setVisibility(View.VISIBLE);
        retruque.setVisibility(View.GONE);
        quatreVal.setVisibility(View.GONE);
        jocFora.setVisibility(View.GONE);
        laFalta.setVisibility(View.VISIBLE);

        if(hayAnimaciones){
            deshacerAnimaciones(tvJugador1);
            deshacerAnimaciones(tvJugador2);
            deshacerAnimaciones(tvJugador3);
            posTvJugador1 = 0;
            posTvJugador2 = 0;
            posTvJugador3 = 0;
            hayAnimaciones = false;
        }

        if(hayAnimacionRival1){
            Log.d("KKKKKK", "REiniciando animacion 1");
            tvMesaRival1.animate().scaleX((float) 1).scaleY((float) 1).setDuration(0);
           // tvMesaRival1.animate().rotationXBy(-30).rotationYBy(5).rotation(0).setDuration(0);

            hayAnimacionRival1 = false;
        }
        if(hayAnimacionRival2){
            Log.d("KKKKKK", "REiniciando animacion 2");
            tvMesaRival2.animate().scaleX((float) 1).scaleY((float) 1).setDuration(0);
            //tvMesaRival2.animate().rotationXBy(-30).setDuration(0);

            hayAnimacionRival2 = false;
        }
        if(hayAnimacionRival3){
            Log.d("KKKKKK", "REiniciando animacion 3");
            tvMesaRival3.animate().scaleX((float) 1).scaleY((float) 1).setDuration(0);
            //tvMesaRival3.animate().rotationXBy(-30).rotationYBy(-5).rotation(0).setDuration(0);

            hayAnimacionRival3 = false;
        }

    }
    void resetPuntos(){
        puntosTotalesMios = 0;
        puntosTotalesJugador2 = 0;
        marcador.setText("");
        marcador2.setText("");
    }
    void resetAnimaciones(){
        hayAnimaciones = false;
        hayAnimacionRival1 = false;
        hayAnimacionRival2 = false;
        hayAnimacionRival3 = false;
    }

    void deshacerAnimaciones(View view){

        Log.d("RRRRRR", "Pos1: " + posTvJugador1 + " Pos2: " + posTvJugador2 + " Pos3: " + posTvJugador3);

        if(view.equals(tvJugador1)){
            tvJugador1.animate().translationX(inicio1.x).rotation(0).setDuration(500);
            tvJugador1.animate().translationY(inicio1.y).rotation(0).setDuration(500);

            if(posTvJugador1 == 1){
                view.animate().rotationXBy(-30).scaleX((float) 1).scaleY((float) 1)
                        .rotationYBy(5).setDuration(500);

            }else if(posTvJugador1 == 2){
                view.animate().rotationXBy(-30)
                        .scaleX((float) 1).scaleY((float) 1).setDuration(500);

            }else if(posTvJugador1 == 3){
                view.animate().rotationXBy(-30).scaleX((float) 1).scaleY((float) 1)
                        .rotationYBy(-5).setDuration(600);
            }
        }else if(view.equals(tvJugador2)){
            tvJugador2.animate().translationX(inicio2.x).rotation(0).setDuration(500);
            tvJugador2.animate().translationY(inicio2.y).rotation(0).setDuration(500);
            view.bringToFront();

            if(posTvJugador2 == 1){
                view.animate().rotationXBy(-30).scaleX((float) 1).scaleY((float) 1)
                        .rotationYBy(5).setDuration(500);

            }else if(posTvJugador2 == 2){
                view.animate().rotationXBy(-30)
                        .scaleX((float) 1).scaleY((float) 1).setDuration(500);

            }else if(posTvJugador2 == 3){
                view.animate().rotationXBy(-30).scaleX((float) 1).scaleY((float) 1)
                        .rotationYBy(-5).setDuration(600);
            }
        }else if(view.equals(tvJugador3)){
            tvJugador3.animate().translationX(inicio3.x).rotation(0).setDuration(500);
            tvJugador3.animate().translationY(inicio3.y).rotation(0).setDuration(500);
            view.bringToFront();

            if(posTvJugador3 == 1){
                view.animate().rotationXBy(-30).scaleX((float) 1).scaleY((float) 1)
                        .rotationYBy(5).setDuration(500);

            }else if(posTvJugador3 == 2){
                view.animate().rotationXBy(-30)
                        .scaleX((float) 1).scaleY((float) 1).setDuration(500);

            }else if(posTvJugador3 == 3){
                view.animate().rotationXBy(-30).scaleX((float) 1).scaleY((float) 1)
                        .rotationYBy(-5).setDuration(600);
            }
        }

    }
    void animarTextoAccion(final View view){
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0.0f);
        view.animate().alpha(1.0f).setDuration(1000).start();
        AlphaAnimation animation2 = new AlphaAnimation(1.0f, 0.0f);
        animation2.setDuration(1000);
        animation2.setStartOffset(3500);
        animation2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animation2);
    }

    // Reset game variables in preparation for a new game.
    void desbloquearCartas() {
        tvJugador1.setEnabled(true);
        tvJugador2.setEnabled(true);
        tvJugador3.setEnabled(true);
    }
    void bloquearCartas() {
        tvJugador1.setEnabled(false);
        tvJugador2.setEnabled(false);
        tvJugador3.setEnabled(false);
    }

    void resetTurno() {
        turno = null;
        idJugador1 = null;
        idJugador2 = null;
    }

    void cambiarTurno() {
        cambiarBarraProgreso();
        animarDesaparecerMenu();
        if (mMyId.equals(idJugador1) && turno.equals(idJugador1)) turno = idJugador2;
        if (mMyId.equals(idJugador2) && turno.equals(idJugador2)) turno = idJugador1;

    }
    void inicializarBarraProgreso() {

        mCountDownTimerJ1 = new CountDownTimer(40000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                segundos--;
                progressBar1.setProgress(segundos);

            }

            @Override
            public void onFinish() {
                //Do what you want
                segundos--;
                progressBar1.setProgress(segundos);
            }
        };
        mCountDownTimerJ2 = new CountDownTimer(40000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                segundos--;
                progressBar2.setProgress(segundos);

            }

            @Override
            public void onFinish() {
                //Do what you want
                segundos--;
                progressBar2.setProgress(segundos);
            }
        };
    }


    void cambiarBarraProgreso(){
        if(progressBar1.getVisibility() == View.VISIBLE){
            progressBar1.setVisibility(View.INVISIBLE);
            mCountDownTimerJ1.cancel();
            segundos = 40;
            progressBar2.setVisibility(View.VISIBLE);
            mCountDownTimerJ2.start();
        }else if(progressBar2.getVisibility() == View.VISIBLE){
            progressBar2.setVisibility(View.INVISIBLE);
            mCountDownTimerJ2.cancel();
            segundos = 40;
            progressBar1.setVisibility(View.VISIBLE);
            mCountDownTimerJ1.start();
        }
    }
    void reiniciarBarraProgreso(){
        if(progressBar1.getVisibility() == View.VISIBLE){
            mCountDownTimerJ1.cancel();
            segundos = 40;
            mCountDownTimerJ1.start();
        }else if(progressBar2.getVisibility() == View.VISIBLE){
            mCountDownTimerJ2.cancel();
            segundos = 40;
            mCountDownTimerJ2.start();
        }
    }
    void animacionAbrirCartas(){
        tvJugador1.animate().rotation(-20).setDuration(750);
        tvJugador3.animate().rotation(20).setDuration(750);

        tvJugador1.animate().translationX(inicio1.x - 100).setDuration(750);
        tvJugador3.animate().translationX(inicio3.x + 100).setDuration(750);
        tvJugador1.animate().translationY(inicio1.y + 40).setDuration(750);
        tvJugador3.animate().translationY(inicio3.y + 40).setDuration(750);
    }
    void animacionRival(View view){
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) view.getLayoutParams();

        if(view.equals(tvMesaRival1)) {
            view.setX(view.getX() + 150);
            view.setY(view.getY() - params.height - params.topMargin);

            view.setVisibility(View.VISIBLE);
            view.animate().translationX(inicio1Rival.x).scaleX((float) 0.8).setDuration(500);
            view.animate().translationY(inicio1Rival.y).scaleY((float) 0.8).setDuration(500);
            //view.animate().rotationXBy(30).rotationYBy(-5).rotation(5).setDuration(500);

            view.bringToFront();
            hayAnimacionRival1 = true;
            Log.d("KKKKKK","hay animacion carta rival 1 = true");
        }else if(view.equals(tvMesaRival2)) {
            view.setX(view.getX());
            view.setY(view.getY() - params.height - params.topMargin);

            view.setVisibility(View.VISIBLE);
            view.animate().translationX(inicio2Rival.x).scaleX((float) 0.8).setDuration(500);
            view.animate().translationY(inicio2Rival.y).scaleY((float) 0.8).setDuration(500);
            //view.animate().rotationXBy(30);

            view.bringToFront();
            hayAnimacionRival2 = true;
            Log.d("KKKKKK","hay animacion carta rival 2 = true");
        }else if(view.equals(tvMesaRival3)) {
            view.setX(view.getX() - 150);
            view.setY(view.getY() - params.height - params.topMargin);

            view.setVisibility(View.VISIBLE);
            view.animate().translationX(inicio3Rival.x).scaleX((float) 0.8).setDuration(500);
            view.animate().translationY(inicio3Rival.y).scaleY((float) 0.8).setDuration(500);
            //view.animate().rotationXBy(30).rotationYBy(5).rotation(-5).setDuration(500);

            view.bringToFront();
            hayAnimacionRival3 = true;
            Log.d("KKKKKK","hay animacion carta rival 3 = true");
        }
    }
    void animarAparecerMenu(){
        actionButton.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fab_scale_up);
        actionButton.startAnimation(animation);
    }
    void animarDesaparecerMenu(){
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fab_scale_down);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                actionButton.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        actionButton.startAnimation(animation);
    }

    // Start the gameplay phase of the game.
    void startGame(boolean multiplayer) {

        asignarImagenCarta(carta1, tvJugador1);
        asignarImagenCarta(carta2, tvJugador2);
        asignarImagenCarta(carta3, tvJugador3);

        tvJugador1.setOnTouchListener(new MyTouchListener());
        tvJugador2.setOnTouchListener(new MyTouchListener());
        tvJugador3.setOnTouchListener(new MyTouchListener());

        truc.setOnClickListener(menuListener);
        retruque.setOnClickListener(menuListener);
        quatreVal.setOnClickListener(menuListener);
        jocFora.setOnClickListener(menuListener);
        envid.setOnClickListener(menuListener);
        laFalta.setOnClickListener(menuListener);
        salir.setOnClickListener(menuListener);
        meVoy.setOnClickListener(menuListener);


        if (mMyId.equals(turno) && ronda == 1) {
            Toast.makeText(getApplicationContext(), "Es tu turno", Toast.LENGTH_SHORT).show();
            animacionAbrirCartas();
            progressBar1.setVisibility(View.VISIBLE);
            progressBar2.setVisibility(View.INVISIBLE);
            inicializarBarraProgreso();
            mCountDownTimerJ1.start();
            animarAparecerMenu();
        }

        if (!mMyId.equals(turno) && ronda == 1) {
            tvJugador1.setEnabled(false);
            tvJugador2.setEnabled(false);
            tvJugador3.setEnabled(false);
            Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
            animacionAbrirCartas();
            progressBar2.setVisibility(View.VISIBLE);
            progressBar1.setVisibility(View.INVISIBLE);
            inicializarBarraProgreso();
            mCountDownTimerJ2.start();
            animarDesaparecerMenu();

        }

        switchToScreen(R.id.screen_game);
        mMultiplayer = multiplayer;

    }

    void cartaSeleccionada() {

        //Envio el valor de la carta
        byte[] messageCarta = ("$" + aux.toString()).getBytes();
        enviarValorCarta(messageCarta);
        Log.d("KKKKK", "Ronda: " + ronda);
        Log.d("KKKKK", "valor1: " + valor1);
        Log.d("KKKKK", "valor2: " + valor2);
        Log.d("KKKKK", "valor3: " + valor3);



        Log.d("KKKKK", "Tiro primero? " + tiroPrimero());
        //Caso en el que tiro primero
        if(tiroPrimero()){
            cambiarTurno();
            Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
            bloquearCartas();
            enviarMensajeTurno();

        //Caso en que tiro segundo
        }else {
            //No hay empate
            if(!hayEmpate()){
                //Gano la ronda
                if(soyGanadorRonda()){
                    if (ronda == 1) {
                        misRondasGanadas = 1;
                        ganadorRonda1 = mMyId;
                    }else if (ronda == 2){
                        misRondasGanadas++;
                    }else if (ronda == 3){
                        misRondasGanadas = 2;
                    }
                    //He ganado la mano
                    if (misRondasGanadas == 2) {
                        //enviarMensajeHasPerdido();
                        mostrarResultadosGanadorMano("PRIMERO");
                    //Vuelvo a tirar
                    }else {
                        Toast.makeText(getApplicationContext(), "Es tu turno", Toast.LENGTH_SHORT).show();
                        actualizaRonda();
                        enviarMensajeRonda();
                        enviarMensajeTurno();
                        reiniciarBarraProgreso();
                    }
                //Pierdo la ronda
                }else {
                    //enviarMensajeSumaRonda();
                    if ((ronda == 2 && misRondasGanadas == 0) || (ronda == 3)) {
                        //Pierdes en la segunda ronda o en la tercera
                        mostrarResultadosPerdedorMano("PRIMERO");
                    }else {
                        cambiarTurno();
                        Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
                        bloquearCartas();
                        actualizaRonda();
                        enviarMensajeRonda();
                        enviarMensajeTurno();
                    }
                }
            //Hay empate
            }else {
                if (ronda == 1) {
                    enviarMensajeHayEmpate();
                    casoEmpatePrimero();
                } else {
                    casoEmpateTercero();
                }
            }
        }










/*
        //Caso en el que NO hay empate
        if (!hayEmpate()) {}
            //Compruebo si tiro primero
            tiroPrimero();
           //Caso en el que ganas tirando segundo
            if (soyGanadorRonda()) {
                if (ronda == 1) {
                    misRondasGanadas = 1;
                    ganadorRonda1 = mMyId;
                }
                if (ronda == 2 && misRondasGanadas == 1) misRondasGanadas = 2;
                else if (ronda == 2 && misRondasGanadas == 0) misRondasGanadas = 1;
                if (ronda == 3) misRondasGanadas = 2;
                //actualizaRonda();
                enviarMensajeRonda();

                //He ganado la partida
                if (misRondasGanadas == 2) {
                    enviarMensajeHasPerdido();
                    mostrarResultadosGanadorMano();

                } else {
                    Toast.makeText(getApplicationContext(), "Es tu turno", Toast.LENGTH_SHORT).show();
                    casoTiroPrimero = false;
                    actualizaRonda();
                    //Comunicar el turno al oponente, gane o pierda
                    enviarMensajeTurno();
                    reiniciarBarraProgreso();
                }

                //Caso en el que tiras primero, o pierdes tirando segundo
            } else {
                //Caso en el que pierdo tirando segundo
                if (!casoTiroPrimero) {
                    //Enviar mensaje para que el jugaddor sume rondas ganadas
                    enviarMensajeSumaRonda();
                }
                //Caso continuo
                if ((ronda == 2 && misRondasGanadas == 0) || (ronda == 3 && misRondasGanadas < 2) && !casoTiroPrimero) {
                    enviarMensajeSumaRonda();
                    //Pierdes en la segunda ronda o en la tercera
                    enviarMensajeSumaRonda();
                    mostrarResultadosPerdedorMano();
                } else {
                    //Caso en el que tiras primero o todavia no hay ganador");
                    cambiarTurno();
                    Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
                    tvJugador1.setEnabled(false);
                    tvJugador2.setEnabled(false);
                    tvJugador3.setEnabled(false);

                    casoTiroPrimero = false;
                    actualizaRonda();
                    //Comunicar el turno al oponente, gane o pierda
                    enviarMensajeTurno();
                }
            }
           /* if (((ronda == 3 || (!soyGanadorRonda() && ronda == 2)) && misRondasGanadas < 2 && !casoTiroPrimero) || misRondasGanadas == 2) {
                return;
            } else {
                casoTiroPrimero = false;
                actualizaRonda();
                //Comunicar el turno al oponente, gane o pierda
                enviarMensajeTurno();
            }
        } else {
            //Caso en el que hay empate
            if (ronda == 1) {
                enviarMensajeHayEmpate();
                casoEmpatePrimero();
            } else {
                enviarMensajeHayEmpate();
                casoEmpateTercero();
            }
       */

    }

    void casoEmpatePrimero() {
        showBasicAlert("Empate en la primera ronda!", "La carta que elijas ser� mostrada arriba");
        //Si no soy mano, cambio turno
        if (!mMyId.equals(mano)) {
            Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
            bloquearCartas();
            cambiarTurno();
            enviarMensajeTurno();
        }
        //Si soy mano espero a carta seleccionada tras empate
    }

    void casoEmpateTercero() {
        Log.d("JJJJJJ", "ganador1ronda: "+ganadorRonda1+" MIid: "+mMyId);
        //Caso en el que gano
        if (ganadorRonda1.equals(mMyId)) {
            //enviarMensajeHasPerdido();
            mostrarResultadosGanadorMano("PRIMERO");

            //Caso en el que pierdo
        } else {
            //enviarMensajeSumaRonda();
            mostrarResultadosPerdedorMano("PRIMERO");
        }
    }

    void cartaSeleccionadaEmpate() {

        byte[] messageCarta = ("$" + aux.toString()).getBytes();
        enviarValorCarta(messageCarta);
        // Si soy mano, tiro y cambio turno
        if (mMyId.equals(mano)) {
            cambiarTurno();
            enviarMensajeTurno();
            Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
            //Si no soy mano, compruebo quien gana
        } else if (soyGanadorRondaEmpate()) {
            //Caso en el que gano
            //enviarMensajeHasPerdido();
            mostrarResultadosGanadorMano("PRIMERO");

        } else {
            //Caso en el que pierdo
            //enviarMensajeSumaRonda();
            mostrarResultadosPerdedorMano("PRIMERO");
        }
    }

    /*
     * ALGUNOS METODOS
     *
     *
     *
     *
     */
    boolean tiroPrimero() {
        if (ronda == 1 && valor1 == 0) return true;
        else if (ronda == 2 && valor2 == 0) return true;
        else if (ronda == 3 && valor3 == 0) return true;
        return false;
    }

    boolean soyGanadorRonda() {
        //Ronda 1
        if (ronda == 1 && miValor > valor1) {
            return true;
        }
        //Ronda 2
        if (ronda == 2 && miValor > valor2) {
            return true;
        }
        //Ronda 3
        if (ronda == 3 && miValor > valor3) {
            return true;
        }
        return false;
    }

    boolean soyGanadorRondaEmpate() {
        if (miValor > valorEmpate) {
            return true;
        }
        return false;
    }

    boolean hayEmpate() {
        if (ronda == 1 && miValor == valor1) {
            hayEmpate = true;
            return true;
        }
        if (ronda == 3 && miValor == valor3) {
            hayEmpate = true;
            return true;
        }
        return false;
    }

    void actualizaRonda() {
        if(ronda < 3 ) ronda++;

    }

    public void cerrarDialogoAndStart(int milisegundos) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // acciones que se ejecutan tras los milisegundos
                repartiendo.cancel();
                startGame(true);

            }
        }, milisegundos);
    }

    public void mostrarResultadosGanadorMano(String contador) {
        Log.d("FFFFFFF", "Hay truc? "+hayTruc);
        Log.d("FFFFFFF", "Hay retruc? "+hayRetruc);
        Log.d("FFFFFFF", "Hay cuatre? "+hayCuatreVal);
        Log.d("FFFFFFF", "Hay joc? "+hayJocFora);
        Log.d("FFFFFFF", "Hay envid? "+hayEnvid);

        if (!hayTruc) puntosTruc = NO_QUIERO_TRUC;
        if (hayTruc && !hayRetruc) puntosTruc = TRUC;
        if (hayRetruc && !hayCuatreVal) puntosTruc = RETRUC;
        if (hayCuatreVal && !hayJocFora) puntosTruc = CUATRE_VAL;
        if (hayJocFora) puntosTruc = 24;

        puntosTotalesMios += (puntosTruc + puntosEnvid);
        marcador.setText("Yo: "+puntosTotalesMios);
        actualizarMarcador2(puntosTotalesMios, "GANADOR", contador);

    }

    public void mostrarResultadosPerdedorMano(String contador) {
        puntosTotalesMios += puntosEnvid;
        marcador.setText("Yo: "+puntosTotalesMios);
        actualizarMarcador2(puntosTotalesMios, "PERDEDOR", contador);

    }

    String comprobarGanadorPartida(){
        if(puntosTotalesMios >= 24){
            return "YO";
        }else if(puntosTotalesJugador2 >= 24){
            return "RIVAL";
        }
        return "NADIE";
    }



    /*  MESSAGE SECTION LEYENDA
     * E: Mensaje de hay empate.
     * R: Mensaje de ronda.
     * W: Mensaje indicador de que he ganado la mano.
     * G: Indica que he perdido la ronda y el otro jugador debe de sumar rondas ganadas.
     * S: Mensaje de repartir las cartas.
     * M: Mensaje de actualizar la mano.
     * N: Mensaje indicador de que he envidado.
     * K: Mensaje de envid aceptado y notificaci�n de ganador de envid.
     * V: Mensaje que indica que vuelvo a envidar (cuando mi rival me ha envidado).
     * X: Mensaje de que NO quiero envid o truc (Envid -> 1, Truc -> 2).
     * F: Mensaje de la falta del envid.
     * T: Mensaje de truco.
     * Q: Mensaje de quiero truc cuando me han trucado.
     */

    public void enviarValorCarta(byte[] messageCarta) {
        byte[] message = messageCarta;
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, message,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void enviarMensajeRonda() {
        byte[] messageRonda = ("R " + String.valueOf(ronda)).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageRonda,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void enviarMensajeTurno() {
        byte[] messageTurno = turno.getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageTurno,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void enviarMensajeHasPerdido() {
        byte[] messageGanadorPartida = "W".getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageGanadorPartida,
                        mRoomId, p.getParticipantId());
                Log.d("LLLLLLL", "He ganado la ronda, y la partida");
            }
        }
    }

    public void enviarMensajeHayEmpate() {
        byte[] messageEmpatePrimero = ("E").getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageEmpatePrimero,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void enviarMensajeSumaRonda() {
        byte[] messageGanadorRonda = "G".getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageGanadorRonda,
                        mRoomId, p.getParticipantId());
                Log.d("LLLLLLL", "No tiro primero, envio mensaje G comunicando que sume rondasGanadas");
            }
        }
    }

    public void enviarMensajeRepartir() {
        byte[] messageRepartir = ("S" + " " + sCartasJ2).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageRepartir,
                        mRoomId, p.getParticipantId());

            }
        }
    }

    public void enviarMensajeMano(String nuevaMano) {
        byte[] messageMano = ("M " + nuevaMano).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageMano,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void enviarMensajeEnvid(int envid) {
        byte[] messageEnvid = ("N " + envid).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageEnvid,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void enviarMensajeHayEnvidAndGanador(String ganador, int caso) {
        byte[] messageEnvid = ("K " + ganador + " " + miEnvid + " " + caso).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageEnvid,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void enviarMensajeVuelvoAEnvidar() {
        byte[] messageVuelvo = ("V " + miEnvid).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageVuelvo,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void enviarMensajeNoQuiero(int caso) {
        byte[] messageNoQuiero = ("X " + caso).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageNoQuiero,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void enviarMensajeLaFalta(int caso) {
        byte[] messageFalta = ("F " + miEnvid +" " +caso).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageFalta,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void enviarMensajeTruc() {
        byte[] messageTruco = ("T").getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageTruco,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void enviarMensajeQuieroTruc() {
        byte[] messageQuieroTruc = ("Q").getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageQuieroTruc,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void enviarMensajeRetruc() {
        byte[] messageRetruc = ("L").getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageRetruc,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void enviarMensajeQuieroRetruc() {
        byte[] messageQuieroRetruc = ("I").getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageQuieroRetruc,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void enviarMensajeCuatreVal() {
        byte[] messageCuatreVal = ("C").getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageCuatreVal,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void enviarMensajeQuieroCuatreVal() {
        byte[] messageQuieroCuatreVal = ("B").getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageQuieroCuatreVal,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void enviarMensajeJocFora() {
        byte[] messageJocFora = ("J").getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageJocFora,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void enviarMensajeQuieroJocFora() {
        byte[] messageQuieroJocFora = ("Y").getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageQuieroJocFora,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void enviarMensajeNoQuieroTruc(int caso) {
        byte[] messageNoQuieroTruc = ("D " + caso).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageNoQuieroTruc,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void actualizarMarcador2(int puntosTotales, String usuario, String contador) {
        byte[] messageMarcador = ("Z " + puntosTotales +" " +usuario+" "+contador).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageMarcador,
                        mRoomId, p.getParticipantId());
            }
        }
    }








    /*
     * COMMUNICATIONS SECTION. Methods that implement the game's network
     * protocol.
     */

    // Score of other participants. We update this as we receive their scores
    // from the network.
    Map<String, Integer> mParticipantScore = new HashMap<String, Integer>();

    // Participants who sent us their final score.
    Set<String> mFinishedParticipants = new HashSet<String>();

    // Called when we receive a real-time message from the network.
    // Messages in our game are made up of 2 bytes: the first one is 'F' or 'U'
    // indicating
    // whether it's a final or interim score. The second byte is the score.
    // There is also the
    // 'S' message, which indicates that the game should start.
    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        byte[] buf = rtm.getMessageData();
        String sender = rtm.getSenderParticipantId();
        try {
            switch (buf[0]) {
                case '$':
                    String sBuf = new String(buf, "UTF-8");
                    String arrayBuf[] = sBuf.split(" ");
                    String palo = arrayBuf[1];
                    String numeroCarta = arrayBuf[0].substring(1);
                    Carta newCarta = new Carta(numeroCarta, palo, arrayBuf[2]);
                    int valor = Integer.parseInt(arrayBuf[2]);

                    if (tvMesaRival1.getVisibility() == View.INVISIBLE) {
                        if (hayEmpate) {
                            valorEmpate = valor;
                        } else valor1 = valor;
                        asignarImagenCarta(newCarta, tvMesaRival1);
                        animacionRival(tvMesaRival1);
                        Log.d("KKKKK", "Animando la carta rival 1");
                    } else if (tvMesaRival2.getVisibility() == View.INVISIBLE) {
                        if (hayEmpate) {
                            valorEmpate = valor;
                        } else valor2 = valor;
                        asignarImagenCarta(newCarta, tvMesaRival2);
                        animacionRival(tvMesaRival2);
                        Log.d("KKKKK", "Animando la carta rival 2");
                    } else if (tvMesaRival3.getVisibility() == View.INVISIBLE) {
                        if (hayEmpate) {
                            valorEmpate = valor;
                        } else valor3 = valor;
                        asignarImagenCarta(newCarta, tvMesaRival3);
                        animacionRival(tvMesaRival3);
                        Log.d("KKKKK", "Animando la carta rival 3");
                    }
                    break;
                case 'R':
                    actualizaRonda();
                    break;
                case 'W':
                    //Caso en que pierdo
                    //mostrarResultadosPerdedorMano();
                    break;
                case 'G':
                    //Si ha habido empate
                    Log.d("TTTTTT", "SUmo mis rondas ganadas");
                    if (hayEmpate) {
                       // mostrarResultadosGanadorMano();
                    } else {
                        //Sumo rondas ganadas
                        if (ronda == 1) {
                            ganadorRonda1 = mMyId;
                        }
                        misRondasGanadas++;
                        //Si llego a 2 he ganado
                        if (misRondasGanadas == 2) {
                          //  mostrarResultadosGanadorMano();
                        }
                    }
                    break;
                case 'E':
                    //Mensaje que actualiza si hay empate en la primera ronda
                    if (ronda == 1) showBasicAlert("Empate en la primera ronda!", "La carta que eljas será mostrada arriba");
                    hayEmpate = true;
                    break;

                case 'S':
                    resetAll();
                    desbloquearCartas();
                    String ssBuf = new String(buf, "UTF-8");
                    String[] arrayCartasJ2 = ssBuf.split(" ");
                    numCarta[0] = Integer.parseInt(arrayCartasJ2[1]);
                    numCarta[1] = Integer.parseInt(arrayCartasJ2[2]);
                    numCarta[2] = Integer.parseInt(arrayCartasJ2[3]);
                    repartir(numCarta);
                    carta1 = new Carta(manoJugador.get(0).getNumero(), manoJugador.get(0).getPalo(), manoJugador.get(0).getValor());
                    carta2 = new Carta(manoJugador.get(1).getNumero(), manoJugador.get(1).getPalo(), manoJugador.get(1).getValor());
                    carta3 = new Carta(manoJugador.get(2).getNumero(), manoJugador.get(2).getPalo(), manoJugador.get(2).getValor());
                    cerrarDialogoAndStart(5000);
                    break;

                case 'M':
                    String aux = new String(buf, "UTF-8");
                    String nuevaMano[] = aux.split(" ");
                    mano = nuevaMano[1];
                    turno = mano;
                    inicializarMano();
                    break;

                case 'N':
                    String aux1 = new String(buf, "UTF-8");
                    String suEnvid[] = aux1.split(" ");
                    envidOtro = Integer.parseInt(suEnvid[1]);
                    showSingleChoiceAlertEnvid("Tu rival ha envidado", R.array.envid);
                    envid.setVisibility(View.GONE);
                    laFalta.setVisibility(View.GONE);
                    cambiarBarraProgreso();
                    break;

                case 'K':
                    String aux2 = new String(buf, "UTF-8");
                    String ganador[] = aux2.split(" ");
                    hayEnvid = true;
                    ganadorEnvid = ganador[1];
                    envidOtro = Integer.parseInt(ganador[2]);
                    int caso = Integer.parseInt(ganador[3]);
                    switch (caso) {
                        case 1:
                            textoAccion2.setText("Quiero el envid");
                            animarTextoAccion(textoAccion2);
                            cambiarBarraProgreso();
                            break;
                        case 2:
                            textoAccion2.setText("Quiero el vuelvo");
                            animarTextoAccion(textoAccion2);
                            reiniciarBarraProgreso();
                            break;
                        case 3:
                            textoAccion2.setText("Quiero la falta");
                            animarTextoAccion(textoAccion2);
                            if (!mMyId.equals(turno)) {
                                reiniciarBarraProgreso();
                            }else cambiarBarraProgreso();
                            break;
                    }
                    if (ganadorEnvid.equals(mMyId) && caso == 1) puntosEnvid = ENVID;
                    if (ganadorEnvid.equals(mMyId) && caso == 2) puntosEnvid = TORNE;
                    if (ganadorEnvid.equals(mMyId) && caso == 3) puntosEnvid = 24;

                    if (mMyId.equals(turno)) {
                        desbloquearCartas();
                    }

                    break;

                case 'V':
                    String aux3 = new String(buf, "UTF-8");
                    String otro[] = aux3.split(" ");
                    envidOtro = Integer.parseInt(otro[1]);
                    hayVuelvo = true;
                    showSingleChoiceAlertVuelvo("Tu rival ha vuelto a envidar", R.array.envid2);
                    cambiarBarraProgreso();
                    break;

                case 'X':
                    String aux4 = new String(buf, "UTF-8");
                    String otro1[] = aux4.split(" ");
                    int caso1 = Integer.parseInt(otro1[1]);
                    switch (caso1) {
                        case 1:
                            puntosEnvid = NO_QUIERO_ENVID;
                            textoAccion2.setText("No quiero el envid");
                            animarTextoAccion(textoAccion2);
                            cambiarBarraProgreso();
                            break;
                        case 2:
                            puntosEnvid = ENVID;
                            textoAccion2.setText("No quiero el vuelvo");
                            animarTextoAccion(textoAccion2);
                            reiniciarBarraProgreso();
                            break;
                        case 3:
                            if(hayVuelvo){
                                puntosEnvid = TORNE;
                            }
                            textoAccion2.setText("No quiero la falta");
                            animarTextoAccion(textoAccion2);
                            if (!mMyId.equals(turno)) {
                                reiniciarBarraProgreso();
                            }else cambiarBarraProgreso();
                            break;
                        case 4:

                            puntosEnvid = NO_QUIERO_ENVID;
                            textoAccion2.setText("No quiero la falta");
                            animarTextoAccion(textoAccion2);
                            cambiarBarraProgreso();
                            break;

                    }
                    if (mMyId.equals(turno)) {
                        desbloquearCartas();
                    }

                    break;

                case 'D':
                    String aux7 = new String(buf, "UTF-8");
                    String otro4[] = aux7.split(" ");
                    int caso2 = Integer.parseInt(otro4[1]);
                    switch (caso2) {
                        case 1:
                            textoAccion2.setText("No quiero el truc");
                            animarTextoAccion(textoAccion2);
                            hayTruc = false;
                            break;
                        case 2:
                            textoAccion2.setText("No quiero el retruc");
                            animarTextoAccion(textoAccion2);
                            hayTruc = true;
                            break;
                        case 3:
                            textoAccion2.setText("No quiero el quatre val");
                            animarTextoAccion(textoAccion2);
                            hayRetruc = true;
                            break;
                        case 4:
                            textoAccion2.setText("No quiero el joc fora");
                            animarTextoAccion(textoAccion2);
                            hayCuatreVal = true;
                            break;
                    }
                    mostrarResultadosGanadorMano("PRIMERO");
                    break;

                case 'F':
                    String aux5 = new String(buf, "UTF-8");
                    String otro2[] = aux5.split(" ");
                    envidOtro = Integer.parseInt(otro2[1]);
                    if(Integer.parseInt(otro2[2]) == 2) faltaDirecta = true;
                    showSingleChoiceAlertFalta("Tu rival ha envidado la falta", R.array.envid3);
                    envid.setVisibility(View.GONE);
                    laFalta.setVisibility(View.GONE);
                    cambiarBarraProgreso();
                    break;

                case 'T':
                    showSingleChoiceAlertTruco("Tu rival ha trucado", R.array.truc1);
                    truc.setVisibility(View.GONE);
                    cambiarBarraProgreso();
                    break;

                case 'Q':
                    hayTruc = true;
                    textoAccion2.setText("Quiero el truc");
                    animarTextoAccion(textoAccion2);

                    if (mMyId.equals(turno)) {
                        desbloquearCartas();
                    }
                    cambiarBarraProgreso();
                    break;

                case 'L':
                    showSingleChoiceAlertRetruc("Tu rival ha retrucado", R.array.truc2);
                    cambiarBarraProgreso();
                    break;

                case 'I':
                    hayRetruc = true;
                    textoAccion2.setText("Quiero el retruc");
                    animarTextoAccion(textoAccion2);

                    if(turno.equals(mMyId)){
                        desbloquearCartas();
                        cambiarBarraProgreso();
                    }else reiniciarBarraProgreso();
                    break;

                case 'C':
                    showSingleChoiceAlertCuatreVal("�Cuatre val!", R.array.truc3);
                    cambiarBarraProgreso();
                    break;

                case 'B':
                    hayCuatreVal = true;
                    textoAccion2.setText("Quiero el cuatre val");
                    animarTextoAccion(textoAccion2);

                    if(turno.equals(mMyId)){
                        cambiarBarraProgreso();
                        desbloquearCartas();
                    }else reiniciarBarraProgreso();
                    break;

                case 'J':
                    showSingleChoiceAlertJocFora("�Joc fora!", R.array.envid3);
                    cambiarBarraProgreso();
                    break;

                case 'Y':
                    hayJocFora = true;
                    textoAccion2.setText("Quiero el joc fora");
                    animarTextoAccion(textoAccion2);

                    if(turno.equals(mMyId)){
                        cambiarBarraProgreso();
                        desbloquearCartas();
                    }else reiniciarBarraProgreso();
                    break;

                case 'Z':
                    String aux8 = new String(buf, "UTF-8");
                    String otro5[] = aux8.split(" ");
                    puntosTotalesJugador2 = Integer.parseInt(otro5[1]);
                    String quien = otro5[2];
                    String contador = otro5[3];
                    marcador2.setText("Rival: "+puntosTotalesJugador2);

                    //Para que actualice los puntos antes de comprobar quien es el ganador
                    //Primero y segundo para que no entre en un bucle
                    if(quien.equals("PERDEDOR")){
                        if(contador.equals("PRIMERO")) mostrarResultadosGanadorMano("SEGUNDO");
                    }else if(quien.equals("GANADOR")){
                        if(contador.equals("PRIMERO")) mostrarResultadosPerdedorMano("SEGUNDO");
                    }

                    Log.d("HHHHHH", "Mis puntos: " + puntosTotalesMios);
                    Log.d("HHHHHH", "Puntos rival: " + puntosTotalesJugador2);

                    String ganadorFinal = comprobarGanadorPartida();
                    Log.d("HHHHHH", "Ganador: "+ganadorFinal);
                    if(!ganadorFinal.equals("NADIE")){
                        if(ganadorFinal.equals("YO")){

                            /*
                            if(quien.equals("GANADOR")){
                                if(contador.equals("PRIMERO"))mostrarResultadosPerdedorMano("SEGUNDO");
                            }else if(quien.equals("PERDEDOR")){
                                if(contador.equals("PRIMERO"))mostrarResultadosGanadorMano("SEGUNDO");
                            } */
                            switchToScreen(R.id.screen_win);

                        } else if (ganadorFinal.equals("RIVAL")){

                            /*
                            if(quien.equals("GANADOR")){
                                if(contador.equals("PRIMERO"))mostrarResultadosPerdedorMano("SEGUNDO");
                            }else if(quien.equals("PERDEDOR")){
                                if(contador.equals("PRIMERO"))mostrarResultadosGanadorMano("SEGUNDO");
                            }*/
                            switchToScreen(R.id.screen_lost);
                        }
                    }else {


                        //Lo recibe el ganador
                        if(quien.equals("PERDEDOR")){
                            if (hayEnvid) {
                                LayoutInflater inflater = getLayoutInflater();
                                ViewGroup container = null;

                                if (ganadorEnvid.equals(mMyId)) {
                                    View layout = inflater.inflate(R.layout.progres_content, container);
                                    showProgressCustomDialog(layout);

                                } else {
                                    View layout = inflater.inflate(R.layout.progres_content2, container);
                                    showProgressCustomDialog(layout);
                                }
                            } else {
                                showProgressDialog("Enhorabuena, ganas la mano");
                            }
                            repartirTrasMano();
                            Log.d("KKKKKKKK", "Repartiendo tras mano..." + mMyId);





                        //Lo recibe el perdedor
                        }else if(quien.equals("GANADOR")){
                            if (hayEnvid) {
                                LayoutInflater inflater = getLayoutInflater();
                                ViewGroup container = null;

                                if (ganadorEnvid.equals(mMyId)) {
                                    View layout = inflater.inflate(R.layout.progres_content3, container);
                                    showProgressCustomDialog(layout);

                                } else {
                                    View layout = inflater.inflate(R.layout.progres_content4, container);
                                    showProgressCustomDialog(layout);
                                }
                            } else {
                                showProgressDialog("Lástima, pierdes la mano");
                            }
                            repartirTrasMano();
                            Log.d("KKKKKKKK", "Repartiendo tras mano..." + mMyId);
                        }




                    }
                    break;



                default:

                    String turnoNuevo = new String(buf, "UTF-8");
                    turno = turnoNuevo;

                    if (mMyId.equals(turno) && misRondasGanadas < 2) {
                        if(ronda > 1 && !hayEnvid){
                            envid.setVisibility(View.GONE);
                            laFalta.setVisibility(View.GONE);
                        }
                        desbloquearCartas();
                        Toast.makeText(getApplicationContext(), "Es tu turno", Toast.LENGTH_SHORT).show();
                        cambiarBarraProgreso();
                        animarAparecerMenu();
                    }

                    if (!mMyId.equals(turno) && misRondasGanadas < 2) {
                        bloquearCartas();
                        envid.setVisibility(View.GONE);
                        laFalta.setVisibility(View.GONE);

                        Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
                        Log.d("LLLLLLL", "Me han comunicado cambio de turno");
                        reiniciarBarraProgreso();

                    }
                    break;

            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
/*
        if (buf[0] == 'F' || buf[0] == 'U') {
            // score update.
            int existingScore = mParticipantScore.containsKey(sender) ?
                    mParticipantScore.get(sender) : 0;
            int thisScore = (int) buf[1];
            if (thisScore > existingScore) {
                // this check is necessary because packets may arrive out of
                // order, so we
                // should only ever consider the highest score we received, as
                // we know in our
                // game there is no way to lose points. If there was a way to
                // lose points,
                // we'd have to add a "serial number" to the packet.
                mParticipantScore.put(sender, thisScore);
            }

            // update the scores on the screen
            updatePeerScoresDisplay();

            // if it's a final score, mark this participant as having finished
            // the game
            if ((char) buf[0] == 'F') {
                mFinishedParticipants.add(rtm.getSenderParticipantId());
            }
        }*/
    }

    // Broadcast my score to everybody else.
    void broadcastScore(boolean finalScore) {
        if (!mMultiplayer)
            return; // playing single-player mode

        // First byte in message indicates whether it's a final score or not
        mMsgBuf[0] = (byte) (finalScore ? 'F' : 'U');

        // Second byte is the score.
        mMsgBuf[1] = (byte) mScore;

        // Send to every other participant.
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId))
                continue;
            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;
            if (finalScore) {
                // final score notification must be sent via reliable message
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, mMsgBuf,
                        mRoomId, p.getParticipantId());
            } else {
                // it's an interim score notification, so we can use unreliable
                Games.RealTimeMultiplayer.sendUnreliableMessage(mGoogleApiClient, mMsgBuf, mRoomId,
                        p.getParticipantId());
            }
        }
    }

    /*
     * UI SECTION. Methods that implement the game's UI.
     */

    // This array lists everything that's clickable, so we can install click
    // event handlers.
    final static int[] CLICKABLES = {
            R.id.button_accept_popup_invitation, R.id.button_invite_players,
            R.id.button_quick_game, R.id.button_see_invitations, R.id.button_sign_in,
            R.id.button_sign_out, R.id.button_single_player,
            R.id.button_single_player_2
    };

    // This array lists all the individual screens our game has.
    final static int[] SCREENS = {
            R.id.screen_game, R.id.screen_main, R.id.screen_sign_in,
            R.id.screen_wait, R.id.screen_lost, R.id.screen_win
    };
    int mCurScreen = -1;

    void switchToScreen(int screenId) {
        // make the requested screen visible; hide all others.
        for (int id : SCREENS) {
            findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
        }
        mCurScreen = screenId;

        // should we show the invitation popup?
        boolean showInvPopup;
        if (mIncomingInvitationId == null) {
            // no invitation, so no popup
            showInvPopup = false;
        } else if (mMultiplayer) {
            // if in multiplayer, only show invitation on main screen
            showInvPopup = (mCurScreen == R.id.screen_main);
        } else {
            // single-player: show on main screen and gameplay screen
            showInvPopup = (mCurScreen == R.id.screen_main || mCurScreen == R.id.screen_game);
        }
        findViewById(R.id.invitation_popup).setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
    }

    void switchToMainScreen() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            switchToScreen(R.id.screen_main);
        } else {
            switchToScreen(R.id.screen_sign_in);
        }
    }

    // updates the label that shows my score
    void updateScoreDisplay() {
        // ((TextView) findViewById(R.id.my_score)).setText(formatScore(mScore));
    }

    // formats a score as a three-digit number
    String formatScore(int i) {
        if (i < 0)
            i = 0;
        String s = String.valueOf(i);
        return s.length() == 1 ? "00" + s : s.length() == 2 ? "0" + s : s;
    }

    // updates the screen with the scores from our peers
    void updatePeerScoresDisplay() {
        /**((TextView) findViewById(R.id.score0)).setText(formatScore(mScore) + " - Me");
         int[] arr = {
         R.id.score1, R.id.score2, R.id.score3
         };
         int i = 0;

         if (mRoomId != null) {
         for (Participant p : mParticipants) {
         String pid = p.getParticipantId();
         if (pid.equals(mMyId))
         continue;
         if (p.getStatus() != Participant.STATUS_JOINED)
         continue;
         int score = mParticipantScore.containsKey(pid) ? mParticipantScore.get(pid) : 0;
         ((TextView) findViewById(arr[i])).setText(formatScore(score) + " - " +
         p.getDisplayName());
         ++i;
         }
         }

         for (; i < arr.length; ++i) {
         ((TextView) findViewById(arr[i])).setText("");
         }**/
    }

    /*
     * MISC SECTION. Miscellaneous methods.
     */


    // Sets the flag to keep this screen on. It's recommended to do that during
    // the
    // handshake when setting up a game, because if the screen turns off, the
    // game will be
    // cancelled.
    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    public List<Carta> crearBaraja() {
        Carta carta;
        List<Carta> baraja = new ArrayList<>();

        try {
            //Leemos el archivo baraja.xml situado en raw
            BufferedReader reader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.baraja)));
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(reader);
            int eventType = parser.next();
            //Hasta que se acabe el documento
            while (eventType != XmlPullParser.END_DOCUMENT) {
                //Por cada tag "carta"
                if ((eventType == XmlPullParser.START_TAG) && (parser.getName().equalsIgnoreCase("carta"))) {
                    //Recoger informacion de cada carta
                    String numero = parser.getAttributeValue(null, "numero");
                    String palo = parser.getAttributeValue(null, "palo");
                    String valor = parser.getAttributeValue(null, "valor");
                    carta = new Carta(numero, palo, valor);
                    //Por cada question, la a�adimos a la lista
                    baraja.add(carta);
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        //Devolvemos la lista
        return baraja;
    }

    public int[] crearAleatorio() {

        if (mMyId.equals(mano)) {

            list[0] = (int) (Math.random() * 22);
            int aux = (int) (Math.random() * 22);
            while (list[0] == aux) {
                aux = (int) (Math.random() * 22);
            }
            list[1] = aux;
            int aux2 = (int) (Math.random() * 22);
            while (list[0] == aux2 || list[1] == aux2) {
                aux2 = (int) Math.floor(Math.random() * 21);
            }
            list[2] = aux2;
            int aux3 = (int) (Math.random() * 22);
            while (list[0] == aux3 || list[1] == aux3 || list[2] == aux3) {
                aux3 = (int) Math.floor(Math.random() * 21);
            }
            list2[0] = aux3;
            int aux4 = (int) (Math.random() * 22);
            while (list[0] == aux4 || list[1] == aux4 || list[2] == aux4 || list2[0] == aux4) {
                aux4 = (int) Math.floor(Math.random() * 21);
            }
            list2[1] = aux4;
            int aux5 = (int) (Math.random() * 22);
            while (list[0] == aux5 || list[1] == aux5 || list[2] == aux5 || list2[0] == aux5 || list2[1] == aux5) {
                aux5 = (int) (Math.random() * 22);
            }
            list2[2] = aux5;

            sCartasJ2 = list2[0] + " " + list2[1] + " " + list2[2];

        }
        return list;
    }

    public void repartir(int[] numeros) {

        baraja = crearBaraja();
        manoJugador.add(0, baraja.get(numeros[0]));
        manoJugador.add(1, baraja.get(numeros[1]));
        manoJugador.add(2, baraja.get(numeros[2]));

    }

    public void inicializarMano() {
        //Preparando la partida
        resetAll();
        desbloquearCartas();
        //Si soy mano reparto
        if (mMyId.equals(mano)) {
            repartir(crearAleatorio());
            carta1 = new Carta(manoJugador.get(0).getNumero(), manoJugador.get(0).getPalo(), manoJugador.get(0).getValor());
            carta2 = new Carta(manoJugador.get(1).getNumero(), manoJugador.get(1).getPalo(), manoJugador.get(1).getValor());
            carta3 = new Carta(manoJugador.get(2).getNumero(), manoJugador.get(2).getPalo(), manoJugador.get(2).getValor());
            //Mando las cartas
            enviarMensajeRepartir();
            cerrarDialogoAndStart(5000);
        }
        //Sino soy mano, espero las cartas

    }

    public void cambiarMano() {
        if (mano.equals(idJugador1)) {
            mano = idJugador2;
        } else if (mano.equals(idJugador2)) {
            mano = idJugador1;
        }

        turno = mano;}

    public void repartirTrasMano() {
        cambiarMano();
        if(mMyId.equals(mano)){
            inicializarMano();
        }


 /*       if (mMyId.equals(mano)) {
            cambiarMano();
            enviarMensajeMano(mano);
            Log.d("KKKKKKKK", "Soy mano..." + mMyId);
        }else Log.d("KKKKKKKK", "No soy mano..." + mMyId);
   */ }

    public void asignarImagenCarta(Carta carta, ImageView view) {
        String sCarta = carta.getNumero() + carta.getPalo();
        switch (sCarta) {
            case "1bastos":
                view.setImageResource(R.drawable.uno_bastos);
                break;
            case "1espadas":
                view.setImageResource(R.drawable.uno_espadas);
                break;
            case "3oros":
                view.setImageResource(R.drawable.tres_oros);
                break;
            case "3espadas":
                view.setImageResource(R.drawable.tres_espadas);
                break;
            case "3copas":
                view.setImageResource(R.drawable.tres_copas);
                break;
            case "3bastos":
                view.setImageResource(R.drawable.tres_bastos);
                break;
            case "5bastos":
                view.setImageResource(R.drawable.cinco_bastos);
                break;
            case "5copas":
                view.setImageResource(R.drawable.cinco_copas);
                break;
            case "5espadas":
                view.setImageResource(R.drawable.cinco_espadas);
                break;
            case "5oros":
                view.setImageResource(R.drawable.cinco_oros);
                break;
            case "4bastos":
                view.setImageResource(R.drawable.cuatro_bastos);
                break;
            case "4copas":
                view.setImageResource(R.drawable.cuatro_copas);
                break;
            case "4espadas":
                view.setImageResource(R.drawable.cuatro_espadas);
                break;
            case "4oros":
                view.setImageResource(R.drawable.cuatro_oros);
                break;
            case "6bastos":
                view.setImageResource(R.drawable.seis_bastos);
                break;
            case "6copas":
                view.setImageResource(R.drawable.seis_copas);
                break;
            case "6espadas":
                view.setImageResource(R.drawable.seis_espadas);
                break;
            case "6oros":
                view.setImageResource(R.drawable.seis_oros);
                break;
            case "7bastos":
                view.setImageResource(R.drawable.siete_bastos);
                break;
            case "7copas":
                view.setImageResource(R.drawable.siete_copas);
                break;
            case "7espadas":
                view.setImageResource(R.drawable.siete_espadas);
                break;
            case "7oros":
                view.setImageResource(R.drawable.siete_oros);
                break;
        }
    }

    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public LoadProfileImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                if(e.getMessage() != null) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    private final class MyTouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            PointF inicial = new PointF();
            PointF inicio;
            PointF destino = new PointF();
            RelativeLayout.LayoutParams Params =
                    (RelativeLayout.LayoutParams) view.getLayoutParams();

            switch (motionEvent.getAction()) {
                //Al tocar la pantalla
                case MotionEvent.ACTION_DOWN:
                    view.bringToFront();
                    inicial.x = motionEvent.getX();
                    inicial.y = motionEvent.getY();
                    if(view.equals(tvJugador1)) {
                        xDelta = inicial.x - Params.rightMargin;
                        yDelta = inicial.y - Params.topMargin;
                    }else{
                        xDelta = inicial.x - Params.leftMargin;
                        yDelta = inicial.y - Params.topMargin;
                    }

                    break;
                case MotionEvent.ACTION_UP:

                    //Vemos donde colocamos la carta
                    double medioLayout = ((float)view.getLayoutParams().height)/1.2;
                    if (view.getY() < (medioLayout)){
                        if (tvCartaMesa1.getVisibility() == View.INVISIBLE) {
                            destino.x = tvCartaMesa1.getX();
                            destino.y = tvCartaMesa1.getY() + tvCartaMesa1.getHeight();
                            tvCartaMesa1.setVisibility(View.VISIBLE);

                            view.animate().x(destino.x).y(destino.y).rotation(0).setDuration(500);
                            view.animate().rotationXBy(30).scaleX((float) 0.63).scaleY((float) 0.63)
                                    .rotationYBy(-5).rotation(3).setDuration(500);
                            view.setEnabled(false);

                            if(view.equals(tvJugador1)){
                                posTvJugador1 = 1;
                                tvJugador1.bringToFront();
                                tvJugador2.bringToFront();tvJugador3.bringToFront();
                            }else if(view.equals(tvJugador2)){
                                tvJugador2.bringToFront();
                                posTvJugador2 = 1;
                                tvJugador1.bringToFront();tvJugador3.bringToFront();
                            }else if(view.equals(tvJugador3)){
                                posTvJugador3 = 1;
                                tvJugador3.bringToFront();
                                tvJugador1.bringToFront();tvJugador2.bringToFront();
                            }


                        } else if (tvCartaMesa2.getVisibility() == View.INVISIBLE) {
                            destino.x = tvCartaMesa2.getX();
                            destino.y = tvCartaMesa2.getY() + tvCartaMesa2.getHeight();
                            tvCartaMesa2.setVisibility(View.VISIBLE);

                            view.animate().x(destino.x).y(destino.y).rotation(0).rotationXBy(30)
                                    .scaleX((float) 0.63).scaleY((float) 0.63).setDuration(500);
                            view.setEnabled(false);

                            if(view.equals(tvJugador1) && posTvJugador2 == 0){
                                posTvJugador1 = 2;
                                tvJugador1.bringToFront();
                                tvJugador2.bringToFront();Log.d("RRRRRRR", "Actualizada pos2");
                            }else if(view.equals(tvJugador1) && posTvJugador3 == 0){
                                posTvJugador1 = 2;
                                tvJugador1.bringToFront();
                                tvJugador3.bringToFront();Log.d("RRRRRRR", "Actualizada pos2");
                            }else if(view.equals(tvJugador2) && posTvJugador1 == 0){
                                posTvJugador2 = 2;
                                tvJugador2.bringToFront();
                                tvJugador1.bringToFront();Log.d("RRRRRRR", "Actualizada pos2");
                            }else if(view.equals(tvJugador2) && posTvJugador3 == 0){
                                posTvJugador2 = 2;
                                tvJugador2.bringToFront();
                                tvJugador3.bringToFront();Log.d("RRRRRRR", "Actualizada pos2");
                            }else if(view.equals(tvJugador3) && posTvJugador1 == 0){
                                posTvJugador3 = 2;
                                tvJugador3.bringToFront();
                                tvJugador1.bringToFront();Log.d("RRRRRRR", "Actualizada pos2");
                            }else if(view.equals(tvJugador3) && posTvJugador2 == 0){
                                posTvJugador3 = 2;
                                tvJugador3.bringToFront();
                                tvJugador2.bringToFront();Log.d("RRRRRRR", "Actualizada pos2");
                            }


                        } else if (tvCartaMesa3.getVisibility() == View.INVISIBLE) {
                            destino.x = tvCartaMesa3.getX();
                            destino.y = tvCartaMesa3.getY() + tvCartaMesa3.getHeight();
                            tvCartaMesa3.setVisibility(View.VISIBLE);

                            view.animate().x(destino.x).y(destino.y).rotation(0).setDuration(500);
                            view.animate().rotationXBy(30).scaleX((float) 0.63).scaleY((float) 0.63)
                                    .rotationYBy(5).rotation(-3).setDuration(500);
                            view.setEnabled(false);

                            if(view.equals(tvJugador1)){
                                posTvJugador1 = 3;
                                tvJugador1.bringToFront();
                            }else if(view.equals(tvJugador2)){
                                posTvJugador2 = 3;
                                tvJugador2.bringToFront();
                            }else if(view.equals(tvJugador3)){
                                posTvJugador3 = 3;
                                tvJugador3.bringToFront();
                            }
                            Log.d("RRRRRRR", "Actualizada pos3");
                        }

                        //Calculamos su valor para enviarlo
                        if (view.equals(tvJugador1)) {
                            aux = carta1;
                            miValor = Integer.parseInt(carta1.getValor());
                        }
                        if (view.equals(tvJugador2)) {
                            aux = carta2;
                            miValor = Integer.parseInt(carta2.getValor());
                        }
                        if (view.equals(tvJugador3)) {
                            aux = carta3;
                            miValor = Integer.parseInt(carta3.getValor());
                        }

                        envid.setVisibility(View.GONE);
                        laFalta.setVisibility(View.GONE);

                        if (!hayEmpate) {
                            cartaSeleccionada();
                        } else cartaSeleccionadaEmpate();

                        hayAnimaciones = true;

                    }else {
                        if (view.equals(tvJugador1)) {
                            view.animate().translationX(inicio1.x - 100).setDuration(500);
                            view.animate().translationY(inicio1.y + 40);
                        }
                        if (view.equals(tvJugador2)) {
                            view.animate().translationX(inicio2.x).setDuration(500);
                            view.animate().translationY(inicio2.y).setDuration(500);
                        }
                        if (view.equals(tvJugador3)) {
                            view.animate().translationX(inicio3.x + 100).setDuration(500);
                            view.animate().translationY(inicio3.y + 40).setDuration(500);
                        }
                    }
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    //No hace falta utilizarlo
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    //No hace falta utilizarlo
                    break;
                case MotionEvent.ACTION_MOVE:
                    inicio = new PointF(view.getX(), view.getY());
                    PointF move = new PointF(motionEvent.getX() - inicial.x, motionEvent.getY() - inicial.y);
                    view.setX((inicio.x + move.x) - xDelta);
                    view.setY((inicio.y + move.y) - yDelta);
                    break;
            }
            return true;
        }
    }



}
