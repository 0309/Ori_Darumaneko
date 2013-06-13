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
	
	//���
	private Body tBodyR;
	
	public Bycycle(CCLayer layer, int ptmRatio,
					World mWorld, CGPoint point, float size){
		
		this.layer    = layer;
		this.ptmRatio = ptmRatio;
		
        //���]�Ԃ̃X�v���C�g�V�[�g(�{�́A�O�ցA���)
        CCSpriteSheet ssBycBody = CCSpriteSheet.spriteSheet("cycle_body.png", 100);
        layer.addChild(ssBycBody, 0, BYCYCLE_BODY);
        CCSpriteSheet ssBycTireFront = CCSpriteSheet.spriteSheet("cycle_tire.png", 100);
        layer.addChild(ssBycTireFront, 0, BYCYCLE_TIRE);
		
        makeBycycle(mWorld, point, size);
	}

	public void makeBycycle(
			World mWorld, CGPoint point, float size){

		//�Ƃ肠�������S�_
		//CGPoint point = cPoint;
		
		/////////////////////
		//�X�v���C�g�V�[�g�̒�`
		CCSpriteSheet sheet = (CCSpriteSheet)layer.getChildByTag(BYCYCLE_BODY);
		CGRect rect         = CGRect.make(0, 0, 185, 110);
		CCSprite sprite     = CCSprite.sprite(sheet, rect);
		sheet.addChild(sprite);
		sprite.setPosition(point);

		//�{�f�B�̒�`
		BodyDef mBodyDef = new BodyDef();
		mBodyDef.type = BodyType.DynamicBody;
		//�p�x�����W�A���Œ�`
		mBodyDef.angle = (float)(0 * Math.PI/180);
		//���W�w��
		mBodyDef.position.set(point.x/ptmRatio, point.y/ptmRatio);
		//�{�f�B�̌`���`
		float dw = size;//���[�g���P��
		float dh = (rect.size.height/rect.size.width) * size;//���[�g���P��
		PolygonShape mPolygonShape = new PolygonShape();
		mPolygonShape.setAsBox(dw/2, dh/2);//�����̑傫�����w�肷��K�v������
		
		//�ڑ��ʒu�̈ʒu�����Ŏg�p����
		float cRatio = (dw * ptmRatio) / rect.size.width;
		
		//�{�f�B�̌`�ɍ��킹�ăX�v���C�g���g��k��(1.1f�����傫�߂ɂ��Ă���)
		sprite.setScale(cRatio * 1.1f);
		
		/////////////////////
		//�^�C���̃X�v���C�g�V�[�g
		//�^�C���O��
		CCSpriteSheet tireSheet = (CCSpriteSheet)layer.getChildByTag(BYCYCLE_TIRE);
		CGRect tireRect = CGRect.make(0, 0, 80, 80);
		CCSprite tireSpriteF = CCSprite.sprite(tireSheet, tireRect);
		tireSheet.addChild(tireSpriteF);
		//tireSpriteF.setPosition(point);
		
		//�^�C���̃{�f�B�̒�`
		BodyDef tireBodyDefF = new BodyDef();
		tireBodyDefF.type = BodyType.DynamicBody;
		//�p�x�����W�A���Œ�`
		tireBodyDefF.angle = (float)(0 * Math.PI/180);
		//���W�w��
		tireBodyDefF.position.set(
				(point.x+90*cRatio)/ptmRatio,
				(point.y-30*cRatio)/ptmRatio);
		//�^�C���̃{�f�B�̌`���`
		float tw = (tireRect.size.width/rect.size.width) * dw;//�rect�̊�������Z�o
		CircleShape tireShape = new CircleShape();
		tireShape.setRadius(tw/2);//���a

		//�{�f�B�̌`�ɍ��킹�ăX�v���C�g���g��k��(1.1f�����傫�߂ɂ��Ă���)
		tireSpriteF.setScale(cRatio * 1.1f);

		/////////////////////
		//�^�C���̃X�v���C�g�V�[�g
		//�^�C�����
		CCSprite tireSpriteR = CCSprite.sprite(tireSheet, tireRect);
		tireSheet.addChild(tireSpriteR);
		tireSpriteR.setPosition(point);
		
		//�^�C���̃{�f�B�̒�`
		BodyDef tireBodyDefR = new BodyDef();
		tireBodyDefR.type = BodyType.DynamicBody;
		//�p�x�����W�A���Œ�`
		tireBodyDefR.angle = (float)(0 * Math.PI/180);
		//���W�w��
		tireBodyDefR.position.set(
				(point.x-55*cRatio)/ptmRatio,
				(point.y-30*cRatio)/ptmRatio);

		//�{�f�B�̌`�ɍ��킹�ăX�v���C�g���g��k��(1.1f�����傫�߂ɂ��Ă���)
		tireSpriteR.setScale(cRatio * 1.1f);
		
		synchronized(mWorld){
			//bodyList
			bodyList = new ArrayList<Body>();
			
			////////////////
			//�{��
			Body mBody = mWorld.createBody(mBodyDef);
			mBody.setUserData(sprite);
			
			//�t�B�N�X�`�����`
			FixtureDef mFixtureDef = new FixtureDef();
			mFixtureDef.shape    = mPolygonShape;
			mFixtureDef.density  = 1.0f;
			mFixtureDef.friction = 10.0f;
			mBody.createFixture(mFixtureDef);

			//bodyList�ɒǉ�
			bodyList.add(mBody);
			
			////////////////
			//�^�C���̃{�f�B�O��
			Body tBodyF = mWorld.createBody(tireBodyDefF);
			tBodyF.setUserData(tireSpriteF);
			//�^�C���̃t�B�N�X�`��
			FixtureDef tireFixtureDef = new FixtureDef();
			tireFixtureDef.shape    = tireShape;
			tireFixtureDef.density  = 5.0f;
			tireFixtureDef.friction = 10.0f;
			tBodyF.createFixture(tireFixtureDef);

			//�����H���g�W���C���g
			//�{�̂ƃ^�C�����q����
			RevoluteJointDef rJointDefF = new RevoluteJointDef();
			rJointDefF.initialize(tBodyF, mBody, tBodyF.getWorldCenter());
			//rJointDef.enableMotor    = true;
			//rJointDef.motorSpeed     = 10.0f;
			//rJointDef.maxMotorTorque = 10.0f;//�i���I�ȉ�]
			//rJointDef.enableLimit = true;
			//rJointDef.lowerAngle  = (float)(0f * Math.PI/180);
			//rJointDef.upperAngle  = (float)(90f * Math.PI/180);
			mWorld.createJoint(rJointDefF);
			
			//bodyList�ɒǉ�
			bodyList.add(tBodyF);
			
			////////////////
			//�^�C���̃{�f�B���
			tBodyR = mWorld.createBody(tireBodyDefR);
			tBodyR.setUserData(tireSpriteR);
			//�^�C���̃t�B�N�X�`��
			tBodyR.createFixture(tireFixtureDef);

			//�����H���g�W���C���g
			//�{�̂ƃ^�C�����q����
			RevoluteJointDef rJointDefR = new RevoluteJointDef();
			rJointDefR.initialize(tBodyR, mBody, tBodyR.getWorldCenter());
			//rJointDef.enableMotor    = true;
			//rJointDef.motorSpeed     = 10.0f;
			//rJointDef.maxMotorTorque = 10.0f;//�i���I�ȉ�]
			//rJointDef.enableLimit = true;
			//rJointDef.lowerAngle  = (float)(0f * Math.PI/180);
			//rJointDef.upperAngle  = (float)(90f * Math.PI/180);
			mWorld.createJoint(rJointDefR);
			
			//bodyList�ɒǉ�
			bodyList.add(tBodyR);
		}
	}

	//�i�s
	public void forward(){
		//�^�C���ɏ�����^����
		//tBodyR.applyAngularImpulse(-300.0f);//�i���I�ȉ�]
		tBodyR.applyTorque(-1.0f);//�����I�ȉ�]
	}
	
	//���
	public void backward(){
		//�^�C���ɏ�����^����
		//tBodyR.applyAngularImpulse(-300.0f);//�i���I�ȉ�]
		tBodyR.applyTorque(+1.0f);//�����I�ȉ�]
	}

	//�Փ˔���
	public boolean isHit(Body target){
		return false;
	}
	
	//���[���h����폜
	public void delete(World mWorld){
		Log.d(TAG, "delete");
		if(bodyList.size() > 0){
			for(Body mBody:bodyList){
				Object userData = mBody.getUserData();
				if(userData != null && userData instanceof CCSprite){
					
					final CCSprite mSprite = (CCSprite)userData;
					//�폜
					mWorld.destroyBody(mBody);
					mSprite.removeSelf();
				}
			}
			bodyList = null;
		}
	}
}
