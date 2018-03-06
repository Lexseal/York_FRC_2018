package org.usfirst.frc.team5171.robot.subsystems;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.usfirst.frc.team5171.robot.Macro.*;

public class Record extends Thread{
	double freq;
	double startTime;
	double[] motion = new double[5+3];
	double[] controlServices = new double[3];
	double headingBias, positionBias;
	Drive drive;
	CubeLifter lifter;
	Intake intake;
	String pathToFile;
	FileWriter writer;
	
	public Record(String fileName, Drive _drive, CubeLifter _lifter, Intake _intake, double _freq) {
		freq = _freq;
		String path = new String("/home/lvuser/recordings/");
		File folder = new File(path);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		pathToFile = new String(path+fileName+".csv");
		drive = _drive;
		lifter = _lifter;
		intake = _intake;
	}
	
	private double getCurTime() {
		return System.currentTimeMillis()/1000.0;
	}
	
	private double getRunTime() {
		return getCurTime()-startTime;
	}
	
	public double[] adjustForBias(double[] motion) {
		motion[0] -= headingBias;
		motion[2] -= positionBias;
		return motion;
	}
	
	public void run() {
		try {
			writer = new FileWriter(pathToFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		motion = drive.getAllSensorInfo();
		headingBias = motion[0];
		positionBias = motion[2];
		
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
			motion = drive.getAllSensorInfo();
			motion = adjustForBias(motion);
			
			double liftPos = lifter.getDesPos();
			double[] intakeSpeed = intake.getSpeed();
			controlServices[0] = liftPos;
			controlServices[1] = intakeSpeed[0];
			controlServices[2] = intakeSpeed[1];
			
			try {
				writer.append(runTime+", ");
				writer.append(motion[0]+", ");
				writer.append(motion[1]+", ");
				double deltaTime = runTime-lastTime;
				lastTime = runTime;
				writer.append((motion[0]-lastAngle)/deltaTime+", ");
				lastAngle = motion[0];
				writer.append(motion[2]+", ");
				writer.append(motion[3]+", ");
				
				writer.append(controlServices[0]+", ");
				writer.append(controlServices[1]+", ");
				writer.append(controlServices[2]+"\n");
				
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