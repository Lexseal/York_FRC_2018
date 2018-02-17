package org.usfirst.frc.team5171.robot.subsystems;

public class Macro {
	public static final boolean betaFeature = false;
	public static final boolean testMode = false;
	
	public static final int LEFT_X = 0;
	public static final int THROTTLE = 1;
	public static final int LEFT_UP = 2;
	public static final int RIGHT_UP = 3;
	public static final int TURN = 4;
	public static final int RIGHT_Y = 5;
	public static final int MAX = 16;
	
	public static final int encoderLeft = 0;
	public static final int encoderRight = 2;
	
	public static final double constkP = 1.8;
	public static final double constkI = 1;
	public static final double constkD = 0.24;
	
	public static final double joystickMultiplier = 360; //Robot rotation per second.
	
	public static final double wheelMultiplier = 4096;
	
	public static final double maxSpeed = 4; // m/s
	public static final double wheelCircumfrence = 0.47878; // meters
	public static final double maxRevPer100ms = maxSpeed/wheelCircumfrence/10; // revs
	
	
	public static final String SDcurAng = "DB/String 0";
	public static final String SDdesAng = "DB/String 1";
	public static final String SDAngErr = "DB/String 2";
	public static final String SDcurPos = "DB/String 3";
	public static final String SDdesPos = "DB/String 4";
	public static final String SDkP = "DB/String 5";
	public static final String SDkI = "DB/String 6";
	public static final String SDkD = "DB/String 7";
	public static final String SDTHROTTLE = "DB/String 8";
	public static final String SDTURN = "DB/String 9";
}
