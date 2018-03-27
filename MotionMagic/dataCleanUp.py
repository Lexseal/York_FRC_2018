import numpy as np
import math

file = open("test.csv", "r")
contents = file.readlines()

path = []
file = open("cleanUp.csv", "w")
usefulData = False
timeBias = 0
x = 0
y = 0
lastTime = 0
lastPos = 0
for line in contents:
	line = line.split(",")
	line = list(map(float, line))
	if (usefulData == False):
		if (line[5]!=0):
			usefulData = True
			timeBias = line[0]
		else:
			continue

	'''theta = line[1]*3.1415926/180
	v = line[5]
	deltaTime = line[0]-lastTime
	lastTime = line[0]
	dc = deltaTime*v
	dx = dc*math.sin(theta)
	dy = dc*math.cos(theta)
	x+=dx
	y+=dy'''
	curPos = line[4]
	if ((curPos-lastPos<0 and line[5]>0) or (curPos-lastPos>0 and line[5]<0)):
		continue
	lastPos = curPos
	vector = str(format(line[0]-timeBias, ".5f"))+", "+str(format(line[1], ".5f"))+", "+str(format(line[2], ".5f"))+", "+str(format(line[3], ".5f"))+", "+str(format(line[4], ".5f"))+", "+str(format(line[5], ".5f"))+", "+str(format(line[6], ".5f"))+", "+str(format(line[7], ".5f"))+", "+str(format(line[8], ".5f"))+", "+str(format(line[9], ".5f"))+", "+str(format(line[10], ".5f"))+"\n"
	#vector = str(format(line[0]-timeBias, ".5f"))+", "+str(format(line[1], ".5f"))+", "+str(format(line[2], ".5f"))+", "+str(format(line[3], ".5f"))+", "+str(format(line[4], ".5f"))+", "+str(format(line[5], ".5f"))+", "+str(format(x, ".5f"))+", "+str(format(y, ".5f"))+", "+str(format(line[6], ".5f"))+", "+str(format(line[7], ".5f"))+", "+str(format(line[8], ".5f"))+"\n"
	file.write(vector)
	print(vector)
