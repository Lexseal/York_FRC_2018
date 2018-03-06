package org.usfirst.frc.team5171.robot.commands;
import org.usfirst.frc.team5171.robot.subsystems.*;
import edu.wpi.first.wpilibj.DriverStation;

import static org.usfirst.frc.team5171.robot.Macro.*;

public class AutoTest extends AutoMode {
	//int position = 0;
	Drive drive;
	CubeLifter lifter;
	Intake intake;
	double freq;
	DriverStation station;
	RecordingReader reader;
	double startTime;
	
	private double getCurTime() {
		return System.currentTimeMillis()/1000.0;
	}
	
	private double getRunTime() {
		return getCurTime()-startTime;
	}
	
	public AutoTest(Drive _drive, CubeLifter _lifter, Intake _intake, double _freq) {
		freq = _freq;
		drive = _drive;
		lifter = _lifter;
		intake = _intake;
		station = DriverStation.getInstance();
		reader = new RecordingReader("testAuto");
	}

	// Called just before this Command runs the first time
	public void initialize(int[] plateAssignment) {
		if (!drive.isAlive()) {
			drive.start();
		}
		drive.zeroSensor();
	}

	// Called repeatedly when this Command is scheduled to run
	public void execute() {
		startTime = getRunTime();
		double runTime = getRunTime();
		reader.resetCutoff();
		while (runTime <= recordTime && station.isAutonomous() && station.isEnabled()) {
			try {
				Thread.sleep((long)(1000/freq));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			runTime = getRunTime();
			double[] vector = reader.getVector(runTime);
			for (int i = 0; i < vector.length; i++) {
				//System.out.print(vector[i]+" ");
			}
			//System.out.println("");
			
			double angle = vector[1];
			double omega = (vector[2]+vector[3])/2;
			double position = vector[4];
			double speed = vector[5];
			drive.follow(angle, omega, position, speed);
			
			double liftPos = vector[6];
			lifter.updatePosition(liftPos);
			
			double[] intakeSpeed = {vector[7], vector[8]}; 
			intake.updateSpeed(intakeSpeed);
		}
	}

	// Make this return true when this Command no longer needs to run execute()
	public boolean isFinished() {
		drive.followFinished();
		return true;
	}

	// Called once after isFinished returns true

	// Called when another command which requires one or more of the same
	// subsystems is scheduled to run
}
