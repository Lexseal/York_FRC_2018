package org.usfirst.frc.team5171.robot.subsystems;

import static org.usfirst.frc.team5171.robot.subsystems.Macro.*;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.*;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Vision extends Thread{
    
    public Vision() {
    		
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
        while (!Thread.interrupted()) {
        	cvSink.grabFrame(source);
        }
    }
	
}