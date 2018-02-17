package org.usfirst.frc.team5171.robot.subsystems;

import static org.usfirst.frc.team5171.robot.subsystems.Macro.*;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class StreamingServer extends Thread{
    
    public StreamingServer() {
    		
    }

    public void run() {
    	CameraServer server = CameraServer.getInstance();
    	UsbCamera camera = server.startAutomaticCapture();
        camera.setResolution(320, 240);
        camera.setExposureManual(10);
        camera.setBrightness(10);
        camera.setWhiteBalanceManual(3500);
        camera.setFPS(30);
        
        CvSink cvSink = server.getVideo();
        CvSource hsvStream = server.putVideo("Red", 320, 240);
        
        Mat source = new Mat();
        Mat hsv = new Mat();
        /*Mat blueBin = new Mat();
        Mat redBin = new Mat();*/
        
    	while(!Thread.interrupted()) {
            cvSink.grabFrame(source);
            if (source.width() == 0) {
            	continue;
            }
//            int[] histB = new int[360];
//            int[] histR = new int[360];
            //Calibrate
            Imgproc.cvtColor(source, hsv, Imgproc.COLOR_BGR2HSV);
//            //Fill histograms for red and blue
//            for(int i=0; i<hsv.width(); i++) {
//            	for(int j=0; j<hsv.height(); j++) {
//            		//System.out.println(hsv.get(i, j).toString());
//            		int hue = 0, val = 0;
//            		try {
//            		hue = (int)hsv.get(j, i)[0];
//            		val = (int)hsv.get(j, i)[1];
//            		} catch(Exception e) {
//            			System.out.println(e+"\n "+i+", "+j);
//            		}
//            		if (hue > 120-30 && hue < 120+30 && val > 100)
//            			histB[hue]++;
//            		if (hue > 170-30 && hue < 170+30 && val > 100)
//            			histR[hue]++;
//            	}
//            }
//            
//            int meanB, meanR, stdDevB, stdDevR;
//            int sum = 0, count = 0;
//            //Mean of Blue
//            for (int i=1; i<= 360; i++) {
//            	sum += i*histB[i-1];
//            	count += histB[i-1];
//            }
//            meanB = sum/count;
//            for (int i=0; i < 360; i++) {
//            	sum += (histB[i]-meanB)*(histB[i]-meanB);
//            }
//            sum /= count - 1;
//            stdDevB = (int) Math.sqrt(sum);
//            
//            //Mean of Red
//            sum = 0;
//            count = 0;
//            for (int i=1; i<= 360; i++) {
//            	sum += i*histR[i-1];
//            	count += histR[i-1];
//            }
//            meanR = sum/count;
//            for (int i=0; i < 360; i++) {
//            	sum += (histR[i]-meanR)*(histR[i]-meanR);
//            }
//            sum /= count - 1;
//            stdDevR = (int) Math.sqrt(sum);
//            
//            //Create Binaries
//            Core.inRange(hsv, new Scalar(meanB-stdDevB, 100, 75), 
//            		new Scalar(meanB+stdDevB, 255, 255), blueBin);
//            
//            Core.inRange(hsv, new Scalar(meanR-stdDevR, 80, 75), 
//            		new Scalar(meanR-stdDevR, 255, 255), redBin);
            
            //Debug
//            System.out.println(source.size()+" "+source.channels()+"\nB: "+meanB+", "+stdDevB+". R: "+meanR+", "+stdDevR);
            
    		 /*Mat redBinary = new Mat(); 
             Mat redLines = new Mat();
    		Core.inRange(hsv, new Scalar(170-40, 30, 90), new Scalar(170+40, 255, 255), redBinary); //red
            Imgproc.HoughLinesP(redBinary, redLines, 1, 3.1415926/180, 80, 40, 100);
    		int rHLineCount = redLines.rows();*/
    		
            /* int scale = 3;
    		Mat element = Imgproc.getStructuringElement( Imgproc.MORPH_ELLIPSE,
    				 new Size( 2*scale + 1, 2*scale+1 ),
                     new Point( scale, scale ) );
    		Imgproc.dilate(hsv, hsv, element);
    		scale = 2;
    		element = Imgproc.getStructuringElement( Imgproc.MORPH_ELLIPSE,
    				 new Size( 2*scale + 1, 2*scale+1 ),
                     new Point( scale, scale ) );
    		Imgproc.erode(hsv, hsv, element);*/
//    		Imgproc.medianBlur(hsv, hsv, 5);
            
            Mat blueBinary = new Mat(); 
            Mat blueLines = new Mat();
            Core.inRange(hsv, new Scalar(120-30, 80, 120), new Scalar(120+30, 255, 255), blueBinary); //blue
            Imgproc.Canny(blueBinary, blueBinary, 100, 200);
            Imgproc.HoughLinesP(blueBinary, blueLines, 1, 3.1415926/180, 80, 40, 100);
    		int bHLineCount = blueLines.rows();
    		
    		//		--- Draw lines ---
    		
        	/*for( int i = 0; i < redLines.rows(); i++ ) {
    			Imgproc.line( redBinary, new Point(redLines.get(i,0)[0], redLines.get(i,0)[1]),
    		            new Point(redLines.get(i,0)[2], redLines.get(i,0)[3]), new Scalar(120,255,120), 3);
    		}*/
            hsvStream.putFrame(blueBinary);
            if (bHLineCount > 0)
            	System.out.println(/*"R: "+rHLineCount+*/", B: "+bHLineCount);
            
            for (int i = 0; i < bHLineCount; i++) {
            	double x = Math.abs(blueLines.get(i, 0)[0]-blueLines.get(i, 0)[2]);
            	double y = Math.abs(blueLines.get(i, 0)[1]-blueLines.get(i, 0)[3]);
            	if (Math.hypot(x, y) > 100) {
            		System.out.println("bluefound");
            	}
            }
            
        }
    }
	
}