package org.usfirst.frc.team5171.robot.subsystems;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.usfirst.frc.team5171.robot.Macro.*;

public class Record extends Thread{
	double freq;
	double startTime;
	double[] info;
	double headingBias, positionBias;
	Drive drive;
	String pathToFile;
	FileWriter writer;
	
	public Record(String fileName, Drive _drive, double _freq) {
		freq = _freq;
		String path = new String("/home/lvuser/recordings/");
		File folder = new File(path);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		pathToFile = new String(path+fileName+".csv");
		drive = _drive;
	}
	
	private double getCurTime() {
		return System.currentTimeMillis()/1000.0;
	}
	
	private double getRunTime() {
		return getCurTime()-startTime;
	}
	
	public double[] adjustForBias(double[] info) {
		info[0] -= headingBias;
		info[2] -= positionBias;
		return info;
	}
	
	public void run() {
		try {
			writer = new FileWriter(pathToFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		info = drive.getAllSensorInfo();
		headingBias = info[0];
		positionBias = info[2];
		
		startTime = getCurTime();
		double runTime = getRunTime();
		double lastAngle = 0;
		double lastTime = 0;
		while (runTime<=recordTime && writer!=null && drive!=null) {
			System.out.println(""+runTime);
			
			try {
				Thread.sleep((long)(1000/freq));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			runTime = getRunTime();
			info = drive.getAllSensorInfo();
			info = adjustForBias(info);
			
			try {
				writer.append(runTime+", ");
				writer.append(info[0]+", ");
				writer.append(info[1]+", ");
				double deltaTime = runTime-lastTime;
				lastTime = runTime;
				writer.append((info[0]-lastAngle)/deltaTime+", ");
				lastAngle = info[0];
				writer.append(info[2]+", ");
				writer.append(info[3]+"\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (writer != null) {
			try {
				writer.flush();
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
