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

public class Bycycle{
	
	private static final String TAG = "myTag";
	
	private final CCLayer    layer;
	private final int        ptmRatio;
	
	private List<Body>       bodyList;
	
	private static final int BYCYCLE_BODY = 1;
	private static final int BYCYCLE_TIRE = 2;
	
	//後輪
	private Body tBodyR;
	
	public Bycycle(CCLayer layer, int ptmRatio,
					World mWorld, CGPoint point, float size){
		
		this.layer    = layer;
		this.ptmRatio = ptmRatio;
		
        //自転車のスプライトシート(本体、前輪、後輪)
        CCSpriteSheet ssBycBody = CCSpriteSheet.spriteSheet("cycle_body.png", 100);
        layer.addChild(ssBycBody, 0, BYCYCLE_BODY);
        CCSpriteSheet ssBycTireFront = CCSpriteSheet.spriteSheet("cycle_tire.png", 100);
        layer.addChild(ssBycTireFront, 0, BYCYCLE_TIRE);
		
        makeBycycle(mWorld, point, size);
	}

	public void makeBycycle(
			World mWorld, CGPoint point, float size){

		//とりあえず中心点
		//CGPoint point = cPoint;
		
		/////////////////////
		//スプライトシートの定義
		CCSpriteSheet sheet = (CCSpriteSheet)layer.getChildByTag(BYCYCLE_BODY);
		CGRect rect         = CGRect.make(0, 0, 185, 110);
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
		float dh = (rect.size.height/rect.size.width) * size;//メートル単位
		PolygonShape mPolygonShape = new PolygonShape();
		mPolygonShape.setAsBox(dw/2, dh/2);//半分の大きさを指定する必要がある
		
		//接続位置の位置調整で使用する
		float cRatio = (dw * ptmRatio) / rect.size.width;
		
		//ボディの形に合わせてスプライトを拡大縮小(1.1fだけ大きめにしておく)
		sprite.setScale(cRatio * 1.1f);
		
		/////////////////////
		//タイヤのスプライトシート
		//タイヤ前輪
		CCSpriteSheet tireSheet = (CCSpriteSheet)layer.getChildByTag(BYCYCLE_TIRE);
		CGRect tireRect = CGRect.make(0, 0, 80, 80);
		CCSprite tireSpriteF = CCSprite.sprite(tireSheet, tireRect);
		tireSheet.addChild(tireSpriteF);
		//tireSpriteF.setPosition(point);
		
		//タイヤのボディの定義
		BodyDef tireBodyDefF = new BodyDef();
		tireBodyDefF.type = BodyType.DynamicBody;
		//角度をラジアンで定義
		tireBodyDefF.angle = (float)(0 * Math.PI/180);
		//座標指定
		tireBodyDefF.position.set(
				(point.x+90*cRatio)/ptmRatio,
				(point.y-30*cRatio)/ptmRatio);
		//タイヤのボディの形を定義
		float tw = (tireRect.size.width/rect.size.width) * dw;//基準rectの割合から算出
		CircleShape tireShape = new CircleShape();
		tireShape.setRadius(tw/2);//半径

		//ボディの形に合わせてスプライトを拡大縮小(1.1fだけ大きめにしておく)
		tireSpriteF.setScale(cRatio * 1.1f);

		/////////////////////
		//タイヤのスプライトシート
		//タイヤ後輪
		CCSprite tireSpriteR = CCSprite.sprite(tireSheet, tireRect);
		tireSheet.addChild(tireSpriteR);
		tireSpriteR.setPosition(point);
		
		//タイヤのボディの定義
		BodyDef tireBodyDefR = new BodyDef();
		tireBodyDefR.type = BodyType.DynamicBody;
		//角度をラジアンで定義
		tireBodyDefR.angle = (float)(0 * Math.PI/180);
		//座標指定
		tireBodyDefR.position.set(
				(point.x-55*cRatio)/ptmRatio,
				(point.y-30*cRatio)/ptmRatio);

		//ボディの形に合わせてスプライトを拡大縮小(1.1fだけ大きめにしておく)
		tireSpriteR.setScale(cRatio * 1.1f);
		
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
			mFixtureDef.friction = 10.0f;
			mBody.createFixture(mFixtureDef);

			//bodyListに追加
			bodyList.add(mBody);
			
			////////////////
			//タイヤのボディ前輪
			Body tBodyF = mWorld.createBody(tireBodyDefF);
			tBodyF.setUserData(tireSpriteF);
			//タイヤのフィクスチャ
			FixtureDef tireFixtureDef = new FixtureDef();
			tireFixtureDef.shape    = tireShape;
			tireFixtureDef.density  = 5.0f;
			tireFixtureDef.friction = 10.0f;
			tBodyF.createFixture(tireFixtureDef);

			//リヴォルトジョイント
			//本体とタイヤを繋げる
			RevoluteJointDef rJointDefF = new RevoluteJointDef();
			rJointDefF.initialize(tBodyF, mBody, tBodyF.getWorldCenter());
			//rJointDef.enableMotor    = true;
			//rJointDef.motorSpeed     = 10.0f;
			//rJointDef.maxMotorTorque = 10.0f;//永続的な回転
			//rJointDef.enableLimit = true;
			//rJointDef.lowerAngle  = (float)(0f * Math.PI/180);
			//rJointDef.upperAngle  = (float)(90f * Math.PI/180);
			mWorld.createJoint(rJointDefF);
			
			//bodyListに追加
			bodyList.add(tBodyF);
			
			////////////////
			//タイヤのボディ後輪
			tBodyR = mWorld.createBody(tireBodyDefR);
			tBodyR.setUserData(tireSpriteR);
			//タイヤのフィクスチャ
			tBodyR.createFixture(tireFixtureDef);

			//リヴォルトジョイント
			//本体とタイヤを繋げる
			RevoluteJointDef rJointDefR = new RevoluteJointDef();
			rJointDefR.initialize(tBodyR, mBody, tBodyR.getWorldCenter());
			//rJointDef.enableMotor    = true;
			//rJointDef.motorSpeed     = 10.0f;
			//rJointDef.maxMotorTorque = 10.0f;//永続的な回転
			//rJointDef.enableLimit = true;
			//rJointDef.lowerAngle  = (float)(0f * Math.PI/180);
			//rJointDef.upperAngle  = (float)(90f * Math.PI/180);
			mWorld.createJoint(rJointDefR);
			
			//bodyListに追加
			bodyList.add(tBodyR);
		}
	}

	//進行
	public void forward(){
		//タイヤに初速を与える
		//tBodyR.applyAngularImpulse(-300.0f);//永続的な回転
		tBodyR.applyTorque(-1.0f);//初期的な回転
	}
	
	//後退
	public void backward(){
		//タイヤに初速を与える
		//tBodyR.applyAngularImpulse(-300.0f);//永続的な回転
		tBodyR.applyTorque(+1.0f);//初期的な回転
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
