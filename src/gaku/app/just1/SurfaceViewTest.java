package gaku.app.just1;

import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
 
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
 
public class SurfaceViewTest extends SurfaceView implements SurfaceHolder.Callback {
    // このサンプルでは実行間隔を 0.016秒間隔（約 60 fps に相当）に設定してみた
    private static final long INTERVAL_PERIOD = 16;
    private ScheduledExecutorService scheduledExecutorService;
    private static final float FONT_SIZE = 24f;
    private Paint paintCircle, paintFps;
    private float x, y, r;
    private ArrayList<Long> intervalTime = new ArrayList<Long>(20);
 
    public SurfaceViewTest(Context context) {
        super(context);
        init();
    }
 
    public SurfaceViewTest(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
 
    public SurfaceViewTest(Context context, AttributeSet attrs, int defStyle) {
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
        paintFps.setColor(Color.BLACK);
        paintFps.setAntiAlias(true);
    }
 
    // コールバック内容の定義 (1/3)
    @Override
    public void surfaceCreated(final SurfaceHolder surfaceHolder) {
        x = getWidth() / 2;
        y = getHeight() / 2;
 
        // SingleThreadScheduledExecutor による単一 Thread のインターバル実行
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // fps（実測値）の計測
                intervalTime.add(System.currentTimeMillis());
                float fps = 20000 / (intervalTime.get(19) - intervalTime.get(0));
                intervalTime.remove(0);
 
                // ロックした Canvas の取得
                Canvas canvas = surfaceHolder.lockCanvas();
                canvas.drawColor(Color.RED);
                r = r > 120 ? 10 : r + 3;
                canvas.drawCircle(x, y, r, paintCircle);
                canvas.drawText(String.format("%.1f fps", fps), 0, FONT_SIZE, paintFps);
                // ロックした Canvas の解放
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }, 100, INTERVAL_PERIOD, TimeUnit.MILLISECONDS);
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
 
    // タッチイベントに対応する処理
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            x = event.getX();
            y = event.getY();
        }
        return super.onTouchEvent(event);
    }
}