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

public class NormaBtn{
	
	private static final String TAG = "myTag";
	
	private final CCLayer    layer;
	private final int        ptmRatio;
	
	private boolean active;
	
	private static final int BACK_BTN = 1;
	private CCSprite btnSprite;
	private CCLabel  label;
	
	//トグルボタンx,yはメートル換算
	public NormaBtn(CCLayer layer, int ptmRatio,
						float x, float y, float size){
		
		this.layer    = layer;
		this.ptmRatio = ptmRatio;
		
		active = false;
		
		makeToggleBtn(x, y, size);
	}
	
	private void makeToggleBtn(float x, float y, float size){
		
		/////////////////////
        //ボタンのスプライトシート
		CCSpriteSheet ssBack = CCSpriteSheet.spriteSheet("btn_norm.png", 100);
		layer.addChild(ssBack, 1, BACK_BTN);
		
		CGRect rect = CGRect.make(0, 0, 130, 50);
		
		btnSprite = new CCSprite();
		btnSprite.setTexture(ssBack.getTexture());
		btnSprite.setTextureRect(rect);
		
		float bw = size * ptmRatio;//メートル換算でのピクセル数
		float bh = (rect.size.height / rect.size.width) * bw;
		
		btnSprite.setScaleX(bw/rect.size.width);
		btnSprite.setScaleY(bh/rect.size.height);
		
		btnSprite.setPosition(x * ptmRatio, y * ptmRatio);
		
		layer.addChild(btnSprite, 0);
		
		/////////////////////
		//ラベルの追加
		//dw=144でフォントサイズ30(だいたい5分の1)
		int txSize = (int)(bw/5-2);
		label = CCLabel.makeLabel("Xsize:00", "Pollyanna.ttf", txSize);
		label.setColor(ccColor3B.ccBLACK);
		label.setPosition((x-0.12f) * ptmRatio, (y-0.03f) * ptmRatio);
		label.setAnchorPoint(0f, 0f);
		layer.addChild(label, 1);
	}
	
	public void on(){
		if(!active){
			active = true;
			CGRect activeRect = CGRect.make(0, 50, 130, 50);
			btnSprite.setTextureRect(activeRect);
		}
	}
	
	public void off(){
		if(active){
			active = false;
			CGRect inactiveRect = CGRect.make(0, 0, 130, 50);
			btnSprite.setTextureRect(inactiveRect);
		}
	}
	
	public void changeToggle(){
		if(!active){
			active = true;
			CGRect activeRect = CGRect.make(0, 50, 130, 50);
			btnSprite.setTextureRect(activeRect);
		}else{
			active = false;
			CGRect inactiveRect = CGRect.make(0, 0, 130, 50);
			btnSprite.setTextureRect(inactiveRect);
		}
	}
	
	public boolean isInside(CGPoint point){
		CGRect rect = btnSprite.getBoundingBox();
		if(rect.contains(point.x, point.y)){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean isActive(){
		return active;
	}
	
	public void setString(String str){
		CharSequence cs = str;
		label.setString(cs);
	}
}