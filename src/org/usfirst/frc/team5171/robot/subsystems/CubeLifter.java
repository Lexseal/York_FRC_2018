package org.usfirst.frc.team5171.robot.subsystems;

import static org.usfirst.frc.team5171.robot.Macro.*;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
//import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class CubeLifter extends Thread {
	int port;
	int freq;
	DigitalInput liftSwitch;
	TalonSRX motor;
	double curPos, desPos;
	double lastTime;
	double I;
	double testKP = 0, testKI = 0, testKD = 0;
	boolean recenterComplete = true;
	
	public CubeLifter(int _port, int _switchPin, int _freq) {
		port = _port;
		freq = _freq;
		
		liftSwitch = new DigitalInput(_switchPin);
		
		motor = new TalonSRX(port);
		
		motor.setInverted(true);
		motor.setSensorPhase(true);
		
		motor.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 10);
		//motor.setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, 5, 10);
		
		motor.configNominalOutputForward(0, 10);
		motor.configNominalOutputReverse(0, 10);
		motor.configPeakOutputForward(1, 10);
		motor.configPeakOutputReverse(-1, 10);
		
		motor.config_kF(0, 0, 10);
		motor.config_kP(0, 0.2, 10);
		motor.config_kI(0, 0, 10);
		motor.config_kD(0, 0.005, 10);
		
		motor.configOpenloopRamp(0.2, 10);
		motor.configClosedloopRamp(0.2, 10);
		
		zeroSensor();
	}
	
	private boolean switchPressed() {
		return liftSwitch.get();
	}
	
	private double getCurPos() {
		return motor.getSelectedSensorPosition(0)/wheelMultiplier*liftHeightPerRev;
	}
	
	public void updateSpeed(double speed) {
		if (curPos<(secondStageLanding+20) && (curPos>secondStageLanding-5) && speed<0) {
			if (Math.abs(secondStageLanding-curPos) < 3) {
				speed = -0.1;
			} else {
				speed = -0.2;
			}
		} else if (speed < -0.6) {
			speed = -0.6;
		}
		//System.out.println(curPos);
		
		motor.set(ControlMode.PercentOutput, speed);
	}
	
	public void liftRecenter() {
		recenterComplete = false;
	}
	
	public void updatePosition(double position) {
		curPos = getCurPos();
		desPos = position;
		if (desPos < 0) {
			desPos = 0;
		} else if (desPos > liftMaxHeight) {
			desPos = liftMaxHeight;
		}
	}
	
	public boolean liftIsReady() {
		if (Math.abs(desPos-curPos) < 3) {
			return true;
		}
		return false;
	}
	
	public boolean protectionMode() {
		if (getCurPos() > protectedLiftHeight) {
			return true;
		}
		return false;
	}
	
	public void setPIDConstants(double _kP, double _kI, double _kD) {
		testKP = _kP;
		testKI = _kI;
		testKD = _kD;
	}
	
	public void updateDisplacement(double displacement) {
		desPos = desPos+displacement*1.5;
		if (desPos < 0) {
			desPos = 0;
		} else if (desPos > liftMaxHeight) {
			desPos = liftMaxHeight;
		}
	}
	
	public boolean zeroSensor() {
		motor.setSelectedSensorPosition(0, 0, 10);
		SmartDashboard.putBoolean("DB/LED3", true);
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
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
	
	public double getDesPos() {
		return desPos;
	}
	
	public void run() {
		lastTime = System.currentTimeMillis();
		
		while (true) {
			try {
				Thread.sleep(1000/freq);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			curPos = getCurPos();
			//System.out.println(motor.getSelectedSensorPosition(0));
			
			double error = desPos-curPos;
			double speed = motor.getSelectedSensorVelocity(0);
			double currentTime = System.currentTimeMillis();
			double deltaTime = currentTime - lastTime; //Get deltaTime
			lastTime = currentTime;
			
			double output = updatePID(error, speed, deltaTime);
			
			if (recenterComplete) {
				if (switchPressed()) {
					if (output < 0) {
						updateSpeed(0);
					} else {
						updateSpeed(output);
					}
					zeroSensor();
				} else {
					SmartDashboard.putBoolean("DB/LED3", false);
					updateSpeed(output);
				}
			} else {
				if (switchPressed()) {
					recenterComplete = true;
					zeroSensor();
					desPos = curPos;
					updateSpeed(0);
				} else {
					updateSpeed(-0.2);
				}
			}
			
			//System.out.println(desPos+"   "+curPos+"   "+motor.getSelectedSensorPosition(0) + "   " +output);
		}
	}
}
