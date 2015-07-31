package bq.alvaro_ferran.m1r0;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;


/**
 * Created by alvaro-ferran on 8/06/15.
 */
public class MainActivity extends Activity {


    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null; //original
    private static String address = "98:D3:31:B2:DA:74"; // MAC-address of Bluetooth module
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TAG = "bluetooth"; //solo para log

    //private DrawingView drawView;
    ImageView imageView, imageCircles, bqLogo, link1, link2;
    Bitmap circleBitmap;
    Canvas canvas;
    ImageButton black, red, white, needle, linearAct;
    Button bluetoothButton;
    int blackState, yellowState, whiteState, needleState=0;
    int scaraOption=1;
    int color=0xFF000000;


    private int mInterval = 5000; // 5 seconds by default, can be changed later
    private Handler mHandler;

    /********ON CREATE**************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        imageView = (ImageView) findViewById(R.id.cubo);
        imageCircles = (ImageView) findViewById(R.id.circulos);
        //link1 = (ImageView) findViewById(R.id.link1);
        //link2 = (ImageView) findViewById(R.id.link2);
        bqLogo = (ImageView) findViewById(R.id.bqlogo);

        black= (ImageButton) findViewById(R.id.imageButton1);
        red= (ImageButton) findViewById(R.id.imageButton2);
        white= (ImageButton) findViewById(R.id.imageButton3);
        needle= (ImageButton) findViewById(R.id.imageButton4);
        linearAct= (ImageButton) findViewById(R.id.imageButton5);
        bluetoothButton= (Button) findViewById(R.id.bluetoothButton);

        uiManagement();

        mHandler = new Handler();
        startRepeatingTask();





    }



    /********WIDGET MANAGEMENT******************************************************************************/

    private void uiManagement(){

        black.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                scaraOption=1;

                black.setImageResource(R.drawable.negro2);
                red.setImageResource(R.drawable.rojo1);
                white.setImageResource(R.drawable.blanco1);
                needle.setImageResource(R.drawable.aguja1);

                color=0XFF000000;
            }
        });

        red.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                scaraOption=2;

                black.setImageResource(R.drawable.negro1);
                red.setImageResource(R.drawable.rojo2);
                white.setImageResource(R.drawable.blanco1);
                needle.setImageResource(R.drawable.aguja1);

                color = 0xFFFF0000;

            }
        });

        white.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)  {
                scaraOption=3;

                black.setImageResource(R.drawable.negro1);
                red.setImageResource(R.drawable.rojo1);
                white.setImageResource(R.drawable.blanco2);
                needle.setImageResource(R.drawable.aguja1);

                color=0XFFd8d8d8;

            }
        });

        needle.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                scaraOption=4;

                black.setImageResource(R.drawable.negro1);
                red.setImageResource(R.drawable.rojo1);
                white.setImageResource(R.drawable.blanco1);
                needle.setImageResource(R.drawable.aguja2);

                color=0XFFAAAAAA;

            }
        });

        linearAct.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    black.setImageResource(R.drawable.negro1);
                    red.setImageResource(R.drawable.rojo1);
                    white.setImageResource(R.drawable.blanco1);
                    needle.setImageResource(R.drawable.aguja1);
                    linearAct.setImageResource(R.drawable.actuador2);

                    //sendBT("=linearAct+");
                    sendBT("linearAct");
                    Toast.makeText(getBaseContext(), "DIPPING OBJECT IN PAINT", Toast.LENGTH_SHORT).show();

                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    linearAct.setImageResource(R.drawable.actuador1);
                    return true;
                }
                return false;
            }
        });


    }




    /********ON TOUCH EVENT*********************************************************************************/

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        float bucketX1= imageView.getLeft();
        float bucketX2= imageView.getRight();
        float bucketY1= imageView.getTop();
        float bucketY2= imageView.getBottom();

        long moveCounter=0;

        if (  ( (touchX>bucketX1) && (touchX<bucketX2) )   &&   ( (touchY>bucketY1) && (touchY<bucketY2) )  ) { //si esta dentro del cubo

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (scaraOption!=4) {
                        boolean draw=sendCoordinates(touchX, touchY); //only sends and draws reacheable points
                        if  (draw==true) circleAnimation(touchX, touchY,30,2,0);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(scaraOption==4){
                        circleAnimation(touchX, touchY,5,0,0.5f);
                        slowRate(touchX,touchY);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if(scaraOption==4) {
                        //sendBT("=up+")
                        sendBT("up");
                    }
                    break;
                default:
                    return false;
            }
        }

        return true;
    }




    /********SLOW DRAWING RATE***************************************************************************/

    long moveRate=0;
    void slowRate(float touchX, float touchY){
        moveRate++;
        if((moveRate%17)==0)  sendCoordinates(touchX, touchY);
    }



    /********LINEAR INTERPOLATION***************************************************************************/

    double map(double vx, double v1, double v2, double n1, double n2){
        // v1 start of range
        // v2 end of range
        // vx the starting number between the range
        double percentage = (vx-v1)/(v2-v1);
        // n1 start of new range
        // n2 end of new range
        return (n2-n1)*percentage+n1;
    }



    /********INVERSE KINEMATICS*****************************************************************************/

    double[] inverseKinematics(double x, double y) {
        double[] a={0,0};

        double L1=300;
        double L2=200;

        double distanceX=0;
        double distanceY=105; //distance from scara's base to box
        double offset1=27;
        double offset2=10;

        double x2= map( (double) x - imageView.getLeft() - imageView.getWidth()/2 , 0, imageView.getWidth(),  0, 470 ) + distanceX;   //COORD DE LA CAJA EN MM, origen en x=W/2,y=H
        double y2= map( (double) y - imageView.getTop()  , 0, imageView.getHeight(), 0, 350 )  +distanceY ;

        double Lab=Math.sqrt( Math.pow(x2,2) + Math.pow(y2,2) );
        double Aa= Math.acos(x2/Lab);
        double Ab= Math.acos( ( Math.pow(Lab,2) + Math.pow(L1,2) - Math.pow(L2,2) ) / (2*Lab*L1) );
        double A1=Aa+Ab;
        double A2= Math.acos((Math.pow(L2, 2) + Math.pow(L1, 2) - Math.pow(Lab, 2)) / (2 * L2 * L1));

        A1= (int) Math.toDegrees(A1);
        A2= (int) Math.toDegrees(A2);
        A1= A1-offset1;
        A2= (180-A2/2)-offset2; //change direction

        a[0]=A1;
        a[1]=A2;
        return a;
    }




    /********DISTANCE BETWEEN POINTS************************************************************************/
    double distance2Points(double x1, double y1, double x2, double y2){
        double d= Math.sqrt( Math.pow(x2-x1,2) + Math.pow(y2-y1,2) );
        return d;
    }


    /********SEND COORDINATES*******************************************************************************/
    float touchXOld=0, touchYold=0; //used for servo-moving time

    boolean sendCoordinates(float touchX, float touchY){

        double[] angles=inverseKinematics(touchX,touchY);

        double unSpeed=4;
        double time=distance2Points((double) touchXOld,(double) touchYold,(double) touchX,(double) touchY) * unSpeed;

        touchXOld=touchX;
        touchYold=touchY;

        //Toast.makeText(getBaseContext(), ""+time+"", Toast.LENGTH_SHORT).show();

        if(angles[0]>0 && time>0) {  //no error
            sendBT("" + angles[0] + "," + angles[1] + "," + time + "," + scaraOption + "");
            //Toast.makeText(getBaseContext(), "Coords: " +  + " " +  + "\n Angles: " + A1 + " " + A2 + "\n Ltot: " + Lab, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }




    /********CIRCLE ANIMATION*******************************************************************************/

    public void circleAnimation(float x, float y, float radius, float scaleEnd, float alphaEnd)  {


        circleBitmap = Bitmap.createBitmap((int) getWindowManager()
                .getDefaultDisplay().getWidth(), (int) getWindowManager()
                .getDefaultDisplay().getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(circleBitmap);
        imageCircles.setImageBitmap(circleBitmap);

        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(x, y, radius, paint);

        imageCircles.setPivotX((int) x);
        imageCircles.setPivotY((int) y);
        ObjectAnimator transparency = ObjectAnimator.ofFloat(imageCircles, "alpha", 1, alphaEnd);
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(imageCircles, "scaleX", 1,scaleEnd);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(imageCircles, "scaleY", 1,scaleEnd);
        int duration=700;
        scaleDownX.setDuration(duration);
        scaleDownY.setDuration(duration);
        transparency.setDuration(duration);
        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.play(scaleDownX).with(scaleDownY).with(transparency);
        scaleDown.start();


    }


    /********LOGO ANIMATION*********************************************************************************/
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {

            ObjectAnimator rotation = ObjectAnimator.ofFloat(bqLogo, "rotation", 0, 540);          //rotation, rotationX, rotationY
            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(bqLogo, "scaleX", 1,1.2f,1);        //from 1 to 1.5 to 1 (inflate and deflate)
            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(bqLogo, "scaleY", 1,1.2f,1);
            int duration=1000;
            scaleDownX.setDuration(duration);
            scaleDownY.setDuration(duration);
            rotation.setDuration(duration);
            AnimatorSet scaleDown = new AnimatorSet();
            scaleDown.play(scaleDownX).with(scaleDownY).with(rotation);
            scaleDown.start();


            mHandler.postDelayed(mStatusChecker, mInterval);
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }



    /********ON RESUME**************************************************************************************/

    @Override
    public void onResume() {
        super.onResume();



        bluetoothButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                bluetoothButton.setVisibility(View.GONE);

                BluetoothDevice device = btAdapter.getRemoteDevice(address);    //Pointer to BT in Robot

                try {
                    btSocket = createBluetoothSocket(device);   //Create Socket to Device
                } catch (IOException e1) {
                    errorExit("Fatal Error", "In onResume() and socket create failed: " + e1.getMessage() + ".");
                }

                btAdapter.cancelDiscovery();    //Discovery consumes resources -> Cancel before connecting

                try {
                    btSocket.connect();     //Connect to Robot
                } catch (IOException e) {
                    try {
                        btSocket.close();   //If unable to connect, close socket
                    } catch (IOException e2) {
                        errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
                    }
                }

                try {
                    outStream = btSocket.getOutputStream(); //Create output stream
                } catch (IOException e) {
                    errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
                }

            }
        });

        uiManagement();


    }


    /********ON PAUSE***************************************************************************************/

    @Override
    public void onPause() {
        super.onPause();

        if (outStream != null) {
            try {
                sendBT(""+0 + "," + 180+"," + 5000 + ","+scaraOption+"");
                outStream.flush();  //If output stream is not empty, send data
            } catch (IOException e) {
                errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }

        try     {
            btSocket.close();   //Close socket
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }


    /********CHECK BT STATE*********************************************************************************/

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    /********CREATE BT SOCKET*******************************************************************************/

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke( MY_UUID,device);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection", e);
                //Toast.makeText(getBaseContext(),"Could not create socket connection", Toast.LENGTH_LONG).show();
            }
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    /********SEND DATA**************************************************************************************/

    public void sendBT(String message) {
        byte[] msgBuffer = message.getBytes();

        try {
            outStream.write(msgBuffer);
            outStream.flush();

        } catch (IOException e) {
            String msg= "Phone not connected to client's Bluetooth";
            errorExit("Fatal Error", msg);
        }
    }

    /********ERROR EXIT*************************************************************************************/

    private void errorExit(String title, String message){
        /*try     {
            btSocket.close();   //Close socket
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }*/
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }


}
