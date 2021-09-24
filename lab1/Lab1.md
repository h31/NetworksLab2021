The sending (receiving) side encodes (decodes) the data using module "Serialization.py" (selfmade JSON).

File "Serialization.py" contains:
1) the "dump" function for converting a dictionary into byte array;
2) the "load" function for converting a byte array into dictionary.

Protocol:
1) the Client sends his nickname to the Server: {"nickname":"Ivan"}
2) the Server checks if the nickname isn't already taken, and returns a positive response, otherwise a negative one:
{"status":"success"} / {"status":"error: this nickname is already taken"}
3) after connecting to the Server, the Client sends messages with the text field or text, attachment and data fields:
{"text":"Hello"} / {"text":"Hello", "attachment":"1.jpg", "data":b"..."}
4) the Server forwards each received message to all clients except the sender:
{"time":"12:34", "nickname": "Ivan", "text":"Hello"} / {"time":"12:34", "nickname": "Ivan", "text":"Hello", "attachment":"1.jpg", "data":b"..."} 