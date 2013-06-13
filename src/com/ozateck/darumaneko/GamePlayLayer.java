package com.ozateck.darumaneko;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;
import java.lang.Math;

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
import org.cocos2d.sound.SoundEngine;
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

public class GamePlayLayer extends CCLayer
					implements SensorEventListener{
	
	private static final String TAG = "myTag";
	
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
	
	//�������
	private World mWorld;
	//�d�͂̕���
	private Vector2 mGravity;

	//FPS
	private float FPS = 
			(float)CCDirector.sharedDirector().getAnimationInterval();
	private float rdelta = 0;
	
	//�L���X�g�A����܃��X�g
	private CCSprite      bgSprite;
	private CCSprite      groundSprite;
	private List<Cat>     catList;
	private List<Daruma>  darumaList;
	
	//�{�^��
	private NormaBtn btnS;
	private NormaBtn btnM;
	private NormaBtn btnL;
	
	private static final int TAG_NONE   = 0;
	private static final int TAG_SMALL  = 1;
	private static final int TAG_MIDDLE = 2;
	private static final int TAG_LARGE  = 3;
	private static final float sizeS = 0.06f;
	private static final float sizeM = 0.08f;
	private static final float sizeL = 0.10f;
	private static final int  pointS = 10;
	private static final int  pointM = 15;
	private static final int  pointL = 20;
	private int countS = 0;
	private int countM = 0;
	private int countL = 0;
	
	//�X�R�A�o�[
	private ScoreBar scoreBar;
	private int score = 0;
	private int time  = 30;
	
	//�~�o�[
	private BatuBar batuBar;
	private int batuCount = 0;//�~�̏�����
	private int batuLimit = 5;//�~�̐�
	
	//�h�b�g���C���̒n�ʂ���̍���
	private static final float dotlineHeight = 0.7f;
	
	//sensor
	private SensorManager sensorManager;
	private Sensor        acceleroS;
	private float[]       values = new float[3];
	
	public GamePlayLayer(Context context){
		
		this.context = context;

		//libs/armeabi/libgdx.so��ǂݍ���//
		System.loadLibrary("gdx");
		
		//�^�b�`�A�N�V������L���ɂ���
		setIsTouchEnabled(true);
		
		/*
		 * ���[���h�Ŏg�p��������`
		 */
		
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
		
		/*
		 * ���[���h���`
		 */
		
		//���[���h�̐���
		mGravity = new Vector2(0.0f, -9.8f);
		float scaleWidth  = dispSize.width  / ptmRatio;
		float scaleHeight = dispSize.height / ptmRatio;
		
		Log.d(TAG, "scaleWidth,scaleHeight:" + scaleWidth + "_" + scaleHeight);
		
		//������Ԃ̐���
		mWorld = new World(mGravity, true);
		mWorld.setContinuousPhysics(true);
		
		//������Ԃ̃O�����h�̐ݒ�
		BodyDef mGroundBodyDef = new BodyDef();
		mGroundBodyDef.position.set(0.0f, 0.0f);
		
		//������Ԃ̃O�����h��Body�̐ݒ�
		Body mGroundBody = mWorld.createBody(mGroundBodyDef);
		
		//������Ԃ̃O�����h�̌`�𐶐�
		PolygonShape mGroundShape = new PolygonShape();
		
		//������Ԃ̋��E�����`
		float expandSize    = WORLD_WIDTH_METER * 0.2f;
		Vector2 bottomLeft  = new Vector2(0f-expandSize, 0f);
		Vector2 topLeft     = new Vector2(0f-expandSize, scaleHeight);
		Vector2 topRight    = new Vector2(scaleWidth+expandSize, scaleHeight);
		Vector2 bottomRight = new Vector2(scaleWidth+expandSize, 0f);
		
		//������Ԃ̉����E
		mGroundShape.setAsEdge(bottomLeft, bottomRight);
		mGroundBody.createFixture(mGroundShape, 0);
		
		//������Ԃ̏㋫�E
		mGroundShape.setAsEdge(topLeft, topRight);
		mGroundBody.createFixture(mGroundShape, 0);
		
		//������Ԃ̍����E
		mGroundShape.setAsEdge(topLeft,  bottomLeft);
		mGroundBody.createFixture(mGroundShape, 0);
		
		//������Ԃ̉E���E
		mGroundShape.setAsEdge(topRight, bottomRight);
		mGroundBody.createFixture(mGroundShape, 0);
		
        //�X�e�[�W����
        makeStage();
        
        //�L���X�g
        catList = new ArrayList<Cat>();
        CGPoint nPoint = CCDirector.sharedDirector().convertToGL(
				CGPoint.make(dispSize.width/2, dispSize.height*4/5));
        Cat cat = new Cat(this, ptmRatio, mWorld, nPoint, 0.28f);
        catList.add(cat);
        
        //����܃��X�g
        darumaList = new ArrayList<Daruma>();
        //Daruma daruma = new Daruma(this, ptmRatio, mWorld, cPoint, 0.05f);
        //darumaList.add(daruma);
        
        //�m���}�{�^��
        btnS = new NormaBtn(this, ptmRatio, 0.2f, 0.36f, 0.3f);
        btnS.setString("Small:" + countS);
        btnS.on();
        btnM = new NormaBtn(this, ptmRatio, 0.2f, 0.24f, 0.3f);
        btnM.setString("Middle:" + countM);
        btnM.off();
        btnL = new NormaBtn(this, ptmRatio, 0.2f, 0.12f, 0.3f);
        btnL.setString("Large:" + countL);
        btnL.off();
        
        //�X�R�A�o�[
        scoreBar = new ScoreBar(this, ptmRatio, 
        		WORLD_WIDTH_METER/2,
        		dispSize.height / dispSize.width * WORLD_WIDTH_METER,
        		WORLD_WIDTH_METER, score, time);
        scoreBar.setScore(score);
        
        //�~�o�[
        batuBar = new BatuBar(this, ptmRatio,
        			WORLD_WIDTH_METER/2+0.20f, 0.12f, 0.05f, batuLimit);
        batuBar.setCount(batuCount);
        
        /*
         * �Z���T�[�̏���
         */
        
		//sensor
		sensorManager = 
				(SensorManager) CCDirector.sharedDirector().
				getActivity().getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> list;
		list = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);//�����x�Z���T�[
		if(list.size() > 0) acceleroS = list.get(0);
		
		if(acceleroS != null){
			sensorManager.registerListener(this,
					acceleroS, SensorManager.SENSOR_DELAY_FASTEST);
		}
	}
	
	//�X�e�[�W����
	private void makeStage(){

		//�w�i�̃X�v���C�g�V�[�g(��ʈ�t�ɍL����)
		bgSprite = CCSprite.sprite("back_game.png", 
								CGRect.make(0, 0, 480, 800));
		bgSprite.setScaleX(dispSize.width  / 480);
		bgSprite.setScaleY(dispSize.height / 800);
        bgSprite.setPosition(CGPoint.make(dispSize.width/2, dispSize.height/2));
        addChild(bgSprite, 0);
        
        //���C���𐶐�
        makeLine();
        
        //�n�ʐ���
        makeGround();
	}
	
	private void makeLine(){
		//�X�v���C�g�̐ݒ�(dotline)
		CGRect rect = CGRect.make(0, 0, 480, 5);
		CCSprite sprite = CCSprite.sprite("dotline.png", rect);
		sprite.setPosition(dispSize.width/2, dotlineHeight * ptmRatio);
		addChild(sprite, 0);
	}
	
	private void makeGround(){
		//�폜�o�[�̒��S�_
		CGPoint point = 
				CCDirector.sharedDirector().convertToGL(
						CGPoint.make(dispSize.width/2, dispSize.height));

		//�X�v���C�g�̐ݒ�
		CGRect rect  = CGRect.make(0, 0, 480, 30);
		groundSprite = CCSprite.sprite("ground.png", rect);
		groundSprite.setPosition(point);
		addChild(groundSprite, 0);
		
		//�_�C�i�~�b�N�{�f�B�̒�`
		BodyDef mBodyDef = new BodyDef();
		mBodyDef.type = BodyType.StaticBody;
		//�p�x�����W�A���Œ�`
		mBodyDef.angle = (float)(0 * Math.PI/180);
		//���W�w��
		mBodyDef.position.set(point.x/ptmRatio, point.y/ptmRatio);
		//�{�f�B�̌`���`
		float dw = WORLD_WIDTH_METER;//������t
		float dh = (rect.size.height/rect.size.width) * dw;//���[�g���P��
		PolygonShape mPolygonShape = new PolygonShape();
		mPolygonShape.setAsBox(dw/2, dh/2);//�����̑傫�����w�肷��K�v������
		
		//�ڑ��ʒu�̈ʒu�����Ŏg�p����
		float cRatio = (dw * ptmRatio) / rect.size.width;
		
		//�{�f�B�̌`�ɍ��킹�ăX�v���C�g���g��k��(1.1f�����傫�߂ɂ��Ă���)
		groundSprite.setScale(cRatio * 1.4f);
		
		synchronized(mWorld){
			//�_�C�i�~�b�N�{�f�B��fixture���`
			Body mBody = mWorld.createBody(mBodyDef);
			mBody.setUserData(groundSprite);

			//�t�B�N�X�`�����`
			FixtureDef mFixtureDef = new FixtureDef();
			mFixtureDef.shape = mPolygonShape;
			mFixtureDef.density = 0.1f;
			mFixtureDef.friction = 0.1f;
			
			mBody.createFixture(mFixtureDef);
		}
	}
	
	@Override
	public void onEnter(){
		super.onEnter();
		schedule("tick");//�X�P�W���[���̊J�n
		schedule("gameTick", 1.0f);//�Q�[�����Ԃ̊J�n
		//BGM�ĊJ
		//SoundEngine.sharedEngine().playSound(context, R.raw.bgm_play, true);
	}
	
	@Override
	public void onExit(){
		super.onExit();
		unschedule("tick");//�X�P�W���[���̉���
		unschedule("gameTick");//�Q�[�����Ԃ̉���
		//BGM��~
		//SoundEngine.sharedEngine().pauseSound();
	}
	
	//����Ăяo����鏈��
	public synchronized void tick(float delta){
		//FPS�̊��o���Z���ꍇ�̓X�L�b�v
		if((rdelta += delta) < FPS){
			return;
		}else{
			//mWorld��i�߂�
			synchronized(mWorld){
				mWorld.step(FPS, 8, 1);
			}
			rdelta = 0;
		}
		
		//������Ԃ̒��ɂ���Body��Iterator���A�\���ʒu�𔽉f������
		Iterator<Body> it = mWorld.getBodies();
		while(it.hasNext()){
			Body body = it.next();
			Object userData = body.getUserData();
			
			if(userData != null && userData instanceof CCSprite){
				final CCSprite sprite = (CCSprite)userData;
				final Vector2  pos    = body.getPosition();
				sprite.setPosition(pos.x*ptmRatio, pos.y*ptmRatio);
				sprite.setRotation(
						-1.0f*ccMacros.CC_RADIANS_TO_DEGREES(body.getAngle()));
			}
		}
		
		//darumaList�̍폜����
		//�����̎�̍폜���@�́A�g��for���ł͎g���Ă͂����Ȃ��B
		for(int i=0; i<darumaList.size(); i++){
			Daruma daruma = darumaList.get(i);
			if(daruma.isHit(groundSprite.getBoundingBox())){
				synchronized(darumaList){
					darumaList.remove(i);
				}
				synchronized(mWorld){
					daruma.delete(mWorld);
				}

				switch(daruma.getTag()){
				case TAG_SMALL:
					countS--;
					btnS.setString("Small:" + countS);
					score -= pointS;
					SoundEngine.sharedEngine().playEffect(context, R.raw.dead);
					break;
				case TAG_MIDDLE:
					countM--;
					btnM.setString("Middle:" + countM);
					score -= pointM;
					SoundEngine.sharedEngine().playEffect(context, R.raw.dead);
					break;
				case TAG_LARGE:
					countL--;
					btnL.setString("Large:" + countL);
					score -= pointL;
					SoundEngine.sharedEngine().playEffect(context, R.raw.dead);
					break;
				}
				scoreBar.setScore(score);
				
				//�~�o�[�̍X�V
				batuCount++;
				if(batuCount > batuLimit){
					gameOver();
				}else{
					batuBar.setCount(batuCount);
				}
			}
		}
		
	}
	
	//�Q�[���^�C�}�[�̏���
	public synchronized void gameTick(float delta){
		time--;
		if(time < 0){
			gameResult();
		}else{
			scoreBar.setTime(time);
		}
	}
	
	@Override
	public boolean ccTouchesBegan(MotionEvent event){

		//�^�b�`���ꂽ���W
		CGPoint point = 
				CCDirector.sharedDirector().convertToGL(
						CGPoint.make(event.getX(), event.getY()));
		
		if(btnS.isInside(point)){
			btnS.on();
			btnM.off();
			btnL.off();
			SoundEngine.sharedEngine().playEffect(context, R.raw.btn_go);
		}else if(btnM.isInside(point)){
			btnS.off();
			btnM.on();
			btnL.off();
			SoundEngine.sharedEngine().playEffect(context, R.raw.btn_go);
		}else if(btnL.isInside(point)){
			btnS.off();
			btnM.off();
			btnL.on();
			SoundEngine.sharedEngine().playEffect(context, R.raw.btn_go);
		}
		
		return CCTouchDispatcher.kEventHandled;
	}
	
	@Override
	public boolean ccTouchesEnded(MotionEvent event){
		
		//�^�b�`���ꂽ���W
		CGPoint point = 
				CCDirector.sharedDirector().convertToGL(
						CGPoint.make(event.getX(), event.getY()));
		
		if(point.y > dotlineHeight * ptmRatio){
			//����ܒǉ�
			Daruma daruma;
			if(btnS.isActive()){
				daruma = new Daruma(this, ptmRatio, mWorld, point, sizeS);
				daruma.setTag(TAG_SMALL);
				darumaList.add(daruma);
				countS++;
				btnS.setString("Small:" + countS);
				score += pointS;
				SoundEngine.sharedEngine().playEffect(context, R.raw.birth);
			}else if(btnM.isActive()){
				daruma = new Daruma(this, ptmRatio, mWorld, point, sizeM);
				daruma.setTag(TAG_MIDDLE);
				darumaList.add(daruma);
				countM++;
				btnM.setString("Middle:" + countM);
				score += pointM;
				SoundEngine.sharedEngine().playEffect(context, R.raw.birth);
			}else if(btnL.isActive()){
				daruma = new Daruma(this, ptmRatio, mWorld, point, sizeL);
				daruma.setTag(TAG_LARGE);
				darumaList.add(daruma);
				countL++;
				btnL.setString("Large:" + countL);
				score += pointL;
				SoundEngine.sharedEngine().playEffect(context, R.raw.birth);
			}
			scoreBar.setScore(score);
		}
		
		return CCTouchDispatcher.kEventHandled;
	}
	
	/*
     * �Z���T�[�̃��\�b�h
     */
	@Override
	public void onSensorChanged(SensorEvent event){
		//sensor//
		if(event.sensor == acceleroS){
			values[0] = event.values[0];//x
			values[1] = event.values[1];//y
			values[2] = event.values[2];//z
			
			//mGravity.set(values[0]*-1, values[1]*-1);//PORTLAIT�p
			//mGravity.set(values[1], values[0]*-1);//LANDSCAPE�p
			//mWorld.setGravity(mGravity);
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy){
		//nothing
	}
	
	private void gameResult(){
		//GameResult��
		CCScene scene = CCScene.node();
		CCLayer layer = new GameResultLayer(context,
							score, countS, countM, countL);
		scene.addChild(layer);
		CCDirector.sharedDirector().replaceScene(scene);
	}
	
	private void gameOver(){
		//GameOver��
		CCScene scene = CCScene.node();
		CCLayer layer = new GameOverLayer(context);
		scene.addChild(layer);
		CCDirector.sharedDirector().replaceScene(scene);
	}
}
