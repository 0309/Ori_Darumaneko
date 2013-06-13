package com.ozateck.darumaneko;

import java.util.List;
import java.util.ArrayList;

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

public class ScoreBar{
	
	private static final String TAG = "myTag";
	
	private final CCLayer    layer;
	private final int        ptmRatio;
	
	private int score;
	private int time;
	
	private static final int BACK_SCOREBAR = 0;
	private CCLabel scoreLabel;
	private CCLabel timeLabel;
	
	//x,yはメートル換算
	public ScoreBar(CCLayer layer, int ptmRatio,
						float x, float y, float size,
						int score, int time){
		
		this.layer    = layer;
		this.ptmRatio = ptmRatio;
		
		this.score = score;
		this.time  = time;
		
		makeScoreBar(x, y, size);
	}
	
	private void makeScoreBar(float x, float y, float size){

		/////////////////////
        //背景のスプライトシート
		CCSpriteSheet ssBack = CCSpriteSheet.spriteSheet("back_score.png", 100);
		layer.addChild(ssBack, 1, BACK_SCOREBAR);
		
		CGRect rect = CGRect.make(0, 0, 480, 50);
		
		CCSprite backSprite = new CCSprite();
		backSprite.setTexture(ssBack.getTexture());
		backSprite.setTextureRect(rect);
		
		float bw = size * ptmRatio;//メートル換算でのピクセル数
		float bh = (rect.size.height / rect.size.width) * bw;
		
		backSprite.setScaleX(bw/rect.size.width);
		backSprite.setScaleY(bh/rect.size.height);
		
		backSprite.setPosition(x * ptmRatio, y * ptmRatio - bh/2);
		
		layer.addChild(backSprite, 0);
		
		/////////////////////
		//ラベルの追加
		//dw=144でフォントサイズ30(だいたい12分の1)
		int txSize = (int)(bw/12-2);
		scoreLabel = CCLabel.makeLabel("Score:" + score, "Pollyanna.ttf", txSize);
		scoreLabel.setColor(ccColor3B.ccBLACK);
		scoreLabel.setAnchorPoint(0, 0);
		scoreLabel.setPosition(0.05f * ptmRatio, y * ptmRatio - bh);
		layer.addChild(scoreLabel, 1);
		
		/////////////////////
		//ラベルの追加(タイマー部分)
		timeLabel = CCLabel.makeLabel("Time:" + time, "Pollyanna.ttf", txSize);
		timeLabel.setColor(ccColor3B.ccBLACK);
		timeLabel.setAnchorPoint(0, 0);
		timeLabel.setPosition((x+0.2f) * ptmRatio, y * ptmRatio - bh);
		layer.addChild(timeLabel, 1);
	}
	
	public void setScore(int score){
		scoreLabel.setString("Score:" + score);
	}
	
	public void setTime(int time){
		if(time < 10){
			timeLabel.setString("Time:0" + time);
		}else{
			timeLabel.setString("Time:" + time);
		}
	}
}