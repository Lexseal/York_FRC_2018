package org.usfirst.frc.team5171.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.PWM;
import edu.wpi.first.wpilibj.command.Scheduler;

import static org.usfirst.frc.team5171.robot.Macro.*;

import org.usfirst.frc.team5171.robot.commands.*;
import org.usfirst.frc.team5171.robot.subsystems.*;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	public static OI oi;
	
	AutoMode[] modes = new AutoMode[5];
	AutoMode autoMode;
	SendableChooser<String> priorityChooser = new SendableChooser<String>();
	SendableChooser<String> positionChooser = new SendableChooser<String>();

	int axisList[] = { LEFT_X, THROTTLE, LEFT_UP, RIGHT_UP, TURN, RIGHT_Y }; //the list of axis you are interested to track
	int buttonList[] = { A, B, X, Y, LB, RB }; //the list of buttons you are interested to track
	Controller driveStick = new Controller(0, axisList, buttonList, 1, 18, 200, 1.6); //driving xbox controller with port 0, 1% deadband and 18% cutoff running at 200Hz. The return value is x^1.6
	Controller controlStick = new Controller(1, axisList, buttonList, 1, 20, 200, 1.6); //secondary xbox controller with port 0, 1% deadband and 20% cutoff running at 200Hz. The return value is x^1.6

	int leftMotors[] = { 1, 3 };
	int rightMotors[] = { 2, 4 }; //motor CAN IDs
	Drive drive = new Drive(leftMotors, rightMotors, 200); //200Hz

	CubeLifter lift = new CubeLifter(6, 9, 200); //lift with motor 6, limit switch 9 at 200Hz

	int[] intakeMotors = { 7, 8 };
	Intake intake = new Intake(intakeMotors, 200);
	
	Climber climber = new Climber(5);

	StreamingServer stream = new StreamingServer(); //camera streaming
	
	Record recorder;
	Thread recordingThread = new Thread();

	/**
	 * This function is run when the robot is first started up and should be used
	 * for any initialization code.
	 */
	@Override
	public void robotInit() {
		oi = new OI();
		priorityChooser.addDefault("Switch", switchFirst);
		priorityChooser.addObject("Scale", scaleFirst);
		
		positionChooser.addDefault("Left Start", leftStart);
		positionChooser.addObject("Middle Start", middleStart);
		positionChooser.addObject("Right Start", rightStart);

		SmartDashboard.putData("Priority Chooser", priorityChooser);
		SmartDashboard.putData("Position Chooser", positionChooser);
		
		modes[0] = new AutoSwitchFromLeft(drive, lift, intake, 100);
		modes[1] = new AutoSwitchFromMiddle(drive, lift, intake, 100);
		modes[2] = new AutoSwitchFromRight(drive, lift, intake, 100);
		modes[3] = new AutoScaleFromLeft(drive, lift, intake, 100);
		modes[4] = new AutoScaleFromRight(drive, lift, intake, 100); //initialize all 5 auto modes here

		intake.start();
		lift.start();
		stream.start(); //start intake, lift, and streaming service
	}

	@Override
	public void disabledInit() {
		if (!modes[0].isFresh()) {
			modes[0] = new AutoSwitchFromLeft(drive, lift, intake, 100);
		}
		if (!modes[1].isFresh()) {
			modes[1] = new AutoSwitchFromMiddle(drive, lift, intake, 100);
		}
		if (!modes[2].isFresh()) {
			modes[2] = new AutoSwitchFromRight(drive, lift, intake, 100);
		}
		if (!modes[3].isFresh()) {
			modes[3] = new AutoScaleFromLeft(drive, lift, intake, 100);
		}
		if (!modes[4].isFresh()) {
			modes[4] = new AutoScaleFromRight(drive, lift, intake, 100);
		}
	}

	@Override
	public void disabledPeriodic() {
	}

	@Override
	public void autonomousInit() {
		String priority = priorityChooser.getSelected();
		String position = positionChooser.getSelected();
		
		System.out.println(position);
		if (priority == switchFirst) {
			if (position == leftStart) {
				autoMode = modes[0];
				System.out.println("mode0");
			} else if (position == middleStart) {
				autoMode = modes[1];
				System.out.println("mode1");
			} else if (position == rightStart) {
				autoMode = modes[2];
				System.out.println("mode2");
			}
		} else if (priority == scaleFirst) {
			if (position == leftStart) {
				autoMode = modes[3];
				System.out.println("mode3");
			} else if (position == rightStart) {
				autoMode = modes[4];
				System.out.println("mode4");
			}
		} //get the desired auto mode

		DriverStation station = DriverStation.getInstance();
		String platePlacement = station.getGameSpecificMessage();
		platePlacement = SmartDashboard.getString(SDkD, "");

		int[] platePos = { 0, 0 };
		if (platePlacement.length() > 0) {
			for (int i = 0; i < 2; i++) {
				if (platePlacement.charAt(i) == 'L') {
					platePos[i] = -1;
				} else if (platePlacement.charAt(i) == 'R') {
					platePos[i] = 1;
				}
			}
		} //get the position of plates. {1, -1} means the switch on the right but the scale is on the left
		
		System.out.println(platePos[0] +" "+ platePos[1]);
		if (autoMode != null && autoMode.isFresh()) {
			autoMode.initialize(platePos);
			autoMode.execute();
			autoMode.isFinished();
		}
	}

	/**
	 * This function is called periodically during autonomous
	 */
	@Override
	public void autonomousPeriodic() {
	}

	@Override
	public void teleopInit() {
		if (autoMode != null) {
			autoMode.isFinished();
		}
		
		if (!driveStick.isAlive()) {
			driveStick.start();
		}
		if (!controlStick.isAlive()) {
			controlStick.start();
		}
		if (!drive.isAlive()) {
			drive.start();
		}
		drive.zeroSensor();
	}

	/**
	 * This function is called periodically during operator control
	 */
	@Override
	public void teleopPeriodic() {
		if (driveTestMode) { // Set PID constants in test mode
			double kP = Double.parseDouble(SmartDashboard.getString(SDkP, ""));
			double kI = Double.parseDouble(SmartDashboard.getString(SDkI, ""));
			double kD = Double.parseDouble(SmartDashboard.getString(SDkD, ""));
			drive.setPIDConstants(kP, kI, kD);
		}
		if (liftTestMode) {
			double kP = Double.parseDouble(SmartDashboard.getString(SDkP, ""));
			double kI = Double.parseDouble(SmartDashboard.getString(SDkI, ""));
			double kD = Double.parseDouble(SmartDashboard.getString(SDkD, ""));
			lift.setPIDConstants(kP, kI, kD);
		}
		
		if(!recordingThread.isAlive() && SmartDashboard.getBoolean("DB/Button 0", false) && driveStick.getButton(A)==true && drive.getCurSpeed()>0) {
			String file = SmartDashboard.getString(SDkP, ""); //recording file name
			recorder = new Record(file, drive, lift, intake, 60); //initialize recorder with drive, lift, and intake infomation at 60Hz
			recordingThread = new Thread(recorder);
			recordingThread.start(); //start recording if the smartdashboard button 0 is pressed
		} else if (recordingThread.isAlive()) { 
			drive.setRecordingStat(true);
		} else {
			drive.setRecordingStat(false);
		}
		
		lift.updateDisplacement(controlStick.getAxis(LEFT_UP) - controlStick.getAxis(RIGHT_UP));
		//lifter.updateSpeed(controlStick.get(LEFT_UP)-controlStick.get(RIGHT_UP));
		if (controlStick.getButton(INTAKE_POS_BUTTON)) {
			lift.updatePosition(liftHome);
		} else if (controlStick.getButton(SWITCH_POS_BUTTON)) {
			lift.updatePosition(liftSwitchHeight);
		} else if (controlStick.getButton(SCALE_POS_BUTTON)) {
			lift.updatePosition(liftMaxHeight);
		} else if (controlStick.getButton(LIFT_RECENTER)) {
			lift.liftRecenter();
		}
		
		if (lift.protectionMode()) {
			drive.restrictedAcc();
			drive.normalAcc();
		}
    drive.setLiftHeight(lift.getCurPos());
		if (driveStick.getButton(LB) && driveStick.getButton(RB)) {
			drive.updateVelocity(driveStick.getAxis(THROTTLE), driveStick.getAxis(TURN));
		} else {
			drive.updateVelocity(-driveStick.getAxis(THROTTLE), driveStick.getAxis(TURN));
		}
		
		double[] intakeSpeed = { -driveStick.getAxis(LEFT_UP) - controlStick.getAxis(LEFT_X),
				driveStick.getAxis(RIGHT_UP) - controlStick.getAxis(TURN) };
		intake.updateSpeed(intakeSpeed);
		if (intakeSpeed[0]-intakeSpeed[1] > 0.1 ) {
			pwm.setSpeed(-0.3);
		} else if(intakeSpeed[0]-intakeSpeed[1] < -0.1) {
			pwm.setSpeed(-0.6);
		} else {
			pwm.setSpeed(0.0);
		}
		System.out.println(pwm.getRaw());
		
		climber.updateSpeed(controlStick.getAxis(THROTTLE));
	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override
	public void testPeriodic() {
	}
}
