package org.usfirst.frc.team5171.robot.commands;
import org.usfirst.frc.team5171.robot.subsystems.Drive;

public class AutoTest extends AutoMode {
	int position = 0;
	
	Drive drive;
	public AutoTest(Drive _drive) {
		drive = _drive;
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
	}

	// Make this return true when this Command no longer needs to run execute()
	public boolean isFinished() {
		return false;
	}

	// Called once after isFinished returns true

	// Called when another command which requires one or more of the same
	// subsystems is scheduled to run
}
