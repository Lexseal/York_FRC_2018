package org.usfirst.frc.team5171.robot.commands;

import static org.usfirst.frc.team5171.robot.Macro.recordTime;

import org.usfirst.frc.team5171.robot.subsystems.CubeLifter;
import org.usfirst.frc.team5171.robot.subsystems.Drive;
import org.usfirst.frc.team5171.robot.subsystems.Intake;
import org.usfirst.frc.team5171.robot.subsystems.RecordingReader;

import edu.wpi.first.wpilibj.DriverStation;

public class AutoMode {
	//int position = 0;
	protected Drive drive;
	protected CubeLifter lifter;
	protected Intake intake;
	protected double freq;
	protected DriverStation station;
	protected RecordingReader[] reader;
	protected double startTime;

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
		reader = new RecordingReader[0];
	}

	// Called just before this Command runs the first time
	public void initialize(int[] plateAssignment) {
		System.out.println("initialized");
		if (!drive.isAlive()) {
			drive.start();
		}
		drive.zeroSensor();
	}

	// Called repeatedly when this Command is scheduled to run
	public void execute() {
		System.out.println(reader.length);
		for (int i = 0; i < reader.length; i++) {
			startTime = getRunTime();
			double runTime = getRunTime();
			reader[i].resetCutoff();
			while (runTime <= recordTime && station.isAutonomous() && station.isEnabled()) {
				try {
					Thread.sleep((long)(1000/freq));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				runTime = getRunTime();
				double[] vector = reader[i].getVector(runTime);
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
		return true;
	}

	// Called once after isFinished returns true
}