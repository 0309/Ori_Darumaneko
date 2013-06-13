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

import com.ozateck.db.DBManager;

public class GameResultLayer extends CCLayer{
	
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
	
	//スコア
	private int score;
	
	public GameResultLayer(Context context,
			int score, int countS, int countM, int countL){
		
		this.context = context;
		this.score = score;
		
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
		
		makeStage(score, countS, countM, countL);
	}
	
	private void makeStage(
			int score, int countS, int countM, int countL){

		//背景のスプライト(画面一杯に広げる)
		CCSprite bgSprite = CCSprite.sprite("back_result.png", 
								CGRect.make(0, 0, 480, 800));
		bgSprite.setScaleX(dispSize.width  / 480);
		bgSprite.setScaleY(dispSize.height / 800);
        bgSprite.setPosition(CGPoint.make(dispSize.width/2, dispSize.height/2));
        addChild(bgSprite, 0);
        
		/////////////////////
		//ラベルの追加
		//ディスプレイの横サイズでフォントサイズ指定(だいたい10分の1)
		int txSize = (int)(dispSize.width / 10);
		CCLabel titleLabel = CCLabel.makeLabel("RESULT", "Pollyanna.ttf", txSize);
		titleLabel.setColor(ccColor3B.ccBLACK);
		//titleLabel.setAnchorPoint(0, 0);
        titleLabel.setPosition(cPoint.x, cPoint.y + 0.45f * ptmRatio);
		addChild(titleLabel, 2);

		//Score
		CCLabel scoreLabel = CCLabel.makeLabel(
				"Score:" + score, "Pollyanna.ttf", txSize - 2);
		scoreLabel.setColor(ccColor3B.ccBLACK);
        scoreLabel.setPosition(cPoint.x, cPoint.y + 0.30f * ptmRatio);
		addChild(scoreLabel, 2);
		
		//Sのスプライト
		CGRect iconRect = CGRect.make(0, 0, 80, 80);
		float iconSize = 0.2f * ptmRatio;
		CCSprite isSprite = CCSprite.sprite("icon_s.png", iconRect);
		isSprite.setScale(iconSize / 80);
		isSprite.setPosition(cPoint.x - 0.10f * ptmRatio, cPoint.y + 0.05f * ptmRatio);
		addChild(isSprite);
		
		CCLabel csLabel = CCLabel.makeLabel(
				" × " + countS, "Pollyanna.ttf", txSize);
		csLabel.setColor(ccColor3B.ccBLACK);
		csLabel.setAnchorPoint(0, 0);
		csLabel.setPosition(cPoint.x, cPoint.y);
		addChild(csLabel);
		
		//Mのスプライト
		CCSprite imSprite = CCSprite.sprite("icon_m.png", iconRect);
		imSprite.setScale(iconSize / 80);
		imSprite.setPosition(cPoint.x - 0.10f * ptmRatio, cPoint.y - 0.15f * ptmRatio);
		addChild(imSprite);
		
		CCLabel cmLabel = CCLabel.makeLabel(
				" × " + countM, "Pollyanna.ttf", txSize);
		cmLabel.setColor(ccColor3B.ccBLACK);
		cmLabel.setAnchorPoint(0, 0);
		cmLabel.setPosition(cPoint.x, cPoint.y - 0.20f * ptmRatio);
		addChild(cmLabel);
		
		//Lのスプライト
		CCSprite ilSprite = CCSprite.sprite("icon_l.png", iconRect);
		ilSprite.setScale(iconSize / 80);
		ilSprite.setPosition(cPoint.x - 0.10f * ptmRatio, cPoint.y - 0.35f * ptmRatio);
		addChild(ilSprite);
		
		CCLabel clLabel = CCLabel.makeLabel(
				" × " + countL, "Pollyanna.ttf", txSize);
		clLabel.setColor(ccColor3B.ccBLACK);
		clLabel.setAnchorPoint(0, 0);
		clLabel.setPosition(cPoint.x, cPoint.y - 0.40f * ptmRatio);
		addChild(clLabel);
		
		//Tap to
		CCLabel taptoLabel = CCLabel.makeLabel("Tap to Ranking", "Pollyanna.ttf", txSize-8);
		taptoLabel.setColor(ccColor3B.ccBLACK);
		//taptoLabel.setAnchorPoint(0, 0);
        taptoLabel.setPosition(cPoint.x, cPoint.y - 0.6f * ptmRatio);
		addChild(taptoLabel, 2);
	}

	@Override
	public void onEnter(){
		super.onEnter();
		//BGM開始
		SoundEngine.sharedEngine().playSound(context, R.raw.bgm_result, false);
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
		gameRanking();
		
		return CCTouchDispatcher.kEventHandled;
	}
	
	private void gameRanking(){
		//gameRankingへ
		CCScene scene = CCScene.node();
		CCLayer layer = new GameRankingLayer(context, score);
		scene.addChild(layer);
		CCDirector.sharedDirector().replaceScene(scene);
	}
}
	