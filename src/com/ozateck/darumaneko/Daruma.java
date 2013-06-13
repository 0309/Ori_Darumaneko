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

		//�Ƃ肠�������S�_
		//CGPoint point = cPoint;
		
		/////////////////////
        //����܂̃X�v���C�g
		CGRect rect     = CGRect.make(0, 0, 80, 80);
        CCSprite sprite = CCSprite.sprite("daruma.png", rect);
        layer.addChild(sprite, 1);
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
		sprite.setScale(cRatio * 1.5f);
		
		synchronized(mWorld){
			//bodyList
			bodyList = new ArrayList<Body>();
			
			////////////////
			//�{��
			mBody = mWorld.createBody(mBodyDef);
			mBody.setUserData(sprite);
			
			//�t�B�N�X�`�����`
			FixtureDef mFixtureDef = new FixtureDef();
			mFixtureDef.shape    = mPolygonShape;
			mFixtureDef.density  = 1.0f;
			mFixtureDef.friction = 5.0f;
			mBody.createFixture(mFixtureDef);

			//bodyList�ɒǉ�
			bodyList.add(mBody);
		}
	}
	
	//tag���Z�b�g
	public void setTag(int tag){
		this.tag = tag;
	}
	
	//tag���Q�b�g
	public int getTag(){
		return tag;
	}

	//�Փ˔���
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
