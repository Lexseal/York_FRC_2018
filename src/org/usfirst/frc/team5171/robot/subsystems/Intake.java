package org.usfirst.frc.team5171.robot.subsystems;

//import static org.usfirst.frc.team5171.robot.Macro.*;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

public class Intake extends Thread {
	int[] port;
	int freq;
	VictorSPX[] motor = new VictorSPX[2];
	boolean newCommand = false;
	double spitOutDist = 0;
	
	public Intake(int[] _port, int _freq) {
		port = _port;
		freq = _freq;
		
		for (int i = 0; i < port.length; i++) {
			motor[i] = new VictorSPX(port[i]);
			motor[i].configNominalOutputForward(0, 10);
			motor[i].configNominalOutputReverse(0, 10);
			motor[i].configPeakOutputForward(1, 10);
			motor[i].configPeakOutputReverse(-1, 10);
		}
	}
	
	public void updateSpeed(double[] speed) {
		motor[0].set(ControlMode.PercentOutput, speed[0]/2);
		motor[1].set(ControlMode.PercentOutput, speed[1]/2);
	}
	
	public void spitOut(double distance) {
		spitOutDist = distance;
		newCommand = true;
	}
	
	public void run() {
		while (true) {
			try {
				Thread.sleep(1000/freq);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (newCommand) {
				newCommand = false;
				updateSpeed(new double[]{-spitOutDist, -spitOutDist});
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}