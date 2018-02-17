package org.usfirst.frc.team5171.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.DriverStation;

import org.usfirst.frc.team5171.robot.commands.*;
import org.usfirst.frc.team5171.robot.subsystems.*;
import static org.usfirst.frc.team5171.robot.subsystems.Macro.*;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {

	public static final ExampleSubsystem exampleSubsystem = new ExampleSubsystem();
	public static OI oi;

	AutoMode autoMode;
	SendableChooser<AutoMode> chooser = new SendableChooser<AutoMode>();
	
	int portlist[] = {LEFT_X, THROTTLE, LEFT_UP, RIGHT_UP, TURN, RIGHT_Y};
	Controller joystick = new Controller(0, portlist, 5, 18, 100, 2);
	
	int leftMotors[] = {1, 3};
	int rightMotors[] = {2, 4};
	Drive drive = new Drive(leftMotors, rightMotors, 100);
	
	boolean prevDis = true;
	
	//Vision stream = new Vision();
	StreamingServer stream = new StreamingServer();

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		oi = new OI();
		chooser.addDefault("", new AutoTest(drive));
	    SmartDashboard.putData("Autonomous Selector", chooser);
	    stream.start();
	}

	/**
	 * This function is called once each time the robot enters Disabled mode.
	 * You can use it to reset any subsystem information you want to clear when
	 * the robot is disabled.
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
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString code to get the auto name from the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional commands to the
	 * chooser code above (like the commented example) or additional comparisons
	 * to the switch structure below with additional strings & commands.
	 */
	@Override
	public void autonomousInit() {
		autoMode = chooser.getSelected();
		
		// schedule the autonomous command (example)
		if (autoMode != null) {
			autoMode.initialize();
			autoMode.execute();
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
		if (!joystick.isAlive()) {
			joystick.start();
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
		Scheduler.getInstance().run();
		if (testMode) { //Set PID constants in test mode
			double kP = Double.parseDouble(SmartDashboard.getString(SDkP, ""));
			double kI = Double.parseDouble(SmartDashboard.getString(SDkI, ""));
			double kD = Double.parseDouble(SmartDashboard.getString(SDkD, ""));
			drive.setPIDConstants(kP, kI, kD);
		}
		
		SmartDashboard.putString(SDTHROTTLE, ""+(-joystick.get(THROTTLE)));
		SmartDashboard.putString(SDTURN, ""+joystick.get(TURN));
		drive.updateVelocity(-joystick.get(THROTTLE), joystick.get(TURN));
	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override
	public void testPeriodic() {
	}
}
