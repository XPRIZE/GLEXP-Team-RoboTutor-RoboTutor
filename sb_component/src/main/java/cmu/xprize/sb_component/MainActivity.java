package cmu.xprize.sb_component;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button);
        Button button2 = (Button) findViewById(R.id.button2);

        final CSb_Scoreboard scoreboard = (CSb_Scoreboard) findViewById(R.id.test);
//        final CSb_Lollipop lollipop = (CSb_Lollipop) findViewById(R.id.lollipop);
//
//
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scoreboard.increase();
//                lollipop.animateToCircle();
            }
        });
//
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scoreboard.decrease();
//                lollipop.animateToStick();
            }
        });
    }
}
