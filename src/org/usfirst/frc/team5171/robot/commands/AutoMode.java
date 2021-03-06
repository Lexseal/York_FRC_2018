package org.usfirst.frc.team5171.robot.commands;

import static org.usfirst.frc.team5171.robot.Macro.recordTime;

import java.util.ArrayList;

import org.usfirst.frc.team5171.robot.subsystems.CubeLifter;
import org.usfirst.frc.team5171.robot.subsystems.Drive;
import org.usfirst.frc.team5171.robot.subsystems.Intake;
import org.usfirst.frc.team5171.robot.subsystems.RecordingReader;

import edu.wpi.first.wpilibj.DriverStation;

public class AutoMode {
	protected Drive drive;
	protected CubeLifter lifter;
	protected Intake intake;
	protected double freq;
	protected DriverStation station;
	protected ArrayList<RecordingReader> reader;
	protected double startTime;
	protected boolean fresh = true;

	public boolean isFresh() {
		return fresh;
	}
	
	protected double getCurTime() {
		return System.currentTimeMillis()/1000.0;
	}

	protected double getRunTime() {
		return getCurTime()-startTime;
	}

	public AutoMode(Drive _drive, CubeLifter _lifter, Intake _intake, double _freq) {
		freq = _freq;
		drive = _drive;
		lifter = _lifter;
		intake = _intake;
		station = DriverStation.getInstance();
		reader = new ArrayList<RecordingReader>(0);
	}

	// Called  before running execute
	public void initialize(int[] plateAssignment) {
		System.out.println("initialized");
		if (!drive.isAlive()) {
			drive.start();
		}
		drive.zeroSensor();
	}

	// Called once
	public void execute() {
		System.out.println(reader.size());
		for (int i = 0; i < reader.size(); i++) {
			RecordingReader vectors = reader.get(i);
			startTime = getRunTime();
			double runTime = getRunTime();
			vectors.resetCutoff();
			while (runTime <= recordTime && station.isAutonomous() && station.isEnabled()) {
				try {
					Thread.sleep((long)(1000/freq));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				runTime = getRunTime();
				double[] vector = vectors.getVector(runTime);
				//System.out.println("");
				//vector = {runtime, theta, omega1, omega2, s, v, x, y, lift, lIntake, rIntake}
				double angle = vector[1];
				double omega = vector[2];
				double position = vector[3];
				double speed = vector[4];
				double x = vector[5];
				double y = vector[6];
				drive.follow(angle, omega, position, speed, x, y);

				double liftPos = vector[7];
				lifter.updatePosition(liftPos);

				double[] intakeSpeed = {vector[8], vector[9]}; 
				intake.updateSpeed(intakeSpeed);
			}
			if (!station.isAutonomous() || !station.isEnabled()) {
				break;
			}
		}
	}

	// Make this return true when this Command no longer needs to run execute()
	public boolean isFinished() {
		drive.followFinished();
		fresh = false;
		return true;
	}

	// Called once after isFinished returns true
}