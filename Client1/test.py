import re

message = input()
files = re.findall(r'[^send].*', message)
fileName = files[0].strip(' ')
print(fileName)