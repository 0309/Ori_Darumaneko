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

	//mWorld�Ŏg�p�����ʂ̉��T�C�Y�͈��(�P�ʂ̓��[�g��)
	//���j�^�̉��T�C�Y����ɂ��āA���[�g���P�ʂŐ��䂷��B
	protected static final float WORLD_WIDTH_METER = 1.0f;
	
	//���j�^�T�C�Y
	private CGSize  dispSize;
	//1���[�g���ɂ����s�N�Z����
	private int     ptmRatio;
	//���j�^�̒��S�_
	private CGPoint cPoint;
	
	//�X�R�A
	private int score;
	
	public GameResultLayer(Context context,
			int score, int countS, int countM, int countL){
		
		this.context = context;
		this.score = score;
		
		//�^�b�`�A�N�V������L���ɂ���
		setIsTouchEnabled(true);

		//���j�^�T�C�Y���m��
		dispSize = CCDirector.sharedDirector().winSize();
		
		//1���[�g���ɂ����s�N�Z�������m��
		ptmRatio = (int)(dispSize.width / WORLD_WIDTH_METER);
		
		//���j�^�̒��S�_���m��
		cPoint = CCDirector.sharedDirector().convertToGL(
				CGPoint.make(dispSize.width/2, dispSize.height/2));
		
		Log.d(TAG, "WORLD_WIDTH_METER:" + WORLD_WIDTH_METER);
		Log.d(TAG, "mSize:" + dispSize.width + "_" + dispSize.height);
		Log.d(TAG, "ptmRatio:" + ptmRatio);
		
		makeStage(score, countS, countM, countL);
	}
	
	private void makeStage(
			int score, int countS, int countM, int countL){

		//�w�i�̃X�v���C�g(��ʈ�t�ɍL����)
		CCSprite bgSprite = CCSprite.sprite("back_result.png", 
								CGRect.make(0, 0, 480, 800));
		bgSprite.setScaleX(dispSize.width  / 480);
		bgSprite.setScaleY(dispSize.height / 800);
        bgSprite.setPosition(CGPoint.make(dispSize.width/2, dispSize.height/2));
        addChild(bgSprite, 0);
        
		/////////////////////
		//���x���̒ǉ�
		//�f�B�X�v���C�̉��T�C�Y�Ńt�H���g�T�C�Y�w��(��������10����1)
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
		
		//S�̃X�v���C�g
		CGRect iconRect = CGRect.make(0, 0, 80, 80);
		float iconSize = 0.2f * ptmRatio;
		CCSprite isSprite = CCSprite.sprite("icon_s.png", iconRect);
		isSprite.setScale(iconSize / 80);
		isSprite.setPosition(cPoint.x - 0.10f * ptmRatio, cPoint.y + 0.05f * ptmRatio);
		addChild(isSprite);
		
		CCLabel csLabel = CCLabel.makeLabel(
				" �~ " + countS, "Pollyanna.ttf", txSize);
		csLabel.setColor(ccColor3B.ccBLACK);
		csLabel.setAnchorPoint(0, 0);
		csLabel.setPosition(cPoint.x, cPoint.y);
		addChild(csLabel);
		
		//M�̃X�v���C�g
		CCSprite imSprite = CCSprite.sprite("icon_m.png", iconRect);
		imSprite.setScale(iconSize / 80);
		imSprite.setPosition(cPoint.x - 0.10f * ptmRatio, cPoint.y - 0.15f * ptmRatio);
		addChild(imSprite);
		
		CCLabel cmLabel = CCLabel.makeLabel(
				" �~ " + countM, "Pollyanna.ttf", txSize);
		cmLabel.setColor(ccColor3B.ccBLACK);
		cmLabel.setAnchorPoint(0, 0);
		cmLabel.setPosition(cPoint.x, cPoint.y - 0.20f * ptmRatio);
		addChild(cmLabel);
		
		//L�̃X�v���C�g
		CCSprite ilSprite = CCSprite.sprite("icon_l.png", iconRect);
		ilSprite.setScale(iconSize / 80);
		ilSprite.setPosition(cPoint.x - 0.10f * ptmRatio, cPoint.y - 0.35f * ptmRatio);
		addChild(ilSprite);
		
		CCLabel clLabel = CCLabel.makeLabel(
				" �~ " + countL, "Pollyanna.ttf", txSize);
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
		//BGM�J�n
		SoundEngine.sharedEngine().playSound(context, R.raw.bgm_result, false);
	}
	
	@Override
	public void onExit(){
		super.onExit();
		SoundEngine.sharedEngine().pauseSound();
	}
	
	@Override
	public boolean ccTouchesBegan(MotionEvent event){

		//�^�b�`���ꂽ���W
		CGPoint point = 
				CCDirector.sharedDirector().convertToGL(
						CGPoint.make(event.getX(), event.getY()));
		
		return CCTouchDispatcher.kEventHandled;
	}
	
	@Override
	public boolean ccTouchesEnded(MotionEvent event){

		//�^�b�`���ꂽ���W
		CGPoint point = 
				CCDirector.sharedDirector().convertToGL(
						CGPoint.make(event.getX(), event.getY()));
		
		SoundEngine.sharedEngine().playEffect(context, R.raw.btn_back);
		gameRanking();
		
		return CCTouchDispatcher.kEventHandled;
	}
	
	private void gameRanking(){
		//gameRanking��
		CCScene scene = CCScene.node();
		CCLayer layer = new GameRankingLayer(context, score);
		scene.addChild(layer);
		CCDirector.sharedDirector().replaceScene(scene);
	}
}
	