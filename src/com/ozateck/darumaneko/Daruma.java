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

public class Daruma{

	private static final String TAG = "myTag";
	
	private final CCLayer    layer;
	private final int        ptmRatio;
	
	private Body             mBody;
	private List<Body>       bodyList;
	private int              tag;
	
	public Daruma(CCLayer layer, int ptmRatio,
					World mWorld, CGPoint point, float size){
		
		this.layer    = layer;
		this.ptmRatio = ptmRatio;
		
        makeBody(mWorld, point, size);
	}

	public void makeBody(
			World mWorld, CGPoint point, float size){

		//とりあえず中心点
		//CGPoint point = cPoint;
		
		/////////////////////
        //だるまのスプライト
		CGRect rect     = CGRect.make(0, 0, 80, 80);
        CCSprite sprite = CCSprite.sprite("daruma.png", rect);
        layer.addChild(sprite, 1);
        sprite.setPosition(point);

		//ボディの定義
		BodyDef mBodyDef = new BodyDef();
		mBodyDef.type = BodyType.DynamicBody;
		//角度をラジアンで定義
		mBodyDef.angle = (float)(0 * Math.PI/180);
		
		//座標指定
		mBodyDef.position.set(point.x/ptmRatio, point.y/ptmRatio);
		//ボディの形を定義
		float dw = size;//メートル単位
		float dh = (rect.size.height/rect.size.width) * size;//メートル単位
		PolygonShape mPolygonShape = new PolygonShape();
		mPolygonShape.setAsBox(dw/2, dh/2);//半分の大きさを指定する必要がある
		
		//接続位置の位置調整で使用する
		float cRatio = (dw * ptmRatio) / rect.size.width;
		
		//ボディの形に合わせてスプライトを拡大縮小(1.1fだけ大きめにしておく)
		sprite.setScale(cRatio * 1.5f);
		
		synchronized(mWorld){
			//bodyList
			bodyList = new ArrayList<Body>();
			
			////////////////
			//本体
			mBody = mWorld.createBody(mBodyDef);
			mBody.setUserData(sprite);
			
			//フィクスチャを定義
			FixtureDef mFixtureDef = new FixtureDef();
			mFixtureDef.shape    = mPolygonShape;
			mFixtureDef.density  = 1.0f;
			mFixtureDef.friction = 5.0f;
			mBody.createFixture(mFixtureDef);

			//bodyListに追加
			bodyList.add(mBody);
		}
	}
	
	//tagをセット
	public void setTag(int tag){
		this.tag = tag;
	}
	
	//tagをゲット
	public int getTag(){
		return tag;
	}

	//衝突判定
	public boolean isHit(CGRect targetRect){
		
		for(Body mBody:bodyList){
			Object userData = mBody.getUserData();
			if(userData != null && userData instanceof CCSprite){
				final CCSprite sprite = (CCSprite)userData;
				final CGRect   rect   = sprite.getBoundingBox();
				
				if(CGRect.intersects(rect, targetRect)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public void setForce(float x, float y){
		Vector2 force = new Vector2(x, y);
        mBody.applyLinearImpulse(force, mBody.getPosition());
	}
	
	//ワールドから削除
	public void delete(World mWorld){
		Log.d(TAG, "delete");
		if(bodyList.size() > 0){
			for(Body mBody:bodyList){
				Object userData = mBody.getUserData();
				if(userData != null && userData instanceof CCSprite){
					
					final CCSprite mSprite = (CCSprite)userData;
					//削除
					mWorld.destroyBody(mBody);
					mSprite.removeSelf();
				}
			}
			bodyList = null;
		}
	}
}
