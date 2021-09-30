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
		while chr(data[index]) != "\"":
			if index == lastCharacterIndex: return result
			index += 1
		
		key = bytesToStr(getValue())
		
		while chr(data[index]) not in ("\"", "{", "["):
			index += 1
		
		if chr(data[index]) == "{":
			value = convertToDictionary(findLastCharacterIndex())
		if chr(data[index]) == "[":
			value = convertToList(findLastCharacterIndex())
		if chr(data[index]) == "\"":
			value = getValue()
		result[key] = value

def findLastCharacterIndex():
	global data, index
	if chr(data[index]) == "{":
		closingSign = "}"
	else:
		closingSign = "]"
	
	result = index
	cnt = 1
	inside_the_string = False
	while cnt != 0:
		result += 1
		if chr(data[result - 1]) != "\\" and chr(data[result]) == "\"":
			inside_the_string = not inside_the_string
		if not inside_the_string:
			if data[result] == data[index]: cnt += 1
			if chr(data[result]) == closingSign: cnt -= 1
	return result

def convertToList(lastCharacterIndex):
	global data, index
	result = []
	index += 1
	while True:
		while chr(data[index]) not in ("{", "[", "\""):
			if index == lastCharacterIndex: return result
			index += 1
		
		if chr(data[index]) == "{":
			value = convertToDictionary(findLastCharacterIndex())
		if chr(data[index]) == "[":
			value = convertToList(findLastCharacterIndex())
		if chr(data[index]) == "\"":
			value = getValue()
		result.append(value)
	
def getValue():
	global data, index
	start = index + 1
	while True:
		index += 1
		if chr(data[index]) == "\"": break
		if chr(data[index]) == "\\" and chr(data[index + 1]) == "\"": index += 1
		
	index += 1
	return data[start:index - 1]

# usage sample
# {"text1":"text2", "text3":[{"text4":["text5"]}, "text6", ["text7"]]} ->
# -> b'{"text1":"text2","text3":[{"text4":["text5"]},"text6",["text7"]]}' ->
# {"text1":"text2", "text3":[{"text4":["text5"]}, "text6", ["text7"]]}

#dictionary = {"message\"":"text2", "text3":[{"{text4}":["[text5]"]}, "text6", ["text7", [""], {}]]}
#data = dump(dictionary)
#print(data)
#result = load(data)
#print(result)