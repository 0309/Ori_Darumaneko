package com.ozateck.darumaneko;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.view.MotionEvent;
import android.util.Log;

import org.cocos2d.actions.instant.CCCallFunc;
import org.cocos2d.actions.instant.CCCallFuncN;
import org.cocos2d.actions.interval.CCDelayTime;
import org.cocos2d.actions.interval.CCMoveTo;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.events.CCTouchDispatcher;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCColorLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCLabel;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCSpriteFrame;
import org.cocos2d.nodes.CCSpriteSheet;
import org.cocos2d.opengl.CCTexture2D;
import org.cocos2d.sound.SoundEngine;
import org.cocos2d.types.CGRect;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.ccColor3B;
import org.cocos2d.types.ccColor4B;

public class GameReadyLayer extends CCLayer{
	
	private final static String TAG = "myTag";
	
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
	
	public GameReadyLayer(Context context){
		
		this.context = context;
		
		//タッチアクションを有効にする
		setIsTouchEnabled(true);

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
		
		//アクションを適用
		CCDelayTime delayTime  = CCDelayTime.action(2.0f);
		CCCallFunc  actionDone = CCCallFunc.action(this, "gamePlay");
		CCSequence  actions = CCSequence.actions(delayTime, actionDone);
		runAction(actions);
		
		makeStage();
	}
	
	private void makeStage(){

		//背景のスプライト(画面一杯に広げる)
		CCSprite bgSprite = CCSprite.sprite("back_ready.png", 
								CGRect.make(0, 0, 480, 800));
		bgSprite.setScaleX(dispSize.width  / 480);
		bgSprite.setScaleY(dispSize.height / 800);
        bgSprite.setPosition(CGPoint.make(dispSize.width/2, dispSize.height/2));
        addChild(bgSprite, 0);

		//ロゴのスプライト
		CCSprite logoSprite = CCSprite.sprite("logo_ready.png", 
								CGRect.make(0, 0, 250, 250));
		float logoSize = (float)dispSize.width / 2;
		logoSprite.setScale(logoSize / 250f);
        logoSprite.setPosition(cPoint);
        addChild(logoSprite, 1);
        
		/////////////////////
		//ラベルの追加
		//ディスプレイの横サイズでフォントサイズ指定(だいたい10分の1)
		int txSize = (int)(dispSize.width / 10);
		CCLabel titleLabel = CCLabel.makeLabel("Game Ready!", "Pollyanna.ttf", txSize);
		titleLabel.setColor(ccColor3B.ccBLACK);
		//titleLabel.setAnchorPoint(0, 0);
		titleLabel.setPosition(cPoint);
		addChild(titleLabel, 2);
	}

	@Override
	public void onEnter(){
		super.onEnter();
		//BGM開始
		SoundEngine.sharedEngine().playSound(context, R.raw.bgm_ready, false);
	}
	
	@Override
	public void onExit(){
		super.onExit();
		SoundEngine.sharedEngine().pauseSound();
	}
	
	@Override
	public boolean ccTouchesBegan(MotionEvent event){

		//タッチされた座標
		CGPoint point = 
				CCDirector.sharedDirector().convertToGL(
						CGPoint.make(event.getX(), event.getY()));
		
		return CCTouchDispatcher.kEventHandled;
	}
	
	@Override
	public boolean ccTouchesEnded(MotionEvent event){

		//タッチされた座標
		CGPoint point = 
				CCDirector.sharedDirector().convertToGL(
						CGPoint.make(event.getX(), event.getY()));
		
		return CCTouchDispatcher.kEventHandled;
	}
	
	public void gamePlay(){
		//gamePlayへ
		CCScene scene = CCScene.node();
		CCLayer layer = new GamePlayLayer(context);
		scene.addChild(layer);
		CCDirector.sharedDirector().replaceScene(scene);
	}
}
	