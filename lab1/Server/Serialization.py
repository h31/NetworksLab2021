def strToBytes(string): # convert " to \"
	result = b""
	for i in range(len(string)):
		if string[i] == "\"":
			result += b'\\'
		result += string[i].encode("utf-8")
	return result

def bytesToStr(input_bytes): # convert \" to "
	string = input_bytes.decode("utf-8")
	result = ""
	i = 0
	while i < len(string):
		if string[i] == "\\":
			if i != len(string) - 1:
				if string[i + 1] == "\"":
					i += 1
		result += string[i]
		i += 1
	return result

# convert a dictionary to bytes
def dump(dictionary):
	if not isinstance(dictionary, dict): return None
	if len(dictionary) == 0: return None
	return convertToBytes(dictionary)

def convertToBytes(container):
	result = b''
	if isinstance(container, dict):
		result += b'{'
		for key in container:
			if chr(result[-1]) in ("\"", "}", "]"): result += b','
			result += b'"' + strToBytes(key) + b'":'
			if isinstance(container[key], dict) or isinstance(container[key], list):
				result += convertToBytes(container[key])
			else:
				if isinstance(container[key], bytes):
					result += b'"' + container[key] + b'"'
				else:
					result += b'"' + strToBytes(container[key]) + b'"'
		result += b'}'
	
	if isinstance(container, list):
		result += b'['
		for item in container:
			if chr(result[-1]) in ("\"", "}", "]"): result += b','
			if isinstance(item, dict) or isinstance(item, list):
				result += convertToBytes(item)
			else:
				if isinstance(item, bytes):
					result += b'"' + item + b'"'
				else:
					result += b'"' + strToBytes(item) + b'"'
		result += b']'
	return result

# convert bytes to a dictionary	
def load(input_data):
	if not isinstance(input_data, bytes): return None
	if len(input_data) == 0: return None
	
	global data, index
	data = input_data
	index = 0
	return convertToDictionary(len(data) - 1)

def convertToDictionary(lastCharacterIndex):
	global data, index
	result = {}
	while True:
		key = bytesToStr(getValue())
		while not chr(data[index]) in ("{", "[", "\""):
			index += 1
		if chr(data[index]) == "{":
			value = convertToDictionary(findLastCharacterIndex())
			index += 1
		if chr(data[index]) == "[":
			value = convertToList(findLastCharacterIndex())
			index += 1
		if chr(data[index]) == "\"":
			value = getValue()
		result[key] = value
		if index >= lastCharacterIndex: break
	return result

def findLastCharacterIndex():
	global data, index
	if chr(data[index]) == "{":
		closingSign = "}"
	else:
		closingSign = "]"
	
	result = index
	cnt = 1
	while cnt != 0:
		result += 1
		if data[result] == data[index]: cnt += 1
		if chr(data[result]) == closingSign: cnt -= 1
	return result - 1

def convertToList(lastCharacterIndex):
	global data, index
	result = []
	index += 1
	while True:
		while not chr(data[index]) in ("{", "[", "\""):
			index += 1
		if chr(data[index]) == "{":
			value = convertToDictionary(findLastCharacterIndex())
			index += 1
		if chr(data[index]) == "[":
			value = convertToList(findLastCharacterIndex())
			index += 1
		if chr(data[index]) == "\"":
			value = getValue()
		result.append(value)
		if index >= lastCharacterIndex: break
	return result
	
def getValue():
	global data, index
	while chr(data[index]) != "\"":
		index += 1
	
	start = index + 1
	while True:
		index += 1
		if chr(data[index]) == "\"": break
		if chr(data[index]) == "\\" and chr(data[index + 1]) == "\"": index += 1
		
	index += 1
	return data[start:index - 1]

# usage sample
#dictionary = {"mes\"sage1" : {"time": "1", "author" : ["John", "Bob"], "text" : "Hello \"World\"", "attachment" : "no"},
#			"message2" : {"time": {"1" : "2"}, "author" : "John", "text" : "Hi", "attachment" : "no",
#			"message3" : [[b'test\\"data', {"55" : ["", [b'test\\"data', "8"]]}]]}}
#data = dump(dictionary)
#print(data)
#result = load(data)
#print(result)