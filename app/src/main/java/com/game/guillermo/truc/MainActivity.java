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
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
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


import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
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
    TextView textoAccion;
    private List<Carta> baraja = new ArrayList<>();
    private List<Carta> manoJugador = new ArrayList<>();
    Carta carta1;
    Carta carta2;
    Carta carta3;
    int miValor=0, valor1 = 0, valor2 = 0, valor3 = 0, valorEmpate = 0;
    int ronda = 1;
    int misRondasGanadas = 0;
    boolean casoTiroPrimero = false;
    AlertDialog.Builder dialogoNuevaPartida;
    AlertDialog dialog;
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
    int puntos = 0;
    int miEnvid = 0;
    boolean hayEnvid = false;
    int puntosTotalesMios = 0;
    int puntosTotalesJugador2 = 0;
    String ganadorRonda1 = null;
    String sCartasJ2 = "";
    int[] list = new int[3];
    int[] list2 = new int[3];
    int[] numCarta = new int[3];
    boolean mostrarRepartiendo = false;
    Carta aux;
    int envidOtro = 0;
    String ganadorEnvid = null;

    private FloatingActionMenu menu;
    private FloatingActionButton fabTruc;
    private FloatingActionButton fabEnvid;
    private FloatingActionButton fabMeVoy;
    MaterialDialog.Builder materialDialog;
    MaterialDialog repartiendo;
    MaterialDialog dialogEnvid;
    MaterialDialog.ButtonCallback callbackReinicio;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = "";

                switch (v.getId()) {
                    case R.id.fab12:
                        text = fabTruc.getLabelText();
                        break;
                    case R.id.fab22:
                        envido();
                        break;
                    case R.id.fab32:
                        text = fabMeVoy.getLabelText();
                        break;
                }

                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        };

        menu = (FloatingActionMenu) findViewById(R.id.menu);
        menu.showMenuButton(true);
        menu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
                String text = "";
                if (opened) {
                    //Bloquear todos los elementos
                } else {
                    //Desbloquear todos los elementos
                }
            }
        });

        fabTruc = (FloatingActionButton) findViewById(R.id.fab12);
        fabEnvid = (FloatingActionButton) findViewById(R.id.fab22);
        fabMeVoy = (FloatingActionButton) findViewById(R.id.fab32);

        fabTruc.setOnClickListener(clickListener);
        fabEnvid.setOnClickListener(clickListener);
        fabMeVoy.setOnClickListener(clickListener);

        tvJugador1 = (ImageView) findViewById(R.id.carta1Jugador);
        tvJugador2 = (ImageView) findViewById(R.id.carta2Jugador);
        tvJugador3 = (ImageView) findViewById(R.id.carta3Jugador);

        tvCartaMesa1 = (ImageView) findViewById(R.id.carta1Mesa);
        tvCartaMesa2 = (ImageView) findViewById(R.id.carta2Mesa);
        tvCartaMesa3 = (ImageView) findViewById(R.id.carta3Mesa);

        tvMesaRival1 = (ImageView) findViewById(R.id.carta1MesaRival);
        tvMesaRival2 = (ImageView) findViewById(R.id.carta2MesaRival);
        tvMesaRival3 = (ImageView) findViewById(R.id.carta3MesaRival);

        textoAccion = (TextView) findViewById(R.id.textoJugador2);
        marcador = (TextView) findViewById(R.id.marcador);

        dialogoNuevaPartida = new AlertDialog.Builder(this);
        dialogoNuevaPartida.setTitle("Importante");
        dialogoNuevaPartida.setMessage("Este es un programa solo de prueba y no la versión completa");



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
    private void showSingleChoiceAlert(String title, int array) {
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
                                enviarMensajeHayEnvidAndGanador(ganadorEnvid, 1);
                                break;
                            //Vuelvo
                            case 1:
                                enviarMensajeVuelvoAEnvidar();
                                break;
                            //Falta
                            case 2:
                                enviarMensajeLaFalta();
                                break;
                            //No quiero
                            case 3:
                                enviarMensajeNoQuiero(1);
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
                                desbloquearCartas();
                                break;
                            //Falta
                            case 1:
                                enviarMensajeLaFalta();
                                break;
                            //No quiero
                            case 2:
                                desbloquearCartas();
                                menu.setClickable(true);
                                enviarMensajeNoQuiero(2);
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
                                if(mMyId.equals(turno))desbloquearCartas();
                                comprobarGanadorEnvid();
                                enviarMensajeHayEnvidAndGanador(ganadorEnvid, 3);
                                break;
                            case 1:
                                if(mMyId.equals(turno))desbloquearCartas();
                                menu.setClickable(true);
                                enviarMensajeNoQuiero(3);
                                break;
                        }
                        return true;
                    }
                })
                .positiveText("Elegir")
                .cancelable(false)
                .show().getWindow().setBackgroundDrawable(new ColorDrawable(0x30000000));
    }
    private void comprobarGanadorEnvid(){
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
    private void envido(){
        menu.close(true);
        Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
        tvJugador1.setEnabled(false);
        tvJugador2.setEnabled(false);
        tvJugador3.setEnabled(false);
        menu.setClickable(false);
        miEnvid = comprobarEnvid();
        enviarMensajeEnvid(miEnvid);
        fabEnvid.setEnabled(false);
    }
    private int comprobarEnvid(){
        String palo1 = carta1.getPalo();
        String palo2 = carta2.getPalo();
        String palo3 = carta3.getPalo();
        boolean todosMismoPalo = false;
        int maximo = 0;

        if(palo1.equals(palo2) && palo1.equals(palo3)) todosMismoPalo = true;

        if(todosMismoPalo){
            if(carta1.getNumero().compareTo(carta2.getNumero()) > 0){
                if(carta2.getNumero().compareTo(carta3.getNumero()) > 0) {
                    return Integer.parseInt(carta1.getNumero()) + Integer.parseInt(carta2.getNumero()) + 20;
                }else { return Integer.parseInt(carta1.getNumero()) + Integer.parseInt(carta3.getNumero()) + 20 ; }
            }else{
                if(carta1.getNumero().compareTo(carta3.getNumero()) > 0){
                    return Integer.parseInt(carta1.getNumero()) + Integer.parseInt(carta2.getNumero()) + 20;
                }else  return Integer.parseInt(carta3.getNumero()) + Integer.parseInt(carta2.getNumero()) + 20;

            }

        }

        else {
            if(palo1.equals(palo2))  return Integer.parseInt(carta1.getNumero()) + Integer.parseInt(carta2.getNumero()) + 20;
            else if(palo1.equals(palo3)) return Integer.parseInt(carta1.getNumero()) + Integer.parseInt(carta3.getNumero()) + 20;
            else if(palo2.equals(palo3)) return Integer.parseInt(carta3.getNumero()) + Integer.parseInt(carta2.getNumero()) + 20;
        }

        maximo = Integer.parseInt(carta3.getNumero());
        if(carta1.getNumero().compareTo(carta2.getNumero()) > 0){
            if(carta2.getNumero().compareTo(carta3.getNumero()) > 0) {
                maximo = Integer.parseInt(carta1.getNumero());
            }else{
                if(carta1.getNumero().compareTo(carta3.getNumero()) > 0){
                    maximo = Integer.parseInt(carta2.getNumero());
                }
            }
        }


        return maximo;



    }

  @Override
  public void onClick(View v) {
    Intent intent;

        switch (v.getId()) {
            case R.id.button_single_player_2:
                switchToScreen(R.id.screen_game);
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

                    showProgressDialog("¡Empezamos!");
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
                  BaseGameUtils.showActivityResultError(this,requestCode,responseCode, R.string.signin_other_error);
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

        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()){
          switchToScreen(R.id.screen_sign_in);
        }
        else {
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
        } else {
          Log.d(TAG,"Connecting client.");
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
          Log.d(TAG,"onConnected: connection hint has a room invite!");
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

        ArrayList<String> ids = room.getParticipantIds();
        for(int i = 0; i<ids.size(); i++){
            String aux = ids.get(i);
            if(!aux.equals(mMyId)){
                remoteId = aux;
                break;
            }
        }
        if(mMyId.compareTo(remoteId) > 0){
            idJugador1 = mMyId;
            idJugador2 = remoteId;
        }
        else{
            idJugador1 = remoteId;
            idJugador2 = mMyId;
        }

        turno = idJugador1;
        mano = idJugador1;

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

    void resetAll(){
        miValor = 0;
        valor1 = 0; valor2 = 0; valor3 = 0; valorEmpate =0;
        ronda = 1;
        misRondasGanadas = 0;
        casoTiroPrimero = false;
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
        ganadorRonda1 = null;
        miEnvid = 0;
        fabEnvid.setEnabled(true);
        hayEnvid = false;
        ganadorEnvid = "";
        envidOtro = 0;
    }


    // Reset game variables in preparation for a new game.
    void desbloquearCartas() {
        tvJugador1.setEnabled(true);
        tvJugador2.setEnabled(true);
        tvJugador3.setEnabled(true);
    }
    void resetTurno() {
        turno = null;
        idJugador1 = null;
        idJugador2 = null;
    }
    void cambiarTurno(){
        if(mMyId.equals(idJugador1) && turno.equals(idJugador1)) turno = idJugador2;
        if(mMyId.equals(idJugador2) && turno.equals(idJugador2)) turno = idJugador1;

    }
    // Start the gameplay phase of the game.
    void startGame(boolean multiplayer) {

        asignarImagenCarta(carta1, tvJugador1);
        asignarImagenCarta(carta2, tvJugador2);
        asignarImagenCarta(carta3, tvJugador3);

        tvJugador1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!hayEmpate){
                    cartaSeleccionada(view);
                }else cartaSeleccionadaEmpate(view);

            }
        });
        tvJugador2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!hayEmpate){
                    cartaSeleccionada(view);
                }else cartaSeleccionadaEmpate(view);
            }
        });
        tvJugador3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hayEmpate) {
                    cartaSeleccionada(view);
                } else cartaSeleccionadaEmpate(view);
            }
        });

        if(mMyId.equals(turno) && ronda == 1){
            Toast.makeText(getApplicationContext(),"Es tu turno", Toast.LENGTH_SHORT).show();
        }

        if(!mMyId.equals(turno) && ronda == 1) {
            tvJugador1.setEnabled(false);
            tvJugador2.setEnabled(false);
            tvJugador3.setEnabled(false);
            fabEnvid.setEnabled(false);
            Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();

        }

        switchToScreen(R.id.screen_game);
        mMultiplayer = multiplayer;

    }
    void cartaSeleccionada(View view){

        ponerCartaSobreMesa(view);
        //Envio el valor de la carta
        byte[] messageCarta = ("$"+aux.toString()).getBytes();
        enviarValorCarta(messageCarta);
        //Caso en el que NO hay empate
        if(!hayEmpate()) {
            //Compruebo si tiro primero
            tiroPrimero();
            //Caso en el que ganas tirando segundo
            if (soyGanadorRonda()) {
                if (ronda == 1){
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
            }*/
        }else{
        //Caso en el que hay empate
        if (ronda == 1){
            enviarMensajeHayEmpate();
            casoEmpatePrimero();
        }else{
            enviarMensajeHayEmpate();
            casoEmpateTercero();
        }
        }

    }

    void casoEmpatePrimero(){
        showBasicAlert("Empate en la primera ronda!", "La carta que elijas será mostrada arriba");
        //Si no soy mano, cambio turno
        if(!mMyId.equals(mano)) {
            desbloquearCartas();
            Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
            tvJugador1.setEnabled(false);
            tvJugador2.setEnabled(false);
            tvJugador3.setEnabled(false);
            cambiarTurno();
            enviarMensajeTurno();
        }
        //Si soy mano espero a carta seleccionada tras empate
    }
    void casoEmpateTercero(){
        //Caso en el que gano
        if(ganadorRonda1.equals(mMyId)){
            enviarMensajeHasPerdido();
            mostrarResultadosGanadorMano();

        //Caso en el que pierdo
        }else{
            enviarMensajeSumaRonda();
            mostrarResultadosPerdedorMano();
        }
    }

    void cartaSeleccionadaEmpate(View view){

        Carta aux = new Carta(null ,null,null);
        if(view.equals(tvJugador1)){ aux=carta1; miValor = Integer.parseInt(carta1.getValor());}
        if(view.equals(tvJugador2)){ aux=carta2; miValor = Integer.parseInt(carta2.getValor());}
        if(view.equals(tvJugador3)){ aux=carta3; miValor = Integer.parseInt(carta3.getValor());}

        tvJugador1.setVisibility(View.INVISIBLE);
        tvJugador2.setVisibility(View.INVISIBLE);
        tvJugador3.setVisibility(View.INVISIBLE);

        if(tvCartaMesa1.getVisibility() == View.INVISIBLE){
            //tvCartaMesa1.setText(aux.getText().toString());
            asignarImagenCarta(aux,tvCartaMesa1);
            tvCartaMesa1.setVisibility(View.VISIBLE);
        }
        else if(tvCartaMesa2.getVisibility() == View.INVISIBLE){
            //tvCartaMesa2.setText(aux.getText().toString());
            asignarImagenCarta(aux,tvCartaMesa2);
            tvCartaMesa2.setVisibility(View.VISIBLE);
        }
        else if(tvCartaMesa3.getVisibility() == View.INVISIBLE){
            //tvCartaMesa3.setText(aux.getText().toString());
            asignarImagenCarta(aux,tvCartaMesa3);
            tvCartaMesa3.setVisibility(View.VISIBLE);
        }
        byte[] messageCarta = ("$"+aux.toString()).getBytes();
        enviarValorCarta(messageCarta);
        // Si soy mano, tiro y cambio turno
        if(mMyId.equals(mano)){
            cambiarTurno();
            enviarMensajeTurno();
            Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
        //Si no soy mano, compruebo quien gana
        } else if(soyGanadorRondaEmpate()){
            //Caso en el que gano
            enviarMensajeHasPerdido();
            mostrarResultadosGanadorMano();

        }else{
            //Caso en el que pierdo
            enviarMensajeSumaRonda();
            mostrarResultadosPerdedorMano();
        }
    }

    /*
     * ALGUNOS METODOS
     *
     *
     *
     *
     */
    void tiroPrimero(){
        if(ronda == 1 && valor1 == 0) casoTiroPrimero = true;
        if(ronda == 2 && valor2 == 0) casoTiroPrimero = true;
        if(ronda == 3 && valor3 == 0) casoTiroPrimero = true;
    }
    boolean soyGanadorRonda(){
        //Ronda 1
        if(ronda == 1 && miValor>valor1 && valor1!=0){
            casoTiroPrimero = false;
            return true;
        }
        //Ronda 2
        if(ronda == 2 && miValor>valor2 && valor2!=0){
            casoTiroPrimero = false;
            return true;
        }
        //Ronda 3
        if(ronda == 3 && miValor>valor3 && valor3!=0){
            casoTiroPrimero = false;
            return true;
        }
        return false;
    }
    boolean soyGanadorRondaEmpate(){
        if(miValor>valorEmpate){
            return true;
        }
        return false;
    }
    boolean hayEmpate(){
        if(ronda == 1 && miValor == valor1 && !casoTiroPrimero){
            hayEmpate = true;
            return true;
        }
        if(ronda == 3 && miValor == valor3 && !casoTiroPrimero){
            hayEmpate = true;
            return true;
        }
        return false;
    }

    void actualizaRonda(){
        if(valor1 != 0 && valor2 == 0) ronda = 2;
        if(valor1 != 0 && valor2 != 0) ronda = 3;

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
    public void cerrarAccion(int milisegundos) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // acciones que se ejecutan tras los milisegundos
                textoAccion.setText("");

            }
        }, milisegundos);
    }
    public void mostrarResultadosGanadorMano(){
        if(hayEnvid){
            LayoutInflater inflater = getLayoutInflater();
            ViewGroup container = null;

            if(ganadorEnvid.equals(mMyId)){
                View layout = inflater.inflate(R.layout.progres_content, container);
                String content = "Tu envid: "+miEnvid+" Envid rival: "+envidOtro;
                showProgressCustomDialog(layout);

            }else{
                View layout = inflater.inflate(R.layout.progres_content2, container);
                String content = "Tu envid: "+miEnvid+" Envid rival: "+envidOtro;
                showProgressCustomDialog(layout);
            }
        } else {
            showProgressDialog("Enhorabuena, ganas la mano");
        }
        repartirTrasMano();
    }
    public void mostrarResultadosPerdedorMano(){
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup container = null;

        if(hayEnvid){
            if(ganadorEnvid.equals(mMyId)){
                View layout = inflater.inflate(R.layout.progres_content3, container);
                String content = "Lástima, pierdes la mano\nGanaste el envid:\nTu envid: "+miEnvid+" Envid rival: "+envidOtro;
                showProgressCustomDialog(layout);

            }else{
                View layout = inflater.inflate(R.layout.progres_content4, container);
                String content = "Lástima, pierdes la mano\nPerdiste el envid:\nTu envid: "+miEnvid+" Envid rival: "+envidOtro;
                showProgressCustomDialog(layout);
            }
        } else {
            showProgressDialog("Lástima, pierdes la mano");
        }
        repartirTrasMano();
    }



    /*  MESSAGE SECTION
     * E: Mensaje de hay empate.
     * R: Mensaje de ronda.
     * W: Mensaje indicador de que he ganado la mano.
     * G: Indica que he perdido la ronda y el otro jugador debe de sumar rondas ganadas.
     * S: Mensaje de repartir las cartas.
     * M: Mensaje de actualizar la mano.
     * N: Mensaje indicador de que he envidado.
     * K: Mensaje de envid aceptado y notificación de ganador de envid.
     * V: Mensaje que indica que vuelvo a envidar (cuando mi rival me ha envidado).
     * X: Mensaje de que NO quiero envid o truc (Envid -> 1, Truc -> 2).
     * F: Mensaje de la falta del envid.
     */

    public void enviarValorCarta(byte[] messageCarta){
        byte[] message = messageCarta;
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, message,
                        mRoomId, p.getParticipantId());
            }
        }
    }
    public void enviarMensajeRonda(){
        byte[] messageRonda = ("R " + String.valueOf(ronda)).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageRonda,
                        mRoomId, p.getParticipantId());
            }
        }
    }
    public void enviarMensajeTurno(){
        byte[] messageTurno = turno.getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageTurno,
                        mRoomId, p.getParticipantId());
            }
        }
    }
    public void enviarMensajeHasPerdido(){
        byte[] messageGanadorPartida = "W".getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageGanadorPartida,
                        mRoomId, p.getParticipantId());
                Log.d("LLLLLLL", "He ganado la ronda, y la partida");
            }
        }
    }
    public void enviarMensajeHayEmpate(){
        byte[] messageEmpatePrimero = ("E").getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageEmpatePrimero,
                        mRoomId, p.getParticipantId());
            }
        }
    }
    public void enviarMensajeSumaRonda(){
        byte[] messageGanadorRonda = "G".getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageGanadorRonda,
                        mRoomId, p.getParticipantId());
                Log.d("LLLLLLL", "No tiro primero, envio mensaje G comunicando que sume rondasGanadas");
            }
        }
    }

    public void enviarMensajeRepartir(){
        byte[] messageRepartir = ("S"+" "+sCartasJ2).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageRepartir,
                        mRoomId, p.getParticipantId());

            }
        }
    }
    public void enviarMensajeMano(){
        byte[] messageMano = ("M "+mano).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageMano,
                        mRoomId, p.getParticipantId());
            }
        }
    }
    public void enviarMensajeEnvid(int envid){
        byte[] messageEnvid = ("N "+envid).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageEnvid,
                        mRoomId, p.getParticipantId());
            }
        }
    }
    public void enviarMensajeHayEnvidAndGanador(String ganador, int caso){
        byte[] messageEnvid = ("K "+ganador+" "+miEnvid+" "+caso).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageEnvid,
                        mRoomId, p.getParticipantId());
            }
        }
    }
    public void enviarMensajeVuelvoAEnvidar(){
        byte[] messageVuelvo = ("V "+miEnvid).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageVuelvo,
                        mRoomId, p.getParticipantId());
            }
        }
    }
    public void enviarMensajeNoQuiero(int caso){
        byte[] messageNoQuiero = ("X "+caso).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageNoQuiero,
                        mRoomId, p.getParticipantId());
            }
        }
    }
    public void enviarMensajeLaFalta(){
        byte[] messageFalta = ("F "+miEnvid).getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageFalta,
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
            if(buf[0] == '$' && tvMesaRival1.getVisibility()==View.INVISIBLE){
                String sBuf = new String(buf, "UTF-8");
                String arrayBuf[] = sBuf.split(" ");
                int valor = Integer.parseInt(arrayBuf[2]);
                if(hayEmpate){
                    valorEmpate = valor;
                } else valor1 = valor;
                String palo = arrayBuf[1];
                //String arrayBuf2[] = arrayBuf[0].split("$");
                String numeroCarta = arrayBuf[0].substring(1);
                Carta newCarta = new Carta(numeroCarta,palo,arrayBuf[2]);
                //tvMesaRival1.setText(numeroCarta+" de "+palo);
                asignarImagenCarta(newCarta,tvMesaRival1);
                tvMesaRival1.setVisibility(View.VISIBLE);

            } else if(buf[0] == '$' && tvMesaRival2.getVisibility()==View.INVISIBLE){
                String sBuf = new String(buf, "UTF-8");
                String arrayBuf[] = sBuf.split(" ");
                int valor = Integer.parseInt(arrayBuf[2]);
                if(hayEmpate){
                    valorEmpate = valor;
                } else valor2 = valor;
                String palo = arrayBuf[1];
                //String arrayBuf2[] = arrayBuf[0].split("$");
                String numeroCarta = arrayBuf[0].substring(1);
                Carta newCarta = new Carta(numeroCarta,palo,arrayBuf[2]);
                //tvMesaRival2.setText(numeroCarta+" de "+palo);
                asignarImagenCarta(newCarta,tvMesaRival2);
                tvMesaRival2.setVisibility(View.VISIBLE);

            } else if(buf[0] == '$' && tvMesaRival3.getVisibility()==View.INVISIBLE){
                String sBuf = new String(buf, "UTF-8");
                String arrayBuf[] = sBuf.split(" ");
                int valor = Integer.parseInt(arrayBuf[2]);
                if(hayEmpate){
                    valorEmpate = valor;
                } else valor3 = valor;
                String palo = arrayBuf[1];
                //String arrayBuf2[] = arrayBuf[0].split("$");
                String numeroCarta = arrayBuf[0].substring(1);
                Carta newCarta = new Carta(numeroCarta,palo,arrayBuf[2]);
                asignarImagenCarta(newCarta,tvMesaRival3);
                //tvMesaRival3.setText(numeroCarta+" de "+palo);
                tvMesaRival3.setVisibility(View.VISIBLE);

            } else if(buf[0] == 'R'){
                /*String sBuf = new String(buf, "UTF-8");
                String arrayBuf[] = sBuf.split(" ");
                String ronda = arrayBuf[1];
                this.ronda = Integer.parseInt(ronda);*/
                actualizaRonda();

            } else if(buf[0] == 'G'){
                //Si ha habido empate
                Log.d("TTTTTT","SUmo mis rondas ganadas");
                if(hayEmpate){
                    mostrarResultadosGanadorMano();
                }else{
                //Sumo rondas ganadas
                if(ronda == 1){
                    ganadorRonda1 = mMyId;
                }
                misRondasGanadas++;
                actualizaRonda();
                //Si llego a 2 he ganado
                    if (misRondasGanadas == 2) {
                        mostrarResultadosGanadorMano();
                        }
                }

            }else if(buf[0] == 'W'){
                //Caso en que pierdo
                mostrarResultadosPerdedorMano();

            }else if(buf[0] == 'E') {
                //Mensaje que actualiza si hay empate en la primera ronda
                if (ronda == 1) {
                    showBasicAlert("Empate en la primera ronda!", "La carta que eljas será mostrada arriba");
                    hayEmpate = true;
                } else hayEmpate = true;

            }else if(buf[0] == 'S'){
                resetAll();
                desbloquearCartas();
                Log.d("TTTTTT", "Recibo las cartas");
                String sBuf = new String(buf, "UTF-8");
                Log.d("TTTTTT", sBuf.toString());
                String[] arrayCartasJ2 = sBuf.split(" ");
                Log.d("TTTTTT", arrayCartasJ2.toString());
                numCarta[0] = Integer.parseInt(arrayCartasJ2[1]);
                numCarta[1] = Integer.parseInt(arrayCartasJ2[2]);
                numCarta[2] = Integer.parseInt(arrayCartasJ2[3]);
                repartir(numCarta);
                carta1 = new Carta(manoJugador.get(0).getNumero(), manoJugador.get(0).getPalo(), manoJugador.get(0).getValor());
                carta2 = new Carta(manoJugador.get(1).getNumero(), manoJugador.get(1).getPalo(), manoJugador.get(1).getValor());
                carta3 = new Carta(manoJugador.get(2).getNumero(), manoJugador.get(2).getPalo(), manoJugador.get(2).getValor());
                cerrarDialogoAndStart(6000);
                Log.d("TTTTTT", "Start game");

            }else if(buf[0] == 'M') {
                Log.d("TTTTTT", "Recibo el mensaje con la nueva mano");
                String aux = new String(buf, "UTF-8");
                String nuevaMano[] = aux.split(" ");
                mano = nuevaMano[1];
                Log.d("TTTTTT","Mano: " +mano+" ID: "+mMyId);
                turno = mano;
                inicializarMano();


            }
            else if(buf[0] == 'N') {
                String aux = new String(buf, "UTF-8");
                String suEnvid[] = aux.split(" ");
                envidOtro = Integer.parseInt(suEnvid[1]);
                showSingleChoiceAlert("Tu rival ha envidado", R.array.envid);
                fabEnvid.setEnabled(false);



            }
            else if(buf[0] == 'K') {
                menu.setClickable(true);
                String aux = new String(buf, "UTF-8");
                String ganador[] = aux.split(" ");
                hayEnvid = true;
                ganadorEnvid = ganador[1];
                envidOtro = Integer.parseInt(ganador[2]);
                int caso = Integer.parseInt(ganador[3]);
                switch (caso){
                    case 1:
                        textoAccion.setText("Quiero el envid");
                        cerrarAccion(3000);
                        break;
                    case 2:
                        textoAccion.setText("Quiero el vuelvo");
                        cerrarAccion(3000);
                        break;
                    case 3:
                        textoAccion.setText("Quiero la falta");
                        cerrarAccion(3000);
                        break;
                }


                if(mMyId.equals(turno)){
                    desbloquearCartas();
                }


            }
            else if(buf[0] == 'V') {
                String aux = new String(buf, "UTF-8");
                String otro[] = aux.split(" ");
                envidOtro = Integer.parseInt(otro[1]);
                showSingleChoiceAlertVuelvo("Tu rival ha vuelto a envidar", R.array.envid2);


            }
            else if(buf[0] == 'X') {
                String aux = new String(buf, "UTF-8");
                String otro[] = aux.split(" ");
                int caso = Integer.parseInt(otro[1]);
                switch (caso){
                    case 1:
                        textoAccion.setText("No quiero el envid");
                        cerrarAccion(3000);
                        break;
                    case 2:
                        textoAccion.setText("No quiero el vuelvo");
                        cerrarAccion(3000);
                        break;
                    case 3:
                        textoAccion.setText("No quiero la falta");
                        cerrarAccion(3000);
                        break;
                }
                if(mMyId.equals(turno)){
                    desbloquearCartas();
                }

            }
            else if(buf[0] == 'F') {
                String aux = new String(buf, "UTF-8");
                String otro[] = aux.split(" ");
                envidOtro = Integer.parseInt(otro[1]);
                showSingleChoiceAlertFalta("Tu rival ha envidado la falta", R.array.envid3);


            }
            else {
                String turnoNuevo = new String(buf, "UTF-8");
                turno = turnoNuevo;
                desbloquearCartas();
                if(mMyId.equals(turno) && misRondasGanadas<2){
                    if(ronda == 1 && !hayEnvid)fabEnvid.setEnabled(true);
                    if(ronda > 1 && !hayEnvid)fabEnvid.setEnabled(false);

                    Toast.makeText(getApplicationContext(),"Es tu turno", Toast.LENGTH_SHORT).show();
                }

                if(!mMyId.equals(turno) && misRondasGanadas<2) {
                    tvJugador1.setEnabled(false);
                    tvJugador2.setEnabled(false);
                    tvJugador3.setEnabled(false);
                    fabEnvid.setEnabled(false);

                    Toast.makeText(getApplicationContext(), "Esperando al Jugador", Toast.LENGTH_SHORT).show();
                    Log.d("LLLLLLL", "Me han comunicado cambio de turno");

                }
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
            R.id.screen_wait
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
        }
        else {
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


    public List<Carta> crearBaraja(){
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
                    //Por cada question, la añadimos a la lista
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

        if (mMyId.equals(mano)){

            list[0] = (int) (Math.random() * 22);
            int aux = (int) (Math.random() * 22);
            while(list[0] == aux){
                aux = (int) (Math.random() * 22);
            }
            list[1] = aux;
            int aux2 = (int) (Math.random() * 22);
            while(list[0] == aux2 || list[1] == aux2){
                aux2 = (int) Math.floor(Math.random() * 21);
            }
            list[2] = aux2;
            int aux3 = (int) (Math.random() * 22);
            while(list[0] == aux3 || list[1] == aux3 || list[2] == aux3){
                aux3 = (int) Math.floor(Math.random() * 21);
            }
            list2[0] = aux3;
            int aux4 = (int) (Math.random() * 22);
            while(list[0] == aux4 || list[1] == aux4 || list[2] == aux4 || list2[0] == aux4){
                aux4 = (int) Math.floor(Math.random() * 21);
            }
            list2[1] = aux4;
            int aux5 = (int) (Math.random() * 22);
            while(list[0] == aux5 || list[1] == aux5 || list[2] == aux5 || list2[0] == aux5 || list2[1] == aux5){
                aux5 = (int) (Math.random() * 22);
            }
            list2[2] = aux5;

            sCartasJ2 = list2[0]+" "+list2[1]+" "+list2[2];

        }
        return list;
    }

    public void repartir(int[] numeros){

        baraja = crearBaraja();
        manoJugador.add(0, baraja.get(numeros[0]));
        manoJugador.add(1, baraja.get(numeros[1]));
        manoJugador.add(2, baraja.get(numeros[2]));

    }

    public void inicializarMano(){
        //Preparando la partida
        resetAll();
        desbloquearCartas();
        //Si soy mano reparto
        if(mMyId.equals(mano)){
            repartir(crearAleatorio());
            carta1 = new Carta(manoJugador.get(0).getNumero(), manoJugador.get(0).getPalo(), manoJugador.get(0).getValor());
            carta2 = new Carta(manoJugador.get(1).getNumero(), manoJugador.get(1).getPalo(), manoJugador.get(1).getValor());
            carta3 = new Carta(manoJugador.get(2).getNumero(), manoJugador.get(2).getPalo(), manoJugador.get(2).getValor());
            //Mando las cartas
            enviarMensajeRepartir();
            cerrarDialogoAndStart(6000);
            Log.d("TTTTTT", "Envio las cartas y start game");
        }
        //Sino soy mano, espero las cartas

    }

    public void cambiarMano(){
        if(mano.equals(idJugador1)){
            mano = idJugador2;
        }else if(mano.equals(idJugador2)){
            mano = idJugador1;
        }

        turno = mano;
        Log.d("TTTTTT", "Cambio mano");
    }
    public void repartirTrasMano(){
        Log.d("TTTTTT", "Mano: " + mano + " ID: " + mMyId);
        if(mMyId.equals(mano)){
            cambiarMano();
            enviarMensajeMano();
            Log.d("TTTTTT", "Mano: " + mano + " ID: " + mMyId);
            Log.d("TTTTTT", "EnviarMensajeMano");
        }
    }

    public void asignarImagenCarta(Carta carta, ImageView view){
        String sCarta = carta.getNumero() + carta.getPalo();
        switch(sCarta){
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
    void ponerCartaSobreMesa(View view){

        view.setVisibility(View.INVISIBLE);

        if(view.equals(tvJugador1)){  aux=carta1; miValor = Integer.parseInt(carta1.getValor());}
        if(view.equals(tvJugador2)){  aux=carta2; miValor = Integer.parseInt(carta2.getValor());}
        if(view.equals(tvJugador3)){  aux=carta3; miValor = Integer.parseInt(carta3.getValor());}

        if(tvCartaMesa1.getVisibility() == View.INVISIBLE){
            //tvCartaMesa1.setText(aux.getText().toString());
            asignarImagenCarta(aux,tvCartaMesa1);
            tvCartaMesa1.setVisibility(View.VISIBLE);
        }
        else if(tvCartaMesa2.getVisibility() == View.INVISIBLE){
            //tvCartaMesa2.setText(aux.getText().toString());
            asignarImagenCarta(aux,tvCartaMesa2);
            tvCartaMesa2.setVisibility(View.VISIBLE);
        }
        else if(tvCartaMesa3.getVisibility() == View.INVISIBLE){
            //tvCartaMesa3.setText(aux.getText().toString());
            asignarImagenCarta(aux,tvCartaMesa3);
            tvCartaMesa3.setVisibility(View.VISIBLE);
        }
    }
}
