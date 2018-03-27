import numpy as np

fileName = input("fileName: ")
file = open(fileName, "r")
contents = file.readlines()

fileNameList = list(fileName)
for i in range(len(fileNameList)):
	if (fileNameList[i] == "R"):
		fileNameList[i] = "L"
	elif (fileNameList[i] == "L"):
		fileNameList[i] = "R"

fileName = "".join(fileNameList)
print(fileName)
file = open(fileName, "w")
for line in contents:
	line = line.split(",")
	line = list(map(float, line))
	vector = str(format(line[0], ".5f"))+", "+str(format(-line[1], ".5f"))+", "+str(format(-line[2], ".5f"))+", "+str(format(line[3], ".5f"))+", "+str(format(line[4], ".5f"))+", "+str(format(-line[5], ".5f"))+", "+str(format(line[6], ".5f"))+", "+str(format(line[7], ".5f"))+", "+str(format(line[8], ".5f"))+", "+str(format(line[9], ".5f"))+"\n"
	#vector = str(format(line[0]-timeBias, ".5f"))+", "+str(format(line[1], ".5f"))+", "+str(format(line[2], ".5f"))+", "+str(format(line[3], ".5f"))+", "+str(format(line[4], ".5f"))+", "+str(format(line[5], ".5f"))+", "+str(format(x, ".5f"))+", "+str(format(y, ".5f"))+", "+str(format(line[6], ".5f"))+", "+str(format(line[7], ".5f"))+", "+str(format(line[8], ".5f"))+"\n"
	file.write(vector)