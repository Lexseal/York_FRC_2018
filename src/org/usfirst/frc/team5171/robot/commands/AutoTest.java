package org.usfirst.frc.team5171.robot.commands;
import org.usfirst.frc.team5171.robot.subsystems.Drive;

public class AutoTest extends AutoMode {
	Drive drive;
	public AutoTest(Drive _drive) {
		drive = _drive;
	}

	// Called just before this Command runs the first time
	public void initialize() {
		if (!drive.isAlive()) {
			drive.start();
		}
		drive.zeroDistance();
		drive.zeroSensor();
	}

	// Called repeatedly when this Command is scheduled to run
	public void execute() {
		drive.updateLocation(0, 2);
		/*drive.updateLocation(0.5, -0.5);
		drive.updateLocation(-0.5, -0.5);
		drive.updateLocation(-0.5, 0.5);
		drive.updateLocation(0, 0);*/
	}

	// Make this return true when this Command no longer needs to run execute()
	public boolean isFinished() {
		return false;
	}

	// Called once after isFinished returns true

	// Called when another command which requires one or more of the same
	// subsystems is scheduled to run
}
