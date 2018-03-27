package org.usfirst.frc.team5171.robot.commands;

import org.usfirst.frc.team5171.robot.subsystems.CubeLifter;
import org.usfirst.frc.team5171.robot.subsystems.Drive;
import org.usfirst.frc.team5171.robot.subsystems.Intake;
import org.usfirst.frc.team5171.robot.subsystems.RecordingReader;

public class AutoScaleFromRight extends AutoMode {

	public AutoScaleFromRight(Drive _drive, CubeLifter _lifter, Intake _intake, double _freq) {
		super(_drive, _lifter, _intake, _freq);
		
		reader.add(new RecordingReader("R_LScale"));
		reader.add(new RecordingReader("R_RScale"));
	}

	public void initialize(int[] plateAssignment) {
		if (!drive.isAlive()) {
			drive.start();
		}
		drive.zeroSensor();
		
		if (plateAssignment[1] == -1) { //If scale on the left, remove right auto.
			reader.remove(1);
		} else if (plateAssignment[1] == 1) { //If scale on the right, remove left auto.
			reader.remove(0);
		} else { //If info inconclusive, remove both.
			reader.remove(0);
			reader.remove(1);
		}
	}
}
