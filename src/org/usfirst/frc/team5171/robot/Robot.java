package org.usfirst.frc.team5171.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.DriverStation;

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
	
	AutoMode[] modes = new AutoMode[8];
	AutoMode autoMode;
	SendableChooser<String> priorityChooser = new SendableChooser<String>();
	SendableChooser<String> positionChooser = new SendableChooser<String>();

	int axisList[] = { LEFT_X, THROTTLE, LEFT_UP, RIGHT_UP, TURN, RIGHT_Y };
	int buttonList[] = { A, B, X, Y, LB, RB };
	Controller driveStick = new Controller(0, axisList, buttonList, 3, 18, 200, 1.6);
	Controller controlStick = new Controller(1, axisList, buttonList, 3, 20, 200, 1.6);

	int leftMotors[] = { 1, 3 };
	int rightMotors[] = { 2, 4 };
	Drive drive = new Drive(leftMotors, rightMotors, 200);

	CubeLifter lifter = new CubeLifter(6, 9, 200);

	int[] intakeMotors = { 7, 8 };
	Intake intake = new Intake(intakeMotors, 200);
	
	Climber climber = new Climber(5);

	StreamingServer stream = new StreamingServer();
	
	Record recorder;
	Thread recordingThread = new Thread();
	
	/**/

	/**
	 * This function is run when the robot is first started up and should be used
	 * for any initialization code.
	 */
	@Override
	public void robotInit() {
		oi = new OI();
		priorityChooser.addDefault("Switch First", switchFirst);
		priorityChooser.addObject("Scale First", scaleFirst);
		
		positionChooser.addDefault("Left Start", leftStart);
		positionChooser.addObject("Middle Start", middleStart);
		positionChooser.addObject("Middle Wait", middleWait);
		positionChooser.addObject("Right Start", rightStart);

		SmartDashboard.putData("Priority Chooser", priorityChooser);
		SmartDashboard.putData("Position Chooser", positionChooser);
		
		modes[0] = new AutoTest(drive, lifter, intake, 100);
		/*modes[1] = new (, 100);
		modes[1] = new (, 100);
		modes[1] = new (, 100);
		modes[1] = new (, 100);
		modes[1] = new (, 100);
		modes[1] = new (, 100);
		modes[1] = new (, 100);*/

		intake.start();
		lifter.start();
		// stream.start();
	}

	/**
	 * This function is called once each time the robot enters Disabled mode. You
	 * can use it to reset any subsystem information you want to clear when the
	 * robot is disabled.
	 */
	@Override
	public void disabledInit() {
	}

	@Override
	public void disabledPeriodic() {
		Scheduler.getInstance().run();
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable chooser
	 * code works with the Java SmartDashboard. If you prefer the LabVIEW Dashboard,
	 * remove all of the chooser code and uncomment the getString code to get the
	 * auto name from the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional commands to the
	 * chooser code above (like the commented example) or additional comparisons to
	 * the switch structure below with additional strings & commands.
	 */
	@Override
	public void autonomousInit() {
		String priority = priorityChooser.getSelected();
		String position = positionChooser.getSelected();

		if (priority == switchFirst) {
			switch (position) {
			case leftStart:
				autoMode = modes[0];
			case middleStart:

			case middleWait:

			case rightStart:

			}
		} else if (priority == scaleFirst) {
			switch (position) {
			case leftStart:

			case middleStart:

			case middleWait:

			case rightStart:

			}
		}

		DriverStation station = DriverStation.getInstance();
		String platePlacement = station.getGameSpecificMessage();

		int[] platePos = { 0, 0 };
		if (platePlacement.length() > 0) {
			for (int i = 0; i < 2; i++) {
				if (platePlacement.charAt(i) == 'L') {
					platePos[i] = -1;
				} else {
					platePos[i] = 1;
				}
			}
		}

		// schedule the autonomous command (example)
		if (autoMode != null) {
			System.out.println("in");
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
		
		String file = new String("testAuto");
		recorder = new Record(file, drive, lifter, intake, 50);
	}

	/**
	 * This function is called periodically during operator control
	 */
	@Override
	public void teleopPeriodic() {
		Scheduler.getInstance().run();
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
			lifter.setPIDConstants(kP, kI, kD);
		}
		
		if(!recordingThread.isAlive() && SmartDashboard.getBoolean("DB/Button 0", false)) {
			recordingThread = new Thread(recorder);
			recordingThread.start();
		}

		drive.updateVelocity(-driveStick.getAxis(THROTTLE), driveStick.getAxis(TURN));
		lifter.updateDisplacement(controlStick.getAxis(LEFT_UP) - controlStick.getAxis(RIGHT_UP));
		//lifter.updateSpeed(controlStick.get(LEFT_UP)-controlStick.get(RIGHT_UP));

		double[] speed = { -driveStick.getAxis(LEFT_UP) - controlStick.getAxis(LEFT_X),
				driveStick.getAxis(RIGHT_UP) - controlStick.getAxis(TURN) };
		intake.updateSpeed(speed);
		
		climber.updateSpeed(controlStick.getAxis(THROTTLE));
	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override
	public void testPeriodic() {
	}
}
