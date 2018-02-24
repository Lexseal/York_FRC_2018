package org.usfirst.frc.team5171.robot.subsystems;

import static org.usfirst.frc.team5171.robot.Macro.*;

import com.analog.adis16448.frc.ADIS16448_IMU;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.DriverStation;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

public class Drive extends Thread {
	int[] leftMotorNum;
	int[] rightMotorNum; //Motor CAN ports.
	TalonSRX[] motor = new TalonSRX[MAX];
	/*In a 4-motor config, 0 and 1 are left motors while 2 and 3 are right motors.
	  In a 6-motor config, 0, 1, and 2 are left while 3, 4, and 5 are right.*/
	ADIS16448_IMU imu;
	double curAng, desiredAng, angSpeed;
	double curPos, desPos;
	double throttle;
	double lastTime;
	double freq;
	double x, y; //robot position
	
	double testKP = 0, testKI = 0, testKD = 0; //used when test mode enabled
	double I = 0; //integral term
	
	double lastTurn = 0; //register the turn command at last cycle to determine if breaking is needed this cycle
	
	DriverStation station = DriverStation.getInstance();
	
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
			motor[i].setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, 5, 10); // modify the encoder refresh rate
			
			motor[i].configNominalOutputForward(0, 10);
			motor[i].configNominalOutputReverse(0, 10);
			motor[i].configPeakOutputForward(1, 10);
			motor[i].configPeakOutputReverse(-1, 10);
			
			motor[i].config_kF(0, 0, 10);
			motor[i].config_kP(0, 0.24, 10);
			motor[i].config_kI(0, 0, 10);
			motor[i].config_kD(0, 0.005, 10);
			
			motor[i].configOpenloopRamp(0.1, 10);
			motor[i].configClosedloopRamp(0.1, 10);
		}
		
		imu = new ADIS16448_IMU();
		imu.calibrate(); //Init gyro.
		
		freq = _freq;
		
		x = 0;
		y = 0;
	}
	
	private double getCurPos() {
		double encoderReadOut = (motor[0].getSelectedSensorPosition(0)+motor[2].getSelectedSensorPosition(0))/2;
		return (encoderReadOut/wheelMultiplier*wheelCircumfrence);
	}
	
	private void updateMotor(double leftOutput, double rightOutput, ControlMode mode) {
		SmartDashboard.putString(SDLMotor, ""+leftOutput);
		SmartDashboard.putString(SDRMotor, ""+rightOutput);
		if (mode == ControlMode.Velocity) {
			motor[0].set(mode, leftOutput*maxRevPer100ms*wheelMultiplier);
			motor[1].set(ControlMode.Follower, motor[0].getDeviceID());
			motor[2].set(mode, rightOutput*maxRevPer100ms*wheelMultiplier);
			motor[3].set(ControlMode.Follower, motor[2].getDeviceID());
		} else if (mode == ControlMode.PercentOutput) {
			
		}
	}
	
	public void updateVelocity(double _throttle, double _turn) {
		throttle = _throttle;
		if (Math.abs(_turn) > 0) {
			desiredAng = desiredAng+_turn*joystickMultiplier;
		} else if (Math.abs(lastTurn)>0 && _turn==0) {
			desiredAng = imu.getAngleZ()+imu.getRate()/5;
			I = 0;
		} else if (_throttle == 0 && _turn == 0 && lastTurn == 0) {
			desiredAng = imu.getAngleZ();
		}
		lastTurn = _turn;
	}
	
	public void updatePosition(double _angle, double _displacement) {
		//achieve angle
		desiredAng = curAng + _angle;
		System.out.println("ang and disp" + _angle +" "+ _displacement + " desiAng " + desiredAng);
		double startTime = System.currentTimeMillis();
		while (Math.abs(desiredAng-imu.getAngleZ())>1 || (System.currentTimeMillis()-startTime)<1000 || station.isAutonomous()) {
		}
		
		//achieve displacement
		/*curPos = (motor[0].getSelectedSensorPosition(0)+motor[2].getSelectedSensorPosition(0))/2;
		curPos = curPos/wheelMultiplier*wheelCircumfrence;*/
		desPos = curPos+_displacement;
		startTime = System.currentTimeMillis();
		while (station.isAutonomous()) {
			/*curPos = (motor[0].getSelectedSensorPosition(0)+motor[2].getSelectedSensorPosition(0))/2;
			curPos = curPos/wheelMultiplier*wheelCircumfrence;*/
			double error = desPos-curPos;
			
			if (Math.abs(error) < 0.05 /*|| (System.currentTimeMillis()-startTime)>_displacement*2000*/) {
				updateVelocity(0, 0);
				break;
			} else {
				//double power = Math.abs(0.25-Math.pow((error-_displacement/2), 4)/(0.625*Math.pow(_displacement, 4)));
				//double power = Math.abs(0.6-Math.pow((error-_displacement/2), 2)/(0.71*Math.pow(_displacement, 2)));
				double iniP = 0.2, finP = 0.35+Math.abs(_displacement)*0.1;
				double dter = (16/Math.pow(_displacement, 2)+2);
				double a = (finP-iniP)*dter;
				double b = +Math.abs(_displacement)/2;
				double c = iniP-a/(2+Math.pow(b, 2));
				double power = a/(2+Math.pow((Math.abs(error)-b), 2))+c;
				
				if (error > 0) {
					System.out.println(power+" "+curPos+" "+error);
					updateVelocity(power, 0);
				} else {
					System.out.println(power+" "+curPos+" "+error);
					updateVelocity(-power, 0);
				}
			}
		}
	}
	
	public void updateCoordinate(double _x, double _y) {
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
	
	public void setPIDConstants(double _kP, double _kI, double _kD) {
		testKP = _kP;
		testKI = _kI;
		testKD = _kD;
	}
	
	private double updatePID(double error, double omega, double deltaTime) {
		double kP = 0, kI = 0, kD = 0;
		double P = 0, D = 0;
		if (driveTestMode) {
			kP = testKP;
			kI = testKI;
			kD = testKD;
		} else {
			kP = constkP;
			kI = constkI;
			kD = constkD;
		}
		
		double output = 0;
		
		P = kP * error;
		
		I = I + kI * error*deltaTime/1000;
		if (I > 12) {
			I = 12;
		} else if (I < -12) {
			I = -12;
		}
		
		D = (-kD) * omega;
		
		/*SmartDashboard.putString(SDkP, "P: "+P);
		SmartDashboard.putString(SDkI, "I: "+I);
		SmartDashboard.putString(SDkD, "D: "+D);*/
		
		output = P + I + D;
		
		return output;
	}
	
	public boolean zeroSensor() {
		imu.reset();
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		curAng = imu.getAngleZ();
		desiredAng = curAng;
		x = 0;
		y = 0;
		return true;
	}
	
	public void run() {
		curAng = imu.getAngleZ();
		curPos = getCurPos();
		
		desiredAng = curAng;
		
		throttle = 0;
		
		lastTime = System.currentTimeMillis(); //Init lastTime for integral calculation.
		
		while (true) {
			try {
				Thread.sleep((long)(1000/freq));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			curAng = imu.getAngleZ();
			SmartDashboard.putString(SDcurAng, ""+curAng);
			
			curPos = getCurPos();
			SmartDashboard.putString(SDcurPos, ""+curPos);
			//SmartDashboard.putString(SDkP, ""+motor[0].getSelectedSensorVelocity(0));
			//SmartDashboard.putString(SDkI, ""+motor[2].getSelectedSensorVelocity(0));
			
			double error = desiredAng-curAng; //Get error
			error = error%360;
			if (error > 180) {
				error -= 360;
			} else if (error < -180) {
				error += 360;
			}
			SmartDashboard.putString(SDAngErr, ""+error);

			double currentTime = System.currentTimeMillis();
			double deltaTime = currentTime - lastTime; //Get deltaTime
			lastTime = currentTime;

			angSpeed = imu.getRate(); //Get omega

			double output = updatePID(error, angSpeed, deltaTime);
			if (output > 80) {
				output = 80;
			} else if (output < -80) {
				output = -80;
			}
			
			updateMotor(throttle+output/100, throttle-output/100, ControlMode.Velocity);
		}
	}
}
