package org.usfirst.frc.team5171.robot.subsystems;

import com.analog.adis16448.frc.ADIS16448_IMU;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import static org.usfirst.frc.team5171.robot.subsystems.Macro.*;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

public class Drive extends Thread {
	int[] leftMotorNum;
	int[] rightMotorNum; //Motor CAN ports.
	//TalonSRX[] motor;
	TalonSRX[] motor = new TalonSRX[MAX];
	/*In a 4-motor config, 0 and 1 are left while 2 and 3 are right.
						In a 6-motor config, 0, 1, and 2 are left while 3, 4, and 5 are right.*/
	ADIS16448_IMU imu;
	double curAng, desiredAng, angSpeed;
	double curPos, desPos;
	double throttle;
	double lastTime;
	double freq;
	double x, y; //robot position
	
	boolean isPosCtl;
	
	double testP = 0, testI = 0, testD = 0;
	double I = 0;
	
	double lastTurn = 0;
	
	public Drive(int _left[], int _right[], double _freq) {
		leftMotorNum = _left;
		rightMotorNum = _right;
		for (int i = 0; i < _left.length; i++) {
			motor[i] = new TalonSRX(_left[i]); //Init left motors using CAN.
			motor[i].configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 10);
			motor[i].setInverted(false);
			motor[i].setSensorPhase(true);
		}
		for (int i = 0; i < _right.length; i++) {
			motor[i+_left.length] = new TalonSRX(_right[i]); //Init right motors.
			motor[i+_left.length].configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 10);
			motor[i+_left.length].setInverted(true);
			motor[i+_left.length].setSensorPhase(true);
		}
		
		for (int i = 0; i < _left.length+_right.length; i++) {
			motor[i].configNominalOutputForward(0, 10);
			motor[i].configNominalOutputReverse(0, 10);
			motor[i].configPeakOutputForward(1, 10);
			motor[i].configPeakOutputReverse(-1, 10);
			motor[i].config_kF(0, 0, 10);
			motor[i].config_kP(0, 0.25, 10);
			motor[i].config_kI(0, 0, 10);
			motor[i].config_kD(0, 0, 10);
		}
		
		imu = new ADIS16448_IMU();
		imu.calibrate(); //Init gyro.
		
		freq = _freq;
		
		x = 0;
		y = 0;
	}
	
	private void updateMotor(double leftOutput, double rightOutput, ControlMode mode) {
		if (mode == ControlMode.Velocity) {
			motor[0].set(mode, leftOutput*maxRevPer100ms*wheelMultiplier);
			motor[1].set(ControlMode.Follower, motor[0].getDeviceID());
			motor[2].set(mode, rightOutput*maxRevPer100ms*wheelMultiplier);
			motor[3].set(ControlMode.Follower, motor[2].getDeviceID());
		} else if (mode == ControlMode.PercentOutput) {
			
		}
		//SmartDashboard.putString(SDkP, ""+leftOutput/wheelCircumfrence*wheelMultiplier/10);
		//SmartDashboard.putString(SDkI, ""+rightOutput/wheelCircumfrence*wheelMultiplier/10);
	}
	
	public void updatePosition(double _angle, double _displacement) {
		//achieve angle
		desiredAng += _angle;
		System.out.println("ang and disp" + _angle +" "+ _displacement + " desiAng " + desiredAng);
		double startTime = System.currentTimeMillis();
		while (true) {
			if (Math.abs(desiredAng-imu.getAngleZ())<1 || (System.currentTimeMillis()-startTime)>1000) {
				break;
			}
		}
		
		//achieve displacement
		curPos = (motor[0].getSelectedSensorPosition(0)+motor[2].getSelectedSensorPosition(0))/2;
		curPos = curPos/wheelMultiplier*wheelCircumfrence;
		desPos = curPos+_displacement;
		startTime = System.currentTimeMillis();
		while (true) {
			curPos = (motor[0].getSelectedSensorPosition(0)+motor[2].getSelectedSensorPosition(0))/2;
			curPos = curPos/wheelMultiplier*wheelCircumfrence;
			double error = desPos-curPos;
			
			if (curPos>(desPos) /*|| (System.currentTimeMillis()-startTime)>_displacement*2000*/ ) {
				updateVelocity(0, 0);
				break;
			} else {
				//double power = Math.abs(0.25-Math.pow((error-_displacement/2), 4)/(0.625*Math.pow(_displacement, 4)));
				//double power = Math.abs(0.6-Math.pow((error-_displacement/2), 2)/(0.71*Math.pow(_displacement, 2)));
				double iniP = 0.25, finP = 0.7;
				double dter = (16/Math.pow(_displacement, 2)+2);
				double a = (finP-iniP)*dter;
				double b = _displacement/2;
				double c = iniP-a/(2+Math.pow(b, 2));
				double power = a/(2+Math.pow((error-b), 2))+c;
				
				System.out.println(power+" "+curPos+" "+error);
				updateVelocity(power, 0);
			}
		}
	}
	
	public void updateLocation(double _x, double _y) {
		double dx = _x-x;
		double dy = _y-y;
		double theta = -((Math.atan2(dy, dx))*(180/3.1416)-90);
		double correctAng = (curAng)%360;
		double dTheta = theta-correctAng;
		double displacement = Math.hypot(dx, dy);
		x = _x;
		y = _y;
		System.out.println("dTheta" + dTheta +" "+ displacement);
		updatePosition(dTheta, displacement);
	}
	
	public void updateVelocity(double _throttle, double _turn) {
		throttle = 0.8*_throttle;
		if (Math.abs(_turn) > 0) {
			desiredAng = desiredAng+_turn/freq*joystickMultiplier;
		} else if (Math.abs(lastTurn)>0 && _turn==0) {
			desiredAng = imu.getAngleZ()+imu.getRate()/freq*15;
		}
		lastTurn = _turn;
	}
	
	public void setPIDConstants(double _kP, double _kI, double _kD) {
		testP = _kP;
		testI = _kI;
		testD = _kD;
	}
	
	private double updatePID(double error, double omega, double deltaTime) {
		double kP = 0, kI = 0, kD = 0;
		double P = 0, D = 0;
		if (testMode) {
			kP = testP;
			kI = testI;
			kD = testD;
		} else {
			kP = constkP;
			kI = constkI;
			kD = constkD;
		}
		
		double output = 0;
		
		P = kP * error;
		
		I = I + kI * error*deltaTime/1000;
		if (I > 10) {
			I = 10;
		} else if (I < -10) {
			I = -10;
		}
		
		//SmartDashboard.putString(SDcurAng, "P"+P);
		//SmartDashboard.putString(SDdesAng, "I"+I);
		
		D = (-kD) * omega;
		
		output = P + I + D;
		
		return output;
	}
	
	public boolean zeroDistance() {
		motor[0].setSelectedSensorPosition(0, 0, 10);
		motor[2].setSelectedSensorPosition(0, 0, 10);
		return true;
	}
	
	public boolean zeroSensor() {
		imu.reset();
		curAng = imu.getAngleZ();
		desiredAng = curAng;
		x = 0;
		y = 0;
		return true;
	}
	
	public void run() {
		curAng = imu.getAngleZ();
		curPos = (motor[0].getSelectedSensorPosition(0)+motor[2].getSelectedSensorPosition(0))/2;
		
		desiredAng = curAng;
		
		throttle = 0;
		
		lastTime = System.currentTimeMillis(); //Init lastTime for integral calculation.
		
		while (true) {
			try {
				sleep((long)(1000/freq));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			curAng = imu.getAngleZ();
			SmartDashboard.putString(SDcurAng, ""+curAng);
			SmartDashboard.putString(SDkP, ""+motor[0].getSelectedSensorPosition(0));
			SmartDashboard.putString(SDkI, ""+motor[2].getSelectedSensorPosition(0));
			//SmartDashboard.putString(SDkP, ""+motor[0].getSelectedSensorVelocity(0));
			//SmartDashboard.putString(SDkI, ""+motor[2].getSelectedSensorVelocity(0));
			//SmartDashboard.putString(SDdesAng, ""+desiredAng);
			double error = desiredAng-curAng; //Get error
			error = error%360;
			if (error > 180) {
				error -= 360;
			} else if (error < -180) {
				error += 360;
			}
			SmartDashboard.putString(SDAngErr, ""+error);
			if (Math.abs(error) < 1) {
				I = I/2;
			}
			//SmartDashboard.putString(SDkI, ""+I);

			double currentTime = System.currentTimeMillis();
			double deltaTime = currentTime - lastTime; //Get deltaTime
			lastTime = currentTime;

			angSpeed = imu.getRate(); //Get omega

			double output = updatePID(error, angSpeed, deltaTime);
			//SmartDashboard.putString(SDcurPos, "output "+output);

			updateMotor(throttle+output/100, throttle-output/100, ControlMode.Velocity);
		}
	}
}
