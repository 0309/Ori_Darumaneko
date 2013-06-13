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

public class GameOverLayer extends CCLayer{
	
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
	
	public GameOverLayer(Context context){
		
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
		
		makeStage();
	}
	
	private void makeStage(){

		//背景のスプライト(画面一杯に広げる)
		CCSprite bgSprite = CCSprite.sprite("back_over.png", 
								CGRect.make(0, 0, 480, 800));
		bgSprite.setScaleX(dispSize.width  / 480);
		bgSprite.setScaleY(dispSize.height / 800);
        bgSprite.setPosition(CGPoint.make(dispSize.width/2, dispSize.height/2));
        addChild(bgSprite, 0);
        
		/////////////////////
		//ラベルの追加
		//ディスプレイの横サイズでフォントサイズ指定(だいたい10分の1)
		int txSize = (int)(dispSize.width / 10);
		CCLabel titleLabel = CCLabel.makeLabel("Game Over!", "Pollyanna.ttf", txSize);
		titleLabel.setColor(ccColor3B.ccBLACK);
		//titleLabel.setAnchorPoint(0, 0);
        titleLabel.setPosition(cPoint.x, cPoint.y + 0.1f * ptmRatio);
		addChild(titleLabel, 2);
		
		CCLabel taptoLabel = CCLabel.makeLabel("Tap to Top", "Pollyanna.ttf", txSize-8);
		taptoLabel.setColor(ccColor3B.ccBLACK);
		//taptoLabel.setAnchorPoint(0, 0);
        taptoLabel.setPosition(cPoint.x, cPoint.y - 0.05f * ptmRatio);
		addChild(taptoLabel, 2);
	}

	@Override
	public void onEnter(){
		super.onEnter();
		//BGM開始
		SoundEngine.sharedEngine().playSound(context, R.raw.bgm_over, false);
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
		
		SoundEngine.sharedEngine().playEffect(context, R.raw.btn_back);
		gameTop();
		
		return CCTouchDispatcher.kEventHandled;
	}
	
	private void gameTop(){
		//gameTopへ
		CCScene scene = CCScene.node();
		CCLayer layer = new GameTopLayer(context);
		scene.addChild(layer);
		CCDirector.sharedDirector().replaceScene(scene);
	}
}
	