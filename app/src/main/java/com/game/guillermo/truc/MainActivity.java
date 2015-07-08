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
import android.media.AudioManager;
import android.media.SoundPool;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.dd.CircularProgressButton;
import com.github.alexkolpa.fabtoolbar.FabToolbar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
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
import com.google.android.gms.games.leaderboard.*;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MainActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener, RealTimeMessageReceivedListener,
        RoomStatusUpdateListener, RoomUpdateListener, OnInvitationReceivedListener{

    /*
     * API INTEGRATION SECTION. This section contains the code that integrates
     * the game with the Google Play game services API.
     */

    final static String TAG = "Retruque";

    // Request codes for the UIs that we show with startActivityForResult:
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_INVITATION_INBOX = 10001;
    final static int RC_WAITING_ROOM = 10002;
    final static int REQUEST_LEADERBOARD = 10100;
    final static int REQUEST_ACHIEVEMENTS = 10110;
    final static String LEADERBOARD_ID = "CgkIpb6oxu8SEAIQCA";
    final static String ACHIEVEMENTS_ID = "CgkIpb6oxu8SEAIQCQ";

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

    /**
     * *** VARIABLES DEL JUEGO *****
     */

    //Enteros
    int miValor = 0, valor1 = 0, valor2 = 0, valor3 = 0, valorEmpate = 0;
    int ronda = 1;
    int misRondasGanadas = 0;
    int puntosTruc = 0;
    int puntosEnvid = 0;
    int miEnvid = 0;
    int puntosTotalesMios = 0;
    int puntosTotalesJugador2 = 0;
    int envidOtro = 0;
    int maximo = 0;
    int numeroJugadores = 0;
    int segundos = 40;
    int segundos2 = 40;
    private float xDelta;
    private float yDelta;
    int posTvJugador1 = 0, posTvJugador2 = 0, posTvJugador3 = 0;

    //Strings
    String idJugador1 = null;
    String idJugador2 = null;
    String turno = null;
    String remoteId;
    String mano;
    String ganadorRonda1 = null;
    String ganadorRonda2 = null;
    String sCartasJ2 = "";
    String ganadorEnvid = null;
    String desconectado = "";

    //Boolean
    boolean hayEmpate = false;
    boolean hayEnvid = false;
    boolean mostrarRepartiendo = false;
    boolean todosMismoPalo = false;
    boolean hayTruc = false;
    boolean hayRetruc = false;
    boolean hayCuatreVal = false;
    boolean hayJocFora = false;
    boolean hayVuelvo = false;
    boolean faltaDirecta = false;
    boolean hayAnimaciones = false;
    boolean hayAnimacionRival1 = false;
    boolean hayAnimacionRival2 = false;
    boolean hayAnimacionRival3 = false;
    boolean tapoPrimera = false;
    boolean tapo = false;

    //Listas y arrays
    int[] list = new int[3];
    int[] list2 = new int[3];
    int[] numCarta = new int[3];


    //ImageViews
    ImageView tvJugador1;
    ImageView tvJugador2;
    ImageView tvJugador3;
    ImageView tvCartaMesa1;
    ImageView tvCartaMesa2;
    ImageView tvCartaMesa3;
    ImageView tvMesaRival1;
    ImageView tvMesaRival2;
    ImageView tvMesaRival3;
    ImageView imgPerfilRival;
    ImageView imgPerfil;
    ImageButton truc;
    ImageButton envid;
    ImageButton meVoy;
    ImageButton retruque;
    ImageButton quatreVal;
    ImageButton jocFora;
    ImageButton salir;
    ImageButton laFalta;
    ImageView manoDedo;
    ImageView dedo;
    ImageButton abandonar;
    ImageButton tapar;

    //TextViews
    TextView textoAccion1;
    TextView textoAccion2;
    TextView nombreJugador1;
    TextView nombreJugador2;


    //Otros objetos
    private List<Carta> baraja = new ArrayList<>();
    private List<Carta> manoJugador = new ArrayList<>();
    Carta carta1;
    Carta carta2;
    Carta carta3;
    AlertDialog.Builder dialogoNuevaPartida;
    AlertDialog dialog;
    Carta aux;
    MaterialDialog.Builder materialDialog;
    MaterialDialog repartiendo;
    MaterialDialog dialogEnvid;
    MaterialDialog.ButtonCallback callbackReinicio;
    ProgressBar progressBar1;
    ProgressBar progressBar2;
    CountDownTimer mCountDownTimerJ1 = null;
    CountDownTimer mCountDownTimerJ2 = null;
    PointF inicio1;
    PointF inicio2;
    PointF inicio3;
    PointF inicioManoDedo;
    PointF inicio1Rival;
    PointF inicio2Rival;
    PointF inicio3Rival;
    FabToolbar actionButton;
    View.OnClickListener menuListener;

    //Constantes
    final static int NO_QUIERO_TRUC = 1;
    final static int TRUC = 2;
    final static int RETRUC = 3;
    final static int CUATRE_VAL = 4;
    final static int NO_QUIERO_ENVID = 1;
    final static int ENVID = 2;
    final static int TORNE = 4;

    /**
     * *** VARIABLES EXTRA PARA 4 JUGADORES *****
     */

    //Enteros
    int valor1_derecha = 0;
    int valor2_derecha = 0;
    int valor3_derecha = 0;
    int valor1_arriba = 0;
    int valor2_arriba = 0;
    int valor3_arriba = 0;
    int valor1_izq = 0;
    int valor2_izq = 0;
    int valor3_izq = 0;
    int valorEmpateDerecha = 0;
    int valorEmpateArriba = 0;
    int valorEmpateIzq = 0;
    int rondasGanadasMiEquipo = 0;
    int mensajesRecibidos = 0;
    int mensajesRecibidosTruc = 0;
    int sonidoRepartir = 0;
    int sonidoBasto = 0;
    int sonidoEspada = 0;
    int sonidoTirar = 0;
    int numeroSenyas = 0;

    //Strings
    String idJugador3 = null;
    String idJugador4 = null;
    String sCartasJ1 = "";
    String sCartasJ3 = "";
    String sCartasJ4 = "";
    String idCompanyero = "";
    String idInvitado = "";
    String ganadorRonda1_4J = "";
    String ganadorRonda2_4J = "";
    String ganadorRonda3_4J = "";
    String sQuieroTruc = "NOQUIERO";
    String primerMensaje = "";
    String nombreJ1 = "";
    String nombreJ2 = "";
    String nombreJ3 = "";
    String nombreJ4 = "";
    String senyaCompi1 = "";
    String senyaCompi2 = "";
    String senyaRivalDerecha = "";
    String senyaRivalIzq = "";

    //Boolean
    private boolean hayEmpate4J = false;
    private boolean hayAnimacionDerechaC1 = false;
    private boolean hayAnimacionDerechaC2 = false;
    private boolean hayAnimacionDerechaC3 = false;
    private boolean hayAnimacionArribaC1 = false;
    private boolean hayAnimacionArribaC2 = false;
    private boolean hayAnimacionArribaC3 = false;
    private boolean hayAnimacionIzqC1 = false;
    private boolean hayAnimacionIzqC2 = false;
    private boolean hayAnimacionIzqC3 = false;

    //Listas y arrays1
    int[] list3 = new int[3];
    int[] list4 = new int[3];
    String[] equipo1 = new String[2];
    String[] equipo2 = new String[2];
    String[] arrayCartasJugadores;
    ArrayList<View> senyas = new ArrayList<>(2);

    //ImageViews
    ImageView tvJugador1_4J; //Cartas de la mano del jugador 1
    ImageView tvJugador2_4J;
    ImageView tvJugador3_4J;
    ImageView tvCartaMesa1_4J; //Cartas de la mesa del jugador 1
    ImageView tvCartaMesa2_4J;
    ImageView tvCartaMesa3_4J;
    ImageView tvMesaJ2_C1; // Cartas de la mesa del jugador 2
    ImageView tvMesaJ2_C2;
    ImageView tvMesaJ2_C3;
    ImageView tvMesaJ3_C1; //Cartas de la mesa del jugador 3
    ImageView tvMesaJ3_C2;
    ImageView tvMesaJ3_C3;
    ImageView tvMesaJ4_C1; //Cartas de la mesa del jugador 4
    ImageView tvMesaJ4_C2;
    ImageView tvMesaJ4_C3;
    ImageButton truc_4J;
    ImageButton envid_4J;
    ImageButton meVoy_4J;
    ImageButton retruque_4J;
    ImageButton jocFora_4J;
    ImageButton salir_4J;
    ImageButton laFalta_4J;
    ImageButton quatreVal_4J;
    ImageButton abandonar_4J;
    ImageView imgPerfilAbajo;
    ImageView imgPerfilDerecha;
    ImageView imgPerfilArriba;
    ImageView imgPerfilIzq;
    ImageView mano_4J;
    ImageView dedo_4J;
    ImageButton tapar_4J;

    //TextViews
    TextView txtNumeroJugador;
    TextView bocadilloDerecha;
    TextView bocadilloArriba;
    TextView bocadilloIzq;
    TextView nombreJugArriba;
    TextView nombreJugAbajo;
    TextView nombreJugDerecha;
    TextView nombreJugIzq;

    //Otros objetos
    PointF inicio1J1;
    PointF inicio2J1;
    PointF inicio3J1;
    PointF inicio1RivalJ2;
    PointF inicio2RivalJ2;
    PointF inicio3RivalJ2;
    PointF inicio1RivalJ3;
    PointF inicio2RivalJ3;
    PointF inicio3RivalJ3;
    PointF inicio1RivalJ4;
    PointF inicio2RivalJ4;
    PointF inicio3RivalJ4;
    private float xDelta_4J;
    private float yDelta_4J;
    FabToolbar actionButton_4J;
    ProgressBar progressBarAbajo;
    ProgressBar progressBarDerecha;
    ProgressBar progressBarArriba;
    ProgressBar progressBarIzq;
    CountDownTimer mCountDownTimerAbajo = null;
    CountDownTimer mCountDownTimerDerecha = null;
    CountDownTimer mCountDownTimerArriba = null;
    CountDownTimer mCountDownTimerIzq = null;
    CircularProgressButton botonMarcadorAbajo;
    CircularProgressButton botonMarcadorArriba;
    CircularProgressButton botonMarcadorAbajo_4J;
    CircularProgressButton botonMarcadorArriba_4J;
    PointF inicioManoDedo_4J;
    SoundPool soundPool;
    MaterialDialog dialogIconos;

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
                        if (!hayTruc) puntosTruc = NO_QUIERO_TRUC;
                        if (hayTruc && !hayRetruc) puntosTruc = TRUC;
                        if (hayRetruc && !hayCuatreVal) puntosTruc = RETRUC;
                        if (hayCuatreVal && !hayJocFora) puntosTruc = CUATRE_VAL;
                        if (hayJocFora) puntosTruc = 24;
                        mostrarResultadosPerdedorMano("PRIMERO");
                        break;

                    case R.id.salir:
                        actionButton.hide();
                        break;

                    case R.id.abandonar:
                        showBasicAlertDesconectarse("Abandonar partida", "Si abandonas, perder�s la partida. �Est�s seguro?");
                        break;

                    case R.id.envido_4J:
                        envido();
                        actionButton_4J.hide();
                        envid_4J.setVisibility(View.GONE);
                        laFalta_4J.setVisibility(View.GONE);
                      break;

                    case R.id.salir_4J:
                        actionButton_4J.hide();
                        break;

                    case R.id.la_falta_4J:
                        envidoLaFalta();
                        actionButton_4J.hide();
                        envid_4J.setVisibility(View.GONE);
                        laFalta_4J.setVisibility(View.GONE);
                        break;

                    case R.id.truco_4J:
                        truco();
                        actionButton_4J.hide();
                        truc_4J.setVisibility(View.GONE);
                        break;

                    case R.id.retruco_4J:
                        retruco();
                        actionButton_4J.hide();
                        retruque_4J.setVisibility(View.GONE);
                        break;

                    case R.id.quatre_4J:
                        quatreVal();
                        actionButton_4J.hide();
                        quatreVal_4J.setVisibility(View.GONE);
                        break;

                    case R.id.joc_fora_4J:
                        jocFora();
                        actionButton_4J.hide();
                        jocFora_4J.setVisibility(View.GONE);
                        break;
                    case R.id.me_voy_4J:
                        if (!hayTruc) puntosTruc = NO_QUIERO_TRUC;
                        if (hayTruc && !hayRetruc) puntosTruc = TRUC;
                        if (hayRetruc && !hayCuatreVal) puntosTruc = RETRUC;
                        if (hayCuatreVal && !hayJocFora) puntosTruc = CUATRE_VAL;
                        if (hayJocFora) puntosTruc = 24;
                        actionButton_4J.hide();
                        primerMensaje = mMyId;
                        mostrarResultadosPerdedorMano("PRIMERO");
                        break;
                    case R.id.abandonar_4J:
                        showBasicAlertDesconectarse("Abandonar partida", "Si abandonas, perder�s la partida. �Est�s seguro?");
                        break;
                    case R.id.tapar:
                        actionButton.hide();
                        if(ronda == 1) {
                            tapoPrimera = true;
                            tapar.setVisibility(View.GONE);
                        }
                        else tapo = true;
                        break;
                    case R.id.tapar_4J:
                        actionButton_4J.hide();
                        if(ronda == 1) {
                            tapoPrimera = true;
                            tapar_4J.setVisibility(View.GONE);
                        }
                        else tapo = true;
                        break;
                }
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

        tvMesaRival1.animate().rotationXBy(30).setDuration(0);
        tvMesaRival2.animate().rotationXBy(30).setDuration(0);
        tvMesaRival3.animate().rotationXBy(30).setDuration(0);

        tvMesaRival1.setVisibility(View.INVISIBLE);
        tvMesaRival2.setVisibility(View.INVISIBLE);
        tvMesaRival3.setVisibility(View.INVISIBLE);
        inicio1Rival = new PointF(tvJugador1.getX(), tvJugador1.getY());
        inicio2Rival = new PointF(tvJugador2.getX(), tvJugador2.getY());
        inicio3Rival = new PointF(tvJugador3.getX(), tvJugador3.getY());

        textoAccion1 = (TextView) findViewById(R.id.textoJugador1);
        textoAccion2 = (TextView) findViewById(R.id.textoJugador2);
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
        abandonar = (ImageButton) findViewById(R.id.abandonar);

        tapar = (ImageButton) findViewById(R.id.tapar);
        tapar_4J = (ImageButton) findViewById(R.id.tapar_4J);

        progressBar1 = (ProgressBar) findViewById(R.id.progres_segundos_1);
        progressBar2 = (ProgressBar) findViewById(R.id.progres_segundos_2);

        manoDedo = (ImageView) findViewById(R.id.mano);
        dedo = (ImageView) findViewById(R.id.dedo);
        inicioManoDedo = new PointF(dedo.getX(), dedo.getY());

        mano_4J = (ImageView) findViewById(R.id.mano_4J);
        dedo_4J = (ImageView) findViewById(R.id.dedo_4J);
        inicioManoDedo_4J = new PointF(dedo_4J.getX(), dedo_4J.getY());

        //ImageViews para el modo de 4 jugadores
        tvJugador1_4J = (ImageView) findViewById(R.id.carta1ManoJ1);
        tvJugador2_4J = (ImageView) findViewById(R.id.carta2ManoJ1);
        tvJugador3_4J = (ImageView) findViewById(R.id.carta3ManoJ1);

        tvCartaMesa1_4J = (ImageView) findViewById(R.id.carta1MesaJ1);
        tvCartaMesa2_4J = (ImageView) findViewById(R.id.carta2MesaJ1);
        tvCartaMesa3_4J = (ImageView) findViewById(R.id.carta3MesaJ1);

        tvMesaJ2_C1 = (ImageView) findViewById(R.id.carta1MesaJ2);
        tvMesaJ2_C2 = (ImageView) findViewById(R.id.carta2MesaJ2);
        tvMesaJ2_C3 = (ImageView) findViewById(R.id.carta3MesaJ2);

        tvMesaJ3_C1 = (ImageView) findViewById(R.id.carta1MesaJ3);
        tvMesaJ3_C2 = (ImageView) findViewById(R.id.carta2MesaJ3);
        tvMesaJ3_C3 = (ImageView) findViewById(R.id.carta3MesaJ3);

        tvMesaJ4_C1 = (ImageView) findViewById(R.id.carta1MesaJ4);
        tvMesaJ4_C2 = (ImageView) findViewById(R.id.carta2MesaJ4);
        tvMesaJ4_C3 = (ImageView) findViewById(R.id.carta3MesaJ4);

        inicio1J1 = new PointF(tvJugador1_4J.getX(), tvJugador1_4J.getY());
        inicio2J1 = new PointF(tvJugador2_4J.getX(), tvJugador2_4J.getY());
        inicio3J1 = new PointF(tvJugador3_4J.getX(), tvJugador3_4J.getY());
        inicio1RivalJ2 = new PointF(tvMesaJ2_C1.getX(), tvMesaJ2_C1.getY());
        inicio2RivalJ2 = new PointF(tvMesaJ2_C2.getX(), tvMesaJ2_C2.getY());
        inicio3RivalJ2 = new PointF(tvMesaJ2_C3.getX(), tvMesaJ2_C3.getY());
        inicio1RivalJ3 = new PointF(tvMesaJ3_C1.getX(), tvMesaJ3_C1.getY());
        inicio2RivalJ3 = new PointF(tvMesaJ3_C2.getX(), tvMesaJ3_C2.getY());
        inicio3RivalJ3 = new PointF(tvMesaJ3_C3.getX(), tvMesaJ3_C3.getY());
        inicio1RivalJ4 = new PointF(tvMesaJ4_C1.getX(), tvMesaJ4_C1.getY());
        inicio2RivalJ4 = new PointF(tvMesaJ4_C2.getX(), tvMesaJ4_C2.getY());
        inicio3RivalJ4 = new PointF(tvMesaJ4_C3.getX(), tvMesaJ4_C3.getY());

        actionButton_4J = (FabToolbar) findViewById(R.id.fab_toolbar_4J);
        truc_4J = (ImageButton) findViewById(R.id.truco_4J);
        envid_4J = (ImageButton) findViewById(R.id.envido_4J);
        meVoy_4J = (ImageButton) findViewById(R.id.me_voy_4J);
        retruque_4J = (ImageButton) findViewById(R.id.retruco_4J);
        quatreVal_4J = (ImageButton) findViewById(R.id.quatre_4J);
        jocFora_4J = (ImageButton) findViewById(R.id.joc_fora_4J);
        salir_4J = (ImageButton) findViewById(R.id.salir_4J);
        laFalta_4J = (ImageButton) findViewById(R.id.la_falta_4J);
        abandonar_4J = (ImageButton) findViewById(R.id.abandonar_4J);

        imgPerfilAbajo = (ImageView) findViewById(R.id.imgPerfilAbajo);
        imgPerfilDerecha = (ImageView) findViewById(R.id.imgPerfilDerecha);
        imgPerfilArriba = (ImageView) findViewById(R.id.imgPerfilArriba);
        imgPerfilIzq = (ImageView) findViewById(R.id.imgPerfilIzq);

        progressBarAbajo = (ProgressBar) findViewById(R.id.progres_segundos_abajo);
        progressBarDerecha = (ProgressBar) findViewById(R.id.progres_segundos_derecha);
        progressBarArriba = (ProgressBar) findViewById(R.id.progres_segundos_arriba);
        progressBarIzq = (ProgressBar) findViewById(R.id.progres_segundos_izq);

        //Marcadores modo 2 jugadores
        botonMarcadorAbajo = (CircularProgressButton) findViewById(R.id.botonMarcadorJ1);
        botonMarcadorArriba = (CircularProgressButton) findViewById(R.id.botonMarcadorJ2);
        botonMarcadorAbajo.setProgress(100);
        botonMarcadorArriba.setProgress(100);
        botonMarcadorAbajo.setCompleteText("0");
        botonMarcadorArriba.setCompleteText("0");

        //Marcadores modo 4 jugadores
        botonMarcadorAbajo_4J = (CircularProgressButton) findViewById(R.id.botonMarcadorAbajo);
        botonMarcadorArriba_4J = (CircularProgressButton) findViewById(R.id.botonMarcadorArriba);
        botonMarcadorAbajo_4J.setProgress(100);
        botonMarcadorArriba_4J.setProgress(100);
        botonMarcadorAbajo_4J.setCompleteText("0");
        botonMarcadorArriba_4J.setCompleteText("0");

        soundPool = new SoundPool(8, AudioManager.STREAM_MUSIC, 0);
        sonidoBasto = soundPool.load(this,R.raw.basto,1);
        sonidoEspada = soundPool.load(this,R.raw.espada,2);
        sonidoRepartir = soundPool.load(this,R.raw.repartiendocartasrapido,3);
        sonidoTirar = soundPool.load(this,R.raw.tirarcarta,4);

        txtNumeroJugador = (TextView) findViewById(R.id.textoNumeroJugador);

        bocadilloDerecha = (TextView) findViewById(R.id.bocadilloJ2);
        bocadilloArriba = (TextView) findViewById(R.id.bocadilloJ3);
        bocadilloIzq = (TextView) findViewById(R.id.bocadilloJ4);

        //TextViews para los nombres de los jugadores (4J)
        nombreJugAbajo = (TextView) findViewById(R.id.nombreJugadorAbajo);
        nombreJugArriba = (TextView) findViewById(R.id.nombreJugadorArriba);
        nombreJugIzq = (TextView) findViewById(R.id.nombreJugadorIzq);
        nombreJugDerecha = (TextView) findViewById(R.id.nombreJugadorDerecha);

        //TextViews para los nombres de los jugadores (2J)
        nombreJugador1 = (TextView) findViewById(R.id.nombreJugador1);
        nombreJugador2 = (TextView) findViewById(R.id.nombreJugador2);

        tvMesaJ2_C1.animate().rotationXBy(30).setDuration(0);
        tvMesaJ2_C2.animate().rotationXBy(30).setDuration(0);
        tvMesaJ2_C3.animate().rotationXBy(30).setDuration(0);
        tvMesaJ3_C1.animate().rotationXBy(30).setDuration(0);
        tvMesaJ3_C2.animate().rotationXBy(30).setDuration(0);
        tvMesaJ3_C3.animate().rotationXBy(30).setDuration(0);
        tvMesaJ4_C1.animate().rotationXBy(30).setDuration(0);
        tvMesaJ4_C2.animate().rotationXBy(30).setDuration(0);
        tvMesaJ4_C3.animate().rotationXBy(30).setDuration(0);

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
                                enviarMensajeVuelvoAEnvidar("");
                                cambiarBarraProgreso();
                                hayVuelvo = true;
                                break;
                            //Falta
                            case 2:
                                miEnvid = comprobarEnvid();
                                enviarMensajeLaFalta(1, "");
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

                                if (!turno.equals(mMyId)) {
                                    cambiarBarraProgreso();
                                } else{
                                    reiniciarBarraProgreso();
                                    desbloquearCartas();
                                    animarAparecerMenu();
                                }
                                break;
                            //Falta
                            case 1:
                                enviarMensajeLaFalta(1, "");
                                cambiarBarraProgreso();
                                break;
                            //No quiero
                            case 2:

                                enviarMensajeNoQuiero(2);
                                if (!turno.equals(mMyId)) {
                                    cambiarBarraProgreso();
                                } else{
                                    reiniciarBarraProgreso();
                                    desbloquearCartas();
                                    animarAparecerMenu();
                                }
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
                                comprobarGanadorEnvid();
                                //puntos a sumar por la falta
                                if (ganadorEnvid.equals(mMyId)) {
                                    if (puntosTotalesJugador2 <= 12) {
                                        puntosEnvid = 24;
                                    } else if (puntosTotalesJugador2 > 12) {
                                        puntosEnvid = 24 - puntosTotalesJugador2;
                                    }
                                }
                                enviarMensajeHayEnvidAndGanador(ganadorEnvid, 3);
                                if (!turno.equals(mMyId)) {
                                    cambiarBarraProgreso();
                                } else{
                                    reiniciarBarraProgreso();
                                    desbloquearCartas();
                                    animarAparecerMenu();
                                }
                                break;
                            case 1:
                                if (faltaDirecta) {
                                    enviarMensajeNoQuiero(4);
                                } else enviarMensajeNoQuiero(3);
                                if (!turno.equals(mMyId)) {
                                    cambiarBarraProgreso();
                                } else{
                                    reiniciarBarraProgreso();
                                    desbloquearCartas();
                                    animarAparecerMenu();
                                }
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
                                enviarMensajeNoQuieroTruc(1, "");
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
                                if (!turno.equals(mMyId)) {
                                    cambiarBarraProgreso();
                                } else {
                                    reiniciarBarraProgreso();
                                    desbloquearCartas();
                                    animarAparecerMenu();
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
                                enviarMensajeNoQuieroTruc(2, "");
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
                                if (!turno.equals(mMyId)) {
                                    cambiarBarraProgreso();
                                } else {
                                    reiniciarBarraProgreso();
                                    desbloquearCartas();
                                    animarAparecerMenu();
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
                                enviarMensajeNoQuieroTruc(3, "");
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
                                if (!turno.equals(mMyId)) {
                                    cambiarBarraProgreso();
                                } else {
                                    reiniciarBarraProgreso();
                                    desbloquearCartas();
                                    animarAparecerMenu();
                                }
                                break;
                            //No quiero
                            case 1:
                                enviarMensajeNoQuieroTruc(4, "");
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

    private void showIconosAlert() {
        dialogIconos = new MaterialDialog.Builder(this)
                .title("�Tiempo de se�as!")
                .autoDismiss(false)
                .positiveText("Enviar")
                .negativeText("No hago se�as")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        String senyaPrimera = "nada";
                        String senyaSegunda = "nada";

                        if (numeroSenyas > 0) {
                            switch (senyas.get(0).getId()) {
                                case R.id.espada:
                                    senyaPrimera = "ESPADA";
                                    break;
                                case R.id.basto:
                                    senyaPrimera = "BASTO";
                                    break;
                                case R.id.manillaEspadas:
                                    senyaPrimera = "7ESPADAS";
                                    break;
                                case R.id.manillaOros:
                                    senyaPrimera = "7OROS";
                                    break;
                                case R.id.tres:
                                    senyaPrimera = "TRES";
                                    break;
                                case R.id.ciego:
                                    senyaPrimera = "CIEGO";
                                    break;
                                case R.id.envid33:
                                    senyaPrimera = "33";
                                    break;
                                case R.id.envid32:
                                    senyaPrimera = "32";
                                    break;
                                case R.id.envid31:
                                    senyaPrimera = "31";
                                    break;
                            }
                        }

                        if (numeroSenyas > 1) {
                            switch (senyas.get(1).getId()) {
                                case R.id.espada:
                                    senyaSegunda = "ESPADA";
                                    break;
                                case R.id.basto:
                                    senyaSegunda = "BASTO";
                                    break;
                                case R.id.manillaEspadas:
                                    senyaSegunda = "7ESPADAS";
                                    break;
                                case R.id.manillaOros:
                                    senyaSegunda = "7OROS";
                                    break;
                                case R.id.tres:
                                    senyaSegunda = "TRES";
                                    break;
                                case R.id.ciego:
                                    senyaSegunda = "CIEGO";
                                    break;
                                case R.id.envid33:
                                    senyaSegunda = "33";
                                    break;
                                case R.id.envid32:
                                    senyaSegunda = "32";
                                    break;
                                case R.id.envid31:
                                    senyaSegunda = "31";
                                    break;
                            }
                        }
                        //DESCOMENTAR EL MENSAJE CUANDO LOS PONGAS DENTRO DEL JUEGO PARA COMPROBAR QUE FUNCIONA ANTES DE SEGUIR
                        enviarMensajeSenyas(senyaPrimera, senyaSegunda);
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }
                })
                .customView(R.layout.dialog_color_chooser, false)
                .build();
        dialogIconos.show();

    }

    private void showBasicAlertDesconectarse(String title, String message) {
        materialDialog = new MaterialDialog.Builder(this)
                .title(title)
                .content(message)
                .positiveText("Aceptar")
                .negativeText("Cancelar")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        Log.d("FFFFF", "Aceptar en dialogo de desconectarse");
                        desconectado = mMyId;
                        leaveRoom();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }
                })
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

    void activarDesactivarMiBarra(String activar){
        if(activar.equals("ACTIVAR")){
            segundos = 40;
            segundos2 = 40;
            progressBarAbajo.setVisibility(View.VISIBLE);
            iniciarBarraProgresoAbajo();

        }else if(activar.equals("DESACTIVAR")){
            progressBarAbajo.setVisibility(View.INVISIBLE);
            mCountDownTimerAbajo.cancel();

        }
    }

    void activarDesactivarBarraCompi(String activar){
        if(activar.equals("ACTIVAR")){
            segundos = 40;
            segundos2 = 40;
            progressBarArriba.setVisibility(View.VISIBLE);
            iniciarBarraProgresoArriba();

        }else if(activar.equals("DESACTIVAR")){
            progressBarArriba.setVisibility(View.INVISIBLE);
            mCountDownTimerArriba.cancel();

        }
    }

    void barrasInvisibles(){
        progressBarAbajo.setVisibility(View.INVISIBLE);
        progressBarDerecha.setVisibility(View.INVISIBLE);
        progressBarArriba.setVisibility(View.INVISIBLE);
        progressBarIzq.setVisibility(View.INVISIBLE);
    }

    boolean isBarrasInvisibles(){
        if(progressBarAbajo.getVisibility() == View.INVISIBLE &&
        progressBarDerecha.getVisibility() == View.INVISIBLE &&
        progressBarArriba.getVisibility() == View.INVISIBLE &&
        progressBarIzq.getVisibility() == View.INVISIBLE){
            return true;
        }
        else return false;
    }

    void activarBarraRival(String caso){
        switch (caso){
            case "TRUC":
                progressBarDerecha.setVisibility(View.VISIBLE);
                progressBarIzq.setVisibility(View.VISIBLE);
                segundos = 40;
                segundos2 = 40;
                iniciarBarraProgresoDerecha();
                iniciarBarraProgresoIzq();
                break;

            case "ENVID":
                String calculaEnvid = "";
                if (mano.equals(idJugador1) && mMyId.equals(idJugador3)) {
                    calculaEnvid = idJugador4;
                } else if (mano.equals(idJugador1) && mMyId.equals(idJugador4)) {
                    calculaEnvid = idJugador3;
                } else if (mano.equals(idJugador2) && mMyId.equals(idJugador4)) {
                    calculaEnvid = idJugador1;
                } else if (mano.equals(idJugador2) && mMyId.equals(idJugador1)) {
                    calculaEnvid = idJugador4;
                } else if (mano.equals(idJugador3) && mMyId.equals(idJugador1)) {
                    calculaEnvid = idJugador2;
                } else if (mano.equals(idJugador3) && mMyId.equals(idJugador2)) {
                    calculaEnvid = idJugador1;
                } else if (mano.equals(idJugador4) && mMyId.equals(idJugador2)) {
                    calculaEnvid = idJugador3;
                } else if (mano.equals(idJugador4) && mMyId.equals(idJugador3)) {
                    calculaEnvid = idJugador2;
                }
                segundos = 40;
                segundos2 = 40;

                if(mMyId.equals(idJugador1) && calculaEnvid.equals(idJugador2)){
                    progressBarDerecha.setVisibility(View.VISIBLE);
                    iniciarBarraProgresoDerecha();
                }else if(mMyId.equals(idJugador1) && calculaEnvid.equals(idJugador4)){
                    progressBarIzq.setVisibility(View.VISIBLE);
                    iniciarBarraProgresoIzq();
                }
                if(mMyId.equals(idJugador2) && calculaEnvid.equals(idJugador1)){
                    progressBarIzq.setVisibility(View.VISIBLE);
                    iniciarBarraProgresoIzq();
                }else if(mMyId.equals(idJugador2) && calculaEnvid.equals(idJugador3)){
                    progressBarDerecha.setVisibility(View.VISIBLE);
                    iniciarBarraProgresoDerecha();
                }
                if(mMyId.equals(idJugador3) && calculaEnvid.equals(idJugador2)){
                    progressBarIzq.setVisibility(View.VISIBLE);
                    iniciarBarraProgresoIzq();
                }else if(mMyId.equals(idJugador3) && calculaEnvid.equals(idJugador4)){
                    progressBarDerecha.setVisibility(View.VISIBLE);
                    iniciarBarraProgresoDerecha();
                }
                if(mMyId.equals(idJugador4) && calculaEnvid.equals(idJugador1)) {
                    progressBarDerecha.setVisibility(View.VISIBLE);
                    iniciarBarraProgresoDerecha();
                }else if(mMyId.equals(idJugador4) && calculaEnvid.equals(idJugador3)){
                    progressBarIzq.setVisibility(View.VISIBLE);
                    iniciarBarraProgresoIzq();
                }
                break;
        }
    }

    private void envido() {
        switch (numeroJugadores) {
            case 2:
                actionButton.hide();
                animarDesaparecerMenu();
                Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
                bloquearCartas();
                miEnvid = comprobarEnvid();
                enviarMensajeEnvid(miEnvid);
                cambiarBarraProgreso();
                break;
            case 4:
                actionButton_4J.hide();
                animarDesaparecerMenu();
                Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
                bloquearCartas();
                enviarMensajeEnvid(0);
                activarDesactivarMiBarra("DESACTIVAR");
                activarBarraRival("ENVID");
                break;
        }
    }

    private void envidoLaFalta() {
        if(numeroJugadores == 2){
            actionButton.hide();
            animarDesaparecerMenu();
            Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
            bloquearCartas();
            miEnvid = comprobarEnvid();
            enviarMensajeLaFalta(2, "");
            cambiarBarraProgreso();
        }else if(numeroJugadores == 4){
            actionButton_4J.hide();
            animarDesaparecerMenu();
            Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
            bloquearCartas();
            String calculaEnvid = "";
            if (mano.equals(idJugador1) && mMyId.equals(idJugador3)) {
                calculaEnvid = idJugador4;
            } else if (mano.equals(idJugador1) && mMyId.equals(idJugador4)) {
                calculaEnvid = idJugador3;
            } else if (mano.equals(idJugador2) && mMyId.equals(idJugador4)) {
                calculaEnvid = idJugador1;
            } else if (mano.equals(idJugador2) && mMyId.equals(idJugador1)) {
                calculaEnvid = idJugador4;
            } else if (mano.equals(idJugador3) && mMyId.equals(idJugador1)) {
                calculaEnvid = idJugador2;
            } else if (mano.equals(idJugador3) && mMyId.equals(idJugador2)) {
                calculaEnvid = idJugador1;
            } else if (mano.equals(idJugador4) && mMyId.equals(idJugador2)) {
                calculaEnvid = idJugador3;
            } else if (mano.equals(idJugador4) && mMyId.equals(idJugador3)) {
                calculaEnvid = idJugador2;
            }
            enviarMensajeLaFalta(2, calculaEnvid);
            activarDesactivarMiBarra("DESACTIVAR");
            activarBarraRival("ENVID");
        }
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

        if(numeroJugadores == 2){
            actionButton.hide();
            animarDesaparecerMenu();
            cambiarBarraProgreso();
        }else if(numeroJugadores == 4){
            actionButton_4J.hide();
            animarDesaparecerMenu();
            activarDesactivarMiBarra("DESACTIVAR");
            activarBarraRival("TRUC");
        }
    }

    public void retruco() {
        Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
        bloquearCartas();
        enviarMensajeRetruc();
        if(numeroJugadores == 2){
            actionButton.hide();
            animarDesaparecerMenu();
            cambiarBarraProgreso();
        }else if(numeroJugadores == 4){
            actionButton_4J.hide();
            animarDesaparecerMenu();
            activarDesactivarMiBarra("DESACTIVAR");
            activarBarraRival("TRUC");
        }
    }

    public void quatreVal() {
        Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
        bloquearCartas();
        enviarMensajeCuatreVal();
        if(numeroJugadores == 2){
            actionButton.hide();
            animarDesaparecerMenu();
            cambiarBarraProgreso();
        }else if(numeroJugadores == 4){
            actionButton_4J.hide();
            animarDesaparecerMenu();
            activarDesactivarMiBarra("DESACTIVAR");
            activarBarraRival("TRUC");
        }
    }

    public void jocFora() {
        Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
        bloquearCartas();
        enviarMensajeJocFora();
        if(numeroJugadores == 2){
            actionButton.hide();
            animarDesaparecerMenu();
            cambiarBarraProgreso();
        }else if(numeroJugadores == 4){
            actionButton_4J.hide();
            animarDesaparecerMenu();
            activarDesactivarMiBarra("DESACTIVAR");
            activarBarraRival("TRUC");
        }
    }


    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {

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
                /*
                // show list of invitable players
                intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, 1, 3);
                switchToScreen(R.id.screen_wait);
                startActivityForResult(intent, RC_SELECT_PLAYERS);*/
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
                startQuickGame2jugadores();
                break;
            case R.id.button_quick_game_4:
                // user wants to play against a random opponent right now
                startQuickGame4jugadores();
                break;
            case R.id.button_ranking:
                startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                        LEADERBOARD_ID), REQUEST_LEADERBOARD);
                break;
            case R.id.button_logros:
                startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient),
                        REQUEST_ACHIEVEMENTS);
                break;
            case R.id.boton_iconos:
                showIconosAlert();
                break;
        }
    }

    void startQuickGame2jugadores() {
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

    void startQuickGame4jugadores() {
        // quick-start a game with 3 randomly selected opponent
        final int MIN_OPPONENTS = 3, MAX_OPPONENTS = 3;
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
        //resetTurno();
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
                    showProgressDialog("?Empezamos!");
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
        if (invitees.size() != 0) {
            idInvitado = invitees.get(0);
        }

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
        idCompanyero = invId;
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
        roomConfigBuilder.setInvitationIdToAccept(invId)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
        switchToScreen(R.id.screen_wait);
        keepScreenOn();
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
        /*if (keyCode == KeyEvent.KEYCODE_BACK && mCurScreen == R.id.screen_game) {
            leaveRoom();
            return true;
        }*/
        if (keyCode == KeyEvent.KEYCODE_BACK){

            Log.d("FFFFF","Entra en tecla atras");
            if(mCurScreen == R.id.screen_lost){
                Log.d("FFFFF", "Caso screen lost");
                leaveRoom();
                //switchToMainScreen();
            }
            else if(mCurScreen == R.id.screen_win){
                Log.d("FFFFF", "Caso screen win");
                leaveRoom();
                //switchToMainScreen();
            }
            else if(mCurScreen == R.id.screen_game){
                Log.d("FFFFF","Atras desde la pantalla de juego de 2 jugadores");
                showBasicAlertDesconectarse("Abandonar partida", "Si abandonas, perder�s la partida. �Est�s seguro?");
            }
            else if(mCurScreen == R.id.screen_game_4_jugadores){
                Log.d("FFFFF","Atras desde la pantalla de juego de 4 jugadores");
                showBasicAlertDesconectarse("Abandonar partida", "Si abandonas, perder�s la partida. �Est�s seguro?");
            }
            else if(mCurScreen == R.id.screen_wait){
                Log.d("FFFFF","Caso screen wait");
                switchToMainScreen();
            }
            return true;
        }
        return super.onKeyDown(keyCode, e);
    }

    // Leave the room.
    void leaveRoom() {
       if(desconectado.equals(mMyId)){ enviarMensajeDesconectado();}
        Log.d(TAG, "Leaving room.");
        resetPuntos();
        stopKeepingScreenOn();
        if (mRoomId != null) {
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
            mRoomId = null;
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
        Log.d("ZZZ", "Numero de jugadores: " + numeroJugadores);

        // print out the list of participants (for debug purposes)
        Log.d(TAG, "Room ID: " + mRoomId);
        Log.d(TAG, "My ID " + mMyId);
        Log.d(TAG, "<< CONNECTED TO ROOM>>");

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
        //room.getAutoMatchCriteria().ge
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
        numeroJugadores = mParticipants.size();
        Log.e(TAG, "numero de jugadores" + numeroJugadores);

        if (numeroJugadores == 2) {
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
                if(mParticipants.get(0).getParticipantId().equals(idJugador1)){
                    nombreJ1 = mParticipants.get(0).getDisplayName();
                    nombreJ2 = mParticipants.get(1).getDisplayName();
                }
                else{
                    nombreJ1 = mParticipants.get(1).getDisplayName();
                    nombreJ2 = mParticipants.get(0).getDisplayName();
                }
            } else {
                idJugador1 = remoteId;
                idJugador2 = mMyId;
                if(mParticipants.get(0).getParticipantId().equals(idJugador1)){
                    nombreJ1 = mParticipants.get(0).getDisplayName();
                    nombreJ2 = mParticipants.get(1).getDisplayName();
                }
                else{
                    nombreJ1 = mParticipants.get(1).getDisplayName();
                    nombreJ2 = mParticipants.get(0).getDisplayName();
                }
            }

            if(mMyId.equals(idJugador1)){
                nombreJugador1.setText(nombreJ1);
                nombreJugador2.setText(nombreJ2);
            }else if(mMyId.equals(idJugador2)){
                nombreJugador1.setText(nombreJ2);
                nombreJugador2.setText(nombreJ1);
            }

            turno = idJugador1;
            mano = idJugador1;
        } else if (numeroJugadores == 4) {

            ArrayList<String> ids4 = room.getParticipantIds();
            List<String> ids4ordenadas = ids4.subList(0, ids4.size());
            Collections.sort(ids4ordenadas);
            Log.d("ZZZ", "Lista de jugadores ordenada: " + ids4ordenadas);
            idJugador1 = ids4ordenadas.get(0);
            idJugador2 = ids4ordenadas.get(1);
            idJugador3 = ids4ordenadas.get(2);
            idJugador4 = ids4ordenadas.get(3);

            equipo1[0] = idJugador1;
            equipo1[1] = idJugador3;
            equipo2[0] = idJugador2;
            equipo2[1] = idJugador4;

            turno = idJugador1;
            mano = idJugador1;

            for(int i=0;i<4;i++){
                if (mParticipants.get(i).getParticipantId().equals(idJugador1)) nombreJ1 = mParticipants.get(i).getDisplayName();
                else if (mParticipants.get(i).getParticipantId().equals(idJugador2)) nombreJ2 = mParticipants.get(i).getDisplayName();
                else if (mParticipants.get(i).getParticipantId().equals(idJugador3)) nombreJ3 = mParticipants.get(i).getDisplayName();
                else if (mParticipants.get(i).getParticipantId().equals(idJugador4)) nombreJ4 = mParticipants.get(i).getDisplayName();
            }

            Log.d("ZZZ", "IDJ1: " + idJugador1);
            Log.d("ZZZ", "IDJ2: " + idJugador2);
            Log.d("ZZZ", "IDJ3: " + idJugador3);
            Log.d("ZZZ", "IDJ4: " + idJugador4);
            Log.d("ZZZ", "Nombre J1: " + nombreJ1);
            Log.d("ZZZ", "Nombre J2: " + nombreJ2);
            Log.d("ZZZ", "Nombre J3: " + nombreJ3);
            Log.d("ZZZ", "Nombre J4: " + nombreJ4);

        }

        if (mRoomId != null) {
            if(numeroJugadores == 2) {
                for (Participant p : mParticipants) {
                    String pid = p.getParticipantId();

                    if (pid.equals(mMyId)) {
                        new LoadProfileImage(imgPerfil).execute(p.getIconImageUrl());
                    } else {
                        new LoadProfileImage(imgPerfilRival).execute(p.getIconImageUrl());
                    }
                }
            }else if(numeroJugadores == 4){
                for (Participant p : mParticipants) {
                    String pid = p.getParticipantId();
                    if (mMyId.equals(idJugador1)) {
                        if(pid.equals(idJugador1)){
                            new LoadProfileImage(imgPerfilAbajo).execute(p.getIconImageUrl());
                            nombreJugAbajo.setText(nombreJ1);
                        }
                        if(pid.equals(idJugador2)){
                            new LoadProfileImage(imgPerfilDerecha).execute(p.getIconImageUrl());
                            nombreJugDerecha.setText(nombreJ2);
                        }
                        if(pid.equals(idJugador3)){
                            new LoadProfileImage(imgPerfilArriba).execute(p.getIconImageUrl());
                            nombreJugArriba.setText(nombreJ3);
                        }
                        if(pid.equals(idJugador4)){
                            new LoadProfileImage(imgPerfilIzq).execute(p.getIconImageUrl());
                            nombreJugIzq.setText(nombreJ4);
                        }
                    } else if (mMyId.equals(idJugador2)) {
                        if(pid.equals(idJugador1)){
                            new LoadProfileImage(imgPerfilIzq).execute(p.getIconImageUrl());
                            nombreJugIzq.setText(nombreJ1);
                        }
                        if(pid.equals(idJugador2)){
                            new LoadProfileImage(imgPerfilAbajo).execute(p.getIconImageUrl());
                            nombreJugAbajo.setText(nombreJ2);
                        }
                        if(pid.equals(idJugador3)){
                            new LoadProfileImage(imgPerfilDerecha).execute(p.getIconImageUrl());
                            nombreJugDerecha.setText(nombreJ3);
                        }
                        if(pid.equals(idJugador4)){
                            new LoadProfileImage(imgPerfilArriba).execute(p.getIconImageUrl());
                            nombreJugArriba.setText(nombreJ4);
                        }
                    } else if (mMyId.equals(idJugador3)) {
                        if(pid.equals(idJugador1)){
                            new LoadProfileImage(imgPerfilArriba).execute(p.getIconImageUrl());
                            nombreJugArriba.setText(nombreJ1);
                        }
                        if(pid.equals(idJugador2)){
                            new LoadProfileImage(imgPerfilIzq).execute(p.getIconImageUrl());
                            nombreJugIzq.setText(nombreJ2);
                        }
                        if(pid.equals(idJugador3)){
                            new LoadProfileImage(imgPerfilAbajo).execute(p.getIconImageUrl());
                            nombreJugAbajo.setText(nombreJ3);
                        }
                        if(pid.equals(idJugador4)) {
                            new LoadProfileImage(imgPerfilDerecha).execute(p.getIconImageUrl());
                            nombreJugDerecha.setText(nombreJ4);
                        }
                    } else if (mMyId.equals(idJugador4)) {
                        if(pid.equals(idJugador1)){
                            new LoadProfileImage(imgPerfilDerecha).execute(p.getIconImageUrl());
                            nombreJugDerecha.setText(nombreJ1);
                        }
                        if(pid.equals(idJugador2)){
                            new LoadProfileImage(imgPerfilArriba).execute(p.getIconImageUrl());
                            nombreJugArriba.setText(nombreJ2);
                        }
                        if(pid.equals(idJugador3)){
                            new LoadProfileImage(imgPerfilIzq).execute(p.getIconImageUrl());
                            nombreJugIzq.setText(nombreJ3);
                        }
                        if(pid.equals(idJugador4)){
                            new LoadProfileImage(imgPerfilAbajo).execute(p.getIconImageUrl());
                            nombreJugAbajo.setText(nombreJ4);
                        }
                    }
                }
            }
        }
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
    }

    /*
     * GAME LOGIC SECTION. Methods that implement the game's rules.
     */

    void resetAll() {
        if (numeroJugadores == 2) {
            miValor = 0;
            valor1 = 0;
            valor2 = 0;
            valor3 = 0;
            valorEmpate = 0;
            ronda = 1;
            misRondasGanadas = 0;
            tvJugador1.setVisibility(View.INVISIBLE);
            tvJugador2.setVisibility(View.INVISIBLE);
            tvJugador3.setVisibility(View.INVISIBLE);
            tvCartaMesa1.setVisibility(View.INVISIBLE);
            tvCartaMesa2.setVisibility(View.INVISIBLE);
            tvCartaMesa3.setVisibility(View.INVISIBLE);
            tvMesaRival1.setVisibility(View.INVISIBLE);
            tvMesaRival2.setVisibility(View.INVISIBLE);
            tvMesaRival3.setVisibility(View.INVISIBLE);
            hayEmpate = false;
            ganadorRonda1 = "";
            ganadorRonda2 = "";
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
            desconectado = "";
            tapo = false;
            tapoPrimera = false;
            tapar.setVisibility(View.VISIBLE);

            manoDedo.setVisibility(View.INVISIBLE);
            dedo.setVisibility(View.INVISIBLE);
            //animarDesaparecerMano();

            textoAccion1.setVisibility(View.INVISIBLE);
            textoAccion2.setVisibility(View.INVISIBLE);
            if (mCountDownTimerJ1 != null && mCountDownTimerJ2 != null) cancelarBarraProgreso();
            segundos = 40;
            segundos2 = 40;

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

            if (hayAnimaciones) {
                deshacerAnimaciones(tvJugador1);
                deshacerAnimaciones(tvJugador2);
                deshacerAnimaciones(tvJugador3);
                posTvJugador1 = 0;
                posTvJugador2 = 0;
                posTvJugador3 = 0;
                hayAnimaciones = false;
            }

            if (hayAnimacionRival1) {
                Log.d("KKKKKK", "REiniciando animacion 1");
                tvMesaRival1.animate().scaleX((float) 1).scaleY((float) 1).setDuration(0);
                hayAnimacionRival1 = false;
            }
            if (hayAnimacionRival2) {
                Log.d("KKKKKK", "REiniciando animacion 2");
                tvMesaRival2.animate().scaleX((float) 1).scaleY((float) 1).setDuration(0);
                hayAnimacionRival2 = false;
            }
            if (hayAnimacionRival3) {
                Log.d("KKKKKK", "REiniciando animacion 3");
                tvMesaRival3.animate().scaleX((float) 1).scaleY((float) 1).setDuration(0);
                hayAnimacionRival3 = false;
            }
        } else if (numeroJugadores == 4) {
            bloquearCartas();
            miValor = 0;
            valorEmpate = 0;
            ronda = 1;
            tvJugador1_4J.setVisibility(View.INVISIBLE);
            tvJugador2_4J.setVisibility(View.INVISIBLE);
            tvJugador3_4J.setVisibility(View.INVISIBLE);
            tvCartaMesa1_4J.setVisibility(View.INVISIBLE);
            tvCartaMesa2_4J.setVisibility(View.INVISIBLE);
            tvCartaMesa3_4J.setVisibility(View.INVISIBLE);
            tvMesaJ2_C1.setVisibility(View.INVISIBLE);
            tvMesaJ2_C2.setVisibility(View.INVISIBLE);
            tvMesaJ2_C3.setVisibility(View.INVISIBLE);
            tvMesaJ3_C1.setVisibility(View.INVISIBLE);
            tvMesaJ3_C2.setVisibility(View.INVISIBLE);
            tvMesaJ3_C3.setVisibility(View.INVISIBLE);
            tvMesaJ4_C1.setVisibility(View.INVISIBLE);
            tvMesaJ4_C2.setVisibility(View.INVISIBLE);
            tvMesaJ4_C3.setVisibility(View.INVISIBLE);
            hayEmpate4J = false;
            miEnvid = 0;
            hayEnvid = false;
            ganadorEnvid = null;
            envidOtro = 0;
            todosMismoPalo = false;
            maximo = 0;
            valor1_derecha = 0;
            valor2_derecha = 0;
            valor3_derecha = 0;
            valor1_arriba = 0;
            valor2_arriba = 0;
            valor3_arriba = 0;
            valor1_izq = 0;
            valor2_izq = 0;
            valor3_izq = 0;
            valorEmpateDerecha = 0;
            valorEmpateArriba = 0;
            valorEmpateIzq = 0;
            rondasGanadasMiEquipo = 0;
            ganadorRonda1_4J = "";
            ganadorRonda2_4J = "";
            ganadorRonda3_4J = "";
            mensajesRecibidos = 0;
            mensajesRecibidosTruc = 0;
            puntosEnvid = 0;
            puntosTruc = 0;
            hayTruc = false;
            hayRetruc = false;
            hayCuatreVal = false;
            hayJocFora = false;
            hayVuelvo = false;
            faltaDirecta = false;
            primerMensaje = "";
            desconectado = "";
            tapo = false;
            tapoPrimera = false;
            tapar_4J.setVisibility(View.VISIBLE);

            if (mCountDownTimerAbajo != null || mCountDownTimerArriba != null
                    || mCountDownTimerDerecha != null || mCountDownTimerIzq != null) cancelarBarraProgreso();
            segundos = 40;
            segundos2 = 40;

            mano_4J.setVisibility(View.INVISIBLE);
            dedo_4J.setVisibility(View.INVISIBLE);

            truc_4J.setVisibility(View.VISIBLE);
            envid_4J.setVisibility(View.VISIBLE);
            meVoy_4J.setVisibility(View.VISIBLE);
            salir_4J.setVisibility(View.VISIBLE);
            retruque_4J.setVisibility(View.GONE);
            quatreVal_4J.setVisibility(View.GONE);
            jocFora_4J.setVisibility(View.GONE);
            laFalta_4J.setVisibility(View.VISIBLE);
            abandonar.setVisibility(View.VISIBLE);
            abandonar_4J.setVisibility(View.VISIBLE);

            progressBarAbajo.setVisibility(View.INVISIBLE);
            progressBarDerecha.setVisibility(View.INVISIBLE);
            progressBarIzq.setVisibility(View.INVISIBLE);
            progressBarArriba.setVisibility(View.INVISIBLE);

            bocadilloDerecha.setVisibility(View.INVISIBLE);
            bocadilloIzq.setVisibility(View.INVISIBLE);
            bocadilloArriba.setVisibility(View.INVISIBLE);

            if (hayAnimaciones) {
                deshacerAnimaciones(tvJugador1_4J);
                deshacerAnimaciones(tvJugador2_4J);
                deshacerAnimaciones(tvJugador3_4J);
                posTvJugador1 = 0;
                posTvJugador2 = 0;
                posTvJugador3 = 0;
                hayAnimaciones = false;
            }

            if (hayAnimacionDerechaC1) {
                tvMesaJ2_C1.animate().scaleX((float) 1).scaleY((float) 1).setDuration(0);
                hayAnimacionDerechaC1 = false;
            }
            if (hayAnimacionDerechaC2) {
                tvMesaJ2_C1.animate().scaleX((float) 1).scaleY((float) 1).setDuration(0);
                hayAnimacionDerechaC2 = false;
            }
            if (hayAnimacionDerechaC3) {
                tvMesaJ2_C1.animate().scaleX((float) 1).scaleY((float) 1).setDuration(0);
                hayAnimacionDerechaC3 = false;
            }
            if (hayAnimacionArribaC1) {
                tvMesaJ3_C1.animate().scaleX((float) 1).scaleY((float) 1).setDuration(0);
                hayAnimacionArribaC1 = false;
            }
            if (hayAnimacionArribaC2) {
                tvMesaJ3_C1.animate().scaleX((float) 1).scaleY((float) 1).setDuration(0);
                hayAnimacionArribaC2 = false;
            }
            if (hayAnimacionArribaC3) {
                tvMesaJ3_C1.animate().scaleX((float) 1).scaleY((float) 1).setDuration(0);
                hayAnimacionArribaC3 = false;
            }
            if (hayAnimacionIzqC1) {
                tvMesaJ4_C1.animate().scaleX((float) 1).scaleY((float) 1).setDuration(0);
                hayAnimacionIzqC1 = false;
            }
            if (hayAnimacionIzqC2) {
                tvMesaJ4_C1.animate().scaleX((float) 1).scaleY((float) 1).setDuration(0);
                hayAnimacionIzqC2 = false;
            }
            if (hayAnimacionIzqC3) {
                tvMesaJ4_C1.animate().scaleX((float) 1).scaleY((float) 1).setDuration(0);
                hayAnimacionIzqC3 = false;
            }

        }
    }

    void resetPuntos() {
        puntosTotalesMios = 0;
        puntosTotalesJugador2 = 0;
        botonMarcadorAbajo.setCompleteText("0");
        botonMarcadorArriba.setCompleteText("0");
        botonMarcadorAbajo_4J.setCompleteText("0");
        botonMarcadorArriba_4J.setCompleteText("0");
        //marcador.setText("");
        //marcador2.setText("");
        //Poner los nuevos marcadores
    }

    void resetAnimaciones() {
        hayAnimaciones = false;
        hayAnimacionRival1 = false;
        hayAnimacionRival2 = false;
        hayAnimacionRival3 = false;
    }

    void deshacerAnimaciones(View view) {

        switch (numeroJugadores) {

            case 2:
                if(view.equals(tvJugador1)){
                    view.animate().translationX(inicio1.x).rotation(0).setDuration(500);
                    view.animate().translationY(inicio1.y).rotation(0).setDuration(500);

                    if (posTvJugador1 != 0) {
                        view.animate().rotationXBy(-30).scaleX((float) 1).scaleY((float) 1).setDuration(500);
                    }

                }else if(view.equals(tvJugador2)){

                    view.animate().translationX(inicio2.x).rotation(0).setDuration(500);
                    view.animate().translationY(inicio2.y).rotation(0).setDuration(500);

                    if (posTvJugador2 != 0) {
                        view.animate().rotationXBy(-30).scaleX((float) 1).scaleY((float) 1).setDuration(500);
                    }

                } else if(view.equals(tvJugador3)){

                    view.animate().translationX(inicio3.x).rotation(0).setDuration(500);
                    view.animate().translationY(inicio3.y).rotation(0).setDuration(500);

                    if (posTvJugador3 != 0) {
                        view.animate().rotationXBy(-30).scaleX((float) 1).scaleY((float) 1).setDuration(500);
                    }
                }

                break;

            case 4:

                if(view.equals(tvJugador1_4J)){
                view.animate().translationX(inicio1J1.x).rotation(0).setDuration(500);
                view.animate().translationY(inicio1J1.y).rotation(0).setDuration(500);

                if (posTvJugador1 != 0) {
                    view.animate().rotationXBy(-30).scaleX((float) 1).scaleY((float) 1).setDuration(500);
                }

                }else if(view.equals(tvJugador2_4J)){

                    view.animate().translationX(inicio2J1.x).rotation(0).setDuration(500);
                    view.animate().translationY(inicio2J1.y).rotation(0).setDuration(500);

                    if (posTvJugador2 != 0) {
                        view.animate().rotationXBy(-30).scaleX((float) 1).scaleY((float) 1).setDuration(500);
                    }

                } else if(view.equals(tvJugador3_4J)){

                    view.animate().translationX(inicio3J1.x).rotation(0).setDuration(500);
                    view.animate().translationY(inicio3J1.y).rotation(0).setDuration(500);

                    if (posTvJugador3 != 0) {
                        view.animate().rotationXBy(-30).scaleX((float) 1).scaleY((float) 1).setDuration(500);
                    }
                }

                break;
        }

    }

    void animarTextoAccion(final View view) {
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

    void animarBocadillosEnvid(int caso, String sender, String quiero){

        switch (caso) {

            case 1:
                if (esDeMiEquipo(sender)) {
                    if(quiero.equals("QUIERO")){
                        bocadilloArriba.setText("Quiero el envid");
                    }else bocadilloArriba.setText("No quiero el envid");
                    animarTextoAccion(bocadilloArriba);

                } else {

                    if(esRivalDerecha(sender)){
                        if(quiero.equals("QUIERO")){
                            bocadilloDerecha.setText("Quiero el envid");
                        }else bocadilloDerecha.setText("No quiero el envid");
                        animarTextoAccion(bocadilloDerecha);
                    }else if(esRivalIzquierda(sender)){
                        if(quiero.equals("QUIERO")){
                            bocadilloIzq.setText("Quiero el envid");
                        } else bocadilloIzq.setText("No quiero el envid");
                        animarTextoAccion(bocadilloIzq);
                    }
                }

                break;

            case 2:
                if (esDeMiEquipo(sender)) {
                    if(quiero.equals("QUIERO")){
                        bocadilloArriba.setText("Quiero el vuelvo");
                    }else bocadilloArriba.setText("No quiero el vuelvo");
                    animarTextoAccion(bocadilloArriba);
                } else {
                    if(esRivalDerecha(sender)){
                        if(quiero.equals("QUIERO")){
                            bocadilloDerecha.setText("Quiero el vuelvo");
                        }else bocadilloDerecha.setText("No quiero el vuelvo");
                        animarTextoAccion(bocadilloDerecha);
                    }else if(esRivalIzquierda(sender)){
                        if(quiero.equals("QUIERO")){
                            bocadilloIzq.setText("Quiero el vuelvo");
                        } else bocadilloIzq.setText("No quiero el vuelvo");
                        animarTextoAccion(bocadilloIzq);
                    }
                }
                break;

            case 3:
                if (esDeMiEquipo(sender)) {
                    if(quiero.equals("QUIERO")){
                        bocadilloArriba.setText("Quiero la falta");
                    } else bocadilloArriba.setText("No quiero la falta");
                    animarTextoAccion(bocadilloArriba);
                } else {
                    if(esRivalDerecha(sender)){
                        if(quiero.equals("QUIERO")){
                            bocadilloDerecha.setText("Quiero la falta");
                        }else bocadilloDerecha.setText("No quiero la falta");
                        animarTextoAccion(bocadilloDerecha);
                    }else if(esRivalIzquierda(sender)){
                        if(quiero.equals("QUIERO")){
                            bocadilloIzq.setText("Quiero la falta");
                        } else bocadilloIzq.setText("No quiero la falta");
                        animarTextoAccion(bocadilloIzq);
                    }
                }
        }
    }

    void animarBocadillosTruc(int caso, String quiero, String sender){
        switch (quiero){
            case "QUIERO":
                if(esRivalDerecha(sender)){
                    switch (caso){
                        case 1:
                            bocadilloDerecha.setText("Quiero el truc");
                            animarTextoAccion(bocadilloDerecha);
                            progressBarDerecha.setVisibility(View.INVISIBLE);
                            mCountDownTimerDerecha.cancel();
                            break;
                        case 2:
                            bocadilloDerecha.setText("Quiero el retruc");
                            animarTextoAccion(bocadilloDerecha);
                            progressBarDerecha.setVisibility(View.INVISIBLE);
                            mCountDownTimerDerecha.cancel();
                            break;
                        case 3:
                            bocadilloDerecha.setText("Quiero el quatre val");
                            animarTextoAccion(bocadilloDerecha);
                            progressBarDerecha.setVisibility(View.INVISIBLE);
                            mCountDownTimerDerecha.cancel();
                            break;
                        case 4:
                            bocadilloDerecha.setText("Quiero el joc fora");
                            animarTextoAccion(bocadilloDerecha);
                            progressBarDerecha.setVisibility(View.INVISIBLE);
                            mCountDownTimerDerecha.cancel();
                            break;
                    }
                }else if(esRivalIzquierda(sender)){
                    switch (caso){
                        case 1:
                            bocadilloIzq.setText("Quiero el truc");
                            animarTextoAccion(bocadilloIzq);
                            progressBarIzq.setVisibility(View.INVISIBLE);
                            mCountDownTimerIzq.cancel();
                            break;
                        case 2:
                            bocadilloIzq.setText("Quiero el retruc");
                            animarTextoAccion(bocadilloIzq);
                            progressBarIzq.setVisibility(View.INVISIBLE);
                            mCountDownTimerIzq.cancel();
                            break;
                        case 3:
                            bocadilloIzq.setText("Quiero el quatre val");
                            animarTextoAccion(bocadilloIzq);
                            progressBarIzq.setVisibility(View.INVISIBLE);
                            mCountDownTimerIzq.cancel();
                            break;
                        case 4:
                            bocadilloIzq.setText("Quiero el joc fora");
                            animarTextoAccion(bocadilloIzq);
                            progressBarIzq.setVisibility(View.INVISIBLE);
                            mCountDownTimerIzq.cancel();
                            break;
                    }
                }
                break;
            case "RETRUQUE":
                if(esRivalDerecha(sender)){
                    bocadilloDerecha.setText("Retruque");
                    animarTextoAccion(bocadilloDerecha);
                    progressBarDerecha.setVisibility(View.INVISIBLE);
                    mCountDownTimerDerecha.cancel();
                }else if(esRivalIzquierda(sender)){
                    bocadilloIzq.setText("Retruque");
                    animarTextoAccion(bocadilloIzq);
                    progressBarIzq.setVisibility(View.INVISIBLE);
                    mCountDownTimerIzq.cancel();
                }
                break;
            case "QUATRE":
                if(esRivalDerecha(sender)){
                    bocadilloDerecha.setText("Quatre val");
                    animarTextoAccion(bocadilloDerecha);
                    progressBarDerecha.setVisibility(View.INVISIBLE);
                    mCountDownTimerDerecha.cancel();
                }else if(esRivalIzquierda(sender)){
                    bocadilloIzq.setText("Quatre val");
                    animarTextoAccion(bocadilloIzq);
                    progressBarIzq.setVisibility(View.INVISIBLE);
                    mCountDownTimerIzq.cancel();
                }
                break;
            case "JOC":
                if(esRivalDerecha(sender)){
                    bocadilloDerecha.setText("Joc fora");
                    animarTextoAccion(bocadilloDerecha);
                    progressBarDerecha.setVisibility(View.INVISIBLE);
                    mCountDownTimerDerecha.cancel();
                }else if(esRivalIzquierda(sender)){
                    bocadilloIzq.setText("Joc fora");
                    animarTextoAccion(bocadilloIzq);
                    progressBarIzq.setVisibility(View.INVISIBLE);
                    mCountDownTimerIzq.cancel();
                }
                break;
            case "NOQUIERO":
                if(esRivalDerecha(sender)){
                    switch (caso){
                        case 1:
                            bocadilloDerecha.setText("No quiero el truc");
                            animarTextoAccion(bocadilloDerecha);
                            progressBarDerecha.setVisibility(View.INVISIBLE);
                            mCountDownTimerDerecha.cancel();
                            break;
                        case 2:
                            bocadilloDerecha.setText("No quiero el retruc");
                            animarTextoAccion(bocadilloDerecha);
                            progressBarDerecha.setVisibility(View.INVISIBLE);
                            mCountDownTimerDerecha.cancel();
                            break;
                        case 3:
                            bocadilloDerecha.setText("No quiero el quatre val");
                            animarTextoAccion(bocadilloDerecha);
                            progressBarDerecha.setVisibility(View.INVISIBLE);
                            mCountDownTimerDerecha.cancel();
                            break;
                        case 4:
                            bocadilloDerecha.setText("No quiero el joc fora");
                            animarTextoAccion(bocadilloDerecha);
                            progressBarDerecha.setVisibility(View.INVISIBLE);
                            mCountDownTimerDerecha.cancel();
                            break;
                    }
                }else if(esRivalIzquierda(sender)){
                    switch (caso){
                        case 1:
                            bocadilloIzq.setText("No quiero el truc");
                            animarTextoAccion(bocadilloIzq);
                            progressBarIzq.setVisibility(View.INVISIBLE);
                            mCountDownTimerIzq.cancel();
                            break;
                        case 2:
                            bocadilloIzq.setText("No quiero el retruc");
                            animarTextoAccion(bocadilloIzq);
                            progressBarIzq.setVisibility(View.INVISIBLE);
                            mCountDownTimerIzq.cancel();
                            break;
                        case 3:
                            bocadilloIzq.setText("No quiero el quatre val");
                            animarTextoAccion(bocadilloIzq);
                            progressBarIzq.setVisibility(View.INVISIBLE);
                            mCountDownTimerIzq.cancel();
                            break;
                        case 4:
                            bocadilloIzq.setText("No quiero el joc fora");
                            animarTextoAccion(bocadilloIzq);
                            progressBarIzq.setVisibility(View.INVISIBLE);
                            mCountDownTimerIzq.cancel();
                            break;
                    }
                }
                break;
        }

    }

    // Reset game variables in preparation for a new game.
    void desbloquearCartas() {
        if (numeroJugadores == 2) {
            tvJugador1.setEnabled(true);
            tvJugador2.setEnabled(true);
            tvJugador3.setEnabled(true);
        } else if (numeroJugadores == 4) {
            tvJugador1_4J.setEnabled(true);
            tvJugador2_4J.setEnabled(true);
            tvJugador3_4J.setEnabled(true);
        }
    }

    void bloquearCartas() {
        if (numeroJugadores == 2) {
            tvJugador1.setEnabled(false);
            tvJugador2.setEnabled(false);
            tvJugador3.setEnabled(false);
        } else if (numeroJugadores == 4) {
            tvJugador1_4J.setEnabled(false);
            tvJugador2_4J.setEnabled(false);
            tvJugador3_4J.setEnabled(false);
        }

    }

    void resetTurno() {
        turno = null;
        idJugador1 = null;
        idJugador2 = null;
        idJugador3 = null;
        idJugador4 = null;
    }

    void cambiarTurno() {

        if (numeroJugadores == 2) {
            cambiarBarraProgreso();
            animarDesaparecerMenu();
            if (mMyId.equals(idJugador1) && turno.equals(idJugador1)) turno = idJugador2;
            if (mMyId.equals(idJugador2) && turno.equals(idJugador2)) turno = idJugador1;

        } else if (numeroJugadores == 4) {

            if (mMyId.equals(idJugador1) && turno.equals(idJugador1)) turno = idJugador2;
            if (mMyId.equals(idJugador2) && turno.equals(idJugador2)) turno = idJugador3;
            if (mMyId.equals(idJugador3) && turno.equals(idJugador3)) turno = idJugador4;
            if (mMyId.equals(idJugador4) && turno.equals(idJugador4)) turno = idJugador1;

            animarDesaparecerMenu();
            cambiarBarraProgreso();

        }

    }

    void iniciarBarraProgresoJ1() {

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
        }.start();
    }

    void iniciarBarraProgresoJ2() {
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
        }.start();
    }

    void iniciarBarraProgresoAbajo() {

        if(mCountDownTimerAbajo == null){

            mCountDownTimerAbajo = new CountDownTimer(40000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    segundos--;
                    progressBarAbajo.setProgress(segundos);

                }
                @Override
                public void onFinish() {
                    //Do what you want
                    segundos--;
                    progressBarAbajo.setProgress(segundos);
                }
            }.start();
        } else{
            mCountDownTimerAbajo.cancel();
            mCountDownTimerAbajo.start();
        }
    }

    void iniciarBarraProgresoDerecha() {

        if(mCountDownTimerDerecha == null){
            mCountDownTimerDerecha = new CountDownTimer(40000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    segundos--;
                    progressBarDerecha.setProgress(segundos);

                }

                @Override
                public void onFinish() {
                    //Do what you want
                    segundos--;
                    progressBarDerecha.setProgress(segundos);
                }
            }.start();
        }else {
            mCountDownTimerDerecha.cancel();
            mCountDownTimerDerecha.start();
        }
    }

    void iniciarBarraProgresoArriba() {

        if(mCountDownTimerArriba == null){
            mCountDownTimerArriba = new CountDownTimer(40000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    segundos2--;
                    progressBarArriba.setProgress(segundos2);

                }

                @Override
                public void onFinish() {
                    //Do what you want
                    segundos2--;
                    progressBarArriba.setProgress(segundos2);
                }
            }.start();
        }else {
            mCountDownTimerArriba.cancel();
            mCountDownTimerArriba.start();
        }
    }

    void iniciarBarraProgresoIzq() {

        if(mCountDownTimerIzq == null){
        mCountDownTimerIzq = new CountDownTimer(40000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                segundos2--;
                progressBarIzq.setProgress(segundos2);

            }

            @Override
            public void onFinish() {
                //Do what you want
                segundos2--;
                progressBarIzq.setProgress(segundos2);
            }
        }.start();
        }else {
            mCountDownTimerIzq.cancel();
            mCountDownTimerIzq.start();
        }
    }


    void cambiarBarraProgreso() {
        if(numeroJugadores == 2) {
            if (progressBar1.getVisibility() == View.VISIBLE) {
                progressBar1.setVisibility(View.INVISIBLE);
                mCountDownTimerJ1.cancel();
                segundos = 40;
                segundos2 = 40;
                progressBar2.setVisibility(View.VISIBLE);
                iniciarBarraProgresoJ2();
            } else if (progressBar2.getVisibility() == View.VISIBLE) {
                progressBar2.setVisibility(View.INVISIBLE);
                mCountDownTimerJ2.cancel();
                segundos = 40;
                segundos2 = 40;
                progressBar1.setVisibility(View.VISIBLE);
                iniciarBarraProgresoJ1();
            }
        }else if(numeroJugadores == 4){

            if(turno.equals(mMyId)){
                if(isBarrasInvisibles()){
                    segundos = 40;
                    segundos2 = 40;
                    progressBarAbajo.setVisibility(View.VISIBLE);
                    reiniciarBarraProgreso();
                }else{
                    if (progressBarAbajo.getVisibility() == View.VISIBLE) {
                        reiniciarBarraProgreso();
                    } else if (progressBarDerecha.getVisibility() == View.VISIBLE) {
                        progressBarDerecha.setVisibility(View.INVISIBLE);
                        mCountDownTimerDerecha.cancel();
                        segundos = 40;
                        segundos2 = 40;
                        progressBarAbajo.setVisibility(View.VISIBLE);
                        iniciarBarraProgresoAbajo();
                    }else if (progressBarArriba.getVisibility() == View.VISIBLE) {
                        progressBarArriba.setVisibility(View.INVISIBLE);
                        mCountDownTimerArriba.cancel();
                        segundos = 40;
                        segundos2 = 40;
                        progressBarAbajo.setVisibility(View.VISIBLE);
                        iniciarBarraProgresoAbajo();
                    }else if (progressBarIzq.getVisibility() == View.VISIBLE) {
                        progressBarIzq.setVisibility(View.INVISIBLE);
                        mCountDownTimerIzq.cancel();
                        segundos = 40;
                        segundos2 = 40;
                        progressBarAbajo.setVisibility(View.VISIBLE);
                        iniciarBarraProgresoAbajo();
                    }
                }
            }else {
                if(mMyId.equals(comprobarDerechaTurno())) {
                    if(isBarrasInvisibles()){
                        segundos = 40;
                        segundos2 = 40;
                        progressBarIzq.setVisibility(View.VISIBLE);
                        reiniciarBarraProgreso();
                    }else {
                        if (progressBarAbajo.getVisibility() == View.VISIBLE) {
                            progressBarAbajo.setVisibility(View.INVISIBLE);
                            mCountDownTimerAbajo.cancel();
                            segundos = 40;
                            segundos2 = 40;
                            progressBarIzq.setVisibility(View.VISIBLE);
                            iniciarBarraProgresoIzq();
                        } else if (progressBarDerecha.getVisibility() == View.VISIBLE) {
                            progressBarDerecha.setVisibility(View.INVISIBLE);
                            mCountDownTimerDerecha.cancel();
                            segundos = 40;
                            segundos2 = 40;
                            progressBarIzq.setVisibility(View.VISIBLE);
                            iniciarBarraProgresoIzq();
                        } else if (progressBarArriba.getVisibility() == View.VISIBLE) {
                            progressBarArriba.setVisibility(View.INVISIBLE);
                            mCountDownTimerArriba.cancel();
                            segundos = 40;
                            segundos2 = 40;
                            progressBarIzq.setVisibility(View.VISIBLE);
                            iniciarBarraProgresoIzq();
                        } else if (progressBarIzq.getVisibility() == View.VISIBLE) {
                            reiniciarBarraProgreso();
                        }
                    }

                }else if(mMyId.equals(comprobarArribaTurno())){
                    if(isBarrasInvisibles()){
                        segundos = 40;
                        segundos2 = 40;
                        progressBarArriba.setVisibility(View.VISIBLE);
                        reiniciarBarraProgreso();
                    }else {
                        if (progressBarAbajo.getVisibility() == View.VISIBLE) {
                            progressBarAbajo.setVisibility(View.INVISIBLE);
                            mCountDownTimerAbajo.cancel();
                            segundos = 40;
                            segundos2 = 40;
                            progressBarArriba.setVisibility(View.VISIBLE);
                            iniciarBarraProgresoArriba();
                        } else if (progressBarDerecha.getVisibility() == View.VISIBLE) {
                            progressBarDerecha.setVisibility(View.INVISIBLE);
                            mCountDownTimerDerecha.cancel();
                            segundos = 40;
                            segundos2 = 40;
                            progressBarArriba.setVisibility(View.VISIBLE);
                            iniciarBarraProgresoArriba();
                        } else if (progressBarArriba.getVisibility() == View.VISIBLE) {
                            reiniciarBarraProgreso();
                        } else if (progressBarIzq.getVisibility() == View.VISIBLE) {
                            progressBarIzq.setVisibility(View.INVISIBLE);
                            mCountDownTimerIzq.cancel();
                            segundos = 40;
                            segundos2 = 40;
                            progressBarArriba.setVisibility(View.VISIBLE);
                            iniciarBarraProgresoArriba();
                        }
                    }
                }else if(mMyId.equals(comprobarIzqTurno())){
                    if(isBarrasInvisibles()){
                        segundos = 40;
                        segundos2 = 40;
                        progressBarDerecha.setVisibility(View.VISIBLE);
                        reiniciarBarraProgreso();
                    }else{
                        if (progressBarAbajo.getVisibility() == View.VISIBLE) {
                            progressBarAbajo.setVisibility(View.INVISIBLE);
                            mCountDownTimerAbajo.cancel();
                            segundos = 40;
                            segundos2 = 40;
                            progressBarDerecha.setVisibility(View.VISIBLE);
                            iniciarBarraProgresoDerecha();
                        } else if (progressBarDerecha.getVisibility() == View.VISIBLE) {
                            reiniciarBarraProgreso();
                        }else if (progressBarArriba.getVisibility() == View.VISIBLE) {
                            progressBarArriba.setVisibility(View.INVISIBLE);
                            mCountDownTimerArriba.cancel();
                            segundos = 40;
                            segundos2 = 40;
                            progressBarDerecha.setVisibility(View.VISIBLE);
                            iniciarBarraProgresoDerecha();
                        }else if (progressBarIzq.getVisibility() == View.VISIBLE) {
                            progressBarIzq.setVisibility(View.INVISIBLE);
                            mCountDownTimerIzq.cancel();
                            segundos = 40;
                            segundos2 = 40;
                            progressBarDerecha.setVisibility(View.VISIBLE);
                            iniciarBarraProgresoDerecha();
                        }
                    }
                }

            }






/*
            if (progressBarAbajo.getVisibility() == View.VISIBLE) {
                progressBarAbajo.setVisibility(View.INVISIBLE);
                mCountDownTimerAbajo.cancel();
                segundos = 40;
                progressBarDerecha.setVisibility(View.VISIBLE);
                iniciarBarraProgresoDerecha();
            } else if (progressBarDerecha.getVisibility() == View.VISIBLE) {
                progressBarDerecha.setVisibility(View.INVISIBLE);
                mCountDownTimerDerecha.cancel();
                segundos = 40;
                progressBarArriba.setVisibility(View.VISIBLE);
                iniciarBarraProgresoArriba();
            }else if (progressBarArriba.getVisibility() == View.VISIBLE) {
                progressBarArriba.setVisibility(View.INVISIBLE);
                mCountDownTimerArriba.cancel();
                segundos = 40;
                progressBarIzq.setVisibility(View.VISIBLE);
                iniciarBarraProgresoIzq();
            }else if (progressBarIzq.getVisibility() == View.VISIBLE) {
                progressBarIzq.setVisibility(View.INVISIBLE);
                mCountDownTimerIzq.cancel();
                segundos = 40;
                progressBarAbajo.setVisibility(View.VISIBLE);
                iniciarBarraProgresoAbajo();
            }*/
        }
    }

    void reiniciarBarraProgreso() {
        if(numeroJugadores == 2) {
            if (progressBar1.getVisibility() == View.VISIBLE) {
                mCountDownTimerJ1.cancel();
                segundos = 40;
                segundos2 = 40;
                iniciarBarraProgresoJ1();
            } else if (progressBar2.getVisibility() == View.VISIBLE) {
                mCountDownTimerJ2.cancel();
                segundos = 40;
                iniciarBarraProgresoJ2();
            }
        }else if(numeroJugadores == 4){
            if (progressBarAbajo.getVisibility() == View.VISIBLE) {
                mCountDownTimerAbajo.cancel();
                segundos = 40;
                segundos2 = 40;
                iniciarBarraProgresoAbajo();
            } else if (progressBarDerecha.getVisibility() == View.VISIBLE) {
                mCountDownTimerDerecha.cancel();
                segundos = 40;
                segundos2 = 40;
                iniciarBarraProgresoDerecha();
            } else if (progressBarArriba.getVisibility() == View.VISIBLE) {
                mCountDownTimerArriba.cancel();
                segundos = 40;
                segundos2 = 40;
                iniciarBarraProgresoArriba();
            } else if (progressBarIzq.getVisibility() == View.VISIBLE) {
                mCountDownTimerIzq.cancel();
                segundos = 40;
                segundos2 = 40;
                iniciarBarraProgresoIzq();
            }
        }
    }

    void cancelarBarraProgreso() {
        if(numeroJugadores == 2) {
            if (progressBar1.getVisibility() == View.VISIBLE) {
                mCountDownTimerJ1.cancel();
            } else if (progressBar2.getVisibility() == View.VISIBLE) {
                mCountDownTimerJ2.cancel();
            }
        }else if(numeroJugadores == 4){
            segundos = 40;
            segundos2 = 40;
            if (progressBarAbajo.getVisibility() == View.VISIBLE) {
                mCountDownTimerAbajo.cancel();
            } else if (progressBarDerecha.getVisibility() == View.VISIBLE) {
                mCountDownTimerDerecha.cancel();
            }else if (progressBarArriba.getVisibility() == View.VISIBLE) {
                mCountDownTimerArriba.cancel();
            }else if (progressBarIzq.getVisibility() == View.VISIBLE) {
                mCountDownTimerIzq.cancel();
            }
        }
    }

    void animacionAbrirCartas() {
        if(numeroJugadores == 2){
            tvJugador1.animate().rotation(-10).setDuration(500);
            tvJugador3.animate().rotation(10).setDuration(500);

            tvJugador1.animate().translationX(inicio1.x - 30).setDuration(500);
            tvJugador3.animate().translationX(inicio3.x + 30).setDuration(500);
            tvJugador1.animate().translationY(inicio1.y + 15).setDuration(500);
            tvJugador3.animate().translationY(inicio3.y + 15).setDuration(500);
        }else if(numeroJugadores == 4){
            tvJugador1_4J.animate().rotation(-10).setDuration(500);
            tvJugador3_4J.animate().rotation(10).setDuration(500);

            tvJugador1_4J.animate().translationX(inicio1J1.x - 30).setDuration(500);
            tvJugador3_4J.animate().translationX(inicio3J1.x + 30).setDuration(500);
            tvJugador1_4J.animate().translationY(inicio1J1.y + 15).setDuration(500);
            tvJugador3_4J.animate().translationY(inicio3J1.y + 15).setDuration(500);
        }
    }

    void animarDesaparecerMano() {
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) tvJugador1.getLayoutParams();

        manoDedo.animate().translationX(manoDedo.getX()).setDuration(300);
        manoDedo.animate().translationY(manoDedo.getY() + params.height).setDuration(300);

        dedo.animate().translationX(dedo.getX()).setDuration(300);
        dedo.animate().translationY(dedo.getY() + params.height).setDuration(300);
    }

    void aparecerCartas() {
        if(numeroJugadores == 2){
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) tvJugador1.getLayoutParams();

            tvJugador1.setX(tvJugador1.getX());
            tvJugador1.setY(tvJugador1.getY() + params.height);
            tvJugador2.setX(tvJugador2.getX());
            tvJugador2.setY(tvJugador2.getY() + params.height);
            tvJugador3.setX(tvJugador3.getX());
            tvJugador3.setY(tvJugador3.getY() + params.height);
            dedo.setX(dedo.getX());
            dedo.setY(dedo.getY() + params.height);
            manoDedo.setX(manoDedo.getX());
            manoDedo.setY(manoDedo.getY() + params.height);

            tvJugador1.setVisibility(View.VISIBLE);
            tvJugador2.setVisibility(View.VISIBLE);
            tvJugador3.setVisibility(View.VISIBLE);
            manoDedo.setVisibility(View.VISIBLE);
            dedo.setVisibility(View.VISIBLE);

            tvJugador1.animate().translationX(inicio1.x).setDuration(500);
            tvJugador1.animate().translationY(inicio1.y).setDuration(500);
            tvJugador1.bringToFront();

            tvJugador2.animate().translationX(inicio2.x).setDuration(500);
            tvJugador2.animate().translationY(inicio2.y).setDuration(500);
            tvJugador2.bringToFront();

            tvJugador3.animate().translationX(inicio3.x).setDuration(500);
            tvJugador3.animate().translationY(inicio3.y).setDuration(500);
            tvJugador3.bringToFront();

            manoDedo.animate().translationX(inicioManoDedo.x).setDuration(500);
            manoDedo.animate().translationY(inicioManoDedo.y).setDuration(500);

            dedo.animate().translationX(inicioManoDedo.x).setDuration(500);
            dedo.animate().translationY(inicioManoDedo.y).setDuration(500);

        }else if(numeroJugadores == 4){

            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) tvJugador1_4J.getLayoutParams();

            tvJugador1_4J.setX(tvJugador1_4J.getX());
            tvJugador1_4J.setY(tvJugador1_4J.getY() + params.height);
            tvJugador2_4J.setX(tvJugador2_4J.getX());
            tvJugador2_4J.setY(tvJugador2_4J.getY() + params.height);
            tvJugador3_4J.setX(tvJugador3_4J.getX());
            tvJugador3_4J.setY(tvJugador3_4J.getY() + params.height);
            dedo_4J.setX(dedo_4J.getX());
            dedo_4J.setY(dedo_4J.getY() + params.height);
            mano_4J.setX(mano_4J.getX());
            mano_4J.setY(mano_4J.getY() + params.height);

            tvJugador1_4J.setVisibility(View.VISIBLE);
            tvJugador2_4J.setVisibility(View.VISIBLE);
            tvJugador3_4J.setVisibility(View.VISIBLE);
            mano_4J.setVisibility(View.VISIBLE);
            dedo_4J.setVisibility(View.VISIBLE);

            tvJugador1_4J.animate().translationX(inicio1J1.x).setDuration(500);
            tvJugador1_4J.animate().translationY(inicio1J1.y).setDuration(500);
            tvJugador1_4J.bringToFront();

            tvJugador2_4J.animate().translationX(inicio2J1.x).setDuration(500);
            tvJugador2_4J.animate().translationY(inicio2J1.y).setDuration(500);
            tvJugador2_4J.bringToFront();

            tvJugador3_4J.animate().translationX(inicio3J1.x).setDuration(500);
            tvJugador3_4J.animate().translationY(inicio3J1.y).setDuration(500);
            tvJugador3_4J.bringToFront();

            mano_4J.animate().translationX(inicioManoDedo_4J.x).setDuration(500);
            mano_4J.animate().translationY(inicioManoDedo_4J.y).setDuration(500);

            dedo_4J.animate().translationX(inicioManoDedo_4J.x).setDuration(500);
            dedo_4J.animate().translationY(inicioManoDedo_4J.y).setDuration(500);
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // acciones que se ejecutan tras los milisegundos
                animacionAbrirCartas();
                if(mMyId.equals(turno))desbloquearCartas();

            }
        }, 1100);

    }

    void animacionRival(View view) {
        if(numeroJugadores == 2){
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) view.getLayoutParams();

            if (view.equals(tvMesaRival1)) {
                view.setX(view.getX() + 150);
                view.setY(view.getY() - params.height - params.topMargin);

                view.setVisibility(View.VISIBLE);
                view.animate().translationX(inicio1Rival.x).scaleX((float) 0.65).setDuration(500);
                view.animate().translationY(inicio1Rival.y).scaleY((float) 0.65).setDuration(500);
                view.bringToFront();
                hayAnimacionRival1 = true;
                Log.d("KKKKKK", "hay animacion carta rival 1 = true");
            } else if (view.equals(tvMesaRival2)) {
                view.setX(view.getX());
                view.setY(view.getY() - params.height - params.topMargin);

                view.setVisibility(View.VISIBLE);
                view.animate().translationX(inicio2Rival.x).scaleX((float) 0.65).setDuration(500);
                view.animate().translationY(inicio2Rival.y).scaleY((float) 0.65).setDuration(500);
                view.bringToFront();
                hayAnimacionRival2 = true;
                Log.d("KKKKKK", "hay animacion carta rival 2 = true");
            } else if (view.equals(tvMesaRival3)) {
                view.setX(view.getX() - 150);
                view.setY(view.getY() - params.height - params.topMargin);

                view.setVisibility(View.VISIBLE);
                view.animate().translationX(inicio3Rival.x).scaleX((float) 0.65).setDuration(500);
                view.animate().translationY(inicio3Rival.y).scaleY((float) 0.65).setDuration(500);
                view.bringToFront();
                hayAnimacionRival3 = true;
                Log.d("KKKKKK", "hay animacion carta rival 3 = true");
            }
        }else if(numeroJugadores == 4){
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) view.getLayoutParams();

            if (view.equals(tvMesaJ2_C1)) {
                view.setX(view.getX());
                view.setY(0 - view.getHeight());
                //view.setY(view.getY() - params.height - params.topMargin);

                view.setVisibility(View.VISIBLE);
                view.animate().translationX(inicio1RivalJ2.x).scaleX((float) 0.6).setDuration(500);
                view.animate().translationY(inicio1RivalJ2.y).scaleY((float) 0.6).setDuration(500);
                view.bringToFront();

                hayAnimacionDerechaC1 = true;

                //hayAnimacionRival1 = true;
                //Log.d("KKKKKK", "hay animacion carta rival 1 = true");
            } else if (view.equals(tvMesaJ2_C2)) {
                view.setX(view.getX());
                view.setY(0 - view.getHeight());

                view.setVisibility(View.VISIBLE);
                view.animate().translationX(inicio2RivalJ2.x).scaleX((float) 0.6).setDuration(500);
                view.animate().translationY(inicio2RivalJ2.y).scaleY((float) 0.6).setDuration(500);
                view.bringToFront();

                hayAnimacionDerechaC2 = true;

            } else if (view.equals(tvMesaJ2_C3)) {
                view.setX(view.getX());
                view.setY(0);

                view.setVisibility(View.VISIBLE);
                view.animate().translationX(inicio3RivalJ2.x).scaleX((float) 0.6).setDuration(500);
                view.animate().translationY(inicio3RivalJ2.y).scaleY((float) 0.6).setDuration(500);
                view.bringToFront();

                hayAnimacionDerechaC3 = true;

            }
            else
            if (view.equals(tvMesaJ3_C1)) {
                view.setX(view.getX());
                view.setY(0 - view.getHeight());
                //view.setY(view.getY() - params.height - params.topMargin);

                view.setVisibility(View.VISIBLE);
                view.animate().translationX(inicio1RivalJ3.x).scaleX((float) 0.6).setDuration(500);
                view.animate().translationY(inicio1RivalJ3.y).scaleY((float) 0.6).setDuration(500);
                view.bringToFront();

                hayAnimacionArribaC1 = true;

            } else if (view.equals(tvMesaJ3_C2)) {
                view.setX(view.getX());
                view.setY(0 - view.getHeight());

                view.setVisibility(View.VISIBLE);
                view.animate().translationX(inicio2RivalJ3.x).scaleX((float) 0.6).setDuration(500);
                view.animate().translationY(inicio2RivalJ3.y).scaleY((float) 0.6).setDuration(500);
                view.bringToFront();

                hayAnimacionArribaC2 = true;

            } else if (view.equals(tvMesaJ3_C3)) {
                view.setX(view.getX());
                view.setY(0 - view.getHeight());

                view.setVisibility(View.VISIBLE);
                view.animate().translationX(inicio3RivalJ3.x).scaleX((float) 0.6).setDuration(500);
                view.animate().translationY(inicio3RivalJ3.y).scaleY((float) 0.6).setDuration(500);
                view.bringToFront();

                hayAnimacionArribaC3 = true;

            }
            else
            if (view.equals(tvMesaJ4_C1)) {
                view.setX(view.getX());
                view.setY(0 - view.getHeight());

                view.setVisibility(View.VISIBLE);
                view.animate().translationX(inicio3RivalJ4.x).scaleX((float) 0.6).setDuration(500);
                view.animate().translationY(inicio3RivalJ4.y).scaleY((float) 0.6).setDuration(500);
                view.bringToFront();

                hayAnimacionIzqC1 = true;

            } else if (view.equals(tvMesaJ4_C2)) {
                view.setX(view.getX());
                view.setY(0 - view.getHeight());

                view.setVisibility(View.VISIBLE);
                view.animate().translationX(inicio3RivalJ4.x).scaleX((float) 0.6).setDuration(500);
                view.animate().translationY(inicio3RivalJ4.y).scaleY((float) 0.6).setDuration(500);
                view.bringToFront();

                hayAnimacionIzqC2 = true;

            } else if (view.equals(tvMesaJ4_C3)) {
                view.setX(view.getX());
                view.setY(0 - view.getHeight());

                view.setVisibility(View.VISIBLE);
                view.animate().translationX(inicio3RivalJ4.x).scaleX((float) 0.6).setDuration(500);
                view.animate().translationY(inicio3RivalJ4.y).scaleY((float) 0.6).setDuration(500);
                view.bringToFront();

                hayAnimacionIzqC3 = true;
            }
        }
    }

    void animarAparecerMenu() {
        if(numeroJugadores == 2){
            actionButton.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.fab_scale_up);
            actionButton.startAnimation(animation);
        }else if(numeroJugadores == 4){
            actionButton_4J.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.fab_scale_up);
            actionButton_4J.startAnimation(animation);
        }
    }

    void animarDesaparecerMenu() {
        if(numeroJugadores == 2){
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
        }else if(numeroJugadores == 4){
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.fab_scale_down);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    actionButton_4J.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            actionButton_4J.startAnimation(animation);
        }
    }

    // Start the gameplay phase of the game.
    void startGame(boolean multiplayer) {

        switch (numeroJugadores) {

            case 2:
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
                abandonar.setOnClickListener(menuListener);
                tapar.setOnClickListener(menuListener);


                if (mMyId.equals(turno) && ronda == 1) {
                    bloquearCartas();
                    Toast.makeText(getApplicationContext(), "Es tu turno", Toast.LENGTH_SHORT).show();
                    aparecerCartas();
                    progressBar1.setVisibility(View.VISIBLE);
                    progressBar2.setVisibility(View.INVISIBLE);
                    iniciarBarraProgresoJ1();
                    animarAparecerMenu();
                }

                if (!mMyId.equals(turno) && ronda == 1) {
                    bloquearCartas();
                    Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
                    aparecerCartas();
                    progressBar2.setVisibility(View.VISIBLE);
                    progressBar1.setVisibility(View.INVISIBLE);
                    iniciarBarraProgresoJ2();
                    animarDesaparecerMenu();

                }

                switchToScreen(R.id.screen_game);
                mMultiplayer = multiplayer;
                break;

            case 4:
                switchToScreen(R.id.screen_game_4_jugadores);
                mMultiplayer = multiplayer;

                asignarImagenCarta(carta1, tvJugador1_4J);
                asignarImagenCarta(carta2, tvJugador2_4J);
                asignarImagenCarta(carta3, tvJugador3_4J);

                tvJugador1_4J.setOnTouchListener(new MyTouchListener4J());
                tvJugador2_4J.setOnTouchListener(new MyTouchListener4J());
                tvJugador3_4J.setOnTouchListener(new MyTouchListener4J());

                truc_4J.setOnClickListener(menuListener);
                retruque_4J.setOnClickListener(menuListener);
                quatreVal_4J.setOnClickListener(menuListener);
                jocFora_4J.setOnClickListener(menuListener);
                envid_4J.setOnClickListener(menuListener);
                laFalta_4J.setOnClickListener(menuListener);
                salir_4J.setOnClickListener(menuListener);
                meVoy_4J.setOnClickListener(menuListener);
                abandonar_4J.setOnClickListener(menuListener);
                tapar_4J.setOnClickListener(menuListener);

                Handler handler2 = new Handler();
                handler2.postDelayed(new Runnable() {
                    public void run() {
                        // acciones que se ejecutan tras los milisegundos
                        showIconosAlert();
                    }
                }, 3000);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        // acciones que se ejecutan tras los milisegundos
                        if(dialogIconos.isShowing()) dialogIconos.dismiss();

                        if (mMyId.equals(turno) && ronda == 1) {
                            Toast.makeText(getApplicationContext(), "Es tu turno", Toast.LENGTH_SHORT).show();
                            aparecerCartas();
                            animarAparecerMenu();
                            envid_4J.setVisibility(View.GONE);
                            laFalta_4J.setVisibility(View.GONE);
                            progressBarAbajo.setVisibility(View.VISIBLE);
                            iniciarBarraProgresoAbajo();
                        }

                        if (!mMyId.equals(turno) && ronda == 1) {
                            aparecerCartas();
                            animarDesaparecerMenu();
                            Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();

                            if(mMyId.equals(comprobarSiguienteMano())){
                                progressBarIzq.setVisibility(View.VISIBLE);
                                iniciarBarraProgresoIzq();

                            }else if(mMyId.equals(comprobarSegundoMano())){
                                progressBarArriba.setVisibility(View.VISIBLE);
                                iniciarBarraProgresoArriba();

                            }else if(mMyId.equals(comprobarTerceroeMano())){
                                progressBarDerecha.setVisibility(View.VISIBLE);
                                iniciarBarraProgresoDerecha();
                            }

                        }

                        if (mMyId.equals(idJugador1)) txtNumeroJugador.setText("Soy el jugador 1");
                        else if (mMyId.equals(idJugador2)) txtNumeroJugador.setText("Soy el jugador 2");
                        else if (mMyId.equals(idJugador3)) txtNumeroJugador.setText("Soy el jugador 3");
                        else if (mMyId.equals(idJugador4)) txtNumeroJugador.setText("Soy el jugador 4");

                    }
                }, 23000);

                break;
        }
    }

    void cartaSeleccionada() {

        switch (numeroJugadores) {
            case 2:
                //Envio el valor de la carta
                byte[] messageCarta = ("$" + aux.toString()).getBytes();
                enviarValorCarta(messageCarta);
                Log.d("KKKKK", "Ronda: " + ronda);
                Log.d("KKKKK", "valor1: " + valor1);
                Log.d("KKKKK", "valor2: " + valor2);
                Log.d("KKKKK", "valor3: " + valor3);


                Log.d("KKKKK", "Tiro primero? " + tiroPrimero());
                //Caso en el que tiro primero
                if (tiroPrimero()) {
                    cambiarTurno();
                    Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
                    bloquearCartas();
                    enviarMensajeTurno();

                    //Caso en que tiro segundo
                } else {
                    //No hay empate
                    if (!hayEmpate()) {
                        //Gano la ronda
                        if (soyGanadorRonda()) {
                            if (ronda == 1) {
                                misRondasGanadas = 1;
                            } else if (ronda == 2) {
                                misRondasGanadas++;
                            } else if (ronda == 3) {
                                misRondasGanadas = 2;
                            }
                            //He ganado la mano
                            if (misRondasGanadas == 2) {
                                //enviarMensajeHasPerdido();
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        // acciones que se ejecutan tras los milisegundos
                                        mostrarResultadosGanadorMano("PRIMERO");

                                    }
                                }, 1500);

                                //Vuelvo a tirar
                            } else {
                                Toast.makeText(getApplicationContext(), "Es tu turno", Toast.LENGTH_SHORT).show();
                                actualizaRonda();
                                enviarMensajeRonda();
                                enviarMensajeTurno();
                                reiniciarBarraProgreso();
                            }
                            //Pierdo la ronda
                        } else {
                            if ((ronda == 2 && misRondasGanadas == 0) || (ronda == 3)) {
                                //Pierdes en la segunda ronda o en la tercera
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        // acciones que se ejecutan tras los milisegundos
                                        mostrarResultadosPerdedorMano("PRIMERO");
                                    }
                                }, 1500);

                            } else {
                                cambiarTurno();
                                Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
                                bloquearCartas();
                                actualizaRonda();
                                enviarMensajeRonda();
                                enviarMensajeTurno();
                            }
                        }
                        //Hay empate
                    } else {
                        if (ronda == 1) {
                            enviarMensajeHayEmpate();
                            casoEmpatePrimero();
                        } else {
                            casoEmpateTercero();
                        }
                    }
                }
                break;

            case 4:

                //Envio el valor de la carta
                byte[] messageCarta2 = ("$" + aux.toString()).getBytes();
                enviarValorCarta(messageCarta2);
                //No tiro ultimo
                Log.d("KKKKK", "Quien calcula? " + quienCalcula());
                Log.d("KKKKK", "Calculo yo? " + mMyId.equals(quienCalcula()));

                if (!mMyId.equals(quienCalcula())) {
                    cambiarTurno();
                    Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
                    bloquearCartas();
                    enviarMensajeTurno4J(turno);

                    //Tiro ultimo
                } else {
                    if (!hayEmpate4J()) {
                        //Calculo quien gana, tanto si es de mi equipo como si no
                        if (ronda == 1) {
                            ganadorRonda1_4J = calcularGanadorRonda();
                            enviarMensajeGanadorRonda4J(ganadorRonda1_4J);
                            Log.d("KKKKK", "ganador ronda 1 " + ganadorRonda1_4J);
                        } else if (ronda == 2) {
                            ganadorRonda2_4J = calcularGanadorRonda();
                            enviarMensajeGanadorRonda4J(ganadorRonda2_4J);
                            Log.d("KKKKK", "ganador ronda 2 " + ganadorRonda2_4J);
                        } else if (ronda == 3) {
                            ganadorRonda3_4J = calcularGanadorRonda();
                            Log.d("KKKKK", "ganador ronda 3 " + ganadorRonda3_4J);
                        }

                        //El ganador de la ronda es de mi equipo
                        if (esDeMiEquipo(calcularGanadorRonda()) || mMyId.equals(calcularGanadorRonda())) {
                            Log.d("KKKKK", "Gano yo o mi equipo");
                            if (ronda == 1) {
                                rondasGanadasMiEquipo = 1;
                            } else if (ronda == 2) {
                                rondasGanadasMiEquipo++;
                            } else if (ronda == 3) {
                                rondasGanadasMiEquipo = 2;
                            }
                            //Soy ganador
                            if (mMyId.equals(calcularGanadorRonda())) {
                                if (rondasGanadasMiEquipo == 2) {
                                    //enviarMensajeHasPerdido();
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        public void run() {
                                            // acciones que se ejecutan tras los milisegundos
                                            primerMensaje = mMyId;
                                            mostrarResultadosGanadorMano("PRIMERO");

                                        }
                                    }, 1500);

                                    //Vuelvo a tirar
                                } else {
                                    Log.d("KKKKK", "gano yo");
                                    Toast.makeText(getApplicationContext(), "Es tu turno", Toast.LENGTH_SHORT).show();
                                    actualizaRonda();
                                    enviarMensajeRonda();
                                    enviarMensajeTurno4J(turno);
                                    reiniciarBarraProgreso();
                                }
                                //Ha ganado mi compa�ero
                            } else {
                                if (rondasGanadasMiEquipo == 2) {
                                    //enviarMensajeHasPerdido();
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        public void run() {
                                            // acciones que se ejecutan tras los milisegundos
                                            primerMensaje = mMyId;
                                            mostrarResultadosGanadorMano("PRIMERO");

                                        }
                                    }, 1500);
                                }else {
                                Log.d("KKKKK", "gana mi compi");
                                Toast.makeText(getApplicationContext(), "Esperando al jugador", Toast.LENGTH_SHORT).show();
                                    bloquearCartas();
                                    if (ronda == 1){
                                        enviarMensajeTurno4J(ganadorRonda1_4J);
                                        turno = ganadorRonda1_4J;
                                    }
                                    else if (ronda == 2){
                                        enviarMensajeTurno4J(ganadorRonda2_4J);
                                        turno = ganadorRonda2_4J;
                                    }
                                    else if (ronda == 3){
                                        enviarMensajeTurno4J(ganadorRonda3_4J);
                                        turno = ganadorRonda3_4J;
                                    }
                                    animarDesaparecerMenu();
                                    cambiarBarraProgreso();
                                actualizaRonda();
                                enviarMensajeRonda();
                                }
                            }


                            //Mi equipo pierde la ronda
                        } else {
                            if ((ronda == 2 && rondasGanadasMiEquipo == 0) || (ronda == 3)) {
                                //Pierdes en la segunda ronda o en la tercera
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        // acciones que se ejecutan tras los milisegundos
                                        primerMensaje = mMyId;
                                        mostrarResultadosPerdedorMano("PRIMERO");
                                    }
                                }, 1500);

                            } else {
        /*                        Log.d("KKKKK", "perdemos la ronda");
                                cambiarTurno();
                                Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
                                bloquearCartas();
                                if (ronda == 1) enviarMensajeTurno4J(ganadorRonda1_4J);
                                else if (ronda == 2) enviarMensajeTurno4J(ganadorRonda2_4J);
                                else if (ronda == 3) enviarMensajeTurno4J(ganadorRonda3_4J);
                                actualizaRonda();
                                enviarMensajeRonda();*/

                                Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
                                bloquearCartas();
                                if (ronda == 1){
                                    enviarMensajeTurno4J(ganadorRonda1_4J);
                                    turno = ganadorRonda1_4J;
                                }
                                else if (ronda == 2){
                                    enviarMensajeTurno4J(ganadorRonda2_4J);
                                    turno = ganadorRonda2_4J;
                                }
                                else if (ronda == 3){
                                    enviarMensajeTurno4J(ganadorRonda3_4J);
                                    turno = ganadorRonda3_4J;
                                }
                                animarDesaparecerMenu();
                                cambiarBarraProgreso();
                                actualizaRonda();
                                enviarMensajeRonda();

                            }
                        }
                        // Hay empate
                    } else {
                        if (ronda == 1) {
                            enviarMensajeHayEmpate();
                            casoEmpatePrimero();
                        } else {
                            casoEmpateTercero();
                        }
                    }
                }
        }
    }

    void casoEmpatePrimero() {
        showBasicAlert("Empate en la primera ronda!", "La carta que elijas ser? mostrada arriba");
        //Si no soy mano, cambio turno
        if (!mMyId.equals(mano)) {
            switch (numeroJugadores) {
                case 2:
                    Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
                    bloquearCartas();
                    cambiarTurno();
                    enviarMensajeTurno();
                    break;
                case 4:
                    Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
                    bloquearCartas();
                    enviarMensajeTurno4J(mano);
                    break;
            }
        }
        //Si soy mano espero a carta seleccionada tras empate
    }

    void casoEmpateTercero() {
        switch (numeroJugadores) {
            case 2:
                //Caso en el que gano
                if (ganadorRonda1.equals(mMyId)) {
                    //enviarMensajeHasPerdido();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            // acciones que se ejecutan tras los milisegundos
                            mostrarResultadosGanadorMano("PRIMERO");

                        }
                    }, 1500);

                    //Caso en el que pierdo
                } else {
                    //enviarMensajeSumaRonda();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            // acciones que se ejecutan tras los milisegundos
                            mostrarResultadosPerdedorMano("PRIMERO");
                        }
                    }, 1500);
                }
                break;
            case 4:
                //Caso en el que gano
                if (ganadorRonda1_4J.equals(mMyId) || esDeMiEquipo(ganadorRonda1_4J)) {
                    Log.d("KKKKK", "Ganamos la mano");
                    //enviarMensajeHasPerdido();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            // acciones que se ejecutan tras los milisegundos
                            primerMensaje = mMyId;
                            mostrarResultadosGanadorMano("PRIMERO");

                        }
                    }, 1500);

                    //Caso en el que pierdo
                } else {
                    Log.d("KKKKK", "perdemos la mano");
                    //enviarMensajeSumaRonda();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            // acciones que se ejecutan tras los milisegundos
                            primerMensaje = mMyId;
                            mostrarResultadosPerdedorMano("PRIMERO");
                        }
                    }, 1500);
                }

                break;
        }
    }

    void cartaSeleccionadaEmpate() {

        byte[] messageCarta = ("$" + aux.toString()).getBytes();
        enviarValorCarta(messageCarta);

        switch (numeroJugadores) {
            case 2:
                // Si soy mano, tiro y cambio turno
                if (mMyId.equals(mano)) {
                    cambiarTurno();
                    enviarMensajeTurno();
                    Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
                    //Si no soy mano, compruebo quien gana
                } else if (soyGanadorRondaEmpate()) {
                    //Caso en el que gano
                    //enviarMensajeHasPerdido();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            // acciones que se ejecutan tras los milisegundos
                            mostrarResultadosGanadorMano("PRIMERO");

                        }
                    }, 1500);

                } else {
                    //Caso en el que pierdo
                    //enviarMensajeSumaRonda();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            // acciones que se ejecutan tras los milisegundos
                            mostrarResultadosPerdedorMano("PRIMERO");
                        }
                    }, 1500);
                }
                break;

            case 4:
                String realizaCalculo = "";
                if (idJugador1.equals(mano)) {
                    realizaCalculo = idJugador4;
                } else if (idJugador2.equals(mano)) {
                    realizaCalculo = idJugador1;
                } else if (idJugador3.equals(mano)) {
                    realizaCalculo = idJugador2;
                } else if (idJugador4.equals(mano)) {
                    realizaCalculo = idJugador3;
                }
                // Si tengo que calcular
                if (!mMyId.equals(realizaCalculo)) {
                    cambiarTurno();
                    enviarMensajeTurno();
                    Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
                    //Si no soy mano, compruebo quien gana
                } else if (soyGanadorRondaEmpate4J()) {
                    Log.d("ZZZZ", "Soy ganador de ronda empate");
                    //Caso en el que gano
                    //enviarMensajeHasPerdido();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            // acciones que se ejecutan tras los milisegundos
                            primerMensaje = mMyId;
                            mostrarResultadosGanadorMano("PRIMERO");

                        }
                    }, 1500);

                } else {
                    Log.d("ZZZZ", "Soy perdedor de ronda empate");
                    //Caso en el que pierdo
                    //enviarMensajeSumaRonda();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            // acciones que se ejecutan tras los milisegundos
                            primerMensaje = mMyId;
                            mostrarResultadosPerdedorMano("PRIMERO");
                        }
                    }, 1500);
                }
                break;
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

    String quienCalcula() {
        if(numeroJugadores == 2){
            if (ronda == 1 && mano.equals(idJugador1)) {
                return idJugador2;
            } else if (ronda == 1 && mano.equals(idJugador2)) {
                return idJugador1;
            } else if (ronda == 2 && ganadorRonda1.equals(idJugador1)) {
                return idJugador2;
            } else if (ronda == 2 && ganadorRonda1.equals(idJugador2)) {
                return idJugador1;
            } else if (ronda == 3 && ganadorRonda2.equals(idJugador1)) {
                return idJugador2;
            } else if (ronda == 3 && ganadorRonda2.equals(idJugador2)) {
                return idJugador1;
            }
        }
        else if(numeroJugadores == 4){
                if (ronda == 1 && mano.equals(idJugador1)) {
                    return idJugador4;
                } else if (ronda == 1 && mano.equals(idJugador2)) {
                    return idJugador1;
                } else if (ronda == 1 && mano.equals(idJugador3)) {
                    return idJugador2;
                } else if (ronda == 1 && mano.equals(idJugador4)) {
                    return idJugador3;
                } else if (ronda == 2 && ganadorRonda1_4J.equals(idJugador1)) {
                    return idJugador4;
                } else if (ronda == 2 && ganadorRonda1_4J.equals(idJugador2)) {
                    return idJugador1;
                } else if (ronda == 2 && ganadorRonda1_4J.equals(idJugador3)) {
                    return idJugador2;
                } else if (ronda == 2 && ganadorRonda1_4J.equals(idJugador4)) {
                    return idJugador3;
                } else if (ronda == 3 && ganadorRonda2_4J.equals(idJugador1)) {
                    return idJugador4;
                } else if (ronda == 3 && ganadorRonda2_4J.equals(idJugador2)) {
                    return idJugador1;
                } else if (ronda == 3 && ganadorRonda2_4J.equals(idJugador3)) {
                    return idJugador2;
                } else if (ronda == 3 && ganadorRonda2_4J.equals(idJugador4)) {
                    return idJugador3;
                }
        }
        return "";
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

    String calcularGanadorRonda() {
        //Ronda 1
        if (ronda == 1) {
            int[] valores = {valor1_derecha, valor1_arriba, valor1_izq, miValor};
            int max = valores[0];
            for (int i = 0; i <= valores.length - 1; i++) {
                if (max < valores[i]) max = valores[i];
            }
            if (max == miValor) return mMyId;
            if (mMyId.equals(idJugador1)) {
                if (max == valor1_derecha) return idJugador2;
                else if (max == valor1_arriba) return idJugador3;
                else if (max == valor1_izq) return idJugador4;
            } else if (mMyId.equals(idJugador2)) {
                if (max == valor1_derecha) return idJugador3;
                else if (max == valor1_arriba) return idJugador4;
                else if (max == valor1_izq) return idJugador1;
            } else if (mMyId.equals(idJugador3)) {
                if (max == valor1_derecha) return idJugador4;
                else if (max == valor1_arriba) return idJugador1;
                else if (max == valor1_izq) return idJugador2;
            } else if (mMyId.equals(idJugador4)) {
                if (max == valor1_derecha) return idJugador1;
                else if (max == valor1_arriba) return idJugador2;
                else if (max == valor1_izq) return idJugador3;
            }

        }
        //Ronda 2
        if (ronda == 2) {
            int[] valores = {valor2_derecha, valor2_arriba, valor2_izq, miValor};
            int max = valores[0];
            for (int i = 0; i <= valores.length - 1; i++) {
                if (max < valores[i]) max = valores[i];
            }
            if (max == miValor) return mMyId;
            if (mMyId.equals(idJugador1)) {
                if (max == valor2_derecha) return idJugador2;
                else if (max == valor2_arriba) return idJugador3;
                else if (max == valor2_izq) return idJugador4;
            } else if (mMyId.equals(idJugador2)) {
                if (max == valor2_derecha) return idJugador3;
                else if (max == valor2_arriba) return idJugador4;
                else if (max == valor2_izq) return idJugador1;
            } else if (mMyId.equals(idJugador3)) {
                if (max == valor2_derecha) return idJugador4;
                else if (max == valor2_arriba) return idJugador1;
                else if (max == valor2_izq) return idJugador2;
            } else if (mMyId.equals(idJugador4)) {
                if (max == valor2_derecha) return idJugador1;
                else if (max == valor2_arriba) return idJugador2;
                else if (max == valor2_izq) return idJugador3;
            }
        }
        //Ronda 3
        if (ronda == 3) {
            int[] valores = {valor3_derecha, valor3_arriba, valor3_izq, miValor};
            int max = valores[0];
            for (int i = 0; i <= valores.length - 1; i++) {
                if (max < valores[i]) max = valores[i];
            }
            if (max == miValor) return mMyId;
            if (mMyId.equals(idJugador1)) {
                if (max == valor3_derecha) return idJugador2;
                else if (max == valor3_arriba) return idJugador3;
                else if (max == valor3_izq) return idJugador4;
            } else if (mMyId.equals(idJugador2)) {
                if (max == valor3_derecha) return idJugador3;
                else if (max == valor3_arriba) return idJugador4;
                else if (max == valor3_izq) return idJugador1;
            } else if (mMyId.equals(idJugador3)) {
                if (max == valor3_derecha) return idJugador4;
                else if (max == valor3_arriba) return idJugador1;
                else if (max == valor3_izq) return idJugador2;
            } else if (mMyId.equals(idJugador4)) {
                if (max == valor3_derecha) return idJugador1;
                else if (max == valor3_arriba) return idJugador2;
                else if (max == valor3_izq) return idJugador3;
            }
        }

        return "";

    }

    boolean soyGanadorRondaEmpate() {
        if (miValor > valorEmpate) {
            return true;
        }
        return false;
    }

    boolean soyGanadorRondaEmpate4J() {
        int maximoMiequipo = 0;
        int maximoEquipoRival = 0;

        if (miValor > valorEmpateArriba) maximoMiequipo = miValor;
        else maximoMiequipo = valorEmpateArriba;

        if (valorEmpateDerecha > valorEmpateIzq) maximoEquipoRival = valorEmpateDerecha;
        else maximoEquipoRival = valorEmpateIzq;

        if (maximoMiequipo > maximoEquipoRival) {
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

    boolean hayEmpate4J() {
        if (ronda == 1) {
            int maximoMiequipo = 0;
            int maximoEquipoRival = 0;

            if (miValor > valor1_arriba) maximoMiequipo = miValor;
            else maximoMiequipo = valor1_arriba;

            if (valor1_derecha > valor1_izq) maximoEquipoRival = valor1_derecha;
            else maximoEquipoRival = valor1_izq;

            if (maximoMiequipo == maximoEquipoRival) {
                hayEmpate4J = true;
                return true;
            }

        } else if (ronda == 2) {

            int maximoMiequipo = 0;
            int maximoEquipoRival = 0;

            if (miValor > valor2_arriba) maximoMiequipo = miValor;
            else maximoMiequipo = valor2_arriba;

            if (valor2_derecha > valor2_izq) maximoEquipoRival = valor2_derecha;
            else maximoEquipoRival = valor2_izq;

            if (maximoMiequipo == maximoEquipoRival) {
                hayEmpate4J = true;
                return true;
            }

        } else if (ronda == 3) {

            int maximoMiequipo = 0;
            int maximoEquipoRival = 0;

            if (miValor > valor3_arriba) maximoMiequipo = miValor;
            else maximoMiequipo = valor3_arriba;

            if (valor3_derecha > valor3_izq) maximoEquipoRival = valor3_derecha;
            else maximoEquipoRival = valor3_izq;

            if (maximoMiequipo == maximoEquipoRival) {
                hayEmpate4J = true;
                return true;
            }

        }
        return false;
    }


    void actualizaRonda() {
        if (ronda < 3) ronda++;

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
        Log.d("FFFFFFF", "Hay truc? " + hayTruc);
        Log.d("FFFFFFF", "Hay retruc? " + hayRetruc);
        Log.d("FFFFFFF", "Hay cuatre? " + hayCuatreVal);
        Log.d("FFFFFFF", "Hay joc? " + hayJocFora);
        Log.d("FFFFFFF", "Hay envid? " + hayEnvid);

        if (!hayTruc) puntosTruc = NO_QUIERO_TRUC;
        if (hayTruc && !hayRetruc) puntosTruc = TRUC;
        if (hayRetruc && !hayCuatreVal) puntosTruc = RETRUC;
        if (hayCuatreVal && !hayJocFora) puntosTruc = CUATRE_VAL;
        if (hayJocFora) puntosTruc = 24;

        switch (numeroJugadores) {
            case 2:
                puntosTotalesMios += (puntosTruc + puntosEnvid);
                botonMarcadorAbajo.setProgress(0);
                botonMarcadorAbajo.setIndeterminateProgressMode(true); // turn on indeterminate progress
                botonMarcadorAbajo.setProgress(50); // set progress > 0 & < 100 to display indeterminate progress
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        // acciones que se ejecutan tras los milisegundos
                        botonMarcadorAbajo.setCompleteText(Integer.toString(puntosTotalesMios));
                        botonMarcadorAbajo.setProgress(100);
                    }
                }, 5000);
                actualizarMarcador2(puntosTotalesMios, "GANADOR", contador);
                break;
            case 4:
                puntosTotalesMios += (puntosTruc + puntosEnvid);
                //marcador.setText("Yo: "+puntosTotalesMios);
                //actualizarMarcador2(puntosTotalesMios, "GANADOR", contador);
                botonMarcadorAbajo.setProgress(0);
                botonMarcadorAbajo.setIndeterminateProgressMode(true); // turn on indeterminate progress
                botonMarcadorAbajo.setProgress(50); // set progress > 0 & < 100 to display indeterminate progress
                Handler handler2 = new Handler();
                handler2.postDelayed(new Runnable() {
                    public void run() {
                        // acciones que se ejecutan tras los milisegundos
                        botonMarcadorAbajo.setCompleteText(Integer.toString(puntosTotalesMios));
                        botonMarcadorAbajo.setProgress(100);
                    }
                }, 5000);
                if (contador.equals("PRIMERO")) {
                    actualizarMarcador2(puntosTotalesMios, "GANADOR", contador);
                } else actualizarMarcador2_4J(puntosTotalesMios, "GANADOR", contador);
                break;
        }

    }

    public void mostrarResultadosPerdedorMano(String contador) {

        switch (numeroJugadores) {
            case 2:
                puntosTotalesMios += puntosEnvid;
                botonMarcadorAbajo.setProgress(0);
                botonMarcadorAbajo.setIndeterminateProgressMode(true); // turn on indeterminate progress
                botonMarcadorAbajo.setProgress(50); // set progress > 0 & < 100 to display indeterminate progress
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        // acciones que se ejecutan tras los milisegundos
                        botonMarcadorAbajo.setCompleteText(Integer.toString(puntosTotalesMios));
                        botonMarcadorAbajo.setProgress(100);
                    }
                }, 5000);
                actualizarMarcador2(puntosTotalesMios, "PERDEDOR", contador);
                break;
            case 4:
                puntosTotalesMios += puntosEnvid;
                //marcador.setText("Yo: "+puntosTotalesMios);
                //actualizarMarcador2(puntosTotalesMios, "PERDEDOR", contador);
                if (contador.equals("PRIMERO")) {
                    actualizarMarcador2(puntosTotalesMios, "PERDEDOR", contador);
                } else actualizarMarcador2_4J(puntosTotalesMios, "PERDEDOR", contador);
                break;
        }

    }

    String comprobarGanadorPartida() {
        if (puntosTotalesMios >= 24) {
            return "YO";
        } else if (puntosTotalesJugador2 >= 24) {
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
     * K: Mensaje de envid aceptado y notificaci?n de ganador de envid.
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

    public void enviarMensajeTurno4J(String t) {
        byte[] messageTurno = t.getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageTurno,
                        mRoomId, p.getParticipantId());
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

    public void enviarMensajeRepartir4J() {
        Log.d("BBBBBBBBB", "Enviando el mensaje de repartir...");
        byte[] messageRepartir = ("S" + " " + sCartasJ2 + " " + sCartasJ3 + " " + sCartasJ4 + " " + sCartasJ1).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageRepartir,
                        mRoomId, p.getParticipantId());
                Log.d("BBBBBBBBB", "Mensaje enviado");

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
        switch (numeroJugadores) {
            case 2:
                byte[] messageEnvid = ("N " + envid).getBytes();
                for (Participant p : mParticipants) {
                    if (!p.getParticipantId().equals(mMyId)) {
                        Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageEnvid,
                                mRoomId, p.getParticipantId());
                    }
                }
                break;
            case 4:
                for (Participant p : mParticipants) {
                    if (!p.getParticipantId().equals(mMyId)) {
                        Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, "N".getBytes(),
                                mRoomId, p.getParticipantId());
                    }
                }
                break;
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

    public void enviarMensajeVuelvoAEnvidar(String sender) {
        if(numeroJugadores == 2){
            byte[] messageVuelvo = ("V " + miEnvid).getBytes();
            for (Participant p : mParticipants) {
                if (!p.getParticipantId().equals(mMyId)) {
                    Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageVuelvo,
                            mRoomId, p.getParticipantId());
                }
            }
        }else if(numeroJugadores == 4){
            byte[] messageVuelvo = ("V "+sender).getBytes();
            for (Participant p : mParticipants) {
                if (!p.getParticipantId().equals(mMyId)) {
                    Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageVuelvo,
                            mRoomId, p.getParticipantId());
                }
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

    public void enviarMensajeLaFalta(int caso, String sender) {
        if(numeroJugadores ==2){
            byte[] messageFalta = ("F " + miEnvid + " " + caso).getBytes();
            for (Participant p : mParticipants) {
                if (!p.getParticipantId().equals(mMyId)) {
                    Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageFalta,
                            mRoomId, p.getParticipantId());
                }
            }
        }else if(numeroJugadores == 4){
            byte[] messageFalta = ("F " + sender + " " + caso).getBytes();
            for (Participant p : mParticipants) {
                if (!p.getParticipantId().equals(mMyId)) {
                    Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageFalta,
                            mRoomId, p.getParticipantId());
                }
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

  /*  public void enviarMensajeQuieroTruc_4J(String sender, String respuesta, int caso) {
        byte[] messageQuieroTruc = ("W "+respuesta+" "+caso+" "+sender).getBytes();
        Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageQuieroTruc,
                        mRoomId, sender);


    }*/
    public void enviarMensajeReQuaJoc_4J(String respuesta, int caso) {
        byte[] messageQuieroTruc = ("W "+respuesta+" "+caso).getBytes();
      /*  String rival1 = "";
        String rival2 = "";
        if(esDeMiEquipo(idJugador1) || esDeMiEquipo(idJugador3)){
            rival1 = idJugador2;
            rival2 = idJugador4;
        }
        else if(esDeMiEquipo(idJugador2) || esDeMiEquipo(idJugador4)){
            rival1 = idJugador1;
            rival2 = idJugador3;
        }
        Log.d("KKKKK", "le envio "+respuesta+" a: "+rival1+" y a "+rival2);
        Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageQuieroTruc,
                mRoomId, rival1);
        Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageQuieroTruc,
                mRoomId, rival2);*/

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

    public void enviarMensajeNoQuieroTruc(int caso, String primer) {
        byte[] messageNoQuieroTruc = ("D " + caso +" "+primer).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageNoQuieroTruc,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void actualizarMarcador2(int puntosTotales, String usuario, String contador) {
        byte[] messageMarcador = ("Z " + puntosTotales + " " + usuario + " " + contador).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageMarcador,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void actualizarMarcador2_4J(int puntosTotales, String usuario, String contador) {
        byte[] messageMarcador = ("Z " + puntosTotales + " " + usuario + " " + contador).getBytes();
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(contador)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageMarcador,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void enviarMensajeGanadorRonda4J(String ganador) {
        byte[] messageGanadorRonda4J = ("O " + ganador).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageGanadorRonda4J,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void enviarMensajeListo() {
        byte[] messageGanadorRonda4J = ("H").getBytes();
        Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageGanadorRonda4J,
                        mRoomId, mano);

    }

    public void enviarPuntuacionCompi(int puntosMios, int puntosRival) {
        byte[] messageGanadorRonda4J = ("A " + puntosMios +" "+puntosRival).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId) && esDeMiEquipo(p.getParticipantId())) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageGanadorRonda4J,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void enviarMensajeBarraProgreso(String caso) {
        byte[] messageGanadorRonda4J = ("1 "+caso).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId) && !esDeMiEquipo(p.getParticipantId())) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageGanadorRonda4J,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void enviarMensajeDesconectado() {
        byte[] messageDesconectado = ("2").getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageDesconectado,
                        mRoomId, p.getParticipantId());
            }
        }
    }

    public void enviarMensajeSenyas(String senya1, String senya2) {
        byte[] messageSenyas = ("3 "+senya1+" "+senya2).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageSenyas,
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
                    if (numeroJugadores == 2) {
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
                            comprobarSonidosCartas(newCarta);
                            animacionRival(tvMesaRival1);
                            Log.d("KKKKK", "Animando la carta rival 1");
                        } else if (tvMesaRival2.getVisibility() == View.INVISIBLE) {
                            if (hayEmpate) {
                                valorEmpate = valor;
                            } else valor2 = valor;
                            asignarImagenCarta(newCarta, tvMesaRival2);
                            comprobarSonidosCartas(newCarta);
                            animacionRival(tvMesaRival2);
                            Log.d("KKKKK", "Animando la carta rival 2");
                        } else if (tvMesaRival3.getVisibility() == View.INVISIBLE) {
                            if (hayEmpate) {
                                valorEmpate = valor;
                            } else valor3 = valor;
                            asignarImagenCarta(newCarta, tvMesaRival3);
                            comprobarSonidosCartas(newCarta);
                            animacionRival(tvMesaRival3);
                            Log.d("KKKKK", "Animando la carta rival 3");
                        }
                    }//2 jugadores

                    else if (numeroJugadores == 4) {
                        String sBuf = new String(buf, "UTF-8");
                        String arrayBuf[] = sBuf.split(" ");
                        String palo = arrayBuf[1];
                        String numeroCarta = arrayBuf[0].substring(1);
                        Carta newCarta = new Carta(numeroCarta, palo, arrayBuf[2]);
                        int valor = Integer.parseInt(arrayBuf[2]);

                        if (mMyId.equals(idJugador1)) {

                            if (sender.equals(idJugador2)) {
                                if (tvMesaJ2_C1.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateDerecha = valor;
                                    } else valor1_derecha = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ2_C1);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ2_C1);
                                    tvMesaJ2_C1.setVisibility(View.VISIBLE);
                                } else if (tvMesaJ2_C2.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateDerecha = valor;
                                    } else valor2_derecha = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ2_C2);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ2_C2);
                                    tvMesaJ2_C2.setVisibility(View.VISIBLE);
                                } else if (tvMesaJ2_C3.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateDerecha = valor;
                                    } else valor3_derecha = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ2_C3);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ2_C3);
                                    tvMesaJ2_C3.setVisibility(View.VISIBLE);
                                }
                            } else if (sender.equals(idJugador3)) {

                                if (tvMesaJ3_C1.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateArriba = valor;
                                    } else valor1_arriba = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ3_C1);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ3_C1);

                                } else if (tvMesaJ3_C2.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateArriba = valor;
                                    } else valor2_arriba = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ3_C2);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ3_C2);

                                } else if (tvMesaJ3_C3.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateArriba = valor;
                                    } else valor3_arriba = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ3_C3);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ3_C3);

                                }
                            } else if (sender.equals(idJugador4)) {
                                if (tvMesaJ4_C1.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateIzq = valor;
                                    } else valor1_izq = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ4_C1);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ4_C1);

                                } else if (tvMesaJ4_C2.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateIzq = valor;
                                    } else valor2_izq = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ4_C2);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ4_C2);

                                } else if (tvMesaJ4_C3.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateIzq = valor;
                                    } else valor3_izq = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ4_C3);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ4_C3);

                                }
                            }

                        } else if (mMyId.equals(idJugador2)) {

                            if (sender.equals(idJugador1)) {
                                if (tvMesaJ4_C1.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateIzq = valor;
                                    } else valor1_izq = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ4_C1);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ4_C1);

                                } else if (tvMesaJ4_C2.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateIzq = valor;
                                    } else valor2_izq = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ4_C2);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ4_C2);

                                } else if (tvMesaJ4_C3.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateIzq = valor;
                                    } else valor3_izq = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ4_C3);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ4_C3);
                                }
                            } else if (sender.equals(idJugador3)) {
                                if (tvMesaJ2_C1.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateDerecha = valor;
                                    } else valor1_derecha = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ2_C1);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ2_C1);

                                } else if (tvMesaJ2_C2.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateDerecha = valor;
                                    } else valor2_derecha = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ2_C2);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ2_C2);

                                } else if (tvMesaJ2_C3.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateDerecha = valor;
                                    } else valor3_derecha = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ2_C3);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ2_C3);

                                }
                            } else if (sender.equals(idJugador4)) {
                                if (tvMesaJ3_C1.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateArriba = valor;
                                    } else valor1_arriba = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ3_C1);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ3_C1);

                                } else if (tvMesaJ3_C2.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateArriba = valor;
                                    } else valor2_arriba = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ3_C2);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ3_C2);

                                } else if (tvMesaJ3_C3.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateArriba = valor;
                                    } else valor3_arriba = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ3_C3);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ3_C3);
                                }
                            }
                        } else if (mMyId.equals(idJugador3)) {

                            if (sender.equals(idJugador1)) {
                                if (tvMesaJ3_C1.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateArriba = valor;
                                    } else valor1_arriba = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ3_C1);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ3_C1);

                                } else if (tvMesaJ3_C2.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateArriba = valor;
                                    } else valor2_arriba = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ3_C2);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ3_C2);

                                } else if (tvMesaJ3_C3.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateArriba = valor;
                                    } else valor3_arriba = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ3_C3);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ3_C3);

                                }
                            } else if (sender.equals(idJugador2)) {
                                if (tvMesaJ4_C1.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateIzq = valor;
                                    } else valor1_izq = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ4_C1);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ4_C1);

                                } else if (tvMesaJ4_C2.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateIzq = valor;
                                    } else valor2_izq = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ4_C2);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ4_C2);

                                } else if (tvMesaJ4_C3.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateIzq = valor;
                                    } else valor3_izq = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ4_C3);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ4_C3);

                                }
                            } else if (sender.equals(idJugador4)) {
                                if (tvMesaJ2_C1.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateDerecha = valor;
                                    } else valor1_derecha = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ2_C1);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ2_C1);

                                } else if (tvMesaJ2_C2.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateDerecha = valor;
                                    } else valor2_derecha = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ2_C2);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ2_C2);

                                } else if (tvMesaJ2_C3.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateDerecha = valor;
                                    } else valor3_derecha = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ2_C3);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ2_C3);

                                }
                            }
                        } else if (mMyId.equals(idJugador4)) {

                            if (sender.equals(idJugador1)) {
                                if (tvMesaJ2_C1.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateDerecha = valor;
                                    } else valor1_derecha = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ2_C1);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ2_C1);

                                } else if (tvMesaJ2_C2.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateDerecha = valor;
                                    } else valor2_derecha = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ2_C2);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ2_C2);

                                } else if (tvMesaJ2_C3.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateDerecha = valor;
                                    } else valor3_derecha = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ2_C3);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ2_C3);

                                }
                            } else if (sender.equals(idJugador2)) {
                                if (tvMesaJ3_C1.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateArriba = valor;
                                    } else valor1_arriba = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ3_C1);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ3_C1);

                                } else if (tvMesaJ3_C2.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateArriba = valor;
                                    } else valor2_arriba = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ3_C2);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ3_C2);

                                } else if (tvMesaJ3_C3.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateArriba = valor;
                                    } else valor3_arriba = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ3_C3);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ3_C3);

                                }
                            } else if (sender.equals(idJugador3)) {
                                if (tvMesaJ4_C1.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateIzq = valor;
                                    } else valor1_izq = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ4_C1);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ4_C1);

                                } else if (tvMesaJ4_C2.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateIzq = valor;
                                    } else valor2_izq = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ4_C2);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ4_C2);

                                } else if (tvMesaJ4_C3.getVisibility() == View.INVISIBLE) {
                                    if (hayEmpate4J) {
                                        valorEmpateIzq = valor;
                                    } else valor3_izq = valor;
                                    asignarImagenCarta(newCarta, tvMesaJ4_C3);
                                    comprobarSonidosCartas(newCarta);
                                    animacionRival(tvMesaJ4_C3);

                                }
                            }

                        } //4 jugadores
                    }

                    break;
                case 'R':
                    actualizaRonda();
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
                    if (ronda == 1)
                        showBasicAlert("Empate en la primera ronda!", "La carta que eljas ser� mostrada arriba");
                    hayEmpate = true;
                    hayEmpate4J = true;
                    break;

                case 'S':
                    if (numeroJugadores == 2) {
                        resetAll();
                        String ssBuf = new String(buf, "UTF-8");
                        String[] arrayCartasJ2 = ssBuf.split(" ");
                        numCarta[0] = Integer.parseInt(arrayCartasJ2[1]);
                        numCarta[1] = Integer.parseInt(arrayCartasJ2[2]);
                        numCarta[2] = Integer.parseInt(arrayCartasJ2[3]);
                        repartir(numCarta);
                        reproducirSonidoRepartir();
                        carta1 = new Carta(manoJugador.get(0).getNumero(), manoJugador.get(0).getPalo(), manoJugador.get(0).getValor());
                        carta2 = new Carta(manoJugador.get(1).getNumero(), manoJugador.get(1).getPalo(), manoJugador.get(1).getValor());
                        carta3 = new Carta(manoJugador.get(2).getNumero(), manoJugador.get(2).getPalo(), manoJugador.get(2).getValor());
                        cerrarDialogoAndStart(4000);
                    } else if (numeroJugadores == 4) {

                        resetAll();
                        Log.d("TTTTTT", "Recibo las cartas");
                        String ssBuf = new String(buf, "UTF-8");
                        Log.d("TTTTTT", ssBuf.toString());
                        arrayCartasJugadores = ssBuf.split(" ");

                        ArrayList<String> ids = new ArrayList<>(4);
                        ids.add(idJugador1);
                        ids.add(idJugador2);
                        ids.add(idJugador3);
                        ids.add(idJugador4);
                        if (ids.get(0).equals(mano)) ids.remove(0);
                        else if (ids.get(1).equals(mano)) ids.remove(1);
                        else if (ids.get(2).equals(mano)) ids.remove(2);
                        else if (ids.get(3).equals(mano)) ids.remove(3);


                        if (mMyId.equals(ids.get(0))) {
                            numCarta[0] = Integer.parseInt(arrayCartasJugadores[1]);
                            numCarta[1] = Integer.parseInt(arrayCartasJugadores[2]);
                            numCarta[2] = Integer.parseInt(arrayCartasJugadores[3]);
                        } else if (mMyId.equals(ids.get(1))) {
                            numCarta[0] = Integer.parseInt(arrayCartasJugadores[4]);
                            numCarta[1] = Integer.parseInt(arrayCartasJugadores[5]);
                            numCarta[2] = Integer.parseInt(arrayCartasJugadores[6]);
                        } else if (mMyId.equals(ids.get(2))) {
                            numCarta[0] = Integer.parseInt(arrayCartasJugadores[7]);
                            numCarta[1] = Integer.parseInt(arrayCartasJugadores[8]);
                            numCarta[2] = Integer.parseInt(arrayCartasJugadores[9]);
                        }
                        reproducirSonidoRepartir();
                        repartir(numCarta);
                        carta1 = new Carta(manoJugador.get(0).getNumero(), manoJugador.get(0).getPalo(), manoJugador.get(0).getValor());
                        carta2 = new Carta(manoJugador.get(1).getNumero(), manoJugador.get(1).getPalo(), manoJugador.get(1).getValor());
                        carta3 = new Carta(manoJugador.get(2).getNumero(), manoJugador.get(2).getPalo(), manoJugador.get(2).getValor());
                        cerrarDialogoAndStart(4000);
                        Log.d("TTTTTT", "Start game");
                    }

                    break;

                case 'M':
                    String aux = new String(buf, "UTF-8");
                    String nuevaMano[] = aux.split(" ");
                    mano = nuevaMano[1];
                    turno = mano;
                    inicializarMano();
                    break;

                case 'N':

                    switch (numeroJugadores) {
                        case 2:
                            String aux1 = new String(buf, "UTF-8");
                            String suEnvid[] = aux1.split(" ");
                            envidOtro = Integer.parseInt(suEnvid[1]);
                            showSingleChoiceAlertEnvid("Tu rival ha envidado", R.array.envid);
                            envid.setVisibility(View.GONE);
                            laFalta.setVisibility(View.GONE);
                            cambiarBarraProgreso();
                            break;

                        case 4:
                            if (esDeMiEquipo(sender)) {
                                //Animar bocadillo del compi
                                bocadilloArriba.setText("Envido!");
                                animarTextoAccion(bocadilloArriba);
                                cancelarBarraProgreso();
                                activarDesactivarBarraCompi("DESACTIVAR");
                                progressBarIzq.setVisibility(View.VISIBLE);
                                iniciarBarraProgresoIzq();

                            } else {
                                String calculaEnvid = "";
                                if (mano.equals(idJugador1) && sender.equals(idJugador3)) {
                                    calculaEnvid = idJugador4;
                                } else if (mano.equals(idJugador1) && sender.equals(idJugador4)) {
                                    calculaEnvid = idJugador3;
                                } else if (mano.equals(idJugador2) && sender.equals(idJugador4)) {
                                    calculaEnvid = idJugador1;
                                } else if (mano.equals(idJugador2) && sender.equals(idJugador1)) {
                                    calculaEnvid = idJugador4;
                                } else if (mano.equals(idJugador3) && sender.equals(idJugador1)) {
                                    calculaEnvid = idJugador2;
                                } else if (mano.equals(idJugador3) && sender.equals(idJugador2)) {
                                    calculaEnvid = idJugador1;
                                } else if (mano.equals(idJugador4) && sender.equals(idJugador2)) {
                                    calculaEnvid = idJugador3;
                                } else if (mano.equals(idJugador4) && sender.equals(idJugador3)) {
                                    calculaEnvid = idJugador2;
                                }
                                Log.d("EEEEEE", "Quien calcula: "+calculaEnvid);
                                if (mMyId.equals(calculaEnvid)) {
                                    cancelarBarraProgreso();
                                    barrasInvisibles();
                                    activarDesactivarMiBarra("ACTIVAR");
                                    showSingleChoiceAlertEnvid_4J("Tu rival ha envidado", R.array.envid, sender);
                                } else {
                                    //Animar bocadillo del compi del que tiene que calcular
                                    bocadilloDerecha.setText("Envide!");
                                    animarTextoAccion(bocadilloDerecha);
                                    cancelarBarraProgreso();
                                    barrasInvisibles();
                                    activarDesactivarBarraCompi("ACTIVAR");
                                }
                            }
                            envid_4J.setVisibility(View.GONE);
                            laFalta_4J.setVisibility(View.GONE);
                            break;
                    }
                    break;

                case 'K':
                    String aux2 = new String(buf, "UTF-8");
                    String ganador[] = aux2.split(" ");
                    hayEnvid = true;
                    ganadorEnvid = ganador[1];
                    Log.d("EEEEEE", "Ganador del envid es: "+ganadorEnvid);
                    envidOtro = Integer.parseInt(ganador[2]);
                    int caso = Integer.parseInt(ganador[3]);

                    if(numeroJugadores == 2){
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
                                } else cambiarBarraProgreso();
                                break;
                        }
                        if (ganadorEnvid.equals(mMyId) && caso == 1) puntosEnvid = ENVID;
                        if (ganadorEnvid.equals(mMyId) && caso == 2) puntosEnvid = TORNE;
                        if (ganadorEnvid.equals(mMyId) && caso == 3) puntosEnvid = 24;

                        if (mMyId.equals(turno)) {
                            desbloquearCartas();
                            animarAparecerMenu();
                        }


                    }else if(numeroJugadores == 4){

                        switch (caso) {
                            case 1:
                                animarBocadillosEnvid(1, sender, "QUIERO");
                                break;
                            case 2:
                                animarBocadillosEnvid(2, sender, "QUIERO");
                                break;
                            case 3:
                                animarBocadillosEnvid(3, sender, "QUIERO");
                                break;
                        }

                        if ((ganadorEnvid.equals(mMyId) || esDeMiEquipo(ganadorEnvid)) && caso == 1) puntosEnvid = ENVID;
                        if ((ganadorEnvid.equals(mMyId) || esDeMiEquipo(ganadorEnvid)) && caso == 2) puntosEnvid = TORNE;
                        if ((ganadorEnvid.equals(mMyId) || esDeMiEquipo(ganadorEnvid)) && caso == 3){
                            if (puntosTotalesJugador2 <= 12) {
                                puntosEnvid = 24;
                            } else if (puntosTotalesJugador2 > 12) {
                                puntosEnvid = 24 - puntosTotalesJugador2;
                            }
                        }

                        if (turno.equals(mMyId)) {
                            cancelarBarraProgreso();
                            barrasInvisibles();
                            activarDesactivarMiBarra("ACTIVAR");
                            desbloquearCartas();
                            animarAparecerMenu();
                        }else cambiarBarraProgreso();
                    }

                    break;

                case 'V':
                    if(numeroJugadores == 2){
                        String aux3 = new String(buf, "UTF-8");
                        String otro[] = aux3.split(" ");
                        envidOtro = Integer.parseInt(otro[1]);
                        hayVuelvo = true;
                        showSingleChoiceAlertVuelvo("Tu rival ha vuelto a envidar", R.array.envid2);
                        cambiarBarraProgreso();

                    }else if(numeroJugadores == 4){
                        String aux3 = new String(buf, "UTF-8");
                        String otro[] = aux3.split(" ");
                        String yo = otro[1];

                        if(mMyId.equals(yo)){
                            hayVuelvo = true;
                            cancelarBarraProgreso();
                            barrasInvisibles();
                            activarDesactivarMiBarra("ACTIVAR");
                            showSingleChoiceAlertVuelvo_4J("Tu rival ha vuelto a envidar", R.array.envid2, sender);

                        }else {
                            if(!mMyId.equals(sender) && esDeMiEquipo(sender)){
                                bocadilloArriba.setText("Vuelvo a envidar!");
                                animarTextoAccion(bocadilloArriba);
                                cancelarBarraProgreso();
                                barrasInvisibles();
                                progressBarIzq.setVisibility(View.VISIBLE);
                                iniciarBarraProgresoIzq();

                            } else if(!mMyId.equals(sender) && !esDeMiEquipo(sender)){
                                bocadilloDerecha.setText("Vuelvo a envidar!");
                                animarTextoAccion(bocadilloDerecha);
                                cancelarBarraProgreso();
                                barrasInvisibles();
                                progressBarArriba.setVisibility(View.VISIBLE);
                                iniciarBarraProgresoArriba();
                            }
                        }
                    }
                    break;

                case 'X':
                    String aux4 = new String(buf, "UTF-8");
                    String otro1[] = aux4.split(" ");
                    int caso1 = Integer.parseInt(otro1[1]);
                    if(numeroJugadores == 2){
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
                                if (hayVuelvo) {
                                    puntosEnvid = TORNE;
                                } else puntosEnvid = ENVID;

                                textoAccion2.setText("No quiero la falta");
                                animarTextoAccion(textoAccion2);
                                if (!mMyId.equals(turno)) {
                                    reiniciarBarraProgreso();
                                } else cambiarBarraProgreso();
                                break;
                            case 4:

                                puntosEnvid = NO_QUIERO_ENVID;
                                textoAccion2.setText("No quiero la falta");
                                animarTextoAccion(textoAccion2);
                                cambiarBarraProgreso();
                                break;

                        }

                    //COPIAR DE ARRIBA
                    }else if(numeroJugadores ==4){
                        if(!esDeMiEquipo(sender)){
                            switch (caso1) {
                                case 1:
                                    animarBocadillosEnvid(1, sender, "NOQUIERO");
                                    puntosEnvid = NO_QUIERO_ENVID;
                                    Log.d("KKKKKK", "Puntos del envid:" +puntosEnvid);
                                    break;
                                case 2:
                                    animarBocadillosEnvid(2, sender, "NOQUIERO");
                                    puntosEnvid = ENVID;
                                    Log.d("KKKKKK", "Puntos del envid:" +puntosEnvid);
                                    break;
                                case 3:
                                    animarBocadillosEnvid(3, sender, "NOQUIERO");
                                    if (hayVuelvo) {
                                        puntosEnvid = TORNE;
                                    } else puntosEnvid = ENVID;
                                    Log.d("KKKKKK", "Puntos del envid:" +puntosEnvid);
                                    break;
                                case 4:
                                    animarBocadillosEnvid(3, sender, "NOQUIERO");
                                    puntosEnvid = NO_QUIERO_ENVID;
                                    Log.d("KKKKKK", "Puntos del envid:" +puntosEnvid);
                                    break;

                            }
                        }else {
                            switch (caso1) {
                                case 1:
                                    animarBocadillosEnvid(1, sender, "NOQUIERO");
                                    break;
                                case 2:
                                    animarBocadillosEnvid(2, sender, "NOQUIERO");
                                    break;
                                case 3:
                                    animarBocadillosEnvid(3, sender, "NOQUIERO");
                                    break;
                                case 4:
                                    animarBocadillosEnvid(3, sender, "NOQUIERO");
                                    break;
                            }
                        }
                    }
                    if (turno.equals(mMyId)) {
                        cancelarBarraProgreso();
                        barrasInvisibles();
                        activarDesactivarMiBarra("ACTIVAR");
                        desbloquearCartas();
                        animarAparecerMenu();
                    }else cambiarBarraProgreso();

                    break;

                case 'D':
                    String aux7 = new String(buf, "UTF-8");
                    String otro4[] = aux7.split(" ");
                    int caso2 = Integer.parseInt(otro4[1]);

                    if(numeroJugadores == 2){
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
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                // acciones que se ejecutan tras los milisegundos
                                mostrarResultadosGanadorMano("PRIMERO");

                            }
                        }, 1500);

                    }else if(numeroJugadores == 4){
                        primerMensaje = otro4[2];
                        //Animar bocadillos correspondientes

                        switch (caso2) {
                            case 1:
                                //textoAccion2.setText("No quiero el truc");
                                //animarTextoAccion(textoAccion2);

                                hayTruc = false;
                                break;
                            case 2:
                                //textoAccion2.setText("No quiero el retruc");
                                //animarTextoAccion(textoAccion2);

                                hayTruc = true;
                                break;
                            case 3:
                               // textoAccion2.setText("No quiero el quatre val");
                                //animarTextoAccion(textoAccion2);

                                hayRetruc = true;
                                break;
                            case 4:
                                //textoAccion2.setText("No quiero el joc fora");
                                //animarTextoAccion(textoAccion2);

                                hayCuatreVal = true;
                                break;
                        }
                    }

                    break;

                case 'F':
                    String aux5 = new String(buf, "UTF-8");
                    String otro2[] = aux5.split(" ");

                    if(numeroJugadores == 2){
                        envidOtro = Integer.parseInt(otro2[1]);
                        if (Integer.parseInt(otro2[2]) == 2) faltaDirecta = true;
                        showSingleChoiceAlertFalta("Tu rival ha envidado la falta", R.array.envid3);
                        envid.setVisibility(View.GONE);
                        laFalta.setVisibility(View.GONE);
                        cambiarBarraProgreso();

                    }else if(numeroJugadores ==4){
                        String yo = otro2[1];
                        cancelarBarraProgreso();
                        barrasInvisibles();

                        if(mMyId.equals(yo)){
                            if (Integer.parseInt(otro2[2]) == 2) faltaDirecta = true;
                            activarDesactivarMiBarra("ACTIVAR");
                            showSingleChoiceAlertFalta_4J("Tu rival ha envidado la falta", R.array.envid3);
                            envid_4J.setVisibility(View.GONE);
                            laFalta_4J.setVisibility(View.GONE);

                        }else {
                            if(!mMyId.equals(sender) && esDeMiEquipo(sender)){
                                bocadilloArriba.setText("La falta!");
                                animarTextoAccion(bocadilloArriba);
                                progressBarIzq.setVisibility(View.VISIBLE);
                                iniciarBarraProgresoIzq();

                            } else if(!mMyId.equals(sender) && !esDeMiEquipo(sender)){
                                bocadilloDerecha.setText("Envido la falta!");
                                animarTextoAccion(bocadilloDerecha);
                                activarDesactivarBarraCompi("ACTIVAR");
                            }
                        }
                    }
                    break;

                case 'T':
                    if(numeroJugadores == 2) {
                        showSingleChoiceAlertTruco("Tu rival ha trucado", R.array.truc1);
                        truc.setVisibility(View.GONE);
                        cambiarBarraProgreso();

                    }else if(numeroJugadores == 4){
                        truc_4J.setVisibility(View.GONE);
                        if(esDeMiEquipo(sender)){
                            //Mostrar bocadillo
                            bocadilloArriba.setText("Truco!");
                            animarTextoAccion(bocadilloArriba);
                            activarDesactivarBarraCompi("DESACTIVAR");
                            activarBarraRival("TRUC");

                        }else {
                            cancelarBarraProgreso();
                            barrasInvisibles();
                            activarDesactivarMiBarra("ACTIVAR");
                            activarDesactivarBarraCompi("ACTIVAR");
                            showSingleChoiceAlertTruco_4J("Tu rival ha trucado", R.array.truc1, sender);
                        }
                    }
                    break;

                case 'Q':
                    hayTruc = true;
                    //if(numeroJugadores == 2) {
                        textoAccion2.setText("Quiero el truc");
                        animarTextoAccion(textoAccion2);
                        cambiarBarraProgreso();
                    /*}else if(numeroJugadores == 4){
                        //Animar bocadillos correspondientes
                        cambiarBarraProgreso();
                    }*/
                    if (mMyId.equals(turno)) {
                        desbloquearCartas();
                        animarAparecerMenu();
                    }

                    break;

                case 'L':
                    if(numeroJugadores == 2) {
                        showSingleChoiceAlertRetruc("Tu rival ha retrucado", R.array.truc2);
                        cambiarBarraProgreso();

                    }else if(numeroJugadores == 4){
                        if(esDeMiEquipo(sender)){
                            //Mostrar bocadillo
                            bocadilloArriba.setText("Retruque!");
                            animarTextoAccion(bocadilloArriba);
                            retruque_4J.setVisibility(View.GONE);
                            activarDesactivarBarraCompi("DESACTIVAR");
                            activarBarraRival("TRUC");

                        }else {
                            cancelarBarraProgreso();
                            barrasInvisibles();
                            activarDesactivarMiBarra("ACTIVAR");
                            activarDesactivarBarraCompi("ACTIVAR");
                            showSingleChoiceAlertRetruc_4J("Tu rival ha retrucado", R.array.truc2, sender);
                        }
                    }
                    break;

                case 'I':
                    hayRetruc = true;
                    textoAccion2.setText("Quiero el retruc");
                    animarTextoAccion(textoAccion2);

                    if (turno.equals(mMyId)) {
                        desbloquearCartas();
                        animarAparecerMenu();
                        cambiarBarraProgreso();
                    } else reiniciarBarraProgreso();
                    break;

                case 'C':
                    if(numeroJugadores == 2) {
                        showSingleChoiceAlertCuatreVal("�Quatre val!", R.array.truc3);
                        cambiarBarraProgreso();

                    }else if(numeroJugadores == 4){
                        if(esDeMiEquipo(sender)){
                            //Mostrar bocadillo
                            bocadilloArriba.setText("Quatre val!");
                            animarTextoAccion(bocadilloArriba);
                            quatreVal_4J.setVisibility(View.GONE);
                            activarDesactivarBarraCompi("DESACTIVAR");
                            activarBarraRival("TRUC");

                        }else {
                            cancelarBarraProgreso();
                            barrasInvisibles();
                            activarDesactivarMiBarra("ACTIVAR");
                            activarDesactivarBarraCompi("ACTIVAR");
                            showSingleChoiceAlertCuatreVal_4J("�Quatre val!", R.array.truc3, sender);
                        }
                    }

                    break;

                case 'B':
                    hayCuatreVal = true;
                    textoAccion2.setText("Quiero el cuatre val");
                    animarTextoAccion(textoAccion2);

                    if (turno.equals(mMyId)) {
                        animarAparecerMenu();
                        cambiarBarraProgreso();
                        desbloquearCartas();
                    } else reiniciarBarraProgreso();
                    break;

                case 'J':
                    if(numeroJugadores == 2) {
                        showSingleChoiceAlertJocFora("�Joc fora!", R.array.envid3);
                        cambiarBarraProgreso();

                    }else if(numeroJugadores == 4){
                        if(esDeMiEquipo(sender)){
                            //Mostrar bocadillo
                            bocadilloArriba.setText("Joc fora!");
                            animarTextoAccion(bocadilloArriba);
                            jocFora_4J.setVisibility(View.GONE);
                            activarDesactivarBarraCompi("DESACTIVAR");
                            activarBarraRival("TRUC");

                        }else {
                            cancelarBarraProgreso();
                            barrasInvisibles();
                            activarDesactivarMiBarra("ACTIVAR");
                            activarDesactivarBarraCompi("ACTIVAR");
                            showSingleChoiceAlertJocFora_4J("�Joc fora!", R.array.envid3, sender);
                        }
                    }
                    break;

                case 'Y':
                    hayJocFora = true;
                    textoAccion2.setText("Quiero el joc fora");
                    animarTextoAccion(textoAccion2);

                    if (turno.equals(mMyId)) {
                        animarAparecerMenu();
                        cambiarBarraProgreso();
                        desbloquearCartas();
                    } else reiniciarBarraProgreso();
                    break;

                case '1':
                    String sAux = new String(buf, "UTF-8");
                    String sOtro[] = sAux.split(" ");
                    String que = sOtro[1];

                    if(que.equals("QUIERO")){
                        cambiarBarraProgreso();
                        if(mMyId.equals(turno)){
                            reiniciarBarraProgreso();
                            desbloquearCartas();
                            animarAparecerMenu();
                        }
                    }
                    else activarBarraRival("TRUC");
                    break;

                case 'W':
                    String aux10 = new String(buf, "UTF-8");
                    String otro7[] = aux10.split(" ");
                    String respuesta = otro7[1];
                    int caso3 = Integer.parseInt(otro7[2]);

                    if(esDeMiEquipo(sender)){
                        //Bocadillos y eliminar botones
                        if(respuesta.equals("QUIERO")){
                            if (caso3 == 1){
                                hayTruc = true;
                                retruque_4J.setVisibility(View.VISIBLE);
                                bocadilloArriba.setText("Quiero el truc");
                                animarTextoAccion(bocadilloArriba);
                            }
                            else if (caso3 == 2){
                                hayRetruc = true;
                                quatreVal_4J.setVisibility(View.VISIBLE);
                                bocadilloArriba.setText("Quiero el retruq");
                                animarTextoAccion(bocadilloArriba);
                            }
                            else if (caso3 == 3){
                                hayCuatreVal = true;
                                jocFora_4J.setVisibility(View.VISIBLE);
                                bocadilloArriba.setText("Quiero el quatre val");
                                animarTextoAccion(bocadilloArriba);
                            }
                            else if (caso3 == 4){
                                hayJocFora = true;
                                bocadilloArriba.setText("Quiero el Joc fora");
                                animarTextoAccion(bocadilloArriba);
                            }
                        }else if(respuesta.equals("RETRUQUE")){
                            bocadilloArriba.setText("Retruque");
                            animarTextoAccion(bocadilloArriba);

                        }else if(respuesta.equals("QUATRE")){
                            bocadilloArriba.setText("Quatre val");
                            animarTextoAccion(bocadilloArriba);

                        }else if(respuesta.equals("JOC")){
                            bocadilloArriba.setText("Joc fora");
                            animarTextoAccion(bocadilloArriba);

                        }else if(respuesta.equals("NOQUIERO")){
                            if (caso3 == 1){
                                bocadilloArriba.setText("No quiero el truc");
                                animarTextoAccion(bocadilloArriba);
                            }
                            else if (caso3 == 2){
                                bocadilloArriba.setText("No quiero el retruq");
                                animarTextoAccion(bocadilloArriba);
                            }
                            else if (caso3 == 3){
                                bocadilloArriba.setText("No quiero el quatre val");
                                animarTextoAccion(bocadilloArriba);
                            }
                            else if (caso3 == 4){
                                bocadilloArriba.setText("No quiero el Joc fora");
                                animarTextoAccion(bocadilloArriba);
                            }
                        }

                        activarDesactivarBarraCompi("DESACTIVAR");

                    }else if(!esDeMiEquipo(sender)){

                        //Bocadillos y eliminar barras de progreso
                        if(respuesta.equals("QUIERO")){
                            if (caso3 == 1){
                                animarBocadillosTruc(1, "QUIERO", sender);
                            }
                            else if (caso3 == 2){
                                animarBocadillosTruc(2, "QUIERO", sender);
                            }
                            else if (caso3 == 3){
                                animarBocadillosTruc(3, "QUIERO", sender);
                            }
                            else if (caso3 == 4){
                                animarBocadillosTruc(4, "QUIERO", sender);
                            }
                        }else if(respuesta.equals("RETRUQUE")){
                            animarBocadillosTruc(1, "RETRUQUE", sender);

                        }else if(respuesta.equals("QUATRE")){
                            animarBocadillosTruc(1, "QUATRE", sender);

                        }else if(respuesta.equals("JOC")){
                            animarBocadillosTruc(1, "JOC", sender);

                        }else if(respuesta.equals("NOQUIERO")){
                            if (caso3 == 1){
                                animarBocadillosTruc(1, "NOQUIERO", sender);
                            }
                            else if (caso3 == 2){
                                animarBocadillosTruc(2, "NOQUIERO", sender);
                            }
                            else if (caso3 == 3){
                                animarBocadillosTruc(3, "NOQUIERO", sender);
                            }
                            else if (caso3 == 4){
                                animarBocadillosTruc(4, "NOQUIERO", sender);
                            }
                        }

                        //Ejecucion tras los bocadillos
                        if(respuesta.equals("QUIERO")){
                            if(sQuieroTruc.equals("NOQUIERO"))sQuieroTruc = "QUIERO";
                        }
                        if(respuesta.equals("RETRUQUE")){
                            sQuieroTruc = "RETRUQUE";
                        }
                        if(respuesta.equals("QUATRE")){
                            sQuieroTruc = "QUATRE";
                        }
                        if(respuesta.equals("JOC")){
                            sQuieroTruc = "JOC";
                        }

                        mensajesRecibidosTruc++;
                        Log.d("KKKKK", "Respuesta numero: "+mensajesRecibidosTruc+", respuesta: "+sQuieroTruc);

                        //Quieren el truc
                        if(mensajesRecibidosTruc == 2 && sQuieroTruc.equals("QUIERO")) {

                            cancelarBarraProgreso();
                            barrasInvisibles();
                            activarDesactivarMiBarra("ACTIVAR");
                            if (caso3 == 1) hayTruc = true;
                            else if (caso3 == 2) hayRetruc = true;
                            else if (caso3 == 3) hayCuatreVal = true;
                            else if (caso3 == 4) hayJocFora = true;
                            if (mMyId.equals(turno)) {
                                desbloquearCartas();
                                animarAparecerMenu();
                            }
                            //Animar los bocadillos
                            cambiarBarraProgreso();
                            enviarMensajeBarraProgreso("QUIERO");
                            //enviarMensajeQuieroTruc();
                            mensajesRecibidosTruc = 0;
                            sQuieroTruc = "NOQUIERO";

                        }else if(mensajesRecibidosTruc == 2 && sQuieroTruc.equals("RETRUQUE")){

                            cancelarBarraProgreso();
                            barrasInvisibles();
                            activarDesactivarMiBarra("ACTIVAR");
                            activarDesactivarBarraCompi("ACTIVAR");
                            //Animar los bocadillos
                            //cambiarBarraProgreso();
                            enviarMensajeBarraProgreso("RETRUQUE");
                            showSingleChoiceAlertRetruc_4J("Tu rival ha retrucado", R.array.truc2, "");
                            mensajesRecibidosTruc = 0;
                            sQuieroTruc = "NOQUIERO";

                        }else if(mensajesRecibidosTruc == 2 && sQuieroTruc.equals("QUATRE")){

                            cancelarBarraProgreso();
                            barrasInvisibles();
                            activarDesactivarMiBarra("ACTIVAR");
                            activarDesactivarBarraCompi("ACTIVAR");
                            //Animar los bocadillos
                            //cambiarBarraProgreso();
                            enviarMensajeBarraProgreso("QUATRE");
                            showSingleChoiceAlertCuatreVal_4J("�Quatre val!", R.array.truc3, "");
                            mensajesRecibidosTruc = 0;
                            sQuieroTruc = "NOQUIERO";

                        }else if(mensajesRecibidosTruc == 2 && sQuieroTruc.equals("JOC")){

                            cancelarBarraProgreso();
                            barrasInvisibles();
                            activarDesactivarMiBarra("ACTIVAR");
                            activarDesactivarBarraCompi("ACTIVAR");
                            //Animar los bocadillos
                            //cambiarBarraProgreso();
                            enviarMensajeBarraProgreso("JOC");
                            showSingleChoiceAlertJocFora_4J("�Joc fora!", R.array.envid3, "");
                            mensajesRecibidosTruc = 0;
                            sQuieroTruc = "NOQUIERO";

                        //No quieren el truc
                        }else if(mensajesRecibidosTruc == 2 && sQuieroTruc.equals("NOQUIERO")){

                            barrasInvisibles();
                            cancelarBarraProgreso();
                            if(mMyId.equals(equipo1[0]) || mMyId.equals(equipo2[0])){
                                primerMensaje = mMyId;
                                //Animar los bocadillos
                                if(caso3 == 1){
                                    enviarMensajeNoQuieroTruc(1, primerMensaje);
                                    hayTruc = false;
                                }
                                else if(caso3 == 2){
                                    hayTruc = true;
                                    enviarMensajeNoQuieroTruc(2, primerMensaje);
                                }
                                else if(caso3 == 3){
                                    hayRetruc = true;
                                    enviarMensajeNoQuieroTruc(3, primerMensaje);
                                }
                                else if(caso3 == 4){
                                    hayCuatreVal = true;
                                    enviarMensajeNoQuieroTruc(4, primerMensaje);
                                }
                                sQuieroTruc = "NOQUIERO";
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        // acciones que se ejecutan tras los milisegundos
                                        mostrarResultadosGanadorMano("PRIMERO");

                                    }
                                }, 1500);
                            }
                        }
                    }

                    break;



                case 'Z':
                    String aux8 = new String(buf, "UTF-8");
                    String otro5[] = aux8.split(" ");

                    switch (numeroJugadores) {

                        case 2:
                            puntosTotalesJugador2 = Integer.parseInt(otro5[1]);
                            String quien = otro5[2];
                            String contador = otro5[3];
                            botonMarcadorArriba.setProgress(0);
                            botonMarcadorArriba.setIndeterminateProgressMode(true); // turn on indeterminate progress
                            botonMarcadorArriba.setProgress(50); // set progress > 0 & < 100 to display indeterminate progress
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    // acciones que se ejecutan tras los milisegundos
                                    botonMarcadorArriba.setCompleteText(Integer.toString(puntosTotalesJugador2));
                                    botonMarcadorArriba.setProgress(100);
                                }
                            }, 5000);

                            //Para que actualice los puntos antes de comprobar quien es el ganador
                            //Primero y segundo para que no entre en un bucle
                            if (quien.equals("PERDEDOR")) {
                                if (contador.equals("PRIMERO")){
                                        mostrarResultadosGanadorMano("SEGUNDO");
                                }
                            } else if (quien.equals("GANADOR")) {
                                if (contador.equals("PRIMERO")) {
                                    mostrarResultadosPerdedorMano("SEGUNDO");
                                }
                            }

                            Log.d("HHHHHH", "Mis puntos: " + puntosTotalesMios);
                            Log.d("HHHHHH", "Puntos rival: " + puntosTotalesJugador2);

                            String ganadorFinal = comprobarGanadorPartida();
                            Log.d("HHHHHH", "Ganador: " + ganadorFinal);
                            if (!ganadorFinal.equals("NADIE")) {
                                if (ganadorFinal.equals("YO")) {

                                    //Actualiza el ranking sumando una victoria
                                    updateLeaderboards(mGoogleApiClient, LEADERBOARD_ID);
                                    Games.Achievements.unlock(mGoogleApiClient, ACHIEVEMENTS_ID);

                                /*
                                if(quien.equals("GANADOR")){
                                    if(contador.equals("PRIMERO"))mostrarResultadosPerdedorMano("SEGUNDO");
                                }else if(quien.equals("PERDEDOR")){
                                    if(contador.equals("PRIMERO"))mostrarResultadosGanadorMano("SEGUNDO");
                                } */
                                    switchToScreen(R.id.screen_win);

                                } else if (ganadorFinal.equals("RIVAL")) {
                               /* if(quien.equals("GANADOR")){
                                    if(contador.equals("PRIMERO"))mostrarResultadosPerdedorMano("SEGUNDO");
                                }else if(quien.equals("PERDEDOR")){
                                    if(contador.equals("PRIMERO"))mostrarResultadosGanadorMano("SEGUNDO");
                                } */
                                    switchToScreen(R.id.screen_lost);
                                }
                            } else {
                                //Lo recibe el ganador
                                if (quien.equals("PERDEDOR")) {
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
                                } else if (quien.equals("GANADOR")) {
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
                                        showProgressDialog("L�stima, pierdes la mano");
                                    }
                                    repartirTrasMano();
                                    Log.d("KKKKKKKK", "Repartiendo tras mano..." + mMyId);
                                }

                            }
                            break;

                        case 4:
                            if(!esDeMiEquipo(sender)){
                                puntosTotalesJugador2 = Integer.parseInt(otro5[1]);
                            }
                            if(mMyId.equals(primerMensaje)){
                                enviarPuntuacionCompi(puntosTotalesMios, puntosTotalesJugador2);
                                Log.d("KKKKK", "Puntos mi equipo: "+puntosTotalesMios);
                                Log.d("KKKKK", "Puntos rquipo rival: " + puntosTotalesJugador2);
                            }
                            String quien_4J = otro5[2];
                            String contador_4J = otro5[3];
                            //marcador2.setText("Rival: "+puntosTotalesJugador2);

                            boolean soyGanador = false;
                            //Soy del equipo
                            if (esDeMiEquipo(sender) && quien_4J.equals("PERDEDOR")) {
                                Log.d("ZZZZ", "Soy del equipo del perdedor");
                                soyGanador = false;
                            } else if (esDeMiEquipo(sender) && quien_4J.equals("GANADOR")) {
                                Log.d("ZZZZ", "Soy del equipo del ganador");
                                soyGanador = true;

                                //NO soy del equipo
                            } else if (!esDeMiEquipo(sender)) {
                                Log.d("ZZZZ", "NO soy del equipo del que calcula");
                                //Para que actualice los puntos antes de comprobar quien es el ganador
                                //Primero y segundo para que no entre en un bucle
                                if (quien_4J.equals("PERDEDOR")) {
                                    soyGanador = true;
                                    if (contador_4J.equals("PRIMERO")){
                                        if (!hayTruc) puntosTruc = NO_QUIERO_TRUC;
                                        if (hayTruc && !hayRetruc) puntosTruc = TRUC;
                                        if (hayRetruc && !hayCuatreVal) puntosTruc = RETRUC;
                                        if (hayCuatreVal && !hayJocFora) puntosTruc = CUATRE_VAL;
                                        if (hayJocFora) puntosTruc = 24;
                                        puntosTotalesMios += (puntosTruc + puntosEnvid);
                                        //marcador.setText("Yo: "+puntosTotalesMios);
                                        //actualizarMarcador2(puntosTotalesMios, "GANADOR", contador);

                                        Log.d("KKKKK", "Puntos mi equipo: "+puntosTotalesMios);
                                        Log.d("KKKKK", "Puntos rquipo rival: "+puntosTotalesJugador2);

                                        if(equipo1[0].equals(mMyId) || equipo2[0].equals(mMyId)) {
                                            actualizarMarcador2_4J(puntosTotalesMios, "GANADOR", sender);
                                            Log.d("ZZZZ", "Env�o el segundo mensaje");
                                        }
                                    }

                                } else if (quien_4J.equals("GANADOR")) {
                                    soyGanador = false;
                                    if (contador_4J.equals("PRIMERO")){
                                        puntosTotalesMios += puntosEnvid;
                                        //marcador.setText("Yo: "+puntosTotalesMios);
                                        //actualizarMarcador2(puntosTotalesMios, "PERDEDOR", contador);

                                        Log.d("KKKKK", "Puntos mi equipo: "+puntosTotalesMios);
                                        Log.d("KKKKK", "Puntos rquipo rival: "+puntosTotalesJugador2);

                                        if(equipo1[0].equals(mMyId) || equipo2[0].equals(mMyId)) {
                                            Log.d("ZZZZ", "Env�o el segundo mensaje");
                                            actualizarMarcador2_4J(puntosTotalesMios, "PERDEDOR", sender);
                                        }
                                    }

                                }
                            }

                            //Lo recibe el ganador
                            if (soyGanador) {
                                    if (hayEnvid) {
                                        Log.d("KKKKKKKK", "Hay envid");
                                        LayoutInflater inflater = getLayoutInflater();
                                        ViewGroup container = null;

                                        if (ganadorEnvid.equals(mMyId) || esDeMiEquipo(ganadorEnvid)) {
                                            View layout = inflater.inflate(R.layout.progres_content, container);
                                            showProgressCustomDialog(layout);
                                            Log.d("KKKKKKKK", "Gano todo");
                                        } else {
                                            View layout = inflater.inflate(R.layout.progres_content2, container);
                                            showProgressCustomDialog(layout);
                                            Log.d("KKKKKKKK", "Gano truc pierdo envid");
                                        }
                                    } else {
                                        showProgressDialog("Enhorabuena, ganas la mano");
                                        Log.d("KKKKKKKK", "No hay envid");
                                    }

                                repartirTrasMano();
                                Log.d("KKKKKKKK", "Repartiendo tras mano..." + mMyId);


                                //Lo recibe el perdedor
                            } else if (!soyGanador) {
                                    if (hayEnvid) {
                                        Log.d("KKKKKKKK", "Hay envid");
                                        LayoutInflater inflater = getLayoutInflater();
                                        ViewGroup container = null;

                                        if (ganadorEnvid.equals(mMyId) || esDeMiEquipo(ganadorEnvid)) {
                                            View layout = inflater.inflate(R.layout.progres_content3, container);
                                            showProgressCustomDialog(layout);
                                            Log.d("KKKKKKKK", "pierdo truc gano envid");

                                        } else {
                                            View layout = inflater.inflate(R.layout.progres_content4, container);
                                            showProgressCustomDialog(layout);
                                            Log.d("KKKKKKKK", "Pierdo todo");
                                        }
                                    } else {
                                        showProgressDialog("L�stima, pierdes la mano");
                                        Log.d("KKKKKKKK", "No hay envid");
                                    }
                                repartirTrasMano();
                                Log.d("KKKKKKKK", "Repartiendo tras mano..." + mMyId);
                            }

                            break;
                    }


                    break;

                case 'A':
                    String aux11 = new String(buf, "UTF-8");
                    String otro8[] = aux11.split(" ");
                    puntosTotalesMios = Integer.parseInt(otro8[1]);
                    puntosTotalesJugador2 = Integer.parseInt(otro8[2]);
                    Log.d("KKKKK", "Puntos mi equipo: "+puntosTotalesMios);
                    Log.d("KKKKK", "Puntos rquipo rival: " + puntosTotalesJugador2);
                    break;

                case 'O':
                    String aux9 = new String(buf, "UTF-8");
                    String otro6[] = aux9.split(" ");

                    if(numeroJugadores == 2){
                        if (ronda == 1) ganadorRonda1_4J = otro6[1];
                        else if (ronda == 2) ganadorRonda2_4J = otro6[1];

                    }else if(numeroJugadores == 4){
                        if (ronda == 1) ganadorRonda1_4J = otro6[1];
                        else if (ronda == 2) ganadorRonda2_4J = otro6[1];
                        else if (ronda == 3) ganadorRonda3_4J = otro6[1];
                    }

                    break;

                case 'H':
                    mensajesRecibidos++;
                    if(mensajesRecibidos == 3){
                        inicializarMano();
                        botonMarcadorAbajo_4J.setProgress(0);
                        botonMarcadorAbajo_4J.setIndeterminateProgressMode(true); // turn on indeterminate progress
                        botonMarcadorAbajo_4J.setProgress(50); // set progress > 0 & < 100 to display indeterminate progress
                        Handler handler2 = new Handler();
                        handler2.postDelayed(new Runnable() {
                            public void run() {
                                // acciones que se ejecutan tras los milisegundos
                                botonMarcadorAbajo_4J.setCompleteText(Integer.toString(puntosTotalesMios));
                                botonMarcadorAbajo_4J.setProgress(100);
                            }
                        }, 5000);

                        botonMarcadorArriba_4J.setProgress(0);
                        botonMarcadorArriba_4J.setIndeterminateProgressMode(true); // turn on indeterminate progress
                        botonMarcadorArriba_4J.setProgress(50); // set progress > 0 & < 100 to display indeterminate progress
                        Handler handler3 = new Handler();
                        handler3.postDelayed(new Runnable() {
                            public void run() {
                                // acciones que se ejecutan tras los milisegundos
                                botonMarcadorArriba_4J.setCompleteText(Integer.toString(puntosTotalesJugador2));
                                botonMarcadorArriba_4J.setProgress(100);
                            }
                        }, 5000);
                    }

                    break;

                case '2':
                    if(numeroJugadores == 2){
                        updateLeaderboards(mGoogleApiClient, LEADERBOARD_ID);
                        switchToScreen(R.id.screen_win);
                    }else if(numeroJugadores == 4) {
                        if(esDeMiEquipo(sender)){ switchToScreen(R.id.screen_lost);}
                        else {
                            updateLeaderboards(mGoogleApiClient, LEADERBOARD_ID);
                            switchToScreen(R.id.screen_win);
                        }
                    }
                    break;

                case '3':
                    //DESCOMENTAR LOS TOAST CUANDO LOS PONGAS DENTRO DEL JUEGO PARA COMPROBAR QUE FUNCIONA ANTES DE SEGUIR
                    String aux12 = new String(buf, "UTF-8");
                    String otro9[] = aux12.split(" ");
                    if(esDeMiEquipo(sender)){
                        senyaCompi1 = otro9[1];
                        senyaCompi2 = otro9[2];
                        Toast.makeText(getApplicationContext(), senyaCompi1+" "+senyaCompi2, Toast.LENGTH_SHORT).show();
                    }else{
                        if(esRivalDerecha(sender)) {
                            senyaRivalDerecha = otro9[1];
                            Toast.makeText(getApplicationContext(), senyaRivalDerecha, Toast.LENGTH_SHORT).show();
                        }else{
                            senyaRivalIzq = otro9[1];
                            Toast.makeText(getApplicationContext(), senyaRivalIzq, Toast.LENGTH_SHORT).show();
                        }

                    }

                    break;

                default:

                    switch (numeroJugadores) {

                        case 2:
                            String turnoNuevo = new String(buf, "UTF-8");
                            turno = turnoNuevo;

                            if (mMyId.equals(turno) && misRondasGanadas < 2) {
                                if (ronda > 1 && !hayEnvid) {
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

                        case 4:
                            String turnoNuevo4J = new String(buf, "UTF-8");
                            turno = turnoNuevo4J;
                            Log.d("LLLLLLL", "Turno para: " + turno + " mi id: " + mMyId);
                            if (mMyId.equals(turno) && rondasGanadasMiEquipo < 2) {
                                if ((mMyId.equals(comprobarSiguienteMano())) || (ronda > 1 && !hayEnvid)) {
                                    envid_4J.setVisibility(View.GONE);
                                    laFalta_4J.setVisibility(View.GONE);
                                }
                                desbloquearCartas();
                                Toast.makeText(getApplicationContext(), "Es tu turno", Toast.LENGTH_SHORT).show();
                                cambiarBarraProgreso();
                                animarAparecerMenu();
                            }

                            if (!mMyId.equals(turno) && rondasGanadasMiEquipo < 2) {
                                bloquearCartas();

                                Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
                                Log.d("LLLLLLL", "Me han comunicado cambio de turno");
                                cambiarBarraProgreso();

                            }
                            break;
                    }
                    break;

            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    String comprobarSiguienteMano(){
        if(mano.equals(idJugador1)){
            return idJugador2;
        }else if(mano.equals(idJugador2)){
            return idJugador3;
        }else if(mano.equals(idJugador3)){
            return idJugador4;
        }else if(mano.equals(idJugador4)){
            return idJugador1;
        }
        return "";
    }
    String comprobarSegundoMano(){
        if(mano.equals(idJugador1)){
            return idJugador3;
        }else if(mano.equals(idJugador2)){
            return idJugador4;
        }else if(mano.equals(idJugador3)){
            return idJugador1;
        }else if(mano.equals(idJugador4)){
            return idJugador2;
        }
        return "";
    }
    String comprobarTerceroeMano(){
        if(mano.equals(idJugador1)){
            return idJugador4;
        }else if(mano.equals(idJugador2)){
            return idJugador1;
        }else if(mano.equals(idJugador3)){
            return idJugador2;
        }else if(mano.equals(idJugador4)){
            return idJugador3;
        }
        return "";
    }

    String comprobarDerechaTurno(){
        if(turno.equals(idJugador1)){
            return idJugador2;
        }else if(turno.equals(idJugador2)){
            return idJugador3;
        }else if(turno.equals(idJugador3)){
            return idJugador4;
        }else if(turno.equals(idJugador4)){
            return idJugador1;
        }
        return "";
    }

    String comprobarArribaTurno(){
        if(turno.equals(idJugador1)){
            return idJugador3;
        }else if(turno.equals(idJugador2)){
            return idJugador4;
        }else if(turno.equals(idJugador3)){
            return idJugador1;
        }else if(turno.equals(idJugador4)){
            return idJugador2;
        }
        return "";
    }
    String comprobarIzqTurno(){
        if(turno.equals(idJugador1)){
            return idJugador4;
        }else if(turno.equals(idJugador2)){
            return idJugador1;
        }else if(turno.equals(idJugador3)){
            return idJugador2;
        }else if(turno.equals(idJugador4)){
            return idJugador3;
        }
        return "";
    }

    String[] comprobarUltimosJugadores(){
        String[] ultimos = new String[2];
        if(mano.equals(idJugador1)){
            if(mMyId.equals(idJugador3)){
                ultimos[0] = idJugador3;
                ultimos[1] = idJugador4;
            }else if(mMyId.equals(idJugador4)){
                ultimos[0] = idJugador4;
                ultimos[1] = idJugador3;
            }
        }else if(mano.equals(idJugador2)){
            if(mMyId.equals(idJugador4)){
                ultimos[0] = idJugador4;
                ultimos[1] = idJugador1;
            }else if(mMyId.equals(idJugador1)){
                ultimos[0] = idJugador1;
                ultimos[1] = idJugador4;
            }
        }else if(mano.equals(idJugador3)){
            if(mMyId.equals(idJugador1)){
                ultimos[0] = idJugador1;
                ultimos[1] = idJugador2;
            }else if(mMyId.equals(idJugador2)){
                ultimos[0] = idJugador2;
                ultimos[1] = idJugador1;
            }
        }else if(mano.equals(idJugador4)){
            if(mMyId.equals(idJugador2)){
                ultimos[0] = idJugador2;
                ultimos[1] = idJugador3;
            }else if(mMyId.equals(idJugador3)){
                ultimos[0] = idJugador3;
                ultimos[1] = idJugador2 ;
            }
        }
        return ultimos;
    }

    boolean esRivalDerecha(String sender){
        if(mMyId.equals(idJugador1)){
            if(sender.equals(idJugador2)) return true;

        }else if(mMyId.equals(idJugador2)){
            if(sender.equals(idJugador3)) return true;

        }else if(mMyId.equals(idJugador3)){
            if(sender.equals(idJugador4)) return true;

        }else if(mMyId.equals(idJugador4)){
            if(sender.equals(idJugador1)) return true;

        }
        return false;
    }
    boolean esRivalIzquierda(String sender){
        if(mMyId.equals(idJugador1)){
            if(sender.equals(idJugador4)) return true;

        }else if(mMyId.equals(idJugador2)){
            if(sender.equals(idJugador1)) return true;

        }else if(mMyId.equals(idJugador3)){
            if(sender.equals(idJugador2)) return true;

        }else if(mMyId.equals(idJugador4)){
            if(sender.equals(idJugador3)) return true;

        }
        return false;
    }

    /*
     * UI SECTION. Methods that implement the game's UI.
     */

    // This array lists everything that's clickable, so we can install click
    // event handlers.
    final static int[] CLICKABLES = {
            R.id.button_accept_popup_invitation, R.id.button_invite_players,
            R.id.button_quick_game, R.id.button_see_invitations, R.id.button_sign_in,
            R.id.button_sign_out, R.id.button_quick_game_4,R.id.button_ranking,
            R.id.button_logros, R.id.boton_iconos
    };

    // This array lists all the individual screens our game has.
    final static int[] SCREENS = {
            R.id.screen_game, R.id.screen_main, R.id.screen_sign_in,
            R.id.screen_wait, R.id.screen_lost, R.id.screen_win,
            R.id.screen_game_4_jugadores
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
                    //Por cada question, la a?adimos a la lista
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

        switch (numeroJugadores) {
            case 2:

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
                break;

            case 4:
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
                    while (list[0] == aux5 || list[1] == aux5 || list[2] == aux5 || list2[0] == aux5
                            || list2[1] == aux5) {
                        aux5 = (int) (Math.random() * 22);
                    }
                    list2[2] = aux5;

                    int aux6 = (int) (Math.random() * 22);
                    while (list[0] == aux6 || list[1] == aux6 || list[2] == aux6 || list2[0] == aux6
                            || list2[1] == aux6 || list2[2] == aux6) {
                        aux6 = (int) (Math.random() * 22);
                    }
                    list3[0] = aux6;

                    int aux7 = (int) (Math.random() * 22);
                    while (list[0] == aux7 || list[1] == aux7 || list[2] == aux7 || list2[0] == aux7
                            || list2[1] == aux7 || list2[2] == aux7 || list3[0] == aux7) {
                        aux7 = (int) (Math.random() * 22);
                    }
                    list3[1] = aux7;

                    int aux8 = (int) (Math.random() * 22);
                    while (list[0] == aux8 || list[1] == aux8 || list[2] == aux8 || list2[0] == aux8
                            || list2[1] == aux8 || list2[2] == aux8 || list3[0] == aux8
                            || list3[1] == aux8) {
                        aux8 = (int) (Math.random() * 22);
                    }
                    list3[2] = aux8;

                    int aux9 = (int) (Math.random() * 22);
                    while (list[0] == aux9 || list[1] == aux9 || list[2] == aux9 || list2[0] == aux9
                            || list2[1] == aux9 || list2[2] == aux9 || list3[0] == aux9
                            || list3[1] == aux9 || list3[2] == aux9) {
                        aux9 = (int) (Math.random() * 22);
                    }
                    list4[0] = aux9;

                    int aux10 = (int) (Math.random() * 22);
                    while (list[0] == aux10 || list[1] == aux10 || list[2] == aux10 || list2[0] == aux10
                            || list2[1] == aux10 || list2[2] == aux10 || list3[0] == aux10
                            || list3[1] == aux10 || list3[2] == aux10 || list4[0] == aux10) {
                        aux10 = (int) (Math.random() * 22);
                    }
                    list4[1] = aux10;

                    int aux11 = (int) (Math.random() * 22);
                    while (list[0] == aux11 || list[1] == aux11 || list[2] == aux11 || list2[0] == aux11
                            || list2[1] == aux11 || list2[2] == aux11 || list3[0] == aux11
                            || list3[1] == aux11 || list3[2] == aux11 || list4[0] == aux11 || list4[1] == aux11) {
                        aux11 = (int) (Math.random() * 22);
                    }
                    list4[2] = aux11;

                    sCartasJ1 = list[0] + " " + list[1] + " " + list[2];
                    sCartasJ2 = list2[0] + " " + list2[1] + " " + list2[2];
                    sCartasJ3 = list3[0] + " " + list3[1] + " " + list3[2];
                    sCartasJ4 = list4[0] + " " + list4[1] + " " + list4[2];

                }
                break;
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
        Log.d("BBBBBBBBB", "cartas2: " + sCartasJ2 + " cartas3: " + sCartasJ3 + " cartas3: " + sCartasJ3);
        resetAll();
        //Si soy mano reparto
        if (mMyId.equals(mano)) {
            repartir(crearAleatorio());
            reproducirSonidoRepartir();
            carta1 = new Carta(manoJugador.get(0).getNumero(), manoJugador.get(0).getPalo(), manoJugador.get(0).getValor());
            carta2 = new Carta(manoJugador.get(1).getNumero(), manoJugador.get(1).getPalo(), manoJugador.get(1).getValor());
            carta3 = new Carta(manoJugador.get(2).getNumero(), manoJugador.get(2).getPalo(), manoJugador.get(2).getValor());
            //Mando las cartas
            Log.d("BBBBBBBBB", "mandando cartas");
            Log.d("BBBBBBBBB", "cartas2: " + sCartasJ2 + " cartas3: " + sCartasJ3 + " cartas3: " + sCartasJ3);
            if (numeroJugadores == 2) {
                enviarMensajeRepartir();
            } else if (numeroJugadores == 4) {
                enviarMensajeRepartir4J();
                Log.d("BBBBBBBBB", "Mensaje 4J");
            }
            cerrarDialogoAndStart(4000);
        }
        //Sino soy mano, espero las cartas

    }

    public void cambiarMano() {
        switch (numeroJugadores) {
            case 2:
                if (mano.equals(idJugador1)) {
                    mano = idJugador2;
                } else if (mano.equals(idJugador2)) {
                    mano = idJugador1;
                }

                turno = mano;
                break;

            case 4:
                if (mano.equals(idJugador1)) {
                    mano = idJugador2;
                } else if (mano.equals(idJugador2)) {
                    mano = idJugador3;
                } else if (mano.equals(idJugador3)) {
                    mano = idJugador4;
                } else if (mano.equals(idJugador4)) {
                    mano = idJugador1;
                }

                turno = mano;
                break;
        }
    }

    public void repartirTrasMano() {
        if(numeroJugadores == 2){
            cambiarMano();
            if (mMyId.equals(mano)) {
                inicializarMano();
            }
        }else if(numeroJugadores == 4){
            cambiarMano();
            if (!mMyId.equals(mano)) {
                enviarMensajeListo();
                botonMarcadorAbajo_4J.setProgress(0);
                botonMarcadorAbajo_4J.setIndeterminateProgressMode(true); // turn on indeterminate progress
                botonMarcadorAbajo_4J.setProgress(50); // set progress > 0 & < 100 to display indeterminate progress
                Handler handler2 = new Handler();
                handler2.postDelayed(new Runnable() {
                    public void run() {
                        // acciones que se ejecutan tras los milisegundos
                        botonMarcadorAbajo_4J.setCompleteText(Integer.toString(puntosTotalesMios));
                        botonMarcadorAbajo_4J.setProgress(100);
                    }
                }, 5000);

                botonMarcadorArriba_4J.setProgress(0);
                botonMarcadorArriba_4J.setIndeterminateProgressMode(true); // turn on indeterminate progress
                botonMarcadorArriba_4J.setProgress(50); // set progress > 0 & < 100 to display indeterminate progress
                Handler handler3 = new Handler();
                handler3.postDelayed(new Runnable() {
                    public void run() {
                        // acciones que se ejecutan tras los milisegundos
                        botonMarcadorArriba_4J.setCompleteText(Integer.toString(puntosTotalesJugador2));
                        botonMarcadorArriba_4J.setProgress(100);
                    }
                }, 5000);
            }
        }


 /*       if (mMyId.equals(mano)) {
            cambiarMano();
            enviarMensajeMano(mano);
            Log.d("KKKKKKKK", "Soy mano..." + mMyId);
        }else Log.d("KKKKKKKK", "No soy mano..." + mMyId);
   */
    }

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
            /** Cartas para contar los puntos **/
            case "12oros":
                view.setImageResource(R.drawable.doceoros);
                break;
            case "12bastos":
                view.setImageResource(R.drawable.docebastos);
                break;
            case "12espadas":
                view.setImageResource(R.drawable.doceespadas);
                break;
            case "12copas":
                view.setImageResource(R.drawable.docecopas);
                break;
            case "11oros":
                view.setImageResource(R.drawable.onceoros);
                break;
            case "11bastos":
                view.setImageResource(R.drawable.oncebastos);
                break;
            case "11espadas":
                view.setImageResource(R.drawable.onceespadas);
                break;
            case "11copas":
                view.setImageResource(R.drawable.oncecopas);
                break;
            case "10oros":
                view.setImageResource(R.drawable.diezoros);
                break;
            case "10bastos":
                view.setImageResource(R.drawable.diesbastos);
                break;
            case "10espadas":
                view.setImageResource(R.drawable.diezespadas);
                break;
            case "10copas":
                view.setImageResource(R.drawable.diezcopas);
                break;
            case "2oros":
                view.setImageResource(R.drawable.dosoros);
                break;
            case "2bastos":
                view.setImageResource(R.drawable.dosbastos);
                break;
            case "2espadas":
                view.setImageResource(R.drawable.dosespadas);
                break;
            case "2copas":
                view.setImageResource(R.drawable.doscopas);
                break;
            case "1oros":
                view.setImageResource(R.drawable.unooros);
                break;
            case "1copas":
                view.setImageResource(R.drawable.unocopas);
                break;
        }
    }

    public void quitarTick(ImageView view) {
        view.setImageResource(0);
    }
    public void ponerTick(ImageView view) {
        view.setImageResource(R.drawable.ic_check);
    }


    public void seleccionaUnaSenya(View view) {
        numeroSenyas++;
        if(numeroSenyas >2){
            quitarTick((ImageView)senyas.get(0));
            senyas.remove(0);
            senyas.add(view);
            ponerTick((ImageView) view);
            //señas.add(0, señas.get(1));
            //señas.add(1, view.getId());
        }else senyas.add(view);
        ponerTick((ImageView)view);


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
                if (e.getMessage() != null) {
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
                    if (view.equals(tvJugador1)) {
                        xDelta = inicial.x - Params.rightMargin;
                        yDelta = inicial.y - Params.topMargin;
                    } else {
                        xDelta = inicial.x - Params.leftMargin;
                        yDelta = inicial.y - Params.topMargin;
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    //Vemos donde colocamos la carta
                    double medioLayout = ((float) view.getLayoutParams().height) / 1.2;
                    if (view.getY() < (medioLayout)) {
                        if (tvCartaMesa1.getVisibility() == View.INVISIBLE) {
                            destino.x = tvCartaMesa1.getX();
                            destino.y = tvCartaMesa1.getY();
                            tvCartaMesa1.setVisibility(View.VISIBLE);

                            if(tapoPrimera || tapo){
                                aux = new Carta("1", "oros", "1");
                                asignarImagenCarta(aux, (ImageView) view);
                            }

                            view.animate().x(destino.x).y(destino.y).rotation(0).rotationXBy(30)
                                    .scaleX((float) 0.8).scaleY((float) 0.8).setDuration(500);
                            view.setEnabled(false);

                            if (view.equals(tvJugador1)) {
                                posTvJugador1 = 1;
                                tvJugador1.bringToFront();
                                tvJugador2.bringToFront();
                                tvJugador3.bringToFront();
                            } else if (view.equals(tvJugador2)) {
                                tvJugador2.bringToFront();
                                posTvJugador2 = 1;
                                tvJugador1.bringToFront();
                                tvJugador3.bringToFront();
                            } else if (view.equals(tvJugador3)) {
                                posTvJugador3 = 1;
                                tvJugador3.bringToFront();
                                tvJugador1.bringToFront();
                                tvJugador2.bringToFront();
                            }


                        } else if (tvCartaMesa2.getVisibility() == View.INVISIBLE) {
                            destino.x = tvCartaMesa2.getX();
                            destino.y = tvCartaMesa2.getY();
                            tvCartaMesa2.setVisibility(View.VISIBLE);

                            if(tapoPrimera || tapo){
                                aux = new Carta("1", "oros", "1");
                                asignarImagenCarta(aux, (ImageView) view);
                            }

                            view.animate().x(destino.x).y(destino.y).rotation(0).rotationXBy(30)
                                    .scaleX((float) 0.8).scaleY((float) 0.8).setDuration(500);
                            view.setEnabled(false);

                            if (view.equals(tvJugador1) && posTvJugador2 == 0) {
                                posTvJugador1 = 2;
                                tvJugador1.bringToFront();
                                tvJugador2.bringToFront();
                                Log.d("RRRRRRR", "Actualizada pos2");
                            } else if (view.equals(tvJugador1) && posTvJugador3 == 0) {
                                posTvJugador1 = 2;
                                tvJugador1.bringToFront();
                                tvJugador3.bringToFront();
                                Log.d("RRRRRRR", "Actualizada pos2");
                            } else if (view.equals(tvJugador2) && posTvJugador1 == 0) {
                                posTvJugador2 = 2;
                                tvJugador2.bringToFront();
                                tvJugador1.bringToFront();
                                Log.d("RRRRRRR", "Actualizada pos2");
                            } else if (view.equals(tvJugador2) && posTvJugador3 == 0) {
                                posTvJugador2 = 2;
                                tvJugador2.bringToFront();
                                tvJugador3.bringToFront();
                                Log.d("RRRRRRR", "Actualizada pos2");
                            } else if (view.equals(tvJugador3) && posTvJugador1 == 0) {
                                posTvJugador3 = 2;
                                tvJugador3.bringToFront();
                                tvJugador1.bringToFront();
                                Log.d("RRRRRRR", "Actualizada pos2");
                            } else if (view.equals(tvJugador3) && posTvJugador2 == 0) {
                                posTvJugador3 = 2;
                                tvJugador3.bringToFront();
                                tvJugador2.bringToFront();
                                Log.d("RRRRRRR", "Actualizada pos2");
                            }


                        } else if (tvCartaMesa3.getVisibility() == View.INVISIBLE) {
                            destino.x = tvCartaMesa3.getX();
                            destino.y = tvCartaMesa3.getY();
                            tvCartaMesa3.setVisibility(View.VISIBLE);

                            if(tapoPrimera || tapo){
                                aux = new Carta("1", "oros", "1");
                                asignarImagenCarta(aux, (ImageView) view);
                            }

                            view.animate().x(destino.x).y(destino.y).rotation(0).rotationXBy(30)
                                    .scaleX((float) 0.8).scaleY((float) 0.8).setDuration(500);

                            view.setEnabled(false);

                            if (view.equals(tvJugador1)) {
                                posTvJugador1 = 3;
                                tvJugador1.bringToFront();
                            } else if (view.equals(tvJugador2)) {
                                posTvJugador2 = 3;
                                tvJugador2.bringToFront();
                            } else if (view.equals(tvJugador3)) {
                                posTvJugador3 = 3;
                                tvJugador3.bringToFront();
                            }
                            Log.d("RRRRRRR", "Actualizada pos3");
                        }
                        if(tapoPrimera || tapo){
                            miValor = 1;
                            comprobarSonidosCartas(aux);
                            tapo = false;
                        }else {
                            //Calculamos su valor para enviarlo
                            if (view.equals(tvJugador1)) {
                                aux = carta1;
                                comprobarSonidosCartas(aux);
                                miValor = Integer.parseInt(carta1.getValor());
                            }
                            if (view.equals(tvJugador2)) {
                                aux = carta2;
                                comprobarSonidosCartas(aux);
                                miValor = Integer.parseInt(carta2.getValor());
                            }
                            if (view.equals(tvJugador3)) {
                                aux = carta3;
                                comprobarSonidosCartas(aux);
                                miValor = Integer.parseInt(carta3.getValor());
                            }
                        }

                        envid.setVisibility(View.GONE);
                        laFalta.setVisibility(View.GONE);

                        if (!hayEmpate) {
                            cartaSeleccionada();
                        } else cartaSeleccionadaEmpate();

                        hayAnimaciones = true;

                    } else {
                        if (view.equals(tvJugador1)) {
                            view.animate().translationX(inicio1.x - 30).setDuration(500);
                            view.animate().translationY(inicio1.y + 15);
                        }
                        if (view.equals(tvJugador2)) {
                            view.animate().translationX(inicio2.x).setDuration(500);
                            view.animate().translationY(inicio2.y).setDuration(500);
                        }
                        if (view.equals(tvJugador3)) {
                            view.animate().translationX(inicio3.x + 30).setDuration(500);
                            view.animate().translationY(inicio3.y + 15).setDuration(500);
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

    private final class MyTouchListener4J implements View.OnTouchListener {
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
                    if (view.equals(tvJugador1_4J)) {
                        xDelta_4J = inicial.x - Params.rightMargin;
                        yDelta_4J = inicial.y - Params.topMargin;
                    } else {
                        xDelta_4J = inicial.x - Params.leftMargin;
                        yDelta_4J = inicial.y - Params.topMargin;
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    //Vemos donde colocamos la carta
                    double medioLayout = ((float) view.getLayoutParams().height) / 1.2;

                    if (view.getY() < (medioLayout)) {
                        if (tvCartaMesa1_4J.getVisibility() == View.INVISIBLE) {
                            destino.x = tvCartaMesa1_4J.getX();
                            destino.y = tvCartaMesa1_4J.getY();
                            tvCartaMesa1_4J.setVisibility(View.VISIBLE);

                            if(tapoPrimera || tapo){
                                aux = new Carta("1", "oros", "0");
                                asignarImagenCarta(aux, (ImageView) view);
                            }

                            view.animate().x(destino.x).y(destino.y).rotation(0).rotationXBy(30)
                                    .scaleX((float) 0.6).scaleY((float) 0.6)
                                    .rotation(0).setDuration(500);
                            view.setEnabled(false);

                            if (view.equals(tvJugador1_4J)) {
                                posTvJugador1 = 1;
                                tvJugador1_4J.bringToFront();
                                tvJugador2_4J.bringToFront();
                                tvJugador3_4J.bringToFront();

                            } else if (view.equals(tvJugador2_4J)) {
                                tvJugador2_4J.bringToFront();
                                posTvJugador2 = 1;
                                tvJugador1_4J.bringToFront();
                                tvJugador3_4J.bringToFront();

                            } else if (view.equals(tvJugador3_4J)) {
                                posTvJugador3 = 1;
                                tvJugador3_4J.bringToFront();
                                tvJugador1_4J.bringToFront();
                                tvJugador2_4J.bringToFront();
                            }


                        } else if (tvCartaMesa2_4J.getVisibility() == View.INVISIBLE) {
                            destino.x = tvCartaMesa2_4J.getX();
                            destino.y = tvCartaMesa2_4J.getY();
                            tvCartaMesa2_4J.setVisibility(View.VISIBLE);

                            if(tapoPrimera || tapo){
                                aux = new Carta("1", "oros", "0");
                                asignarImagenCarta(aux, (ImageView) view);
                            }

                            view.animate().x(destino.x).y(destino.y).rotation(0).rotationXBy(30)
                                    .scaleX((float) 0.6).scaleY((float) 0.6)
                                    .rotation(0).setDuration(500);
                            view.setEnabled(false);

                            if (view.equals(tvJugador1_4J) && posTvJugador2 == 0) {
                                posTvJugador1 = 2;
                                tvJugador1_4J.bringToFront();
                                tvJugador2_4J.bringToFront();
                                Log.d("RRRRRRR", "Actualizada pos2");
                            } else if (view.equals(tvJugador1_4J) && posTvJugador3 == 0) {
                                posTvJugador1 = 2;
                                tvJugador1_4J.bringToFront();
                                tvJugador3_4J.bringToFront();
                                Log.d("RRRRRRR", "Actualizada pos2");
                            } else if (view.equals(tvJugador2_4J) && posTvJugador1 == 0) {
                                posTvJugador2 = 2;
                                tvJugador2_4J.bringToFront();
                                tvJugador1_4J.bringToFront();
                                Log.d("RRRRRRR", "Actualizada pos2");
                            } else if (view.equals(tvJugador2_4J) && posTvJugador2 == 0) {
                                posTvJugador2 = 2;
                                tvJugador2_4J.bringToFront();
                                tvJugador3_4J.bringToFront();
                                Log.d("RRRRRRR", "Actualizada pos2");
                            } else if (view.equals(tvJugador3_4J) && posTvJugador1 == 0) {
                                posTvJugador3 = 2;
                                tvJugador3_4J.bringToFront();
                                tvJugador1_4J.bringToFront();
                                Log.d("RRRRRRR", "Actualizada pos2");
                            } else if (view.equals(tvJugador3_4J) && posTvJugador2 == 0) {
                                posTvJugador3 = 2;
                                tvJugador3_4J.bringToFront();
                                tvJugador2_4J.bringToFront();
                                Log.d("RRRRRRR", "Actualizada pos2");
                            }


                        } else if (tvCartaMesa3_4J.getVisibility() == View.INVISIBLE) {
                            destino.x = tvCartaMesa3_4J.getX();
                            destino.y = tvCartaMesa3_4J.getY();
                            tvCartaMesa3_4J.setVisibility(View.VISIBLE);

                            if(tapoPrimera || tapo){
                                aux = new Carta("1", "oros", "0");
                                asignarImagenCarta(aux, (ImageView) view);
                            }

                            view.animate().x(destino.x).y(destino.y).rotation(0).rotationXBy(30)
                                    .scaleX((float) 0.6).scaleY((float) 0.6)
                                    .rotation(0).setDuration(500);
                            view.setEnabled(false);

                            if (view.equals(tvJugador1_4J)) {
                                posTvJugador1 = 3;
                                tvJugador1_4J.bringToFront();
                            } else if (view.equals(tvJugador2_4J)) {
                                posTvJugador2 = 3;
                                tvJugador2_4J.bringToFront();
                            } else if (view.equals(tvJugador3_4J)) {
                                posTvJugador3 = 3;
                                tvJugador3_4J.bringToFront();
                            }
                            Log.d("RRRRRRR", "Actualizada pos3");
                        }

                        //Calculamos su valor para enviarlo
                        if(tapoPrimera || tapo){
                            miValor = 0;
                            comprobarSonidosCartas(aux);
                            tapo = false;
                        }else {

                            if (view.equals(tvJugador1_4J)) {
                                aux = carta1;
                                comprobarSonidosCartas(aux);
                                miValor = Integer.parseInt(carta1.getValor());
                            }
                            if (view.equals(tvJugador2_4J)) {
                                aux = carta2;
                                comprobarSonidosCartas(aux);
                                miValor = Integer.parseInt(carta2.getValor());
                            }
                            if (view.equals(tvJugador3_4J)) {
                                aux = carta3;
                                comprobarSonidosCartas(aux);
                                miValor = Integer.parseInt(carta3.getValor());
                            }
                        }
                        envid_4J.setVisibility(View.GONE);
                        laFalta_4J.setVisibility(View.GONE);

                        if (!hayEmpate4J) {
                            cartaSeleccionada();
                        } else cartaSeleccionadaEmpate();

                        hayAnimaciones = true;

                    } else {
                        if (view.equals(tvJugador1_4J)) {
                            view.animate().translationX(inicio1J1.x - 30).setDuration(500);
                            view.animate().translationY(inicio1J1.y + 15);
                        }
                        if (view.equals(tvJugador2_4J)) {
                            view.animate().translationX(inicio2J1.x).setDuration(500);
                            view.animate().translationY(inicio2J1.y).setDuration(500);
                        }
                        if (view.equals(tvJugador3_4J)) {
                            view.animate().translationX(inicio3J1.x + 30).setDuration(500);
                            view.animate().translationY(inicio3J1.y + 15).setDuration(500);
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
                    view.setX((inicio.x + move.x) - xDelta_4J);
                    view.setY((inicio.y + move.y) - yDelta_4J);
                    break;
            }
            return true;
        }
    }

    public boolean esDeMiEquipo(String id) {

        if (mMyId.equals(equipo1[0]) || mMyId.equals(equipo1[1])) {
            if (id.equals(equipo1[0]) || id.equals(equipo1[1])) return true;
        } else if (mMyId.equals(equipo2[0]) || mMyId.equals(equipo2[1])) {
            if (id.equals(equipo2[0]) || id.equals(equipo2[1])) return true;
        }

        return false;
    }

    private void showSingleChoiceAlertEnvid_4J(String title, int array, final String sender) {
        new MaterialDialog.Builder(this)
                .title(title)
                .titleColorRes(R.color.menuItems)
                .items(array)
                .itemColorRes(R.color.menuItems)
                .itemsCallbackSingleChoice(1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        switch (which) {
                            //Quiero
                            case 0:
                                hayEnvid = true;
                                ganadorEnvid = comprobarGanadorEnvid_4J();
                                puntosEnvid = ENVID;
                                enviarMensajeHayEnvidAndGanador(ganadorEnvid, 1);
                                cambiarBarraProgreso();
                                break;
                            //Vuelvo
                            case 1:
                                enviarMensajeVuelvoAEnvidar(sender);
                                //Soy el ultimo
                                if(mMyId.equals(comprobarTerceroeMano())){
                                    cancelarBarraProgreso();
                                    barrasInvisibles();
                                    progressBarIzq.setVisibility(View.VISIBLE);
                                    iniciarBarraProgresoIzq();

                                //Soy tercero
                                }else if(mMyId.equals(comprobarSegundoMano())){
                                    cancelarBarraProgreso();
                                    barrasInvisibles();
                                    progressBarDerecha.setVisibility(View.VISIBLE);
                                    iniciarBarraProgresoDerecha();
                                }
                                //cambiarBarraProgreso();
                                hayVuelvo = true;
                                break;
                            //Falta
                            case 2:
                                enviarMensajeLaFalta(1, sender);
                                //Soy el ultimo
                                if(mMyId.equals(comprobarTerceroeMano())){
                                    cancelarBarraProgreso();
                                    barrasInvisibles();
                                    progressBarIzq.setVisibility(View.VISIBLE);
                                    iniciarBarraProgresoIzq();

                                    //Soy tercero
                                }else if(mMyId.equals(comprobarSegundoMano())){
                                    cancelarBarraProgreso();
                                    barrasInvisibles();
                                    progressBarDerecha.setVisibility(View.VISIBLE);
                                    iniciarBarraProgresoDerecha();
                                }
                                //cambiarBarraProgreso();
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

    private void showSingleChoiceAlertVuelvo_4J(String title, int array, final String sender) {
        new MaterialDialog.Builder(this)
                .title(title)
                .titleColorRes(R.color.menuItems)
                .items(array)
                .itemColorRes(R.color.menuItems)
                .itemsCallbackSingleChoice(1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        switch (which) {
                            //Quiero
                            case 0:
                                hayEnvid = true;
                                ganadorEnvid = comprobarGanadorEnvid_4J();
                                puntosEnvid = TORNE;
                                enviarMensajeHayEnvidAndGanador(ganadorEnvid, 2);
                                cambiarBarraProgreso();
                                if (turno.equals(mMyId)) {
                                    desbloquearCartas();
                                    animarAparecerMenu();
                                }


                                break;
                            //Falta
                            case 1:
                                enviarMensajeLaFalta(1, sender);
                                //Soy el ultimo
                                if(mMyId.equals(comprobarTerceroeMano())){
                                    cancelarBarraProgreso();
                                    barrasInvisibles();
                                    progressBarIzq.setVisibility(View.VISIBLE);
                                    iniciarBarraProgresoIzq();

                                    //Soy tercero
                                }else if(mMyId.equals(comprobarSegundoMano())){
                                    cancelarBarraProgreso();
                                    barrasInvisibles();
                                    progressBarDerecha.setVisibility(View.VISIBLE);
                                    iniciarBarraProgresoDerecha();
                                }
                                //cambiarBarraProgreso();
                                break;
                            //No quiero
                            case 2:
                                enviarMensajeNoQuiero(2);
                                cambiarBarraProgreso();
                                if (turno.equals(mMyId)) {
                                    desbloquearCartas();
                                    animarAparecerMenu();
                                }
                                break;
                        }
                        return true;
                    }
                })
                .positiveText("Elegir")
                .cancelable(false)
                .show().getWindow().setBackgroundDrawable(new ColorDrawable(0x30000000));
    }

    private void showSingleChoiceAlertFalta_4J(String title, int array) {
        new MaterialDialog.Builder(this)
                .title(title)
                .titleColorRes(R.color.menuItems)
                .items(array)
                .itemColorRes(R.color.menuItems)
                .itemsCallbackSingleChoice(1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        switch (which) {
                            //Quiero
                            case 0:
                                hayEnvid = true;
                                ganadorEnvid = comprobarGanadorEnvid_4J();
                                enviarMensajeHayEnvidAndGanador(ganadorEnvid, 3);
                                //puntos a sumar por la falta
                               if (ganadorEnvid.equals(mMyId)) {
                                    if (puntosTotalesJugador2 <= 12) {
                                        puntosEnvid = 24;
                                    } else if (puntosTotalesJugador2 > 12) {
                                        puntosEnvid = 24 - puntosTotalesJugador2;
                                    }
                                }
                                cambiarBarraProgreso();
                                if (turno.equals(mMyId)) {
                                    desbloquearCartas();
                                    animarAparecerMenu();
                                }
                                break;

                            case 1:
                                if (faltaDirecta) {
                                    enviarMensajeNoQuiero(4);
                                } else enviarMensajeNoQuiero(3);
                                cambiarBarraProgreso();
                                if (turno.equals(mMyId)) {
                                    desbloquearCartas();
                                    animarAparecerMenu();
                                }
                                break;
                        }
                        return true;
                    }
                })
                .positiveText("Elegir")
                .cancelable(false)
                .show().getWindow().setBackgroundDrawable(new ColorDrawable(0x30000000));
    }

    private String comprobarGanadorEnvid_4J() {

        int[] numCartaDerecha = new int[3];
        int[] numCartaArriba = new int[3];
        int[] numCartaIzq = new int[3];
        int[] numCartaAbajo = new int[3];

        String valorAbajo = "";
        String valorDerecha = "";
        String valorArriba = "";
        String valorIzq = "";

        if (idJugador1.equals(mano)) {
            numCartaAbajo[0] = Integer.parseInt(arrayCartasJugadores[10]);
            numCartaAbajo[1] = Integer.parseInt(arrayCartasJugadores[11]);
            numCartaAbajo[2] = Integer.parseInt(arrayCartasJugadores[12]);
            numCartaDerecha[0] = Integer.parseInt(arrayCartasJugadores[1]);
            numCartaDerecha[1] = Integer.parseInt(arrayCartasJugadores[2]);
            numCartaDerecha[2] = Integer.parseInt(arrayCartasJugadores[3]);
            numCartaArriba[0] = Integer.parseInt(arrayCartasJugadores[4]);
            numCartaArriba[1] = Integer.parseInt(arrayCartasJugadores[5]);
            numCartaArriba[2] = Integer.parseInt(arrayCartasJugadores[6]);
            numCartaIzq[0] = Integer.parseInt(arrayCartasJugadores[7]);
            numCartaIzq[1] = Integer.parseInt(arrayCartasJugadores[8]);
            numCartaIzq[2] = Integer.parseInt(arrayCartasJugadores[9]);
            valorAbajo = idJugador1;
            valorDerecha = idJugador2;
            valorArriba = idJugador3;
            valorIzq = idJugador4;
        } else if (mano.equals(idJugador2)) {
            numCartaAbajo[0] = Integer.parseInt(arrayCartasJugadores[10]);
            numCartaAbajo[1] = Integer.parseInt(arrayCartasJugadores[11]);
            numCartaAbajo[2] = Integer.parseInt(arrayCartasJugadores[12]);
            numCartaIzq[0] = Integer.parseInt(arrayCartasJugadores[1]);
            numCartaIzq[1] = Integer.parseInt(arrayCartasJugadores[2]);
            numCartaIzq[2] = Integer.parseInt(arrayCartasJugadores[3]);
            numCartaDerecha[0] = Integer.parseInt(arrayCartasJugadores[4]);
            numCartaDerecha[1] = Integer.parseInt(arrayCartasJugadores[5]);
            numCartaDerecha[2] = Integer.parseInt(arrayCartasJugadores[6]);
            numCartaArriba[0] = Integer.parseInt(arrayCartasJugadores[7]);
            numCartaArriba[1] = Integer.parseInt(arrayCartasJugadores[8]);
            numCartaArriba[2] = Integer.parseInt(arrayCartasJugadores[9]);
            valorAbajo = idJugador2;
            valorDerecha = idJugador3;
            valorArriba = idJugador4;
            valorIzq = idJugador1;
        } else if (mano.equals(idJugador3)) {
            numCartaAbajo[0] = Integer.parseInt(arrayCartasJugadores[10]);
            numCartaAbajo[1] = Integer.parseInt(arrayCartasJugadores[11]);
            numCartaAbajo[2] = Integer.parseInt(arrayCartasJugadores[12]);
            numCartaArriba[0] = Integer.parseInt(arrayCartasJugadores[1]);
            numCartaArriba[1] = Integer.parseInt(arrayCartasJugadores[2]);
            numCartaArriba[2] = Integer.parseInt(arrayCartasJugadores[3]);
            numCartaIzq[0] = Integer.parseInt(arrayCartasJugadores[4]);
            numCartaIzq[1] = Integer.parseInt(arrayCartasJugadores[5]);
            numCartaIzq[2] = Integer.parseInt(arrayCartasJugadores[6]);
            numCartaDerecha[0] = Integer.parseInt(arrayCartasJugadores[7]);
            numCartaDerecha[1] = Integer.parseInt(arrayCartasJugadores[8]);
            numCartaDerecha[2] = Integer.parseInt(arrayCartasJugadores[9]);
            valorAbajo = idJugador3;
            valorDerecha = idJugador4;
            valorArriba = idJugador1;
            valorIzq = idJugador2;
        } else if (mano.equals(idJugador4)) {
            numCartaAbajo[0] = Integer.parseInt(arrayCartasJugadores[10]);
            numCartaAbajo[1] = Integer.parseInt(arrayCartasJugadores[11]);
            numCartaAbajo[2] = Integer.parseInt(arrayCartasJugadores[12]);
            numCartaDerecha[0] = Integer.parseInt(arrayCartasJugadores[1]);
            numCartaDerecha[1] = Integer.parseInt(arrayCartasJugadores[2]);
            numCartaDerecha[2] = Integer.parseInt(arrayCartasJugadores[3]);
            numCartaArriba[0] = Integer.parseInt(arrayCartasJugadores[4]);
            numCartaArriba[1] = Integer.parseInt(arrayCartasJugadores[5]);
            numCartaArriba[2] = Integer.parseInt(arrayCartasJugadores[6]);
            numCartaIzq[0] = Integer.parseInt(arrayCartasJugadores[7]);
            numCartaIzq[1] = Integer.parseInt(arrayCartasJugadores[8]);
            numCartaIzq[2] = Integer.parseInt(arrayCartasJugadores[9]);
            valorAbajo = idJugador4;
            valorDerecha = idJugador1;
            valorArriba = idJugador2;
            valorIzq = idJugador3;
        }

        baraja = crearBaraja();
        Carta C1_J1 = baraja.get(numCartaAbajo[0]);
        Carta C2_J1 = baraja.get(numCartaAbajo[1]);
        Carta C3_J1 = baraja.get(numCartaAbajo[2]);
        Carta C1_J2 = baraja.get(numCartaDerecha[0]);
        Carta C2_J2 = baraja.get(numCartaDerecha[1]);
        Carta C3_J2 = baraja.get(numCartaDerecha[2]);
        Carta C1_J3 = baraja.get(numCartaArriba[0]);
        Carta C2_J3 = baraja.get(numCartaArriba[1]);
        Carta C3_J3 = baraja.get(numCartaArriba[2]);
        Carta C1_J4 = baraja.get(numCartaIzq[0]);
        Carta C2_J4 = baraja.get(numCartaIzq[1]);
        Carta C3_J4 = baraja.get(numCartaIzq[2]);

        int valorAbajoEnvid = 0;
        int valorDerechaEnvid = 0;
        int valorArribaEnvid = 0;
        int valorIzqEnvid = 0;

        todosMismoPalo = false;

        /** Calculo del envid del JUGADOR 1 */
        if (C1_J1.getPalo().equals(C2_J1.getPalo()) && C1_J1.getPalo().equals(C3_J1.getPalo()))
            todosMismoPalo = true;

        if (todosMismoPalo) {
            if (C1_J1.getNumero().compareTo(C2_J1.getNumero()) > 0) {
                if (C2_J1.getNumero().compareTo(C3_J1.getNumero()) > 0) {
                    valorAbajoEnvid = Integer.parseInt(C1_J1.getNumero()) + Integer.parseInt(C2_J1.getNumero()) + 20;
                } else {
                    valorAbajoEnvid = Integer.parseInt(C1_J1.getNumero()) + Integer.parseInt(C3_J1.getNumero()) + 20;
                }
            } else {
                if (C1_J1.getNumero().compareTo(C3_J1.getNumero()) > 0) {
                    valorAbajoEnvid = Integer.parseInt(C1_J1.getNumero()) + Integer.parseInt(C2_J1.getNumero()) + 20;
                } else
                    valorAbajoEnvid = Integer.parseInt(C3_J1.getNumero()) + Integer.parseInt(C2_J1.getNumero()) + 20;

            }

        } else {
            if (C1_J1.getPalo().equals(C2_J1.getPalo()))
                valorAbajoEnvid = Integer.parseInt(C1_J1.getNumero()) + Integer.parseInt(C2_J1.getNumero()) + 20;
            else if (C1_J1.getPalo().equals(C3_J1.getPalo()))
                valorAbajoEnvid = Integer.parseInt(C1_J1.getNumero()) + Integer.parseInt(C3_J1.getNumero()) + 20;
            else if (C2_J1.getPalo().equals(C3_J1.getPalo()))
                valorAbajoEnvid = Integer.parseInt(C3_J1.getNumero()) + Integer.parseInt(C2_J1.getNumero()) + 20;
        }

        if (valorAbajoEnvid == 0) {

            if (C1_J1.getNumero().compareTo(C2_J1.getNumero()) > 0) {
                if (C1_J1.getNumero().compareTo(C3_J1.getNumero()) > 0) {
                    valorAbajoEnvid = Integer.parseInt(C1_J1.getNumero());
                } else valorAbajoEnvid = Integer.parseInt(C3_J1.getNumero());
            } else if (C2_J1.getNumero().compareTo(C3_J1.getNumero()) > 0) {
                valorAbajoEnvid = Integer.parseInt(C2_J1.getNumero());
            } else valorAbajoEnvid = Integer.parseInt(C3_J1.getNumero());

        }

        todosMismoPalo = false;

        /** Calculo del envid del JUGADOR 2 */
        if (C1_J2.getPalo().equals(C2_J2.getPalo()) && C1_J2.getPalo().equals(C3_J2.getPalo()))
            todosMismoPalo = true;

        if (todosMismoPalo) {
            if (C1_J2.getNumero().compareTo(C2_J2.getNumero()) > 0) {
                if (C2_J2.getNumero().compareTo(C3_J2.getNumero()) > 0) {
                    valorDerechaEnvid = Integer.parseInt(C1_J2.getNumero()) + Integer.parseInt(C2_J2.getNumero()) + 20;
                } else {
                    valorDerechaEnvid = Integer.parseInt(C1_J2.getNumero()) + Integer.parseInt(C3_J2.getNumero()) + 20;
                }
            } else {
                if (C1_J2.getNumero().compareTo(C3_J2.getNumero()) > 0) {
                    valorDerechaEnvid = Integer.parseInt(C1_J2.getNumero()) + Integer.parseInt(C2_J2.getNumero()) + 20;
                } else
                    valorDerechaEnvid = Integer.parseInt(C3_J2.getNumero()) + Integer.parseInt(C2_J2.getNumero()) + 20;

            }

        } else {
            if (C1_J2.getPalo().equals(C2_J2.getPalo()))
                valorDerechaEnvid = Integer.parseInt(C1_J2.getNumero()) + Integer.parseInt(C2_J2.getNumero()) + 20;
            else if (C1_J2.getPalo().equals(C3_J2.getPalo()))
                valorDerechaEnvid = Integer.parseInt(C1_J2.getNumero()) + Integer.parseInt(C3_J2.getNumero()) + 20;
            else if (C2_J2.getPalo().equals(C3_J2.getPalo()))
                valorDerechaEnvid = Integer.parseInt(C3_J2.getNumero()) + Integer.parseInt(C2_J2.getNumero()) + 20;
        }

        if (valorDerechaEnvid == 0) {

            if (C1_J2.getNumero().compareTo(C2_J2.getNumero()) > 0) {
                if (C1_J2.getNumero().compareTo(C3_J2.getNumero()) > 0) {
                    valorDerechaEnvid = Integer.parseInt(C1_J2.getNumero());
                } else valorDerechaEnvid = Integer.parseInt(C3_J2.getNumero());
            } else if (C2_J2.getNumero().compareTo(C3_J2.getNumero()) > 0) {
                valorDerechaEnvid = Integer.parseInt(C2_J2.getNumero());
            } else valorDerechaEnvid = Integer.parseInt(C3_J2.getNumero());

        }

        todosMismoPalo = false;

        /** Calculo del envid del JUGADOR 3 */
        if (C1_J3.getPalo().equals(C2_J3.getPalo()) && C1_J3.getPalo().equals(C3_J3.getPalo()))
            todosMismoPalo = true;

        if (todosMismoPalo) {
            if (C1_J3.getNumero().compareTo(C2_J3.getNumero()) > 0) {
                if (C2_J3.getNumero().compareTo(C3_J3.getNumero()) > 0) {
                    valorArribaEnvid = Integer.parseInt(C1_J3.getNumero()) + Integer.parseInt(C2_J3.getNumero()) + 20;
                } else {
                    valorArribaEnvid = Integer.parseInt(C1_J3.getNumero()) + Integer.parseInt(C3_J3.getNumero()) + 20;
                }
            } else {
                if (C1_J3.getNumero().compareTo(C3_J3.getNumero()) > 0) {
                    valorArribaEnvid = Integer.parseInt(C1_J3.getNumero()) + Integer.parseInt(C2_J3.getNumero()) + 20;
                } else
                    valorArribaEnvid = Integer.parseInt(C3_J3.getNumero()) + Integer.parseInt(C2_J3.getNumero()) + 20;

            }

        } else {
            if (C1_J3.getPalo().equals(C2_J3.getPalo()))
                valorArribaEnvid = Integer.parseInt(C1_J3.getNumero()) + Integer.parseInt(C2_J3.getNumero()) + 20;
            else if (C1_J3.getPalo().equals(C3_J3.getPalo()))
                valorArribaEnvid = Integer.parseInt(C1_J3.getNumero()) + Integer.parseInt(C3_J3.getNumero()) + 20;
            else if (C2_J3.getPalo().equals(C3_J3.getPalo()))
                valorArribaEnvid = Integer.parseInt(C3_J3.getNumero()) + Integer.parseInt(C2_J3.getNumero()) + 20;
        }

        if (valorArribaEnvid == 0) {

            if (C1_J3.getNumero().compareTo(C2_J3.getNumero()) > 0) {
                if (C1_J3.getNumero().compareTo(C3_J3.getNumero()) > 0) {
                    valorArribaEnvid = Integer.parseInt(C1_J3.getNumero());
                } else valorArribaEnvid = Integer.parseInt(C3_J3.getNumero());
            } else if (C2_J3.getNumero().compareTo(C3_J3.getNumero()) > 0) {
                valorArribaEnvid = Integer.parseInt(C2_J3.getNumero());
            } else valorArribaEnvid = Integer.parseInt(C3_J3.getNumero());

        }

        todosMismoPalo = false;

        /** Calculo del envid del JUGADOR 4 */
        if (C1_J4.getPalo().equals(C2_J4.getPalo()) && C1_J4.getPalo().equals(C3_J4.getPalo()))
            todosMismoPalo = true;

        if (todosMismoPalo) {
            if (C1_J4.getNumero().compareTo(C2_J4.getNumero()) > 0) {
                if (C2_J4.getNumero().compareTo(C3_J4.getNumero()) > 0) {
                    valorIzqEnvid = Integer.parseInt(C1_J4.getNumero()) + Integer.parseInt(C2_J4.getNumero()) + 20;
                } else {
                    valorIzqEnvid = Integer.parseInt(C1_J4.getNumero()) + Integer.parseInt(C3_J4.getNumero()) + 20;
                }
            } else {
                if (C1_J4.getNumero().compareTo(C3_J4.getNumero()) > 0) {
                    valorIzqEnvid = Integer.parseInt(C1_J4.getNumero()) + Integer.parseInt(C2_J4.getNumero()) + 20;
                } else
                    valorIzqEnvid = Integer.parseInt(C3_J4.getNumero()) + Integer.parseInt(C2_J4.getNumero()) + 20;

            }

        } else {
            if (C1_J4.getPalo().equals(C2_J4.getPalo()))
                valorIzqEnvid = Integer.parseInt(C1_J4.getNumero()) + Integer.parseInt(C2_J4.getNumero()) + 20;
            else if (C1_J4.getPalo().equals(C3_J4.getPalo()))
                valorIzqEnvid = Integer.parseInt(C1_J4.getNumero()) + Integer.parseInt(C3_J4.getNumero()) + 20;
            else if (C2_J4.getPalo().equals(C3_J4.getPalo()))
                valorIzqEnvid = Integer.parseInt(C3_J4.getNumero()) + Integer.parseInt(C2_J4.getNumero()) + 20;
        }

        if (valorIzqEnvid == 0) {

            if (C1_J4.getNumero().compareTo(C2_J4.getNumero()) > 0) {
                if (C1_J4.getNumero().compareTo(C3_J4.getNumero()) > 0) {
                    valorIzqEnvid = Integer.parseInt(C1_J4.getNumero());
                } else valorIzqEnvid = Integer.parseInt(C3_J4.getNumero());
            } else if (C2_J4.getNumero().compareTo(C3_J4.getNumero()) > 0) {
                valorIzqEnvid = Integer.parseInt(C2_J4.getNumero());
            } else valorIzqEnvid = Integer.parseInt(C3_J4.getNumero());

        }

        todosMismoPalo = false;

        Log.d("KKKKKKKK", "valor envid del mano: " + valorAbajoEnvid);
        Log.d("KKKKKKKK", "valor envid del de la derecha del mano: " + valorDerechaEnvid);
        Log.d("KKKKKKKK", "valor envid del de arriba: " + valorArribaEnvid);
        Log.d("KKKKKKKK", "valor envid del de la izquierda: " + valorIzqEnvid);

        int[] maximosPrime = {valorAbajoEnvid, valorDerechaEnvid, valorArribaEnvid, valorIzqEnvid};
        maximo = maximosPrime[0];
        for (int i = 0; i < maximosPrime.length; i++) {
            if (maximo < maximosPrime[i]) {
                maximo = maximosPrime[i];
            }
        }


        if (maximo == valorAbajoEnvid) {
            Log.d("KKKKKKKK", "id Ganador del envid: " + valorAbajo);
            return valorAbajo;
        } else if (maximo == valorDerechaEnvid) {
            Log.d("KKKKKKKK", "id Ganador del envid: " + valorDerecha);
            return valorDerecha;
        } else if (maximo == valorArribaEnvid) {
            Log.d("KKKKKKKK", "id Ganador del envid: " + valorArriba);
            return valorArriba;
        } else if (maximo == valorIzqEnvid) {
            Log.d("KKKKKKKK", "id Ganador del envid: " + valorIzq);
            return valorIzq;
        }

        return "";
    }

    private void showSingleChoiceAlertTruco_4J(String title, int array, final String sender) {
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
                                enviarMensajeReQuaJoc_4J("QUIERO", 1);
                                activarDesactivarMiBarra("DESACTIVAR");
                                retruque_4J.setVisibility(View.VISIBLE);
                                break;
                            //Retruque
                            case 1:
                                enviarMensajeReQuaJoc_4J("RETRUQUE", 1);
                                activarDesactivarMiBarra("DESACTIVAR");
                                break;
                            //No quiero
                            case 2:
                                enviarMensajeReQuaJoc_4J("NOQUIERO", 1);
                                activarDesactivarMiBarra("DESACTIVAR");
                                break;
                        }
                        return true;
                    }
                })
                .positiveText("Elegir")
                .cancelable(false)
                .show().getWindow().setBackgroundDrawable(new ColorDrawable(0x30000000));
    }

    private void showSingleChoiceAlertRetruc_4J(String title, int array, final String sender) {
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
                                enviarMensajeReQuaJoc_4J("QUIERO", 2);
                                activarDesactivarMiBarra("DESACTIVAR");
                                quatreVal_4J.setVisibility(View.VISIBLE);
                                break;
                            //Cuatre val
                            case 1:
                                enviarMensajeReQuaJoc_4J("QUATRE", 2);
                                activarDesactivarMiBarra("DESACTIVAR");
                                break;
                            //No quiero
                            case 2:
                                enviarMensajeReQuaJoc_4J("NOQUIERO", 2);
                                activarDesactivarMiBarra("DESACTIVAR");

                                break;
                        }
                        return true;
                    }
                })
                .positiveText("Elegir")
                .cancelable(false)
                .show().getWindow().setBackgroundDrawable(new ColorDrawable(0x30000000));
    }

    private void showSingleChoiceAlertCuatreVal_4J(String title, int array, final String sender) {
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
                                enviarMensajeReQuaJoc_4J("QUIERO", 3);
                                activarDesactivarMiBarra("DESACTIVAR");
                                jocFora_4J.setVisibility(View.VISIBLE);
                                break;
                            //Joc fora
                            case 1:
                                enviarMensajeReQuaJoc_4J("JOC", 3);
                                activarDesactivarMiBarra("DESACTIVAR");
                                break;
                            //No quiero
                            case 2:
                                enviarMensajeReQuaJoc_4J("NOQUIERO", 3);
                                activarDesactivarMiBarra("DESACTIVAR");
                                break;
                        }
                        return true;
                    }
                })
                .positiveText("Elegir")
                .cancelable(false)
                .show().getWindow().setBackgroundDrawable(new ColorDrawable(0x30000000));
    }

    private void showSingleChoiceAlertJocFora_4J(String title, int array, final String sender) {
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
                                enviarMensajeReQuaJoc_4J("QUIERO", 4);
                                desbloquearCartas();
                                activarDesactivarMiBarra("DESACTIVAR");
                                break;
                            //No quiero
                            case 1:
                                enviarMensajeReQuaJoc_4J("NOQUIERO", 4);
                                break;

                        }
                        return true;
                    }
                })
                .positiveText("Elegir")
                .cancelable(false)
                .show().getWindow().setBackgroundDrawable(new ColorDrawable(0x30000000));
    }


    private static void updateLeaderboards(final GoogleApiClient googleApiClient, final String leaderboardId) {
        Games.Leaderboards.loadCurrentPlayerLeaderboardScore(
                googleApiClient,
                leaderboardId,
                LeaderboardVariant.TIME_SPAN_ALL_TIME,
                LeaderboardVariant.COLLECTION_PUBLIC
        ).setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {

            @Override
            public void onResult(Leaderboards.LoadPlayerScoreResult loadPlayerScoreResult) {
                if (loadPlayerScoreResult != null) {
                    if (GamesStatusCodes.STATUS_OK == loadPlayerScoreResult.getStatus().getStatusCode()) {
                        long score = 0;
                        if (loadPlayerScoreResult.getScore() != null) {
                            score = loadPlayerScoreResult.getScore().getRawScore();
                        }
                        Games.Leaderboards.submitScore(googleApiClient, leaderboardId, ++score);
                    }
                }
            }

        });
    }

    /* Funciones para el sonido del juego */

    public void comprobarSonidosCartas(Carta newCarta){
        //Sonidos dependiendo de la carta que se tira
        if(newCarta.getValor().equals("10")) reproducirSonidoEspada();
        else if(newCarta.getValor().equals("9")) reproducirSonidoBasto();
        else reproducirSonidoTirarCarta();
    }
    private void reproducirSonidoBasto(){
        soundPool.play(sonidoBasto, 1, 1, 0, 0, 1);
    }
    private void reproducirSonidoEspada(){
        soundPool.play(sonidoEspada, 1, 1, 0, 0, 1);
    }
    private void reproducirSonidoRepartir(){
        soundPool.play(sonidoRepartir, 1, 1, 0, 0, 1);
    }
    private void reproducirSonidoTirarCarta() {
        soundPool.play(sonidoTirar, 1, 1, 0, 0, 1);
    }

}






