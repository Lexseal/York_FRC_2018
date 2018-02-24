package org.usfirst.frc.team5171.robot.subsystems;

import static org.usfirst.frc.team5171.robot.Macro.*;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

public class CubeLifter extends Thread {
	int port;
	int freq;
	TalonSRX motor;
	double curPos, desiredPos;
	double lastTime;
	double I;
	double testKP = 0, testKI = 0, testKD = 0;
	
	public CubeLifter(int _port, int _freq) {
		port = _port;
		freq = _freq;
		motor = new TalonSRX(port);
		
		motor.setInverted(false);
		motor.setSensorPhase(true);
		
		motor.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 10);
		motor.setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, 5, 10);
		
		motor.configNominalOutputForward(0, 10);
		motor.configNominalOutputReverse(0, 10);
		motor.configPeakOutputForward(1, 10);
		motor.configPeakOutputReverse(-1, 10);
		
		motor.config_kF(0, 0, 10);
		motor.config_kP(0, 0.2, 10);
		motor.config_kI(0, 0, 10);
		motor.config_kD(0, 0.005, 10);
		
		motor.configOpenloopRamp(0.1, 10);
		motor.configClosedloopRamp(0.1, 10);
		
		zeroSensor();
	}
	
	private double getCurPos() {
		return motor.getSelectedSensorPosition(0)/wheelMultiplier*liftHeightPerRev;
	}
	
	public void updateSpeed(double speed) {
		
		motor.set(ControlMode.PercentOutput, speed);
	}
	
	public void updatePosition(double position) {
		curPos = getCurPos();
		desiredPos = position;
		if (desiredPos < 0) {
			desiredPos = 0;
		}
	}
	
	public void setPIDConstants(double _kP, double _kI, double _kD) {
		testKP = _kP;
		testKI = _kI;
		testKD = _kD;
	}
	
	public void updateDisplacement(double displacement) {
		desiredPos = desiredPos+displacement;
		if (desiredPos < 0) {
			desiredPos = 0;
		}
	}
	
	public boolean zeroSensor() {
		motor.setSelectedSensorPosition(0, 0, 10);
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	
	private double updatePID(double error, double speed, double deltaTime) {
		double kP = 0, kI = 0, kD = 0;
		double P = 0, D = 0;
		if (liftTestMode) {
			kP = testKP;
			kI = testKI;
			kD = testKD;
		} else {
			kP = liftkP;
			kI = liftkI;
			kD = liftkD;
		}
		
		P = kP * error;
		
		I = I + kI * error*deltaTime/1000;
		if (I > 0.2) {
			I = 0.2;
		} else if (I < -0.2) {
			I = -0.2;
		}
		
		D = (-kD) * speed;
		
		double output = P + I + D;
		
		return output;
	}
	
	public void run() {
		lastTime = System.currentTimeMillis();
		
		while (true) {
			try {
				Thread.sleep(1000/freq);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			curPos = getCurPos();
			//System.out.println(motor.getSelectedSensorPosition(0));
			
			double error = desiredPos-curPos;
			double speed = motor.getSelectedSensorVelocity(0);
			double currentTime = System.currentTimeMillis();
			double deltaTime = currentTime - lastTime; //Get deltaTime
			lastTime = currentTime;
			
			double output = updatePID(error, speed, deltaTime);
			
			System.out.println(desiredPos+"   "+curPos+"   "+motor.getSelectedSensorPosition(0) + "   " +output);
			updateSpeed(output);
		}
	}
}
