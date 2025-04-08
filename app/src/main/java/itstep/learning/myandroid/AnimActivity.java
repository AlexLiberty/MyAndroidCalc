package itstep.learning.myandroid;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AnimActivity extends AppCompatActivity {

    Animation alphaAnimation;
    Animation rotateAnimation;
    Animation rotate2Animation;

    Animation scaleAnimation;

    Animation bellAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_anim);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        alphaAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_alpha);
        findViewById(R.id.anim_v_alpha).setOnClickListener(
                v->v.startAnimation(alphaAnimation)
        );

        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);
        findViewById(R.id.anim_v_rotate).setOnClickListener(
                v->v.startAnimation(rotateAnimation)
        );

        rotate2Animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate2);
        findViewById(R.id.anim_v_rotate2).setOnClickListener(
                v->v.startAnimation(rotate2Animation)
        );

        scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_scale);
        findViewById(R.id.anim_v_scale).setOnClickListener(
                v->v.startAnimation(scaleAnimation)
        );

        bellAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_bell_rotate);
        findViewById(R.id.anim_v_dz).setOnClickListener(
                v -> v.startAnimation(bellAnimation)
        );
    }
}