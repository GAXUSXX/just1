package gaku.app.just1;

import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class just1Surface extends SurfaceView implements SurfaceHolder.Callback {
    // このサンプルでは実行間隔を 0.016秒間隔（約 60 fps に相当）に設定してみた
    //private static final long INTERVAL_PERIOD = 16;
	private static final long INTERVAL_PERIOD = 50;
    private ScheduledExecutorService scheduledExecutorService;
    private static final float FONT_SIZE = 54f;
    private Paint paintCircle, paintFps;
    private float x, y, r;
    private ArrayList<Long> intervalTime = new ArrayList<Long>(20);
    private float touchX = 0;
    private float touchY = 0;
    private int itemFlickFlag = 0;
    private int FlickFlag = 0;
    private float SetX = 0;
    private float SetY = 0;
    private int xFlickFlag = 0;
    private int yFlickFlag = 0;
    private int countnum = 100;
    private int downFlag = 0;
    private long pressTime = 0;
    private int banFlag = 0;
    private int clearFlag = 0;
    private int countStartFlag = 0;
    private float countTime = 0;
    private int[] resource = new int [101];

    private SurfaceHolder holder;
    //画像読み込み
    Resources res = this.getContext().getResources();
    //Bitmap prin = BitmapFactory.decodeResource(res, R.drawable.prin);
    //Bitmap prin2 = BitmapFactory.decodeResource(res, R.drawable.prin2);
    //Bitmap sara = BitmapFactory.decodeResource(res, R.drawable.sara);

    public just1Surface(Context context) {
        super(context);
        init();
    }

    public just1Surface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public just1Surface(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /*
     * コンストラクター引数が1〜3個の場合のすべてで共通となる初期化ルーチン
     */
    private void init() {
        /*
         * このクラス（SurfaceViewTest）では、SurfaceView の派生クラスを定義するだけでなく、
         * SurfaceHolder.Callback インターフェイスのコールバックも実装（implement）しているが、
         * SurfaceHolder であるこのクラスのインスタンスの呼び出し元のアクティビティ（通常はMainActivity）
         * に対して、関連するコールバック（surfaceChanged, surfaceCreated, surfaceDestroyed）
         * の呼び出し先がこのクラスのインスタンス（this）であることを呼出元アクティビティに登録する。
         */
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        // fps 計測用の設定値の初期化
        for (int i = 0; i < 19; i++) {
            intervalTime.add(System.currentTimeMillis());
        }

        // 描画に関する各種設定
        paintCircle = new Paint();
        paintCircle.setStyle(Style.FILL);
        paintCircle.setColor(Color.WHITE);
        paintCircle.setAntiAlias(false);
        paintFps = new Paint();
        paintFps.setTypeface(Typeface.DEFAULT);
        paintFps.setTextSize(FONT_SIZE);
        paintFps.setColor(Color.WHITE);
        paintFps.setAntiAlias(true);

        for(int i=0;i<101;i++){
        	resource[i] = getResources().getIdentifier("a" + String.valueOf(i), "drawable", "gaku.app.just1");
        }
    }

    // コールバック内容の定義 (1/3)
    @Override
    public void surfaceCreated(final SurfaceHolder surfaceHolder) {
        x = 0;
        y = (float) (getHeight() / 3);
        DrawSurface(surfaceHolder);
    }

    // コールバック内容の定義 (2/3)
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
    }

    // コールバック内容の定義 (3/3)
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        // SingleThreadScheduledExecutor を停止する
        scheduledExecutorService.shutdown();

        // 呼出元アクティビティ側のこのクラスのインスタンスに対するコールバック登録を解除する
        surfaceHolder.removeCallback(this);
    }

    public void DrawSurface(final SurfaceHolder surfaceHolder){
    	// SingleThreadScheduledExecutor による単一 Thread のインターバル実行
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

            	if(downFlag == 1 && pressTime > 30){
            		countnum--;
            	}
            	pressTime++;

                // fps（実測値）の計測
                intervalTime.add(System.currentTimeMillis());
                float fps = 20000 / (intervalTime.get(19) - intervalTime.get(0));
                intervalTime.remove(0);

                // ロックした Canvas の取得
                Canvas canvas = surfaceHolder.lockCanvas();
                canvas.drawColor(Color.BLACK);
                r = r > 120 ? 10 : r + 3;

                //canvas.drawCircle(x, y, r, paintCircle);
                // 100x100にリサイズ
                int imageSize = getWidth()/4;

                canvas.drawText(String.format("%.1f fps", fps), 0, FONT_SIZE, paintFps);

                if(banFlag == 1 || countnum < 1 && pressTime >30 && clearFlag != 1){
                	canvas.drawText("BAN", 300, FONT_SIZE, paintFps);
            	}
                else if(countnum < 1){
                	canvas.drawText("Clear!!", 300, FONT_SIZE, paintFps);
                	float endTime = countTime/21;
                	canvas.drawText(String.valueOf(endTime), 100, FONT_SIZE+200, paintFps);
                	clearFlag = 1;
                }
                else{
                	Bitmap sara = BitmapFactory.decodeResource(res, resource[countnum]);
                	canvas.drawBitmap(sara, 100, 300, paintCircle);
                }
                if(countStartFlag == 1 && clearFlag != 1){
            		countTime++;
            	}
                // ロックした Canvas の解放
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }, 100, INTERVAL_PERIOD, TimeUnit.MILLISECONDS);
    }

    public int TouchFlag = 0;
    // タッチイベントに対応する処理
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
        	countStartFlag = 1;
        	touchX = event.getX();
        	touchY = event.getY();

            //x = event.getX();
            //y = event.getY();
            if(x > getWidth()/1.85 && event.getX() > getWidth()/2.15 && event.getX() < getWidth()/1.45 && event.getY() > getHeight()/3.35 && event.getY() < getHeight()/2.65){
	            //x = 0;
	            //y = (float) (getHeight() / 3);
            	Log.v("TOUCH","Flagon");
            	TouchFlag = 1;
            }
            Log.e("touch","down");
            if(downFlag != 2){
            	downFlag = 1;
            }
            break;
        case MotionEvent.ACTION_UP:
        	if(TouchFlag == 1){

        		Log.v("YPos",String.valueOf(event.getY() - touchY));
	        	if(event.getY() - touchY > getHeight()/15){
	        		y += getHeight()/7;
	        		itemFlickFlag = 1;
	        		yFlickFlag = 1;
	        	}
	        	else if(event.getX() - touchX > getWidth()/14){
	        		y += getHeight()/7;
	        		itemFlickFlag = 1;
	        		xFlickFlag = 1;
	        	}
        	}
        	if(downFlag != 2 && countnum < 1){
        		banFlag = 1;
        	}
        	Log.e("touch","up");
        	if(pressTime > 30 ){
	        	downFlag = 2;
	        	pressTime = 0;
        	}
        	if(downFlag == 2){
        		countnum--;
        	}
        	TouchFlag = 0;
        	break;
        }
        return true;
    }
}