package com.ozateck.darumaneko;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;
import java.lang.Math;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.MotionEvent;
import android.util.Log;

import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCLabel;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCSpriteSheet;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.sound.SoundEngine;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccColor3B;
import org.cocos2d.config.ccMacros;
import org.cocos2d.events.CCTouchDispatcher;
import org.cocos2d.opengl.CCGLSurfaceView;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.GearJoint;
import com.badlogic.gdx.physics.box2d.joints.GearJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;

public class GamePlayLayer extends CCLayer
					implements SensorEventListener{
	
	private static final String TAG = "myTag";
	
	private Context context;
	
	//mWorldで使用する画面の横サイズは一定(単位はメートル)
	//モニタの横サイズを基準にして、メートル単位で制御する。
	protected static final float WORLD_WIDTH_METER = 1.0f;
	
	//モニタサイズ
	private CGSize  dispSize;
	//1メートルにつき何ピクセルか
	private int     ptmRatio;
	//モニタの中心点
	private CGPoint cPoint;
	
	//物理空間
	private World mWorld;
	//重力の方向
	private Vector2 mGravity;

	//FPS
	private float FPS = 
			(float)CCDirector.sharedDirector().getAnimationInterval();
	private float rdelta = 0;
	
	//猫リスト、だるまリスト
	private CCSprite      bgSprite;
	private CCSprite      groundSprite;
	private List<Cat>     catList;
	private List<Daruma>  darumaList;
	
	//ボタン
	private NormaBtn btnS;
	private NormaBtn btnM;
	private NormaBtn btnL;
	
	private static final int TAG_NONE   = 0;
	private static final int TAG_SMALL  = 1;
	private static final int TAG_MIDDLE = 2;
	private static final int TAG_LARGE  = 3;
	private static final float sizeS = 0.06f;
	private static final float sizeM = 0.08f;
	private static final float sizeL = 0.10f;
	private static final int  pointS = 10;
	private static final int  pointM = 15;
	private static final int  pointL = 20;
	private int countS = 0;
	private int countM = 0;
	private int countL = 0;
	
	//スコアバー
	private ScoreBar scoreBar;
	private int score = 0;
	private int time  = 30;
	
	//×バー
	private BatuBar batuBar;
	private int batuCount = 0;//×の初期数
	private int batuLimit = 5;//×の数
	
	//ドットラインの地面からの高さ
	private static final float dotlineHeight = 0.7f;
	
	//sensor
	private SensorManager sensorManager;
	private Sensor        acceleroS;
	private float[]       values = new float[3];
	
	public GamePlayLayer(Context context){
		
		this.context = context;

		//libs/armeabi/libgdx.soを読み込む//
		System.loadLibrary("gdx");
		
		//タッチアクションを有効にする
		setIsTouchEnabled(true);
		
		/*
		 * ワールドで使用する情報を定義
		 */
		
		//モニタサイズを確定
		dispSize = CCDirector.sharedDirector().winSize();
		
		//1メートルにつき何ピクセルかを確定
		ptmRatio = (int)(dispSize.width / WORLD_WIDTH_METER);
		
		//モニタの中心点を確定
		cPoint = CCDirector.sharedDirector().convertToGL(
				CGPoint.make(dispSize.width/2, dispSize.height/2));
		
		Log.d(TAG, "WORLD_WIDTH_METER:" + WORLD_WIDTH_METER);
		Log.d(TAG, "mSize:" + dispSize.width + "_" + dispSize.height);
		Log.d(TAG, "ptmRatio:" + ptmRatio);
		
		/*
		 * ワールドを定義
		 */
		
		//ワールドの生成
		mGravity = new Vector2(0.0f, -9.8f);
		float scaleWidth  = dispSize.width  / ptmRatio;
		float scaleHeight = dispSize.height / ptmRatio;
		
		Log.d(TAG, "scaleWidth,scaleHeight:" + scaleWidth + "_" + scaleHeight);
		
		//物理空間の生成
		mWorld = new World(mGravity, true);
		mWorld.setContinuousPhysics(true);
		
		//物理空間のグランドの設定
		BodyDef mGroundBodyDef = new BodyDef();
		mGroundBodyDef.position.set(0.0f, 0.0f);
		
		//物理空間のグランドのBodyの設定
		Body mGroundBody = mWorld.createBody(mGroundBodyDef);
		
		//物理空間のグランドの形を生成
		PolygonShape mGroundShape = new PolygonShape();
		
		//物理空間の境界線を定義
		float expandSize    = WORLD_WIDTH_METER * 0.2f;
		Vector2 bottomLeft  = new Vector2(0f-expandSize, 0f);
		Vector2 topLeft     = new Vector2(0f-expandSize, scaleHeight);
		Vector2 topRight    = new Vector2(scaleWidth+expandSize, scaleHeight);
		Vector2 bottomRight = new Vector2(scaleWidth+expandSize, 0f);
		
		//物理空間の下境界
		mGroundShape.setAsEdge(bottomLeft, bottomRight);
		mGroundBody.createFixture(mGroundShape, 0);
		
		//物理空間の上境界
		mGroundShape.setAsEdge(topLeft, topRight);
		mGroundBody.createFixture(mGroundShape, 0);
		
		//物理空間の左境界
		mGroundShape.setAsEdge(topLeft,  bottomLeft);
		mGroundBody.createFixture(mGroundShape, 0);
		
		//物理空間の右境界
		mGroundShape.setAsEdge(topRight, bottomRight);
		mGroundBody.createFixture(mGroundShape, 0);
		
        //ステージ生成
        makeStage();
        
        //猫リスト
        catList = new ArrayList<Cat>();
        CGPoint nPoint = CCDirector.sharedDirector().convertToGL(
				CGPoint.make(dispSize.width/2, dispSize.height*4/5));
        Cat cat = new Cat(this, ptmRatio, mWorld, nPoint, 0.28f);
        catList.add(cat);
        
        //だるまリスト
        darumaList = new ArrayList<Daruma>();
        //Daruma daruma = new Daruma(this, ptmRatio, mWorld, cPoint, 0.05f);
        //darumaList.add(daruma);
        
        //ノルマボタン
        btnS = new NormaBtn(this, ptmRatio, 0.2f, 0.36f, 0.3f);
        btnS.setString("Small:" + countS);
        btnS.on();
        btnM = new NormaBtn(this, ptmRatio, 0.2f, 0.24f, 0.3f);
        btnM.setString("Middle:" + countM);
        btnM.off();
        btnL = new NormaBtn(this, ptmRatio, 0.2f, 0.12f, 0.3f);
        btnL.setString("Large:" + countL);
        btnL.off();
        
        //スコアバー
        scoreBar = new ScoreBar(this, ptmRatio, 
        		WORLD_WIDTH_METER/2,
        		dispSize.height / dispSize.width * WORLD_WIDTH_METER,
        		WORLD_WIDTH_METER, score, time);
        scoreBar.setScore(score);
        
        //×バー
        batuBar = new BatuBar(this, ptmRatio,
        			WORLD_WIDTH_METER/2+0.20f, 0.12f, 0.05f, batuLimit);
        batuBar.setCount(batuCount);
        
        /*
         * センサーの準備
         */
        
		//sensor
		sensorManager = 
				(SensorManager) CCDirector.sharedDirector().
				getActivity().getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> list;
		list = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);//加速度センサー
		if(list.size() > 0) acceleroS = list.get(0);
		
		if(acceleroS != null){
			sensorManager.registerListener(this,
					acceleroS, SensorManager.SENSOR_DELAY_FASTEST);
		}
	}
	
	//ステージ生成
	private void makeStage(){

		//背景のスプライトシート(画面一杯に広げる)
		bgSprite = CCSprite.sprite("back_game.png", 
								CGRect.make(0, 0, 480, 800));
		bgSprite.setScaleX(dispSize.width  / 480);
		bgSprite.setScaleY(dispSize.height / 800);
        bgSprite.setPosition(CGPoint.make(dispSize.width/2, dispSize.height/2));
        addChild(bgSprite, 0);
        
        //ラインを生成
        makeLine();
        
        //地面生成
        makeGround();
	}
	
	private void makeLine(){
		//スプライトの設定(dotline)
		CGRect rect = CGRect.make(0, 0, 480, 5);
		CCSprite sprite = CCSprite.sprite("dotline.png", rect);
		sprite.setPosition(dispSize.width/2, dotlineHeight * ptmRatio);
		addChild(sprite, 0);
	}
	
	private void makeGround(){
		//削除バーの中心点
		CGPoint point = 
				CCDirector.sharedDirector().convertToGL(
						CGPoint.make(dispSize.width/2, dispSize.height));

		//スプライトの設定
		CGRect rect  = CGRect.make(0, 0, 480, 30);
		groundSprite = CCSprite.sprite("ground.png", rect);
		groundSprite.setPosition(point);
		addChild(groundSprite, 0);
		
		//ダイナミックボディの定義
		BodyDef mBodyDef = new BodyDef();
		mBodyDef.type = BodyType.StaticBody;
		//角度をラジアンで定義
		mBodyDef.angle = (float)(0 * Math.PI/180);
		//座標指定
		mBodyDef.position.set(point.x/ptmRatio, point.y/ptmRatio);
		//ボディの形を定義
		float dw = WORLD_WIDTH_METER;//横幅一杯
		float dh = (rect.size.height/rect.size.width) * dw;//メートル単位
		PolygonShape mPolygonShape = new PolygonShape();
		mPolygonShape.setAsBox(dw/2, dh/2);//半分の大きさを指定する必要がある
		
		//接続位置の位置調整で使用する
		float cRatio = (dw * ptmRatio) / rect.size.width;
		
		//ボディの形に合わせてスプライトを拡大縮小(1.1fだけ大きめにしておく)
		groundSprite.setScale(cRatio * 1.4f);
		
		synchronized(mWorld){
			//ダイナミックボディにfixtureを定義
			Body mBody = mWorld.createBody(mBodyDef);
			mBody.setUserData(groundSprite);

			//フィクスチャを定義
			FixtureDef mFixtureDef = new FixtureDef();
			mFixtureDef.shape = mPolygonShape;
			mFixtureDef.density = 0.1f;
			mFixtureDef.friction = 0.1f;
			
			mBody.createFixture(mFixtureDef);
		}
	}
	
	@Override
	public void onEnter(){
		super.onEnter();
		schedule("tick");//スケジュールの開始
		schedule("gameTick", 1.0f);//ゲーム時間の開始
		//BGM再開
		//SoundEngine.sharedEngine().playSound(context, R.raw.bgm_play, true);
	}
	
	@Override
	public void onExit(){
		super.onExit();
		unschedule("tick");//スケジュールの解除
		unschedule("gameTick");//ゲーム時間の解除
		//BGM停止
		//SoundEngine.sharedEngine().pauseSound();
	}
	
	//毎回呼び出される処理
	public synchronized void tick(float delta){
		//FPSの感覚より短い場合はスキップ
		if((rdelta += delta) < FPS){
			return;
		}else{
			//mWorldを進める
			synchronized(mWorld){
				mWorld.step(FPS, 8, 1);
			}
			rdelta = 0;
		}
		
		//物理空間の中にあるBodyをIteratorし、表示位置を反映させる
		Iterator<Body> it = mWorld.getBodies();
		while(it.hasNext()){
			Body body = it.next();
			Object userData = body.getUserData();
			
			if(userData != null && userData instanceof CCSprite){
				final CCSprite sprite = (CCSprite)userData;
				final Vector2  pos    = body.getPosition();
				sprite.setPosition(pos.x*ptmRatio, pos.y*ptmRatio);
				sprite.setRotation(
						-1.0f*ccMacros.CC_RADIANS_TO_DEGREES(body.getAngle()));
			}
		}
		
		//darumaListの削除判定
		//※この手の削除方法は、拡張for文では使ってはいけない。
		for(int i=0; i<darumaList.size(); i++){
			Daruma daruma = darumaList.get(i);
			if(daruma.isHit(groundSprite.getBoundingBox())){
				synchronized(darumaList){
					darumaList.remove(i);
				}
				synchronized(mWorld){
					daruma.delete(mWorld);
				}

				switch(daruma.getTag()){
				case TAG_SMALL:
					countS--;
					btnS.setString("Small:" + countS);
					score -= pointS;
					SoundEngine.sharedEngine().playEffect(context, R.raw.dead);
					break;
				case TAG_MIDDLE:
					countM--;
					btnM.setString("Middle:" + countM);
					score -= pointM;
					SoundEngine.sharedEngine().playEffect(context, R.raw.dead);
					break;
				case TAG_LARGE:
					countL--;
					btnL.setString("Large:" + countL);
					score -= pointL;
					SoundEngine.sharedEngine().playEffect(context, R.raw.dead);
					break;
				}
				scoreBar.setScore(score);
				
				//×バーの更新
				batuCount++;
				if(batuCount > batuLimit){
					gameOver();
				}else{
					batuBar.setCount(batuCount);
				}
			}
		}
		
	}
	
	//ゲームタイマーの処理
	public synchronized void gameTick(float delta){
		time--;
		if(time < 0){
			gameResult();
		}else{
			scoreBar.setTime(time);
		}
	}
	
	@Override
	public boolean ccTouchesBegan(MotionEvent event){

		//タッチされた座標
		CGPoint point = 
				CCDirector.sharedDirector().convertToGL(
						CGPoint.make(event.getX(), event.getY()));
		
		if(btnS.isInside(point)){
			btnS.on();
			btnM.off();
			btnL.off();
			SoundEngine.sharedEngine().playEffect(context, R.raw.btn_go);
		}else if(btnM.isInside(point)){
			btnS.off();
			btnM.on();
			btnL.off();
			SoundEngine.sharedEngine().playEffect(context, R.raw.btn_go);
		}else if(btnL.isInside(point)){
			btnS.off();
			btnM.off();
			btnL.on();
			SoundEngine.sharedEngine().playEffect(context, R.raw.btn_go);
		}
		
		return CCTouchDispatcher.kEventHandled;
	}
	
	@Override
	public boolean ccTouchesEnded(MotionEvent event){
		
		//タッチされた座標
		CGPoint point = 
				CCDirector.sharedDirector().convertToGL(
						CGPoint.make(event.getX(), event.getY()));
		
		if(point.y > dotlineHeight * ptmRatio){
			//だるま追加
			Daruma daruma;
			if(btnS.isActive()){
				daruma = new Daruma(this, ptmRatio, mWorld, point, sizeS);
				daruma.setTag(TAG_SMALL);
				darumaList.add(daruma);
				countS++;
				btnS.setString("Small:" + countS);
				score += pointS;
				SoundEngine.sharedEngine().playEffect(context, R.raw.birth);
			}else if(btnM.isActive()){
				daruma = new Daruma(this, ptmRatio, mWorld, point, sizeM);
				daruma.setTag(TAG_MIDDLE);
				darumaList.add(daruma);
				countM++;
				btnM.setString("Middle:" + countM);
				score += pointM;
				SoundEngine.sharedEngine().playEffect(context, R.raw.birth);
			}else if(btnL.isActive()){
				daruma = new Daruma(this, ptmRatio, mWorld, point, sizeL);
				daruma.setTag(TAG_LARGE);
				darumaList.add(daruma);
				countL++;
				btnL.setString("Large:" + countL);
				score += pointL;
				SoundEngine.sharedEngine().playEffect(context, R.raw.birth);
			}
			scoreBar.setScore(score);
		}
		
		return CCTouchDispatcher.kEventHandled;
	}
	
	/*
     * センサーのメソッド
     */
	@Override
	public void onSensorChanged(SensorEvent event){
		//sensor//
		if(event.sensor == acceleroS){
			values[0] = event.values[0];//x
			values[1] = event.values[1];//y
			values[2] = event.values[2];//z
			
			//mGravity.set(values[0]*-1, values[1]*-1);//PORTLAIT用
			//mGravity.set(values[1], values[0]*-1);//LANDSCAPE用
			//mWorld.setGravity(mGravity);
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy){
		//nothing
	}
	
	private void gameResult(){
		//GameResultへ
		CCScene scene = CCScene.node();
		CCLayer layer = new GameResultLayer(context,
							score, countS, countM, countL);
		scene.addChild(layer);
		CCDirector.sharedDirector().replaceScene(scene);
	}
	
	private void gameOver(){
		//GameOverへ
		CCScene scene = CCScene.node();
		CCLayer layer = new GameOverLayer(context);
		scene.addChild(layer);
		CCDirector.sharedDirector().replaceScene(scene);
	}
}
