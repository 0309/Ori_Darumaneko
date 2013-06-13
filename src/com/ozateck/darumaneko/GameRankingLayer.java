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
import com.ozateck.db.RankData;

public class GameRankingLayer extends CCLayer{
	
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

	private static final int BACK_RANKBAR = 1;
	private int newScore;
	private DBManager mDBManager;
	
	public GameRankingLayer(Context context, int newScore){
		
		this.context  = context;
		this.newScore = newScore;
		
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
		
		makeStage();
	}
	
	private void makeStage(){

		//�w�i�̃X�v���C�g(��ʈ�t�ɍL����)
		CCSprite bgSprite = CCSprite.sprite("back_ranking.png", 
								CGRect.make(0, 0, 480, 800));
		bgSprite.setScaleX(dispSize.width  / 480);
		bgSprite.setScaleY(dispSize.height / 800);
        bgSprite.setPosition(CGPoint.make(dispSize.width/2, dispSize.height/2));
        addChild(bgSprite, 0);
        
		/////////////////////
		//���x���̒ǉ�
		//�f�B�X�v���C�̉��T�C�Y�Ńt�H���g�T�C�Y�w��(��������10����1)
		int txSize = (int)(dispSize.width / 10);
		CCLabel titleLabel = CCLabel.makeLabel("RANKING", "Pollyanna.ttf", txSize);
		titleLabel.setColor(ccColor3B.ccBLACK);
		//titleLabel.setAnchorPoint(0, 0);
        titleLabel.setPosition(cPoint.x, cPoint.y + 0.45f * ptmRatio);
		addChild(titleLabel, 2);
		
		//Tap to
		CCLabel taptoLabel = CCLabel.makeLabel("Tap to Top", "Pollyanna.ttf", txSize-8);
		taptoLabel.setColor(ccColor3B.ccBLACK);
		//taptoLabel.setAnchorPoint(0, 0);
        taptoLabel.setPosition(cPoint.x, cPoint.y - 0.5f * ptmRatio);
		addChild(taptoLabel, 2);
	}
	
	private void makeRankbar(CCSpriteSheet ssSheet,
			float x, float y, float size, int rank, int point, boolean active){
		CGRect offRect = CGRect.make(0, 0, 200, 50);
		CGRect onRect  = CGRect.make(0, 50, 200, 50);
		
		CCSprite rbSprite = new CCSprite();
		rbSprite.setTexture(ssSheet.getTexture());
		if(!active){
			rbSprite.setTextureRect(offRect);
		}else{
			rbSprite.setTextureRect(onRect);
		}
		rbSprite.setScale(size / 160);
		rbSprite.setPosition(x, y);
		addChild(rbSprite, 1);

		/////////////////////
		//���x���̒ǉ�
		//��ʃT�C�Y�̂�������10����1
		int txSize = (int)(dispSize.width/10-2);
		CCLabel rankLabel = CCLabel.makeLabel("" + rank, "Pollyanna.ttf", txSize);
		rankLabel.setColor(ccColor3B.ccBLACK);
		rankLabel.setPosition(x - 0.2f * ptmRatio, y - 0.01f * ptmRatio);
		//rankLabel.setAnchorPoint(0f, 0f);
		addChild(rankLabel, 2);
		
		CCLabel scoreLabel = CCLabel.makeLabel("" + point, "Pollyanna.ttf", txSize);
		scoreLabel.setColor(ccColor3B.ccBLACK);
		scoreLabel.setPosition(x + 0.05f * ptmRatio, y - 0.01f * ptmRatio);
		//scoreLabel.setAnchorPoint(0f, 0f);
		addChild(scoreLabel, 2);
	}
	
	@Override
	public void onEnter(){
		super.onEnter();
		
		//DBManager
		mDBManager = new DBManager(context);
		if(mDBManager.getTotalCount() == 0){
			mDBManager.initialize();//DB�̏�����
		}
		
		Log.d(TAG, "total:" + mDBManager.getTotalCount());
		
		//Rank�o�[�̃X�v���C�g�V�[�g
		CCSpriteSheet ssRankbar = CCSpriteSheet.spriteSheet("back_rankbar.png", 100);
		addChild(ssRankbar, 1, BACK_RANKBAR);
		
		//�X�R�A�Ƀ����N�C������Ă���΃��X�g�ɒǉ�
		int limit = 5;
		List<RankData> rankList = mDBManager.getRankList("guest", newScore, limit);
		
		//�X�R�A�{�[�h�̕`��
		float rbSize  = 0.5f * ptmRatio;//�����N�o�[�̃T�C�Y
		float padding = 0.15f;//�����N�o�[�̊Ԋu
		for(int i=0; i<rankList.size(); i++){
			RankData data = rankList.get(i);
			Log.d(TAG, "newdata:" + 
					data.myInd + "_" + data.myName + "_" + data.myScore);
			//�����L���O��\��
			boolean rankin = data.rankin;
			if(rankin){
				makeRankbar(ssRankbar, cPoint.x,
						cPoint.y + (0.25f - padding * i) * ptmRatio,
						rbSize, i+1, rankList.get(i).myScore, true);
			}else{
				makeRankbar(ssRankbar, cPoint.x,
						cPoint.y + (0.25f - padding * i) * ptmRatio,
						rbSize, i+1, rankList.get(i).myScore, false);
			}
		}
		
		//DB�̍X�V
		if(mDBManager.isRankin(newScore, limit)){
			mDBManager.resetAll();
			for(int i=0; i<rankList.size(); i++){
				RankData data = rankList.get(i);
				mDBManager.insert(data.myName, ""+data.myScore);
			}
		}
	}
	
	@Override
	public void onExit(){
		super.onExit();
		
		//DBManager
		mDBManager.close();
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
		gameTop();
		
		return CCTouchDispatcher.kEventHandled;
	}
	
	private void gameTop(){
		//gameTop��
		CCScene scene = CCScene.node();
		CCLayer layer = new GameTopLayer(context);
		scene.addChild(layer);
		CCDirector.sharedDirector().replaceScene(scene);
	}
}
	