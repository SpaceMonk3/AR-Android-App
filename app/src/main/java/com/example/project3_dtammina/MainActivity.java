package com.example.project3_dtammina;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.collision.Ray;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.ux.TransformableNode;


import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Point point;
    private Scene scene;
    private boolean startTimer = true;
    private TextView cubesLeftTxt;
    private boolean gameRunning = true;
    private SoundPool sfx;
    private Button restart;
    private ModelRenderable bullet;
    private MediaPlayer mediaPlayer;
    private Camera camera;
    private int sound;
    private int cubesLeft = 15;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //trying to install ARCoreApk
        try {
            ArCoreApk.getInstance().requestInstall(this, /* userRequestedInstall= */ true);
        } catch (UnavailableDeviceNotCompatibleException e) {
            throw new RuntimeException(e);
        } catch (UnavailableUserDeclinedInstallationException e) {
            throw new RuntimeException(e);
        }


        Display display = getWindowManager().getDefaultDisplay();
        point = new Point();
        display.getRealSize(point);

        setContentView(R.layout.activity_main);

        //load SFX
        loadSFX();

        // Initialize MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true); // Set looping to true for continuous playback
        mediaPlayer.setVolume(0.5f, 0.5f);
        mediaPlayer.start(); // Start playing the background music

        //setting up UI and Fragment
        cubesLeftTxt = findViewById(R.id.cubesCount);
        CustomArFragment arFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

        //setting scene and camera
        scene = arFragment.getArSceneView().getScene();
        camera = scene.getCamera();

        //adding cubes and bullets/ balls
        addCubesToScene();
        buildBulletModel();

        //setting up restart button and method
        restart = findViewById(R.id.restart);
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //restart app
                playAgain();
            }
        });

        Button shoot = findViewById(R.id.shootButton);

        shoot.setOnClickListener(v -> {
            if (startTimer) {
                startTimer();
                startTimer = false;
            }
            shoot();
        });

//        // thread to continuously update cube positions
//        new Thread(() -> {
//            while (gameRunning) {
//                try {
//                    Thread.sleep(100); // Adjust the delay based on your desired motion speed
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                runOnUiThread(() -> {
//                    updateCubesPosition();
//                });
//            }
//        }).start();

    }

//    private void updateCubesPosition() {
//        for (Node node : scene.getChildren()) {
//            if (node instanceof TransformableNode) {
//                moveNode((TransformableNode) node);
//            }
//        }
//    }

//    private void moveNode(TransformableNode node) {
//        // You can customize the motion logic here
//        // For example, you can use translation, rotation, etc.
//        // For simplicity, let's make the cubes move forward along the z-axis
//        Vector3 currentPosition = node.getWorldPosition();
//        Vector3 newPosition = new Vector3(currentPosition.x, currentPosition.y, currentPosition.z - 100f);
//        node.setWorldPosition(newPosition);
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the background music when the activity is destroyed
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    //Loading and creating SoundPool for sfx
    private void loadSFX() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build();

        sfx = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        sound = sfx.load(this, R.raw.crash, 1);
    }

    private void shoot() {

        Ray ray = camera.screenPointToRay(point.x / 2f, point.y / 2f);
        Node node = new Node();
        node.setRenderable(bullet);
        scene.addChild(node);

        new Thread(() -> {
            for (int i = 0;i < 200;i++) {
                int finalI = i;
                runOnUiThread(() -> {

                    Vector3 vector3 = ray.getPoint(finalI * 0.1f);
                    node.setWorldPosition(vector3);

                    Node nodeInContact = scene.overlapTest(node);

                    if (nodeInContact != null) {
                        cubesLeft--;
                        cubesLeftTxt.setText("Boxes: " + cubesLeft);
                        scene.removeChild(nodeInContact);

                        mediaPlayer.setVolume(0.3f, 0.3f);
                        sfx.play(sound, 1.0f, 1.0f, 1, 0
                                , 1f);
                        mediaPlayer.setVolume(0.5f, 0.5f);
                    }
                });

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(() -> scene.removeChild(node));
        }).start();
    }

    private void startTimer() {
        TextView timer = findViewById(R.id.timerText);

        new Thread(() -> {
            int seconds = 0;
            while (cubesLeft > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                seconds++;

                int minutesPassed = seconds / 60;
                int secondsPassed = seconds % 60;

                runOnUiThread(() -> timer.setText(minutesPassed + "." + secondsPassed));

                if(minutesPassed == 0 && secondsPassed >= 30){
                    mediaPlayer.pause();
                    runOnUiThread(() -> dialogView("You Lose!", "Want to play again?", "No", "Yes"));
                    break;
                }
            }

            if(cubesLeft == 0) {
                mediaPlayer.pause();
                runOnUiThread(() -> dialogView("You Win!", "Want to play again?", "No", "Yes"));
            }
        }).start();

    }


    private void dialogView(String message, String Content, String negative, String positive ) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle( message )
                .setMessage(Content)
                .setNegativeButton(negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.cancel();
                        mediaPlayer.start();
                    }})
                .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        playAgain();
                    }
                }).show();
    }

    //method to reload app to play again
    public void playAgain(){
        //restart app to play again
        finish();
        startActivity(getIntent());

    }


    //building the bullet model aka make a sphere
    private void buildBulletModel() {
        Texture.builder().setSource(this, R.drawable.texture).build().thenAccept(texture -> {
                    MaterialFactory.makeOpaqueWithTexture(this, texture).thenAccept(material -> {
                                bullet = ShapeFactory.makeSphere(0.01f, new Vector3(0f, 0f, 0f), material);
                    });
        });
    }

    private void addCubesToScene() {
        for (int i = 0; i < 15; i++) {
            Node node = new Node();

            //creating a cube shape using the texture "cube"
            Texture.builder()
                    .setSource(this, R.drawable.cube)
                    .build()
                    .thenAccept(texture -> {
                        MaterialFactory.makeOpaqueWithTexture(this, texture)
                                .thenAccept(material -> {
                                    // Set color directly
                                    material.setFloat3(MaterialFactory.MATERIAL_COLOR, new Color(255.0f, 255.0f, 255.0f));
                                    ModelRenderable cubeRenderable = ShapeFactory
                                            .makeCube(new Vector3(0.2f, 0.2f, 0.2f), new Vector3(0f, 0f, 0f), material);
                                    node.setRenderable(cubeRenderable);
                                    scene.addChild(node);
                                });
                    });

            Random random = new Random();

            int x = random.nextInt(5);
            int z = random.nextInt(5);
            int y = random.nextInt(15);

            z = -z;

            //randomly generate negatives too
            if(random.nextInt(1) == 0){
                y = -y;
            }
            node.setWorldPosition(new Vector3(
                    (float) x,
                    (float) y/10,
                    (float) z
            ));
        }

    }

    //method for info button
    public void infoClick(View v){
        mediaPlayer.pause();
        dialogView("Welcome to Balls", "Welcome to Balls and Cubes. The goal of this game is to shoot all the funny little cubes around you within 30 seconds. If you don't... well you lose.", "Ok", "");
    }

}
