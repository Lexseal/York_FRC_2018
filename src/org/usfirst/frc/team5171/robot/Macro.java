package org.usfirst.frc.team5171.robot;

public class Macro {
	public static final boolean betaFeature = false;
	public static final boolean driveTestMode = false;
	public static final boolean liftTestMode = false;
	
	public static final int LEFT_X = 0;
	public static final int THROTTLE = 1;
	public static final int LEFT_UP = 2;
	public static final int RIGHT_UP = 3;
	public static final int TURN = 4;
	public static final int RIGHT_Y = 5;
	public static final int MAX = 16;
	
	public static final int A = 1;
	public static final int B = 2;
	public static final int X = 3;
	public static final int Y = 4;
	public static final int LB = 4;
	public static final int RB = 5;
	
	public static final int INTAKE_POS_BUTTON = 1;
	public static final int SWITCH_POS_BUTTON = 2;
	public static final int SCALE_POS_BUTTON = 3;
	public static final int LIFT_RECENTER = 4;
	public static final int INTAKE_BUTTON = 0;
	public static final int SPIT_OUT_BUTTON = 0;
	
	public static final int encoderLeft = 0;
	public static final int encoderRight = 2;
	
	public static final double constkP = 2.4;
	public static final double constkI = 2;
	public static final double constkD = 0.24;
	
	public static final double joystickMultiplier = 3; //Robot rotation per time unit. //change back to 3.2
	
	public static final double wheelMultiplier = 4096;
	
	public static final double maxSpeed = 4; // m/s
	public static final double wheelCircumfrence = 0.47878; // meters
	public static final double revPer100ms = 1.0/wheelCircumfrence/10;
	public static final double maxRevPer100ms = maxSpeed/wheelCircumfrence/10; // revs
	public static final double maxForwardThrottleChange = 4.3; //units/s
	public static final double maxReverseThrottleChange = 6.8; //units/s
	
	public static final double liftHeightPerRev = 12.545; // cm/rev
	public static final double liftkP = 0.08;
	public static final double liftkI = 0.0;
	public static final double liftkD = 0.0002;
	public static final double protectedLiftHeight = 140;
	public static final double liftHome = 0.1;
	public static final double liftSwitchHeight = 105;
	public static final double liftMaxHeight = 233.5;
	public static final double secondStageLanding = 134;
	
	public static final double recordTime = 30;
	
	public static final String SDcurAng = "DB/String 0";
	public static final String SDdesAng = "DB/String 1";
	public static final String SDAngErr = "DB/String 2";
	public static final String SDcurPos = "DB/String 3";
	public static final String SDdesPos = "DB/String 4";
	public static final String SDkP = "DB/String 5";
	public static final String SDkI = "DB/String 6";
	public static final String SDkD = "DB/String 7";
	public static final String SDLMotor = "DB/String 8";
	public static final String SDRMotor = "DB/String 9";
	
	public static final String switchFirst = "switchFirst";
	public static final String scaleFirst = "scaleFirst";
	public static final String leftStart = "L";
	public static final String middleStart = "M";
	public static final String middleWait = "MW";
	public static final String rightStart = "R";
}
