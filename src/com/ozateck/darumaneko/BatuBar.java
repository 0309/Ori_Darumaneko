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

public class BatuBar{
	
	private static final String TAG = "myTag";
	
	private final CCLayer layer;
	private final int     ptmRatio;
	
	private int batuLimit;
	private List<CCSprite> batuList;
	
	private static final int BACK_BATU = 1;
	
	//トグルボタンx,yはメートル換算
	public BatuBar(CCLayer layer, int ptmRatio,
						float x, float y, float size,
						int batuLimit){
		
		this.layer    = layer;
		this.ptmRatio = ptmRatio;
		
		this.batuLimit = batuLimit;
		batuList = new ArrayList<CCSprite>();
		
		makeBatu(x, y, size);
	}
	
	private void makeBatu(float x, float y, float size){

		/////////////////////
        //×マークのスプライトシート
		CCSpriteSheet ssBack = CCSpriteSheet.spriteSheet("back_batu.png", 100);
		layer.addChild(ssBack, 0, BACK_BATU);
		
		CGRect rect = CGRect.make(0, 0, 40, 40);
		
		//batuNumの数だけマークを配置
		for(int i=0; i<batuLimit; i++){
			CCSprite batuSprite = new CCSprite();
			batuSprite.setTexture(ssBack.getTexture());
			batuSprite.setTextureRect(rect);
			
			float bw = size * ptmRatio;//メートル換算でのピクセル数
			float bh = (rect.size.height / rect.size.width) * bw;
			
			batuSprite.setScaleX(bw/rect.size.width);
			batuSprite.setScaleY(bh/rect.size.height);
			
			batuSprite.setPosition(x * ptmRatio + (bw*i), y * ptmRatio);
			
			batuList.add(batuSprite);//batuListに追加
			
			layer.addChild(batuSprite, 0);
		}
	}
	
	public void setCount(int count){
		if(batuList.size() >= count){
			CGRect rect = CGRect.make(40, 0, 40, 40);
			for(int i=0; i<count; i++){
				batuList.get(i).setTextureRect(rect);
			}
		}
	}
}
	