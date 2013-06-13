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

	//mWorld�Ŏg�p�����ʂ̉��T�C�Y�͈��(�P�ʂ̓��[�g��)
	//���j�^�̉��T�C�Y����ɂ��āA���[�g���P�ʂŐ��䂷��B
	protected static final float WORLD_WIDTH_METER = 1.0f;
	
	//���j�^�T�C�Y
	private CGSize  dispSize;
	//1���[�g���ɂ����s�N�Z����
	private int     ptmRatio;
	//���j�^�̒��S�_
	private CGPoint cPoint;
	
	public GameReadyLayer(Context context){
		
		this.context = context;
		
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
		
		//�A�N�V������K�p
		CCDelayTime delayTime  = CCDelayTime.action(2.0f);
		CCCallFunc  actionDone = CCCallFunc.action(this, "gamePlay");
		CCSequence  actions = CCSequence.actions(delayTime, actionDone);
		runAction(actions);
		
		makeStage();
	}
	
	private void makeStage(){

		//�w�i�̃X�v���C�g(��ʈ�t�ɍL����)
		CCSprite bgSprite = CCSprite.sprite("back_ready.png", 
								CGRect.make(0, 0, 480, 800));
		bgSprite.setScaleX(dispSize.width  / 480);
		bgSprite.setScaleY(dispSize.height / 800);
        bgSprite.setPosition(CGPoint.make(dispSize.width/2, dispSize.height/2));
        addChild(bgSprite, 0);

		//���S�̃X�v���C�g
		CCSprite logoSprite = CCSprite.sprite("logo_ready.png", 
								CGRect.make(0, 0, 250, 250));
		float logoSize = (float)dispSize.width / 2;
		logoSprite.setScale(logoSize / 250f);
        logoSprite.setPosition(cPoint);
        addChild(logoSprite, 1);
        
		/////////////////////
		//���x���̒ǉ�
		//�f�B�X�v���C�̉��T�C�Y�Ńt�H���g�T�C�Y�w��(��������10����1)
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
		//BGM�J�n
		SoundEngine.sharedEngine().playSound(context, R.raw.bgm_ready, false);
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
		
		return CCTouchDispatcher.kEventHandled;
	}
	
	public void gamePlay(){
		//gamePlay��
		CCScene scene = CCScene.node();
		CCLayer layer = new GamePlayLayer(context);
		scene.addChild(layer);
		CCDirector.sharedDirector().replaceScene(scene);
	}
}
	