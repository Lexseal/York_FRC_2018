package org.usfirst.frc.team5171.robot.subsystems;

import static org.usfirst.frc.team5171.robot.Macro.*;

import edu.wpi.first.wpilibj.Joystick;

public class Controller extends Thread {
	int[] portList;
	double[] inputList = new double[MAX];
	double[] lastInput = new double[MAX];
	double deadBand, cutOff, freq, expo;
	Joystick stick;
	
	public Controller(int _joyPort, int _portlist[], double _deadBand, double _cutOff, double _freq, double _expo) {
		portList = _portlist;
		for (int i = 0; i < portList.length; i++) {
			inputList[portList[i]] = 0;
		}
		lastInput = inputList;
		deadBand = _deadBand/100;
		cutOff = _cutOff/100;
		freq = _freq;
		expo = _expo;
		stick = new Joystick(_joyPort);
	}
	
	public double get(int _port) {
		if (inputList[_port] < 0) {
			return -Math.pow(inputList[_port], expo);
		}
		return Math.pow(inputList[_port], expo);
	}
	
	public void run() {
		while(true) {
			try {
				sleep((long)(1000/freq));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for (int i = 0; i < portList.length; i++) {
				int curPort = portList[i];
				inputList[curPort] = stick.getRawAxis(curPort);
				if (Math.abs(inputList[curPort]-lastInput[curPort])/(1/freq)<=deadBand) {
					inputList[curPort] = lastInput[curPort];
				} else {
					inputList[curPort] = (inputList[curPort]+lastInput[curPort])/2;
				}
				if (Math.abs(inputList[curPort]) < cutOff) {
					inputList[curPort] = 0;
				}
			}
		}
	}
}
