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

public class Cat{
	
	private static final String TAG = "myTag";
	
	private final CCLayer    layer;
	private final int        ptmRatio;
	
	private List<Body>       bodyList;
	
	private static final int CAT  = 1;
	private static final int TIRE = 2;
	private static final int BAR  = 3;
	
	public Cat(CCLayer layer, int ptmRatio,
					World mWorld, CGPoint point, float size){
		
		this.layer    = layer;
		this.ptmRatio = ptmRatio;
		
		//猫のスプライトシート(本体、車輪、バー)
		CCSpriteSheet ssCatBody = CCSpriteSheet.spriteSheet("cat.png", 100);
		layer.addChild(ssCatBody, 1, CAT);
		CCSpriteSheet ssTireBody = CCSpriteSheet.spriteSheet("tire.png", 100);
		layer.addChild(ssTireBody, 0, TIRE);
		CCSpriteSheet ssBarBody = CCSpriteSheet.spriteSheet("bar.png", 100);
		layer.addChild(ssBarBody, 2, BAR);
		
		makeNeko(mWorld, point, size);
	}
	
	public void makeNeko(
			World mWorld, CGPoint point, float size){
		
		//取りあえず中心点
		//CGPoint point = cPoint;
		
		/////////////////////
		//スプライトシートの定義
		CCSpriteSheet sheet = (CCSpriteSheet)layer.getChildByTag(CAT);
		CGRect rect         = CGRect.make(0, 0, 200, 210);
		CCSprite sprite     = CCSprite.sprite(sheet, rect);
		sheet.addChild(sprite);
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
		float dh = (rect.size.height/rect.size.width) * dw;//メートル単位
		PolygonShape mPolygonShape = new PolygonShape();
		mPolygonShape.setAsBox(dw/2, dh/2);//半分の大きさを指定する必要がある
		
		//接続位置の位置調整で使用する
		float cRatio = (dw * ptmRatio) / rect.size.width;
		
		//ボディの形に合わせてスプライトを拡大縮小(1.1fだけ大きめにしておく)
		sprite.setScale(cRatio * 1.1f);
		
		/////////////////////
		//バーのスプライトシート
		//バー
		CCSpriteSheet barSheet = (CCSpriteSheet)layer.getChildByTag(BAR);
		CGRect barRect = CGRect.make(0, 0, 400, 20);
		CCSprite barSprite = CCSprite.sprite(barSheet, barRect);
		barSheet.addChild(barSprite);
		//barSpriteF.setPosition(point);
		
		//バーのボディの定義
		BodyDef barBodyDef = new BodyDef();
		barBodyDef.type = BodyType.DynamicBody;
		//角度をラジアンで定義
		barBodyDef.angle = (float)(0 * Math.PI/180);
		//座標指定
		barBodyDef.position.set(
				(point.x+0*cRatio)/ptmRatio,
				(point.y+120*cRatio)/ptmRatio);
		//バーのボディの形を定義
		float bw = (barRect.size.width/rect.size.width) * dw;//基準rectの割合から算出
		float bh = (barRect.size.height/barRect.size.width) * bw;
		PolygonShape barPolygonShape = new PolygonShape();
		barPolygonShape.setAsBox(bw/2, bh/2);//半分の大きさに指定する必要がある
		
		//ボディの形に合わせてスプライトを拡大縮小(1.1fだけ大きめに指定しておく);
		barSprite.setScale(cRatio * 1.1f);
		
		/////////////////////
		//タイヤのスプライトシート
		//タイヤ前輪
		CCSpriteSheet tireSheet = (CCSpriteSheet)layer.getChildByTag(TIRE);
		CGRect tireRect = CGRect.make(0, 0, 155, 155);
		CCSprite tireSprite = CCSprite.sprite(tireSheet, tireRect);
		tireSheet.addChild(tireSprite);
		//tireSprite.setPosition(point);
		
		//タイヤのボディの定義
		BodyDef tireBodyDef = new BodyDef();
		tireBodyDef.type = BodyType.DynamicBody;
		//角度をラジアンで定義
		tireBodyDef.angle = (float)(0 * Math.PI/180);
		//座標指定
		tireBodyDef.position.set(
				(point.x+0*cRatio)/ptmRatio,
				(point.y-190*cRatio)/ptmRatio);
		//タイヤのボディの形を定義
		float tw = (tireRect.size.width/rect.size.width) * dw;//基準rectの割合から算出
		float th = tw;
		//CircleShape tireShape = new CircleShape();
		//tireShape.setRadius(tw/2);//半径
		PolygonShape tireShape = new PolygonShape();
		tireShape.setAsBox(tw/2, th/2);

		//ボディの形に合わせてスプライトを拡大縮小(1.1fだけ大きめにしておく)
		tireSprite.setScale(cRatio * 1.1f);
		
		synchronized(mWorld){
			//bodyList
			bodyList = new ArrayList<Body>();
			
			////////////////
			//本体
			Body mBody = mWorld.createBody(mBodyDef);
			mBody.setUserData(sprite);
			
			//フィクスチャを定義
			FixtureDef mFixtureDef = new FixtureDef();
			mFixtureDef.shape    = mPolygonShape;
			mFixtureDef.density  = 1.0f;
			mFixtureDef.friction = 30.0f;
			mBody.createFixture(mFixtureDef);

			//bodyListに追加
			bodyList.add(mBody);
			
			////////////////
			//バーのボディ
			Body barBody = mWorld.createBody(barBodyDef);
			barBody.setUserData(barSprite);
			//バーのフィクスチャ
			FixtureDef barFixtureDef = new FixtureDef();
			barFixtureDef.shape    = barPolygonShape;
			barFixtureDef.density  = 5.0f;
			barFixtureDef.friction = 30.0f;
			barBody.createFixture(barFixtureDef);

			//リヴォルトジョイント
			//本体とバーを繋げる
			RevoluteJointDef rJointDefB = new RevoluteJointDef();
			rJointDefB.initialize(barBody, mBody, barBody.getWorldCenter());
			//rJointDef.enableMotor    = true;
			//rJointDef.motorSpeed     = 10.0f;
			//rJointDef.maxMotorTorque = 10.0f;//永続的な回転
			rJointDefB.enableLimit = true;
			rJointDefB.lowerAngle  = (float)(-4f * Math.PI/180);
			rJointDefB.upperAngle  = (float)(+4f * Math.PI/180);
			mWorld.createJoint(rJointDefB);
			
			//bodyListに追加
			bodyList.add(barBody);
			
			////////////////
			//タイヤのボディ前輪
			Body tireBody = mWorld.createBody(tireBodyDef);
			tireBody.setUserData(tireSprite);
			//タイヤのフィクスチャ
			FixtureDef tireFixtureDef = new FixtureDef();
			tireFixtureDef.shape    = tireShape;
			tireFixtureDef.density  = 5.0f;
			tireFixtureDef.friction = 30.0f;
			tireBody.createFixture(tireFixtureDef);

			//リヴォルトジョイント
			//本体とタイヤを繋げる
			RevoluteJointDef rJointDefT = new RevoluteJointDef();
			rJointDefT.initialize(tireBody, mBody, tireBody.getWorldCenter());
			//rJointDef.enableMotor    = true;
			//rJointDef.motorSpeed     = 10.0f;
			//rJointDef.maxMotorTorque = 10.0f;//永続的な回転
			rJointDefT.enableLimit = true;
			rJointDefT.lowerAngle  = (float)(-2f * Math.PI/180);
			rJointDefT.upperAngle  = (float)(+2f * Math.PI/180);
			mWorld.createJoint(rJointDefT);
			
			//bodyListに追加
			bodyList.add(tireBody);
		}
	}

	//進行
	public void forward(){
		//タイヤに初速を与える
		//tBodyR.applyAngularImpulse(-300.0f);//永続的な回転
		//tBodyR.applyTorque(-1.0f);//初期的な回転
	}
	
	//後退
	public void backward(){
		//タイヤに初速を与える
		//tBodyR.applyAngularImpulse(-300.0f);//永続的な回転
		//tBodyR.applyTorque(+1.0f);//初期的な回転
	}
	
	//動く
	public void setForce(float x, float y){
		if(bodyList.size() > 0){
			Body mBody = bodyList.get(0);
			Vector2 force = new Vector2(x, y);
	        mBody.applyLinearImpulse(force, mBody.getPosition());
		}
	}

	//衝突判定
	public boolean isHit(Body target){
		return false;
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
