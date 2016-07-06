package cmu.xprize.ak_component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by jacky on 2016/6/30.
 */

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread thread;
    private Background bg;
    private Player player;
    private ArrayList<RoadTree> rightRoadTrees;
    private ArrayList<RoadTree> leftRoadTrees;
    private ArrayList<QuestionBoard> questionBoards;
    private long treeTime1;
    private long treeTime2;
    private long questionTime;
    private int boardCount;

    protected final static int WIDTH = 960, HEIGHT = 600;

    protected static int MOVESPEED = 5;

    public GamePanel(Context context) {
        super(context);

        getHolder().addCallback(this);
        thread = new GameThread(this, getHolder());

        rightRoadTrees = new ArrayList<>();
        leftRoadTrees = new ArrayList<>();

        questionBoards = new ArrayList<>();

        setFocusable(true);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN){
//            if(!player.getPlaying())
//            {
//                player.setPlaying(true);
//            }
//            else
//            {
//                player.setUp(true);
//            }
            player.onTouchEvent(event);
            return true;
        }
        if(event.getAction()==MotionEvent.ACTION_UP)
        {
            return true;
        }

        return super.onTouchEvent(event);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        System.out.println("start!");

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.landscape_scratch3);
        bg = new Background(bitmap);
        bg.setVector(-3);

        Bitmap ori = BitmapFactory.decodeResource(getResources(), R.drawable.car_rear);
        player = new Player(Bitmap.createScaledBitmap(ori, 75, 75, false), 75, 75);
        player.rearNum = new Random().nextInt(100);

        treeTime1 = treeTime2 = questionTime = System.nanoTime();

        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while(retry){
            try {
                thread.setRunning(false);
                thread.join();
                retry = false;
            }catch (Exception e){
                e.printStackTrace();
            }
            System.out.println("end!");
        }

    }

    @Override
    public void draw(Canvas canvas) {
        final float scaleFactorX = getWidth() * 1.0f / WIDTH;
        final float scaleFactorY = getHeight() * 1.0f / HEIGHT;
        if(canvas!=null) {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            player.draw(canvas);

            Paint paint = new Paint();
            paint.setTextSize(40);
            paint.setColor(Color.BLACK);
            canvas.drawText("Score: " + player.score,
                50, 500, paint);

            for(RoadTree roadTree : rightRoadTrees) {
                roadTree.draw(canvas);
            }

            for(RoadTree roadTree : leftRoadTrees) {
                roadTree.draw(canvas);
            }

            for(QuestionBoard questionBoard : questionBoards) {
                questionBoard.draw(canvas);
            }


            canvas.restoreToCount(savedState);
        }
    }

    public void update(){
        bg.update();
        player.update();

        long elapse1 = (System.nanoTime() - treeTime1) / 1000000;
        long elapse2 = (System.nanoTime() - treeTime2) / 1000000;
        long elapse = (System.nanoTime() - questionTime) / 1000000;



        if(elapse1 > 1300) {
            Bitmap oriTree = BitmapFactory.decodeResource(getResources(), R.drawable.tree);
            rightRoadTrees.add(new RoadTree(
                    Bitmap.createScaledBitmap(oriTree, 50, 100, false),
                    580, 135,
                    (int)(MOVESPEED / 2), MOVESPEED,
                    5, 10
            ));
            treeTime1 = System.nanoTime();

        }

        if(elapse2 > 2000) {
            Bitmap oriTree = BitmapFactory.decodeResource(getResources(), R.drawable.tree);
            leftRoadTrees.add(new RoadTree(
                    Bitmap.createScaledBitmap(oriTree, 50, 100, false),
                    300, 135,
                    -(int) (MOVESPEED / 2), MOVESPEED,
                    5, 10));
            treeTime2 = System.nanoTime();
        }

        for(int i = 0; i < rightRoadTrees.size(); i++) {
            rightRoadTrees.get(i).update();
            if(rightRoadTrees.get(i).y > HEIGHT) {
                rightRoadTrees.remove(i);
            }
        }

        for(int i = 0; i < leftRoadTrees.size(); i++) {
            leftRoadTrees.get(i).update();
            if(leftRoadTrees.get(i).y > HEIGHT) {
                leftRoadTrees.remove(i);
            }
        }

        if(elapse > 5000) {
            QuestionBoard questionBoard = new QuestionBoard(430, 100, 90, 30);
            Random r = new Random();
            int left = r.nextInt(50);
            int right = r.nextInt(50) + left + 1;
            questionBoard.leftNum = left;
            questionBoard.rightNum = right;
            questionBoards.add(questionBoard);
            questionTime = System.nanoTime();
        }

        for(int i = 0; i < questionBoards.size(); i++) {
            questionBoards.get(i).update();
            if(questionBoards.get(i).getRectangle().intersect(player.getRectangle())){
                if(judge(questionBoards.get(i))){
                    player.score += 1;
                }else{
                    player.score -= 1;
                }
                questionBoards.remove(i);
                System.out.println(player.score);
                boardCount++;
            }
        }

        if(boardCount == 4){
            player.rearNum = new Random().nextInt(100);
            boardCount = 0;
            MOVESPEED++;
        }

    }

    private boolean judge(QuestionBoard questionBoard){
        if(player.rearNum < questionBoard.leftNum && player.getLane() == Player.Lane.LEFT ||
                player.rearNum > questionBoard.rightNum && player.getLane() == Player.Lane.RIGHT ||
                (player.rearNum < questionBoard.rightNum && player.rearNum > questionBoard.leftNum
                && player.getLane() == Player.Lane.MID)){
            return true;
        }
        return false;
    }
}
