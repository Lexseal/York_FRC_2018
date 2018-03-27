import numpy as np
import matplotlib.pyplot as plt
import time
import math

plt.axis([-5, 5, 0, 10])
plt.ion()

def draw(fileName, idx, correctionEnabled, loss):
	file = open(fileName+".csv", "r")
	contents = file.readlines()

	x = 0
	y = 0
	vectors = []
	for vector in contents:
		vector = vector.split(",")
		vector = list(map(float, vector))
		vectors.append(np.array(vector))

	#vector = [runtime, theta, omega1, omega2, s, v, x, y, lift, lIntake, rIntake]
	vectors = np.array(vectors)
	endTime = vectors[len(vectors)-1][0]
	startTime = time.time()
	lastIdx = 0
	lastTime = 0
	maxCorrection = 3
	while(lastIdx < len(vectors)):
		time.sleep(0.001)
		runTime = time.time()-startTime
		#print(runTime)
		if(runTime >= vectors[lastIdx][0]):
			pltSrt = time.time()
			vector = vectors[lastIdx]
			lastIdx += loss

			desX = vector[idx+1]
			desY = vector[idx+2]
			angleCorrection = 180/3.1415926*math.atan2(desX-x, desY-y)-vector[1];
			hypoCorrection = math.hypot(desX-x, desY-y);
			angleCorrection *= math.pow(hypoCorrection, 1);
			#print(desX, x, desY, y, angleCorrection)
			if (angleCorrection > maxCorrection):
				angleCorrection = maxCorrection
			elif (angleCorrection < -maxCorrection):
				angleCorrection = -maxCorrection
			if (vector[idx] < 0):
				angleCorrection *= -1

			if (correctionEnabled):
				theta = (vector[1]+angleCorrection)*3.1415926/180
			else:
				theta = (vector[1])*3.1415926/180
			v = vector[idx]
			deltaTime = vector[0]-lastTime
			lastTime = vector[0]
			dc = v*deltaTime
			dx = dc*math.sin(theta)
			dy = dc*math.cos(theta)
			x += dx
			y += dy

			plt.scatter(x, y, s=1)
			print(x, y)
			plt.pause(0.001)

#draw("cleanUp", 5, True, 1)
#draw("timeOptimal", 4, False)
#draw("timeOptimal", 4, False, 1)
fileName = input("fileName: ")
draw(fileName, 4, True, 3)

fileNameList = list(fileName)
for i in range(len(fileNameList)):
	if (fileNameList[i] == "R"):
		fileNameList[i] = "L"
	elif (fileNameList[i] == "L"):
		fileNameList[i] = "R"
fileName = "".join(fileNameList)
draw(fileName, 4, True, 3)
time.sleep(3600)


