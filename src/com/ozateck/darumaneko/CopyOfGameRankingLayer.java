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

public class CopyOfGameRankingLayer extends CCLayer{
	
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
	private List<String[]> rankList;
	
	public CopyOfGameRankingLayer(Context context, int newScore){
		
		this.context  = context;
		this.newScore = newScore;
		mDBManager = new DBManager(context);
		
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
		
		/*
		//�\������
		int lineNum = 5;
		//�����L���O��lineNum�������̏ꍇ�͏����l��o�^
		if(mDBManager.getTotalCount() < lineNum){
			for(int i=mDBManager.getTotalCount(); i<lineNum; i++){
				try{
					mDBManager.insert("hoge", 100);
				}catch(Exception e){
					Log.d(TAG, "insert error.");
				}
			}
		}

		//�����L���O�Ƃ̔�r
		rankList = mDBManager.getList(lineNum);
		
		//Rank�o�[�̃X�v���C�g�V�[�g
		CCSpriteSheet ssRankbar = CCSpriteSheet.spriteSheet("back_rankbar.png", 100);
		addChild(ssRankbar, 1, BACK_RANKBAR);
		
		if(rankList.size() > 0){
			//����ւ������邩�ǂ����𔻒�
			boolean flag  = false;
			int flagIndex = 0;
			for(int i=0; i<lineNum; i++){
				if(flag == false && 
						newScore > Integer.valueOf(rankList.get(i)[2])){
					String[] record = new String[]{"" + i, "hoge", "" + newScore};
					rankList.add(i, record);
					flag      = true;//����ւ�����
					flagIndex = i;
					break;
				}
			}
			//�����L���O��\��
			float rbSize = 0.5f * ptmRatio;
			float padding = 0.15f;
			for(int i=0; i<lineNum; i++){
				
				Log.d(TAG, "point:" + rankList.get(i)[2]);
				
				if(flag == true && flagIndex == i){
					makeRankbar(ssRankbar, cPoint.x,
							cPoint.y + (0.25f - padding * i) * ptmRatio,
							rbSize, i+1, Integer.valueOf(rankList.get(i)[2]), true);
				}else{
					makeRankbar(ssRankbar, cPoint.x,
							cPoint.y + (0.25f - padding * i) * ptmRatio,
							rbSize, i+1, Integer.valueOf(rankList.get(i)[2]), false);
				}
			}
			//�����L���O�̍X�V
			if(flag == true){
				for(int i=0; i<lineNum; i++){
					int    myind   = Integer.valueOf(rankList.get(i)[0]);
					String myname  = rankList.get(i)[1];
					int    myscore = Integer.valueOf(rankList.get(i)[2]);
					try{
						mDBManager.update(i, myname, myscore);
					}catch(Exception e){
						Log.d(TAG, "update failed.");
					}
				}
			}
		}
		*/
		
//		if(rankList.size() > 0){
//			//����ւ������邩�ǂ����𔻒�
//			boolean flag  = false;
//			int flagIndex = 0;
//			for(int i=0; i<lineNum; i++){
//				if(newScore > rankList.get(i)){
//					rankList.add(i, newScore);
//					flag      = true;//����ւ�����
//					flagIndex = i;
//					break;
//				}
//			}
//			//�����L���O��\��
//			float padding = 0.15f;
//			for(int i=0; i<lineNum; i++){
//				if(flag == true && flagIndex == i){
//					makeRankbar(ssRankbar, cPoint.x,
//							cPoint.y + (0.25f - padding * i) * ptmRatio,
//							rbSize, i+1, rankList.get(i), true);
//				}else{
//					makeRankbar(ssRankbar, cPoint.x,
//							cPoint.y + (0.25f - padding * i) * ptmRatio,
//							rbSize, i+1, rankList.get(i), false);
//				}
//			}
//			//�����L���O�̍X�V
//			if(flag == true){
//				
//			}
//		}
		
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
	}
	
	@Override
	public void onExit(){
		super.onExit();
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
	
	
	
	
	
	
	
	
	
	
	
	
	String onamae;
	
	
	public String getOnamae(){
		String onamae = "yamada";
		return onamae;
	}
	
	public void setOnamae(String middleOnamae){
		onamae = middleOnamae;
	}
	
	public String getOnamae(String middleOnamae){
		String onamae = "yamada" + middleOnamae;
		return onamae;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
	