package com.ozateck.darumaneko;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;
import android.view.MotionEvent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.util.Log;

import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCLabel;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.sound.SoundEngine;
import org.cocos2d.types.CGPoint;
import org.cocos2d.events.CCTouchDispatcher;
import org.cocos2d.opengl.CCGLSurfaceView;

public class MainActivity extends Activity{
	
	public static final String TAG = "myTag";
	
	@Override
	public void onCreate(Bundle icicle){
		super.onCreate(icicle);
		Log.d(TAG, "onCreate()");
		
		//window setting
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//縦
		
		//glsurfaceview
		CCGLSurfaceView mCCGLSurfaceView = new CCGLSurfaceView(this);
		CCDirector.sharedDirector().attachInView(mCCGLSurfaceView);
		setContentView(mCCGLSurfaceView);
		
		//show FPS
		CCDirector.sharedDirector().setDisplayFPS(false);
		
		//GameTopへ
		CCScene scene = CCScene.node();
		CCLayer layer = new GameTopLayer(this);
		scene.addChild(layer);
		
		//frames per second
		CCDirector.sharedDirector().setAnimationInterval(1.0f / 60);
		CCDirector.sharedDirector().runWithScene(scene);
		
		//Sounds
		SoundEngine.sharedEngine().preloadSound(this, R.raw.gameplay);
		SoundEngine.sharedEngine().preloadSound(this, R.raw.bgm_ready);
		SoundEngine.sharedEngine().preloadSound(this, R.raw.bgm_play);
		SoundEngine.sharedEngine().preloadSound(this, R.raw.bgm_result);
		SoundEngine.sharedEngine().preloadSound(this, R.raw.bgm_over);
		SoundEngine.sharedEngine().setSoundVolume(0.5f);
		SoundEngine.sharedEngine().preloadEffect(this, R.raw.btn_go);
		SoundEngine.sharedEngine().preloadEffect(this, R.raw.btn_back);
		SoundEngine.sharedEngine().preloadEffect(this, R.raw.birth);
		SoundEngine.sharedEngine().preloadEffect(this, R.raw.dead);
		SoundEngine.sharedEngine().setEffectsVolume(0.2f);
	}
	
	@Override
	public void onStart(){
		super.onStart();
		Log.d(TAG, "onStart()");
	}
	
	@Override
	public void onResume(){
		super.onResume();
		Log.d(TAG, "onResume()");
		CCDirector.sharedDirector().resume();
	}
	
	@Override
	public void onPause(){
		super.onPause();
		Log.d(TAG, "onPause()");
		CCDirector.sharedDirector().pause();//エラーが起こる
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		Log.d(TAG, "onDestroy()");
		CCDirector.sharedDirector().end();//エラーが起こる
		SoundEngine.sharedEngine().realesAllSounds();
		SoundEngine.sharedEngine().realesAllEffects();
	}
}