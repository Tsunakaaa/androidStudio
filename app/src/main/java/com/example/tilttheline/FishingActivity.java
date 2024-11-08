package com.example.tilttheline;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FishingActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ImageView fishingObject;
    private float posX = 0, posY = 0;

    private Handler handler;
    private Random random;


    private List<ImageView> allFish; // Déclaration de allFish comme variable d'instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing);

        // Initialisation du gestionnaire de capteurs
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // Initialisation de la vue de l'objet de pêche et des poissons
        fishingObject = findViewById(R.id.fishing_object);
        ImageView fish1 = findViewById(R.id.fish_1);
        ImageView fish2 = findViewById(R.id.fish_2);
        ImageView fish3 = findViewById(R.id.fish_3);
        ImageView fish4 = findViewById(R.id.fish_4);
        ImageView fish5 = findViewById(R.id.fish_5);
        Button fishButton = findViewById(R.id.fishButton);
        ImageButton catchButton = findViewById(R.id.catchButton);

        // Initialisation du handler et du générateur de nombres aléatoires
        handler = new Handler();
        random = new Random();

        // Créez et initialisez la liste allFish
        allFish = new ArrayList<>();
        allFish.add(fish1);
        allFish.add(fish2);
        allFish.add(fish3);
        allFish.add(fish4);
        allFish.add(fish5);

        // Initialisez les positions des poissons dans les limites de l'écran
        initializeFishPositions(allFish);

        // Démarrer le mouvement des poissons
        startFishMovement(allFish);

        // Action pour afficher l'objet de pêche
        fishButton.setOnClickListener(v ->
        {fishingObject.setVisibility(View.VISIBLE);
        catchButton.setVisibility(View.VISIBLE);
        catchButton.setClickable(true);
        fishButton.setClickable(false);
        fishButton.setVisibility(View.INVISIBLE);
        });

        catchButton.setOnClickListener(v ->
        {fishingObject.setVisibility(View.INVISIBLE);
            catchButton.setVisibility(View.INVISIBLE);
            catchButton.setClickable(false);
            fishButton.setClickable(true);
            fishButton.setVisibility(View.VISIBLE);
        });
    }

    private void initializeFishPositions(List<ImageView> allFish) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        float centerX = screenWidth / 2f;
        float centerY = screenHeight / 2f;

        for (ImageView fish : allFish) {
            // Initialisation des poissons à des positions aléatoires dans les limites de l'écran
            float randomX = random.nextInt(screenWidth - fish.getWidth());
            float randomY = random.nextInt((int) (screenHeight * 0.66) - fish.getHeight());

            fish.setX(randomX);
            fish.setY(randomY);

            // Calcul de l'angle pour se diriger vers le centre de l'écran
            float deltaX = centerX - randomX;
            float deltaY = centerY - randomY;

            // Calcul de l'angle en radians puis conversion en degrés
            float angle = (float) Math.toDegrees(Math.atan2(deltaY, deltaX));

            // Appliquez la rotation du poisson vers le centre
            fish.setRotation(angle);
        }
    }

    private void startFishMovement(List<ImageView> allFish) {
        // Pour chaque poisson, planifier un déplacement indépendant avec un délai aléatoire
        for (ImageView fish : allFish) {
            Runnable moveFishRunnable = new Runnable() {
                @Override
                public void run() {
                    moveFish(fish);  // Déplacer le poisson
                    // Planifier le prochain mouvement du poisson avec un délai aléatoire
                    handler.postDelayed(this, random.nextInt(1000) + 1000); // Délai aléatoire entre 1 et 2 secondes
                }
            };
            handler.postDelayed(moveFishRunnable, random.nextInt(1000) + 1000); // Débuter le mouvement avec un délai initial aléatoire
        }
    }

    private void moveFish(ImageView fish) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        // Calculer un angle de mouvement aléatoire
        float angle = random.nextInt(360); // Angle aléatoire entre 0 et 360 degrés

        // Restreindre l'angle entre -90 et 90 degrés
        angle = angle > 180 ? angle - 360 : angle; // Si l'angle est supérieur à 180, ajustez-le dans la plage [-90, 90]

        // Appliquer un déplacement basé sur cet angle, avancer d'1/5 de la largeur de l'écran
        float deltaMoveX = (float) (screenWidth / 5 * Math.cos(Math.toRadians(angle))); // Avancer dans une direction aléatoire
        float deltaMoveY = (float) (screenHeight / 5 * Math.sin(Math.toRadians(angle)));

        // Nouvelle position du poisson
        float newX = fish.getX() + deltaMoveX;
        float newY = fish.getY() + deltaMoveY;

        // Si le poisson risque de sortir de l'écran, se déplacer vers un autre poisson
        if (!isFishInBounds(newX, newY, fish, screenWidth, screenHeight)) {
            // Choisir un poisson aléatoire parmi les autres poissons
            ImageView targetFish = allFish.get(random.nextInt(allFish.size()));

            // Calculer la direction vers le poisson cible
            float targetX = targetFish.getX();
            float targetY = targetFish.getY();

            // Calculer l'angle vers le poisson cible
            float deltaX = targetX - fish.getX();
            float deltaY = targetY - fish.getY();
            float angleToTarget = (float) Math.toDegrees(Math.atan2(deltaY, deltaX));

            // Calculer la position intermédiaire à mi-chemin vers le poisson cible
            float midX = fish.getX() + (deltaX / 2);  // Position intermédiaire en X
            float midY = fish.getY() + (deltaY / 2);  // Position intermédiaire en Y

            // Déplacer le poisson à mi-chemin vers le poisson cible
            fish.animate()
                    .x(midX)
                    .y(midY)
                    .setDuration(2000) // Durée du déplacement
                    .start();

            // Appliquer la rotation vers le poisson cible
            fish.animate()
                    .rotation(angleToTarget)
                    .setDuration(500) // Durée de la rotation vers la cible
                    .start();
        } else {
            // Si le mouvement est valide (le poisson reste dans les limites), appliquer le déplacement
            fish.animate()
                    .x(newX)
                    .y(newY)
                    .setDuration(2000) // Durée du déplacement
                    .start();

            // Calcul de l'angle de rotation
            float newAngle = angle; // Utiliser l'angle restreint

            // Appliquer l'animation de rotation en 0.5 seconde
            fish.animate()
                    .rotation(newAngle) // Tourner vers la nouvelle direction
                    .setDuration(500)   // Durée de la rotation
                    .start();
        }
    }




    // Fonction pour vérifier si l'objet de pêche est sur le poisson
    private boolean isFishingObjectOnFish(ImageView fish) {
        int fishLeft = (int) fish.getX();
        int fishRight = fishLeft + fish.getWidth();
        int fishTop = (int) fish.getY();
        int fishBottom = fishTop + fish.getHeight();

        int fishingObjectLeft = (int) fishingObject.getX();
        int fishingObjectRight = fishingObjectLeft + fishingObject.getWidth();
        int fishingObjectTop = (int) fishingObject.getY();
        int fishingObjectBottom = fishingObjectTop + fishingObject.getHeight();

        // Vérifie si l'objet de pêche entre en collision avec le poisson
        return !(fishingObjectRight < fishLeft || fishingObjectLeft > fishRight ||
                fishingObjectBottom < fishTop || fishingObjectTop > fishBottom);
    }

    private boolean isFishInBounds(float x, float y, ImageView fish, int screenWidth, int screenHeight) {
        // Vérifie si la nouvelle position est dans les limites de l'écran
        return x >= 0 && x + fish.getWidth() <= screenWidth &&
                y >= 0 && y + fish.getHeight() <= screenHeight * 0.75; // Limite Y à 75% de la hauteur de l'écran
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            posX = event.values[0];
            posY = event.values[1];

            // Obtenez la largeur et la hauteur de l'écran
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;

            // Récupérez les dimensions de l'objet de pêche (fishingObject)
            int fishingObjectWidth = fishingObject.getWidth();
            int fishingObjectHeight = fishingObject.getHeight();

            // Définir les limites de la zone pour l'objet de pêche (comme pour les poissons)
            int zoneLeft = 0;
            int zoneRight = screenWidth;
            int zoneTop = 0;
            int zoneBottom = (int) (0.66 * screenHeight); // 75% de la hauteur

            // Calculer les nouvelles positions avec les valeurs d'accéléromètre
            float newX = fishingObject.getTranslationX() - posX;
            float newY = fishingObject.getTranslationY() + posY;

            // Limiter la position X pour que l'objet ne dépasse pas les bords de l'écran
            if (newX < zoneLeft) {
                newX = zoneLeft; // Ne peut pas être plus à gauche que 0
            } else if (newX + fishingObjectWidth > zoneRight) {
                newX = zoneRight - fishingObjectWidth; // Ne peut pas être plus à droite que la largeur de l'écran
            }

            // Limiter la position Y pour que l'objet ne dépasse pas les bords de l'écran
            if (newY < zoneTop) {
                newY = zoneTop; // Ne peut pas être plus haut que 0
            } else if (newY + fishingObjectHeight > zoneBottom) {
                newY = zoneBottom - fishingObjectHeight; // Ne peut pas être plus bas que 66% de la hauteur de l'écran
            }

            // Mettre à jour la position de l'objet de pêche (fishingObject)
            fishingObject.setTranslationX(newX);
            fishingObject.setTranslationY(newY);

            // Vérifier en continu si l'objet de pêche est sur l'un des poissons
            for (ImageView fish : allFish) {
                if (isFishingObjectOnFish(fish)) {
                    // Si l'objet de pêche est sur un poisson, changer l'image du poisson
                    fish.setImageResource(R.drawable.fish_image_g);
                }// Remplacer par l'image appropriée du poisson
                if (!isFishingObjectOnFish(fish)) {
                    // Si l'objet de pêche n'est plus sur un poisson, changer l'image du poisson
                    fish.setImageResource(R.drawable.fish_image); // Remplacer par l'image appropriée du poisson
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Non utilisé
    }
}
