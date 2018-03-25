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
	double curAng, desAng, curAngSpeed, desAngSpeed;
	double curPos, desPos, curSpeed, desSpeed;
	double throttle = 0, lastThrottle = 0;
	double desX, x, desY, y; //robot position
	double lastTime, deltaTime;
	double freq;
	
	double testKP = 0, testKI = 0, testKD = 0; //used when test mode enabled
	double I = 0; //integral term
	
	double lastTurn = 0; //register the turn command at last cycle to determine if breaking is needed this cycle
	
	double restrictionMultiplier = 1;
	double liftHeight = 0;
	
	DriverStation station = DriverStation.getInstance();
	
	boolean isFollowMode = false;
	
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
			
			motor[i].configOpenloopRamp(0.35, 10);
			motor[i].configClosedloopRamp(0.35, 10);
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
	
	private void updateCoordinate() {
		double dc = curSpeed*deltaTime/1000;
		x += Math.sin(curAng*3.1415926/180)*dc;
		y += Math.cos(curAng*3.1415926/180)*dc;
		//System.out.println(x+", "+y);
	}
	
	public double getCurSpeed() {
		double encoderReadOut = (motor[0].getSelectedSensorVelocity(0)+motor[2].getSelectedSensorVelocity(0))/2;
		return (encoderReadOut/wheelMultiplier*wheelCircumfrence)*10;
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
	
	public void updateVelocity(double _speed, double _turn) {
		throttle = _speed;
		if (Math.abs(_turn) > 0) {
			desAng = desAng+_turn*joystickMultiplier;
		} else if (Math.abs(lastTurn)>0 && _turn==0) {
			desAng = imu.getAngleZ()+imu.getRate()/5;
			I = 0;
		} else if (_speed == 0 && _turn == 0 && lastTurn == 0) {
			desAng = imu.getAngleZ();
		}
		lastTurn = _turn;
	}
	
	public void updateDisplacement(double _angle, double _displacement) {
		//achieve angle
		desAng = curAng + _angle;
		System.out.println("ang and disp" + _angle +" "+ _displacement + " desiAng " + desAng);
		double startTime = System.currentTimeMillis();
		while (Math.abs(desAng-imu.getAngleZ())>1 || (System.currentTimeMillis()-startTime)<1000 || (station.isAutonomous() && station.isEnabled())) {
		}
		
		//achieve displacement
		/*curPos = (motor[0].getSelectedSensorPosition(0)+motor[2].getSelectedSensorPosition(0))/2;
		curPos = curPos/wheelMultiplier*wheelCircumfrence;*/
		desPos = curPos+_displacement;
		startTime = System.currentTimeMillis();
		while (station.isAutonomous() && station.isEnabled()) {
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
	
	public void updatePosition(double _x, double _y) {
		double dx = _x-x;
		double dy = _y-y;
		double theta = -((Math.atan2(dy, dx))*(180/3.1416)-90);
		double correctAng = (curAng)%360;
		double dTheta = theta-correctAng;
		double displacement = Math.hypot(dx, dy);
		System.out.println("dTheta" + dTheta +" "+ displacement);
		updateDisplacement(dTheta, displacement);
	}
	
	public void followFinished() {
		isFollowMode = false;
	}
	
	public void follow(double angle, double omega, double position, double speed, double _x, double _y) {
		isFollowMode = true;
		desAngSpeed = omega;
		desAng = angle;
		desSpeed = speed;
		desPos = position;
		desX = _x;
		desY = _y;
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
		motor[0].setSelectedSensorPosition(0, 0, 10);
		motor[2].setSelectedSensorPosition(0, 0, 10);
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		curAng = imu.getAngleZ();
		desAng = curAng;
		x = 0;
		y = 0;
		return true;
	}
	
	public void restrictedAcc() {
		restrictionMultiplier = 0.3;
	}
	
	public void normalAcc() {
		restrictionMultiplier = 1;
	}
	
	public void run() {
		curAng = imu.getAngleZ();
		curPos = getCurPos();
		
		desAng = curAng;
		
		throttle = 0;
		
		lastTime = System.currentTimeMillis(); //Init lastTime for integral calculation.
		
		while (true) {
			try {
				Thread.sleep((long)(1000/freq));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			curAng = imu.getAngleZ();
			curAngSpeed = imu.getRate(); //Get omega
			SmartDashboard.putString(SDcurAng, ""+curAng);
			
			curPos = getCurPos();
			curSpeed = getCurSpeed();
			SmartDashboard.putString(SDcurPos, ""+curSpeed);
			
			double currentTime = System.currentTimeMillis();
			deltaTime = currentTime - lastTime; //Get deltaTime
			lastTime = currentTime;
			
			updateCoordinate();
			
			if (isFollowMode) {
				double speedErr = desSpeed-curSpeed;
				double outputFromSpeed = desSpeed*0.52+speedErr*0.20;
				//0.58 0.4
				//outputFromSpeed = 0;
				
				double posErr = desPos-curPos;
				double outputFromPos = posErr*0.20;
				//System.out.println(speedErr+" "+posErr);
				//0.4
				//outputFromPos = 0;
				
				/*double kP1 = Double.parseDouble(SmartDashboard.getString(SDkP, ""));
				double kP2 = Double.parseDouble(SmartDashboard.getString(SDkI, ""));
				double kP3 = Double.parseDouble(SmartDashboard.getString(SDkD, ""));*/
				
				double angleCorrection = 180/3.1415925*Math.atan2(desX-x, desY-y)-curAng;
				double hypoCorrection = Math.hypot(desX-x, desY-y);
				angleCorrection *= Math.pow(hypoCorrection, 1);
				if (angleCorrection > 3){
					angleCorrection = 3;
				} else if (angleCorrection < -3) {
					angleCorrection = -3;
				}
				if (getCurSpeed() < 0) {
					angleCorrection *= -1;
				}
				System.out.println(angleCorrection);
				
				double omegaErr = desAngSpeed-curAngSpeed;
				//double outputFromOmega = desAngSpeed*0.0052+omegaErr*0.0016;
				//0.0052 0.0017
				//outputFromOmega = 0;
				
				double angErr = desAng-curAng;
				//angErr += angleCorrection;
				//double kP = Double.parseDouble(SmartDashboard.getString(SDkP, ""));
				//double outputFromAng = angErr*0.016;
				//0.0005
				//outputFromAng = 0;
				System.out.println(omegaErr+" "+angErr);
				
				//double outputL = outputFromSpeed+outputFromPos+outputFromAng+outputFromOmega;
				//double outputR = outputFromSpeed+outputFromPos-outputFromAng-outputFromOmega;
				double outputL = outputFromSpeed+outputFromPos+updatePID(angErr, curAngSpeed, deltaTime)/100+omegaErr*0.006;
				double outputR = outputFromSpeed+outputFromPos-updatePID(angErr, curAngSpeed, deltaTime)/100-omegaErr*0.006;
				
				updateMotor(outputL, outputR, ControlMode.Velocity);
				//System.out.println(outputL+" "+outputR);
			} else {
				double angError = desAng-curAng; //Get angle error
				angError = angError%360;
				if (angError > 180) {
					angError -= 360;
				} else if (angError < -180) {
					angError += 360;
				}
				SmartDashboard.putString(SDAngErr, ""+angError);
				
				double output = updatePID(angError, curAngSpeed, deltaTime);
				if (output > 70) {
					output = 70;
				} else if (output < -70) {
					output = -70;
				}
				liftHeight = Integer.parseInt(SmartDashboard.getString(SDLMotor, "1"));
				if ((throttle-lastThrottle) > maxForwardThrottleChange*(1.0/liftHeight)*restrictionMultiplier*(1.0/freq)) {
					throttle = lastThrottle+maxForwardThrottleChange*(1.0/liftHeight)*restrictionMultiplier*(1.0/freq);
				} else if ((lastThrottle-throttle) > maxReverseThrottleChange*(1.0/liftHeight)*restrictionMultiplier*(1.0/freq)) {
					throttle = lastThrottle-maxReverseThrottleChange*(1.0/liftHeight)*restrictionMultiplier*(1.0/freq);
				}
				System.out.println("accel X:"+imu.getAccelX()+" Y:"+imu.getAccelY()+" Z:"+imu.getAccelZ());
				lastThrottle = throttle;
				updateMotor(throttle+output/100, throttle-output/100, ControlMode.Velocity);
			}
		}
	}
	
	public double[] getAllSensorInfo() {
		double[] infoList = {imu.getAngleZ(), imu.getRate(), getCurPos(), getCurSpeed(), throttle, x, y};
		return infoList;
	}

	public void setLiftHeight(double curPos2) {
		liftHeight = curPos2; 
		
	}
}
