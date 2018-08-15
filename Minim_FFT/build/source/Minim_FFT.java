import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import codeanticode.syphon.*; 
import netP5.*; 
import oscP5.*; 
import ddf.minim.*; 
import ddf.minim.analysis.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Minim_FFT extends PApplet {








SyphonServer server; // syphonクラスのインスタンス

OscP5 oscP5; //OSCP5クラスのインスタンス
NetAddress myRemoteLocation;
int receivePort = 8000;
String sendIP = "192.168.3.8";
int sendPort = 9000;

Minim minim;
AudioPlayer player; //サウンドプレイヤー
AudioInput in;  //マイク入力用の変数
FFT fft; //FFTクラス
int fftSize  = 2048; //FFTのサイズ
float[] rot = new float[fftSize]; //現在の角度を保存
float[] rotSpeed  = new float[fftSize]; //角速度

public void settings() {
  size(1280, 720, P3D);
  PJOGL.profile=1;

  oscP5 = new OscP5(this,receivePort);
  myRemoteLocation = new NetAddress(sendIP,sendPort);
}

public void setup() {
  frameRate(60);

  minim = new Minim(this);
  //バッファ（メモリ上のスペース。この場合は2048要素のfloat型の配列）を確保し、マイク入力用の変数inを設定する。
  in = minim.getLineIn(Minim. STEREO, 2048);

  server = new SyphonServer(this, "Processing Syphon");

  noStroke();
  //混色は加算合成
  blendMode(ADD);
  //色はHSBで指定
  colorMode(HSB, 360, 100, 100, 100);
  //FFTオブジェクトの生成
  //fft = new FFT(player.bufferSize(), player.sampleRate());
  fft = new FFT(in.bufferSize(), in.sampleRate());
  // 角度と角速度を初期化
  for (int i = 0; i < fftSize; i++) {
    rot[i] = 0;
    rotSpeed[i] = 0;
  }
}

public void draw() {
  server.sendScreen(); // syphonサーバーに映像を送る
  background(0);
  // 画面の中心を基準点に
  translate(width/2, height/2);
  // FFT解析実行
  fft.forward(in.mix);
  // グラフで描画
  for (int i = 0; i < fft.specSize (); i++) {
  //float h = map(i, 0, fft.specSize(), 200, 180);
  float h = map(i*9, fft.specSize(), 0, 0, 180);
  float x = map(i, 0, fft.specSize(), 0, width*4.0f);
  // 回転速度とサイズをリスケール
  float r = map(fft.getBand(i), 0, 1.0f, 0, 0.2f);
  float size = map(fft.getBand(i)*2, 0, 1.0f, 0.1f, 0.5f);
  // FFT解析結果を角速度の配列に保存
  rotSpeed[i] = r;
  // 角速度だけ角度を変化して配列に保存
  rot[i] += rotSpeed[i];
  // 座標を記録
  pushMatrix();
  // 回転
  rotate(radians(rot[i]*4));
  // グラフを描画
  fill(h, 80, 80, 80);
  ellipse(x,0,size*20,size*20);
  // 回転を戻す
  popMatrix();
}
}

public void oscReceiverd(OscMessage msg) {
  if(msg.checkAddrPattern("/1/fader1") == true) {
    server.sendScreen(); // syphonサーバーに映像を送る
    background(0);
    //left ch
    //fft.forward(player.left);
    fft.forward(in.left);
    for (int i = 0; i < fft.specSize(); i++) {
      float h = map(i, 0, fft.specSize(), 1000, 180);
      float ellipseSize = map(fft.getBand(i)*2, 0, fftSize/16, 0, width);
      float x = map(i, 0, fft.specSize(), width/2, width);
      noStroke();
      fill(h, 80, 80, 7);
      ellipse(x,height/2,ellipseSize*2,ellipseSize*2);
    }
    //right ch
    //fft.forward(player.right);
    fft.forward(in.right);
    for (int i = 0; i < fft.specSize(); i++) {
      float h = map(i, 0, fft.specSize(), 200, 180);
      float ellipseSize = map(fft.getBand(i)*2, 0, fftSize/16, 0, width);
      float x = map(i, 0, fft.specSize(), width/2, 0);
      noStroke();
      fill(h, 80, 80, 7);
      ellipse(x,height/2,ellipseSize*2,ellipseSize*2);
    }
}
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Minim_FFT" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
