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
		
		//�L�̃X�v���C�g�V�[�g(�{�́A�ԗցA�o�[)
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
		
		//��肠�������S�_
		//CGPoint point = cPoint;
		
		/////////////////////
		//�X�v���C�g�V�[�g�̒�`
		CCSpriteSheet sheet = (CCSpriteSheet)layer.getChildByTag(CAT);
		CGRect rect         = CGRect.make(0, 0, 200, 210);
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
		float dh = (rect.size.height/rect.size.width) * dw;//���[�g���P��
		PolygonShape mPolygonShape = new PolygonShape();
		mPolygonShape.setAsBox(dw/2, dh/2);//�����̑傫�����w�肷��K�v������
		
		//�ڑ��ʒu�̈ʒu�����Ŏg�p����
		float cRatio = (dw * ptmRatio) / rect.size.width;
		
		//�{�f�B�̌`�ɍ��킹�ăX�v���C�g���g��k��(1.1f�����傫�߂ɂ��Ă���)
		sprite.setScale(cRatio * 1.1f);
		
		/////////////////////
		//�o�[�̃X�v���C�g�V�[�g
		//�o�[
		CCSpriteSheet barSheet = (CCSpriteSheet)layer.getChildByTag(BAR);
		CGRect barRect = CGRect.make(0, 0, 400, 20);
		CCSprite barSprite = CCSprite.sprite(barSheet, barRect);
		barSheet.addChild(barSprite);
		//barSpriteF.setPosition(point);
		
		//�o�[�̃{�f�B�̒�`
		BodyDef barBodyDef = new BodyDef();
		barBodyDef.type = BodyType.DynamicBody;
		//�p�x�����W�A���Œ�`
		barBodyDef.angle = (float)(0 * Math.PI/180);
		//���W�w��
		barBodyDef.position.set(
				(point.x+0*cRatio)/ptmRatio,
				(point.y+120*cRatio)/ptmRatio);
		//�o�[�̃{�f�B�̌`���`
		float bw = (barRect.size.width/rect.size.width) * dw;//�rect�̊�������Z�o
		float bh = (barRect.size.height/barRect.size.width) * bw;
		PolygonShape barPolygonShape = new PolygonShape();
		barPolygonShape.setAsBox(bw/2, bh/2);//�����̑傫���Ɏw�肷��K�v������
		
		//�{�f�B�̌`�ɍ��킹�ăX�v���C�g���g��k��(1.1f�����傫�߂Ɏw�肵�Ă���);
		barSprite.setScale(cRatio * 1.1f);
		
		/////////////////////
		//�^�C���̃X�v���C�g�V�[�g
		//�^�C���O��
		CCSpriteSheet tireSheet = (CCSpriteSheet)layer.getChildByTag(TIRE);
		CGRect tireRect = CGRect.make(0, 0, 155, 155);
		CCSprite tireSprite = CCSprite.sprite(tireSheet, tireRect);
		tireSheet.addChild(tireSprite);
		//tireSprite.setPosition(point);
		
		//�^�C���̃{�f�B�̒�`
		BodyDef tireBodyDef = new BodyDef();
		tireBodyDef.type = BodyType.DynamicBody;
		//�p�x�����W�A���Œ�`
		tireBodyDef.angle = (float)(0 * Math.PI/180);
		//���W�w��
		tireBodyDef.position.set(
				(point.x+0*cRatio)/ptmRatio,
				(point.y-190*cRatio)/ptmRatio);
		//�^�C���̃{�f�B�̌`���`
		float tw = (tireRect.size.width/rect.size.width) * dw;//�rect�̊�������Z�o
		float th = tw;
		//CircleShape tireShape = new CircleShape();
		//tireShape.setRadius(tw/2);//���a
		PolygonShape tireShape = new PolygonShape();
		tireShape.setAsBox(tw/2, th/2);

		//�{�f�B�̌`�ɍ��킹�ăX�v���C�g���g��k��(1.1f�����傫�߂ɂ��Ă���)
		tireSprite.setScale(cRatio * 1.1f);
		
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
			mFixtureDef.friction = 30.0f;
			mBody.createFixture(mFixtureDef);

			//bodyList�ɒǉ�
			bodyList.add(mBody);
			
			////////////////
			//�o�[�̃{�f�B
			Body barBody = mWorld.createBody(barBodyDef);
			barBody.setUserData(barSprite);
			//�o�[�̃t�B�N�X�`��
			FixtureDef barFixtureDef = new FixtureDef();
			barFixtureDef.shape    = barPolygonShape;
			barFixtureDef.density  = 5.0f;
			barFixtureDef.friction = 30.0f;
			barBody.createFixture(barFixtureDef);

			//�����H���g�W���C���g
			//�{�̂ƃo�[���q����
			RevoluteJointDef rJointDefB = new RevoluteJointDef();
			rJointDefB.initialize(barBody, mBody, barBody.getWorldCenter());
			//rJointDef.enableMotor    = true;
			//rJointDef.motorSpeed     = 10.0f;
			//rJointDef.maxMotorTorque = 10.0f;//�i���I�ȉ�]
			rJointDefB.enableLimit = true;
			rJointDefB.lowerAngle  = (float)(-4f * Math.PI/180);
			rJointDefB.upperAngle  = (float)(+4f * Math.PI/180);
			mWorld.createJoint(rJointDefB);
			
			//bodyList�ɒǉ�
			bodyList.add(barBody);
			
			////////////////
			//�^�C���̃{�f�B�O��
			Body tireBody = mWorld.createBody(tireBodyDef);
			tireBody.setUserData(tireSprite);
			//�^�C���̃t�B�N�X�`��
			FixtureDef tireFixtureDef = new FixtureDef();
			tireFixtureDef.shape    = tireShape;
			tireFixtureDef.density  = 5.0f;
			tireFixtureDef.friction = 30.0f;
			tireBody.createFixture(tireFixtureDef);

			//�����H���g�W���C���g
			//�{�̂ƃ^�C�����q����
			RevoluteJointDef rJointDefT = new RevoluteJointDef();
			rJointDefT.initialize(tireBody, mBody, tireBody.getWorldCenter());
			//rJointDef.enableMotor    = true;
			//rJointDef.motorSpeed     = 10.0f;
			//rJointDef.maxMotorTorque = 10.0f;//�i���I�ȉ�]
			rJointDefT.enableLimit = true;
			rJointDefT.lowerAngle  = (float)(-2f * Math.PI/180);
			rJointDefT.upperAngle  = (float)(+2f * Math.PI/180);
			mWorld.createJoint(rJointDefT);
			
			//bodyList�ɒǉ�
			bodyList.add(tireBody);
		}
	}

	//�i�s
	public void forward(){
		//�^�C���ɏ�����^����
		//tBodyR.applyAngularImpulse(-300.0f);//�i���I�ȉ�]
		//tBodyR.applyTorque(-1.0f);//�����I�ȉ�]
	}
	
	//���
	public void backward(){
		//�^�C���ɏ�����^����
		//tBodyR.applyAngularImpulse(-300.0f);//�i���I�ȉ�]
		//tBodyR.applyTorque(+1.0f);//�����I�ȉ�]
	}
	
	//����
	public void setForce(float x, float y){
		if(bodyList.size() > 0){
			Body mBody = bodyList.get(0);
			Vector2 force = new Vector2(x, y);
	        mBody.applyLinearImpulse(force, mBody.getPosition());
		}
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
