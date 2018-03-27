import numpy as np

file = open("cleanUp.csv", "r")
contents = file.readlines()

path = []
for line in contents:
	line = line.split(",")
	path.append(list(map(float, line)))

stages = []
stage = []
lastSpeed = 1
iter = 0
newVectors = []
for vector in path:
	iter += 1
	curSpeed = vector[5]
	if ((curSpeed>0 and lastSpeed<=0) or (curSpeed<0 and lastSpeed>=0) or (curSpeed==0 and abs(lastSpeed)!=0)):
		stages.append(np.array(stage))
		stage = []
	elif (iter == len(path)):
		stage.append(np.array(vector))
		stages.append(np.array(stage))
	stage.append(np.array(vector))
	lastSpeed = curSpeed

maxPosAcc = 0.86
maxNegAcc = -1.36
maxSpeed = 2

#maxPosAcc = 0.86
#maxNegAcc = -0.4
#maxSpeed = 1.8

file = open("timeOptimal.csv", "w")
vectors = []
lastDist = 0
timeElapsed = 0
iBias = 0
for i in range(len(stages)):
	stage = stages[i-iBias]
	if (len(stage)<2):
		print(i)
		del stages[i-iBias]
		iBias += 1
		continue
	initPos = stage[0][4]
	finalPos = stage[len(stage)-1][4]
	disp = finalPos-initPos
	deltaTime = 0.001
	disTravelled = 0
	speed = 0
	lastSpeed = 0
	acc = 0
	itera = 0
	cutOffIdx = 0
	while (abs(disTravelled) < abs(disp)):
		vector = []
		vector.append(timeElapsed)
		vector.append(lastDist+disTravelled)
		#print(lastDist, disTravelled)
		vector.append(speed)
		if (disp >= 0):
			slowDownTime = -speed/maxNegAcc
			projectedDisp = disTravelled+speed*slowDownTime/2
			if (projectedDisp < disp):
				speed += maxPosAcc*deltaTime
				acc = maxPosAcc
				if (speed > maxSpeed):
					speed = maxSpeed
			else:
				speed += maxNegAcc*deltaTime
				acc = maxNegAcc
		else:
			slowDownTime = -speed/maxPosAcc
			projectedDisp = disTravelled+speed*slowDownTime/2
			if (projectedDisp > disp):
				speed += maxNegAcc*deltaTime
				acc = maxNegAcc
				if (speed < -maxSpeed):
					speed = -maxSpeed
			else:
				speed += maxPosAcc*deltaTime
				acc = maxPosAcc
		disTravelled += (lastSpeed+speed)/2*deltaTime
		lastSpeed = speed
		timeElapsed += deltaTime
		vector.append(acc)	
		vector.append(projectedDisp)

		if (itera%20 == 0):
			curDisp = vector[1]
			targetIdx = cutOffIdx
			for n in range(cutOffIdx, int(len(stage)-1)):
				 wayPointDisp = stage[n][4]
				 if (disp >= 0):
				 	if (wayPointDisp<curDisp):
				 		targetIdx = n
				 	else:
				 		break
				 else:
				 	if (wayPointDisp>curDisp):
				 		targetIdx = n
				 	else:
				 		break
			cutOffIdx = targetIdx

			wayPoint = stage[targetIdx]

			time = vector[0]

			theta = wayPoint[1]

			'''lastIdx = targetIdx-1
			if (lastIdx < 0):
				lastIdx = 0
			nextIdx = targetIdx+1
			if (nextIdx >= len(path)):
				nextIdx = len(path)-1
			omega = (stage[nextIdx][1]-stage[lastIdx][1])/(0.02*2)'''

			s = vector[1]

			v = vector[2]

			x = wayPoint[6]

			y = wayPoint[7]

			liftPos = wayPoint[8]

			intakeSpeedL = wayPoint[9]
			intakeSpeedR = wayPoint[10]
			newVector = [time, theta, s, v, x, y, liftPos, intakeSpeedL, intakeSpeedR]
			newVectors.append(newVector)
			#newVector = str(format(time, ".5f"))+", "+str(format(theta, ".5f"))+", "+str(format(omega, ".5f"))+", "+str(format(s, ".5f"))+", "+str(format(v, ".5f"))+", "+str(format(x, ".5f"))+", "+str(format(y, ".5f"))+", "+str(format(liftPos, ".5f"))+", "+str(format(intakeSpeedL, ".5f"))+", "+str(format(intakeSpeedR, ".5f"))+", "+str(targetIdx)+"\n"
		itera += 1

		vectors.append(vector)
	lastDist += disTravelled 

for i in range(len(newVectors)):
	vector = newVectors[i]
	lastI = i-1
	nextI = i+1
	if (lastI < 0):
		lastI = 0
	if (nextI >= len(newVectors)):
		nextI = len(newVectors)-1
	lastTheta = newVectors[lastI][1]
	nextTheta = newVectors[nextI][1]
	deltaTime = newVectors[nextI][0]-newVectors[lastI][0]
	omega = (nextTheta-lastTheta)/deltaTime
	newVector = str(format(vector[0], ".5f"))+", "+str(format(vector[1], ".5f"))+", "+str(format(omega, ".5f"))+", "+str(format(vector[2], ".5f"))+", "+str(format(vector[3], ".5f"))+", "+str(format(vector[4], ".5f"))+", "+str(format(vector[5], ".5f"))+", "+str(format(vector[6], ".5f"))+", "+str(format(vector[7], ".5f"))+", "+str(format(vector[8], ".5f"))+"\n"
	file.write(newVector)
	print(newVector)

'''effectiveVectors = []
cutOffIdx = 0
deltaTime = 0.02
file = open("timeOptimal.csv", "w")
maxOmega = -1
for i in range((len(vectors))):
	if (i%(deltaTime*1000) == 0):
		vector = vectors[i]
		disp = vector[1]
		targetIdx = cutOffIdx
		lowestErr = 10
		speed = vector[2]
		for n in range(cutOffIdx, int(cutOffIdx+20)): 
			wayPointDisp = path[n][4]
			error = abs(wayPointDisp-disp)
			if (error < lowestErr):
				lowestErr = error
				targetIdx = n
		cutOffIdx = targetIdx

		#wayPoint = [fakeRuntime, theta, omega1, omega2, s, v, x, y, lift, lIntake, rIntake]
		#vector = [runtime, s, v]
		wayPoint = path[targetIdx]

		time = vector[0]

		theta = wayPoint[1]

		lastIdx = targetIdx-1
		if (lastIdx < 0):
			lastIdx = 0
		nextIdx = targetIdx+1
		if (nextIdx >= len(path)):
			nextIdx = len(path)-1
		omega = (path[nextIdx][1]-path[lastIdx][1])/(deltaTime*2)
		if (maxOmega < omega):
			maxOmega = omega

		s = vector[1]

		v = vector[2]

		x = wayPoint[6]

		y = wayPoint[7]

		liftPos = wayPoint[8]

		intakeSpeedL = wayPoint[9]
		intakeSpeedR = wayPoint[10]

		vector = str(format(time, ".5f"))+", "+str(format(theta, ".5f"))+", "+str(format(omega, ".5f"))+", "+str(format(s, ".5f"))+", "+str(format(v, ".5f"))+", "+str(format(x, ".5f"))+", "+str(format(y, ".5f"))+", "+str(format(liftPos, ".5f"))+", "+str(format(intakeSpeedL, ".5f"))+", "+str(format(intakeSpeedR, ".5f"))+", "+str(targetIdx)+"\n"

		#effectiveVectors.append(vector)
		file.write(vector)
		print(vector)
print(maxOmega)'''
